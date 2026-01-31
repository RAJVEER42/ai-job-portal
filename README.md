<h1 align="center">🚀 AI-Powered Job Portal</h1>
---

<img width="3022" height="1712" alt="image" src="https://github.com/user-attachments/assets/157eb0a7-a9f4-4117-98e7-71b6c590591a" />
<img width="3024" height="1714" alt="image" src="https://github.com/user-attachments/assets/4080e3eb-f67c-4854-a0e5-d23fbfd0e4fc" />

---

A comprehensive, enterprise-grade job portal application featuring AI-powered matching, modern UI/UX, and production-ready infrastructure.

## 🌟 Overview

This project is a full-stack AI-powered job portal that connects job seekers with employers through intelligent matching algorithms. Built with Spring Boot backend and React frontend, it includes advanced features like resume parsing, job recommendations, skill gap analysis, and real-time analytics.

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React Frontend│    │  Spring Boot    │    │   PostgreSQL    │
│   (Port 3000)   │◄──►│   Backend       │◄──►│    Database     │
│                 │    │  (Port 8080)    │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                       ┌─────────────────┐
                       │   OpenAI API    │
                       │  (AI Features)  │
                       └─────────────────┘
```

## 📚 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Quick Start](#-quick-start)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Configuration](#-configuration)
- [Deployment](#-deployment)
- [Testing](#-testing)
- [Documentation](#-documentation)
- [Contributing](#-contributing)

## ✨ Features

### 🤖 AI-Powered Features
- **Intelligent Resume Parsing** - Extract skills, experience, and qualifications
- **Smart Job Matching** - ML-based job recommendations
- **Skill Gap Analysis** - Identify missing skills for desired positions
- **AI Interview Questions** - Generate customized interview questions

### 👥 User Management
- **Multi-role Authentication** - Candidate, Recruiter, Admin roles
- **Profile Management** - Comprehensive user profiles
- **JWT-based Security** - Secure authentication and authorization

### 💼 Job Management
- **Job Posting** - Rich job descriptions with requirements
- **Application Tracking** - Complete application lifecycle
- **Advanced Search** - Filter by location, skills, salary, etc.
- **Real-time Updates** - Live job status updates

### 📊 Analytics & Monitoring
- **Performance Metrics** - System health monitoring
- **Usage Analytics** - User behavior tracking
- **Cache Management** - Redis caching for performance
- **Circuit Breaker Pattern** - Fault tolerance

### 🎨 Modern UI/UX
- **Responsive Design** - Mobile-first approach
- **Dark/Light Themes** - User preference support
- **Component Library** - Reusable UI components
- **Professional Design** - Modern, intuitive interface

## 🛠️ Tech Stack

### Backend
- **Spring Boot 3.2.2** - Core framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database abstraction
- **PostgreSQL** - Primary database
- **JWT** - Token-based authentication
- **OpenAI API** - AI functionality
- **Swagger/OpenAPI** - API documentation
- **Docker** - Containerization

### Frontend
- **React 18** - UI framework
- **React Router** - Navigation
- **Axios** - HTTP client
- **Tailwind CSS** - Styling
- **Lucide React** - Icons
- **Context API** - State management

### DevOps & Tools
- **Docker Compose** - Local development
- **Kubernetes** - Container orchestration
- **Nginx** - Reverse proxy
- **Prometheus** - Monitoring
- **Grafana** - Visualization

## 🚀 Quick Start

### Prerequisites
- **Java 17+**
- **Node.js 16+**
- **PostgreSQL 12+**
- **Docker** (optional)

### 1. Clone Repository
```bash
git clone <repository-url>
cd AI_Job_Portal
```

### 2. Database Setup
```bash
# Create PostgreSQL database
psql -U postgres
CREATE DATABASE jobportal_db;
\q
```

### 3. Backend Setup
```bash
cd jobportal-backend
./mvnw clean install
./mvnw spring-boot:run
```

### 4. Frontend Setup
```bash
cd jobportal-frontend
npm install
npm start
```

### 5. Access Application
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html

## 📖 API Documentation

### 🔗 Swagger UI
Access comprehensive API documentation at: **http://localhost:8080/swagger-ui/index.html**

### 🔑 Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "role": "CANDIDATE",
  "phone": "+1234567890"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "refresh_token_here",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "fullName": "John Doe",
      "role": "CANDIDATE"
    }
  },
  "timestamp": "2026-01-31T10:00:00"
}
```

