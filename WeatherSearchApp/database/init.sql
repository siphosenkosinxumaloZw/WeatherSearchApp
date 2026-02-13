-- PostgreSQL initialization script for production
-- This script is used when running with PostgreSQL in Docker

-- Create database if it doesn't exist
-- Note: This is handled by POSTGRES_DB environment variable in docker-compose.yml

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create indexes for better performance
-- These will be created by Hibernate, but we can optimize them manually if needed

-- Example manual index creation (uncomment if needed)
-- CREATE INDEX IF NOT EXISTS idx_locations_city_name ON locations(city_name);
-- CREATE INDEX IF NOT EXISTS idx_locations_country_code ON locations(country_code);
-- CREATE INDEX IF NOT EXISTS idx_locations_is_favorite ON locations(is_favorite);
-- CREATE INDEX IF NOT EXISTS idx_weather_snapshots_location_id ON weather_snapshots(location_id);
-- CREATE INDEX IF NOT EXISTS idx_weather_snapshots_timestamp ON weather_snapshots(timestamp);

-- Set up proper permissions
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO weatheruser;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO weatheruser;
