import React, { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';
import SystemMonitoring from '../components/SystemMonitoring';
import { 
  BarChart3, 
  RefreshCw, 
  Trash2, 
  CheckCircle, 
  XCircle, 
  Clock,
  Database,
  Zap,
  Mail,
  Activity,
  Shield
} from 'lucide-react';

const AdminDashboard = () => {
  const [cacheStats, setCacheStats] = useState({});
  const [cacheNames, setCacheNames] = useState([]);
  const [emailStats, setEmailStats] = useState({});
  const [activeTab, setActiveTab] = useState('overview');
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [clearingCache, setClearingCache] = useState({});

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [statsResponse, namesResponse, emailResponse] = await Promise.all([
        adminAPI.getCacheStats().catch(() => ({ data: {} })),
        adminAPI.getCacheNames().catch(() => ({ data: [] })),
        adminAPI.getEmailStats().catch(() => ({ data: {} }))
      ]);

      setCacheStats(statsResponse.data || {});
      setCacheNames(namesResponse.data || []);
      setEmailStats(emailResponse.data || {});
    } catch (error) {
      console.error('Error fetching admin data:', error);
    } finally {
      setLoading(false);
    }
  };

  const refreshStats = async () => {
    setRefreshing(true);
    await fetchDashboardData();
    setRefreshing(false);
  };

  const clearCache = async (cacheName) => {
    setClearingCache({ ...clearingCache, [cacheName]: true });
    try {
      await adminAPI.clearCache(cacheName);
      await fetchDashboardData();
    } catch (error) {
      console.error('Error clearing cache:', error);
    } finally {
      setClearingCache({ ...clearingCache, [cacheName]: false });
    }
  };

  const clearAllCaches = async () => {
    setClearingCache({ all: true });
    try {
      await adminAPI.clearAllCaches();
      await fetchDashboardData();
    } catch (error) {
      console.error('Error clearing all caches:', error);
    } finally {
      setClearingCache({ all: false });
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  const calculateHitRate = (hits, misses) => {
    const total = hits + misses;
    return total > 0 ? ((hits / total) * 100).toFixed(1) : 0;
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <div className="flex justify-between items-center">
            <h1 className="text-3xl font-bold text-gray-900">Admin Dashboard</h1>
            <button
              onClick={refreshStats}
              disabled={refreshing}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 transition-colors"
            >
              <RefreshCw className={`w-4 h-4 ${refreshing ? 'animate-spin' : ''}`} />
              Refresh
            </button>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="mb-8">
          <nav className="flex space-x-1 bg-gray-100 rounded-lg p-1">
            {[
              { id: 'overview', label: 'Overview', icon: BarChart3 },
              { id: 'cache', label: 'Cache Management', icon: Database },
              { id: 'monitoring', label: 'System Monitoring', icon: Activity },
              { id: 'security', label: 'Security', icon: Shield }
            ].map((tab) => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center gap-2 px-4 py-2 rounded-md font-medium transition-colors ${
                    activeTab === tab.id
                      ? 'bg-white text-blue-600 shadow-sm'
                      : 'text-gray-600 hover:text-gray-900'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {tab.label}
                </button>
              );
            })}
          </nav>
        </div>

        {/* Overview Tab */}
        {activeTab === 'overview' && (
          <div>
            {/* Performance Overview */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center gap-3">
                  <Database className="w-8 h-8 text-blue-600" />
                  <div>
                    <p className="text-sm text-gray-600">Total Caches</p>
                    <p className="text-2xl font-bold text-gray-900">{cacheNames.length}</p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center gap-3">
                  <Zap className="w-8 h-8 text-green-600" />
                  <div>
                    <p className="text-sm text-gray-600">Avg Hit Rate</p>
                    <p className="text-2xl font-bold text-gray-900">
                      {Object.values(cacheStats).length > 0 
                        ? (Object.values(cacheStats).reduce((acc, cache) => 
                            acc + parseFloat(calculateHitRate(cache.hitCount || 0, cache.missCount || 0)), 0
                          ) / Object.values(cacheStats).length).toFixed(1)
                        : 0
                      }%
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center gap-3">
                  <Clock className="w-8 h-8 text-yellow-600" />
                  <div>
                    <p className="text-sm text-gray-600">Cache Entries</p>
                    <p className="text-2xl font-bold text-gray-900">
                      {Object.values(cacheStats).reduce((acc, cache) => acc + (cache.size || 0), 0)}
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center gap-3">
                  <Mail className="w-8 h-8 text-purple-600" />
                  <div>
                    <p className="text-sm text-gray-600">Emails Sent</p>
                    <p className="text-2xl font-bold text-gray-900">{emailStats.totalSent || 0}</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Email Statistics */}
            <div className="bg-white rounded-lg shadow mb-8">
              <div className="px-6 py-4 border-b border-gray-200">
                <h2 className="text-lg font-semibold text-gray-900">Email Statistics</h2>
              </div>
              <div className="p-6">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div className="text-center">
                    <div className="flex justify-center mb-2">
                      <CheckCircle className="w-12 h-12 text-green-500" />
                    </div>
                    <p className="text-2xl font-bold text-gray-900">{emailStats.sent || 0}</p>
                    <p className="text-sm text-gray-600">Emails Sent</p>
                  </div>
                  
                  <div className="text-center">
                    <div className="flex justify-center mb-2">
                      <XCircle className="w-12 h-12 text-red-500" />
                    </div>
                    <p className="text-2xl font-bold text-gray-900">{emailStats.failed || 0}</p>
                    <p className="text-sm text-gray-600">Failed</p>
                  </div>
                  
                  <div className="text-center">
                    <div className="flex justify-center mb-2">
                      <Clock className="w-12 h-12 text-yellow-500" />
                    </div>
                    <p className="text-2xl font-bold text-gray-900">{emailStats.pending || 0}</p>
                    <p className="text-sm text-gray-600">Pending</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Cache Management Tab */}
        {activeTab === 'cache' && (
          <div className="bg-white rounded-lg shadow mb-8">
            <div className="px-6 py-4 border-b border-gray-200">
              <div className="flex justify-between items-center">
                <h2 className="text-lg font-semibold text-gray-900">Cache Management</h2>
                <button
                  onClick={clearAllCaches}
                  disabled={clearingCache.all}
                  className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:bg-gray-400 transition-colors"
                >
                  <Trash2 className="w-4 h-4" />
                  {clearingCache.all ? 'Clearing...' : 'Clear All Caches'}
                </button>
              </div>
            </div>

            <div className="p-6">
              <div className="grid gap-4">
                {cacheNames.map((cacheName) => {
                  const stats = cacheStats[cacheName] || {};
                  return (
                    <div key={cacheName} className="border border-gray-200 rounded-lg p-4">
                      <div className="flex justify-between items-start mb-4">
                        <div>
                          <h3 className="font-semibold text-gray-900 capitalize">
                            {cacheName.replace(/([A-Z])/g, ' $1')}
                          </h3>
                          <div className="flex gap-6 mt-2 text-sm text-gray-600">
                            <span>Size: {stats.size || 0}</span>
                            <span>Hits: {stats.hitCount || 0}</span>
                            <span>Misses: {stats.missCount || 0}</span>
                            <span>Hit Rate: {calculateHitRate(stats.hitCount || 0, stats.missCount || 0)}%</span>
                          </div>
                        </div>
                        <button
                          onClick={() => clearCache(cacheName)}
                          disabled={clearingCache[cacheName]}
                          className="px-3 py-1 bg-orange-100 text-orange-700 rounded hover:bg-orange-200 disabled:bg-gray-100 disabled:text-gray-400 transition-colors"
                        >
                          {clearingCache[cacheName] ? 'Clearing...' : 'Clear'}
                        </button>
                      </div>
                    </div>
                  );
                })}
                {cacheNames.length === 0 && (
                  <div className="text-center py-8 text-gray-500">
                    No cache data available
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* System Monitoring Tab */}
        {activeTab === 'monitoring' && (
          <SystemMonitoring />
        )}

        {/* Security Tab */}
        {activeTab === 'security' && (
          <div className="bg-white rounded-lg shadow">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-semibold text-gray-900">Security Dashboard</h2>
            </div>
            <div className="p-6">
              <div className="text-center py-12 text-gray-500">
                <Shield className="w-16 h-16 mx-auto mb-4 text-gray-300" />
                <h3 className="text-lg font-medium text-gray-700 mb-2">Security Monitoring</h3>
                <p>Security monitoring features will be implemented here.</p>
                <p className="text-sm mt-2">Track security events, failed login attempts, and system vulnerabilities.</p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminDashboard;