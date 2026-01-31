# Multi-stage Docker build for production-ready JobPortal Backend
FROM eclipse-temurin:17-jdk-alpine as builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for dependency caching)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Production stage
FROM eclipse-temurin:17-jre-alpine

# Install additional packages for production
RUN apk add --no-cache \
    curl \
    bash \
    tzdata

# Set timezone
ENV TZ=UTC

# Create application user for security
RUN addgroup -g 1001 -S jobportal && \
    adduser -S jobportal -u 1001 -G jobportal

# Create log directory
RUN mkdir -p /var/log/jobportal && \
    chown -R jobportal:jobportal /var/log/jobportal

# Set working directory
WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads && \
    chown -R jobportal:jobportal /app

# Switch to application user
USER jobportal

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for production
ENV JAVA_OPTS="-XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+UnlockExperimentalVMOptions \
               -XX:+UseJVMCICompiler \
               -Xmx1g \
               -Xms512m \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/var/log/jobportal/heap-dump.hprof \
               -Djava.security.egd=file:/dev/./urandom"

# Application configuration
ENV SPRING_PROFILES_ACTIVE=production
ENV SERVER_PORT=8080

# Expose port
EXPOSE 8080

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels for metadata
LABEL maintainer="JobPortal Team <dev@jobportal.com>"
LABEL version="1.0.0"
LABEL description="AI-Powered Job Portal Backend"
LABEL org.opencontainers.image.source="https://github.com/jobportal/backend"
