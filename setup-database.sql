-- Create OpenOLAT user and database
CREATE USER openolat WITH PASSWORD 'openolat';
CREATE DATABASE openolat;
GRANT ALL PRIVILEGES ON DATABASE openolat TO openolat;

-- For PostgreSQL 15+, also grant schema privileges
\c openolat
GRANT ALL ON SCHEMA public TO openolat;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO openolat;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO openolat;
