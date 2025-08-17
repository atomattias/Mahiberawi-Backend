#!/bin/bash

# Quick Check User Script (Development)
# This script quickly checks if a user exists in the development database

echo "🔍 Quick Check User (Development)"
echo "================================="

# Database connection details from Railway TCP proxy
DB_HOST="shuttle.proxy.rlwy.net"
DB_PORT="53517"
DB_NAME="railway"
DB_USER="postgres"
DB_PASSWORD="MbcHIqnPaKmcbeJJBGYYLFnPEvGxDmTJ"

PHONE_NUMBER="+4791261801"

echo
echo "🔍 Checking if user exists with phone: $PHONE_NUMBER"
echo

# Set environment variables for psql
export PGPASSWORD="$DB_PASSWORD"

echo "📊 Querying database..."
echo

# Check if user exists
echo "1️⃣ Checking if user exists:"
USER_EXISTS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT id, first_name, last_name, phone_number, email, is_phone_verified FROM users WHERE phone_number = '$PHONE_NUMBER';")

if [ -z "$USER_EXISTS" ]; then
    echo "❌ User NOT FOUND with phone: $PHONE_NUMBER"
else
    echo "✅ User FOUND:"
    echo "$USER_EXISTS"
fi

echo

# Count users with this phone
echo "2️⃣ Counting users with this phone:"
USER_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM users WHERE phone_number = '$PHONE_NUMBER';")
echo "Count: $USER_COUNT"

echo

# List first 5 users
echo "3️⃣ First 5 users in database:"
FIRST_USERS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT id, first_name, last_name, phone_number, email FROM users LIMIT 5;")
echo "$FIRST_USERS"

echo
echo "🔍 User check complete!"
