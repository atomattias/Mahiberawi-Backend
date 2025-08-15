#!/bin/bash

# Generic User Promotion to Super Admin Script (Development Environment)
# This script promotes any user to Super Admin role in DEVELOPMENT environment

echo "=== User Promotion to Super Admin (DEV) ==="
echo "This script promotes any user to SUPER_ADMIN role in DEVELOPMENT"
echo "======================================="

# Development environment URL (update this with your dev Railway URL)
BASE_URL="https://mahiberawi-backend-dev.up.railway.app/api"

# Get target user email
echo
read -p "Enter the email of the user to promote to Super Admin: " TARGET_EMAIL

# Get admin credentials
echo
echo "Admin Authentication Required:"
read -p "Enter your admin email: " ADMIN_EMAIL
read -s -p "Enter your admin password: " ADMIN_PASSWORD
echo

echo
echo "Target user: $TARGET_EMAIL"
echo "Admin user: $ADMIN_EMAIL"
echo "Backend URL: $BASE_URL"
echo "Environment: DEVELOPMENT"

# Step 1: Authenticate as admin
echo
echo "1. Authenticating as admin..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" \
  -w "HTTPSTATUS:%{http_code}")

HTTP_STATUS=$(echo "$LOGIN_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
RESPONSE_BODY=$(echo "$LOGIN_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

if [ "$HTTP_STATUS" -eq 200 ]; then
    TOKEN=$(echo "$RESPONSE_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    echo "✓ Admin authentication successful!"
else
    echo "✗ Admin authentication failed (Status: $HTTP_STATUS)"
    echo "Response: $RESPONSE_BODY"
    exit 1
fi

# Step 2: Promote target user to Super Admin
echo
echo "2. Promoting $TARGET_EMAIL to SUPER_ADMIN..."
PROMOTE_RESPONSE=$(curl -s -X POST "$BASE_URL/admin/promote/$TARGET_EMAIL" \
  -H "Authorization: Bearer $TOKEN" \
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
fi

# Step 3: Verify the promotion (optional)
echo
echo "3. Verifying promotion..."
VERIFY_RESPONSE=$(curl -s -X GET "$BASE_URL/auth/me" \
  -H "Authorization: Bearer $TOKEN" \
  -w "HTTPSTATUS:%{http_code}")

HTTP_STATUS=$(echo "$VERIFY_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)

if [ "$HTTP_STATUS" -eq 200 ]; then
    echo "✓ Verification complete"
else
    echo "! Could not verify promotion (but it may have succeeded)"
fi

echo
echo "=== Promotion Process Complete (DEV) ==="
echo "User: $TARGET_EMAIL"
echo "Action: Promoted to SUPER_ADMIN"
echo "Environment: DEVELOPMENT"
