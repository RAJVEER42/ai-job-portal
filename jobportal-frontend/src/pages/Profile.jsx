import React, { useState, useEffect } from 'react';
import { fileAPI } from '../services/api';
import { Upload, FileText, CheckCircle, AlertCircle, User, Mail, Phone, Briefcase } from 'lucide-react';

const Profile = () => {
  const [user, setUser] = useState(null);
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadStatus, setUploadStatus] = useState(null);

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) {
      try {
        setUser(JSON.parse(userData));
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    }
  }, []);

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    
    if (file) {
      const validTypes = [
        'application/pdf',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      ];
      
      if (!validTypes.includes(file.type)) {
        setUploadStatus({
          type: 'error',
          message: 'Only PDF, DOC, and DOCX files are allowed'
        });
        return;
      }
      
      if (file.size > 10 * 1024 * 1024) {
        setUploadStatus({
          type: 'error',
          message: 'File size must be less than 10MB'
        });
        return;
      }
      
      setSelectedFile(file);
      setUploadStatus(null);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    try {
      setUploading(true);
      setUploadStatus(null);
      
      console.log('Uploading file:', selectedFile.name);
      
      const response = await fileAPI.uploadResume(selectedFile);
      
      console.log('Upload success:', response.data);
      
      setUploadStatus({
        type: 'success',
        message: response.data?.message || 'Resume uploaded successfully!'
      });
      
      setTimeout(() => {
        setSelectedFile(null);
        setUploadStatus(null);
      }, 3000);
      
    } catch (error) {
      console.error('Upload error:', error);
      console.error('Error response:', error.response);
      
      let errorMessage = 'Upload failed. Please try again.';
      
      if (error.response) {
        const status = error.response.status;
        const responseData = error.response.data;
        
        switch (status) {
          case 401:
          case 403:
            errorMessage = 'Authentication required. Please log in and try again.';
            // Redirect to login if authentication failed
            setTimeout(() => {
              window.location.href = '/login';
            }, 2000);
            break;
          case 400:
            errorMessage = responseData?.message || 'Invalid file format. Only PDF, DOC, and DOCX files are allowed.';
            break;
          case 413:
            errorMessage = 'File too large. Maximum size is 10MB.';
            break;
          case 500:
            errorMessage = 'Server error. Please try again later.';
            break;
          default:
            errorMessage = responseData?.message || 
                          responseData?.error ||
                          `Upload failed with status: ${status}`;
        }
      } else if (error.request) {
        // Request made but no response
        errorMessage = 'No response from server. Check your connection.';
      } else {
        // Error setting up request
        errorMessage = error.message || 'Upload failed';
      }
      
      setUploadStatus({
        type: 'error',
        message: errorMessage
      });
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">My Profile</h1>

      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Personal Information</h2>
        
        {user ? (
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <User className="h-5 w-5 text-gray-400" />
              <div>
                <p className="text-sm text-gray-500">Full Name</p>
                <p className="text-gray-900 font-medium">{user.fullName}</p>
              </div>
            </div>
            
            <div className="flex items-center gap-3">
              <Mail className="h-5 w-5 text-gray-400" />
              <div>
                <p className="text-sm text-gray-500">Email</p>
                <p className="text-gray-900 font-medium">{user.email}</p>
              </div>
            </div>
            
            {user.phone && (
              <div className="flex items-center gap-3">
                <Phone className="h-5 w-5 text-gray-400" />
                <div>
                  <p className="text-sm text-gray-500">Phone</p>
                  <p className="text-gray-900 font-medium">{user.phone}</p>
                </div>
              </div>
            )}
            
            <div className="flex items-center gap-3">
              <Briefcase className="h-5 w-5 text-gray-400" />
              <div>
                <p className="text-sm text-gray-500">Role</p>
                <p className="text-gray-900 font-medium capitalize">{user.role?.toLowerCase()}</p>
              </div>
            </div>
          </div>
        ) : (
          <p className="text-gray-500">Loading user information...</p>
        )}
      </div>

      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold mb-4">Upload Resume</h2>
        
        <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-gray-400 transition-colors">
          <Upload className="mx-auto h-12 w-12 text-gray-400 mb-4" />
          
          <input
            type="file"
            id="resume-upload"
            className="hidden"
            accept=".pdf,.doc,.docx"
            onChange={handleFileSelect}
          />
          
          <label
            htmlFor="resume-upload"
            className="cursor-pointer text-blue-600 hover:text-blue-700 font-medium text-lg"
          >
            Choose file
          </label>
          
          <p className="text-sm text-gray-500 mt-2">
            or drag and drop
          </p>
          
          <p className="text-xs text-gray-400 mt-1">
            PDF, DOC, DOCX - Maximum 10MB
          </p>

          {selectedFile && (
            <div className="mt-6 p-4 bg-blue-50 rounded-lg">
              <div className="flex items-center justify-center gap-2 mb-4">
                <FileText className="h-5 w-5 text-blue-600" />
                <span className="text-sm font-medium text-gray-700">
                  {selectedFile.name}
                </span>
                <span className="text-xs text-gray-500">
                  ({(selectedFile.size / 1024).toFixed(2)} KB)
                </span>
              </div>
              
              <button
                onClick={handleUpload}
                disabled={uploading}
                className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
              >
                {uploading ? (
                  <span className="flex items-center gap-2">
                    <span className="animate-spin">⏳</span>
                    Uploading...
                  </span>
                ) : (
                  'Upload Resume'
                )}
              </button>
            </div>
          )}

          {uploadStatus && (
            <div className={`mt-4 p-4 rounded-lg flex items-center justify-center gap-2 ${
              uploadStatus.type === 'success'
                ? 'bg-green-50 text-green-700 border border-green-200'
                : 'bg-red-50 text-red-700 border border-red-200'
            }`}>
              {uploadStatus.type === 'success' ? (
                <CheckCircle className="h-5 w-5" />
              ) : (
                <AlertCircle className="h-5 w-5" />
              )}
              <span className="text-sm font-medium">{uploadStatus.message}</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Profile;