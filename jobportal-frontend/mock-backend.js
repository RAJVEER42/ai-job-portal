const express = require('express');
const cors = require('cors');
const multer = require('multer');
const app = express();

// Configure multer for file uploads
const storage = multer.memoryStorage();
const upload = multer({ storage: storage, limits: { fileSize: 10 * 1024 * 1024 } });

// Enable CORS for frontend
app.use(cors({
  origin: ['http://localhost:3000', 'http://localhost:3001'], // Support both ports
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));

// Add request logging middleware
app.use((req, res, next) => {
  console.log(`🔄 ${req.method} ${req.url} from ${req.get('Origin') || 'no-origin'}`);
  next();
});

app.use(express.json());

// Mock auth endpoints
app.post('/api/auth/login', (req, res) => {
  console.log('📧 Login attempt:', req.body.email);
  res.json({
    success: true,
    message: 'Login successful',
    data: {
      accessToken: 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNzY4OTI5MDAwLCJleHAiOjE3Njg5MzI2MDB9.mock-token',
      refreshToken: 'mock-refresh-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      user: {
        id: 1,
        email: req.body.email,
        fullName: 'Test User',
        role: 'CANDIDATE',
        phone: null
      }
    }
  });
});

app.post('/api/auth/register', (req, res) => {
  console.log('👤 Registration attempt:', req.body.email);
  res.json({
    success: true,
    message: 'User registered successfully',
    data: {
      id: Date.now(),
      email: req.body.email,
      fullName: req.body.fullName,
      role: req.body.role,
      phone: req.body.phone || null
    }
  });
});

// Mock jobs endpoint
app.get('/api/jobs', (req, res) => {
  const { keyword } = req.query;
  console.log('💼 Jobs request, keyword:', keyword);
  
  const jobs = [
    {
      id: 1,
      title: 'Senior Full Stack Developer',
      company: 'TechCorp Inc.',
      location: 'San Francisco, CA',
      salary: '$120,000 - $150,000',
      type: 'Full-time',
      description: 'Looking for a senior developer with React and Node.js experience.',
      requirements: ['React', 'Node.js', 'JavaScript', '5+ years experience'],
      posted: '2026-01-15'
    },
    {
      id: 2,
      title: 'Frontend Developer', 
      company: 'WebSolutions',
      location: 'New York, NY',
      salary: '$90,000 - $120,000',
      type: 'Full-time',
      description: 'Frontend developer needed for modern web applications.',
      requirements: ['React', 'JavaScript', 'HTML', 'CSS'],
      posted: '2026-01-18'
    },
    {
      id: 3,
      title: 'DevOps Engineer',
      company: 'CloudTech',
      location: 'Remote',
      salary: '$110,000 - $140,000',
      type: 'Full-time', 
      description: 'DevOps engineer with cloud platform experience.',
      requirements: ['AWS', 'Docker', 'Kubernetes', 'CI/CD'],
      posted: '2026-01-19'
    }
  ];

  let filteredJobs = jobs;
  if (keyword) {
    filteredJobs = jobs.filter(job => 
      job.title.toLowerCase().includes(keyword.toLowerCase()) ||
      job.company.toLowerCase().includes(keyword.toLowerCase()) ||
      job.description.toLowerCase().includes(keyword.toLowerCase())
    );
  }

  res.json({
    success: true,
    data: filteredJobs,
    total: filteredJobs.length
  });
});

app.get('/api/jobs/search', (req, res) => {
  const { keyword } = req.query;
  console.log('🔍 Job search:', keyword);
  
  // Redirect to main jobs endpoint with keyword
  req.url = `/api/jobs?keyword=${keyword}`;
  app._router.handle(req, res);
});

app.get('/api/jobs/:id', (req, res) => {
  const { id } = req.params;
  console.log('📋 Job details request:', id);
  
  const job = {
    id: parseInt(id),
    title: 'Senior Full Stack Developer',
    company: 'TechCorp Inc.',
    location: 'San Francisco, CA',
    salary: '$120,000 - $150,000',
    type: 'Full-time',
    remote: 'Hybrid',
    experience: '5+ years',
    description: 'We are looking for a Senior Full Stack Developer to join our team. You will be responsible for developing and maintaining web applications using modern technologies.',
    requirements: [
      '5+ years of software development experience',
      'Strong proficiency in JavaScript, React, and Node.js',
      'Experience with databases (PostgreSQL, MongoDB)',
      'Knowledge of cloud platforms (AWS, Azure)',
      'Experience with DevOps tools (Docker, Kubernetes)',
      'Strong problem-solving skills'
    ],
    responsibilities: [
      'Develop and maintain web applications',
      'Collaborate with cross-functional teams',
      'Write clean, maintainable code',
      'Participate in code reviews',
      'Mentor junior developers'
    ],
    benefits: [
      'Competitive salary',
      'Health insurance',
      'Retirement plan',
      'Flexible working hours',
      'Professional development'
    ],
    posted: '2026-01-15',
    deadline: '2026-02-15'
  };

  res.json({
    success: true,
    data: job
  });
});

// Mock file upload with better handling
app.post('/api/files/upload', upload.single('file'), (req, res) => {
  console.log('📎 File upload attempt');
  
  if (!req.file) {
    return res.status(400).json({
      success: false,
      message: 'No file uploaded'
    });
  }

  console.log('📁 File details:', {
    name: req.file.originalname,
    size: req.file.size,
    mimetype: req.file.mimetype
  });

  // Simulate file type validation
  const allowedTypes = [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'text/plain' // Allow for testing
  ];

  if (!allowedTypes.includes(req.file.mimetype)) {
    return res.status(400).json({
      success: false,
      message: 'Invalid file type. Only PDF, DOCX, and DOC are allowed'
    });
  }

  if (req.file.size > 10 * 1024 * 1024) {
    return res.status(413).json({
      success: false,
      message: 'File too large. Maximum size is 10MB'
    });
  }

  // Simulate successful upload
  res.json({
    success: true,
    message: 'Resume uploaded successfully!',
    data: {
      filename: req.file.originalname,
      size: req.file.size,
      type: req.file.mimetype,
      uploadedAt: new Date().toISOString(),
      url: `/files/${Date.now()}-${req.file.originalname}`
    }
  });
});

// Health check
app.get('/api/health', (req, res) => {
  res.json({
    status: 'OK',
    message: 'Mock backend is running',
    timestamp: new Date().toISOString()
  });
});

const PORT = 8081; // Changed from 8080 to avoid conflict
app.listen(PORT, () => {
  console.log(`🚀 Mock Backend Server Running`);
  console.log(`================================`);
  console.log(`📡 Server: http://localhost:${PORT}`);
  console.log(`✅ CORS: Enabled for http://localhost:3000 & http://localhost:3001`);
  console.log(`🔐 Auth: Mock login/register available`);
  console.log(`💼 Jobs: Sample job listings available`);
  console.log(`📎 Upload: File upload simulation enabled`);
  console.log(`================================`);
  console.log(`Ready to test your frontend! 🎉`);
});
