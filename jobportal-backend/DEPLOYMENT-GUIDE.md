# 🚀 JobPortal Backend - Enterprise Deployment Guide

## 📋 Deployment Summary

### 🎯 **What We've Built**

The JobPortal Backend has been enhanced with **enterprise-grade production capabilities**:

#### 🔐 **Security & Authentication**
- ✅ **JWT Authentication** with role-based access control
- ✅ **Advanced Rate Limiting** using Bucket4j token bucket algorithm
- ✅ **Security Headers** (HSTS, CSP, XSS Protection, Frame Options)
- ✅ **CORS Configuration** for secure cross-origin requests

#### ⚡ **Performance & Scalability**
- ✅ **Dual Cache System** (Caffeine for dev, Redis for production)
- ✅ **HikariCP Connection Pool** with optimized database settings
- ✅ **Async Processing** for non-blocking operations
- ✅ **Circuit Breaker Pattern** with Resilience4j
- ✅ **JVM Optimization** with G1GC and production tuning

#### 📊 **Monitoring & Observability**
- ✅ **Prometheus Metrics** with business and technical metrics
- ✅ **Comprehensive Health Checks** (DB, Cache, Email, External APIs)
- ✅ **Structured JSON Logging** with correlation IDs
- ✅ **Performance Monitoring** with custom metrics collection
- ✅ **Admin Dashboard** with system control endpoints

#### 📧 **Communication**
- ✅ **Multi-provider Email** (Gmail, SendGrid, AWS SES, Mailgun)
- ✅ **Professional Email Templates** with responsive design
- ✅ **Retry Mechanism** with exponential backoff
- ✅ **Email Metrics** tracking delivery success rates

#### 🐳 **DevOps & Deployment**
- ✅ **Docker Configuration** with multi-stage builds
- ✅ **Docker Compose** for complete development environment
- ✅ **Kubernetes Manifests** with HPA and health probes
- ✅ **Production Scripts** with automated validation
- ✅ **Nginx Configuration** with SSL and rate limiting

---

## 🚀 **Deployment Options**

### 1. **Quick Docker Deployment** (Recommended)

```bash
# Clone and deploy with Docker Compose
git clone <your-repo>
cd jobportal-backend
cp .env.production.example .env.production
# Edit .env.production with your values
./deploy-production-v2.sh
```

**Services Started:**
- Application: http://localhost:8080
- Prometheus: http://localhost:9090  
- Grafana: http://localhost:3000
- PostgreSQL: localhost:5432
- Redis: localhost:6379

### 2. **Kubernetes Production Deployment**

```bash
# Deploy to Kubernetes cluster
kubectl apply -f k8s/deployment.yaml

# Check deployment status
kubectl get pods -n jobportal
kubectl get services -n jobportal
```

**Features:**
- Auto-scaling (3-10 replicas)
- Rolling updates
- Health checks
- Persistent storage
- Load balancing

### 3. **Traditional Server Deployment**

```bash
# Manual deployment
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
export JWT_SECRET=your-secure-secret
./mvnw clean package
java -jar target/jobportal-backend-*.jar
```

---

## 📊 **Monitoring Dashboard**

### **Key Metrics Available:**

#### Business Metrics
- 📈 Total Users: `/actuator/prometheus` → `jobportal_users_total`
- 📈 Job Applications: `/actuator/prometheus` → `jobportal_applications_total`
- 📈 Active Sessions: `/actuator/prometheus` → `jobportal_users_active_sessions`

#### Performance Metrics
- ⚡ Response Times: `jobportal_request_duration_95p_5m`
- ⚡ Cache Hit Rate: `jobportal_cache_hit_rate_5m`
- ⚡ Email Success Rate: `jobportal_email_success_rate_5m`
- ⚡ JVM Memory Usage: `jobportal_jvm_memory_usage`

#### System Health
- 💚 Application Health: `/actuator/health`
- 💚 Database Status: Health indicator shows connection status
- 💚 Cache Status: Redis/Caffeine connectivity
- 💚 Email Service: SMTP configuration validation

---

## 🔧 **Configuration Management**

### **Environment Variables** (Production)

| Category | Variable | Description | Required |
|----------|----------|-------------|----------|
| **Database** | `POSTGRES_PASSWORD` | PostgreSQL password | ✅ |
| **Cache** | `REDIS_PASSWORD` | Redis password | ✅ |
| **Email** | `MAIL_USERNAME` | SMTP username | ✅ |
| **Email** | `MAIL_PASSWORD` | SMTP password | ✅ |
| **Security** | `JWT_SECRET` | JWT secret (64+ chars) | ✅ |
| **AI** | `OPENAI_API_KEY` | OpenAI API key | ❌ |

### **Feature Toggles**

```bash
# Cache Configuration
export CACHE_TYPE=redis                    # caffeine|redis
export CACHE_ENABLED=true

# Rate Limiting
export RATE_LIMIT_ENABLED=true
export RATE_LIMIT_RPM=60

# Monitoring
export METRICS_ENABLED=true
export SWAGGER_UI_ENABLED=false           # Disable in production

# Email
export MAIL_TEST_MODE=false               # Enable for testing
```

---

## 🛡️ **Security Features**

### **Rate Limiting**
| Endpoint Type | Limit | Burst |
|---------------|-------|-------|
| General API | 60/min | 10/10s |
| Authentication | 10/min | 5/10s |
| Search | 100/min | 20/10s |

