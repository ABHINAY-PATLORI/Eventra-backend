# Event Management System - API Testing Guide

## 🚀 Quick Start - Test All Endpoints

### Prerequisites
- Server running on `http://localhost:8080`
- cURL or Postman installed
- Email configured for OTP delivery

---

## 📧 Step 1: Register a New User

### Register as STUDENT
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "email": "alice@college.edu",
    "password": "SecurePass123",
    "role": "STUDENT"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully. OTP sent to email",
  "data": {
    "id": 1,
    "name": "Alice Johnson",
    "email": "alice@college.edu",
    "role": "STUDENT",
    "verified": false
  }
}
```

✅ **OTP will be sent to alice@college.edu**

---

### Register as ORGANIZER
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bob Smith",
    "email": "bob@college.edu",
    "password": "OrganizerPass123",
    "role": "ORGANIZER"
  }'
```

---

## 🔐 Step 2: Verify OTP & Get JWT Token

### Get OTP from Email (check your inbox)
Looking for email from: `your-configured-sender@gmail.com`
Subject: `College Event Management OTP Verification`
Body: `Your OTP is: 123456` (example)

### Verify OTP
```bash
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@college.edu",
    "otp": "123456"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "OTP verified successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhbGljZUBjb2xsZWdlLmVkdSIsImlhdCI6MTY0NDAwMDAwMH0.xxxxxxxxxxx",
    "userId": 1,
    "name": "Alice Johnson",
    "email": "alice@college.edu",
    "role": "STUDENT",
    "verified": true,
    "passwordAuthentication": false
  }
}
```

💾 **Save the token value for upcoming requests:**
```bash
export STUDENT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
export ORGANIZER_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 📅 Step 3: Test Event APIs (STUDENT)

### Get All Approved Events
```bash
curl -X GET "http://localhost:8080/api/events?page=0&size=10" \
  -H "Authorization: Bearer $STUDENT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Events fetched successfully",
  "data": {
    "content": [],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 0,
    "totalPages": 0,
    "last": true
  }
}
```

*(Empty because no events created yet)*

---

### Search Events by Title
```bash
curl -X GET "http://localhost:8080/api/events/search?title=Java&page=0&size=10" \
  -H "Authorization: Bearer $STUDENT_TOKEN"
```

---

## 🎯 Step 4: Create an Event (ORGANIZER)

### Create New Event
```bash
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -d '{
    "title": "Python Workshop 2026",
    "description": "Learn Python programming from scratch",
    "date": "2026-04-20",
    "time": "14:00:00",
    "location": "Computer Lab 1",
    "capacity": 50,
    "imageUrl": "https://example.com/python.jpg"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Event created successfully",
  "data": {
    "id": 1,
    "title": "Python Workshop 2026",
    "description": "Learn Python programming from scratch",
    "date": "2026-04-20",
    "time": "14:00:00",
    "location": "Computer Lab 1",
    "capacity": 50,
    "status": "PENDING",
    "createdBy": "bob@college.edu"
  }
}
```

✅ **Event created with status: PENDING**

💾 **Save event ID:**
```bash
export EVENT_ID=1
```

---

## 👨‍💼 Step 5: Admin Approval Workflow

### Get Pending Events (ADMIN)
```bash
curl -X GET "http://localhost:8080/api/admin/events/pending?page=0&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

### Approve Event
```bash
curl -X PUT "http://localhost:8080/api/admin/events/$EVENT_ID/approve" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Event approved successfully",
  "data": {
    "id": 1,
    "title": "Python Workshop 2026",
    "status": "APPROVED"
  }
}
```

✅ **Event is now APPROVED**

---

### View Approved Event (STUDENT)
```bash
curl -X GET "http://localhost:8080/api/events" \
  -H "Authorization: Bearer $STUDENT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Events fetched successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Python Workshop 2026",
        "status": "APPROVED",
        "date": "2026-04-20",
        "capacity": 50
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

✅ **Event now visible to students**

---

## 👤 Step 6: Student Participation

### Participate in Event
```bash
curl -X POST "http://localhost:8080/api/events/$EVENT_ID/participate" \
  -H "Authorization: Bearer $STUDENT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Registered for event successfully"
}
```

---

### View Participation History
```bash
curl -X GET "http://localhost:8080/api/student/history" \
  -H "Authorization: Bearer $STUDENT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Registrations fetched successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "eventTitle": "Python Workshop 2026",
        "status": "REGISTERED",
        "prize": null,
        "timestamp": "2026-03-29T10:43:02",
        "cancelledAt": null
      }
    ],
    "totalElements": 1
  }
}
```

---

## 🔄 Step 7: Login (Passwordless OTP)

### Send OTP
```bash
curl -X POST http://localhost:8080/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@college.edu"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "OTP sent successfully"
}
```

---

### Verify OTP to Get New Token
```bash
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@college.edu",
    "otp": "654321"
  }'
