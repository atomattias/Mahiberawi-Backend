#!/bin/bash

# Test Secure Promotion Script
echo "🔐 Testing Secure Promotion Endpoint"
echo "===================================="

DEV_URL="https://web-development-a36e.up.railway.app/api"
EMAIL="mattiasgebrie@gmail.com"

echo "🎯 Target: $EMAIL"
echo "🌐 URL: $DEV_URL"
echo

# Test 1: Without promotion key (should fail)
echo "🧪 Test 1: Without promotion key (should fail)"
RESPONSE=$(curl -s -X POST "$DEV_URL/admin/secure-promote-super-admin/$EMAIL" -w "HTTPSTATUS:%{http_code}")
STATUS=$(echo "$RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

echo "Status: $STATUS"
echo "Response: $BODY"
echo

# Test 2: With invalid promotion key (should fail)
echo "🧪 Test 2: With invalid promotion key (should fail)"
RESPONSE=$(curl -s -X POST "$DEV_URL/admin/secure-promote-super-admin/$EMAIL?promotionKey=invalid-key" -w "HTTPSTATUS:%{http_code}")
STATUS=$(echo "$RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

echo "Status: $STATUS"
echo "Response: $BODY"
echo

echo "✅ Secure promotion endpoint is working!"
echo "💡 To use it:"
echo "   1. Set SUPER_ADMIN_PROMOTION_KEY in Railway"
echo "   2. Call: POST /admin/secure-promote-super-admin/{email}?promotionKey={key}"
echo
