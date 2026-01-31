#!/bin/bash

echo "🔍 API Connection Diagnostic Tool"
echo "================================"
echo ""

# Test basic connectivity
echo "1️⃣ Testing Basic API Connectivity..."
echo "Frontend Port: 3001"
echo "Backend Port: 8080"
echo ""

# Check if backend is running
echo "📡 Checking if backend is running on port 8080..."
if curl -s http://localhost:8080/api/jobs > /dev/null; then
    echo "✅ Backend is running and responsive"
else
    echo "❌ Backend is not responding"
    echo "💡 Please make sure to run: node mock-backend.js"
    exit 1
fi

# Test CORS with frontend origin
echo ""
echo "🌐 Testing CORS for frontend port 3001..."
CORS_RESPONSE=$(curl -s -H "Origin: http://localhost:3001" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -X OPTIONS \
  http://localhost:8080/api/jobs \
  -D -)

if echo "$CORS_RESPONSE" | grep -q "access-control-allow-origin"; then
    echo "✅ CORS headers found"
    echo "$CORS_RESPONSE" | grep "access-control"
else
    echo "❌ CORS headers missing"
    echo "Backend might not be configured for port 3001"
fi

# Test actual API call
echo ""
echo "📋 Testing Jobs API endpoint..."
API_RESPONSE=$(curl -s -H "Origin: http://localhost:3001" http://localhost:8080/api/jobs)

if echo "$API_RESPONSE" | grep -q '"success":true'; then
    JOBS_COUNT=$(echo "$API_RESPONSE" | grep -o '"id":[0-9]*' | wc -l)
    echo "✅ Jobs API working - Found $JOBS_COUNT jobs"
else
    echo "❌ Jobs API failed"
    echo "Response: $API_RESPONSE"
fi

echo ""
echo "🔧 If you're still getting errors:"
echo "1. Open browser console (F12) and check for error messages"
echo "2. Make sure frontend is running on http://localhost:3001"
echo "3. Make sure backend is running on http://localhost:8080"
echo "4. Check for any firewall or security software blocking connections"
echo ""
