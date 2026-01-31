// src/pages/Home.jsx
import React from 'react';
import { Link } from 'react-router-dom';
import { 
  Sparkles, Target, TrendingUp, Users, 
  FileText, Award, BarChart3, Zap 
} from 'lucide-react';

const Home = () => {
  const features = [
    {
      icon: <Sparkles className="w-8 h-8" />,
      title: 'AI-Powered Resume Parser',
      description: 'Upload your resume and let AI extract all your skills and experience automatically.',
      color: 'bg-purple-500'
    },
    {
      icon: <Target className="w-8 h-8" />,
      title: 'Smart Job Matching',
      description: 'Get personalized job recommendations based on your skills and experience.',
      color: 'bg-blue-500'
    },
    {
      icon: <TrendingUp className="w-8 h-8" />,
      title: 'Skill Gap Analysis',
      description: 'Know exactly what skills you need to learn for your dream job.',
      color: 'bg-green-500'
    },
    {
      icon: <FileText className="w-8 h-8" />,
      title: 'Interview Questions',
      description: 'Generate technical interview questions tailored to any job posting.',
      color: 'bg-orange-500'
    },
    {
      icon: <BarChart3 className="w-8 h-8" />,
      title: 'Analytics Dashboard',
      description: 'Track application success rates, skill demands, and market trends.',
      color: 'bg-pink-500'
    },
    {
      icon: <Zap className="w-8 h-8" />,
      title: 'Instant Applications',
      description: 'Apply to jobs with one click using your parsed resume data.',
      color: 'bg-yellow-500'
    }
  ];

  const stats = [
    { label: 'Active Jobs', value: '1,000+', icon: <Users /> },
    { label: 'Success Rate', value: '85%', icon: <Award /> },
    { label: 'AI Features', value: '5+', icon: <Sparkles /> },
    { label: 'Happy Users', value: '10K+', icon: <TrendingUp /> }
  ];

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <div className="relative bg-gradient-to-br from-primary-600 via-primary-700 to-primary-900 text-white overflow-hidden">
        <div className="absolute inset-0 bg-black opacity-10"></div>
        <div className="absolute inset-0">
          <div className="absolute top-0 left-0 w-96 h-96 bg-primary-400 rounded-full filter blur-3xl opacity-20 animate-pulse"></div>
          <div className="absolute bottom-0 right-0 w-96 h-96 bg-purple-400 rounded-full filter blur-3xl opacity-20 animate-pulse delay-1000"></div>
        </div>
        
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 md:py-32">
          <div className="text-center">
            <div className="inline-flex items-center gap-2 bg-white/10 backdrop-blur-sm px-4 py-2 rounded-full mb-6">
              <Sparkles className="w-5 h-5 text-yellow-300" />
              <span className="text-sm font-semibold">AI-Powered Job Portal</span>
            </div>
            
            <h1 className="text-5xl md:text-7xl font-bold mb-6 leading-tight">
              Find Your Dream Job
              <br />
              <span className="bg-gradient-to-r from-yellow-300 to-pink-300 bg-clip-text text-transparent">
                With AI Assistance
              </span>
            </h1>
            
            <p className="text-xl md:text-2xl mb-10 text-gray-100 max-w-3xl mx-auto">
              Leverage cutting-edge AI technology to match your skills with perfect job opportunities.
              Get personalized recommendations, skill insights, and career guidance.
            </p>
            
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link to="/register" className="btn-primary text-xl px-7 py-4 bg-white text-primary-600 hover:bg-gray-100">
                Get Started Free
              </Link>
              <Link to="/jobs" className="btn-secondary bg-orange-600/90 text-xl px-7 py-4 border-white text-white hover:bg-white/10">
                Browse Jobs
              </Link>
            </div>
          </div>

          {/* Stats */}
          <div className="mt-20 grid grid-cols-2 md:grid-cols-4 gap-8">
            {stats.map((stat, index) => (
              <div key={index} className="text-center">
                <div className="inline-flex items-center justify-center w-12 h-12 bg-white/10 backdrop-blur-sm rounded-full mb-3">
                  {React.cloneElement(stat.icon, { className: 'w-6 h-6' })}
                </div>
                <div className="text-3xl font-bold mb-1">{stat.value}</div>
                <div className="text-gray-200">{stat.label}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Features Section */}
      <div className="py-24 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold mb-4">
              Powered by <span className="text-primary-600">Artificial Intelligence</span>
            </h2>
            <p className="text-xl text-gray-600 max-w-2xl mx-auto">
              Experience the future of job hunting with our AI-driven features
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {features.map((feature, index) => (
              <div 
                key={index}
                className="group card hover:scale-105 transition-transform duration-300"
              >
                <div className={`${feature.color} w-16 h-16 rounded-2xl flex items-center justify-center mb-6 text-white group-hover:scale-110 transition-transform`}>
                  {feature.icon}
                </div>
                <h3 className="text-xl font-bold mb-3">{feature.title}</h3>
                <p className="text-gray-600">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* CTA Section */}
      <div className="bg-gradient-to-r from-primary-600 to-purple-600 text-white py-20">
        <div className="max-w-4xl mx-auto text-center px-4">
          <h2 className="text-4xl md:text-5xl font-bold mb-6">
            Ready to Transform Your Job Search?
          </h2>
          <p className="text-xl mb-8 text-gray-100">
            Join thousands of successful job seekers using AI to land their dream roles
          </p>
          <Link to="/register" className="btn-primary text-lg px-8 py-4 bg-white text-primary-600 hover:bg-gray-100">
            Start Your Journey
          </Link>
        </div>
      </div>

      {/* Footer */}
      <footer className="bg-gray-900 text-gray-400 py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <h3 className="text-2xl font-bold text-white mb-4">AI Job Portal</h3>
            <p className="mb-4">Built with Spring Boot & React</p>
            <p className="text-sm">© 2026 AI Job Portal. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Home;