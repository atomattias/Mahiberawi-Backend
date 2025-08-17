#!/bin/bash

# Discover SMS Endpoints Script
# This script tries different SMS-related endpoints to find the correct ones

echo "🔍 Discovering SMS Endpoints (Development)"
echo "=========================================="

# Development environment URL
DEV_URL="https://web-development-a36e.up.railway.app/api"

echo
echo "Development URL: $DEV_URL"
echo

# List of possible SMS endpoints to try
ENDPOINTS=(
    "/auth/phone/resend-verification"
    "/auth/resend-sms"
    "/auth/phone/verify/resend"
    "/auth/phone/resend"
    "/auth/sms/resend"
    "/auth/verification/resend"
    "/auth/phone/verification/resend"
    "/auth/phone/send-verification"
    "/auth/send-sms"
)

PHONE_NUMBER="+4791261801"

echo "📱 Testing SMS endpoints for phone: $PHONE_NUMBER"
echo

for endpoint in "${ENDPOINTS[@]}"; do
    echo "🔍 Testing: $endpoint"
    
    RESPONSE=$(curl -s -X POST "$DEV_URL$endpoint" \
      -H "Content-Type: application/json" \
      -d "{\"phoneNumber\":\"$PHONE_NUMBER\"}" \
      -w "HTTPSTATUS:%{http_code}")
    
    HTTP_STATUS=$(echo "$RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')
    
    if [ "$HTTP_STATUS" -eq 200 ]; then
        echo "✅ SUCCESS (200): $endpoint"
        echo "   Response: $RESPONSE_BODY"
    elif [ "$HTTP_STATUS" -eq 404 ]; then
        echo "❌ NOT FOUND (404): $endpoint"
    elif [ "$HTTP_STATUS" -eq 403 ]; then
        echo "🔒 FORBIDDEN (403): $endpoint - might require auth"
    elif [ "$HTTP_STATUS" -eq 400 ]; then
        echo "⚠️  BAD REQUEST (400): $endpoint - endpoint exists but wrong format"
        echo "   Response: $RESPONSE_BODY"
    else
        echo "❓ UNKNOWN ($HTTP_STATUS): $endpoint"
        echo "   Response: $RESPONSE_BODY"
    fi
    echo
done

echo "🔍 SMS Endpoint Discovery Complete!"
echo
echo "💡 Next Steps:"
echo "1. Look for SUCCESS (200) responses above"
echo "2. If no 200 responses, check your app's SMS endpoints"
echo "3. Try with authentication if endpoints return 403"
