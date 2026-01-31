import React from 'react';
import { Link } from 'react-router-dom';
import { MapPin, DollarSign, Clock, Calendar } from 'lucide-react';

const JobCard = ({ job, showApplyButton = true, compact = false }) => {
  const formatSalary = (min, max) => {
    const formatAmount = (amount) => {
      if (amount >= 100000) {
        return `₹${(amount / 100000).toFixed(1)}L`;
      }
      return `₹${amount.toLocaleString()}`;
    };
    return `${formatAmount(min)} - ${formatAmount(max)}`;
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Not specified';
    return new Date(dateString).toLocaleDateString();
  };

  if (compact) {
    return (
      <Link 
        to={`/jobs/${job.id}`}
        className="block bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
      >
        <div className="flex justify-between items-start mb-2">
          <h3 className="font-semibold text-gray-900 text-sm truncate">{job.title}</h3>
          <span className="px-2 py-1 bg-blue-100 text-blue-700 text-xs font-medium rounded">
            {job.jobType?.replace('_', ' ') || 'Full Time'}
          </span>
        </div>
        
        <p className="text-sm text-gray-600 mb-2">{job.company}</p>
        
        <div className="space-y-1">
          <div className="flex items-center text-xs text-gray-500">
            <MapPin className="w-3 h-3 mr-1" />
            <span className="truncate">{job.location}</span>
          </div>
          
          {job.minSalary && job.maxSalary && (
            <div className="flex items-center text-xs text-gray-500">
              <DollarSign className="w-3 h-3 mr-1" />
              <span>{formatSalary(job.minSalary, job.maxSalary)}</span>
            </div>
          )}
        </div>
      </Link>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow p-6 border border-gray-100">
      <div className="flex items-start justify-between mb-4">
        <div className="flex-1">
          <h3 className="text-lg font-semibold text-gray-900 mb-1 hover:text-blue-600 transition-colors">
            <Link to={`/jobs/${job.id}`}>{job.title}</Link>
          </h3>
          <p className="text-gray-600 font-medium">{job.company}</p>
        </div>
        <span className="px-3 py-1 bg-blue-100 text-blue-700 text-xs font-medium rounded-full">
          {job.jobType?.replace('_', ' ') || 'Full Time'}
        </span>
      </div>
      
      <div className="space-y-2 mb-4">
        <div className="flex items-center text-sm text-gray-600">
          <MapPin className="w-4 h-4 mr-2 flex-shrink-0" />
          <span>{job.location}</span>
        </div>
        
        {job.minSalary && job.maxSalary && (
          <div className="flex items-center text-sm text-gray-600">
            <DollarSign className="w-4 h-4 mr-2 flex-shrink-0" />
            <span>{formatSalary(job.minSalary, job.maxSalary)}</span>
          </div>
        )}
        
        <div className="flex items-center text-sm text-gray-600">
          <Clock className="w-4 h-4 mr-2 flex-shrink-0" />
          <span>{job.experienceRequired || 'Not specified'}</span>
        </div>

        {job.applicationDeadline && (
          <div className="flex items-center text-sm text-gray-600">
            <Calendar className="w-4 h-4 mr-2 flex-shrink-0" />
            <span>Deadline: {formatDate(job.applicationDeadline)}</span>
          </div>
        )}
      </div>

      <p className="text-sm text-gray-700 mb-4 line-clamp-3 leading-relaxed">
        {job.description}
      </p>

      {job.skillsRequired && (
        <div className="flex flex-wrap gap-2 mb-4">
          {job.skillsRequired.split(',').slice(0, 4).map((skill, index) => (
            <span
              key={index}
              className="px-2 py-1 bg-gray-100 text-gray-700 text-xs rounded-md"
            >
              {skill.trim()}
            </span>
          ))}
          {job.skillsRequired.split(',').length > 4 && (
            <span className="px-2 py-1 bg-gray-200 text-gray-600 text-xs rounded-md">
              +{job.skillsRequired.split(',').length - 4} more
            </span>
          )}
        </div>
      )}

      {showApplyButton && (
        <div className="flex gap-3">
          <Link 
            to={`/jobs/${job.id}`}
            className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors text-center font-medium text-sm"
          >
            View Details
          </Link>
          <button className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium">
            Save
          </button>
        </div>
      )}
    </div>
  );
};

export default JobCard;