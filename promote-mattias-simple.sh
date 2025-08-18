#!/bin/bash

# Simple Promote Mattias to Super Admin
echo "👑 Promoting mattiasgebrie@gmail.com to Super Admin"
echo "=================================================="

DEV_URL="https://web-development-a36e.up.railway.app/api"
EMAIL="mattiasgebrie@gmail.com"

echo "🎯 Target: $EMAIL"
echo

# Step 1: Try to login with different password variations
echo "🔐 Step 1: Testing login with different passwords..."

PASSWORDS=("M4tt145&T5e" "Mattias&T5e" "M4tt145_T5e" "Mattias_T5e" "M4tt145T5e" "MattiasT5e")

for password in "${PASSWORDS[@]}"; do
    echo "   Trying password: $password"
    LOGIN_RESPONSE=$(curl -s -X POST "$DEV_URL/auth/login" \
      -H "Content-Type: application/json" \
      -d "{\"email\":\"$EMAIL\",\"password\":\"$password\"}")
    
    if echo "$LOGIN_RESPONSE" | grep -q "token"; then
        echo "   ✅ Login successful with password: $password"
        TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')
        break
    else
        echo "   ❌ Failed with password: $password"
    fi
done

if [ -z "$TOKEN" ]; then
    echo
    echo "❌ Could not login with any password variation"
    echo "💡 Please provide the correct password or create the user first"
    exit 1
fi

echo
echo "🔑 Step 2: Promoting to Super Admin..."
PROMOTE_RESPONSE=$(curl -s -X POST "$DEV_URL/admin/promote-to-super-admin" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"email\":\"$EMAIL\"}")

echo "Promote Response: $PROMOTE_RESPONSE"

if echo "$PROMOTE_RESPONSE" | grep -q "success.*true"; then
    echo "✅ Successfully promoted $EMAIL to Super Admin!"
else
    echo "❌ Failed to promote user"
    echo "Response: $PROMOTE_RESPONSE"
fi

echo
echo "👑 Promotion process complete!"
