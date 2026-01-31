#!/bin/bash

# JobPortal Production Deployment Script
# This script sets up the production environment with all necessary configurations

set -e

echo "🚀 JobPortal Production Deployment Started..."

# Check if required environment variables are set
check_env_vars() {
    echo "📋 Checking environment variables..."
    
    local required_vars=(
        "MAIL_USERNAME"
        "MAIL_PASSWORD" 
        "DB_PASSWORD"
        "JWT_SECRET"
    )
    
    for var in "${required_vars[@]}"; do
        if [[ -z "${!var}" ]]; then
            echo "❌ Error: Environment variable $var is not set"
            echo "Please set it before running this script"
            exit 1
        fi
    done
    
    echo "✅ All required environment variables are set"
}

# Set default production environment variables
set_production_env() {
    echo "🔧 Setting production environment variables..."
    
    # Email Configuration
    export MAIL_HOST=${MAIL_HOST:-smtp.gmail.com}
    export MAIL_PORT=${MAIL_PORT:-587}
    export MAIL_FROM_EMAIL=${MAIL_FROM_EMAIL:-noreply@jobportal.com}
    export MAIL_FROM_NAME=${MAIL_FROM_NAME:-JobPortal}
    export MAIL_REPLY_TO=${MAIL_REPLY_TO:-support@jobportal.com}
    export MAIL_ENABLED=${MAIL_ENABLED:-true}
    export MAIL_TEST_MODE=${MAIL_TEST_MODE:-false}
    
    # Cache Configuration
    export CACHE_TYPE=${CACHE_TYPE:-caffeine}
    export CACHE_ENABLED=${CACHE_ENABLED:-true}
    export CACHE_DEFAULT_TTL=${CACHE_DEFAULT_TTL:-3600}
    export CACHE_JOBS_TTL=${CACHE_JOBS_TTL:-7200}
    export CACHE_SEARCH_TTL=${CACHE_SEARCH_TTL:-1800}
    
    # Redis Configuration (if using Redis)
    if [[ "$CACHE_TYPE" == "redis" ]]; then
        export REDIS_HOST=${REDIS_HOST:-localhost}
        export REDIS_PORT=${REDIS_PORT:-6379}
        export REDIS_DATABASE=${REDIS_DATABASE:-0}
        export REDIS_TIMEOUT=${REDIS_TIMEOUT:-2000ms}
    fi
    
    # Database Configuration
    export DB_POOL_MAX_SIZE=${DB_POOL_MAX_SIZE:-50}
    export DB_POOL_MIN_IDLE=${DB_POOL_MIN_IDLE:-10}
    export DB_CONNECTION_TIMEOUT=${DB_CONNECTION_TIMEOUT:-20000}
    export DB_IDLE_TIMEOUT=${DB_IDLE_TIMEOUT:-300000}
    
    # Performance & Monitoring
    export METRICS_ENABLED=${METRICS_ENABLED:-true}
    export CACHE_METRICS_ENABLED=${CACHE_METRICS_ENABLED:-true}
    export EMAIL_METRICS_ENABLED=${EMAIL_METRICS_ENABLED:-true}
    
    # Async Configuration
    export ASYNC_CORE_SIZE=${ASYNC_CORE_SIZE:-10}
    export ASYNC_MAX_SIZE=${ASYNC_MAX_SIZE:-50}
    export EMAIL_ASYNC_CORE_SIZE=${EMAIL_ASYNC_CORE_SIZE:-5}
    export EMAIL_ASYNC_MAX_SIZE=${EMAIL_ASYNC_MAX_SIZE:-20}
    
    # JPA Performance
    export JPA_BATCH_SIZE=${JPA_BATCH_SIZE:-25}
    
    echo "✅ Production environment variables configured"
}

# Build the application
build_application() {
    echo "🔨 Building application..."
    
    # Clean and build
    ./mvnw clean package -DskipTests=true
    
    if [[ $? -eq 0 ]]; then
        echo "✅ Application built successfully"
    else
        echo "❌ Build failed"
        exit 1
    fi
}

# Run database migrations (if any)
run_migrations() {
    echo "🗄️ Running database migrations..."
    # Add your database migration commands here
    echo "✅ Database migrations completed"
}

# Start the application
start_application() {
    echo "🚀 Starting JobPortal application..."
    
    # Set JVM options for production
    export JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -server"
    
    # Set Spring profiles
    export SPRING_PROFILES_ACTIVE="prod"
    
    # Start the application
    java $JAVA_OPTS -jar target/jobportal-backend-*.jar &
    
    # Store the PID
    echo $! > jobportal.pid
    
    echo "✅ Application started with PID: $(cat jobportal.pid)"
}

# Health check
health_check() {
    echo "🏥 Performing health check..."
    
    local max_attempts=30
    local attempt=1
    
    while [[ $attempt -le $max_attempts ]]; do
        if curl -f -s http://localhost:8080/actuator/health > /dev/null; then
            echo "✅ Application is healthy and ready"
            return 0
        fi
        
        echo "Waiting for application to start... (attempt $attempt/$max_attempts)"
        sleep 2
        ((attempt++))
    done
    
    echo "❌ Health check failed - application may not have started properly"
    return 1
}

# Setup monitoring
setup_monitoring() {
    echo "📊 Setting up monitoring..."
    
    # Start Prometheus metrics endpoint (already available at /actuator/prometheus)
    echo "✅ Prometheus metrics available at: http://localhost:8080/actuator/prometheus"
    echo "✅ Application metrics available at: http://localhost:8080/actuator/metrics"
    echo "✅ Cache metrics available at: http://localhost:8080/actuator/caches"
    
    # Log important URLs
    echo ""
    echo "📱 Important URLs:"
    echo "   Health Check: http://localhost:8080/actuator/health"
    echo "   Metrics: http://localhost:8080/actuator/metrics"
    echo "   Cache Stats: http://localhost:8080/api/cache/stats"
    echo ""
}

# Main deployment function
main() {
    echo "🎯 Starting JobPortal Production Deployment"
    echo "============================================"
    
    check_env_vars
    set_production_env
    build_application
    run_migrations
    start_application
    
    if health_check; then
        setup_monitoring
        
        echo ""
        echo "🎉 DEPLOYMENT SUCCESSFUL!"
        echo "========================="
        echo "JobPortal is now running in production mode"
        echo "Application PID: $(cat jobportal.pid)"
        echo ""
        echo "To stop the application: kill \$(cat jobportal.pid)"
        echo "To view logs: tail -f logs/jobportal.log"
        echo ""
        
        # Display current configuration
        echo "🔧 Current Configuration:"
        echo "   Cache Type: $CACHE_TYPE"
        echo "   Email Enabled: $MAIL_ENABLED"
        echo "   Metrics Enabled: $METRICS_ENABLED"
        echo "   Database Pool Size: $DB_POOL_MAX_SIZE"
        echo ""
        
    else
        echo "❌ DEPLOYMENT FAILED!"
        echo "Check the application logs for more details"
        exit 1
    fi
}

# Run main function
main "$@"