### **Security Headers**
- **HSTS**: 1 year max age with subdomains
- **CSP**: Restrictive content security policy
- **XSS**: Cross-site scripting protection
- **Frame Options**: Clickjacking prevention

### **Circuit Breakers**
- **OpenAI API**: 50% failure rate threshold
- **Email Service**: 60% failure rate threshold  
- **Database**: 80% failure rate threshold

---

## 📱 **API Documentation**

### **Interactive Documentation**
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### **Admin Endpoints**
| Endpoint | Method | Description | Role Required |
|----------|--------|-------------|---------------|
| `/api/admin/health` | GET | System health | ADMIN |
| `/api/admin/metrics` | GET | Performance metrics | ADMIN |
| `/api/admin/cache/clear` | POST | Clear caches | ADMIN |
| `/api/admin/cache/stats` | GET | Cache statistics | ADMIN |

---

## 🚨 **Alerting & Monitoring**

### **Prometheus Alerts**

1. **High Error Rate** (>10% for 5min)
2. **High Response Time** (>2s 95th percentile)
3. **Low Cache Hit Rate** (<80% for 10min)
4. **High Memory Usage** (>90% JVM memory)
5. **Email Service Issues** (<50% success rate)

### **Health Check Endpoints**

```bash
# Application health
curl http://localhost:8080/actuator/health

# Kubernetes probes
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness

# Metrics
curl http://localhost:8080/actuator/prometheus
```

---

## 🔄 **Scaling & High Availability**

### **Horizontal Scaling**

#### Docker Compose Scaling
```bash
# Scale backend instances
docker-compose up -d --scale jobportal-backend=3

# Load balance with nginx
# Nginx automatically balances across instances
```

#### Kubernetes Auto-scaling
```yaml
# Automatic scaling 3-10 replicas
# CPU threshold: 70%
# Memory threshold: 80%
kubectl get hpa -n jobportal
```

### **Database High Availability**
- Master-slave replication
- Connection pooling (50 connections)
- Automatic failover support

### **Cache High Availability**
- Redis Cluster support
- Fallback to in-memory cache
- Cache warming strategies

---

## 📋 **Maintenance & Operations**

### **Backup Procedures**

```bash
# Automated backup (included in deployment script)
./deploy-production-v2.sh  # Creates backup before deployment

# Manual database backup
docker-compose exec postgres pg_dump -U postgres jobportal_db > backup.sql

# Manual file backup
cp -r uploads/ backup/uploads/
```

### **Log Management**

```bash
# Application logs
docker-compose logs -f jobportal-backend

# Structured JSON logs (production)
tail -f /var/log/jobportal/application.log | jq .

# Security audit logs
tail -f /var/log/jobportal/security-audit.log

# Performance logs  
tail -f /var/log/jobportal/performance.log
```

### **Performance Tuning**

#### JVM Tuning
```bash
# Production JVM settings
-Xmx2g -Xms1g
-XX:+UseG1GC 
-XX:MaxGCPauseMillis=200
-XX:+UnlockExperimentalVMOptions
-XX:+UseJVMCICompiler
```

#### Database Tuning
- Connection pool: 50 max connections
- Statement caching enabled
- Batch processing: 25 statements
- Connection timeout: 20s

---

## 🎯 **Next Steps**

### **Immediate Actions**
1. ✅ Set up production environment variables
2. ✅ Configure email provider credentials
3. ✅ Deploy with `./deploy-production-v2.sh`
4. ✅ Verify health checks pass
5. ✅ Test core functionality

### **Production Hardening**
1. 🔐 Set up SSL certificates
2. 🔐 Configure firewall rules
3. 📊 Set up Grafana dashboards
4. 🚨 Configure alert notifications
5. 💾 Schedule automated backups

### **Monitoring Setup**
1. 📈 Connect Grafana to Prometheus
2. 📧 Set up email alerts
3. 📱 Configure Slack/Teams notifications
4. 📊 Create custom dashboards
5. 🔍 Set up log aggregation

---

## 💡 **Pro Tips**

### **Performance Optimization**
- Use Redis for production caching
- Enable database connection pooling
- Configure async processing
- Monitor cache hit rates

### **Security Best Practices**
- Rotate JWT secrets regularly
- Use strong database passwords
- Enable rate limiting in production
- Monitor failed authentication attempts

### **Operational Excellence**
- Automate deployments
- Monitor key metrics continuously
- Set up proper alerting
- Maintain backup procedures
- Document operational procedures

---

## 🆘 **Support & Troubleshooting**

### **Common Issues**

#### High Memory Usage
```bash
# Check JVM memory
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Trigger GC (admin only)
curl -X POST http://localhost:8080/api/admin/system/gc
```

#### Cache Issues
```bash
# Check cache stats
curl http://localhost:8080/api/admin/cache/stats

# Clear cache (admin only)
curl -X POST http://localhost:8080/api/admin/cache/clear
```

#### Database Connection Issues
```bash
# Check connection pool
curl http://localhost:8080/actuator/health

# View active connections
docker-compose exec postgres psql -U postgres -c "SELECT * FROM pg_stat_activity;"
```

### **Support Channels**
- 📧 **Email**: support@jobportal.com
- 📚 **Documentation**: API documentation at `/swagger-ui.html`
- 🐛 **Bug Reports**: GitHub Issues
- 💬 **Community**: Discord/Slack

---

**🎉 Congratulations! Your JobPortal Backend is now production-ready with enterprise-grade capabilities!**