```

---

## 🧪 Advanced Test Cases

### Test 1: Reject Event (Admin)
```bash
curl -X PUT "http://localhost:8080/api/admin/events/1/reject" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

### Test 2: Update Event (Organizer - Own Event Only)
```bash
curl -X PUT "http://localhost:8080/api/events/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -d '{
    "title": "Advanced Python Workshop",
    "capacity": 60
  }'
```

---

### Test 3: Delete Event (Organizer - Own Event Only)
```bash
curl -X DELETE "http://localhost:8080/api/events/1" \
  -H "Authorization: Bearer $ORGANIZER_TOKEN"
```

---

### Test 4: Resend OTP
```bash
curl -X POST http://localhost:8080/api/auth/resend-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@college.edu"
  }'
```

---

## 📊 Error Handling Examples

### Invalid OTP
```bash
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@college.edu",
    "otp": "000000"
  }'
```

**Response (400):**
```json
{
  "success": false,
  "message": "Invalid OTP",
  "statusCode": 400
}
```

---

### Expired OTP
```json
{
  "success": false,
  "message": "OTP has expired. Please request a new OTP",
  "statusCode": 400
}
```

---

### Unauthorized Access
```bash
curl -X POST "http://localhost:8080/api/events" \
  -H "Content-Type: application/json" \
  -d '{"title": "Test"}'
```

**Response (401):**
```json
{
  "success": false,
  "message": "Unauthorized",
  "statusCode": 401
}
```

---

### Insufficient Permissions
```bash
# Student trying to create event (needs ORGANIZER role)
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -d '{"title": "Test Event"}'
```

**Response (403):**
```json
{
  "success": false,
  "message": "Access Denied",
  "statusCode": 403
}
```

---

## 🎬 Full Workflow Automation Script

### save-as: `test-api.sh`
```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

# 1. Register Student
echo "📝 Registering STUDENT..."
STUDENT=$(curl -s -X POST $BASE_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Student",
    "email": "student@test.com",
    "password": "TestPass123",
    "role": "STUDENT"
  }')
echo "$STUDENT"

# 2. Get OTP from email (manually enter)
read -p "Enter OTP for student: " OTP

# 3. Verify OTP
echo "🔐 Verifying OTP..."
TOKEN=$(curl -s -X POST $BASE_URL/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"student@test.com\", \"otp\": \"$OTP\"}" \
  | jq -r '.data.token')
echo "Token: $TOKEN"

# 4. Get Events
echo "📅 Fetching events..."
curl -s -X GET "$BASE_URL/api/events" \
  -H "Authorization: Bearer $TOKEN" | jq

echo "✅ Test completed!"
```

---

## 📱 Testing with Postman

1. Import collection from `Postman-Collection.json`
2. Set environment variables:
   - `BASE_URL` = `http://localhost:8080`
   - `STUDENT_TOKEN` = (from registration)
   - `EVENT_ID` = (from event creation)
3. Run requests in sequence

---

## ✅ Checklist - All API Endpoints

- [ ] `POST /api/auth/register` - Register user
- [ ] `POST /api/auth/verify-otp` - Verify OTP
- [ ] `POST /api/auth/send-otp` - Login OTP
- [ ] `POST /api/auth/resend-otp` - Resend OTP
- [ ] `GET /api/events` - Get approved events
- [ ] `GET /api/events/{id}` - Get event details
- [ ] `GET /api/events/search?title=...` - Search events
- [ ] `POST /api/events` - Create event (ORGANIZER)
- [ ] `PUT /api/events/{id}` - Update event (ORGANIZER)
- [ ] `DELETE /api/events/{id}` - Delete event (ORGANIZER)
- [ ] `POST /api/events/{id}/participate` - Join event (STUDENT)
- [ ] `GET /api/student/history` - View history (STUDENT)
- [ ] `GET /api/admin/events/pending` - Pending events (ADMIN)
- [ ] `PUT /api/admin/events/{id}/approve` - Approve event (ADMIN)
- [ ] `PUT /api/admin/events/{id}/reject` - Reject event (ADMIN)
- [ ] `GET /api/admin/users` - Get all users (ADMIN)

---

## 🐛 Troubleshooting

**Q: OTP not received?**
- Check spam folder
- Verify email configuration in `application.properties`
- Check server logs for email send errors

**Q: "User not found" error?**
- Make sure to register the user first
- Use correct email address

**Q: "Invalid OTP" error?**
- OTP is case-sensitive
- OTP expires after 5 minutes
- Re-register to get a new OTP

**Q: CORS errors?**
- Frontend must be on allowed origin (localhost:3000 or:5173)
- Check browser console for details

**Q: 401 Unauthorized?**
- Include Authorization header with JWT token
- token format: `Bearer <your_jwt_token>`

**Q: 403 Forbidden?**
- User role doesn't have permission
- Check role requirements in API documentation
