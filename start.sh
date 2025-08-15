#!/bin/bash

# Start script for Railway deployment - Environment Aware
echo "🚀 Starting Mahiberawi Backend..."

# Check if JAR file exists
if [ -f "app.jar" ]; then
    echo "✅ Found app.jar"
    ls -la app.jar
else
    echo "❌ app.jar not found!"
    echo "Available files:"
    ls -la
    echo "Checking target directory:"
    ls -la target/ || echo "Target directory not found"
    exit 1
fi

# Environment-specific configuration
ENVIRONMENT=${RAILWAY_ENVIRONMENT:-production}
echo "🌍 Environment: $ENVIRONMENT"

# Set Java options based on environment
case $ENVIRONMENT in
    "development")
        echo "🔧 Development mode - Lower memory, debug enabled"
        export JAVA_OPTS="-Xmx256m -Xms128m -Dspring.profiles.active=development"
        ;;
    "staging")
        echo "🧪 Staging mode - Medium memory, production-like"
        export JAVA_OPTS="-Xmx512m -Xms256m -Dspring.profiles.active=staging"
        ;;
    "production"|"railway")
        echo "🚀 Production mode - Full memory, optimized"
        export JAVA_OPTS="-Xmx1024m -Xms512m -Dspring.profiles.active=production"
        ;;
    *)
        echo "⚠️  Unknown environment: $ENVIRONMENT, using production settings"
        export JAVA_OPTS="-Xmx512m -Xms256m -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-production}"
        ;;
esac

# Set Spring profile (can be overridden by environment variable)
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-production}

echo "☕ Java Options: $JAVA_OPTS"
echo "🌱 Spring Profile: $SPRING_PROFILES_ACTIVE"

# Start the application
echo "🚀 Starting Java application..."
exec java $JAVA_OPTS -jar app.jar 