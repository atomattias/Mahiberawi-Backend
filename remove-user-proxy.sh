#!/bin/bash

# User Removal via Railway TCP Proxy (Development)
# This script removes a user using Railway's TCP proxy

echo "🗑️ User Removal via Railway TCP Proxy (Development)"
echo "=================================================="

# Database connection details from Railway TCP proxy
DB_HOST="shuttle.proxy.rlwy.net"
DB_PORT="53517"
DB_NAME="railway"
DB_USER="postgres"
DB_PASSWORD="MbcHIqnPaKmcbeJJBGYYLFnPEvGxDmTJ"

# Get phone number
read -p "Enter the phone number to remove: " PHONE_NUMBER

if [ -z "$PHONE_NUMBER" ]; then
    echo "❌ Phone number is required"
    exit 1
fi

echo
echo "🗑️ Removing user with phone: $PHONE_NUMBER"
echo

# Check if psql is available
if ! command -v psql &> /dev/null; then
    echo "❌ PostgreSQL client (psql) is not installed"
    echo "💡 Install it with: sudo apt-get install postgresql-client"
    exit 1
fi

echo "🔍 Connecting to database via Railway TCP proxy..."
echo "Host: $DB_HOST"
echo "Port: $DB_PORT"
echo "Database: $DB_NAME"
echo "User: $DB_USER"
echo

# Set environment variables for psql
export PGPASSWORD="$DB_PASSWORD"

echo "📝 SQL Commands to run:"
echo "1. List users: SELECT id, first_name, last_name, phone_number, email FROM users WHERE phone_number = '$PHONE_NUMBER';"
echo "2. Delete user: DELETE FROM users WHERE phone_number = '$PHONE_NUMBER';"
echo "3. Verify deletion: SELECT COUNT(*) FROM users WHERE phone_number = '$PHONE_NUMBER';"
echo

echo "🚀 Opening PostgreSQL shell..."
echo "💡 Run the SQL commands above to remove the user"
echo "💡 Type '\\q' to exit the database shell"
echo

# Connect to the database via TCP proxy
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME"

echo
echo "🗑️ Database operation complete!"
echo "💡 Now you can test registration again with the same phone number"
