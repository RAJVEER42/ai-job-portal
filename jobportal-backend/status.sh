#!/bin/bash

# Job Portal Backend Management Script

echo "🔍 Job Portal Backend Status Checker"
echo "===================================="

# Check if the application is running
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    PROCESS_PID=$(lsof -Pi :8080 -sTCP:LISTEN -t)
    echo "✅ Application is RUNNING (PID: $PROCESS_PID)"
    echo ""
    
    # Test health endpoint
    echo "🏥 Health Check:"
    curl -s http://localhost:8080/actuator/health | python3 -m json.tool 2>/dev/null || echo "Health endpoint not responding"
    echo ""
    
    echo "🌐 Application URLs:"
    echo "   Main Application: http://localhost:8080"
    echo "   API Documentation: http://localhost:8080/swagger-ui.html"
    echo "   Health Check: http://localhost:8080/actuator/health"
    echo "   Application Info: http://localhost:8080/actuator/info"
    echo ""
    
    echo "🛑 To stop the application:"
    echo "   kill $PROCESS_PID"
    echo ""
    
else
    echo "❌ Application is NOT running"
    echo ""
    echo "🚀 To start the application:"
    echo "   JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-22.jdk/Contents/Home java -jar \"target/jobportal-backend-0.0.1-SNAPSHOT.jar\""
    echo ""
fi

# Check Java version being used
echo "☕ Java Environment:"
if [ -n "$JAVA_HOME" ]; then
    echo "   JAVA_HOME: $JAVA_HOME"
    $JAVA_HOME/bin/java -version 2>&1 | head -1
else
    echo "   Using system Java:"
    java -version 2>&1 | head -1
fi
echo ""

# Show recent logs if application is running
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo "📋 Recent Application Activity:"
    echo "   (Check terminal where application was started for full logs)"
fi
