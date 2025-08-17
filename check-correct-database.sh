#!/bin/bash

# Check Correct Database Script (Development)
# This script checks the database that the application is actually using

echo "🔍 Check Correct Database (Development)"
echo "======================================"

echo
echo "🔍 The application logs show tables exist, but our direct connection shows 0 tables."
echo "This suggests we might be connecting to the wrong database or schema."
echo

# Database connection details from Railway TCP proxy
DB_HOST="shuttle.proxy.rlwy.net"
DB_PORT="53517"
DB_NAME="railway"
DB_USER="postgres"
DB_PASSWORD="MbcHIqnPaKmcbeJJBGYYLFnPEvGxDmTJ"

# Set environment variables for psql
export PGPASSWORD="$DB_PASSWORD"

echo "📊 Checking current database connection..."
echo

# Check current database
echo "1️⃣ Current database:"
CURRENT_DB=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT current_database();")
echo "Database: $CURRENT_DB"

echo

# Check current schema
echo "2️⃣ Current schema:"
CURRENT_SCHEMA=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT current_schema();")
echo "Schema: $CURRENT_SCHEMA"

echo

# List all databases
echo "3️⃣ All databases:"
ALL_DBS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "\l")
echo "$ALL_DBS"

echo

# Check if we're in the right database
echo "4️⃣ Checking if 'users' table exists in current database:"
USERS_EXISTS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'users');")
echo "Users table exists: $USERS_EXISTS"

echo

# Check all schemas in current database
echo "5️⃣ All schemas in current database:"
ALL_SCHEMAS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "\dn")
echo "$ALL_SCHEMAS"

echo
echo "🔍 Database check complete!"
