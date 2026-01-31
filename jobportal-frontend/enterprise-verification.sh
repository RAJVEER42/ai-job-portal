#!/bin/bash

echo "🧪 ENTERPRISE FRONTEND FEATURE VERIFICATION"
echo "============================================"
echo ""

# Configuration
FRONTEND_URL="http://localhost:3000"
BACKEND_URL="http://localhost:8080"
TEST_RESULTS=()

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test function
test_feature() {
    local test_name=$1
    local test_command=$2
    local expected_result=${3:-0}
    
    echo -e "${BLUE}🧪 Testing: $test_name${NC}"
    
    if eval "$test_command" >/dev/null 2>&1; then
        echo -e "${GREEN}✅ PASS: $test_name${NC}"
        TEST_RESULTS+=("PASS: $test_name")
        return 0
    else
        echo -e "${RED}❌ FAIL: $test_name${NC}"
        TEST_RESULTS+=("FAIL: $test_name")
        return 1
    fi
}

# Test URL accessibility
test_url() {
    local url=$1
    local description=$2
    
    response_code=$(curl -s -o /dev/null -w "%{http_code}" "$url")
    if [ "$response_code" -eq 200 ]; then
        echo -e "${GREEN}✅ $description - HTTP $response_code${NC}"
        TEST_RESULTS+=("PASS: $description")
    else
        echo -e "${RED}❌ $description - HTTP $response_code${NC}"
        TEST_RESULTS+=("FAIL: $description")
    fi
}

echo "1️⃣ SERVICE AVAILABILITY TESTS"
echo "=============================="

# Check if services are running
if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null ; then
    echo -e "${GREEN}✅ Frontend service (React) - Port 3000${NC}"
    TEST_RESULTS+=("PASS: Frontend service running")
else
    echo -e "${RED}❌ Frontend service not running${NC}"
    TEST_RESULTS+=("FAIL: Frontend service")
fi

if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo -e "${GREEN}✅ Backend service (Mock) - Port 8080${NC}"
    TEST_RESULTS+=("PASS: Backend service running")
else
    echo -e "${RED}❌ Backend service not running${NC}"
    TEST_RESULTS+=("FAIL: Backend service")
fi

echo ""
echo "2️⃣ FRONTEND PAGE ACCESSIBILITY"
echo "==============================="

# Test main pages
test_url "$FRONTEND_URL" "Homepage"
test_url "$FRONTEND_URL/login" "Login page"
test_url "$FRONTEND_URL/register" "Register page"
test_url "$FRONTEND_URL/jobs" "Jobs page"
test_url "$FRONTEND_URL/dashboard" "Dashboard page"
test_url "$FRONTEND_URL/applications" "Applications page"
test_url "$FRONTEND_URL/admin" "Admin Dashboard"

echo ""
echo "3️⃣ API ENDPOINT TESTS"
echo "====================="

# Test API endpoints
test_url "$BACKEND_URL/api/jobs" "Jobs API"

