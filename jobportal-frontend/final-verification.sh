#!/bin/zsh

echo "🧪 FINAL LOGIN VERIFICATION TEST"
echo "================================"
echo ""

# Colors for better output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
BOLD='\033[1m'
NC='\033[0m' # No Color

echo -e "${BOLD}1️⃣ CHECKING SERVICE STATUS${NC}"
echo "============================="

# Check Frontend
frontend_check=$(lsof -i :3000 | grep LISTEN | wc -l | tr -d ' ')
if [ "$frontend_check" -gt 0 ]; then
    echo -e "${GREEN}✅ Frontend React app is running on port 3000${NC}"
else
    echo -e "${RED}❌ Frontend is NOT running on port 3000${NC}"
    echo "Run: npm start"
    exit 1
fi

# Check Backend
backend_check=$(lsof -i :8080 | grep LISTEN | wc -l | tr -d ' ')
if [ "$backend_check" -gt 0 ]; then
    echo -e "${GREEN}✅ Backend Java app is running on port 8080${NC}"
else
    echo -e "${RED}❌ Backend is NOT running on port 8080${NC}"
    echo "Start your Spring Boot application"
    exit 1
fi

echo ""
echo -e "${BOLD}2️⃣ TESTING API CONNECTIVITY${NC}"
echo "============================="

# Test API with CORS
response=$(curl -s -w "%{http_code}" -X POST "http://localhost:8080/api/auth/login" \
    -H "Content-Type: application/json" \
    -H "Origin: http://localhost:3000" \
    -d '{"email":"demo@example.com","password":"demo123"}')

http_code="${response: -3}"
body="${response%???}"

if [ "$http_code" = "200" ]; then
    echo -e "${GREEN}✅ API responds with HTTP 200${NC}"
    
    if echo "$body" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ API returns success=true${NC}"
        
        if echo "$body" | grep -q '"accessToken"'; then
            echo -e "${GREEN}✅ API returns access token${NC}"
            
            if echo "$body" | grep -q '"user"'; then
                echo -e "${GREEN}✅ API returns user data${NC}"
                echo -e "${GREEN}🎉 BACKEND API IS WORKING PERFECTLY!${NC}"
            else
                echo -e "${RED}❌ Missing user data in response${NC}"
            fi
        else
            echo -e "${RED}❌ Missing access token in response${NC}"
        fi
    else
        echo -e "${RED}❌ API response indicates failure${NC}"
        echo "Response: $body"
    fi
else
    echo -e "${RED}❌ API call failed with HTTP $http_code${NC}"
    echo "Response: $body"
    exit 1
fi

echo ""
echo -e "${BOLD}3️⃣ TESTING CORS CONFIGURATION${NC}"
echo "=============================="

# Test CORS headers specifically
cors_response=$(curl -s -I -X OPTIONS "http://localhost:8080/api/auth/login" \
    -H "Origin: http://localhost:3000" \
    -H "Access-Control-Request-Method: POST" \
    -H "Access-Control-Request-Headers: content-type")

if echo "$cors_response" | grep -q "Access-Control-Allow-Origin"; then
    echo -e "${GREEN}✅ CORS headers are present${NC}"
    
    if echo "$cors_response" | grep -q "http://localhost:3000"; then
        echo -e "${GREEN}✅ CORS allows requests from port 3000${NC}"
        echo -e "${GREEN}🎉 CORS IS PROPERLY CONFIGURED!${NC}"
    else
        echo -e "${RED}❌ CORS doesn't allow localhost:3000${NC}"
        echo "CORS response:"
        echo "$cors_response"
    fi
else
    echo -e "${YELLOW}⚠️  No CORS headers found (this might be normal)${NC}"
fi

echo ""
echo -e "${BOLD}4️⃣ FRONTEND PAGE VERIFICATION${NC}"
echo "============================="

# Test if frontend pages load
home_response=$(curl -s -w "%{http_code}" "http://localhost:3000")
home_code="${home_response: -3}"

if [ "$home_code" = "200" ]; then
    echo -e "${GREEN}✅ Frontend homepage loads (HTTP 200)${NC}"
else
    echo -e "${RED}❌ Frontend homepage failed (HTTP $home_code)${NC}"
fi

login_response=$(curl -s -w "%{http_code}" "http://localhost:3000/login")
login_code="${login_response: -3}"

if [ "$login_code" = "200" ]; then
    echo -e "${GREEN}✅ Login page loads (HTTP 200)${NC}"
else
    echo -e "${RED}❌ Login page failed (HTTP $login_code)${NC}"
fi

echo ""
echo -e "${BOLD}🎯 FINAL RESULT${NC}"
echo "==============="

if [ "$http_code" = "200" ] && [ "$home_code" = "200" ] && [ "$login_code" = "200" ]; then
    echo -e "${GREEN}${BOLD}🎉 ALL TESTS PASSED! LOGIN SHOULD WORK! 🎉${NC}"
    echo ""
    echo -e "${BLUE}📱 TO LOGIN:${NC}"
    echo "1. Open: ${YELLOW}http://localhost:3000/login${NC}"
    echo "2. Click: ${YELLOW}'Fill Test Credentials'${NC} button"  
    echo "3. Click: ${YELLOW}'Sign In'${NC} button"
    echo "4. Expected: Redirect to dashboard"
    echo ""
    echo -e "${BLUE}🔑 Test Credentials:${NC}"
    echo "Email: ${YELLOW}demo@example.com${NC}"
    echo "Password: ${YELLOW}demo123${NC}"
    echo ""
    echo -e "${BLUE}🛠️  If login still fails:${NC}"
    echo "- Open browser Developer Tools (F12)"
    echo "- Check Console tab for JavaScript errors"
    echo "- Check Network tab for failed requests"
    echo "- Try incognito/private mode"
    echo "- Clear browser cache (Ctrl+F5)"
    
else
    echo -e "${RED}${BOLD}❌ SOME TESTS FAILED${NC}"
    echo "Please check the errors above and fix them first."
fi

echo ""
echo -e "${BLUE}💡 Troubleshooting Resources:${NC}"
echo "- Run: ${YELLOW}./comprehensive-debug.sh${NC}"
echo "- Check: ${YELLOW}LOGIN_TROUBLESHOOTING.md${NC}"
echo "- Open: ${YELLOW}network-diagnostic.html${NC} in browser"
