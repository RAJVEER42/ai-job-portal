#!/bin/bash

# ========================================
# JOBPORTAL PRODUCTION DEPLOYMENT SCRIPT
# Version: 2.0.0 - Enhanced Enterprise Edition
# ========================================

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="jobportal-backend"
VERSION="1.0.0"
DOCKER_COMPOSE_FILE="docker-compose.yml"
ENV_FILE=".env.production"
LOG_DIR="/var/log/jobportal"
BACKUP_DIR="/var/backups/jobportal"

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check if running as root or with sudo
    if [[ $EUID -eq 0 ]]; then
        log_error "Please don't run this script as root. Use a user with sudo privileges."
        exit 1
    fi
    
    # Check required tools
    local tools=("docker" "docker-compose" "curl" "jq")
    for tool in "${tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            log_error "$tool is required but not installed."
            exit 1
        fi
    done
    
    # Check Docker daemon
    if ! docker info &> /dev/null; then
        log_error "Docker daemon is not running."
        exit 1
    fi
    
    log_success "Prerequisites check passed"
}

setup_environment() {
    log_info "Setting up production environment..."
    
    # Create necessary directories
    sudo mkdir -p "$LOG_DIR" "$BACKUP_DIR"
    sudo chown -R 1001:1001 "$LOG_DIR" # Match Docker user
    
    # Create .env file if it doesn't exist
    if [[ ! -f "$ENV_FILE" ]]; then
        log_warning ".env.production file not found. Creating template..."
        cat > "$ENV_FILE" << 'EOF'
# Production Environment Variables for JobPortal Backend

# Database Configuration
POSTGRES_DB=jobportal_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=change_this_password

# Redis Configuration
REDIS_PASSWORD=change_this_redis_password

# Email Configuration (Required for production)
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM_EMAIL=noreply@yourcompany.com

# Security Configuration
JWT_SECRET=change_this_to_a_very_long_secure_secret_key_minimum_256_bits

# OpenAI Configuration (Optional)
OPENAI_API_KEY=your-openai-api-key

# Grafana Configuration
GRAFANA_PASSWORD=change_this_admin_password

# Performance Settings
DB_POOL_MAX_SIZE=50
REDIS_HOST=redis
CACHE_TYPE=redis

# Monitoring
METRICS_ENABLED=true
SWAGGER_UI_ENABLED=false
EOF
        log_warning "Please edit $ENV_FILE with your production values before continuing."
        read -p "Press Enter to continue after editing the file..."
    fi
    
    log_success "Environment setup completed"
}

