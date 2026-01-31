# 📚 API Testing Guide

This guide provides comprehensive examples for testing all API endpoints using curl commands and includes Swagger UI integration.

## 🔗 Quick Access

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Application Health**: http://localhost:8080/actuator/health

## 🚀 Getting Started

### 1. Start the Application
```bash
cd jobportal-backend
./mvnw spring-boot:run
```

### 2. Access Swagger UI
Open your browser and go to: http://localhost:8080/swagger-ui/index.html

### 3. Test with curl (Alternative)
Use the curl examples below for command-line testing.

## 🔑 Authentication Flow

### Step 1: Register a New User
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "candidate@example.com",
    "password": "password123",
    "fullName": "John Candidate",
    "role": "CANDIDATE",
    "phone": "+1234567890"
  }'
```

### Step 2: Login to Get JWT Token
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "candidate@example.com",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "refresh_token_here",
    "user": {
      "id": 1,
      "email": "candidate@example.com",
      "fullName": "John Candidate",
      "role": "CANDIDATE"
    }
  },
  "timestamp": "2026-01-31T10:00:00"
}
```

### Step 3: Use Token in Subsequent Requests
```bash
# Export token for easier use
export JWT_TOKEN="your_jwt_token_here"

# Use in requests
curl -X GET "http://localhost:8080/api/users/1" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## 👥 User Management APIs

### Get User by ID
```bash
curl -X GET "http://localhost:8080/api/users/1" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Get User by Email
```bash
curl -X GET "http://localhost:8080/api/users/email/candidate@example.com" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Update User Profile
```bash
curl -X PUT "http://localhost:8080/api/users/1" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Updated Candidate",
    "phone": "+1234567891"
  }'
```

## 💼 Job Management APIs

### Create Job (Recruiter Only)
First, register as a recruiter:
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "recruiter@company.com",
    "password": "password123",
    "fullName": "Jane Recruiter",
    "role": "RECRUITER",
    "phone": "+1234567892"
  }'
```

Login and create a job:
```bash
curl -X POST "http://localhost:8080/api/jobs" \
  -H "Authorization: Bearer $RECRUITER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Senior Java Developer",
    "description": "We are looking for a senior Java developer with expertise in Spring Boot and microservices architecture.",
    "requirements": "• 5+ years of Java experience\n• Spring Boot expertise\n• Microservices architecture\n• PostgreSQL knowledge",
    "location": "San Francisco, CA",
    "salaryMin": 120000,
    "salaryMax": 180000,
    "skillsRequired": ["Java", "Spring Boot", "PostgreSQL", "Docker"],
    "experienceLevel": "SENIOR",
    "jobType": "FULL_TIME",
    "remote": false
  }'
```

### Get All Jobs
```bash
curl -X GET "http://localhost:8080/api/jobs?page=0&size=10&sort=createdAt,desc"
```

### Search Jobs
```bash
curl -X GET "http://localhost:8080/api/jobs/search?keyword=Java&location=San Francisco&experienceLevel=SENIOR"
```

### Apply to Job
```bash
curl -X POST "http://localhost:8080/api/jobs/1/apply" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "coverLetter=I am very interested in this Java developer position..." \
  -F "resume=@/path/to/your/resume.pdf"
```

## 🤖 AI-Powered Features

### Parse Resume
```bash
curl -X POST "http://localhost:8080/api/ai/parse-resume" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "file=@/path/to/resume.pdf"
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "personalInfo": {
      "name": "John Doe",
      "email": "john@example.com",
      "phone": "+1234567890"
    },
    "skills": ["Java", "Spring Boot", "React", "PostgreSQL"],
    "experience": [
      {
        "company": "Tech Corp",
        "position": "Senior Developer",
        "duration": "2020-2024",
        "description": "Led development team of 5 engineers..."
      }
    ],
    "education": [
      {
        "institution": "University of Technology",
        "degree": "Bachelor of Computer Science",
        "year": "2020"
      }
    ]
  }
}
```

### Get Job Recommendations
```bash
curl -X GET "http://localhost:8080/api/ai/recommendations?userId=1&limit=10" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Skill Gap Analysis
```bash
curl -X POST "http://localhost:8080/api/ai/skill-gap-analysis" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "targetJobId": 5
  }'
```

### Generate Interview Questions
```bash
curl -X POST "http://localhost:8080/api/ai/interview-questions" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jobId": 5,
    "difficulty": "MEDIUM",
    "questionCount": 10
  }'
