import React, { useState } from 'react';
import { authAPI } from '../services/api';

const LoginTest = () => {
  const [status, setStatus] = useState('Ready to test');
  const [result, setResult] = useState(null);

  const testLogin = async () => {
    setStatus('Testing...');
    setResult(null);

    try {
      console.log('🧪 Starting login test...');
      
      // Test 1: Direct API call
      const apiResponse = await authAPI.login({
        email: 'demo@example.com',
        password: 'demo123'
      });
      
      console.log('📡 Raw API Response:', apiResponse);
      console.log('📦 Response Data:', apiResponse.data);
      
      if (apiResponse.data && apiResponse.data.success) {
        const { accessToken, user } = apiResponse.data.data;
        
        console.log('🔑 Access Token:', !!accessToken);
        console.log('👤 User Data:', user);
        
        setResult({
          success: true,
          token: accessToken ? `${accessToken.substring(0, 20)}...` : 'No token',
          user: user || 'No user data'
        });
        setStatus('✅ Login test successful!');
        
        // Test localStorage
        localStorage.setItem('test-token', accessToken);
        console.log('💾 Token saved to localStorage');
        
      } else {
        setResult({ success: false, message: apiResponse.data?.message || 'Unknown error' });
        setStatus('❌ API returned failure');
      }
      
    } catch (error) {
      console.error('❌ Login test error:', error);
      setResult({ 
        success: false, 
        error: error.message,
        response: error.response?.data
      });
      setStatus('❌ Login test failed');
    }
  };

  return (
    <div style={{ padding: '20px', border: '1px solid #ccc', margin: '20px', borderRadius: '8px' }}>
      <h3>🧪 Login API Test</h3>
      <p>Status: {status}</p>
      
      <button 
        onClick={testLogin}
        style={{ 
          padding: '10px 20px', 
          backgroundColor: '#007bff', 
          color: 'white', 
          border: 'none', 
          borderRadius: '4px',
          cursor: 'pointer'
        }}
      >
        Test Direct API Call
      </button>
      
      {result && (
        <div style={{ marginTop: '20px', padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
          <pre>{JSON.stringify(result, null, 2)}</pre>
        </div>
      )}
      
      <div style={{ marginTop: '20px', fontSize: '12px', color: '#666' }}>
        <p><strong>Instructions:</strong></p>
        <ul>
          <li>Open browser developer tools (F12)</li>
          <li>Click "Test Direct API Call"</li>
          <li>Check console for detailed logs</li>
          <li>Check result display above</li>
        </ul>
      </div>
    </div>
  );
};

export default LoginTest;
