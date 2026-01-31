#!/bin/bash

# Job Portal Frontend Test Script
echo "🚀 Starting Job Portal Frontend Tests"
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Function to test API endpoints
test_api() {
    local endpoint=$1
    local method=${2:-GET}
    local data=${3:-""}
    local expected_status=${4:-200}
    local description=$5
    
    echo -n "Testing: $description... "
    
    if [ "$method" = "POST" ]; then
        status=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$endpoint" -H "Content-Type: application/json" -d "$data")
    else
        status=$(curl -s -o /dev/null -w "%{http_code}" "$endpoint")
    fi
    
    if [ "$status" = "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $status)"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}✗ FAIL${NC} (HTTP $status, expected $expected_status)"
        ((TESTS_FAILED++))
    fi
}

# API Base URL
API_BASE="http://localhost:8080/api"

echo "📡 Testing API Endpoints"
echo "------------------------"

# Test job endpoints
test_api "$API_BASE/jobs" "GET" "" "200" "Fetch jobs (public)"

# Test authentication endpoints
test_api "$API_BASE/auth/login" "POST" '{"email":"demo@example.com","password":"demo123"}' "200" "Login with valid credentials"
test_api "$API_BASE/auth/login" "POST" '{"email":"demo@example.com","password":"wrongpassword"}' "401" "Login with invalid credentials"
test_api "$API_BASE/auth/register" "POST" '{"fullName":"Test User 3","email":"testuser3@example.com","password":"test123","role":"CANDIDATE"}' "201" "Register new user"

# Get a fresh token for protected endpoints
echo -n "Getting fresh auth token... "
TOKEN_RESPONSE=$(curl -s -X POST "$API_BASE/auth/login" -H "Content-Type: application/json" -d '{"email":"demo@example.com","password":"demo123"}')
TOKEN=$(echo $TOKEN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✓ Token obtained${NC}"
    
    # Test protected endpoints with token
    status=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$API_BASE/jobs" -H "Authorization: Bearer $TOKEN")
    if [ "$status" = "200" ]; then
        echo -e "Testing: Protected jobs endpoint with token... ${GREEN}✓ PASS${NC} (HTTP $status)"
        ((TESTS_PASSED++))
    else
        echo -e "Testing: Protected jobs endpoint with token... ${RED}✗ FAIL${NC} (HTTP $status)"
        ((TESTS_FAILED++))
    fi
else
    echo -e "${RED}✗ Failed to obtain token${NC}"
    ((TESTS_FAILED++))
fi

echo ""
echo "🌐 Testing Frontend Accessibility"
echo "--------------------------------"

# Test if frontend is accessible
test_api "http://localhost:3000" "GET" "" "200" "Frontend homepage"
test_api "http://localhost:3000/login" "GET" "" "200" "Login page"
test_api "http://localhost:3000/register" "GET" "" "200" "Register page"
test_api "http://localhost:3000/jobs" "GET" "" "200" "Jobs page"

echo ""
echo "📊 Test Summary"
echo "==============="
echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"
TOTAL=$((TESTS_PASSED + TESTS_FAILED))
echo "Total Tests: $TOTAL"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "\n🎉 ${GREEN}All tests passed!${NC}"
    echo "✅ Login functionality is working correctly"
    echo "✅ API endpoints are accessible"
    echo "✅ Frontend is properly running"
    echo ""
    echo "🔑 Test Credentials:"
    echo "   Email: demo@example.com"
    echo "   Password: demo123"
    echo ""
    echo "   Email: testuser2@example.com"
    echo "   Password: test123"
    exit 0
else
    echo -e "\n❌ ${RED}Some tests failed!${NC}"
    echo "Please check the following:"
    echo "• Backend server running on port 8080"
    echo "• Frontend server running on port 3000"
    echo "• Database connectivity"
    exit 1
fi
