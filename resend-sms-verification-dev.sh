#!/bin/bash

# Resend SMS Verification Script (Development)
# This script resends SMS verification for existing users

echo "📱 Resend SMS Verification (Development)"
echo "========================================"

# Development environment URL
DEV_URL="https://web-development-a36e.up.railway.app/api"

echo
echo "Development URL: $DEV_URL"
echo

# Get phone number
read -p "Enter the phone number to resend SMS verification: " PHONE_NUMBER

if [ -z "$PHONE_NUMBER" ]; then
    echo "❌ Phone number is required"
    exit 1
fi

echo
echo "📱 Resending SMS verification for: $PHONE_NUMBER"
echo

# Resend SMS verification
echo "🔄 Sending SMS verification request..."
RESEND_RESPONSE=$(curl -s -X POST "$DEV_URL/auth/phone/resend-verification" \
  -H "Content-Type: application/json" \
  -d "{\"phoneNumber\":\"$PHONE_NUMBER\"}" \
  -w "HTTPSTATUS:%{http_code}")

HTTP_STATUS=$(echo "$RESEND_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
RESPONSE_BODY=$(echo "$RESEND_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

echo "Status Code: $HTTP_STATUS"
echo "Response: $RESPONSE_BODY"
echo

if [ "$HTTP_STATUS" -eq 200 ]; then
    echo "✅ SMS verification sent successfully!"
    echo "📱 Check your phone for the verification code"
elif [ "$HTTP_STATUS" -eq 404 ]; then
    echo "❌ User not found with this phone number"
elif [ "$HTTP_STATUS" -eq 400 ]; then
    echo "❌ Bad request - check the phone number format"
else
    echo "❌ Failed to send SMS verification (Status: $HTTP_STATUS)"
    echo "Response: $RESPONSE_BODY"
fi

echo
echo "💡 Alternative endpoints to try:"
echo "1. /auth/phone/resend-verification"
echo "2. /auth/resend-sms"
echo "3. /auth/phone/verify/resend"
echo

echo "📱 SMS Resend Complete!"
