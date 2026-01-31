import React, { useState, useEffect } from 'react';
import { monitoringAPI, apiUtils } from '../services/api';
import {
  Activity,
  Server,
  Database,
  Zap,
  AlertCircle,
  CheckCircle,
  Clock,
  TrendingUp,
  Shield,
  Monitor
} from 'lucide-react';

const SystemMonitoring = () => {
  const [healthStatus, setHealthStatus] = useState(null);
  const [metrics, setMetrics] = useState(null);
  const [performanceData, setPerformanceData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchMonitoringData = async () => {
      try {
        setLoading(true);
        const healthData = await apiUtils.checkSystemHealth();
        setHealthStatus(healthData);

        // Fetch performance metrics
        const performanceResponse = await apiUtils.measurePerformance(() => 
          monitoringAPI.getApplicationMetrics()
        );
        
        if (performanceResponse.success) {
          setMetrics(performanceResponse.response.data);
          setPerformanceData(prev => [...prev.slice(-9), {
            timestamp: Date.now(),
            responseTime: performanceResponse.duration,
            cached: performanceResponse.cached
          }]);
        }
      } catch (err) {
        setError(apiUtils.formatError(err));
      } finally {
        setLoading(false);
      }
    };

    fetchMonitoringData();
    const interval = setInterval(fetchMonitoringData, 30000); // Update every 30 seconds

    return () => clearInterval(interval);
  }, []);

  const getStatusColor = (status) => {
    switch (status) {
      case 'UP': return 'text-green-600 bg-green-100';
      case 'DOWN': return 'text-red-600 bg-red-100';
      case 'DEGRADED': return 'text-yellow-600 bg-yellow-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'UP': return <CheckCircle className="w-4 h-4" />;
      case 'DOWN': return <AlertCircle className="w-4 h-4" />;
      case 'DEGRADED': return <Clock className="w-4 h-4" />;
      default: return <Monitor className="w-4 h-4" />;
    }
  };

  if (loading && !healthStatus) {
    return (
      <div className="p-6 bg-white rounded-lg shadow">
        <div className="flex items-center gap-3 mb-4">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
          <h2 className="text-lg font-semibold">Loading System Status...</h2>
        </div>
      </div>
    );
  }

  if (error && !healthStatus) {
    return (
      <div className="p-6 bg-white rounded-lg shadow">
        <div className="flex items-center gap-3 mb-4">
          <AlertCircle className="w-6 h-6 text-red-600" />
          <h2 className="text-lg font-semibold text-red-600">Monitoring Unavailable</h2>
        </div>
        <p className="text-gray-600">Unable to fetch system status: {error.message}</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* System Health Overview */}
      <div className="p-6 bg-white rounded-lg shadow">
        <div className="flex items-center gap-3 mb-4">
          <Activity className="w-6 h-6 text-blue-600" />
          <h2 className="text-lg font-semibold">System Health</h2>
          <div className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(healthStatus?.status)}`}>
            {getStatusIcon(healthStatus?.status)}
            <span className="ml-1">{healthStatus?.status || 'UNKNOWN'}</span>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {Object.entries(healthStatus?.components || {}).map(([name, component]) => (
            <div key={name} className="p-4 border rounded-lg">
              <div className="flex items-center justify-between mb-2">
                <span className="font-medium capitalize">{name.replace(/([A-Z])/g, ' $1')}</span>
                <div className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(component.status)}`}>
                  {getStatusIcon(component.status)}
                  <span className="ml-1">{component.status}</span>
                </div>
              </div>
              {component.details && (
                <div className="text-xs text-gray-600">
                  {Object.entries(component.details).map(([key, value]) => (
                    <div key={key} className="flex justify-between">
                      <span>{key}:</span>
                      <span>{String(value)}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Performance Metrics */}
      <div className="p-6 bg-white rounded-lg shadow">
        <div className="flex items-center gap-3 mb-4">
          <TrendingUp className="w-6 h-6 text-green-600" />
          <h2 className="text-lg font-semibold">Performance Metrics</h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div className="p-4 bg-blue-50 rounded-lg">
            <div className="flex items-center gap-2 mb-2">
              <Zap className="w-4 h-4 text-blue-600" />
              <span className="text-sm font-medium">Avg Response Time</span>
            </div>
            <p className="text-2xl font-bold text-blue-600">
              {performanceData.length > 0 
                ? Math.round(performanceData.reduce((sum, d) => sum + d.responseTime, 0) / performanceData.length)
                : 0}ms
            </p>
          </div>

          <div className="p-4 bg-green-50 rounded-lg">
            <div className="flex items-center gap-2 mb-2">
              <Database className="w-4 h-4 text-green-600" />
              <span className="text-sm font-medium">Cache Hit Rate</span>
            </div>
            <p className="text-2xl font-bold text-green-600">
              {performanceData.length > 0 
                ? Math.round((performanceData.filter(d => d.cached).length / performanceData.length) * 100)
                : 0}%
            </p>
          </div>

          <div className="p-4 bg-purple-50 rounded-lg">
            <div className="flex items-center gap-2 mb-2">
              <Server className="w-4 h-4 text-purple-600" />
              <span className="text-sm font-medium">Active Users</span>
            </div>
            <p className="text-2xl font-bold text-purple-600">
              {metrics?.activeUsers || 0}
            </p>
          </div>

          <div className="p-4 bg-orange-50 rounded-lg">
            <div className="flex items-center gap-2 mb-2">
              <Shield className="w-4 h-4 text-orange-600" />
              <span className="text-sm font-medium">Security Events</span>
            </div>
            <p className="text-2xl font-bold text-orange-600">
              {metrics?.securityEvents || 0}
            </p>
          </div>
        </div>

        {/* Performance Chart */}
        {performanceData.length > 0 && (
          <div className="mt-6">
            <h3 className="font-medium mb-3">Response Time Trend (Last 10 requests)</h3>
            <div className="h-32 flex items-end gap-1">
              {performanceData.map((data, index) => {
                const height = Math.max((data.responseTime / Math.max(...performanceData.map(d => d.responseTime))) * 100, 5);
                return (
                  <div
                    key={index}
                    className={`flex-1 rounded-t transition-all duration-300 ${
                      data.cached ? 'bg-green-400' : 'bg-blue-400'
                    }`}
                    style={{ height: `${height}%` }}
                    title={`${data.responseTime}ms ${data.cached ? '(cached)' : '(fresh)'}`}
                  />
                );
              })}
            </div>
            <div className="flex justify-between text-xs text-gray-500 mt-2">
              <span>Oldest</span>
              <span>Latest</span>
            </div>
            <div className="flex items-center gap-4 mt-2 text-xs">
              <div className="flex items-center gap-1">
                <div className="w-3 h-3 bg-green-400 rounded"></div>
                <span>Cached</span>
              </div>
              <div className="flex items-center gap-1">
                <div className="w-3 h-3 bg-blue-400 rounded"></div>
                <span>Fresh</span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* System Information */}
      {metrics && (
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center gap-3 mb-4">
            <Server className="w-6 h-6 text-gray-600" />
            <h2 className="text-lg font-semibold">System Information</h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <h3 className="font-medium mb-2">Application</h3>
              <div className="space-y-1 text-sm text-gray-600">
                <div className="flex justify-between">
                  <span>Version:</span>
                  <span>{process.env.REACT_APP_VERSION || '1.0.0'}</span>
                </div>
                <div className="flex justify-between">
                  <span>Build:</span>
                  <span>{process.env.NODE_ENV}</span>
                </div>
                <div className="flex justify-between">
                  <span>Uptime:</span>
                  <span>{metrics.uptime || 'N/A'}</span>
                </div>
              </div>
            </div>

            <div>
              <h3 className="font-medium mb-2">Performance</h3>
              <div className="space-y-1 text-sm text-gray-600">
                <div className="flex justify-between">
                  <span>Memory Usage:</span>
                  <span>{metrics.memoryUsage || 'N/A'}</span>
                </div>
                <div className="flex justify-between">
                  <span>CPU Usage:</span>
                  <span>{metrics.cpuUsage || 'N/A'}</span>
                </div>
                <div className="flex justify-between">
                  <span>Requests/min:</span>
                  <span>{metrics.requestsPerMinute || 'N/A'}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SystemMonitoring;
