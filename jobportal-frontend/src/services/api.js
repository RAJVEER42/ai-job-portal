import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
const REQUEST_TIMEOUT = process.env.REACT_APP_REQUEST_TIMEOUT || 15000;
const MAX_RETRIES = 3;
const RETRY_DELAY = 1000;

// Rate limiting and circuit breaker support
class RequestManager {
  constructor() {
    this.requestCounts = new Map();
    this.rateLimitResets = new Map();
    this.circuitBreakerStates = new Map();
  }

  isRateLimited(endpoint) {
    const now = Date.now();
    const resetTime = this.rateLimitResets.get(endpoint);
    
    if (resetTime && now < resetTime) {
      return true;
    }
    
    return false;
  }

  setRateLimit(endpoint, resetTime) {
    this.rateLimitResets.set(endpoint, resetTime);
  }

  isCircuitOpen(endpoint) {
    const state = this.circuitBreakerStates.get(endpoint);
    return state?.isOpen && Date.now() < state.resetTime;
  }

  openCircuit(endpoint) {
    this.circuitBreakerStates.set(endpoint, {
      isOpen: true,
      resetTime: Date.now() + 30000 // 30 seconds
    });
  }

  closeCircuit(endpoint) {
    this.circuitBreakerStates.delete(endpoint);
  }
}

const requestManager = new RequestManager();

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: REQUEST_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
    'X-Client-Version': process.env.REACT_APP_VERSION || '1.0.0',
    'X-Client-Platform': 'web'
  },
});

// Request ID generation for correlation
const generateRequestId = () => {
  return 'req_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now();
};

