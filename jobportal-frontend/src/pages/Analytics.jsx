import React, { useState, useEffect } from 'react';
import { jobAPI } from '../services/api';
import {
  BarChart3,
  TrendingUp,
  Target,
  Award,
  Eye,
  MapPin,
  DollarSign
} from 'lucide-react';

const Analytics = () => {
  const [analytics, setAnalytics] = useState({
    totalJobs: 0,
    totalApplications: 0,
    successRate: 0,
    averageSalary: 0,
    topLocations: [],
    topSkills: [],
    applicationTrends: [],
    jobsByCategory: []
  });
  const [loading, setLoading] = useState(true);
  const [timeframe, setTimeframe] = useState('30days');

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        setLoading(true);
        
        // Fetch jobs data for analytics
        const jobsResponse = await jobAPI.getAllJobs();
        const jobs = jobsResponse.data?.data || jobsResponse.data || [];
        
        // Calculate analytics from available data
        const analytics = calculateAnalytics(jobs);
        setAnalytics(analytics);
      } catch (error) {
        console.error('Error fetching analytics:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchAnalytics();
  }, [timeframe]);

  const calculateAnalytics = (jobs) => {
    // Calculate total jobs
    const totalJobs = jobs.length;
    
    // Mock applications data (in real app, this would come from API)
    const totalApplications = Math.floor(Math.random() * 50) + 20;
    const successRate = Math.floor(Math.random() * 30) + 15;
    
    // Calculate average salary
    const salaries = jobs.filter(job => job.minSalary && job.maxSalary)
                         .map(job => (job.minSalary + job.maxSalary) / 2);
    const averageSalary = salaries.length > 0 
      ? salaries.reduce((sum, salary) => sum + salary, 0) / salaries.length
      : 0;
    
    // Top locations
    const locationCounts = {};
    jobs.forEach(job => {
      const location = job.location?.split(',')[0]?.trim() || 'Unknown';
      locationCounts[location] = (locationCounts[location] || 0) + 1;
    });
    const topLocations = Object.entries(locationCounts)
      .sort(([,a], [,b]) => b - a)
      .slice(0, 5)
      .map(([location, count]) => ({ location, count }));
    
    // Top skills
    const skillCounts = {};
    jobs.forEach(job => {
      if (job.skillsRequired) {
        job.skillsRequired.split(',').forEach(skill => {
          const trimmedSkill = skill.trim();
          skillCounts[trimmedSkill] = (skillCounts[trimmedSkill] || 0) + 1;
        });
      }
    });
    const topSkills = Object.entries(skillCounts)
      .sort(([,a], [,b]) => b - a)
      .slice(0, 8)
      .map(([skill, count]) => ({ skill, count }));
    
    // Mock trend data
    const applicationTrends = [
      { date: '2026-01-14', applications: 5 },
      { date: '2026-01-15', applications: 8 },
      { date: '2026-01-16', applications: 12 },
      { date: '2026-01-17', applications: 6 },
      { date: '2026-01-18', applications: 15 },
      { date: '2026-01-19', applications: 10 },
      { date: '2026-01-20', applications: 7 }
    ];
    
    // Jobs by category
    const categoryCounts = {
      'Software Development': Math.floor(totalJobs * 0.4),
      'Data Science': Math.floor(totalJobs * 0.25),
      'Product Management': Math.floor(totalJobs * 0.15),
      'Design': Math.floor(totalJobs * 0.12),
      'Others': Math.floor(totalJobs * 0.08)
    };
    const jobsByCategory = Object.entries(categoryCounts)
      .map(([category, count]) => ({ category, count }));
    
    return {
      totalJobs,
      totalApplications,
      successRate,
      averageSalary,
      topLocations,
      topSkills,
      applicationTrends,
      jobsByCategory
    };
  };

  const formatSalary = (amount) => {
    if (amount >= 100000) {
      return `₹${(amount / 100000).toFixed(1)}L`;
    }
    return `₹${amount.toLocaleString()}`;
  };

  if (loading) {
    return (
      <div className="min-h-screen flex justify-center items-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex justify-between items-center mb-6">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Analytics Dashboard</h1>
              <p className="text-gray-600 mt-2">Insights into your job search performance</p>
            </div>
            
            <div className="flex items-center gap-2">
              <select
                className="border border-gray-300 rounded-lg px-3 py-2 text-sm"
                value={timeframe}
                onChange={(e) => setTimeframe(e.target.value)}
              >
                <option value="7days">Last 7 days</option>
                <option value="30days">Last 30 days</option>
                <option value="90days">Last 3 months</option>
                <option value="1year">Last year</option>
              </select>
            </div>
          </div>
        </div>

        {/* Key Metrics */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <MetricCard
            icon={<Target className="w-6 h-6" />}
            title="Total Jobs Available"
            value={analytics.totalJobs.toString()}
            change="+12%"
            color="blue"
          />
          <MetricCard
            icon={<Eye className="w-6 h-6" />}
            title="Applications Sent"
            value={analytics.totalApplications.toString()}
            change="+23%"
            color="green"
          />
          <MetricCard
            icon={<Award className="w-6 h-6" />}
            title="Success Rate"
            value={`${analytics.successRate}%`}
            change="+5%"
            color="purple"
          />
          <MetricCard
            icon={<DollarSign className="w-6 h-6" />}
            title="Avg. Salary"
            value={formatSalary(analytics.averageSalary)}
            change="+8%"
            color="orange"
          />
        </div>

        {/* Charts Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          {/* Application Trends */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Application Trends</h3>
              <TrendingUp className="w-5 h-5 text-blue-600" />
            </div>
            <div className="space-y-3">
              {analytics.applicationTrends.map((trend, index) => (
                <div key={index} className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">
                    {new Date(trend.date).toLocaleDateString()}
                  </span>
                  <div className="flex items-center gap-2">
                    <div className="w-20 bg-gray-200 rounded-full h-2">
                      <div 
                        className="bg-blue-600 h-2 rounded-full" 
                        style={{ width: `${(trend.applications / 15) * 100}%` }}
                      ></div>
                    </div>
                    <span className="text-sm font-medium">{trend.applications}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Jobs by Category */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Jobs by Category</h3>
              <BarChart3 className="w-5 h-5 text-green-600" />
            </div>
            <div className="space-y-3">
              {analytics.jobsByCategory.map((category, index) => {
                const colors = ['bg-blue-500', 'bg-green-500', 'bg-purple-500', 'bg-orange-500', 'bg-gray-500'];
                const percentage = (category.count / analytics.totalJobs * 100).toFixed(1);
                return (
                  <div key={index} className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className={`w-3 h-3 rounded-full ${colors[index]}`}></div>
                      <span className="text-sm text-gray-700">{category.category}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-sm text-gray-600">{percentage}%</span>
                      <span className="text-sm font-medium">{category.count}</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Additional Analytics */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Top Locations */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Top Job Locations</h3>
              <MapPin className="w-5 h-5 text-red-600" />
            </div>
            <div className="space-y-3">
              {analytics.topLocations.map((location, index) => (
                <div key={index} className="flex items-center justify-between">
                  <span className="text-sm text-gray-700">{location.location}</span>
                  <div className="flex items-center gap-2">
                    <div className="w-16 bg-gray-200 rounded-full h-2">
                      <div 
                        className="bg-red-500 h-2 rounded-full" 
                        style={{ width: `${(location.count / analytics.totalJobs) * 100}%` }}
                      ></div>
                    </div>
                    <span className="text-sm font-medium">{location.count}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Top Skills */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Most Demanded Skills</h3>
              <Award className="w-5 h-5 text-yellow-600" />
            </div>
            <div className="grid grid-cols-2 gap-2">
              {analytics.topSkills.map((skill, index) => (
                <div 
                  key={index} 
                  className="bg-gray-100 rounded-lg p-3 text-center"
                >
                  <div className="text-sm font-medium text-gray-900">{skill.skill}</div>
                  <div className="text-xs text-gray-600">{skill.count} jobs</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// Metric Card Component
const MetricCard = ({ icon, title, value, change, color }) => {
  const colorClasses = {
    blue: 'bg-blue-100 text-blue-600',
    green: 'bg-green-100 text-green-600',
    purple: 'bg-purple-100 text-purple-600',
    orange: 'bg-orange-100 text-orange-600',
  };

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex items-center justify-between mb-4">
        <div className={`p-3 rounded-lg ${colorClasses[color]}`}>
          {icon}
        </div>
        <span className={`text-sm font-semibold ${change.startsWith('+') ? 'text-green-600' : 'text-red-600'}`}>
          {change}
        </span>
      </div>
      <h3 className="text-gray-600 text-sm">{title}</h3>
      <p className="text-2xl font-bold text-gray-900 mt-1">{value}</p>
    </div>
  );
};

export default Analytics;