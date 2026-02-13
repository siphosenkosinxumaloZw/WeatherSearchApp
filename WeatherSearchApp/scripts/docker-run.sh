#!/bin/bash

# Docker run script for Weather Search App
set -e

echo "🚀 Starting Weather Search App with Docker..."

# Check if API key is set
if [ -z "$OPENWEATHER_API_KEY" ]; then
    echo "❌ Error: OPENWEATHER_API_KEY environment variable is not set"
    echo "Please set it with: export OPENWEATHER_API_KEY=your_api_key_here"
    exit 1
fi

# Stop and remove existing container if it exists
if [ "$(docker ps -q -f name=weather-search-app)" ]; then
    echo "🛑 Stopping existing container..."
    docker stop weather-search-app
fi

if [ "$(docker ps -aq -f name=weather-search-app)" ]; then
    echo "🗑️  Removing existing container..."
    docker rm weather-search-app
fi

# Run the container
echo "🏃‍♂️ Starting new container..."
docker run -d \
    --name weather-search-app \
    -p 8080:8080 \
    -e OPENWEATHER_API_KEY="$OPENWEATHER_API_KEY" \
    -e SPRING_PROFILES_ACTIVE=docker \
    -v weather-data:/app/data \
    --restart unless-stopped \
    weather-search-app:latest

echo "✅ Container started successfully!"
echo ""
echo "🌐 Application is available at: http://localhost:8080"
echo "📊 H2 Console: http://localhost:8080/h2-console"
echo "📋 View logs: docker logs -f weather-search-app"
echo ""
echo "⏹️  To stop: docker stop weather-search-app"
echo "🗑️  To remove: docker rm weather-search-app"
