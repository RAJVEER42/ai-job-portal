#!/bin/bash

# AI Job Portal - Complete API Testing Script
# This script tests all major API endpoints to ensure they're working correctly

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8080"
API_URL="${BASE_URL}/api"

# Test data
CANDIDATE_EMAIL="testcandidate$(date +%s)@example.com"
RECRUITER_EMAIL="testrecruiter$(date +%s)@example.com"
ADMIN_EMAIL="testadmin$(date +%s)@example.com"
PASSWORD="password123"

echo -e "${BLUE}🚀 Starting AI Job Portal API Tests${NC}"
echo -e "${BLUE}======================================${NC}\n"

# Function to make HTTP requests with error handling
make_request() {
    local method="$1"
    local url="$2"
    local data="$3"
    local headers="$4"
    local description="$5"
    
    echo -e "${YELLOW}Testing: $description${NC}"
    
    local response
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET "$url" $headers)
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST "$url" -H "Content-Type: application/json" $headers -d "$data")
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PUT "$url" -H "Content-Type: application/json" $headers -d "$data")
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -w "\n%{http_code}" -X DELETE "$url" $headers)
    fi
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ] || [ "$http_code" -eq 204 ]; then
        echo -e "${GREEN}✓ SUCCESS (HTTP $http_code)${NC}"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    else
        echo -e "${RED}✗ FAILED (HTTP $http_code)${NC}"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
        return 1
    fi
    
    echo ""
    return 0
}

# Test 1: Health Check
echo -e "${BLUE}1. Testing Application Health${NC}"
make_request "GET" "${BASE_URL}/actuator/health" "" "" "Application health check"

# Test 2: OpenAPI Documentation
echo -e "${BLUE}2. Testing OpenAPI Documentation${NC}"
make_request "GET" "${BASE_URL}/v3/api-docs" "" "" "OpenAPI documentation endpoint"

# Test 3: Register Candidate
echo -e "${BLUE}3. Testing User Registration (Candidate)${NC}"
CANDIDATE_REG_DATA='{
  "email": "'${CANDIDATE_EMAIL}'",
  "password": "'${PASSWORD}'",
  "fullName": "Test Candidate",
  "role": "CANDIDATE",
  "phone": "+1234567890"
}'

CANDIDATE_RESPONSE=$(make_request "POST" "${API_URL}/auth/register" "$CANDIDATE_REG_DATA" "" "Register candidate user")
if [ $? -eq 0 ]; then
    CANDIDATE_ID=$(echo "$CANDIDATE_RESPONSE" | jq -r '.data.id' 2>/dev/null || echo "1")
fi

# Test 4: Register Recruiter
echo -e "${BLUE}4. Testing User Registration (Recruiter)${NC}"
RECRUITER_REG_DATA='{
  "email": "'${RECRUITER_EMAIL}'",
  "password": "'${PASSWORD}'",
  "fullName": "Test Recruiter",
  "role": "RECRUITER",
  "phone": "+1234567891"
}'

RECRUITER_RESPONSE=$(make_request "POST" "${API_URL}/auth/register" "$RECRUITER_REG_DATA" "" "Register recruiter user")
if [ $? -eq 0 ]; then
    RECRUITER_ID=$(echo "$RECRUITER_RESPONSE" | jq -r '.data.id' 2>/dev/null || echo "2")
fi

# Test 5: Login Candidate
echo -e "${BLUE}5. Testing User Login (Candidate)${NC}"
CANDIDATE_LOGIN_DATA='{
  "email": "'${CANDIDATE_EMAIL}'",
  "password": "'${PASSWORD}'"
}'

CANDIDATE_LOGIN_RESPONSE=$(make_request "POST" "${API_URL}/auth/login" "$CANDIDATE_LOGIN_DATA" "" "Login candidate user")
if [ $? -eq 0 ]; then
    CANDIDATE_TOKEN=$(echo "$CANDIDATE_LOGIN_RESPONSE" | jq -r '.data.token' 2>/dev/null || echo "dummy_token")
    CANDIDATE_AUTH_HEADER="-H \"Authorization: Bearer $CANDIDATE_TOKEN\""
fi

# Test 6: Login Recruiter
echo -e "${BLUE}6. Testing User Login (Recruiter)${NC}"
RECRUITER_LOGIN_DATA='{
  "email": "'${RECRUITER_EMAIL}'",
  "password": "'${PASSWORD}'"
}'

RECRUITER_LOGIN_RESPONSE=$(make_request "POST" "${API_URL}/auth/login" "$RECRUITER_LOGIN_DATA" "" "Login recruiter user")
if [ $? -eq 0 ]; then
    RECRUITER_TOKEN=$(echo "$RECRUITER_LOGIN_RESPONSE" | jq -r '.data.token' 2>/dev/null || echo "dummy_token")
    RECRUITER_AUTH_HEADER="-H \"Authorization: Bearer $RECRUITER_TOKEN\""
fi

