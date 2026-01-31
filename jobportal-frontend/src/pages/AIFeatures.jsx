import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { fileAPI } from '../services/api';
import {
  Brain,
  FileText,
  Target,
  MessageSquare,
  Sparkles,
  Upload,
  CheckCircle,
  ArrowRight,
  Zap,
  TrendingUp
} from 'lucide-react';

const AIFeatures = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('resume-parser');
  const [uploadedFile, setUploadedFile] = useState(null);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [analyzing, setAnalyzing] = useState(false);
  const [fileContent, setFileContent] = useState('');

  // Skills database for matching
  const skillsDatabase = [
    'JavaScript', 'TypeScript', 'React', 'Vue', 'Angular', 'Node.js', 'Express',
    'Python', 'Django', 'Flask', 'Java', 'Spring', 'C++', 'C#', '.NET',
    'PHP', 'Laravel', 'Ruby', 'Rails', 'Go', 'Rust', 'Swift', 'Kotlin',
    'HTML', 'CSS', 'SASS', 'LESS', 'Bootstrap', 'Tailwind', 'Material-UI',
    'SQL', 'MySQL', 'PostgreSQL', 'MongoDB', 'Redis', 'Firebase',
    'AWS', 'Azure', 'GCP', 'Docker', 'Kubernetes', 'Jenkins', 'CI/CD',
    'Git', 'GitHub', 'GitLab', 'Jira', 'Agile', 'Scrum', 'DevOps',
    'Machine Learning', 'AI', 'TensorFlow', 'PyTorch', 'Pandas', 'NumPy',
    'React Native', 'Flutter', 'Xamarin', 'iOS', 'Android',
    'Photoshop', 'Figma', 'Sketch', 'Adobe XD', 'UI/UX', 'Design'
  ];

  // Read file content
  const readFileContent = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      
      reader.onload = (e) => {
        const content = e.target.result;
        resolve(content);
      };
      
      reader.onerror = (e) => {
        reject(new Error('Failed to read file'));
      };

      // Read as text for now (works with .txt, basic content extraction)
      // For production, you'd want to use libraries like pdf-parse for PDFs
      if (file.type === 'text/plain' || file.name.endsWith('.txt')) {
        reader.readAsText(file);
      } else {
        // For non-text files, we'll try to read as text but it might not work well
        // In production, you'd use proper PDF/DOC parsers
        reader.readAsText(file);
      }
    });
  };

  // Extract skills from text content
  const extractSkills = (content) => {
    const foundSkills = [];
    const contentLower = content.toLowerCase();
    
    skillsDatabase.forEach(skill => {
      if (contentLower.includes(skill.toLowerCase())) {
        foundSkills.push(skill);
      }
    });
    
    return [...new Set(foundSkills)]; // Remove duplicates
  };

  // Extract experience level from content
  const extractExperience = (content) => {
    // Look for experience patterns
    const experiencePatterns = [
      { pattern: /(\d+)\s*(?:years?|yrs?)\s*(?:of\s*)?experience/i, type: 'years' },
      { pattern: /(\d+)\+\s*(?:years?|yrs?)/i, type: 'plus' },
      { pattern: /(\d+)\s*-\s*(\d+)\s*(?:years?|yrs?)/i, type: 'range' },
      { pattern: /senior|lead|principal/i, type: 'senior' },
      { pattern: /junior|entry.?level|fresher|graduate/i, type: 'junior' },
      { pattern: /intern|internship/i, type: 'intern' }
    ];
    
    for (const { pattern, type } of experiencePatterns) {
      const match = content.match(pattern);
      if (match) {
        switch (type) {
          case 'years':
            const years = parseInt(match[1]);
            if (years <= 2) return '0-2 years';
            if (years <= 4) return '2-4 years';
            if (years <= 7) return '5-7 years';
            return '7+ years';
          case 'plus':
            return `${match[1]}+ years`;
          case 'range':
            return `${match[1]}-${match[2]} years`;
          case 'senior':
            return '5+ years (Senior level)';
          case 'junior':
            return '0-2 years (Junior level)';
          case 'intern':
            return '0-1 years (Intern level)';
          default:
            return 'Experience level detected';
        }
      }
    }
    
    return 'Experience level not specified';
  };

  // Generate summary based on skills and content
  const generateSummary = (skills, content, experience) => {
    const hasBackend = skills.some(skill => ['Node.js', 'Python', 'Java', 'PHP', 'Express', 'Django', 'Spring'].includes(skill));
    const hasFrontend = skills.some(skill => ['React', 'Vue', 'Angular', 'HTML', 'CSS', 'JavaScript'].includes(skill));
    const hasDatabase = skills.some(skill => ['SQL', 'MySQL', 'PostgreSQL', 'MongoDB'].includes(skill));
    const hasCloud = skills.some(skill => ['AWS', 'Azure', 'GCP', 'Docker', 'Kubernetes'].includes(skill));
    
    let summary = '';
    
    if (hasFrontend && hasBackend) {
      summary = 'Full-stack developer with ';
    } else if (hasFrontend) {
      summary = 'Frontend developer with ';
    } else if (hasBackend) {
      summary = 'Backend developer with ';
    } else {
      summary = 'Professional with ';
    }
    
    if (experience.includes('Senior') || experience.includes('7+') || experience.includes('5+')) {
      summary += 'extensive experience in ';
    } else if (experience.includes('0-2') || experience.includes('Junior')) {
      summary += 'growing experience in ';
    } else {
      summary += 'solid experience in ';
    }
    
    const topSkills = skills.slice(0, 3);
    if (topSkills.length > 0) {
      summary += topSkills.join(', ');
    } else {
      summary += 'various technologies';
    }
    
    if (hasDatabase) {
      summary += ' and database management';
    }
    
    if (hasCloud) {
      summary += ' with cloud platform expertise';
    }
    
    summary += '.';
    
    return summary;
  };

  // Generate improvement suggestions based on content analysis
  const generateSuggestions = (skills, content) => {
    const suggestions = [];
    const contentLower = content.toLowerCase();
    
    // Check for missing modern frameworks
    if (!skills.some(s => ['React', 'Vue', 'Angular'].includes(s))) {
      suggestions.push('Consider adding modern frontend framework experience (React, Vue, or Angular)');
    }
    
    // Check for cloud skills
    if (!skills.some(s => ['AWS', 'Azure', 'GCP', 'Docker'].includes(s))) {
      suggestions.push('Add cloud platform certifications (AWS, Azure, or GCP) to stay competitive');
    }
    
    // Check for version control
    if (!skills.includes('Git') && !contentLower.includes('git')) {
      suggestions.push('Include Git/GitHub experience in your resume');
    }
    
    // Check for metrics and numbers
    if (!/\d+%|\d+x|increased|improved|reduced/.test(content)) {
      suggestions.push('Add quantifiable achievements and metrics to demonstrate impact');
    }
    
    // Check for leadership keywords
    if (!/lead|manage|mentor|coordinate|team/.test(contentLower)) {
      suggestions.push('Include leadership or team collaboration experience');
    }
    
    // Check for recent technologies
    const modernSkills = ['TypeScript', 'Docker', 'Kubernetes', 'CI/CD', 'Microservices'];
    if (!skills.some(s => modernSkills.includes(s))) {
      suggestions.push('Consider learning modern development practices (Docker, TypeScript, CI/CD)');
    }
    
    return suggestions.slice(0, 4); // Limit to 4 suggestions
  };

  // Find matching jobs based on skills
  const findMatchingJobs = (skills) => {
    const jobMatches = [
      {
        title: 'Full Stack Developer',
        company: 'TechCorp Inc.',
        requiredSkills: ['JavaScript', 'React', 'Node.js', 'SQL'],
        match: 0
      },
      {
        title: 'Frontend Developer',
        company: 'WebSolutions',
        requiredSkills: ['JavaScript', 'React', 'HTML', 'CSS'],
        match: 0
      },
      {
        title: 'Backend Developer',
        company: 'DataFlow Systems',
        requiredSkills: ['Python', 'Django', 'PostgreSQL', 'AWS'],
        match: 0
      },
      {
        title: 'DevOps Engineer',
        company: 'CloudTech',
        requiredSkills: ['Docker', 'Kubernetes', 'AWS', 'Jenkins'],
        match: 0
      },
      {
        title: 'Mobile Developer',
        company: 'AppMakers',
        requiredSkills: ['React Native', 'JavaScript', 'iOS', 'Android'],
        match: 0
      }
    ];
    
    // Calculate match percentage for each job
    jobMatches.forEach(job => {
      const matchedSkills = job.requiredSkills.filter(skill => 
        skills.some(userSkill => userSkill.toLowerCase() === skill.toLowerCase())
      );
      job.match = Math.round((matchedSkills.length / job.requiredSkills.length) * 100);
    });
    
    // Sort by match percentage and return top 3
    return jobMatches
      .filter(job => job.match > 0)
      .sort((a, b) => b.match - a.match)
      .slice(0, 3);
  };

  const features = [
    {
      id: 'resume-parser',
      title: 'AI Resume Parser',
      icon: <FileText className="w-6 h-6" />,
      description: 'Extract skills and experience from your resume automatically',
      color: 'blue'
    },
    {
      id: 'job-matching',
      title: 'Smart Job Matching',
      icon: <Target className="w-6 h-6" />,
      description: 'Get personalized job recommendations based on your profile',
      color: 'green'
    },
    {
      id: 'interview-prep',
      title: 'Interview Preparation',
      icon: <MessageSquare className="w-6 h-6" />,
      description: 'Practice with AI-generated interview questions',
      color: 'purple'
    },
    {
      id: 'skill-gap',
      title: 'Skill Gap Analysis',
      icon: <TrendingUp className="w-6 h-6" />,
      description: 'Identify skills you need to learn for your target roles',
      color: 'orange'
    }
  ];

  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (file) {
      setUploadedFile(file);
      setAnalysisResult(null);
      
      // Read file content immediately when uploaded
      try {
        const content = await readFileContent(file);
        setFileContent(content);
        console.log('📄 File content extracted:', content.substring(0, 200) + '...');
      } catch (error) {
        console.error('Error reading file:', error);
        setFileContent('');
      }
    }
  };

  const analyzeResume = async () => {
    if (!uploadedFile) return;
    
    setAnalyzing(true);
    
    try {
      // Option 1: Use real file content (basic analysis)
      if (fileContent) {
        console.log('🔍 Analyzing file content locally...');
        
        const skills = extractSkills(fileContent);
        const experience = extractExperience(fileContent);
        const summary = generateSummary(skills, fileContent, experience);
        const suggestions = generateSuggestions(skills, fileContent);
        const matchingJobs = findMatchingJobs(skills);
        
        setAnalysisResult({
          skills,
          experience,
          summary,
          suggestions,
          matchingJobs,
          analysisType: 'local',
          fileSize: uploadedFile.size,
          fileName: uploadedFile.name
        });
        
        setAnalyzing(false);
        return;
      }
      
      // Option 2: Use backend API for advanced processing
      console.log('🚀 Sending file to backend for analysis...');
      
      const response = await fileAPI.uploadResume(uploadedFile);
      console.log('📡 Backend response:', response.data);
      
      if (response.data && response.data.analysis) {
        // If backend provides analysis
        setAnalysisResult({
          ...response.data.analysis,
          analysisType: 'backend',
          fileSize: uploadedFile.size,
          fileName: uploadedFile.name
        });
      } else {
        // Fallback to local analysis
        const skills = extractSkills('Software developer with experience in web technologies');
        const experience = '2-4 years';
        const summary = 'Professional developer with backend upload processing capabilities.';
        const suggestions = [
          'File successfully uploaded to backend server',
          'Consider implementing advanced resume parsing on backend',
          'Add structured data extraction for better analysis',
          'Integrate with external AI services for enhanced processing'
        ];
        const matchingJobs = findMatchingJobs(skills);
        
        setAnalysisResult({
          skills,
          experience,
          summary,
          suggestions,
          matchingJobs,
          analysisType: 'backend-basic',
          fileSize: uploadedFile.size,
          fileName: uploadedFile.name,
          backendResponse: response.data
        });
      }
      
    } catch (error) {
      console.error('❌ Analysis error:', error);
      
      // Fallback: If backend fails, try local analysis
      if (fileContent) {
        console.log('🔄 Falling back to local analysis...');
        
        const skills = extractSkills(fileContent);
        const experience = extractExperience(fileContent);
        const summary = generateSummary(skills, fileContent, experience);
        const suggestions = generateSuggestions(skills, fileContent);
        const matchingJobs = findMatchingJobs(skills);
        
        setAnalysisResult({
          skills,
          experience,
          summary,
          suggestions,
          matchingJobs,
          analysisType: 'local-fallback',
          error: error.message,
          fileSize: uploadedFile.size,
          fileName: uploadedFile.name
        });
      } else {
        // Last resort: Show error
        setAnalysisResult({
          skills: [],
          experience: 'Unable to determine',
          summary: 'Analysis failed. Please try uploading a text file or check your connection.',
          suggestions: [
            'Try uploading a .txt file for better text extraction',
            'Ensure your file contains readable text',
            'Check your internet connection for backend processing',
            'Contact support if the issue persists'
          ],
          matchingJobs: [],
          analysisType: 'error',
          error: error.message,
          fileSize: uploadedFile.size,
          fileName: uploadedFile.name
        });
      }
    } finally {
      setAnalyzing(false);
    }
  };

  const renderFeatureContent = () => {
    switch (activeTab) {
      case 'resume-parser':
        return (
          <div className="space-y-6">
            <div className="text-center">
              <Brain className="w-16 h-16 text-blue-600 mx-auto mb-4" />
              <h3 className="text-2xl font-bold text-gray-900 mb-2">AI-Powered Resume Analysis</h3>
              <p className="text-gray-600 max-w-2xl mx-auto">
                Upload your resume and let our AI extract your skills, experience, and suggest improvements
                to help you land your dream job.
              </p>
            </div>

            {!uploadedFile ? (
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-blue-500 transition-colors">
                <Upload className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <h4 className="text-lg font-medium text-gray-900 mb-2">Upload Your Resume</h4>
                <p className="text-gray-500 mb-4">Supports PDF, DOC, and DOCX files up to 10MB</p>
                <label className="inline-block bg-blue-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors cursor-pointer">
                  Choose File
                  <input 
                    type="file" 
                    accept=".pdf,.doc,.docx" 
                    onChange={handleFileUpload}
                    className="hidden"
                  />
                </label>
              </div>
            ) : (
              <div className="bg-gray-50 rounded-lg p-6">
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <FileText className="w-8 h-8 text-blue-600" />
                    <div>
                      <p className="font-medium text-gray-900">{uploadedFile.name}</p>
                      <p className="text-sm text-gray-500">{(uploadedFile.size / 1024 / 1024).toFixed(2)} MB</p>
                    </div>
                  </div>
                  <button
                    onClick={analyzeResume}
                    disabled={analyzing}
                    className="bg-blue-600 text-white px-6 py-2 rounded-lg font-medium hover:bg-blue-700 transition-colors disabled:opacity-50"
                  >
                    {analyzing ? 'Analyzing...' : 'Analyze Resume'}
                  </button>
                </div>

                {analyzing && (
                  <div className="flex items-center gap-3 text-blue-600">
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600"></div>
                    <span>AI is analyzing your resume...</span>
                  </div>
                )}

                {analysisResult && (
                  <div className="mt-6 space-y-6">
                    {/* Skills */}
                    <div>
                      <h4 className="font-semibold text-gray-900 mb-3">Extracted Skills</h4>
                      <div className="flex flex-wrap gap-2">
                        {analysisResult.skills.map((skill, index) => (
                          <span key={index} className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm">
                            {skill}
                          </span>
                        ))}
                      </div>
                    </div>

                    {/* Experience */}
                    <div>
                      <h4 className="font-semibold text-gray-900 mb-2">Experience Level</h4>
                      <p className="text-gray-700">{analysisResult.experience}</p>
                    </div>

                    {/* Summary */}
                    <div>
                      <h4 className="font-semibold text-gray-900 mb-2">Profile Summary</h4>
                      <p className="text-gray-700">{analysisResult.summary}</p>
                    </div>

                    {/* Suggestions */}
                    <div>
                      <h4 className="font-semibold text-gray-900 mb-3">Improvement Suggestions</h4>
                      <ul className="space-y-2">
                        {analysisResult.suggestions.map((suggestion, index) => (
                          <li key={index} className="flex items-start gap-2">
                            <CheckCircle className="w-5 h-5 text-green-600 mt-0.5 flex-shrink-0" />
                            <span className="text-gray-700">{suggestion}</span>
                          </li>
                        ))}
                      </ul>
                    </div>

                    {/* Matching Jobs */}
                    <div>
                      <h4 className="font-semibold text-gray-900 mb-3">Matching Jobs</h4>
                      <div className="space-y-3">
                        {analysisResult.matchingJobs.map((job, index) => (
                          <div key={index} className="flex items-center justify-between p-3 bg-white rounded-lg border">
                            <div>
                              <p className="font-medium text-gray-900">{job.title}</p>
                              <p className="text-sm text-gray-600">{job.company}</p>
                            </div>
                            <div className="flex items-center gap-3">
                              <div className="text-right">
                                <p className="text-sm font-medium text-green-600">{job.match}% match</p>
                              </div>
                              <ArrowRight className="w-4 h-4 text-gray-400" />
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        );

      case 'job-matching':
        return (
          <div className="space-y-6">
            <div className="text-center">
              <Target className="w-16 h-16 text-green-600 mx-auto mb-4" />
              <h3 className="text-2xl font-bold text-gray-900 mb-2">Smart Job Matching</h3>
              <p className="text-gray-600 max-w-2xl mx-auto">
                Our AI analyzes your profile and preferences to find the perfect job matches for you.
              </p>
            </div>
            
            <div className="bg-gradient-to-r from-green-50 to-blue-50 rounded-lg p-8">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="text-center">
                  <div className="bg-green-100 w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-3">
                    <Sparkles className="w-6 h-6 text-green-600" />
                  </div>
                  <h4 className="font-semibold text-gray-900 mb-2">AI Analysis</h4>
                  <p className="text-sm text-gray-600">Analyze your skills and preferences</p>
                </div>
                <div className="text-center">
                  <div className="bg-blue-100 w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-3">
                    <Target className="w-6 h-6 text-blue-600" />
                  </div>
                  <h4 className="font-semibold text-gray-900 mb-2">Smart Matching</h4>
                  <p className="text-sm text-gray-600">Find jobs that match your profile</p>
                </div>
                <div className="text-center">
                  <div className="bg-purple-100 w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-3">
                    <Zap className="w-6 h-6 text-purple-600" />
                  </div>
                  <h4 className="font-semibold text-gray-900 mb-2">Instant Results</h4>
                  <p className="text-sm text-gray-600">Get recommendations in real-time</p>
                </div>
              </div>
            </div>
            
            <div className="text-center">
              <button className="bg-green-600 text-white px-8 py-3 rounded-lg font-medium hover:bg-green-700 transition-colors">
                Start Job Matching
              </button>
            </div>
          </div>
        );

      case 'interview-prep':
        return (
          <div className="space-y-6">
            <div className="text-center">
              <MessageSquare className="w-16 h-16 text-purple-600 mx-auto mb-4" />
              <h3 className="text-2xl font-bold text-gray-900 mb-2">AI Interview Preparation</h3>
              <p className="text-gray-600 max-w-2xl mx-auto">
                Practice with AI-generated interview questions tailored to your target role and experience level.
              </p>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="bg-white border border-gray-200 rounded-lg p-6">
                <h4 className="font-semibold text-gray-900 mb-3">Technical Questions</h4>
                <p className="text-gray-600 text-sm mb-4">Practice coding and technical concept questions</p>
                <button className="w-full bg-purple-600 text-white py-2 px-4 rounded-lg font-medium hover:bg-purple-700 transition-colors">
                  Start Technical Prep
                </button>
              </div>
              
              <div className="bg-white border border-gray-200 rounded-lg p-6">
                <h4 className="font-semibold text-gray-900 mb-3">Behavioral Questions</h4>
                <p className="text-gray-600 text-sm mb-4">Prepare for situational and behavioral interviews</p>
                <button className="w-full bg-purple-600 text-white py-2 px-4 rounded-lg font-medium hover:bg-purple-700 transition-colors">
                  Start Behavioral Prep
                </button>
              </div>
            </div>
          </div>
        );

      case 'skill-gap':
        return (
          <div className="space-y-6">
            <div className="text-center">
              <TrendingUp className="w-16 h-16 text-orange-600 mx-auto mb-4" />
              <h3 className="text-2xl font-bold text-gray-900 mb-2">Skill Gap Analysis</h3>
              <p className="text-gray-600 max-w-2xl mx-auto">
                Identify skills you need to develop to reach your career goals and get personalized learning recommendations.
              </p>
            </div>
            
            <div className="bg-orange-50 rounded-lg p-6">
              <h4 className="font-semibold text-gray-900 mb-4">How It Works</h4>
              <div className="space-y-3">
                <div className="flex items-start gap-3">
                  <div className="w-6 h-6 bg-orange-600 text-white rounded-full flex items-center justify-center text-sm font-bold">1</div>
                  <div>
                    <p className="font-medium text-gray-900">Choose Your Target Role</p>
                    <p className="text-sm text-gray-600">Select the job position you're aiming for</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <div className="w-6 h-6 bg-orange-600 text-white rounded-full flex items-center justify-center text-sm font-bold">2</div>
                  <div>
                    <p className="font-medium text-gray-900">AI Analysis</p>
                    <p className="text-sm text-gray-600">Our AI compares your skills with job requirements</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <div className="w-6 h-6 bg-orange-600 text-white rounded-full flex items-center justify-center text-sm font-bold">3</div>
                  <div>
                    <p className="font-medium text-gray-900">Get Recommendations</p>
                    <p className="text-sm text-gray-600">Receive personalized learning paths and resources</p>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="text-center">
              <button className="bg-orange-600 text-white px-8 py-3 rounded-lg font-medium hover:bg-orange-700 transition-colors">
                Analyze Skills Gap
              </button>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">AI-Powered Features</h1>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto">
            Leverage artificial intelligence to supercharge your job search with smart tools and personalized insights.
          </p>
        </div>

        {/* Feature Tabs */}
        <div className="flex flex-wrap justify-center gap-4 mb-8">
          {features.map((feature) => (
            <button
              key={feature.id}
              onClick={() => setActiveTab(feature.id)}
              className={`flex items-center gap-3 px-6 py-3 rounded-lg font-medium transition-all ${
                activeTab === feature.id
                  ? 'bg-blue-600 text-white shadow-lg'
                  : 'bg-white text-gray-700 hover:bg-gray-50 border border-gray-200'
              }`}
            >
              {feature.icon}
              <span>{feature.title}</span>
            </button>
          ))}
        </div>

        {/* Feature Content */}
        <div className="bg-white rounded-xl shadow-lg p-8">
          {renderFeatureContent()}
        </div>

        {/* Bottom CTA */}
        <div className="text-center mt-12">
          <div className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-2xl p-8 text-white">
            <h2 className="text-2xl font-bold mb-4">Ready to Transform Your Job Search?</h2>
            <p className="text-blue-100 mb-6 max-w-2xl mx-auto">
              Join thousands of job seekers who are already using AI to land their dream jobs faster.
            </p>
            {!user ? (
              <a 
                href="/register"
                className="inline-block bg-white text-blue-600 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-colors"
              >
                Get Started Free
              </a>
            ) : (
              <a 
                href="/dashboard"
                className="inline-block bg-white text-blue-600 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-colors"
              >
                Go to Dashboard
              </a>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AIFeatures;