### 👤 User Management

#### Get User Profile
```http
GET /api/users/{id}
Authorization: Bearer <token>
```

#### Update User Profile
```http
PUT /api/users/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "fullName": "John Updated",
  "phone": "+1234567891"
}
```

### 💼 Job Management

#### Create Job (Recruiter Only)
```http
POST /api/jobs
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Senior Java Developer",
  "description": "We are looking for a senior Java developer...",
  "requirements": "5+ years of Java experience",
  "location": "San Francisco, CA",
  "salaryMin": 100000,
  "salaryMax": 150000,
  "skillsRequired": ["Java", "Spring Boot", "PostgreSQL"],
  "experienceLevel": "SENIOR",
  "jobType": "FULL_TIME",
  "remote": false
}
```

#### Get All Jobs
```http
GET /api/jobs?page=0&size=10&sort=createdAt,desc
```

#### Search Jobs
```http
GET /api/jobs/search?keyword=Java&location=San Francisco&experienceLevel=SENIOR
```

#### Apply to Job
```http
POST /api/jobs/{jobId}/apply
Authorization: Bearer <token>
Content-Type: multipart/form-data

coverLetter: "I am interested in this position..."
resume: [FILE]
```

### 🤖 AI-Powered Features

#### Parse Resume
```http
POST /api/ai/parse-resume
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: [PDF/DOC file]
```

**Response:**
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
        "description": "Led development team..."
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

#### Get Job Recommendations
```http
GET /api/ai/recommendations?userId=1&limit=10
Authorization: Bearer <token>
```

#### Skill Gap Analysis
```http
POST /api/ai/skill-gap-analysis
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 1,
  "targetJobId": 5
}
```

#### Generate Interview Questions
```http
POST /api/ai/interview-questions
Authorization: Bearer <token>
Content-Type: application/json

{
  "jobId": 5,
  "difficulty": "MEDIUM",
  "questionCount": 10
}
```

### 📊 Analytics Endpoints

#### System Health
```http
GET /api/analytics/health
Authorization: Bearer <token>
```

#### Usage Statistics
```http
GET /api/analytics/usage?period=LAST_30_DAYS
Authorization: Bearer <token>
```

#### Performance Metrics
```http
GET /api/analytics/performance
Authorization: Bearer <token>
```

### 📁 File Management

#### Upload File
```http
POST /api/files/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: [FILE]
type: "RESUME" | "PROFILE_PICTURE" | "JOB_DOCUMENT"
```

#### Download File
```http
GET /api/files/download/{fileId}
Authorization: Bearer <token>
```

### 🛡️ Admin Endpoints

#### Get All Users (Admin Only)
```http
GET /api/admin/users?page=0&size=20
Authorization: Bearer <admin_token>
```

#### System Statistics
```http
GET /api/admin/stats
Authorization: Bearer <admin_token>
```

#### Manage User Status
```http
PATCH /api/admin/users/{userId}/status
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "status": "ACTIVE" | "INACTIVE" | "SUSPENDED"
}
```

## 🗄️ Database Schema

### Core Tables

#### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Jobs Table
```sql
CREATE TABLE jobs (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    requirements TEXT,
    location VARCHAR(255),
    salary_min DECIMAL(10,2),
    salary_max DECIMAL(10,2),
    experience_level VARCHAR(50),
    job_type VARCHAR(50),
    remote BOOLEAN DEFAULT false,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    recruiter_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Applications Table
```sql
CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT REFERENCES jobs(id),
    candidate_id BIGINT REFERENCES users(id),
    cover_letter TEXT,
    resume_url VARCHAR(500),
    status VARCHAR(50) DEFAULT 'PENDING',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## ⚙️ Configuration

### Backend Configuration
Edit `/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/jobportal_db
spring.datasource.username=postgres
spring.datasource.password=admin123

# JWT
jwt.secret=your-secret-key-here
jwt.expiration=900000

# OpenAI
openai.api.key=your-openai-api-key
openai.model=gpt-4o
openai.max.tokens=2000

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Server
server.port=8080
```

### Frontend Configuration
Create `.env` file:

```env
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_APP_NAME=AI Job Portal
REACT_APP_VERSION=1.0.0
```

## 🐳 Docker Deployment

### Using Docker Compose
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Individual Services
```bash
# Backend
docker build -t jobportal-backend .
docker run -p 8080:8080 jobportal-backend

# Frontend
docker build -t jobportal-frontend .
docker run -p 3000:3000 jobportal-frontend
```

