#!/bin/bash

# Start script for Railway deployment
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

# Set Java options
export JAVA_OPTS="-Xmx512m -Xms256m"

# Start the application
echo "☕ Starting Java application..."
exec java $JAVA_OPTS -jar app.jar 