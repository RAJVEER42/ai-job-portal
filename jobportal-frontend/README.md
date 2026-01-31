# 🚀 Enterprise Job Portal - Frontend

A modern, enterprise-grade React job portal application with advanced features including performance monitoring, circuit breaker patterns, comprehensive error handling, and production-ready deployment infrastructure.

## ✨ Enterprise Features

### 🔧 Core Enterprise Architecture
- **Circuit Breaker Pattern** - Automatic service failure detection and recovery
- **Request Correlation IDs** - Distributed tracing for API calls
- **Retry Logic** - Exponential backoff with configurable retry attempts
- **Performance Monitoring** - Real-time response time tracking and cache detection
- **Error Boundaries** - Graceful error handling with user-friendly recovery
- **JWT Token Refresh** - Automatic authentication token management

### 📊 Advanced Monitoring & Analytics
- **System Health Dashboard** - Real-time component status monitoring
- **Cache Performance Tracking** - Visual cache hit rates and performance metrics
- **Email System Integration** - Notification system with delivery tracking
- **Admin Analytics** - Comprehensive administrative dashboard with tabbed interface
- **Request Performance Metrics** - API response time visualization

### 🔒 Security Features
- **CSRF Protection** - Cross-site request forgery prevention
- **Rate Limiting Awareness** - Intelligent handling of API rate limits
- **Secure Authentication Flow** - Enhanced login with automatic token refresh
- **Admin Role Management** - Role-based access control

### 🎨 Enhanced User Experience
- **Professional UI Components** - Modern, responsive design with Tailwind CSS
- **Custom Modal System** - Professional confirmation dialogs
- **Loading States** - Enhanced loading indicators and skeleton screens
- **Error Recovery** - User-friendly error messages with recovery options
- **Performance Indicators** - Visual feedback for system performance

## 🛠️ Tech Stack

- **React** 18
- **React Router** for navigation
- **Axios** for API calls
- **Tailwind CSS** for styling
- **Lucide React** for icons
- **Context API** for state management

## 📦 Installation

### Prerequisites

- Node.js 16+ and npm
- Backend API running on `http://localhost:8080`

### Setup Steps

```bash
# 1. Navigate to frontend directory
cd jobportal-frontend

# 2. Install dependencies
npm install

# 3. Start development server
npm start
```

The app will open at `http://localhost:3000`

## 🏗️ Project Structure

```
src/
├── components/
│   └── Navbar.jsx           # Navigation bar
├── pages/
│   ├── Home.jsx            # Landing page
│   ├── Login.jsx           # Login page
│   ├── Register.jsx        # Registration page
│   ├── Jobs.jsx            # Job listings
│   └── Dashboard.jsx       # User dashboard with AI features
├── services/
│   ├── api.js             # API client and endpoints
│   └── auth.js            # Authentication utilities
├── context/
│   └── AuthContext.jsx    # Authentication context
├── App.jsx                # Main app component
└── index.css              # Global styles
```

## 🎯 Key Features

### 1. Authentication
- User registration with role selection (Candidate/Recruiter)
- Secure JWT-based login
- Protected routes for authenticated users

### 2. Job Browsing
- View all active jobs
- Real-time search by title, company, or location
- Beautiful job cards with all details

### 3. AI Features Dashboard
- **Resume Parser**: Upload PDF/TXT and extract skills automatically
- **Job Recommendations**: AI-matched jobs with match scores
- **Analytics**: View platform statistics and trends
- **Skill Tracking**: See what skills are in demand

### 4. Modern UI/UX
- Gradient backgrounds and smooth animations
- Card-based layouts
- Responsive design for mobile/tablet/desktop
- Loading states and error handling

## 🔧 Configuration

### API Base URL

Update in `src/services/api.js`:

```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

### Environment Variables (Optional)

Create `.env` file:

```env
REACT_APP_API_URL=http://localhost:8080/api
```

## 🚀 Available Scripts

```bash
# Start development server
npm start

# Build for production
npm run build

# Run tests
npm test

# Eject (one-way operation)
npm run eject
```

## 📱 Pages Overview

### Home Page (`/`)
- Hero section with gradient background
- Feature showcase
- Statistics display
- Call-to-action buttons

### Jobs Page (`/jobs`)
- Search bar for filtering
- Grid layout of job cards
- Job details (location, type, experience)

### Login Page (`/login`)
- Email and password authentication
- Error handling
- Redirect to dashboard on success

### Register Page (`/register`)
- Full name, email, password
- Role selection (Candidate/Recruiter)
- Success message and redirect

### Dashboard (`/dashboard`)
Protected route with tabs:
- **Overview**: Quick stats and actions
- **Resume Parser**: Upload and parse resumes
- **Job Matches**: AI recommendations
- **Analytics**: Platform insights

## 🎨 Customization

### Colors

Edit `tailwind.config.js`:

```javascript
theme: {
  extend: {
    colors: {
      primary: {
        500: '#3b82f6', // Change your primary color
        600: '#2563eb',
        // ... other shades
      },
    },
  },
}
```

### Styling

All global styles in `src/index.css` using Tailwind utility classes.

## 🔐 Authentication Flow

1. User registers → Account created
2. User logs in → Receives JWT token
3. Token stored in localStorage
4. Token sent with every API request
5. Protected routes check for valid token

## 📊 API Integration

All API calls are in `src/services/api.js`:

- **Auth**: Register, Login
- **Jobs**: Get all, Search, Get by ID
- **Applications**: Create, Get user apps
- **AI**: Parse resume, Get recommendations, Analytics

## 🐛 Troubleshooting

### CORS Issues

Make sure backend allows CORS for `http://localhost:3000`:

```java
@CrossOrigin(origins = "http://localhost:3000")
```

### API Connection Failed

1. Check backend is running on port 8080
2. Verify API_BASE_URL in api.js
3. Check browser console for errors

### Build Errors

```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

## 🚢 Deployment

### Build for Production

```bash
npm run build
```

Creates optimized build in `build/` folder.

### Deploy Options

- **Vercel**: `vercel deploy`
- **Netlify**: Drag & drop `build/` folder
- **AWS S3**: Upload to S3 bucket
- **Firebase**: `firebase deploy`

### Update API URL for Production

In `api.js`, change to production URL:

```javascript
const API_BASE_URL = 'https://your-backend.up.railway.app/api';
```

## 📈 Performance

- Code splitting with React.lazy (can be added)
- Image optimization
- Minimized bundle size
- Fast page loads

## 🎯 Next Steps

- [ ] Add job details page
- [ ] Implement skill gap analysis UI
- [ ] Add interview questions generator
- [ ] Create recruiter-specific views
- [ ] Add application tracking
- [ ] Implement real-time notifications

## 📄 License

MIT License - feel free to use for your projects!

## 🤝 Contributing

Contributions welcome! Open an issue or submit a PR.

---

**Built with ❤️ using React & Tailwind CSS**