validate_environment() {
    log_info "Validating environment variables..."
    
    source "$ENV_FILE"
    
    # Check critical variables
    local required_vars=("POSTGRES_PASSWORD" "REDIS_PASSWORD" "JWT_SECRET" "MAIL_USERNAME" "MAIL_PASSWORD")
    local missing_vars=()
    
    for var in "${required_vars[@]}"; do
        if [[ -z "${!var:-}" ]] || [[ "${!var}" == "change_this"* ]]; then
            missing_vars+=("$var")
        fi
    done
    
    if [[ ${#missing_vars[@]} -gt 0 ]]; then
        log_error "The following required variables are missing or have default values:"
        printf '%s\n' "${missing_vars[@]}"
        exit 1
    fi
    
    # Validate JWT secret length
    if [[ ${#JWT_SECRET} -lt 64 ]]; then
        log_error "JWT_SECRET must be at least 64 characters long for security."
        exit 1
    fi
    
    log_success "Environment validation passed"
}

backup_data() {
    log_info "Creating backup of existing data..."
    
    local backup_timestamp=$(date +"%Y%m%d_%H%M%S")
    local backup_path="$BACKUP_DIR/backup_$backup_timestamp"
    
    # Create backup directory
    sudo mkdir -p "$backup_path"
    
    # Backup database if running
    if docker-compose ps postgres | grep -q "Up"; then
        log_info "Backing up PostgreSQL database..."
        docker-compose exec -T postgres pg_dump -U postgres -d jobportal_db > "$backup_path/database.sql" || {
            log_warning "Database backup failed or no existing database found"
        }
    fi
    
    # Backup uploads if they exist
    if [[ -d "uploads" ]]; then
        log_info "Backing up uploads directory..."
        sudo cp -r uploads "$backup_path/" || log_warning "Failed to backup uploads"
    fi
    
    log_success "Backup completed: $backup_path"
}

build_application() {
    log_info "Building application..."
    
    # Clean previous build
    if [[ -d "target" ]]; then
        rm -rf target
    fi
    
    # Maven build with production profile
    ./mvnw clean package -DskipTests -Dmaven.test.skip=true
    
    if [[ ! -f "target/${APP_NAME}"-*.jar ]]; then
        log_error "Application build failed - JAR file not found"
        exit 1
    fi
    
    log_success "Application build completed"
}

deploy_infrastructure() {
    log_info "Deploying infrastructure with Docker Compose..."
    
    # Stop existing services gracefully
    if docker-compose ps -q | grep -q .; then
        log_info "Stopping existing services..."
        docker-compose down --timeout 30
    fi
    
    # Pull latest images
    docker-compose pull
    
    # Build and start services
    docker-compose --env-file "$ENV_FILE" up -d --build
    
    log_success "Infrastructure deployment started"
}

wait_for_services() {
    log_info "Waiting for services to become healthy..."
    
    local max_attempts=60
    local attempt=0
    
    while [[ $attempt -lt $max_attempts ]]; do
        if docker-compose ps | grep -q "healthy\|Up"; then
            local unhealthy=$(docker-compose ps --filter "health=unhealthy" -q)
            if [[ -z "$unhealthy" ]]; then
                log_success "All services are healthy"
                return 0
            fi
        fi
        
        log_info "Waiting for services to start... ($((attempt + 1))/$max_attempts)"
        sleep 5
        ((attempt++))
    done
    
    log_error "Services failed to become healthy within timeout"
    docker-compose logs --tail=50
    exit 1
}

run_health_checks() {
    log_info "Running comprehensive health checks..."
    
    # Application health check
    local app_url="http://localhost:8080"
    local health_endpoint="$app_url/actuator/health"
    
    if curl -f -s "$health_endpoint" > /dev/null; then
        log_success "Application health check passed"
    else
        log_error "Application health check failed"
        return 1
    fi
    
    # Database connectivity test
    if docker-compose exec -T postgres pg_isready -U postgres > /dev/null; then
        log_success "Database connectivity check passed"
    else
        log_error "Database connectivity check failed"
        return 1
    fi
    
    # Redis connectivity test
    if docker-compose exec -T redis redis-cli ping | grep -q "PONG"; then
        log_success "Redis connectivity check passed"
    else
        log_error "Redis connectivity check failed"
        return 1
    fi
    
    return 0
}

setup_monitoring() {
    log_info "Setting up monitoring and alerting..."
    
    # Check Prometheus
    if curl -f -s "http://localhost:9090/-/ready" > /dev/null; then
        log_success "Prometheus is ready"
    else
        log_warning "Prometheus not accessible"
    fi
    
    # Check Grafana
    if curl -f -s "http://localhost:3000/api/health" > /dev/null; then
        log_success "Grafana is ready"
    else
        log_warning "Grafana not accessible"
    fi
    
    log_info "Monitoring endpoints:"
    log_info "  - Application: http://localhost:8080"
    log_info "  - Health Check: http://localhost:8080/actuator/health"
    log_info "  - Metrics: http://localhost:8080/actuator/prometheus"
    log_info "  - API Docs: http://localhost:8080/swagger-ui.html"
    log_info "  - Prometheus: http://localhost:9090"
    log_info "  - Grafana: http://localhost:3000 (admin/admin)"
}

show_deployment_summary() {
    log_success "🚀 JobPortal Backend Production Deployment Completed!"
    echo
    echo "=========================================="
    echo "DEPLOYMENT SUMMARY"
    echo "=========================================="
    echo "Application: $APP_NAME v$VERSION"
    echo "Status: Running"
    echo "Environment: Production"
    echo "Deployment Time: $(date)"
    echo
    echo "Services Status:"
    docker-compose ps
    echo
    echo "Key URLs:"
    echo "  • Application: http://localhost:8080"
    echo "  • Health Check: http://localhost:8080/actuator/health"
    echo "  • API Documentation: http://localhost:8080/swagger-ui.html"
    echo "  • Metrics: http://localhost:8080/actuator/prometheus"
    echo "  • Prometheus: http://localhost:9090"
    echo "  • Grafana: http://localhost:3000"
    echo
    echo "Log Locations:"
    echo "  • Application Logs: $LOG_DIR"
    echo "  • Container Logs: docker-compose logs -f"
    echo
    echo "Management Commands:"
    echo "  • View logs: docker-compose logs -f"
    echo "  • Stop services: docker-compose down"
    echo "  • Restart: docker-compose restart"
    echo "  • Scale backend: docker-compose up -d --scale jobportal-backend=2"
    echo "=========================================="
}

# Main deployment process
main() {
    echo "=========================================="
    echo "JobPortal Backend Production Deployment"
    echo "Version: 2.0.0 Enterprise Edition"
    echo "=========================================="
    echo
    
    check_prerequisites
    setup_environment
    validate_environment
    backup_data
    build_application
    deploy_infrastructure
    wait_for_services
    
    if run_health_checks; then
        setup_monitoring
        show_deployment_summary
        log_success "Deployment completed successfully! 🎉"
    else
        log_error "Health checks failed. Please check the logs."
        docker-compose logs --tail=50
        exit 1
    fi
}

# Run main function
main "$@"
