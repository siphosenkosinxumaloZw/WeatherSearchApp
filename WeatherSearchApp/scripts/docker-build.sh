#!/bin/bash

# Docker build script for Weather Search App
set -e

echo "🏗️  Building Weather Search App Docker image..."

# Check if API key is set
if [ -z "$OPENWEATHER_API_KEY" ]; then
    echo "❌ Error: OPENWEATHER_API_KEY environment variable is not set"
    echo "Please set it with: export OPENWEATHER_API_KEY=your_api_key_here"
    exit 1
fi

# Build the Docker image
docker build -t weather-search-app:latest .

echo "✅ Docker image built successfully!"
echo ""
echo "🚀 To run the application:"
echo "docker run -d -p 8080:8080 -e OPENWEATHER_API_KEY=\$OPENWEATHER_API_KEY weather-search-app:latest"
echo ""
echo "🐳 Or use docker compose:"
echo "docker compose up -d"
echo ""
echo "🌐 Application will be available at: http://localhost:8080"
