#!/bin/bash

# JobPortal Load Testing Script
# This script performs comprehensive load testing on the JobPortal application

set -e

# Configuration
BASE_URL="http://localhost:8080"
CONCURRENT_USERS=10
REQUESTS_PER_ENDPOINT=100
TEST_DURATION=300  # 5 minutes

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Results arrays
declare -a RESPONSE_TIMES
declare -a ERROR_COUNT

echo -e "${BLUE}🚀 JobPortal Load Testing Started${NC}"
echo "=================================="
echo "Base URL: $BASE_URL"
echo "Concurrent Users: $CONCURRENT_USERS"
echo "Requests per Endpoint: $REQUESTS_PER_ENDPOINT"
echo "Test Duration: ${TEST_DURATION}s"
echo ""

# Function to test an endpoint
test_endpoint() {
    local endpoint="$1"
    local description="$2"
    local method="${3:-GET}"
    
    echo -e "${YELLOW}Testing: $description${NC}"
    echo "Endpoint: $endpoint"
    
    local total_time=0
    local success_count=0
    local error_count=0
    local min_time=9999
    local max_time=0
    
    for i in $(seq 1 $REQUESTS_PER_ENDPOINT); do
        local start_time=$(date +%s%N)
        local response_code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL$endpoint" 2>/dev/null || echo "000")
        local end_time=$(date +%s%N)
        
        local response_time=$(( (end_time - start_time) / 1000000 )) # Convert to milliseconds
        
        if [[ "$response_code" =~ ^[23] ]]; then
            ((success_count++))
            total_time=$((total_time + response_time))
            
            if [[ $response_time -lt $min_time ]]; then
                min_time=$response_time
            fi
            
            if [[ $response_time -gt $max_time ]]; then
                max_time=$response_time
            fi
        else
            ((error_count++))
        fi
        
        # Show progress
        if [[ $((i % 10)) -eq 0 ]]; then
            echo -n "."
        fi
    done
    
    echo "" # New line after progress dots
    
    # Calculate metrics
    local avg_time=0
    if [[ $success_count -gt 0 ]]; then
        avg_time=$((total_time / success_count))
    fi
    
    local success_rate=$((success_count * 100 / REQUESTS_PER_ENDPOINT))
    
    echo "Results:"
    echo "  Success Rate: ${success_rate}% ($success_count/$REQUESTS_PER_ENDPOINT)"
    echo "  Average Response Time: ${avg_time}ms"
    echo "  Min Response Time: ${min_time}ms"
    echo "  Max Response Time: ${max_time}ms"
    echo "  Errors: $error_count"
    
    # Performance evaluation
    if [[ $avg_time -lt 100 ]]; then
        echo -e "  Performance: ${GREEN}Excellent${NC} (<100ms)"
    elif [[ $avg_time -lt 500 ]]; then
        echo -e "  Performance: ${GREEN}Good${NC} (<500ms)"
    elif [[ $avg_time -lt 1000 ]]; then
        echo -e "  Performance: ${YELLOW}Average${NC} (<1s)"
    else
        echo -e "  Performance: ${RED}Poor${NC} (>1s)"
    fi
    
    echo ""
    
    # Store results for summary
    RESPONSE_TIMES+=("$description:$avg_time")
    ERROR_COUNT+=("$description:$error_count")
}

