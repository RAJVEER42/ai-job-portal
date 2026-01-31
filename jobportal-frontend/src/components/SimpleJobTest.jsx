import React, { useState, useEffect } from 'react';

const SimpleJobTest = () => {
  const [status, setStatus] = useState('Initializing...');
  const [jobs, setJobs] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    const testAPI = async () => {
      setStatus('Testing API connection...');
      
      try {
        // Direct fetch test
        const response = await fetch('http://localhost:8080/api/jobs');
        setStatus(`Response status: ${response.status}`);
        
        if (response.ok) {
          const data = await response.json();
          setStatus(`✅ Success! Found ${data.data.length} jobs`);
          setJobs(data.data.slice(0, 3)); // Show first 3 jobs
        } else {
          setError(`HTTP Error: ${response.status}`);
        }
      } catch (err) {
        setError(`Network Error: ${err.message}`);
        setStatus('❌ Failed to connect');
      }
    };

    testAPI();
  }, []);

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial' }}>
      <h2>🧪 Simple API Test</h2>
      <p><strong>Status:</strong> {status}</p>
      
      {error && (
        <div style={{ background: '#ffebee', padding: '10px', borderRadius: '4px', margin: '10px 0' }}>
          <strong>Error:</strong> {error}
        </div>
      )}
      
      {jobs.length > 0 && (
        <div>
          <h3>Sample Jobs:</h3>
          {jobs.map(job => (
            <div key={job.id} style={{ border: '1px solid #ddd', padding: '10px', margin: '10px 0' }}>
              <h4>{job.title}</h4>
              <p>{job.company} - {job.location}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default SimpleJobTest;
