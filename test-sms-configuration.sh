#!/bin/bash

# Test SMS Configuration Script
# This script helps debug SMS configuration issues

echo "📱 Testing SMS Configuration (Development)"
echo "=========================================="

# Development environment URL
DEV_URL="https://web-development-a36e.up.railway.app/api"

echo
echo "Development URL: $DEV_URL"
echo

# Get phone number
read -p "Enter the phone number to test SMS (e.g., +4791261801): " PHONE_NUMBER

if [ -z "$PHONE_NUMBER" ]; then
    echo "❌ Phone number is required"
    exit 1
fi

echo
echo "📱 Testing SMS configuration for: $PHONE_NUMBER"
echo

# Test 1: Check SMS provider configuration
echo "🔍 Test 1: Checking SMS provider configuration..."
SMS_CONFIG_RESPONSE=$(curl -s -X GET "$DEV_URL/auth/phone/debug-codes?phoneNumber=$PHONE_NUMBER" \
  -H "Content-Type: application/json" \
  -w "HTTPSTATUS:%{http_code}")

HTTP_STATUS=$(echo "$SMS_CONFIG_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
RESPONSE_BODY=$(echo "$SMS_CONFIG_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

echo "Status Code: $HTTP_STATUS"
echo "Response: $RESPONSE_BODY"
echo

# Test 2: Send test SMS
echo "📤 Test 2: Sending test SMS..."
TEST_SMS_RESPONSE=$(curl -s -X POST "$DEV_URL/auth/phone/test-sms" \
  -H "Content-Type: application/json" \
  -d "phoneNumber=$PHONE_NUMBER" \
  -w "HTTPSTATUS:%{http_code}")

HTTP_STATUS=$(echo "$TEST_SMS_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
RESPONSE_BODY=$(echo "$TEST_SMS_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

echo "Status Code: $HTTP_STATUS"
echo "Response: $RESPONSE_BODY"
echo

# Test 3: Check registration status
echo "📋 Test 3: Checking registration status..."
REG_STATUS_RESPONSE=$(curl -s -X GET "$DEV_URL/auth/phone/registration-status/$PHONE_NUMBER" \
  -H "Content-Type: application/json" \
  -w "HTTPSTATUS:%{http_code}")

HTTP_STATUS=$(echo "$REG_STATUS_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
RESPONSE_BODY=$(echo "$REG_STATUS_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

echo "Status Code: $HTTP_STATUS"
echo "Response: $RESPONSE_BODY"
echo

# Test 4: Try resend verification
echo "🔄 Test 4: Testing resend verification..."
RESEND_RESPONSE=$(curl -s -X POST "$DEV_URL/auth/phone/resend-verification" \
  -H "Content-Type: application/json" \
  -d "{\"phoneNumber\":\"$PHONE_NUMBER\"}" \
  -w "HTTPSTATUS:%{http_code}")

HTTP_STATUS=$(echo "$RESEND_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
RESPONSE_BODY=$(echo "$RESEND_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

echo "Status Code: $HTTP_STATUS"
echo "Response: $RESPONSE_BODY"
echo

echo "📱 SMS Configuration Test Complete!"
echo
echo "💡 Troubleshooting Tips:"
echo "1. If SMS is not sending, check Twilio environment variables:"
echo "   - TWILIO_ACCOUNT_SID_DEV"
echo "   - TWILIO_AUTH_TOKEN_DEV"
echo "   - TWILIO_FROM_NUMBER_DEV"
echo "   - TWILIO_SERVICE_SID_DEV"
echo
echo "2. Make sure phone number is in international format (+47XXXXXXXXX)"
echo
echo "3. Check Railway logs for SMS provider errors"
echo
echo "4. If access denied, the endpoint might not be in security config"