```

## 📊 Analytics APIs

### System Health
```bash
curl -X GET "http://localhost:8080/api/analytics/health" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Usage Statistics
```bash
curl -X GET "http://localhost:8080/api/analytics/usage?period=LAST_30_DAYS" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Performance Metrics
```bash
curl -X GET "http://localhost:8080/api/analytics/performance" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## 📁 File Management

### Upload File
```bash
curl -X POST "http://localhost:8080/api/files/upload" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "file=@/path/to/document.pdf" \
  -F "type=RESUME"
```

### Download File
```bash
curl -X GET "http://localhost:8080/api/files/download/file-id-here" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  --output downloaded_file.pdf
```

## 🛡️ Admin APIs

### Get All Users (Admin Only)
```bash
curl -X GET "http://localhost:8080/api/admin/users?page=0&size=20" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### System Statistics
```bash
curl -X GET "http://localhost:8080/api/admin/stats" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Manage User Status
```bash
curl -X PATCH "http://localhost:8080/api/admin/users/1/status" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "ACTIVE"
  }'
```

## 🧪 Testing Scenarios

### Complete User Journey Test
```bash
#!/bin/bash
# Complete user journey test script

echo "=== Testing Complete User Journey ==="

# 1. Register candidate
echo "1. Registering candidate..."
CANDIDATE_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testcandidate@example.com",
    "password": "password123",
    "fullName": "Test Candidate",
    "role": "CANDIDATE",
    "phone": "+1234567890"
  }')

echo $CANDIDATE_RESPONSE | jq .

# 2. Register recruiter
echo "2. Registering recruiter..."
RECRUITER_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testrecruiter@example.com",
    "password": "password123",
    "fullName": "Test Recruiter",
    "role": "RECRUITER",
    "phone": "+1234567891"
  }')

echo $RECRUITER_RESPONSE | jq .

# 3. Login candidate
echo "3. Logging in candidate..."
CANDIDATE_LOGIN=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testcandidate@example.com",
    "password": "password123"
  }')

CANDIDATE_TOKEN=$(echo $CANDIDATE_LOGIN | jq -r '.data.token')
echo "Candidate token: $CANDIDATE_TOKEN"

# 4. Login recruiter
echo "4. Logging in recruiter..."
RECRUITER_LOGIN=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testrecruiter@example.com",
    "password": "password123"
  }')

RECRUITER_TOKEN=$(echo $RECRUITER_LOGIN | jq -r '.data.token')
echo "Recruiter token: $RECRUITER_TOKEN"

# 5. Create job
echo "5. Creating job..."
JOB_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/jobs" \
  -H "Authorization: Bearer $RECRUITER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Java Developer",
    "description": "Test job description",
    "requirements": "Java, Spring Boot",
    "location": "Test City",
    "salaryMin": 80000,
    "salaryMax": 120000,
    "skillsRequired": ["Java", "Spring Boot"],
    "experienceLevel": "INTERMEDIATE",
    "jobType": "FULL_TIME",
    "remote": false
  }')

JOB_ID=$(echo $JOB_RESPONSE | jq -r '.data.id')
echo "Created job ID: $JOB_ID"

# 6. Get jobs
echo "6. Getting all jobs..."
curl -s -X GET "http://localhost:8080/api/jobs?page=0&size=5" | jq .

echo "=== Test Complete ==="
```

## 📋 Response Status Codes

| Code | Status | Description |
|------|--------|-------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Authentication required |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists |
| 500 | Internal Server Error | Server error |

## 🔧 Troubleshooting

### Common Issues

#### 1. 401 Unauthorized
- Ensure JWT token is valid and not expired
- Check the Authorization header format: `Bearer <token>`

#### 2. 403 Forbidden
- Verify user has the required role for the operation
- Check role-based access control (RBAC) settings

#### 3. 409 Conflict
- User with email already exists during registration
- Try with a different email address

#### 4. File Upload Issues
- Ensure file size is under 10MB
- Check file format is supported
- Verify multipart/form-data content type

### Debug Commands

#### Check Application Status
```bash
curl -X GET "http://localhost:8080/actuator/health"
```

#### View Application Info
```bash
curl -X GET "http://localhost:8080/actuator/info"
```

#### Check Database Connection
```bash
# This will return an error if DB is down
curl -X GET "http://localhost:8080/api/users/999" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## 📖 Additional Resources

- **Swagger UI**: Interactive API documentation
- **OpenAPI Specification**: Machine-readable API definition
- **Postman Collection**: Import API endpoints into Postman
- **Application Logs**: Check backend logs for detailed error information

---

*For more detailed examples and interactive testing, please use the Swagger UI interface at http://localhost:8080/swagger-ui/index.html*
