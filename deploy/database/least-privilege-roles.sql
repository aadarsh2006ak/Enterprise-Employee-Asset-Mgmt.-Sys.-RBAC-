-- ==============================================================================
-- Enterprise PostgreSQL 15 Least-Privilege Role Separation Architecture
-- ==============================================================================

-- 1. Database Migrator Role (Used strictly during Flyway CI/CD Migration Execution)
CREATE ROLE eams_migrator WITH
    LOGIN
    PASSWORD '<SECURE_MIGRATOR_PASSWORD>'
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE;

GRANT ALL PRIVILEGES ON DATABASE eams TO eams_migrator;
GRANT ALL PRIVILEGES ON SCHEMA public TO eams_migrator;

-- 2. Application Runtime Role (Used exclusively by Spring Boot backend in Production)
CREATE ROLE eams_app_user WITH
    LOGIN
    PASSWORD '<SECURE_APP_PASSWORD>'
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE;

-- Grant database connection
GRANT CONNECT ON DATABASE eams TO eams_app_user;
GRANT USAGE ON SCHEMA public TO eams_app_user;

-- Grant Data Manipulation Only (DML) - NO DROP, NO ALTER, NO TRUNCATE
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO eams_app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO eams_app_user;

-- Ensure future tables created by Flyway migrations automatically grant DML only to app user
ALTER DEFAULT PRIVILEGES FOR ROLE eams_migrator IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO eams_app_user;

ALTER DEFAULT PRIVILEGES FOR ROLE eams_migrator IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO eams_app_user;

-- Revoke all administrative privileges from Application User
REVOKE CREATE ON SCHEMA public FROM eams_app_user;
REVOKE ALL ON SCHEMA public FROM PUBLIC;
