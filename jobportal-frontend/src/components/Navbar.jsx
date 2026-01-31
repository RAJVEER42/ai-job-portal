// src/components/Navbar.jsx
import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Menu,
  X,
  Sparkles,
  Briefcase,
  LayoutDashboard,
  LogOut,
  User,
  BarChart3,
  FileText,
  Shield,
} from 'lucide-react';

const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const { user, logout } = useAuth();
  const location = useLocation();

  const isActive = (path) => location.pathname === path;

  const navLinks = [
    { path: '/jobs', label: 'Browse Jobs', icon: <Briefcase className="w-5 h-5" /> },
  ];

  const userLinks = user
    ? [
        { path: '/dashboard', label: 'Dashboard', icon: <LayoutDashboard className="w-4 h-4" /> },
        { path: '/applications', label: 'My Applications', icon: <FileText className="w-4 h-4" /> },
        { path: '/analytics', label: 'Analytics', icon: <BarChart3 className="w-4 h-4" /> },
        { path: '/profile', label: 'Profile', icon: <User className="w-4 h-4" /> },
        // Admin link - only show if user has admin role
        ...(user?.role === 'admin' ? [
          { path: '/admin', label: 'Admin Dashboard', icon: <Shield className="w-4 h-4" /> }
        ] : []),
      ]
    : [];

  return (
    <nav className="sticky top-0 z-50 backdrop-blur-2xl bg-white/40 border-b border-black/0 shadow-[0_1px_0_rgba(0,0,0,0.04)]">
      <div className="max-w-7xl mx-auto px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-black flex items-center justify-center">
              <Sparkles className="w-5 h-5 text-white" />
            </div>
            <span className="text-[17px] font-semibold tracking-tight text-gray-900">
              AI JobPortal
            </span>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center gap-6">
            {[...navLinks, ...userLinks].map((link) => (
              <Link
                key={link.path}
                to={link.path}
                className={`flex items-center gap-2 px-4 py-2 rounded-xl text-[14px] font-medium transition ${
                  isActive(link.path)
                    ? 'bg-black/10 text-gray-900'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-black/10'
                }`}
              >
                {link.icon}
                {link.label}
              </Link>
            ))}

            {user ? (
              <div className="flex items-center gap-3">
                <div className="flex items-center gap-2 px-4 py-2 rounded-full bg-black/5 border border-black/10">
                  <User className="w-4 h-4 text-gray-600" />
                  <span className="text-[13px] font-medium text-gray-800">
                    {user.fullName}
                  </span>
                </div>

                <button
                  onClick={logout}
                  className="px-4 py-2 rounded-xl text-[14px] text-red-500 hover:bg-red-500/10 transition"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-4">
                <Link
                  to="/login"
                  className="text-[15px] px-5 py-2 rounded-2xl font-medium text-gray-600 hover:text-gray-900 hover:bg-black/10"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="px-6 py-2 rounded-full bg-black text-white text-[14px] font-medium hover:bg-black/90 transition"
                >
                  Get Started
                </Link>
              </div>
            )}
          </div>

          {/* Mobile menu button */}
          <button
            onClick={() => setIsOpen(!isOpen)}
            className="md:hidden text-gray-700"
          >
            {isOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>
      </div>

      {/* Mobile Navigation */}
      {isOpen && (
        <div className="md:hidden backdrop-blur-2xl bg-white/80 border-t border-black/10">
          <div className="px-6 py-4 space-y-2">
            {[...navLinks, ...userLinks].map((link) => (
              <Link
                key={link.path}
                to={link.path}
                onClick={() => setIsOpen(false)}
                className={`flex items-center gap-2 px-4 py-3 rounded-xl text-[14px] font-medium ${
                  isActive(link.path)
                    ? 'bg-black/10 text-gray-900'
                    : 'text-gray-600 hover:bg-black/10'
                }`}
              >
                {link.icon}
                {link.label}
              </Link>
            ))}

            {user ? (
              <>
                <div className="px-4 py-3 rounded-xl bg-black/5 border border-black/10">
                  <p className="text-sm font-medium text-gray-900">{user.fullName}</p>
                  <p className="text-xs text-gray-500">{user.email}</p>
                </div>
                <button
                  onClick={() => {
                    logout();
                    setIsOpen(false);
                  }}
                  className="w-full px-4 py-3 rounded-xl text-[14px] text-red-500 hover:bg-red-500/10 transition"
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  onClick={() => setIsOpen(false)}
                  className="block px-4 py-3 rounded-xl text-gray-600 hover:bg-black/10 font-medium"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  onClick={() => setIsOpen(false)}
                  className="block text-center px-6 py-3 rounded-full bg-black text-white text-[14px] font-medium hover:bg-black/90"
                >
                  Get Started
                </Link>
              </>
            )}
          </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;