// Enhanced request interceptor with enterprise features
api.interceptors.request.use(
  (config) => {
    // Add correlation ID for request tracking
    const requestId = generateRequestId();
    config.headers['X-Request-ID'] = requestId;
    config.metadata = { requestId, startTime: performance.now() };

    // Check circuit breaker
    const endpoint = config.url;
    if (requestManager.isCircuitOpen(endpoint)) {
      return Promise.reject(new Error('Circuit breaker is open for this endpoint'));
    }

    // Check rate limiting
    if (requestManager.isRateLimited(endpoint)) {
      return Promise.reject(new Error('Rate limit exceeded. Please try again later.'));
    }

    // Add authentication token
    const token = localStorage.getItem('accessToken');
    if (token && token !== 'undefined' && token !== 'null') {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Add CSRF protection if available
    const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
    if (csrfToken) {
      config.headers['X-CSRF-TOKEN'] = csrfToken;
    }

    console.log(`🚀 [${requestId}] ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('🚨 Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Enhanced response interceptor with enterprise error handling
api.interceptors.response.use(
  (response) => {
    const config = response.config;
    const requestId = config.metadata?.requestId;
    const duration = config.metadata?.startTime ? 
      Math.round(performance.now() - config.metadata.startTime) : 0;

    // Close circuit breaker on success
    requestManager.closeCircuit(config.url);

    // Log successful requests
    console.log(`✅ [${requestId}] ${config.method?.toUpperCase()} ${config.url} (${duration}ms)`);

    // Add performance metadata to response
    response.meta = {
      requestId,
      duration,
      cached: response.headers['x-cache-status'] === 'HIT',
      timestamp: new Date().toISOString()
    };

    return response;
  },
  async (error) => {
    const config = error.config;
    const requestId = config?.metadata?.requestId || 'unknown';
    const duration = config?.metadata?.startTime ? 
      Math.round(performance.now() - config.metadata.startTime) : 0;

    console.error(`❌ [${requestId}] ${config?.method?.toUpperCase()} ${config?.url} (${duration}ms):`, error);

    // Handle rate limiting (429)
    if (error.response?.status === 429) {
      const resetTime = error.response.headers['x-ratelimit-reset'];
      if (resetTime) {
        requestManager.setRateLimit(config.url, parseInt(resetTime) * 1000);
      }
      
      // Show user-friendly rate limit message
      const retryAfter = error.response.headers['retry-after'] || '60';
      error.userMessage = `Too many requests. Please try again in ${retryAfter} seconds.`;
      return Promise.reject(error);
    }

    // Handle server errors (5xx) with circuit breaker
    if (error.response?.status >= 500) {
      requestManager.openCircuit(config?.url);
      error.userMessage = 'Service temporarily unavailable. Please try again later.';
    }

    // Handle authentication errors (401)
    if (error.response?.status === 401) {
      console.log('🔐 Authentication failed, clearing tokens');
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      
      // Try to refresh token before redirecting
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken && !config._retry) {
        try {
          const response = await authAPI.refresh(refreshToken);
          const newToken = response.data.accessToken;
          localStorage.setItem('accessToken', newToken);
          
          // Retry original request with new token
          config.headers.Authorization = `Bearer ${newToken}`;
          config._retry = true;
          return api(config);
        } catch (refreshError) {
          console.error('🔄 Token refresh failed:', refreshError);
          window.location.href = '/login';
        }
      } else {
        window.location.href = '/login';
      }
    }

    // Handle network errors with retry logic
    if (!error.response && config && !config._retry) {
      for (let i = 1; i <= MAX_RETRIES; i++) {
        try {
          console.log(`🔄 [${requestId}] Retry attempt ${i}/${MAX_RETRIES}`);
          await new Promise(resolve => setTimeout(resolve, RETRY_DELAY * i));
          
          config._retry = true;
          const response = await api(config);
          console.log(`✅ [${requestId}] Retry successful on attempt ${i}`);
          return response;
        } catch (retryError) {
          if (i === MAX_RETRIES) {
            console.error(`❌ [${requestId}] All retry attempts failed`);
            error.userMessage = 'Network error. Please check your connection and try again.';
            break;
          }
        }
      }
    }

    // Add user-friendly error message if not already set
    if (!error.userMessage) {
      switch (error.response?.status) {
        case 400:
          error.userMessage = 'Invalid request. Please check your input.';
          break;
        case 403:
          error.userMessage = 'Access denied. You don\'t have permission for this action.';
          break;
        case 404:
          error.userMessage = 'Resource not found.';
          break;
        case 409:
          error.userMessage = 'Conflict. The resource already exists or is in use.';
          break;
        case 422:
          error.userMessage = 'Validation failed. Please check your input.';
          break;
        default:
          error.userMessage = error.response?.data?.message || 'An unexpected error occurred.';
      }
    }

    return Promise.reject(error);
  }
);

export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
};

export const jobAPI = {
  getAllJobs: (page = 0, size = 10) => api.get(`/jobs?page=${page}&size=${size}`),
  getJobById: (id) => api.get(`/jobs/${id}`),
  createJob: (data) => api.post('/jobs', data),
  updateJob: (id, data) => api.put(`/jobs/${id}`, data),
  deleteJob: (id) => api.delete(`/jobs/${id}`),
  searchJobs: (keyword, filters = {}) => {
    const params = new URLSearchParams();
    if (keyword) params.append('keyword', keyword);
    if (filters.location) params.append('location', filters.location);
    if (filters.minSalary) params.append('minSalary', filters.minSalary);
    if (filters.maxSalary) params.append('maxSalary', filters.maxSalary);
    if (filters.experienceRequired) params.append('experienceRequired', filters.experienceRequired);
    return api.get(`/jobs/search?${params.toString()}`);
  },
  applyToJob: (jobId, applicationData) => api.post(`/jobs/${jobId}/apply`, applicationData),
  getMyApplications: () => api.get('/applications/my'),
  getJobApplications: (jobId) => api.get(`/jobs/${jobId}/applications`),
};

export const applicationAPI = {
  applyToJob: (jobId, applicationData) => api.post(`/jobs/${jobId}/apply`, applicationData),
  updateStatus: (applicationId, status, notes = '') => 
    api.put(`/applications/${applicationId}/status`, { status, notes }),
  getApplication: (id) => api.get(`/applications/${id}`),
  getMyApplications: () => api.get('/applications/my'),
  getJobApplications: (jobId) => api.get(`/jobs/${jobId}/applications`),
  withdrawApplication: (id) => api.delete(`/applications/${id}`),
};

export const adminAPI = {
  getCacheStats: () => api.get('/admin/cache/stats'),
  clearCache: (cacheName) => api.delete(`/admin/cache/${cacheName}`),
  clearAllCaches: () => api.delete('/admin/cache/all'),
  getCacheNames: () => api.get('/admin/cache/names'),
  getSystemHealth: () => api.get('/admin/health'),
  getMetrics: () => api.get('/admin/metrics'),
  getSystemInfo: () => api.get('/admin/info'),
  getRateLimitStats: () => api.get('/admin/rate-limit/stats'),
  getCircuitBreakerStatus: () => api.get('/admin/circuit-breaker/status'),
  getLogLevel: () => api.get('/admin/logging'),
  setLogLevel: (level, logger = '') => api.post('/admin/logging', { level, logger }),
};

export const monitoringAPI = {
  getHealthCheck: () => api.get('/actuator/health'),
  getMetrics: () => api.get('/actuator/metrics'),
  getInfo: () => api.get('/actuator/info'),
  getPrometheusMetrics: () => api.get('/actuator/prometheus'),
  getApplicationMetrics: () => api.get('/admin/metrics/application'),
  getPerformanceMetrics: () => api.get('/admin/metrics/performance'),
};

export const securityAPI = {
  getCurrentSession: () => api.get('/auth/session'),
  invalidateSession: () => api.post('/auth/logout'),
  getSecurityEvents: () => api.get('/admin/security/events'),
  reportSecurityIncident: (incident) => api.post('/admin/security/incident', incident),
};

export const emailAPI = {
  sendWelcomeEmail: (userId) => api.post(`/email/welcome/${userId}`),
  sendTestEmail: (email) => api.post('/email/test', { email }),
  getEmailStats: () => api.get('/admin/email/stats'),
};

export const fileAPI = {
  uploadResume: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
};

// Enterprise utility functions
export const apiUtils = {
  // Check if a request was served from cache
  isCachedResponse: (response) => {
    return response?.meta?.cached || response?.headers?.['x-cache-status'] === 'HIT';
  },

  // Get request performance metrics
  getPerformanceMetrics: (response) => {
    return {
      duration: response?.meta?.duration || 0,
      cached: response?.meta?.cached || false,
      requestId: response?.meta?.requestId || 'unknown',
      timestamp: response?.meta?.timestamp || new Date().toISOString()
    };
  },

  // Check system health with comprehensive status
  async checkSystemHealth() {
    try {
      const [health, metrics] = await Promise.all([
        monitoringAPI.getHealthCheck(),
        monitoringAPI.getApplicationMetrics()
      ]);

      return {
        status: health.data?.status || 'DOWN',
        components: health.data?.components || {},
        metrics: metrics.data || {},
        timestamp: new Date().toISOString()
      };
    } catch (error) {
      return {
        status: 'DOWN',
        error: error.message,
        timestamp: new Date().toISOString()
      };
    }
  },

  // Format error for user display
  formatError: (error) => {
    return {
      message: error.userMessage || error.message || 'An unexpected error occurred',
      details: error.response?.data?.details || null,
      code: error.response?.status || 'UNKNOWN',
      requestId: error.config?.metadata?.requestId || 'unknown',
      timestamp: new Date().toISOString()
    };
  },

  // Check rate limit status
  getRateLimitInfo: (response) => {
    const headers = response?.headers || {};
    return {
      limit: parseInt(headers['x-ratelimit-limit']) || null,
      remaining: parseInt(headers['x-ratelimit-remaining']) || null,
      reset: parseInt(headers['x-ratelimit-reset']) || null,
      retryAfter: parseInt(headers['retry-after']) || null
    };
  },

  // Performance monitoring helper
  measurePerformance: async (apiCall) => {
    const startTime = performance.now();
    try {
      const response = await apiCall();
      const endTime = performance.now();
      const duration = Math.round(endTime - startTime);
      
      return {
        success: true,
        response,
        duration,
        cached: apiUtils.isCachedResponse(response)
      };
    } catch (error) {
      const endTime = performance.now();
      const duration = Math.round(endTime - startTime);
      
      return {
        success: false,
        error: apiUtils.formatError(error),
        duration
      };
    }
  }
};

// Export request manager for external access
export { requestManager };

export default api;