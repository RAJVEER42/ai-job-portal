#!/bin/bash

# Test login functionality
echo "🔐 Testing Frontend Login Functionality"
echo "======================================="

echo "🌐 Opening login page..."
# Check if login page loads
curl -s http://localhost:3000/login > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Login page accessible"
else
    echo "❌ Login page not accessible"
    exit 1
fi

echo ""
echo "🔑 Testing API login directly..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"demo@example.com","password":"demo123"}')

if echo "$RESPONSE" | grep -q '"success":true'; then
    echo "✅ API login works"
    TOKEN=$(echo "$RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
    echo "✅ Token extracted: ${TOKEN:0:20}..."
else
    echo "❌ API login failed"
    echo "Response: $RESPONSE"
    exit 1
fi

echo ""
echo "🎯 Test Instructions:"
echo "1. Open http://localhost:3000/login in browser"
echo "2. Use credentials: demo@example.com / demo123"
echo "3. Check browser console for debug logs"
echo "4. Look for any error messages in the UI"

echo ""
echo "🔍 Troubleshooting:"
echo "- Check if both frontend (3000) and backend (8080) are running"
echo "- Open browser developer tools before login attempt"
echo "- Look for CORS or network errors in console"
echo "- Verify the response format matches expectations"
