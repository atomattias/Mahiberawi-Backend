#!/bin/bash

# Debug SMS Issue Script
# This script helps identify why SMS is not sending

echo "🔍 Debugging SMS Issue"
echo "======================"

DEV_URL="https://web-development-a36e.up.railway.app/api"
PHONE_NUMBER="+4791261801"

echo "Testing with phone: $PHONE_NUMBER"
echo

# Test 1: Check if user exists
echo "1️⃣ Checking if user exists..."
USER_STATUS=$(curl -s -X GET "$DEV_URL/auth/phone/registration-status/$PHONE_NUMBER")
echo "User Status: $USER_STATUS"
echo

# Test 2: Try to register user if not exists
echo "2️⃣ Registering user..."
REG_RESPONSE=$(curl -s -X POST "$DEV_URL/auth/phone/register" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"phoneNumber\":\"$PHONE_NUMBER\",\"password\":\"Test123!\",\"confirmPassword\":\"Test123!\"}")
echo "Registration Response: $REG_RESPONSE"
echo

# Test 3: Try resend verification
echo "3️⃣ Testing resend verification..."
RESEND_RESPONSE=$(curl -s -X POST "$DEV_URL/auth/phone/resend-verification" \
  -H "Content-Type: application/json" \
  -d "{\"phoneNumber\":\"$PHONE_NUMBER\"}")
echo "Resend Response: $RESEND_RESPONSE"
echo

# Test 4: Check debug codes
echo "4️⃣ Checking debug codes..."
DEBUG_RESPONSE=$(curl -s -X GET "$DEV_URL/auth/phone/debug-codes?phoneNumber=$PHONE_NUMBER")
echo "Debug Response: $DEBUG_RESPONSE"
echo

echo "🔍 SMS Issue Analysis:"
echo "======================"
echo
echo "✅ Access Denied Issue: FIXED"
echo "❌ SMS Sending Issue: Still present"
echo
echo "💡 Next Steps:"
echo "1. Check Railway environment variables for Twilio:"
echo "   - TWILIO_ACCOUNT_SID_DEV"
echo "   - TWILIO_AUTH_TOKEN_DEV"
echo "   - TWILIO_FROM_NUMBER_DEV"
echo "   - TWILIO_SERVICE_SID_DEV"
echo
echo "2. Check Railway logs for SMS provider errors"
echo "3. Verify Twilio account is active and has credits"
echo "4. Check if the phone number format is supported by Twilio"
