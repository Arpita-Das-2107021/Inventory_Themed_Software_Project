-- Reset the PostgreSQL database by dropping and recreating the public schema.
-- WARNING: This will remove ALL data in the connected database.

DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

-- After running this, restart the application so Hibernate recreates tables and
-- `src/main/resources/data.sql` will run to seed initial data.
 