## ☸️ Kubernetes Deployment

```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods
kubectl get services

# Access application
kubectl port-forward service/jobportal-frontend 3000:3000
```

## 🧪 Testing

### Backend Tests
```bash
cd jobportal-backend
./mvnw test
```

### Frontend Tests
```bash
cd jobportal-frontend
npm test
```

### API Testing
```bash
# Using the provided test scripts
./test-functionality.sh
./api-diagnostic.sh
```

### Load Testing
```bash
# Backend load testing
./load-test.sh
```

## 📊 Monitoring

### Application Metrics
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001

### Health Checks
- **Backend Health**: http://localhost:8080/actuator/health
- **Frontend Health**: http://localhost:3000/health

## 🔒 Security Features

- **JWT Authentication** with refresh tokens
- **Role-based Access Control** (RBAC)
- **CORS Configuration** for cross-origin requests
- **Input Validation** and sanitization
- **SQL Injection Protection** via JPA
- **Rate Limiting** for API endpoints
- **Secure Password Hashing** using BCrypt

## 🚀 Performance Optimizations

- **Database Connection Pooling**
- **Query Optimization** with JPA criteria
- **Caching Layer** with Redis
- **File Upload Optimization**
- **Lazy Loading** for large datasets
- **Response Compression**

## 📈 Monitoring & Analytics

### Key Metrics Tracked
- **User Registrations** and login patterns
- **Job Posting** and application rates
- **AI Feature Usage** (resume parsing, recommendations)
- **System Performance** (response times, error rates)
- **Cache Hit Rates** and database performance

### Available Dashboards
- **Admin Dashboard** - System overview and user management
- **Recruiter Dashboard** - Job postings and applications
- **Candidate Dashboard** - Job search and applications
- **Analytics Dashboard** - Comprehensive metrics and insights

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📋 Documentation

This project includes comprehensive documentation:

### 📄 Available Documentation Files

- **`README.md`** - Main project documentation (this file)
- **`API_TESTING_GUIDE.md`** - Comprehensive API testing guide with curl examples
- **`AI_Job_Portal_API.postman_collection.json`** - Postman collection for easy API testing
- **`api-test-complete.sh`** - Automated testing script for all API endpoints

### 🔧 Interactive Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
  - Interactive API documentation
  - Try out endpoints directly from the browser
  - View request/response schemas
  - Authentication support built-in

- **OpenAPI Specification**: http://localhost:8080/v3/api-docs
  - Machine-readable API definition
  - Can be imported into various tools
  - Supports code generation

### 📱 Testing Tools

#### 1. Automated Testing Script
```bash
# Run comprehensive API tests
./api-test-complete.sh
```

#### 2. Postman Collection
```bash
# Import the collection file into Postman
AI_Job_Portal_API.postman_collection.json
```

#### 3. Manual Testing Guide
Refer to `API_TESTING_GUIDE.md` for detailed curl commands and examples.

### 🔍 API Endpoints Overview

| Category | Endpoints | Authentication | Description |
|----------|-----------|----------------|-------------|
| **Authentication** | `/api/auth/*` | None | Login, registration, token refresh |
| **User Management** | `/api/users/*` | Required | Profile management, user operations |
| **Job Management** | `/api/jobs/*` | Varies | Job CRUD, search, applications |
| **AI Features** | `/api/ai/*` | Required | Resume parsing, recommendations, skill analysis |
| **Analytics** | `/api/analytics/*` | Required | Performance metrics, usage statistics |
| **File Management** | `/api/files/*` | Required | Upload, download, file operations |
| **Admin** | `/api/admin/*` | Admin Only | User management, system administration |

### 🚀 Quick API Test

Test if the API is running:
```bash
curl http://localhost:8080/actuator/health
```

Access Swagger UI:
```bash
open http://localhost:8080/swagger-ui/index.html
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- OpenAI for AI capabilities
- React team for the frontend framework
- PostgreSQL team for the robust database
- All contributors and testers

## 📞 Support

For support and questions:

- **Email**: Rajveer.24BCS10404@sst.scaler.com
- **Documentation**: http://localhost:8080/swagger-ui/index.html
- **Issues**: GitHub Issues page

---

**Built with ❤️ by Rajveer Bishnoi**

*AI-Powered Job Portal - Connecting Talent with Opportunity*
