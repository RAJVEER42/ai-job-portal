#!/bin/bash

# Job Portal Backend Startup Script
# This script ensures the application runs with Java 22 for compatibility

echo "🚀 Starting Job Portal Backend..."
echo "Using Java 22 for compatibility..."

# Set JAVA_HOME to Java 22
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-22.jdk/Contents/Home

# Check if port 8080 is in use and offer to stop it
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo "⚠️  Port 8080 is already in use"
    echo "Finding process using port 8080..."
    PROCESS_PID=$(lsof -Pi :8080 -sTCP:LISTEN -t)
    echo "Process ID: $PROCESS_PID"
    
    echo "Do you want to stop the existing process? (y/n)"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        echo "Stopping process $PROCESS_PID..."
        kill $PROCESS_PID
        sleep 2
        echo "✅ Process stopped"
    else
        echo "❌ Cannot start application while port 8080 is in use"
        echo "Please stop the existing process or configure a different port"
        exit 1
    fi
fi

# Check if the project is already built
if [ ! -f "target/jobportal-backend-0.0.1-SNAPSHOT.jar" ]; then
    echo "📦 Building the project..."
    mvn clean install -DskipTests
    if [ $? -ne 0 ]; then
        echo "❌ Build failed. Please check for compilation errors."
        exit 1
    fi
fi

echo "🏃 Running the application..."
echo "The application will be available at: http://localhost:8080"
echo "API Documentation (Swagger): http://localhost:8080/swagger-ui.html"
echo "Health Check: http://localhost:8080/actuator/health"

# Run the application
java -jar "target/jobportal-backend-0.0.1-SNAPSHOT.jar"
