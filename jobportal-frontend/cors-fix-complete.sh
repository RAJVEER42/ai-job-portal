#!/bin/bash

# CORS Error Fix - Complete Solution
# Addresses CORS policy blocking between frontend (3000) and backend (8080)

echo "🔧 CORS Error Fix - Complete Solution"
echo "======================================"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
PURPLE='\033[0;35m'
NC='\033[0m'

echo -e "${BLUE}🔍 Understanding the CORS Error:${NC}"
echo "================================="
echo "❌ Error: Access to XMLHttpRequest blocked by CORS policy"
echo "❌ Frontend (3000) → Backend (8080) requests blocked"
echo "❌ Missing 'Access-Control-Allow-Origin' header"
echo ""

echo -e "${YELLOW}📋 CORS Error Causes:${NC}"
echo "====================="
echo "1. Backend server not running on port 8080"
echo "2. Backend running but CORS not configured"
echo "3. Backend running on different port"
echo "4. Firewall/network blocking the connection"
echo ""

echo -e "${BLUE}🔍 Diagnostic Check:${NC}"
echo "==================="

# Check backend
echo -n "Backend (8080): "
if curl -s --connect-timeout 3 http://localhost:8080/api/jobs >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Running${NC}"
    BACKEND_RUNNING=true
else
    echo -e "${RED}❌ Not responding${NC}"
    BACKEND_RUNNING=false
fi

# Check frontend
echo -n "Frontend (3000): "
if curl -s --connect-timeout 3 http://localhost:3000 >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Running${NC}"
    FRONTEND_RUNNING=true
else
    echo -e "${RED}❌ Not responding${NC}"
    FRONTEND_RUNNING=false
fi

# Check for Java processes
echo -n "Java processes: "
JAVA_COUNT=$(ps aux | grep -i java | grep -v grep | wc -l | tr -d ' ')
if [ "$JAVA_COUNT" -gt 0 ]; then
    echo -e "${GREEN}$JAVA_COUNT found${NC}"
else
    echo -e "${RED}None found${NC}"
fi

echo ""

if [ "$BACKEND_RUNNING" = false ]; then
    echo -e "${RED}🚨 MAIN ISSUE: Backend server is not running!${NC}"
    echo -e "${YELLOW}📝 Solutions:${NC}"
    echo ""
    
    echo -e "${PURPLE}Option 1: Start Your Spring Boot Backend${NC}"
    echo "========================================"
    echo "If you have the backend code:"
    echo "cd /path/to/your/backend"
    echo "mvn spring-boot:run"
    echo "# OR"
    echo "./gradlew bootRun"
    echo "# OR"
    echo "java -jar target/your-app.jar"
    echo ""
    
    echo -e "${PURPLE}Option 2: Use Mock Backend for Testing${NC}"
    echo "====================================="
    echo "Create a simple mock server for testing:"
    
    # Create mock backend
    cat > mock-backend.js << 'EOF'
const express = require('express');
const cors = require('cors');
const app = express();

// Enable CORS for all origins
app.use(cors({
  origin: 'http://localhost:3000',
  credentials: true
}));

app.use(express.json());

// Mock auth endpoints
app.post('/api/auth/login', (req, res) => {
  res.json({
    success: true,
    message: 'Login successful',
    data: {
      accessToken: 'mock-token-12345',
      refreshToken: 'mock-refresh-12345',
      user: {
        id: 1,
        email: req.body.email,
        fullName: 'Test User',
        role: 'CANDIDATE'
      }
    }
  });
});

app.post('/api/auth/register', (req, res) => {
  res.json({
    success: true,
    message: 'Registration successful',
    data: {
      id: 1,
      email: req.body.email,
      fullName: req.body.fullName,
      role: req.body.role
    }
  });
});

// Mock jobs endpoint
app.get('/api/jobs', (req, res) => {
  res.json({
    success: true,
    data: [
      {
        id: 1,
        title: 'Full Stack Developer',
        company: 'Tech Corp',
        location: 'San Francisco, CA',
        salary: '$120,000'
      },
      {
        id: 2,
        title: 'Frontend Developer', 
        company: 'Web Solutions',
        location: 'New York, NY',
        salary: '$100,000'
      }
    ]
  });
});

