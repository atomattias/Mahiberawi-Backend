#!/bin/bash

# Check Database Tables Script (Development)
# This script checks what tables exist in the development database

echo "🔍 Check Database Tables (Development)"
echo "====================================="

# Database connection details from Railway TCP proxy
DB_HOST="shuttle.proxy.rlwy.net"
DB_PORT="53517"
DB_NAME="railway"
DB_USER="postgres"
DB_PASSWORD="MbcHIqnPaKmcbeJJBGYYLFnPEvGxDmTJ"

echo
echo "🔍 Checking what tables exist in the database..."
echo

# Set environment variables for psql
export PGPASSWORD="$DB_PASSWORD"

echo "📊 Querying database..."
echo

# List all tables
echo "1️⃣ All tables in the database:"
TABLES=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "\dt")
echo "$TABLES"

echo

# List all schemas
echo "2️⃣ All schemas in the database:"
SCHEMAS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "\dn")
echo "$SCHEMAS"

echo

# Check if any user-related tables exist
echo "3️⃣ Looking for user-related tables:"
USER_TABLES=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT table_name FROM information_schema.tables WHERE table_name LIKE '%user%' OR table_name LIKE '%auth%' OR table_name LIKE '%member%';")
echo "$USER_TABLES"

echo

# Count total tables
echo "4️⃣ Total number of tables:"
TABLE_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';")
echo "Total tables: $TABLE_COUNT"

echo
echo "🔍 Database check complete!"