# Function to test concurrent users
test_concurrent_load() {
    echo -e "${YELLOW}Testing Concurrent Load (${CONCURRENT_USERS} users)${NC}"
    echo "Each user will make 10 requests to random endpoints..."
    
    local pids=()
    local start_time=$(date +%s)
    
    # Start concurrent users
    for i in $(seq 1 $CONCURRENT_USERS); do
        (
            for j in $(seq 1 10); do
                local endpoints=("/api/jobs" "/api/jobs/search?keyword=Java" "/api/jobs/1")
                local endpoint=${endpoints[$RANDOM % ${#endpoints[@]}]}
                curl -s "$BASE_URL$endpoint" > /dev/null 2>&1
                sleep 0.1
            done
        ) &
        pids+=($!)
    done
    
    # Wait for all users to complete
    for pid in "${pids[@]}"; do
        wait $pid
    done
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    echo "Concurrent load test completed in ${duration}s"
    echo ""
}

# Function to test cache performance
test_cache_performance() {
    echo -e "${YELLOW}Testing Cache Performance${NC}"
    
    # First request (cache miss)
    echo "First request (cache miss):"
    local start_time=$(date +%s%N)
    curl -s "$BASE_URL/api/jobs?page=0&size=5" > /dev/null
    local end_time=$(date +%s%N)
    local first_time=$(( (end_time - start_time) / 1000000 ))
    echo "  Time: ${first_time}ms"
    
    # Second request (cache hit)
    echo "Second request (cache hit):"
    start_time=$(date +%s%N)
    curl -s "$BASE_URL/api/jobs?page=0&size=5" > /dev/null
    end_time=$(date +%s%N)
    local second_time=$(( (end_time - start_time) / 1000000 ))
    echo "  Time: ${second_time}ms"
    
    # Calculate improvement
    if [[ $first_time -gt 0 ]]; then
        local improvement=$(( (first_time - second_time) * 100 / first_time ))
        echo -e "  Cache Improvement: ${GREEN}${improvement}%${NC} faster"
    fi
    
    echo ""
}

# Function to check system health during load
check_system_health() {
    echo -e "${YELLOW}System Health Check${NC}"
    
    # Check if application is responding
    local health_response=$(curl -s "$BASE_URL/actuator/health" 2>/dev/null || echo "ERROR")
    
    if [[ "$health_response" == *"UP"* ]]; then
        echo -e "Application Health: ${GREEN}UP${NC}"
    else
        echo -e "Application Health: ${RED}DOWN${NC}"
    fi
    
    # Check metrics endpoint
    local metrics_response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/metrics" 2>/dev/null)
    if [[ "$metrics_response" == "200" ]]; then
        echo -e "Metrics Endpoint: ${GREEN}Available${NC}"
    else
        echo -e "Metrics Endpoint: ${RED}Unavailable${NC}"
    fi
    
    echo ""
}

# Function to generate load test report
generate_report() {
    echo -e "${BLUE}📊 Load Testing Report${NC}"
    echo "======================"
    echo "Test Date: $(date)"
    echo "Test Configuration:"
    echo "  - Concurrent Users: $CONCURRENT_USERS"
    echo "  - Requests per Endpoint: $REQUESTS_PER_ENDPOINT"
    echo "  - Total Test Duration: ${TEST_DURATION}s"
    echo ""
    
    echo "Performance Summary:"
    for result in "${RESPONSE_TIMES[@]}"; do
        local endpoint=$(echo "$result" | cut -d: -f1)
        local time=$(echo "$result" | cut -d: -f2)
        printf "  %-30s %5sms\n" "$endpoint" "$time"
    done
    echo ""
    
    echo "Error Summary:"
    local total_errors=0
    for error in "${ERROR_COUNT[@]}"; do
        local endpoint=$(echo "$error" | cut -d: -f1)
        local count=$(echo "$error" | cut -d: -f2)
        printf "  %-30s %5s errors\n" "$endpoint" "$count"
        total_errors=$((total_errors + count))
    done
    
    echo ""
    echo "Overall Results:"
    local total_requests=$((${#RESPONSE_TIMES[@]} * REQUESTS_PER_ENDPOINT))
    local success_requests=$((total_requests - total_errors))
    local overall_success_rate=$((success_requests * 100 / total_requests))
    
    echo "  Total Requests: $total_requests"
    echo "  Successful: $success_requests"
    echo "  Failed: $total_errors"
    echo "  Overall Success Rate: ${overall_success_rate}%"
    
    if [[ $overall_success_rate -ge 95 ]]; then
        echo -e "  Overall Status: ${GREEN}EXCELLENT${NC}"
    elif [[ $overall_success_rate -ge 90 ]]; then
        echo -e "  Overall Status: ${GREEN}GOOD${NC}"
    elif [[ $overall_success_rate -ge 80 ]]; then
        echo -e "  Overall Status: ${YELLOW}ACCEPTABLE${NC}"
    else
        echo -e "  Overall Status: ${RED}POOR${NC}"
    fi
    
    echo ""
    echo "Recommendations:"
    if [[ $total_errors -gt 0 ]]; then
        echo -e "  ${YELLOW}⚠️  Consider investigating error causes${NC}"
    fi
    
    # Check for slow endpoints
    for result in "${RESPONSE_TIMES[@]}"; do
        local time=$(echo "$result" | cut -d: -f2)
        if [[ $time -gt 1000 ]]; then
            local endpoint=$(echo "$result" | cut -d: -f1)
            echo -e "  ${YELLOW}⚠️  $endpoint is slow (${time}ms) - consider optimization${NC}"
        fi
    done
    
    echo -e "  ${GREEN}✅ Consider setting up continuous monitoring${NC}"
    echo -e "  ${GREEN}✅ Monitor cache hit rates for optimal performance${NC}"
}

# Main function
main() {
    # Check if application is running
    if ! curl -s "$BASE_URL/actuator/health" > /dev/null; then
        echo -e "${RED}❌ Application is not running at $BASE_URL${NC}"
        echo "Please start the application before running load tests"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Application is running${NC}"
    echo ""
    
    # Run system health check
    check_system_health
    
    # Run cache performance test
    test_cache_performance
    
    # Test individual endpoints
    test_endpoint "/api/jobs?page=0&size=10" "Job Listings"
    test_endpoint "/api/jobs/search?keyword=Java" "Job Search"
    test_endpoint "/api/jobs/1" "Single Job Details"
    test_endpoint "/actuator/health" "Health Check"
    test_endpoint "/actuator/metrics" "Metrics Endpoint"
    
    # Test concurrent load
    test_concurrent_load
    
    # Final health check
    check_system_health
    
    # Generate report
    generate_report
    
    echo -e "${GREEN}🎉 Load testing completed successfully!${NC}"
}

# Run main function
main "$@"