// Mock file upload
app.post('/api/files/upload', (req, res) => {
  res.json({
    success: true,
    message: 'File uploaded successfully',
    data: {
      filename: 'resume.pdf',
      size: 1024000,
      url: '/files/resume.pdf'
    }
  });
});

const PORT = 8080;
app.listen(PORT, () => {
  console.log(\`🚀 Mock backend running on http://localhost:\${PORT}\`);
  console.log(\`✅ CORS enabled for http://localhost:3000\`);
});
EOF

    echo ""
    echo "📦 To use the mock backend:"
    echo "npm install express cors"
    echo "node mock-backend.js"
    echo ""
    
    echo -e "${PURPLE}Option 3: Development Proxy (Temporary Fix)${NC}"
    echo "=========================================="
    echo "Add proxy to package.json:"
    echo '{"proxy": "http://localhost:8080"}'
    echo ""
    
else
    echo -e "${GREEN}✅ Backend is running - checking CORS configuration...${NC}"
    
    # Test CORS headers
    echo -e "${BLUE}🔧 Testing CORS headers:${NC}"
    CORS_TEST=$(curl -s -I -H "Origin: http://localhost:3000" http://localhost:8080/api/jobs | grep -i "access-control")
    
    if [ ! -z "$CORS_TEST" ]; then
        echo -e "${GREEN}✅ CORS headers present:${NC}"
        echo "$CORS_TEST"
    else
        echo -e "${RED}❌ CORS headers missing${NC}"
        echo ""
        echo -e "${YELLOW}🔧 Backend CORS Configuration Needed:${NC}"
        echo "Add to your Spring Boot application:"
        echo ""
        echo "@Configuration"
        echo "public class CorsConfig {"
        echo "    @Bean"
        echo "    public CorsConfigurationSource corsConfigurationSource() {"
        echo "        CorsConfiguration configuration = new CorsConfiguration();"
        echo "        configuration.setAllowedOrigins(Arrays.asList(\"http://localhost:3000\"));"
        echo "        configuration.setAllowedMethods(Arrays.asList(\"GET\", \"POST\", \"PUT\", \"DELETE\"));"
        echo "        configuration.setAllowedHeaders(Arrays.asList(\"*\"));"
        echo "        configuration.setAllowCredentials(true);"
        echo "        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();"
        echo "        source.registerCorsConfiguration(\"/api/**\", configuration);"
        echo "        return source;"
        echo "    }"
        echo "}"
    fi
fi

echo ""
echo -e "${BLUE}🔄 Immediate Temporary Solutions:${NC}"
echo "================================="

if [ "$BACKEND_RUNNING" = false ]; then
    echo "1. 🚀 Start the mock backend (see Option 2 above)"
    echo "2. 📦 Add proxy to package.json and restart frontend"
    echo "3. 🌐 Use browser extension to disable CORS (dev only)"
    echo ""
    
    echo -e "${YELLOW}Quick Mock Backend Setup:${NC}"
    echo "npm install express cors"
    echo "node mock-backend.js"
    echo ""
    
    echo -e "${YELLOW}Or add proxy to package.json:${NC}"
    echo "Add this line to package.json:"
    echo '"proxy": "http://localhost:8080",'
    echo "Then restart: npm start"
fi

echo ""
echo -e "${GREEN}🎯 Testing Your AI Features (No Backend Required):${NC}"
echo "=================================================="
echo "While fixing CORS, you can test the AI Features page:"
echo "1. Open: http://localhost:3000/ai-features"
echo "2. Go to 'AI Resume Parser' tab"
echo "3. Upload your test-resume.doc file"
echo "4. Click 'Analyze Resume'"
echo "5. This works without backend!"
echo ""

echo -e "${PURPLE}📋 Next Steps:${NC}"
echo "==============="
echo "1. Choose one solution above to fix CORS"
echo "2. Test login functionality"
echo "3. Test file upload on Profile page"
echo "4. Verify all features working"
echo ""

# Save mock backend for easy access
echo "💾 Mock backend saved as: mock-backend.js"

echo -e "${GREEN}🎉 CORS solutions provided! Choose the best option for your setup.${NC}"
