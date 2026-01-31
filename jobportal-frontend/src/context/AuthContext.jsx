// src/context/AuthContext.jsx
import React, { createContext, useState, useContext, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    const savedUser = localStorage.getItem('user');
    
    if (token && savedUser) {
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      console.log('🔐 Login attempt for:', email);
      const response = await authAPI.login({ email, password });
      console.log('📡 API Response:', response);
      console.log('📦 Response Data:', response.data);
      
      if (response.data && response.data.success) {
        const { accessToken, user: userData } = response.data.data;
        console.log('🔑 Token exists:', !!accessToken);
        console.log('👤 User data:', userData);
        
        if (accessToken && userData) {
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('user', JSON.stringify(userData));
          setUser(userData);
          console.log('✅ Login successful, user set');
          return { success: true };
        } else {
          console.log('❌ Missing token or user data');
          return { success: false, message: 'Invalid response: missing token or user data' };
        }
      } else {
        console.log('❌ API response indicates failure');
        return { success: false, message: response.data?.message || 'Login failed' };
      }
    } catch (error) {
      console.error('❌ Login error:', error);
      console.error('Error details:', error.response?.data);
      return { 
        success: false, 
        message: error.response?.data?.message || error.message || 'Network error occurred' 
      };
    }
  };

  const register = async (data) => {
    try {
      console.log('📝 Registration attempt for:', data.email);
      const response = await authAPI.register(data);
      
      if (response.data && response.data.success) {
        console.log('✅ Registration successful:', response.data.data);
        // Enhanced success message mentioning email notification
        return { 
          success: true, 
          message: '🎉 Registration successful! Welcome email sent to your inbox. Please check your email and login.',
          user: response.data.data
        };
      }
      return { success: false, message: response.data?.message || 'Registration failed' };
    } catch (error) {
      console.error('❌ Registration error:', error);
      return { 
        success: false, 
        message: error.response?.data?.message || 'Registration failed' 
      };
    }
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};