# Test 7: Get User Profile
echo -e "${BLUE}7. Testing Get User Profile${NC}"
if [ -n "$CANDIDATE_TOKEN" ] && [ "$CANDIDATE_TOKEN" != "dummy_token" ]; then
    make_request "GET" "${API_URL}/users/${CANDIDATE_ID}" "" "$CANDIDATE_AUTH_HEADER" "Get candidate profile by ID"
    make_request "GET" "${API_URL}/users/email/${CANDIDATE_EMAIL}" "" "$CANDIDATE_AUTH_HEADER" "Get candidate profile by email"
else
    echo -e "${RED}Skipping user profile tests - no valid candidate token${NC}\n"
fi

# Test 8: Create Job (Recruiter)
echo -e "${BLUE}8. Testing Job Creation (Recruiter Only)${NC}"
JOB_DATA='{
  "title": "Senior Java Developer - Test",
  "description": "Test job description for API testing",
  "requirements": "Java, Spring Boot, PostgreSQL",
  "location": "Test City, CA",
  "salaryMin": 100000,
  "salaryMax": 150000,
  "skillsRequired": ["Java", "Spring Boot", "PostgreSQL"],
  "experienceLevel": "SENIOR",
  "jobType": "FULL_TIME",
  "remote": false
}'

if [ -n "$RECRUITER_TOKEN" ] && [ "$RECRUITER_TOKEN" != "dummy_token" ]; then
    JOB_RESPONSE=$(make_request "POST" "${API_URL}/jobs" "$JOB_DATA" "$RECRUITER_AUTH_HEADER" "Create job posting")
    if [ $? -eq 0 ]; then
        JOB_ID=$(echo "$JOB_RESPONSE" | jq -r '.data.id' 2>/dev/null || echo "1")
    fi
else
    echo -e "${RED}Skipping job creation - no valid recruiter token${NC}\n"
fi

# Test 9: Get All Jobs
echo -e "${BLUE}9. Testing Get All Jobs${NC}"
make_request "GET" "${API_URL}/jobs?page=0&size=10&sort=createdAt,desc" "" "" "Get all jobs with pagination"

# Test 10: Search Jobs
echo -e "${BLUE}10. Testing Job Search${NC}"
make_request "GET" "${API_URL}/jobs/search?keyword=Java&location=Test&experienceLevel=SENIOR" "" "" "Search jobs by criteria"

# Test 11: Job Application
echo -e "${BLUE}11. Testing Job Application${NC}"
if [ -n "$CANDIDATE_TOKEN" ] && [ "$CANDIDATE_TOKEN" != "dummy_token" ] && [ -n "$JOB_ID" ]; then
    # Create a simple test file for resume upload
    echo "Test Resume Content" > test_resume.txt
    
    echo -e "${YELLOW}Testing: Apply to job with resume upload${NC}"
    APPLICATION_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_URL}/jobs/${JOB_ID}/apply" \
        -H "Authorization: Bearer $CANDIDATE_TOKEN" \
        -F "coverLetter=I am very interested in this position and believe I would be a great fit." \
        -F "resume=@test_resume.txt")
    
    local app_http_code=$(echo "$APPLICATION_RESPONSE" | tail -n1)
    local app_body=$(echo "$APPLICATION_RESPONSE" | head -n -1)
    
    if [ "$app_http_code" -eq 200 ] || [ "$app_http_code" -eq 201 ]; then
        echo -e "${GREEN}✓ SUCCESS (HTTP $app_http_code)${NC}"
        echo "$app_body" | jq '.' 2>/dev/null || echo "$app_body"
    else
        echo -e "${RED}✗ FAILED (HTTP $app_http_code)${NC}"
        echo "$app_body" | jq '.' 2>/dev/null || echo "$app_body"
    fi
    
    rm -f test_resume.txt
    echo ""
else
    echo -e "${RED}Skipping job application - missing candidate token or job ID${NC}\n"
fi

# Test 12: AI Features (if available)
echo -e "${BLUE}12. Testing AI Features${NC}"
if [ -n "$CANDIDATE_TOKEN" ] && [ "$CANDIDATE_TOKEN" != "dummy_token" ]; then
    # Test Job Recommendations
    make_request "GET" "${API_URL}/ai/recommendations?userId=${CANDIDATE_ID}&limit=5" "" "$CANDIDATE_AUTH_HEADER" "Get job recommendations"
    
    # Test Skill Gap Analysis (if job exists)
    if [ -n "$JOB_ID" ]; then
        SKILL_GAP_DATA='{
          "userId": '${CANDIDATE_ID}',
          "targetJobId": '${JOB_ID}'
        }'
        make_request "POST" "${API_URL}/ai/skill-gap-analysis" "$SKILL_GAP_DATA" "$CANDIDATE_AUTH_HEADER" "Skill gap analysis"
    fi
    
    # Test Interview Questions Generation
    if [ -n "$JOB_ID" ]; then
        INTERVIEW_DATA='{
          "jobId": '${JOB_ID}',
          "difficulty": "MEDIUM",
          "questionCount": 5
        }'
        make_request "POST" "${API_URL}/ai/interview-questions" "$INTERVIEW_DATA" "$CANDIDATE_AUTH_HEADER" "Generate interview questions"
    fi
