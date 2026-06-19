-- Initialize Servicename Database
-- This script runs when the PostgreSQL container starts for the first time

-- Create additional schemas
CREATE SCHEMA IF NOT EXISTS servicename;

-- Set default search path
ALTER DATABASE servicename SET search_path TO public, servicename;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE servicename TO svc_servicename_dba;
GRANT ALL PRIVILEGES ON SCHEMA public TO svc_servicename_dba;
GRANT ALL PRIVILEGES ON SCHEMA servicename TO svc_servicename_dba;

-- Audit trigger function for tracking row updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

SELECT 'Servicename Database initialized successfully' AS status;
