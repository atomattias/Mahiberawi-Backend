#!/bin/bash

# Generic Secure Super Admin Promotion Script
# This script promotes any user to Super Admin role using the secure promotion endpoint
# Works for any environment (dev, staging, production)

echo "=== Secure Super Admin Promotion Script ==="
echo "This script promotes any user to SUPER_ADMIN role using secure promotion"
echo "=========================================="

# Get environment
echo
echo "Select environment:"
echo "1. Development"
echo "2. Staging" 
echo "3. Production"
echo "4. Custom URL"
read -p "Enter choice (1-4): " ENV_CHOICE

case $ENV_CHOICE in
    1)
        BASE_URL="https://web-development-a36e.up.railway.app/api"
        ENV_NAME="DEVELOPMENT"
        ;;
    2)
        BASE_URL="https://mahiberawi-backend-staging.up.railway.app/api"
        ENV_NAME="STAGING"
        ;;
    3)
        BASE_URL="https://mahiberawi-backend-production.up.railway.app/api"
        ENV_NAME="PRODUCTION"
        ;;
    4)
        read -p "Enter custom base URL (e.g., https://your-app.up.railway.app/api): " BASE_URL
        ENV_NAME="CUSTOM"
        ;;
    *)
        echo "Invalid choice. Using Development environment."
        BASE_URL="https://web-development-a36e.up.railway.app/api"
        ENV_NAME="DEVELOPMENT"
        ;;
esac

# Get target user identifier
echo
echo "Enter the user identifier:"
echo "1. Email address"
echo "2. Phone number"
read -p "Enter choice (1-2): " IDENTIFIER_CHOICE

case $IDENTIFIER_CHOICE in
    1)
        read -p "Enter the email of the user to promote to Super Admin: " TARGET_EMAIL
        # Validate email format
        if [[ ! "$TARGET_EMAIL" =~ ^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$ ]]; then
            echo "✗ Invalid email format. Please enter a valid email address."
            exit 1
        fi
        USER_IDENTIFIER="$TARGET_EMAIL"
        IDENTIFIER_TYPE="email"
        ;;
    2)
        read -p "Enter the phone number of the user to promote to Super Admin: " TARGET_PHONE
        # Basic phone number validation (allows + and digits)
        if [[ ! "$TARGET_PHONE" =~ ^\+[0-9]+$ ]]; then
            echo "✗ Invalid phone number format. Please enter a valid phone number (e.g., +4791261801)."
            exit 1
        fi
        USER_IDENTIFIER="$TARGET_PHONE"
        IDENTIFIER_TYPE="phone"
        ;;
    *)
        echo "✗ Invalid choice. Please select 1 or 2."
        exit 1
        ;;
esac

# Get promotion key
echo
echo "Enter the promotion key (set in SUPER_ADMIN_PROMOTION_KEY environment variable):"
read -s -p "Promotion Key: " PROMOTION_KEY
echo

if [ -z "$PROMOTION_KEY" ]; then
    echo "✗ Promotion key cannot be empty."
    exit 1
fi

echo
echo "=== Promotion Details ==="
echo "Target user: $USER_IDENTIFIER ($IDENTIFIER_TYPE)"
echo "Environment: $ENV_NAME"
echo "Backend URL: $BASE_URL"
echo "========================="

# Confirm action
echo
read -p "Are you sure you want to promote $USER_IDENTIFIER to Super Admin? (y/N): " CONFIRM

if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
    echo "Promotion cancelled."
    exit 0
fi

# Attempt secure promotion to Super Admin
echo
echo "Attempting secure promotion to Super Admin..."
# Use the same endpoint for both email and phone number
PROMOTE_RESPONSE=$(curl -s -X POST "$BASE_URL/admin/secure-promote-super-admin/$USER_IDENTIFIER?promotionKey=$PROMOTION_KEY" \
  -H "Content-Type: application/json" \
  -w "HTTPSTATUS:%{http_code}")

HTTP_STATUS=$(echo "$PROMOTE_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
RESPONSE_BODY=$(echo "$PROMOTE_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

if [ "$HTTP_STATUS" -eq 200 ]; then
    echo "✓ SUCCESS - User promoted to Super Admin!"
    echo "Status Code: $HTTP_STATUS"
    echo "Response: $RESPONSE_BODY"
else
    echo "✗ FAILED - User promotion failed (Status: $HTTP_STATUS)"
    echo "Response: $RESPONSE_BODY"
    echo
    echo "Possible issues:"
    echo "- Promotion key not set in environment variables"
    echo "- Invalid promotion key"
    echo "- User not found"
    echo "- Network connectivity issues"
fi

echo
echo "=== Secure Super Admin promotion process complete ==="
echo "User: $USER_IDENTIFIER ($IDENTIFIER_TYPE)"
echo "Action: Promoted to SUPER_ADMIN"
echo "Environment: $ENV_NAME"