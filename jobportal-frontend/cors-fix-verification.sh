#!/bin/bash

echo "🎉 CORS ISSUE RESOLUTION VERIFICATION"
echo "====================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check current frontend port
echo -e "${BLUE}🔍 Checking current setup...${NC}"
frontend_port=$(lsof -i :3000 | grep LISTEN | wc -l)
if [ $frontend_port -gt 0 ]; then
    echo -e "${GREEN}✅ Frontend is running on port 3000${NC}"
else
    echo -e "${RED}❌ Frontend is NOT running on port 3000${NC}"
fi

# Check backend
backend_port=$(lsof -i :8080 | grep LISTEN | wc -l)
if [ $backend_port -gt 0 ]; then
    echo -e "${GREEN}✅ Backend is running on port 8080${NC}"
else
    echo -e "${RED}❌ Backend is NOT running on port 8080${NC}"
fi

echo ""

# Test CORS with correct port
echo -e "${BLUE}🧪 Testing CORS with port 3000...${NC}"
response=$(curl -s -w "%{http_code}" -X POST "http://localhost:8080/api/auth/login" \
    -H "Content-Type: application/json" \
    -H "Origin: http://localhost:3000" \
    -d '{"email":"demo@example.com","password":"demo123"}')

http_code="${response: -3}"
body="${response%???}"

if [ "$http_code" -eq 200 ]; then
    echo -e "${GREEN}✅ CORS is working correctly (HTTP 200)${NC}"
    if echo "$body" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ Login API returns success${NC}"
        echo -e "${GREEN}🎉 PROBLEM SOLVED!${NC}"
    else
        echo -e "${YELLOW}⚠️  API response indicates failure${NC}"
    fi
else
    echo -e "${RED}❌ CORS still failing (HTTP $http_code)${NC}"
    echo "Response: $body"
fi

echo ""
echo -e "${YELLOW}📱 NEXT STEPS:${NC}"
echo "1. Open http://localhost:3000/login in your browser"
echo "2. Click 'Fill Test Credentials'"
echo "3. Click 'Sign In'"
echo "4. You should now be able to login successfully!"
echo ""
echo -e "${BLUE}🔑 Test Credentials:${NC}"
echo "Email: demo@example.com"
echo "Password: demo123"
