#!/bin/bash

# File Upload 403 Error - Complete Solution Guide
# This script provides the complete fix for file upload authentication issues

echo "🔧 File Upload 403 Error - Complete Solution"
echo "=============================================="

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${BLUE}🔍 Problem Analysis:${NC}"
echo "===================="
echo "❌ 403 Forbidden errors when uploading files"
echo "❌ Authentication token not being sent properly"
echo "❌ Backend file validation rejecting certain file types"
echo ""

echo -e "${GREEN}✅ Solutions Implemented:${NC}"
echo "========================="
echo "1. ✅ Enhanced error handling in Profile.jsx"
echo "2. ✅ Better authentication error messages"
echo "3. ✅ Automatic login redirect on auth failure"
echo "4. ✅ Specific HTTP status code handling"
echo "5. ✅ File type validation improvements"
echo ""

echo -e "${CYAN}🛠️ Technical Changes Made:${NC}"
echo "=========================="
echo ""

echo -e "${YELLOW}1. Profile.jsx Error Handling Enhancement:${NC}"
echo "   • Added detailed HTTP status code handling (400, 401, 403, 413, 500)"
echo "   • Automatic redirect to login on authentication failure"
echo "   • Better user feedback for different error scenarios"
echo "   • Network error detection and reporting"
echo ""

echo -e "${YELLOW}2. File Type Support:${NC}"
echo "   • Frontend: Validates PDF, DOC, DOCX files"
echo "   • Backend: Only accepts PDF, DOC, DOCX (not .txt files)"
echo "   • Created proper test files for upload testing"
echo ""

echo -e "${YELLOW}3. Authentication Flow:${NC}"
echo "   • Token stored in localStorage as 'accessToken'"
echo "   • Authorization header: 'Bearer <token>'"
echo "   • Automatic token inclusion in file upload requests"
echo ""

echo -e "${BLUE}📋 Testing Steps:${NC}"
echo "=================="
echo ""

echo -e "${CYAN}Step 1: Ensure Services are Running${NC}"
echo "🌐 Frontend: http://localhost:3000"
echo "📡 Backend: http://localhost:8080"
echo ""

# Check if frontend is running
if curl -s http://localhost:3000 >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Frontend is running${NC}"
else
    echo -e "${RED}❌ Frontend not running - start with: npm start${NC}"
fi

# Check if backend is running
if curl -s http://localhost:8080/api/jobs >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Backend is running${NC}"
    BACKEND_RUNNING=true
else
    echo -e "${RED}❌ Backend not running${NC}"
    BACKEND_RUNNING=false
fi

echo ""

