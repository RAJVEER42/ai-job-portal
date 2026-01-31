import React, { useState, useEffect } from 'react';
import { applicationAPI } from '../services/api';
import { 
  FileText, 
  Clock, 
  CheckCircle, 
  XCircle, 
  Mail, 
  Eye,
  Calendar,
  DollarSign,
  MapPin,
  AlertCircle
} from 'lucide-react';

const MyApplications = () => {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedApp, setSelectedApp] = useState(null);
  const [showWithdrawConfirm, setShowWithdrawConfirm] = useState(false);
  const [withdrawingAppId, setWithdrawingAppId] = useState(null);

  useEffect(() => {
    fetchApplications();
  }, []);

  const fetchApplications = async () => {
    try {
      setLoading(true);
      const response = await applicationAPI.getMyApplications();
      setApplications(response.data?.data || response.data || []);
    } catch (err) {
      console.error('Error fetching applications:', err);
      setError('Failed to fetch applications');
    } finally {
      setLoading(false);
    }
  };

  const getStatusIcon = (status) => {
    switch (status?.toLowerCase()) {
      case 'pending':
        return <Clock className="w-5 h-5 text-yellow-500" />;
      case 'reviewing':
        return <Eye className="w-5 h-5 text-blue-500" />;
      case 'accepted':
      case 'approved':
        return <CheckCircle className="w-5 h-5 text-green-500" />;
      case 'rejected':
      case 'declined':
        return <XCircle className="w-5 h-5 text-red-500" />;
      default:
        return <AlertCircle className="w-5 h-5 text-gray-500" />;
    }
  };

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'pending':
        return 'bg-yellow-100 text-yellow-800';
      case 'reviewing':
        return 'bg-blue-100 text-blue-800';
      case 'accepted':
      case 'approved':
        return 'bg-green-100 text-green-800';
      case 'rejected':
      case 'declined':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const withdrawApplication = async (applicationId) => {
    setWithdrawingAppId(applicationId);
    setShowWithdrawConfirm(true);
  };

  const confirmWithdraw = async () => {
    try {
      await applicationAPI.withdrawApplication(withdrawingAppId);
      await fetchApplications(); // Refresh list
    } catch (err) {
      console.error('Error withdrawing application:', err);
    } finally {
      setShowWithdrawConfirm(false);
      setWithdrawingAppId(null);
    }
  };

  const cancelWithdraw = () => {
    setShowWithdrawConfirm(false);
    setWithdrawingAppId(null);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-6xl mx-auto px-4 py-8">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">My Applications</h1>
          <p className="text-gray-600">Track your job applications and their status</p>
        </div>

        {applications.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-8 text-center">
            <FileText className="w-16 h-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No Applications Yet</h3>
            <p className="text-gray-600 mb-4">Start applying to jobs to see your applications here</p>
            <a 
              href="/jobs"
              className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Browse Jobs
            </a>
          </div>
        ) : (
          <div className="space-y-6">
            {/* Email Notification Info */}
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <div className="flex items-start gap-2">
                <Mail className="w-5 h-5 text-blue-600 mt-0.5" />
                <div className="text-sm text-blue-800">
                  <p className="font-medium">Email Notifications Active</p>
                  <p>You'll receive email updates when your application status changes</p>
                </div>
              </div>
            </div>

            {/* Applications List */}
            <div className="grid gap-6">
              {applications.map((app) => (
                <div key={app.id} className="bg-white rounded-lg shadow hover:shadow-md transition-shadow">
                  <div className="p-6">
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex-1">
                        <h3 className="text-lg font-semibold text-gray-900 mb-1">
                          {app.job?.title || 'Job Title'}
                        </h3>
                        <p className="text-gray-600 font-medium">
                          {app.job?.company || 'Company Name'}
                        </p>
                        
                        <div className="flex items-center gap-4 mt-2 text-sm text-gray-500">
                          <div className="flex items-center gap-1">
                            <MapPin className="w-4 h-4" />
                            <span>{app.job?.location || 'Location'}</span>
                          </div>
                          {app.job?.salary && (
                            <div className="flex items-center gap-1">
                              <DollarSign className="w-4 h-4" />
                              <span>{app.job.salary}</span>
                            </div>
                          )}
                          <div className="flex items-center gap-1">
                            <Calendar className="w-4 h-4" />
                            <span>Applied {new Date(app.appliedAt || app.createdAt || Date.now()).toLocaleDateString()}</span>
                          </div>
                        </div>
                      </div>

                      <div className="flex items-center gap-3">
                        <div className={`flex items-center gap-2 px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(app.status)}`}>
                          {getStatusIcon(app.status)}
                          <span className="capitalize">{app.status || 'Pending'}</span>
                        </div>
                      </div>
                    </div>

                    {/* Application Details */}
                    <div className="border-t border-gray-200 pt-4">
                      <div className="grid md:grid-cols-2 gap-4 text-sm">
                        <div>
                          <p className="text-gray-600 font-medium">Application ID</p>
                          <p className="text-gray-900">#{app.id || app.applicationId}</p>
                        </div>
                        {app.expectedSalary && (
                          <div>
                            <p className="text-gray-600 font-medium">Expected Salary</p>
                            <p className="text-gray-900">{app.expectedSalary}</p>
                          </div>
                        )}
                        {app.availableStartDate && (
                          <div>
                            <p className="text-gray-600 font-medium">Available From</p>
                            <p className="text-gray-900">{new Date(app.availableStartDate).toLocaleDateString()}</p>
                          </div>
                        )}
                        {app.lastUpdated && (
                          <div>
                            <p className="text-gray-600 font-medium">Last Updated</p>
                            <p className="text-gray-900">{new Date(app.lastUpdated).toLocaleDateString()}</p>
                          </div>
                        )}
                      </div>

                      {app.notes && (
                        <div className="mt-3">
                          <p className="text-gray-600 font-medium text-sm">Notes from Recruiter</p>
                          <p className="text-gray-900 text-sm mt-1 p-2 bg-gray-50 rounded border">{app.notes}</p>
                        </div>
                      )}

                      {/* Status-specific messaging */}
                      {app.status?.toLowerCase() === 'pending' && (
                        <div className="mt-3 p-2 bg-yellow-50 border border-yellow-200 rounded text-sm">
                          <p className="text-yellow-800">
                            📧 Your application is under review. You'll receive an email when the status changes.
                          </p>
                        </div>
                      )}

                      {app.status?.toLowerCase() === 'reviewing' && (
                        <div className="mt-3 p-2 bg-blue-50 border border-blue-200 rounded text-sm">
                          <p className="text-blue-800">
                            👀 Great news! Your application is being actively reviewed by the hiring team.
                          </p>
                        </div>
                      )}

                      {app.status?.toLowerCase() === 'accepted' && (
                        <div className="mt-3 p-2 bg-green-50 border border-green-200 rounded text-sm">
                          <p className="text-green-800">
                            🎉 Congratulations! Your application has been accepted. Check your email for next steps.
                          </p>
                        </div>
                      )}
                    </div>

                    {/* Actions */}
                    <div className="border-t border-gray-200 pt-4 mt-4">
                      <div className="flex justify-between items-center">
                        <div className="flex gap-2">
                          <button
                            onClick={() => setSelectedApp(app)}
                            className="text-blue-600 hover:text-blue-700 text-sm font-medium"
                          >
                            View Details
                          </button>
                          {app.job?.id && (
                            <a 
                              href={`/jobs/${app.job.id}`}
                              className="text-blue-600 hover:text-blue-700 text-sm font-medium"
                            >
                              View Job
                            </a>
                          )}
                        </div>

                        {(app.status?.toLowerCase() === 'pending' || app.status?.toLowerCase() === 'reviewing') && (
                          <button
                            onClick={() => withdrawApplication(app.id)}
                            className="text-red-600 hover:text-red-700 text-sm font-medium"
                          >
                            Withdraw
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Application Detail Modal */}
        {selectedApp && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-xl font-semibold text-gray-900">
                  Application Details
                </h2>
                <button
                  onClick={() => setSelectedApp(null)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  ✕
                </button>
              </div>

              <div className="space-y-4">
                <div>
                  <h3 className="font-medium text-gray-900">Cover Letter</h3>
                  <p className="text-gray-700 mt-1 p-3 bg-gray-50 rounded border">
                    {selectedApp.coverLetter || 'No cover letter provided'}
                  </p>
                </div>

                {selectedApp.additionalInfo && (
                  <div>
                    <h3 className="font-medium text-gray-900">Additional Information</h3>
                    <p className="text-gray-700 mt-1 p-3 bg-gray-50 rounded border">
                      {selectedApp.additionalInfo}
                    </p>
                  </div>
                )}

                <div className="bg-blue-50 border border-blue-200 rounded p-3">
                  <p className="text-sm text-blue-800">
                    💡 <strong>Email Updates:</strong> You'll automatically receive notifications when this application status changes.
                  </p>
                </div>
              </div>

              <div className="mt-6">
                <button
                  onClick={() => setSelectedApp(null)}
                  className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Withdrawal Confirmation Modal */}
        {showWithdrawConfirm && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
              <div className="flex items-center gap-3 mb-4">
                <AlertCircle className="w-6 h-6 text-red-600" />
                <h3 className="text-lg font-semibold text-gray-900">Withdraw Application</h3>
              </div>
              
              <p className="text-gray-600 mb-6">
                Are you sure you want to withdraw this application? This action cannot be undone.
              </p>
              
              <div className="flex gap-3">
                <button
                  onClick={cancelWithdraw}
                  className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={confirmWithdraw}
                  className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                >
                  Withdraw
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default MyApplications;
