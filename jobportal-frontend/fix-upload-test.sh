#!/bin/bash

# File Upload Fix and Test Script
# Addresses the 403 authentication errors and tests file upload functionality

echo "🔧 File Upload Authentication Fix & Test"
echo "========================================="

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
PURPLE='\033[0;35m'
NC='\033[0m'

# Check if backend is running
echo -e "${BLUE}📡 Checking backend server...${NC}"
if ! curl -s http://localhost:8080/api/jobs >/dev/null; then
    echo -e "${RED}❌ Backend server not running on port 8080${NC}"
    echo "Please start the backend server first"
    exit 1
fi
echo -e "${GREEN}✅ Backend server is running${NC}"

# Check if frontend is running
echo -e "${BLUE}🌐 Checking frontend server...${NC}"
if ! curl -s http://localhost:3000 >/dev/null; then
    echo -e "${RED}❌ Frontend server not running on port 3000${NC}"
    echo "Please start the frontend server first"
    exit 1
fi
echo -e "${GREEN}✅ Frontend server is running${NC}"

# Test user registration (in case user doesn't exist)
echo -e "\n${BLUE}👤 Setting up test user for file upload...${NC}"
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName": "Upload Test User", "email": "uploadtest@example.com", "password": "password123", "role": "CANDIDATE"}')

echo "Registration result: $REGISTER_RESPONSE"

# Login to get authentication token
echo -e "\n${BLUE}🔐 Logging in to get authentication token...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "uploadtest@example.com", "password": "password123"}')

echo "Login response: $LOGIN_RESPONSE"

# Extract token using python/node if available, or manual parsing
if command -v python3 >/dev/null; then
    TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    if data.get('success') and data.get('data'):
        print(data['data']['accessToken'])
    else:
        print('FAILED')
except:
    print('FAILED')
")
elif command -v node >/dev/null; then
    TOKEN=$(echo "$LOGIN_RESPONSE" | node -e "
const data = JSON.parse(require('fs').readFileSync(0, 'utf8'));
if (data.success && data.data) {
    console.log(data.data.accessToken);
} else {
    console.log('FAILED');
}
")
else
    # Fallback: try to extract token manually
    TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
fi

if [ "$TOKEN" = "FAILED" ] || [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
    echo -e "${RED}❌ Failed to get authentication token${NC}"
    echo "Login response: $LOGIN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✅ Authentication token obtained${NC}"
echo "Token (first 20 chars): ${TOKEN:0:20}..."

# Test file upload with authentication
echo -e "\n${BLUE}📄 Testing file upload with authentication...${NC}"

if [ ! -f "test-resume.doc" ]; then
    echo -e "${YELLOW}⚠️  Creating test resume file...${NC}"
    echo "John Doe - Senior Developer
Skills: React, Node.js, Python, JavaScript, TypeScript
Experience: 5+ years in full-stack development" > test-resume.doc
fi

UPLOAD_RESPONSE=$(curl -s -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test-resume.doc")

echo "Upload response: $UPLOAD_RESPONSE"

# Check upload success
if echo "$UPLOAD_RESPONSE" | grep -q '"success":true'; then
    echo -e "\n${GREEN}🎉 FILE UPLOAD SUCCESSFUL!${NC}"
    echo -e "${GREEN}✅ Authentication working correctly${NC}"
    echo -e "${GREEN}✅ File upload endpoint accessible${NC}"
else
    echo -e "\n${RED}❌ FILE UPLOAD FAILED${NC}"
    echo "Response: $UPLOAD_RESPONSE"
    
    # Additional debugging
    echo -e "\n${BLUE}🔍 Debugging information:${NC}"
    echo "Token length: ${#TOKEN}"
    echo "File exists: $([ -f test-resume.doc ] && echo 'Yes' || echo 'No')"
    echo "File size: $([ -f test-resume.doc ] && wc -c < test-resume.doc || echo 'N/A') bytes"
    
    # Test token validity
    echo -e "\n${BLUE}🔍 Testing token validity...${NC}"
    TOKEN_TEST=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/jobs | head -100)
    echo "Token test response: $TOKEN_TEST"
fi

echo -e "\n${BLUE}💡 Frontend Integration Steps:${NC}"
echo "================================"
echo "1. User must be logged in (have valid token in localStorage)"
echo "2. Token must be included in Authorization header"
echo "3. File must be valid format (PDF, DOC, DOCX)"
echo "4. File size must be under 10MB"
echo ""

echo -e "${PURPLE}🚀 To test in browser:${NC}"
echo "======================"
echo "1. Open: http://localhost:3000"
echo "2. Login with: uploadtest@example.com / password123"
echo "3. Go to Profile page"
echo "4. Upload test-resume.doc file"
echo "5. Check browser console for detailed logs"
echo ""

echo -e "${BLUE}🔧 Fixed Issues:${NC}"
echo "================"
echo "✅ Improved error handling in Profile.jsx"
echo "✅ Better authentication error messages"
echo "✅ Automatic login redirect on auth failure"
echo "✅ Specific error codes handling (400, 401, 403, 413, 500)"
echo "✅ Network error detection"
echo ""

# Save credentials for easy testing
echo -e "${YELLOW}📋 Test Credentials (saved to test-credentials.txt):${NC}"
echo "Email: uploadtest@example.com
Password: password123
Token: $TOKEN" > test-credentials.txt

echo "Email: uploadtest@example.com"
echo "Password: password123"
echo ""

if echo "$UPLOAD_RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}🎯 FILE UPLOAD ISSUE RESOLVED! ✅${NC}"
else
    echo -e "${RED}🔍 Additional debugging may be needed${NC}"
fi
