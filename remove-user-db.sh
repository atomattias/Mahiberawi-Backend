#!/bin/bash

# Remove User from Database Script (Development)
# This script removes a user directly from the development database

echo "🗑️ Remove User from Database (Development)"
echo "=========================================="

# Get phone number
read -p "Enter the phone number to remove: " PHONE_NUMBER

if [ -z "$PHONE_NUMBER" ]; then
    echo "❌ Phone number is required"
    exit 1
fi

echo
echo "🗑️ Removing user with phone: $PHONE_NUMBER"
echo

# Check if Railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "❌ Railway CLI is not installed"
    echo "💡 Install it with: npm install -g @railway/cli"
    echo "💡 Then login with: railway login"
    exit 1
fi

echo "🔍 Checking Railway CLI status..."
railway status

echo
echo "🗑️ Connecting to development database..."
echo "💡 This will open a PostgreSQL shell where you can run SQL commands"
echo

echo "📝 SQL Commands to run:"
echo "1. List users: SELECT id, first_name, last_name, phone_number, email FROM users WHERE phone_number = '$PHONE_NUMBER';"
echo "2. Delete user: DELETE FROM users WHERE phone_number = '$PHONE_NUMBER';"
echo "3. Verify deletion: SELECT COUNT(*) FROM users WHERE phone_number = '$PHONE_NUMBER';"
echo

echo "🚀 Opening Railway database shell..."
echo "💡 Run the SQL commands above to remove the user"
echo "💡 Type '\\q' to exit the database shell"
echo

# Connect to the development database
railway connect --service mahiberawi-dev-db

echo
echo "🗑️ Database operation complete!"
echo "💡 Now you can test registration again with the same phone number"
