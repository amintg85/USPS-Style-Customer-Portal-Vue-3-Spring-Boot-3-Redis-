# USPS Customer Portal - Complete Testing Guide

## Prerequisites
- All services running: `docker-compose ps`
- Frontend: http://localhost:3000
- Backend: http://localhost:8080

---

## 1. Health Check ✅

```bash
curl http://localhost:8080/actuator/health | python3 -m json.tool
```

**Expected**: All components UP (db, redis, diskSpace, ping)

---

## 2. Authentication Testing

### 2.1 Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "password123"
  }' | python3 -m json.tool
```

**Expected Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Save the token** for subsequent requests:
```bash
export TOKEN="your-jwt-token-here"
```

### 2.2 Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }' | python3 -m json.tool
```

**Expected**: Same response as register (new JWT token)

---

## 3. Shipment Tracking Testing

### 3.1 Create a New Shipment

```bash
curl -X POST http://localhost:8080/api/tracking/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "recipientName": "Jane Smith",
    "recipientAddress": "123 Main Street",
    "recipientCity": "New York",
    "recipientState": "NY",
    "recipientZipCode": "10001"
  }' | python3 -m json.tool
```

**Expected Response**:
```json
{
  "id": 1,
  "trackingNumber": "USPS1234567890ABCD",
  "recipientName": "Jane Smith",
  "status": "PENDING",
  "createdAt": "2025-12-12T20:00:00",
  ...
}
```

**Save the tracking number**:
```bash
export TRACKING_NUMBER="USPS1234567890ABCD"
```

### 3.2 Track a Shipment

```bash
curl -X GET "http://localhost:8080/api/tracking/$TRACKING_NUMBER" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

**Expected Response**:
```json
{
  "shipment": {
    "id": 1,
    "trackingNumber": "USPS1234567890ABCD",
    "status": "PENDING",
    "recipientName": "Jane Smith",
    ...
  },
  "events": [
    {
      "location": "Origin Facility",
      "description": "Shipment created and pending pickup",
      "eventTime": "2025-12-12T20:00:00"
    }
  ]
}
```

### 3.3 Get All My Shipments

```bash
curl -X GET http://localhost:8080/api/tracking/my-shipments \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

**Expected**: Array of all shipments for the authenticated user

---

## 4. Reports Testing

### 4.1 Get User Statistics

```bash
curl -X GET http://localhost:8080/api/reports/statistics \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

**Expected Response**:
```json
{
  "totalShipments": 1,
  "statusCounts": {
    "PENDING": 1,
    "IN_TRANSIT": 0,
    "OUT_FOR_DELIVERY": 0,
    "DELIVERED": 0,
    "EXCEPTION": 0
  }
}
```

### 4.2 Generate Shipment Report (Date Range)

```bash
# Get dates (last 30 days)
START_DATE=$(date -u -v-30d +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || date -u -d "30 days ago" +"%Y-%m-%dT%H:%M:%S")
END_DATE=$(date -u +"%Y-%m-%dT%H:%M:%S")

curl -X GET "http://localhost:8080/api/reports/shipment-report?startDate=$START_DATE&endDate=$END_DATE" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

**Expected Response**:
```json
{
  "totalShipments": 1,
  "period": {
    "start": "2025-11-12T20:00:00",
    "end": "2025-12-12T20:00:00"
  },
  "statusCounts": {
    "PENDING": 1,
    ...
  },
  "deliveryRate": 0.0,
  "shipments": [...]
}
```

---

## 5. Rate Limiting Testing

### 5.1 Test Rate Limit (100 requests/minute)

```bash
# Make 101 rapid requests
for i in {1..101}; do
  echo "Request $i:"
  curl -s -X GET "http://localhost:8080/api/tracking/$TRACKING_NUMBER" \
    -H "Authorization: Bearer $TOKEN" \
    -w "\nHTTP Status: %{http_code}\n\n"
  sleep 0.1
done
```

**Expected**: 
- First 100 requests: HTTP 200
- Request 101+: HTTP 429 (Too Many Requests) with message "Rate limit exceeded"

---

## 6. Redis Caching Testing

### 6.1 Test Cache Performance

```bash
# First request (cache miss - slower)
time curl -s -X GET "http://localhost:8080/api/tracking/$TRACKING_NUMBER" \
  -H "Authorization: Bearer $TOKEN" > /dev/null

# Second request (cache hit - faster)
time curl -s -X GET "http://localhost:8080/api/tracking/$TRACKING_NUMBER" \
  -H "Authorization: Bearer $TOKEN" > /dev/null
```

**Expected**: Second request should be significantly faster (cached)

