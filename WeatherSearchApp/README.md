# Weather Search App

A comprehensive weather application that integrates with the OpenWeatherMap API to provide current weather data and forecasts for user-selected locations. The application features a Spring Boot backend with React frontend, complete with data persistence, CRUD operations, and automated synchronization.

## Features

### Core Functionality
- **API Integration**: Connects to OpenWeatherMap API for real-time weather data
- **Data Persistence**: Stores locations, weather snapshots, and user preferences in H2 database
- **CRUD Operations**: Full Create, Read, Update, Delete functionality for location management
- **Weather Data**: Current weather conditions and 5-day forecasts
- **User Interface**: Modern, responsive React-based web interface

### Advanced Features
- **Automated Sync**: Scheduled tasks for automatic weather data synchronization
- **Favorites System**: Mark locations as favorites for quick access
- **User Preferences**: Customizable units (Celsius/Fahrenheit, km/h/mph, etc.)
- **Auto-refresh**: Optional automatic data refresh at configurable intervals
- **Data Cleanup**: Automated cleanup of old weather data
- **Error Handling**: Comprehensive error handling with user-friendly messages
- **Search Functionality**: Search locations by name or display name

## Technology Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **Spring Data JPA** - Database abstraction
- **Spring Web** - REST API framework
- **OpenFeign** - HTTP client for API integration
- **H2 Database** - In-memory database
- **Maven** - Build tool

### Frontend
- **React 18** - UI framework
- **Tailwind CSS** - Styling framework
- **Font Awesome** - Icons
- **Babel Standalone** - Browser-based JSX transformation

### Testing
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing

## Architecture

### Backend Architecture
The application follows a layered architecture pattern:

```
┌─────────────────┐
│   Controllers   │ ← REST API endpoints
├─────────────────┤
│    Services     │ ← Business logic
├─────────────────┤
│  Repositories   │ ← Data access
├─────────────────┤
│    Entities     │ ← Database models
└─────────────────┘
```

### Database Schema
- **Locations**: Stores user-tracked cities with coordinates
- **Weather Snapshots**: Historical weather data with timestamps
- **User Preferences**: Settings for units and refresh intervals

### API Endpoints
- `GET /api/locations` - Get all locations
- `POST /api/locations` - Add new location
- `PUT /api/locations/{id}` - Update location
- `DELETE /api/locations/{id}` - Delete location
- `GET /api/weather/current/{locationId}` - Get current weather
- `POST /api/weather/sync/{locationId}` - Sync weather data
- `GET /api/weather/forecast/{locationId}` - Get 5-day forecast

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Docker and Docker Compose (for containerized deployment)
- OpenWeatherMap API key (free tier available)

### Option 1: Docker Deployment (Recommended)

#### Quick Start with Docker Compose
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd WeatherSearchApp
   ```

2. Copy environment file and set your API key:
   ```bash
   cp .env.example .env
   # Edit .env file and add your OpenWeatherMap API key
   ```

3. Start with Docker Compose:
   ```bash
   docker compose up -d
   ```

4. Access the application:
   - Web Interface: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console

#### Production Deployment with Docker Compose
```bash
# Start with PostgreSQL and Redis
docker compose --profile production up -d
```

#### Manual Docker Commands
1. Build the image:
   ```bash
   ./scripts/docker-build.sh
   ```

2. Run the container:
   ```bash
   ./scripts/docker-run.sh
   ```

### Option 2: Local Development

#### Get OpenWeatherMap API Key
1. Visit [OpenWeatherMap](https://openweathermap.org/api)
2. Sign up for a free account
3. Navigate to API keys section
4. Copy your API key

#### Configure Application
1. Set the API key as environment variable:
   ```bash
   export OPENWEATHER_API_KEY=your_api_key_here
   ```

   Alternatively, update `src/main/resources/application.properties`:
   ```properties
   weather.api.key=your_api_key_here
   ```

#### Build and Run
1. Build the application:
   ```bash
   mvn clean install
   ```

2. Run the application:
   ```bash
   mvn spring-boot:run
   ```

3. Access the application:
   - Web Interface: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console
   - API Documentation: http://localhost:8080/api

### 4. H2 Database Access
- **JDBC URL**: `jdbc:h2:mem:weatherdb`
- **Username**: `sa`
- **Password**: `password`

## Usage Guide

### Adding Locations
1. Click the "Add" button in the Locations panel
2. Enter city name and country code (e.g., "London" and "GB")
3. Click "Add Location"

### Viewing Weather Data
1. Click on any location in the list
2. Current weather data will display automatically
3. View 5-day forecast in the forecast section
4. Use the refresh button to update data

### Managing Locations
- **Favorite**: Click the star icon to mark as favorite
- **Sync**: Click the sync icon to update weather data
- **Delete**: Click the trash icon to remove location

### Customizing Preferences
1. Use the Settings panel to configure:
   - Temperature units (Celsius/Fahrenheit)
   - Auto-refresh toggle
   - Refresh interval

## Development

### Running Tests
```bash
mvn test
```

### Running Specific Tests
```bash
mvn test -Dtest=LocationServiceTest
```

### Project Structure
```
src/
├── main/
│   ├── java/com/weatherapp/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data transfer objects
│   │   ├── entity/         # Database entities
│   │   ├── repository/     # Data repositories
│   │   ├── service/        # Business logic
│   │   ├── client/         # External API clients
│   │   └── scheduler/      # Scheduled tasks
│   └── resources/
│       ├── static/         # Frontend assets
│       └── application.properties
└── test/                   # Test classes
```

## API Integration Details

### OpenWeatherMap API Usage
- **Current Weather**: `/weather` endpoint
- **5-Day Forecast**: `/forecast` endpoint
- **Rate Limits**: Free tier: 60 calls/minute, 1,000,000 calls/month
- **Error Handling**: Graceful handling of API limits and network failures

### Data Synchronization
- **Automatic Sync**: Every 6 hours via scheduled task
- **Manual Sync**: On-demand via refresh button
- **Conflict Resolution**: Latest API data takes precedence
- **Data Retention**: 30 days for historical data

## Testing Strategy

### Unit Tests
- Service layer business logic
- Repository data access
- Controller endpoint responses
- Error handling scenarios

### Test Coverage
- Location management CRUD operations
- Weather data synchronization
- API integration mocking
- Exception handling

## Docker Deployment

### Docker Configuration Files
- **Dockerfile**: Multi-stage build with Maven and OpenJDK
- **docker-compose.yml**: Development and production configurations
- **.dockerignore**: Optimizes build context
- **application-docker.properties**: Docker-specific configuration
- **application-prod.properties**: Production configuration

### Docker Features
- **Multi-stage build**: Optimized image size
- **Health checks**: Automated container monitoring
- **Volume mounting**: Persistent data storage
- **Environment variables**: Secure configuration
- **Production profiles**: PostgreSQL and Redis support
- **Security**: Non-root user execution

### Docker Commands
```bash
# Build and run with scripts
./scripts/docker-build.sh
./scripts/docker-run.sh