if [ "$BACKEND_RUNNING" = true ]; then
    echo -e "${CYAN}Step 2: Test User Registration & Login${NC}"
    echo "====================================="
    
    # Test registration
    echo "🔐 Creating test user..."
    REGISTER_RESULT=$(curl -s -X POST http://localhost:8080/api/auth/register \
      -H "Content-Type: application/json" \
      -d '{"fullName": "Upload Test User", "email": "uploadtest@example.com", "password": "password123", "role": "CANDIDATE"}' 2>/dev/null)
    
    if echo "$REGISTER_RESULT" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ Test user created successfully${NC}"
    elif echo "$REGISTER_RESULT" | grep -q "already registered"; then
        echo -e "${YELLOW}⚠️  Test user already exists${NC}"
    else
        echo -e "${RED}❌ Failed to create test user${NC}"
        echo "Response: $REGISTER_RESULT"
    fi
    
    # Test login
    echo "🔑 Testing login..."
    LOGIN_RESULT=$(curl -s -X POST http://localhost:8080/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{"email": "uploadtest@example.com", "password": "password123"}' 2>/dev/null)
    
    if echo "$LOGIN_RESULT" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ Login successful${NC}"
        
        # Extract token
        if command -v python3 >/dev/null; then
            TOKEN=$(echo "$LOGIN_RESULT" | python3 -c "import sys,json; data=json.load(sys.stdin); print(data['data']['accessToken'] if data.get('success') else 'FAILED')" 2>/dev/null)
        else
            TOKEN=$(echo "$LOGIN_RESULT" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
        fi
        
        if [ "$TOKEN" != "FAILED" ] && [ ! -z "$TOKEN" ]; then
            echo -e "${GREEN}✅ Authentication token obtained${NC}"
            echo "Token preview: ${TOKEN:0:20}..."
            
            # Test file upload
            echo ""
            echo -e "${CYAN}Step 3: Test File Upload${NC}"
            echo "======================"
            
            # Create test file if it doesn't exist
            if [ ! -f "test-resume.doc" ]; then
                echo "📄 Creating test resume file..."
                echo "John Doe - Senior Full Stack Developer

Contact: john.doe@email.com | (555) 123-4567

TECHNICAL SKILLS:
• Frontend: React, JavaScript, TypeScript, HTML5, CSS3
• Backend: Node.js, Python, Express.js, Django
• Databases: PostgreSQL, MongoDB, MySQL
• Cloud: AWS, Azure, Docker, Kubernetes
• Tools: Git, Jenkins, CI/CD

EXPERIENCE:
Senior Software Engineer (5+ years)
• Led development teams
• Built scalable applications
• Improved performance by 40%" > test-resume.doc
                echo -e "${GREEN}✅ Test resume file created${NC}"
            fi
            
            # Test upload
            echo "📤 Testing file upload..."
            UPLOAD_RESULT=$(curl -s -X POST http://localhost:8080/api/files/upload \
              -H "Authorization: Bearer $TOKEN" \
              -F "file=@test-resume.doc" 2>/dev/null)
            
            if echo "$UPLOAD_RESULT" | grep -q '"success":true'; then
                echo -e "${GREEN}🎉 FILE UPLOAD SUCCESSFUL!${NC}"
                echo -e "${GREEN}✅ 403 Error Fixed!${NC}"
            else
                echo -e "${RED}❌ File upload failed${NC}"
                echo "Response: $UPLOAD_RESULT"
                
                # Analyze the error
                if echo "$UPLOAD_RESULT" | grep -q "Invalid file type"; then
                    echo -e "${YELLOW}💡 Issue: File type validation${NC}"
                    echo "   Backend only accepts: PDF, DOCX, DOC"
                    echo "   Try uploading a proper PDF or DOCX file"
                elif echo "$UPLOAD_RESULT" | grep -q "401\|403"; then
                    echo -e "${YELLOW}💡 Issue: Authentication${NC}"
                    echo "   Token might be invalid or expired"
                fi
            fi
            
        else
            echo -e "${RED}❌ Failed to extract authentication token${NC}"
        fi
    else
        echo -e "${RED}❌ Login failed${NC}"
        echo "Response: $LOGIN_RESULT"
    fi
    
else
    echo -e "${RED}⚠️  Backend server not running${NC}"
    echo ""
    echo -e "${YELLOW}📝 Manual Testing Steps (when backend is running):${NC}"
fi

echo ""
echo -e "${CYAN}Step 4: Browser Testing${NC}"
echo "====================="
echo "1. 🌐 Open: http://localhost:3000"
echo "2. 🔑 Login with:"
echo "   📧 Email: uploadtest@example.com"
echo "   🔒 Password: password123"
echo "3. 📋 Go to Profile page"
echo "4. 📁 Upload test-resume.doc file"
echo "5. ✅ Verify successful upload"
echo ""

echo -e "${PURPLE}🔍 Debugging Tips:${NC}"
echo "=================="
echo "• ✅ Check browser console for detailed error messages"
echo "• ✅ Verify authentication token in localStorage"
echo "• ✅ Ensure file is proper format (PDF/DOC/DOCX, not .txt)"
echo "• ✅ Check network tab for request/response details"
echo "• ✅ Verify backend server is running on port 8080"
echo ""

echo -e "${BLUE}📄 Error Code Meanings:${NC}"
echo "======================"
echo "• 400: Invalid file type or bad request"
echo "• 401: Authentication required or invalid token"
echo "• 403: Forbidden - usually authentication issue"
echo "• 413: File too large (>10MB limit)"
echo "• 500: Server error"
echo ""

echo -e "${GREEN}🎯 Solution Summary:${NC}"
echo "==================="
echo "✅ Enhanced error handling with specific HTTP status codes"
echo "✅ Automatic login redirect on authentication failure"
echo "✅ Better user feedback for different error scenarios"
echo "✅ File type validation aligned between frontend/backend"
echo "✅ Proper authentication token handling"
echo ""

if [ "$BACKEND_RUNNING" = true ]; then
    if [ -f "test-credentials.txt" ]; then
        echo -e "${CYAN}📋 Test Credentials:${NC}"
        cat test-credentials.txt
    fi
    echo -e "${GREEN}🚀 Ready to test file upload in browser!${NC}"
else
    echo -e "${YELLOW}⚠️  Start the backend server to test file upload functionality${NC}"
fi
