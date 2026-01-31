import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { jobAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import JobApplicationForm from '../components/JobApplicationForm';
import { 
  ArrowLeft, 
  MapPin, 
  DollarSign, 
  Calendar, 
  Clock, 
  Building2,
  User,
  CheckCircle,
  AlertCircle
} from 'lucide-react';

const JobDetails = () => {
  const { id } = useParams();
  const { user } = useAuth();
  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showApplicationForm, setShowApplicationForm] = useState(false);
  const [applied, setApplied] = useState(false);

  useEffect(() => {
    const fetchJobDetails = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await jobAPI.getJobById(id);
        
        // Handle different response structures
        const jobData = response.data?.data || response.data;
        setJob(jobData);
      } catch (err) {
        console.error('Error fetching job details:', err);
        setError(err.response?.data?.message || 'Failed to fetch job details');
      } finally {
        setLoading(false);
      }
    };

    fetchJobDetails();
  }, [id]);

  const handleApply = () => {
    if (!user) {
      // Redirect to login if not authenticated
      window.location.href = '/login';
      return;
    }
    setShowApplicationForm(true);
  };

  const handleApplicationSuccess = () => {
    setApplied(true);
    setShowApplicationForm(false);
  };

  const formatSalary = (min, max) => {
    const formatAmount = (amount) => {
      if (amount >= 100000) {
        return `₹${(amount / 100000).toFixed(1)}L`;
      }
      return `₹${amount.toLocaleString()}`;
    };
    return `${formatAmount(min)} - ${formatAmount(max)}`;
  };

  if (loading) {
    return (
      <div className="min-h-screen flex justify-center items-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          <div className="flex items-center gap-2">
            <AlertCircle className="w-5 h-5" />
            <span>{error}</span>
          </div>
        </div>
        <Link to="/jobs" className="inline-flex items-center gap-2 mt-4 text-blue-600 hover:text-blue-700">
          <ArrowLeft className="w-4 h-4" />
          Back to Jobs
        </Link>
      </div>
    );
  }

  if (!job) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Job Not Found</h2>
          <p className="text-gray-600 mb-6">The job you're looking for doesn't exist or has been removed.</p>
          <Link to="/jobs" className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700">
            <ArrowLeft className="w-4 h-4" />
            Back to Jobs
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4">
        {/* Header */}
        <div className="mb-6">
          <Link to="/jobs" className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700 mb-4">
            <ArrowLeft className="w-4 h-4" />
            Back to Jobs
          </Link>
        </div>

        {/* Job Details Card */}
        <div className="bg-white rounded-xl shadow-md p-8 mb-6">
          <div className="flex flex-col lg:flex-row lg:justify-between lg:items-start gap-6">
            <div className="flex-1">
              <h1 className="text-3xl font-bold text-gray-900 mb-3">{job.title}</h1>
              
              <div className="flex flex-wrap gap-4 mb-6">
                <div className="flex items-center gap-2 text-gray-600">
                  <Building2 className="w-5 h-5" />
                  <span className="font-medium">{job.company}</span>
                </div>
                <div className="flex items-center gap-2 text-gray-600">
                  <MapPin className="w-5 h-5" />
                  <span>{job.location}</span>
                </div>
                <div className="flex items-center gap-2 text-gray-600">
                  <DollarSign className="w-5 h-5" />
                  <span>{formatSalary(job.minSalary, job.maxSalary)}</span>
                </div>
                <div className="flex items-center gap-2 text-gray-600">
                  <Clock className="w-5 h-5" />
                  <span>{job.experienceRequired}</span>
                </div>
              </div>

              <div className="mb-6">
                <span className="inline-block bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-medium">
                  {job.jobType?.replace('_', ' ') || 'Full Time'}
                </span>
              </div>

              <div className="mb-6">
                <h2 className="text-xl font-semibold text-gray-900 mb-3">Job Description</h2>
                <p className="text-gray-700 leading-relaxed">{job.description}</p>
              </div>

              <div className="mb-6">
                <h2 className="text-xl font-semibold text-gray-900 mb-3">Required Skills</h2>
                <div className="flex flex-wrap gap-2">
                  {job.skillsRequired?.split(',').map((skill, index) => (
                    <span 
                      key={index}
                      className="bg-gray-100 text-gray-800 px-3 py-1 rounded-full text-sm"
                    >
                      {skill.trim()}
                    </span>
                  ))}
                </div>
              </div>

              {job.recruiter && (
                <div className="mb-6">
                  <h2 className="text-xl font-semibold text-gray-900 mb-3">Posted By</h2>
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                      <User className="w-5 h-5 text-blue-600" />
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">{job.recruiter.fullName}</p>
                      <p className="text-gray-600">{job.recruiter.email}</p>
                    </div>
                  </div>
                </div>
              )}

              {job.applicationDeadline && (
                <div className="flex items-center gap-2 text-gray-600 mb-6">
                  <Calendar className="w-5 h-5" />
                  <span>Application Deadline: {new Date(job.applicationDeadline).toLocaleDateString()}</span>
                </div>
              )}
            </div>

            {/* Application Section */}
            <div className="lg:w-80">
              <div className="bg-gray-50 rounded-lg p-6 sticky top-8">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Apply for this job</h3>
                
                {!user ? (
                  <div className="text-center">
                    <p className="text-gray-600 mb-4">Please login to apply for this job</p>
                    <Link 
                      to="/login" 
                      className="w-full bg-blue-600 text-white py-3 px-4 rounded-lg font-semibold hover:bg-blue-700 transition-colors duration-200 inline-block text-center"
                    >
                      Login to Apply
                    </Link>
                  </div>
                ) : applied ? (
                  <div className="text-center">
                    <div className="flex items-center justify-center gap-2 text-green-600 mb-3">
                      <CheckCircle className="w-6 h-6" />
                      <span className="font-semibold">Applied Successfully!</span>
                    </div>
                    <p className="text-gray-600 text-sm">
                      Your application has been submitted. The recruiter will contact you soon.
                    </p>
                  </div>
                ) : (
                  <div>
                    <p className="text-gray-600 mb-4 text-sm">
                      Ready to take the next step in your career?
                    </p>
                    <button 
                      onClick={handleApply}
                      className="w-full bg-blue-600 text-white py-3 px-4 rounded-lg font-semibold hover:bg-blue-700 transition-colors duration-200"
                    >
                      Apply Now
                    </button>
                    <p className="text-xs text-gray-500 mt-3 text-center">
                      Your profile information will be shared with the recruiter.
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Job Application Form Modal */}
      {showApplicationForm && (
        <JobApplicationForm
          job={job}
          onClose={() => setShowApplicationForm(false)}
          onSuccess={handleApplicationSuccess}
        />
      )}
    </div>
  );
};

export default JobDetails;