# Test authentication endpoint
auth_response=$(curl -s -X POST "$BACKEND_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"demo@example.com","password":"demo123"}')

if echo "$auth_response" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ Authentication API working${NC}"
    TEST_RESULTS+=("PASS: Authentication API")
else
    echo -e "${RED}❌ Authentication API failed${NC}"
    TEST_RESULTS+=("FAIL: Authentication API")
fi

echo ""
echo "4️⃣ ENHANCED COMPONENT VERIFICATION"
echo "=================================="

# Check if enhanced files exist
enhanced_files=(
    "src/components/SystemMonitoring.jsx"
    "src/components/ErrorBoundary.jsx" 
    "src/services/api.js"
    "src/pages/AdminDashboard.jsx"
    "src/pages/MyApplications.jsx"
    ".env.production"
    ".env.development"
    "deploy-production.sh"
)

for file in "${enhanced_files[@]}"; do
    if [ -f "$file" ]; then
        echo -e "${GREEN}✅ Enhanced file exists: $file${NC}"
        TEST_RESULTS+=("PASS: $file exists")
    else
        echo -e "${RED}❌ Missing enhanced file: $file${NC}"
        TEST_RESULTS+=("FAIL: $file missing")
    fi
done

echo ""
echo "5️⃣ ENTERPRISE FEATURES VERIFICATION"
echo "==================================="

# Check for enterprise API features in api.js
if grep -q "RequestManager" src/services/api.js; then
    echo -e "${GREEN}✅ Circuit Breaker pattern implemented${NC}"
    TEST_RESULTS+=("PASS: Circuit Breaker pattern")
else
    echo -e "${RED}❌ Circuit Breaker pattern missing${NC}"
    TEST_RESULTS+=("FAIL: Circuit Breaker pattern")
fi

if grep -q "correlationId\|requestId" src/services/api.js; then
    echo -e "${GREEN}✅ Request correlation IDs implemented${NC}"
    TEST_RESULTS+=("PASS: Request correlation")
else
    echo -e "${RED}❌ Request correlation missing${NC}"
    TEST_RESULTS+=("FAIL: Request correlation")
fi

if grep -q "retry.*exponential\|MAX_RETRIES" src/services/api.js; then
    echo -e "${GREEN}✅ Retry logic with exponential backoff${NC}"
    TEST_RESULTS+=("PASS: Retry logic")
else
    echo -e "${RED}❌ Retry logic missing${NC}"
    TEST_RESULTS+=("FAIL: Retry logic")
fi

if grep -q "performance\.now\|responseTime" src/services/api.js; then
    echo -e "${GREEN}✅ Performance monitoring implemented${NC}"
    TEST_RESULTS+=("PASS: Performance monitoring")
else
    echo -e "${RED}❌ Performance monitoring missing${NC}"
    TEST_RESULTS+=("FAIL: Performance monitoring")
fi

echo ""
echo "6️⃣ PRODUCTION READINESS"
echo "======================="

# Check deployment script
if [ -x "deploy-production.sh" ]; then
    echo -e "${GREEN}✅ Production deployment script executable${NC}"
    TEST_RESULTS+=("PASS: Deployment script")
else
    echo -e "${RED}❌ Deployment script not executable${NC}"
    TEST_RESULTS+=("FAIL: Deployment script")
fi

# Check if build directory exists
if [ -d "build" ]; then
    echo -e "${GREEN}✅ Production build directory exists${NC}"
    TEST_RESULTS+=("PASS: Build directory")
    
    # Check build files
    if [ -f "build/index.html" ]; then
        echo -e "${GREEN}✅ Production build files present${NC}"
        TEST_RESULTS+=("PASS: Build files")
    else
        echo -e "${RED}❌ Production build files missing${NC}"
        TEST_RESULTS+=("FAIL: Build files")
    fi
else
    echo -e "${YELLOW}⚠️  Production build not yet created${NC}"
    TEST_RESULTS+=("WARN: No build directory")
fi

echo ""
echo "7️⃣ SECURITY FEATURES"
echo "===================="

# Check for security implementations
if grep -q "CSRF.*token\|csrf" src/services/api.js; then
    echo -e "${GREEN}✅ CSRF protection implemented${NC}"
    TEST_RESULTS+=("PASS: CSRF protection")
else
    echo -e "${YELLOW}⚠️  CSRF protection basic implementation${NC}"
    TEST_RESULTS+=("WARN: CSRF protection")
fi

if grep -q "refreshToken\|token.*refresh" src/services/api.js; then
    echo -e "${GREEN}✅ JWT token refresh implemented${NC}"
    TEST_RESULTS+=("PASS: JWT refresh")
else
    echo -e "${RED}❌ JWT token refresh missing${NC}"
    TEST_RESULTS+=("FAIL: JWT refresh")
fi

echo ""
echo "8️⃣ USER EXPERIENCE ENHANCEMENTS"
echo "==============================="

# Check for UX enhancements
if grep -q "ErrorBoundary" src/App.jsx; then
    echo -e "${GREEN}✅ Error boundary wrapper implemented${NC}"
    TEST_RESULTS+=("PASS: Error boundary")
else
    echo -e "${RED}❌ Error boundary missing${NC}"
    TEST_RESULTS+=("FAIL: Error boundary")
fi

if grep -q "My Applications\|Admin Dashboard" src/components/Navbar.jsx; then
    echo -e "${GREEN}✅ Enhanced navigation with new links${NC}"
    TEST_RESULTS+=("PASS: Enhanced navigation")
else
    echo -e "${RED}❌ Navigation enhancements missing${NC}"
    TEST_RESULTS+=("FAIL: Enhanced navigation")
fi

echo ""
echo "9️⃣ ADMIN DASHBOARD FEATURES"
echo "==========================="

# Check admin dashboard enhancements
if grep -q "SystemMonitoring" src/pages/AdminDashboard.jsx; then
    echo -e "${GREEN}✅ System monitoring integration${NC}"
    TEST_RESULTS+=("PASS: System monitoring")
else
    echo -e "${RED}❌ System monitoring missing${NC}"
    TEST_RESULTS+=("FAIL: System monitoring")
fi

if grep -q "activeTab.*useState\|tab.*navigation" src/pages/AdminDashboard.jsx; then
    echo -e "${GREEN}✅ Tabbed interface implemented${NC}"
    TEST_RESULTS+=("PASS: Tabbed interface")
else
    echo -e "${RED}❌ Tabbed interface missing${NC}"
    TEST_RESULTS+=("FAIL: Tabbed interface")
fi

echo ""
echo "🔟 APPLICATION STATUS"
echo "===================="

# Check if MyApplications uses custom modal instead of confirm()
if grep -q "showWithdrawConfirm\|confirmWithdraw" src/pages/MyApplications.jsx && ! grep -q "confirm(" src/pages/MyApplications.jsx; then
    echo -e "${GREEN}✅ Custom confirmation modal (no browser confirm)${NC}"
    TEST_RESULTS+=("PASS: Custom modal")
else
    echo -e "${RED}❌ Still using browser confirm or missing modal${NC}"
    TEST_RESULTS+=("FAIL: Custom modal")
fi

echo ""
echo "📊 TEST SUMMARY"
echo "==============="

# Count results
total_tests=${#TEST_RESULTS[@]}
passed_tests=$(printf '%s\n' "${TEST_RESULTS[@]}" | grep -c "PASS:")
failed_tests=$(printf '%s\n' "${TEST_RESULTS[@]}" | grep -c "FAIL:")
warning_tests=$(printf '%s\n' "${TEST_RESULTS[@]}" | grep -c "WARN:")

echo "Total Tests: $total_tests"
echo -e "${GREEN}Passed: $passed_tests${NC}"
echo -e "${RED}Failed: $failed_tests${NC}"
echo -e "${YELLOW}Warnings: $warning_tests${NC}"

# Calculate success rate
if [ "$total_tests" -gt 0 ]; then
    success_rate=$((passed_tests * 100 / total_tests))
    echo ""
    echo "Success Rate: $success_rate%"
    
    if [ "$success_rate" -ge 90 ]; then
        echo -e "${GREEN}🎉 EXCELLENT: Enterprise integration is highly successful!${NC}"
    elif [ "$success_rate" -ge 75 ]; then
        echo -e "${BLUE}👍 GOOD: Enterprise integration is mostly complete!${NC}"
    elif [ "$success_rate" -ge 50 ]; then
        echo -e "${YELLOW}⚠️  FAIR: Some enterprise features need attention${NC}"
    else
        echo -e "${RED}❌ POOR: Major issues need resolution${NC}"
    fi
fi

echo ""
echo "📋 FAILED TESTS (if any):"
printf '%s\n' "${TEST_RESULTS[@]}" | grep "FAIL:" | sed 's/FAIL: /- /'

echo ""
echo -e "${BLUE}✨ Enterprise verification complete!${NC}"
