import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { jobAPI } from '../services/api';
import { Briefcase, MapPin, DollarSign, Clock, Search, Filter } from 'lucide-react';

const Jobs = () => {
  const [jobs, setJobs] = useState([]);
  const [filteredJobs, setFilteredJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [locationFilter, setLocationFilter] = useState('');
  const [experienceFilter, setExperienceFilter] = useState('');
  const [searching, setSearching] = useState(false);
  const [loadTime, setLoadTime] = useState(null);
  const [isCached, setIsCached] = useState(false);
  useEffect(() => {
    const fetchJobs = async () => {
      try {
        setLoading(true);
        setError(null);
        const startTime = performance.now();
        
        console.log('🔄 Fetching jobs from API...');
        console.log('📡 API Base URL:', process.env.REACT_APP_API_URL || 'http://localhost:8080/api');
        
        const response = await jobAPI.getAllJobs();
        
        const endTime = performance.now();
        const responseTime = endTime - startTime;
        setLoadTime(Math.round(responseTime));
        
        console.log('✅ Jobs API Response:', response);
        console.log('⏱️ Response Time:', responseTime.toFixed(2), 'ms');
        
        // Detect if response was cached (fast response typically indicates cache hit)
        setIsCached(responseTime < 100);
        
        // Handle different response structures
        const jobsData = response.data?.data || response.data || [];
        console.log('📊 Jobs Data:', jobsData);
        setJobs(jobsData);
      } catch (err) {
        console.error('❌ Error fetching jobs:', err);
        console.error('📊 Error Details:', {
          message: err.message,
          status: err.response?.status,
          statusText: err.response?.statusText,
          data: err.response?.data
        });
        setError(err.response?.data?.message || err.message || 'Failed to fetch jobs');
      } finally {
        setLoading(false);
      }
    };

    fetchJobs();
  }, []);

  useEffect(() => {
    const handleSearch = async (keyword) => {
      if (!keyword.trim()) {
        setFilteredJobs(jobs);
        return;
      }

      try {
        setSearching(true);
        const response = await jobAPI.searchJobs(keyword);
        const searchResults = response.data?.data || response.data || [];
        setFilteredJobs(searchResults);
      } catch (err) {
        console.error('Error searching jobs:', err);
        setFilteredJobs([]);
      } finally {
        setSearching(false);
      }
    };

    const filterJobs = () => {
      if (!searchTerm && !locationFilter && !experienceFilter) {
        setFilteredJobs(jobs);
        return;
      }

      let filtered = jobs;

      if (searchTerm) {
        handleSearch(searchTerm);
        return;
      }

      if (locationFilter) {
        filtered = filtered.filter(job => 
          job.location.toLowerCase().includes(locationFilter.toLowerCase())
        );
      }

      if (experienceFilter) {
        filtered = filtered.filter(job => 
          job.experienceRequired.toLowerCase().includes(experienceFilter.toLowerCase())
        );
      }

      setFilteredJobs(filtered);
    };

    filterJobs();
  }, [jobs, searchTerm, locationFilter, experienceFilter]);
  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          <h3 className="font-bold mb-2">❌ Connection Error</h3>
          <p className="mb-3">{error}</p>
          
          <div className="bg-red-100 p-3 rounded text-sm">
            <p><strong>Debugging Info:</strong></p>
            <p>• API URL: {process.env.REACT_APP_API_URL || 'http://localhost:8080/api'}</p>
            <p>• Frontend URL: {window.location.origin}</p>
            <p>• Check browser console for detailed error logs</p>
          </div>
          
          <button 
            onClick={() => window.location.reload()} 
            className="mt-3 bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
          >
            🔄 Retry
          </button>
        </div>
        
        <div className="bg-blue-50 border border-blue-200 text-blue-700 px-4 py-3 rounded">
          <h3 className="font-bold mb-2">🔧 Troubleshooting Steps</h3>
          <ol className="list-decimal list-inside text-sm space-y-1">
            <li>Make sure the backend server is running on port 8080</li>
            <li>Check browser console for CORS or network errors</li>
            <li>Verify the API endpoint is accessible</li>
            <li>Try refreshing the page</li>
          </ol>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-3xl font-bold text-gray-900">Browse Jobs</h1>
            
            {/* Performance Indicator */}
            {loadTime && (
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <div className={`w-2 h-2 rounded-full ${isCached ? 'bg-green-500' : 'bg-yellow-500'}`}></div>
                <span>{isCached ? '⚡ Cached' : '🔄 Fresh'}</span>
                <span>({loadTime}ms)</span>
              </div>
            )}
          </div>
          
          {/* Search and Filter Section */}
          <div className="bg-white rounded-lg shadow-md p-6 mb-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* Search Input */}
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search jobs by title, company, or skills..."
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              
              {/* Location Filter */}
              <div className="relative">
                <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Filter by location..."
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  value={locationFilter}
                  onChange={(e) => setLocationFilter(e.target.value)}
                />
              </div>
              
              {/* Experience Filter */}
              <div className="relative">
                <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <select
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent appearance-none"
                  value={experienceFilter}
                  onChange={(e) => setExperienceFilter(e.target.value)}
                >
                  <option value="">All Experience Levels</option>
                  <option value="0-1">0-1 years</option>
                  <option value="1-2">1-2 years</option>
                  <option value="2-4">2-4 years</option>
                  <option value="3-5">3-5 years</option>
                  <option value="4-6">4-6 years</option>
                  <option value="5+">5+ years</option>
                </select>
              </div>
            </div>
            
            <div className="flex justify-between items-center mt-4">
              <p className="text-sm text-gray-600">
                {searching ? 'Searching...' : `${filteredJobs.length} jobs found`}
              </p>
              
              {(searchTerm || locationFilter || experienceFilter) && (
                <button
                  onClick={() => {
                    setSearchTerm('');
                    setLocationFilter('');
                    setExperienceFilter('');
                  }}
                  className="text-sm text-blue-600 hover:text-blue-700 font-medium"
                >
                  Clear Filters
                </button>
              )}
            </div>
          </div>
        </div>

        {filteredJobs.length === 0 ? (
          <div className="text-center py-12">
            <Briefcase className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">No jobs found</h3>
            <p className="mt-1 text-sm text-gray-500">
              {searchTerm || locationFilter || experienceFilter 
                ? 'Try adjusting your search criteria.' 
                : 'Check back later for new opportunities.'}
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredJobs.map((job) => (
              <JobCard key={job.id} job={job} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

const JobCard = ({ job }) => {
  const formatSalary = (min, max) => {
    const formatAmount = (amount) => {
      if (amount >= 100000) {
        return `₹${(amount / 100000).toFixed(1)}L`;
      }
      return `₹${amount.toLocaleString()}`;
    };
    return `${formatAmount(min)} - ${formatAmount(max)}`;
  };

  return (
    <div className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow p-6">
      <div className="flex items-start justify-between mb-4">
        <div className="flex-1">
          <h3 className="text-lg font-semibold text-gray-900 mb-1">
            {job.title}
          </h3>
          <p className="text-gray-600">{job.company}</p>
        </div>
        <span className="px-3 py-1 bg-blue-100 text-blue-700 text-xs font-medium rounded-full">
          {job.jobType?.replace('_', ' ') || 'Full Time'}
        </span>
      </div>
      
      <div className="space-y-2 mb-4">
        <div className="flex items-center text-sm text-gray-600">
          <MapPin className="w-4 h-4 mr-2" />
          {job.location}
        </div>
        
        {job.minSalary && job.maxSalary && (
          <div className="flex items-center text-sm text-gray-600">
            <DollarSign className="w-4 h-4 mr-2" />
            {formatSalary(job.minSalary, job.maxSalary)}
          </div>
        )}
        
        <div className="flex items-center text-sm text-gray-600">
          <Clock className="w-4 h-4 mr-2" />
          {job.experienceRequired}
        </div>
      </div>

      <p className="text-sm text-gray-700 mb-4 line-clamp-3">
        {job.description}
      </p>

      <div className="flex flex-wrap gap-2 mb-4">
        {job.skillsRequired?.split(',').slice(0, 3).map((skill, index) => (
          <span
            key={index}
            className="px-2 py-1 bg-gray-100 text-gray-700 text-xs rounded"
          >
            {skill.trim()}
          </span>
        ))}
        {job.skillsRequired?.split(',').length > 3 && (
          <span className="px-2 py-1 bg-gray-200 text-gray-600 text-xs rounded">
            +{job.skillsRequired.split(',').length - 3} more
          </span>
        )}
      </div>

      <Link 
        to={`/jobs/${job.id}`}
        className="block w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors text-center font-medium"
      >
        View Details
      </Link>
    </div>
  );
};

export default Jobs;
