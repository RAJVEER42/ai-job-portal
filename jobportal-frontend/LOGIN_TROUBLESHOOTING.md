# Login Troubleshooting Guide

## ✅ **ISSUE RESOLVED: January 20, 2026**

### 🔍 **Problem Identified:**
CORS policy error blocking login requests due to port mismatch between frontend and backend CORS configuration.

**Error Message:**
```
Access to XMLHttpRequest at 'http://localhost:8080/api/auth/login' from origin 'http://localhost:3002' 
has been blocked by CORS policy: Response to preflight request doesn't pass access control check: 
No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

### 🔧 **Root Cause:**
- Frontend React app was running on port **3002/3003** instead of expected port **3000**
- Backend CORS configuration only allowed requests from `http://localhost:3000`
- Multiple React development servers were running simultaneously

### 🎯 **Solution Applied:**
1. **Killed all React development servers** running on wrong ports
2. **Restarted React app on port 3000** to match backend CORS configuration  
3. **Verified CORS headers** are properly configured for localhost:3000
4. **Confirmed API connectivity** and response format

### ✅ **Verification Results:**
- ✅ Frontend: Running on port 3000
- ✅ Backend: Running on port 8080  
- ✅ CORS: Properly configured for localhost:3000
- ✅ API: Responding with success and tokens
- ✅ Login: Fully functional

### 🚀 **Current Status: WORKING**

**Test Credentials:**
- Email: `demo@example.com`
- Password: `demo123`

**Steps to Login:**
1. Open: http://localhost:3000/login
2. Click: "Fill Test Credentials" button
3. Click: "Sign In" button  
4. Expected: Redirect to dashboard

---

## 🔍 Network Error Diagnosis

### Current Issue: ~~Network Error during login~~ **RESOLVED**

This guide will help you identify and resolve network-related login issues.

## 1️⃣ Quick Diagnostic Steps

### Check Services Status
```bash
# Check if both services are running
lsof -i :3000  # Frontend
lsof -i :8080  # Backend
```

### Test API Directly
```bash
# Test login API endpoint
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"demo123"}'
```

### Check Browser Console
1. Open Developer Tools (F12)
2. Go to Console tab
3. Look for error messages
4. Go to Network tab
5. Try login and check for failed requests

## 2️⃣ Common Network Error Causes

### A. Backend Service Down
**Symptoms:** 
- `ERR_CONNECTION_REFUSED`
- `Network Error`
- `Failed to fetch`

**Solution:**
```bash
# Check if backend is running
ps aux | grep java
# If not running, start your Spring Boot application
```

### B. Port Conflicts
**Symptoms:**
- Service won't start
- Address already in use

**Solution:**
```bash
# Check what's using the ports
lsof -i :8080
lsof -i :3000
# Kill conflicting processes if needed
kill -9 <PID>
```

### C. CORS Issues
**Symptoms:**
- `Access-Control-Allow-Origin` errors
- `CORS policy` errors

**Solution:**
- Check backend CORS configuration
- Verify Origin headers

### D. Firewall/Security Software
**Symptoms:**
- Intermittent connection failures
- Timeouts

**Solution:**
- Temporarily disable firewall
- Check antivirus/security software

## 3️⃣ Browser-Specific Issues

### Chrome/Chromium
- Clear site data: Settings > Privacy > Clear browsing data
- Disable extensions: Try incognito mode
- Check console for mixed content warnings

### Firefox
- Clear cache: Ctrl+Shift+Del
- Disable tracking protection for localhost
- Check network settings

### Safari
- Clear website data: Develop > Empty Caches
- Check security settings
- Disable content blockers

## 4️⃣ Network Configuration Issues

### Localhost Resolution
```bash
# Check hosts file
cat /etc/hosts | grep localhost

# Should contain:
# 127.0.0.1	localhost
# ::1		localhost
```

### DNS Issues
```bash
# Test localhost resolution
nslookup localhost
ping localhost
```

## 5️⃣ Code-Level Debugging

### Check API Service Configuration
```javascript
// In src/services/api.js
const API_BASE_URL = 'http://localhost:8080/api';

// Verify this matches your backend configuration
```

### Check AuthContext
```javascript
// Look for console logs in browser:
// 🔐 Form submitted with: ...
// 📡 API Response: ...
// 🔑 Token exists: ...
```

## 6️⃣ Advanced Troubleshooting

### Network Monitoring
```bash
# Monitor network connections
netstat -an | grep :8080
netstat -an | grep :3000
```

### Request Debugging
```bash
# Use curl with verbose output
curl -v -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:3000" \
  -d '{"email":"demo@example.com","password":"demo123"}'
```

### Check for Proxy Issues
- Corporate networks may have proxy settings
- Check browser proxy configuration
- Try direct connection

## 7️⃣ Environment-Specific Fixes

### macOS
```bash
# Reset network settings
sudo dscacheutil -flushcache
sudo killall -HUP mDNSResponder
```

### Development Environment
```bash
# Clear npm cache
npm cache clean --force

# Restart development server
npm start
```

## 8️⃣ Error Message Mapping

| Error Message | Likely Cause | Solution |
|---------------|--------------|----------|
| `ERR_CONNECTION_REFUSED` | Backend down | Start backend service |
| `ERR_NETWORK_CHANGED` | Network config | Restart browser |
| `ERR_CERT_AUTHORITY_INVALID` | SSL/TLS issue | Use HTTP for localhost |
| `Failed to fetch` | CORS/Network | Check CORS & connectivity |
| `TypeError: NetworkError` | Request blocked | Check firewall/extensions |

## 9️⃣ Quick Fixes Checklist

- [ ] Backend service is running on port 8080
- [ ] Frontend service is running on port 3000
- [ ] Browser console shows no CORS errors
- [ ] No browser extensions interfering
- [ ] Cache cleared (hard refresh: Ctrl+F5)
- [ ] Tried in incognito/private mode
- [ ] Firewall/antivirus not blocking localhost
- [ ] No proxy settings interfering

## 🔟 Emergency Bypass Method

If all else fails, you can test the login directly:

```bash
# Get a token manually
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"demo123"}' | \
  grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

# Store in browser localStorage manually
# Open browser console and run:
# localStorage.setItem('accessToken', 'YOUR_TOKEN_HERE');
# localStorage.setItem('user', '{"email":"demo@example.com","fullName":"Demo User"}');
# window.location.href = '/dashboard';
```

## 📞 Getting Help

If you're still experiencing issues:

1. **Capture the exact error message** from browser console
2. **Run the diagnostic script**: `./comprehensive-debug.sh`
3. **Check network tab** for HTTP status codes
4. **Note your environment**: Browser, OS version, network setup
5. **Share the console output** showing the error

---

**Last Updated:** January 20, 2026
**Status:** Active troubleshooting for network errors