# Or use docker compose
docker compose up -d

# Production with PostgreSQL and Redis
docker compose --profile production up -d

# View logs
docker compose logs -f weather-app

# Stop containers
docker compose down
```

### Production Considerations
- Use PostgreSQL for persistent data storage
- Configure Redis for caching
- Set up proper monitoring and logging
- Configure environment-specific settings
- Use secrets management for API keys

## Deployment Considerations

### Production Configuration
1. Replace H2 with PostgreSQL or MySQL
2. Configure external database connection
3. Set up proper API key management
4. Configure logging levels
5. Set up monitoring and alerting

### Environment Variables
```bash
OPENWEATHER_API_KEY=your_production_api_key
SPRING_PROFILES_ACTIVE=prod
DB_HOST=your_database_host
DB_PORT=5432
DB_NAME=weatherapp
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

## Performance Optimizations

### Caching Strategy
- Weather data cached for 30 minutes
- Location data cached in memory
- API responses cached to reduce calls

### Database Optimization
- Indexed queries for location searches
- Efficient pagination for weather history
- Automated cleanup of old data

## Security Considerations

### API Security
- Input validation on all endpoints
- SQL injection prevention via JPA
- XSS protection in frontend
- CORS configuration for frontend access

### Data Protection
- No sensitive data stored in plain text
- API keys managed via environment variables
- Database access properly secured

## Troubleshooting

### Common Issues

**API Key Not Working**
- Verify API key is correctly set
- Check OpenWeatherMap account status
- Ensure API key is activated

**Database Connection Issues**
- Verify H2 console settings
- Check application.properties configuration
- Ensure proper JDBC URL format

**Frontend Not Loading**
- Check browser console for JavaScript errors
- Verify static resources are accessible
- Ensure React is loading properly

**Weather Data Not Updating**
- Check API rate limits
- Verify network connectivity
- Check application logs for errors

### Logging
Application logs provide detailed information:
```bash
# View logs in real-time
tail -f logs/application.log

# Check for specific errors
grep "ERROR" logs/application.log
```

## Contributing

### Code Style
- Follow Java naming conventions
- Use meaningful variable names
- Add comments for complex logic
- Maintain test coverage above 80%

### Git Workflow
1. Create feature branch from main
2. Implement changes with tests
3. Run full test suite
4. Submit pull request with description

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or support, please contact:
- Email: your-email@example.com
- GitHub: your-github-username

---

## Architectural Decisions

### Spring Boot Choice
**Rationale**: Spring Boot provides rapid development with convention over configuration, excellent integration with databases, and robust REST API capabilities.

### H2 Database
**Rationale**: H2 provides zero-configuration setup for development and demonstration. Production deployments should use PostgreSQL or MySQL.

### React with CDN
**Rationale**: Using React via CDN eliminates build complexity while providing a modern, responsive UI. For production, consider a proper build pipeline.

### OpenFeign Integration
**Rationale**: OpenFeign provides type-safe HTTP client integration with Spring, making API calls clean and maintainable.

### Scheduled Tasks
**Rationale**: Built-in Spring scheduling provides reliable background processing without external dependencies.

---

**Last Updated**: February 2026
**Version**: 1.0.0
