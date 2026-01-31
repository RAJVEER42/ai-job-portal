import React from 'react';
import { AlertTriangle, RefreshCw, Home, Bug } from 'lucide-react';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
      eventId: null
    };
  }

  static getDerivedStateFromError(error) {
    // Update state so the next render will show the fallback UI
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    // Log error details
    this.setState({
      error,
      errorInfo,
      eventId: this.generateErrorId()
    });

    // Log to console in development
    if (process.env.NODE_ENV === 'development') {
      console.error('🚨 Error Boundary caught an error:', error);
      console.error('Error details:', errorInfo);
    }

    // Report to error tracking service (e.g., Sentry)
    this.reportError(error, errorInfo);
  }

  generateErrorId = () => {
    return 'err_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now();
  };

  reportError = (error, errorInfo) => {
    // In a real application, you would send this to your error tracking service
    const errorReport = {
      id: this.state.eventId || this.generateErrorId(),
      timestamp: new Date().toISOString(),
      error: {
        message: error.message,
        stack: error.stack,
        name: error.name
      },
      errorInfo: {
        componentStack: errorInfo.componentStack
      },
      userAgent: navigator.userAgent,
      url: window.location.href,
      userId: localStorage.getItem('user') ? 
        JSON.parse(localStorage.getItem('user'))?.id : null,
      sessionId: sessionStorage.getItem('sessionId')
    };

    // Console log for development
    console.error('Error Report:', errorReport);

    // In production, send to error tracking service
    if (process.env.NODE_ENV === 'production' && window.Sentry) {
      window.Sentry.captureException(error, {
        tags: { component: 'ErrorBoundary' },
        extra: errorReport
      });
    }
  };

  handleRetry = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
      eventId: null
    });
  };

  handleReportBug = () => {
    const subject = encodeURIComponent(`Error Report - ${this.state.eventId}`);
    const body = encodeURIComponent(`
Error ID: ${this.state.eventId}
Timestamp: ${new Date().toISOString()}
URL: ${window.location.href}
User Agent: ${navigator.userAgent}

Error Details:
${this.state.error?.message || 'Unknown error'}

Stack Trace:
${this.state.error?.stack || 'No stack trace available'}

Component Stack:
${this.state.errorInfo?.componentStack || 'No component stack available'}

Additional Information:
[Please describe what you were doing when this error occurred]
    `);

    window.open(`mailto:support@jobportal.com?subject=${subject}&body=${body}`);
  };

  render() {
    if (this.state.hasError) {
      // Custom error UI based on error type
      const isNetworkError = this.state.error?.message?.toLowerCase().includes('network');
      const isChunkError = this.state.error?.message?.toLowerCase().includes('chunk');

      return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
          <div className="max-w-md w-full">
            {/* Error Icon */}
            <div className="text-center mb-6">
              <div className="mx-auto w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mb-4">
                <AlertTriangle className="w-8 h-8 text-red-600" />
              </div>
              <h1 className="text-2xl font-bold text-gray-900 mb-2">
                {isNetworkError ? 'Connection Error' : 
                 isChunkError ? 'Loading Error' : 
                 'Something went wrong'}
              </h1>
              <p className="text-gray-600">
                {isNetworkError ? 
                  'Please check your internet connection and try again.' :
                  isChunkError ?
                  'The application failed to load properly. Please refresh the page.' :
                  'An unexpected error occurred. Our team has been notified.'
                }
              </p>
            </div>

            {/* Error Details */}
            <div className="bg-white rounded-lg shadow p-6 mb-6">
              <div className="flex items-center gap-2 mb-3">
                <Bug className="w-4 h-4 text-gray-500" />
                <span className="text-sm font-medium text-gray-700">Error Details</span>
              </div>
              
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-500">Error ID:</span>
                  <span className="font-mono text-gray-700">{this.state.eventId}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Time:</span>
                  <span className="text-gray-700">{new Date().toLocaleString()}</span>
                </div>
                {this.state.error?.message && (
                  <div className="pt-2 border-t">
                    <span className="text-gray-500 block mb-1">Message:</span>
                    <span className="text-gray-700 text-xs break-words">
                      {this.state.error.message}
                    </span>
                  </div>
                )}
              </div>
            </div>

            {/* Action Buttons */}
            <div className="space-y-3">
              <button
                onClick={this.handleRetry}
                className="w-full bg-blue-600 text-white py-3 px-4 rounded-lg font-semibold hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
              >
                <RefreshCw className="w-4 h-4" />
                Try Again
              </button>

              <div className="flex gap-3">
                <button
                  onClick={() => window.location.href = '/'}
                  className="flex-1 bg-gray-100 text-gray-700 py-3 px-4 rounded-lg font-medium hover:bg-gray-200 transition-colors flex items-center justify-center gap-2"
                >
                  <Home className="w-4 h-4" />
                  Go Home
                </button>

                <button
                  onClick={this.handleReportBug}
                  className="flex-1 bg-gray-100 text-gray-700 py-3 px-4 rounded-lg font-medium hover:bg-gray-200 transition-colors flex items-center justify-center gap-2"
                >
                  <Bug className="w-4 h-4" />
                  Report Bug
                </button>
              </div>

              <button
                onClick={() => window.location.reload()}
                className="w-full text-gray-500 py-2 text-sm hover:text-gray-700 transition-colors"
              >
                Refresh Page
              </button>
            </div>

            {/* Development Error Details */}
            {process.env.NODE_ENV === 'development' && this.state.error && (
              <details className="mt-6 bg-gray-100 rounded-lg p-4">
                <summary className="cursor-pointer font-medium text-gray-700 mb-2">
                  Development Error Details
                </summary>
                <div className="space-y-2 text-xs">
                  <div>
                    <span className="font-medium">Error:</span>
                    <pre className="mt-1 p-2 bg-red-50 rounded text-red-800 overflow-auto">
                      {this.state.error.toString()}
                    </pre>
                  </div>
                  {this.state.error.stack && (
                    <div>
                      <span className="font-medium">Stack Trace:</span>
                      <pre className="mt-1 p-2 bg-gray-50 rounded text-gray-800 overflow-auto">
                        {this.state.error.stack}
                      </pre>
                    </div>
                  )}
                  {this.state.errorInfo?.componentStack && (
                    <div>
                      <span className="font-medium">Component Stack:</span>
                      <pre className="mt-1 p-2 bg-blue-50 rounded text-blue-800 overflow-auto">
                        {this.state.errorInfo.componentStack}
                      </pre>
                    </div>
                  )}
                </div>
              </details>
            )}
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
