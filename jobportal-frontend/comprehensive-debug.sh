#!/bin/bash

echo "🔍 COMPREHENSIVE LOGIN DEBUG TOOL"
echo "=================================="
echo ""

# Function to check if a service is running
check_service() {
    local port=$1
    local name=$2
    
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        echo "✅ $name is running on port $port"
        return 0
    else
        echo "❌ $name is NOT running on port $port"
        return 1
    fi
}

# Function to test API endpoint
test_api() {
    local url=$1
    local method=${2:-GET}
    local data=${3:-"{}"}
    
    echo "🧪 Testing: $method $url"
    
    if [ "$method" = "POST" ]; then
        response=$(curl -s -w "%{http_code}" -X POST "$url" \
            -H "Content-Type: application/json" \
            -H "Origin: http://localhost:3000" \
            -d "$data")
        
        http_code="${response: -3}"
        body="${response%???}"
        
        if [ "$http_code" -eq 200 ]; then
            echo "✅ API call successful (HTTP $http_code)"
            if echo "$body" | grep -q '"success":true'; then
                echo "✅ API response indicates success"
            else
                echo "⚠️  API response indicates failure"
                echo "Response: $body"
            fi
        else
            echo "❌ API call failed (HTTP $http_code)"
            echo "Response: $body"
        fi
    else
        response=$(curl -s -w "%{http_code}" "$url")
        http_code="${response: -3}"
        
        if [ "$http_code" -eq 200 ]; then
            echo "✅ $url is accessible (HTTP $http_code)"
        else
            echo "❌ $url returned HTTP $http_code"
        fi
    fi
    echo ""
}

echo "1️⃣ CHECKING SERVICES"
echo "==================="
check_service 3000 "Frontend (React)"
check_service 8080 "Backend (Java)"
echo ""

echo "2️⃣ TESTING FRONTEND PAGES"
echo "========================="
test_api "http://localhost:3000"
test_api "http://localhost:3000/login"
echo ""

echo "3️⃣ TESTING BACKEND API"
echo "====================="
test_api "http://localhost:8080/api/auth/login" "POST" '{"email":"demo@example.com","password":"demo123"}'
test_api "http://localhost:8080/api/jobs"
echo ""

echo "4️⃣ TESTING CORS"
echo "==============="
echo "🧪 Testing CORS headers..."
cors_response=$(curl -s -H "Origin: http://localhost:3000" \
    -H "Access-Control-Request-Method: POST" \
    -H "Access-Control-Request-Headers: content-type" \
    -X OPTIONS "http://localhost:8080/api/auth/login" -I)

if echo "$cors_response" | grep -q "Access-Control-Allow-Origin"; then
    echo "✅ CORS is properly configured"
else
    echo "⚠️  CORS might not be configured correctly"
    echo "Response headers:"
    echo "$cors_response"
fi
echo ""

echo "5️⃣ TESTING NETWORK CONNECTIVITY"
echo "==============================="
echo "🌐 Testing localhost resolution..."
if ping -c 1 localhost >/dev/null 2>&1; then
    echo "✅ localhost is reachable"
else
    echo "❌ localhost is not reachable"
fi

echo "🌐 Testing 127.0.0.1 resolution..."
if ping -c 1 127.0.0.1 >/dev/null 2>&1; then
    echo "✅ 127.0.0.1 is reachable"
else
    echo "❌ 127.0.0.1 is not reachable"
fi
echo ""

echo "6️⃣ DEBUGGING INSTRUCTIONS"
echo "========================="
echo "If login is still failing, please:"
echo ""
echo "1. Open http://localhost:3000/login in Chrome/Firefox"
echo "2. Open Developer Tools (F12)"
echo "3. Go to Console tab"
echo "4. Click 'Fill Test Credentials' button"
echo "5. Click 'Sign In'"
echo "6. Look for console logs starting with 🔐, 📡, 🔑, etc."
echo "7. Check Network tab for any failed requests"
echo "8. Report any errors you see"
echo ""
echo "Common issues to check:"
echo "- Browser blocking mixed content (HTTP/HTTPS)"
echo "- Ad blockers or security extensions interfering"
echo "- Local firewall blocking requests"
echo "- Browser cache issues (try hard refresh: Ctrl+F5)"
echo ""
echo "✨ Debug complete! Check the results above."
