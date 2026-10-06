-- Initialize Cms Database
-- This script runs when the PostgreSQL container starts for the first time

-- Create additional schemas
CREATE SCHEMA IF NOT EXISTS cmsservice;

-- Default tenant schema pre-provisioned for SINGLE_TENANT mode and demo data
CREATE SCHEMA IF NOT EXISTS t_platform;
CREATE SCHEMA IF NOT EXISTS t_demo0001;
CREATE SCHEMA IF NOT EXISTS t_acme0001;

-- Set default search path
ALTER DATABASE cmsservice SET search_path TO t_platform, cmsservice, public;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE cmsservice TO svc_cms_dba;
GRANT ALL PRIVILEGES ON SCHEMA public TO svc_cms_dba;
GRANT ALL PRIVILEGES ON SCHEMA cmsservice TO svc_cms_dba;

-- Audit trigger function for tracking row updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

SELECT 'Cms Database initialized successfully' AS status;