### 6.2 Verify Redis is Caching

```bash
# Check Redis directly
docker-compose exec redis redis-cli
# Then in Redis CLI:
KEYS *
GET shipments:*
```

---

## 7. Security Testing

### 7.1 Test Unauthorized Access

```bash
# Try to access protected endpoint without token
curl -X GET http://localhost:8080/api/tracking/my-shipments
```

**Expected**: HTTP 401 (Unauthorized)

### 7.2 Test Invalid Token

```bash
curl -X GET http://localhost:8080/api/tracking/my-shipments \
  -H "Authorization: Bearer invalid-token"
```

**Expected**: HTTP 403 (Forbidden) or 401

### 7.3 Test Access to Other User's Shipment

```bash
# Register second user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Johnson",
    "email": "alice@example.com",
    "password": "password123"
  }' | python3 -m json.tool

# Save Alice's token
export ALICE_TOKEN="alice-jwt-token"

# Try to access John's shipment with Alice's token
curl -X GET "http://localhost:8080/api/tracking/$TRACKING_NUMBER" \
  -H "Authorization: Bearer $ALICE_TOKEN"
```

**Expected**: HTTP 403 (Forbidden) - "Access denied"

---

## 8. Frontend Testing (Browser)

### 8.1 Access Frontend
1. Open browser: http://localhost:3000
2. You should see the login page

### 8.2 Test Registration Flow
1. Click "Register here"
2. Fill in registration form
3. Submit and verify redirect to tracking page

### 8.3 Test Login Flow
1. Logout (if logged in)
2. Enter credentials
3. Verify successful login and redirect

### 8.4 Test Tracking Page
1. Create a new shipment
2. Enter tracking number and track
3. View shipment details and tracking events
4. View list of all shipments

### 8.5 Test Reports Page
1. Navigate to Reports
2. View statistics
3. Generate report with date range
4. Verify report data displays correctly

---

## 9. Complete Test Script

Save this as `test-all.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"
EMAIL="test@example.com"
PASSWORD="test123"

echo "=== 1. Health Check ==="
curl -s $BASE_URL/actuator/health | python3 -m json.tool
echo ""

echo "=== 2. Register User ==="
REGISTER_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
echo $REGISTER_RESPONSE | python3 -m json.tool

TOKEN=$(echo $REGISTER_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])")
echo "Token: $TOKEN"
echo ""

echo "=== 3. Create Shipment ==="
SHIPMENT_RESPONSE=$(curl -s -X POST $BASE_URL/api/tracking/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"recipientName":"Test Recipient","recipientAddress":"123 Test St","recipientCity":"Test City","recipientState":"TS","recipientZipCode":"12345"}')
echo $SHIPMENT_RESPONSE | python3 -m json.tool

TRACKING=$(echo $SHIPMENT_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['trackingNumber'])")
echo "Tracking Number: $TRACKING"
echo ""

echo "=== 4. Track Shipment ==="
curl -s -X GET "$BASE_URL/api/tracking/$TRACKING" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
echo ""

echo "=== 5. Get My Shipments ==="
curl -s -X GET $BASE_URL/api/tracking/my-shipments \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
echo ""

echo "=== 6. Get Statistics ==="
curl -s -X GET $BASE_URL/api/reports/statistics \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
echo ""

echo "=== 7. Test Rate Limit (5 requests) ==="
for i in {1..5}; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/api/tracking/$TRACKING" \
    -H "Authorization: Bearer $TOKEN")
  echo "Request $i: HTTP $STATUS"
done
echo ""

echo "=== All Tests Complete ==="
```

Make it executable and run:
```bash
chmod +x test-all.sh
./test-all.sh
```

---

## 10. Monitoring Endpoints

### 10.1 Actuator Metrics
```bash
curl http://localhost:8080/actuator/metrics | python3 -m json.tool
```

### 10.2 Prometheus Metrics
```bash
curl http://localhost:8080/actuator/prometheus
```

---

## Expected Results Summary

✅ **Authentication**: Register and login work, JWT tokens generated  
✅ **Tracking**: Create, track, and list shipments  
✅ **Reports**: Statistics and date-range reports  
✅ **Security**: Unauthorized access blocked, user isolation  
✅ **Rate Limiting**: 100 requests/minute limit enforced  
✅ **Caching**: Redis improves response times  
✅ **Health**: All components healthy  

---

## Troubleshooting

- **401 Unauthorized**: Check token is valid and included in Authorization header
- **403 Forbidden**: User doesn't own the resource or rate limit exceeded
- **404 Not Found**: Check endpoint URL and tracking number
- **500 Server Error**: Check backend logs: `docker-compose logs backend`

