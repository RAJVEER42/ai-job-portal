import React, { useState } from 'react';
import { applicationAPI } from '../services/api';
import { CheckCircle, Mail, FileText, User } from 'lucide-react';

const JobApplicationForm = ({ job, onApplicationSubmit, onClose }) => {
  const [formData, setFormData] = useState({
    coverLetter: '',
    expectedSalary: '',
    availableStartDate: '',
    additionalInfo: ''
  });
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await applicationAPI.applyToJob(job.id, formData);
      
      if (response.data.success) {
        setSuccess(true);
        // Show email notification message
        setTimeout(() => {
          onApplicationSubmit && onApplicationSubmit(response.data.data);
        }, 2000);
      } else {
        setError(response.data.message || 'Application failed');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit application');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  if (success) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-8 max-w-md w-full mx-4">
          <div className="text-center">
            <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">
              Application Submitted Successfully!
            </h3>
            <div className="space-y-3 text-sm text-gray-600">
              <div className="flex items-center justify-center gap-2">
                <Mail className="w-4 h-4 text-blue-500" />
                <span>Confirmation email sent to your inbox</span>
              </div>
              <div className="flex items-center justify-center gap-2">
                <FileText className="w-4 h-4 text-blue-500" />
                <span>Application tracking number: #{Date.now()}</span>
              </div>
              <div className="flex items-center justify-center gap-2">
                <User className="w-4 h-4 text-blue-500" />
                <span>Recruiter has been notified</span>
              </div>
            </div>
            <p className="text-xs text-gray-500 mt-4">
              📧 You'll receive email updates on your application status
            </p>
            <button
              onClick={onClose}
              className="mt-6 w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-semibold text-gray-900">
            Apply for {job.title}
          </h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
          >
            ✕
          </button>
        </div>

        <div className="mb-6 p-4 bg-blue-50 rounded-lg">
          <h3 className="font-medium text-blue-900">{job.company}</h3>
          <p className="text-sm text-blue-700">{job.location}</p>
          <p className="text-sm text-blue-700">{job.salary}</p>
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Cover Letter *
            </label>
            <textarea
              name="coverLetter"
              value={formData.coverLetter}
              onChange={handleChange}
              required
              rows={5}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Tell us why you're perfect for this role..."
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Expected Salary
              </label>
              <input
                type="text"
                name="expectedSalary"
                value={formData.expectedSalary}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="e.g., $80,000 - $100,000"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Available Start Date
              </label>
              <input
                type="date"
                name="availableStartDate"
                value={formData.availableStartDate}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Additional Information
            </label>
            <textarea
              name="additionalInfo"
              value={formData.additionalInfo}
              onChange={handleChange}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Any additional information you'd like to share..."
            />
          </div>

          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <div className="flex items-start gap-2">
              <Mail className="w-5 h-5 text-yellow-600 mt-0.5" />
              <div className="text-sm text-yellow-800">
                <p className="font-medium">Email Notifications Enabled</p>
                <ul className="list-disc list-inside mt-1 space-y-1">
                  <li>Application confirmation will be sent to your email</li>
                  <li>You'll receive updates when application status changes</li>
                  <li>Recruiter will be notified of your application</li>
                </ul>
              </div>
            </div>
          </div>

          <div className="flex gap-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-2 px-4 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || !formData.coverLetter}
              className="flex-1 py-2 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 transition-colors"
            >
              {loading ? 'Submitting...' : 'Submit Application'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default JobApplicationForm;