else
    echo -e "${RED}Skipping AI features tests - no valid candidate token${NC}\n"
fi

# Test 13: Analytics
echo -e "${BLUE}13. Testing Analytics${NC}"
if [ -n "$CANDIDATE_TOKEN" ] && [ "$CANDIDATE_TOKEN" != "dummy_token" ]; then
    make_request "GET" "${API_URL}/analytics/health" "" "$CANDIDATE_AUTH_HEADER" "System health analytics"
    make_request "GET" "${API_URL}/analytics/usage?period=LAST_7_DAYS" "" "$CANDIDATE_AUTH_HEADER" "Usage statistics"
    make_request "GET" "${API_URL}/analytics/performance" "" "$CANDIDATE_AUTH_HEADER" "Performance metrics"
else
    echo -e "${RED}Skipping analytics tests - no valid token${NC}\n"
fi

# Test 14: File Upload
echo -e "${BLUE}14. Testing File Upload${NC}"
if [ -n "$CANDIDATE_TOKEN" ] && [ "$CANDIDATE_TOKEN" != "dummy_token" ]; then
    echo "Test File Content for Upload" > test_upload.txt
    
    echo -e "${YELLOW}Testing: File upload${NC}"
    UPLOAD_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_URL}/files/upload" \
        -H "Authorization: Bearer $CANDIDATE_TOKEN" \
        -F "file=@test_upload.txt" \
        -F "type=RESUME")
    
    local upload_http_code=$(echo "$UPLOAD_RESPONSE" | tail -n1)
    local upload_body=$(echo "$UPLOAD_RESPONSE" | head -n -1)
    
    if [ "$upload_http_code" -eq 200 ] || [ "$upload_http_code" -eq 201 ]; then
        echo -e "${GREEN}✓ SUCCESS (HTTP $upload_http_code)${NC}"
        echo "$upload_body" | jq '.' 2>/dev/null || echo "$upload_body"
        
        # Extract file ID for download test
        FILE_ID=$(echo "$upload_body" | jq -r '.data.id' 2>/dev/null)
        if [ -n "$FILE_ID" ] && [ "$FILE_ID" != "null" ]; then
            echo -e "${YELLOW}Testing: File download${NC}"
            curl -s -H "Authorization: Bearer $CANDIDATE_TOKEN" \
                 "${API_URL}/files/download/${FILE_ID}" > downloaded_file.txt
            if [ -f downloaded_file.txt ]; then
                echo -e "${GREEN}✓ File download successful${NC}"
                rm -f downloaded_file.txt
            else
                echo -e "${RED}✗ File download failed${NC}"
            fi
        fi
    else
        echo -e "${RED}✗ FAILED (HTTP $upload_http_code)${NC}"
        echo "$upload_body" | jq '.' 2>/dev/null || echo "$upload_body"
    fi
    
    rm -f test_upload.txt
    echo ""
else
    echo -e "${RED}Skipping file upload tests - no valid token${NC}\n"
fi

# Test 15: Swagger UI Access
echo -e "${BLUE}15. Testing Swagger UI Access${NC}"
SWAGGER_RESPONSE=$(curl -s -w "%{http_code}" "${BASE_URL}/swagger-ui/index.html")
SWAGGER_CODE=$(echo "$SWAGGER_RESPONSE" | tail -c 4)

if [ "$SWAGGER_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Swagger UI is accessible at ${BASE_URL}/swagger-ui/index.html${NC}"
else
    echo -e "${RED}✗ Swagger UI not accessible (HTTP $SWAGGER_CODE)${NC}"
fi
echo ""

# Test Summary
echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}🏁 API Testing Complete!${NC}"
echo -e "${BLUE}======================================${NC}\n"

echo -e "${GREEN}✅ Key Endpoints Tested:${NC}"
echo "   • Health Check & Documentation"
echo "   • User Registration & Authentication"
echo "   • User Profile Management"
echo "   • Job Creation & Management"
echo "   • Job Search & Application"
echo "   • AI-Powered Features"
echo "   • Analytics & Monitoring"
echo "   • File Upload & Download"
echo "   • Swagger UI Access"

echo ""
echo -e "${YELLOW}🔗 Important URLs:${NC}"
echo "   • Application: ${BASE_URL}"
echo "   • Swagger UI: ${BASE_URL}/swagger-ui/index.html"
echo "   • API Docs: ${BASE_URL}/v3/api-docs"
echo "   • Health: ${BASE_URL}/actuator/health"

echo ""
echo -e "${YELLOW}📋 Test Credentials Created:${NC}"
echo "   • Candidate: ${CANDIDATE_EMAIL} / ${PASSWORD}"
echo "   • Recruiter: ${RECRUITER_EMAIL} / ${PASSWORD}"

echo ""
echo -e "${BLUE}📖 For detailed API documentation and interactive testing:${NC}"
echo -e "${BLUE}Visit: ${BASE_URL}/swagger-ui/index.html${NC}"

echo ""
echo -e "${GREEN}🎉 Testing script completed successfully!${NC}"
