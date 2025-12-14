#!/bin/bash

BASE_URL="http://localhost:8080"
EMAIL="test$(date +%s)@example.com"
PASSWORD="test123"

echo "=========================================="
echo "USPS Customer Portal - Complete Test Suite"
echo "=========================================="
echo ""

echo "=== 1. Health Check ==="
HEALTH=$(curl -s $BASE_URL/actuator/health)
if echo "$HEALTH" | grep -q '"status":"UP"'; then
  echo "✅ Health check passed"
  echo "$HEALTH" | python3 -m json.tool 2>/dev/null || echo "$HEALTH"
else
  echo "❌ Health check failed"
  exit 1
fi
echo ""

echo "=== 2. Register New User ==="
REGISTER_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

if echo "$REGISTER_RESPONSE" | grep -q "token"; then
  echo "✅ Registration successful"
  TOKEN=$(echo $REGISTER_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])" 2>/dev/null)
  echo "Token: ${TOKEN:0:50}..."
else
  echo "❌ Registration failed"
  echo "$REGISTER_RESPONSE"
  exit 1
fi
echo ""

echo "=== 3. Login ==="
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
  echo "✅ Login successful"
  TOKEN=$(echo $LOGIN_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])" 2>/dev/null)
else
  echo "❌ Login failed"
  echo "$LOGIN_RESPONSE"
  exit 1
fi
echo ""

echo "=== 4. Create Shipment ==="
SHIPMENT_RESPONSE=$(curl -s -X POST $BASE_URL/api/tracking/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"recipientName":"Jane Smith","recipientAddress":"123 Main St","recipientCity":"New York","recipientState":"NY","recipientZipCode":"10001"}')

if echo "$SHIPMENT_RESPONSE" | grep -q "trackingNumber"; then
  echo "✅ Shipment created"
  TRACKING=$(echo $SHIPMENT_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['trackingNumber'])" 2>/dev/null)
  echo "Tracking Number: $TRACKING"
else
  echo "❌ Shipment creation failed"
  echo "$SHIPMENT_RESPONSE"
  exit 1
fi
echo ""

echo "=== 5. Track Shipment ==="
TRACK_RESPONSE=$(curl -s -X GET "$BASE_URL/api/tracking/$TRACKING" \
  -H "Authorization: Bearer $TOKEN")

if echo "$TRACK_RESPONSE" | grep -q "shipment"; then
  echo "✅ Tracking successful"
  echo "$TRACK_RESPONSE" | python3 -m json.tool 2>/dev/null | head -20
else
  echo "❌ Tracking failed"
  echo "$TRACK_RESPONSE"
fi
echo ""

echo "=== 6. Get All Shipments ==="
MY_SHIPMENTS=$(curl -s -X GET $BASE_URL/api/tracking/my-shipments \
  -H "Authorization: Bearer $TOKEN")

if echo "$MY_SHIPMENTS" | grep -q "shipments"; then
  echo "✅ Retrieved shipments"
  COUNT=$(echo "$MY_SHIPMENTS" | python3 -c "import sys, json; print(len(json.load(sys.stdin)['shipments']))" 2>/dev/null)
  echo "Total shipments: $COUNT"
else
  echo "❌ Failed to retrieve shipments"
  echo "$MY_SHIPMENTS"
fi
echo ""

echo "=== 7. Get Statistics ==="
STATS=$(curl -s -X GET $BASE_URL/api/reports/statistics \
  -H "Authorization: Bearer $TOKEN")

if echo "$STATS" | grep -q "totalShipments"; then
  echo "✅ Statistics retrieved"
  TOTAL=$(echo "$STATS" | python3 -c "import sys, json; print(json.load(sys.stdin)['totalShipments'])" 2>/dev/null)
  echo "Total shipments: $TOTAL"
else
  echo "❌ Failed to get statistics"
  echo "$STATS"
fi
echo ""

echo "=== 8. Test Rate Limiting (5 requests) ==="
RATE_LIMIT_PASSED=true
for i in {1..5}; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/api/tracking/$TRACKING" \
    -H "Authorization: Bearer $TOKEN")
  if [ "$STATUS" = "200" ]; then
    echo "  Request $i: ✅ HTTP $STATUS"
  else
    echo "  Request $i: ❌ HTTP $STATUS"
    RATE_LIMIT_PASSED=false
  fi
  sleep 0.2
done
if [ "$RATE_LIMIT_PASSED" = true ]; then
  echo "✅ Rate limiting test passed (first 5 requests should succeed)"
else
  echo "⚠️  Some requests failed (may be rate limited)"
fi
echo ""

echo "=== 9. Test Unauthorized Access ==="
UNAUTH_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X GET $BASE_URL/api/tracking/my-shipments)
if [ "$UNAUTH_RESPONSE" = "401" ] || [ "$UNAUTH_RESPONSE" = "403" ]; then
  echo "✅ Unauthorized access blocked (HTTP $UNAUTH_RESPONSE)"
else
  echo "❌ Security issue: Unauthorized access returned HTTP $UNAUTH_RESPONSE"
fi
echo ""

echo "=========================================="
echo "✅ All Tests Completed!"
echo "=========================================="
echo ""
echo "Frontend: http://localhost:3000"
echo "Backend API: http://localhost:8080"
echo "Health Check: http://localhost:8080/actuator/health"
echo ""
echo "Test Credentials:"
echo "  Email: $EMAIL"
echo "  Password: $PASSWORD"
echo ""

