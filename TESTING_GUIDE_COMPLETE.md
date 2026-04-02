# Event Management System - Complete Testing Guide

## Quick Start Testing

### Server Status
```bash
# Verify server is running
curl http://localhost:8080/api/events
# Expected: 200 OK with events list
```

---

## 1. Authentication Setup

### 1.1 Register New User

```bash
# Register as a Student
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.student@example.com",
    "phone": "9876543210",
    "password": "SecurePass@123",
    "role": "STUDENT"
  }'

# Response: 201 Created
# {
#   "success": true,
#   "message": "User registered successfully. OTP sent to email.",
#   "data": {
#     "userId": 1,
#     "email": "john.student@example.com",
#     "role": "STUDENT"
#   }
# }
```

### 1.2 Register Organizer

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Organizer",
    "email": "jane.organizer@example.com",
    "phone": "9876543211",
    "password": "SecurePass@123",
    "role": "ORGANIZER"
  }'
```

### 1.3 Register Admin

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin User",
    "email": "admin@example.com",
    "phone": "9876543212",
    "password": "SecurePass@123",
    "role": "ADMIN"
  }'
```

### 1.4 Send OTP

```bash
curl -X POST http://localhost:8080/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.student@example.com"
  }'

# Response: 200 OK
# {
#   "success": true,
#   "message": "OTP sent successfully to email"
# }
```

### 1.5 Verify OTP and Get JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.student@example.com",
    "otp": "123456"
  }'

# Response: 200 OK
# {
#   "success": true,
#   "message": "OTP verified successfully",
#   "data": {
#     "userId": 1,
#     "email": "john.student@example.com",
#     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#     "role": "STUDENT"
#   }
# }

# Save the token for subsequent requests
export STUDENT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 1.6 Login (Alternative to OTP)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.student@example.com",
    "password": "SecurePass@123"
  }'

# Response: 200 OK with JWT token
```

---

## 2. Event Management (Organizer/Admin)

### 2.1 Create Event

```bash
# First, register and get ORGANIZER token
export ORGANIZER_TOKEN="<organizer_jwt_token>"

curl -X POST http://localhost:8080/api/events \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Tech Summit 2025",
    "description": "Annual technology conference bringing together industry leaders",
    "date": "2025-04-15",
    "time": "10:00",
    "location": "Manhattan Convention Center",
    "capacity": 500,
    "category": "Technology",
    "eligibility": "All students and professionals welcome",
    "maxParticipants": 500
  }'

# Response: 201 Created
# {
#   "success": true,
#   "message": "Event created successfully",
#   "data": {
#     "id": 1,
#     "title": "Tech Summit 2025",
#     "status": "PENDING",
#     "completed": false,
#     "capacity": 500,
#     "category": "Technology"
#   }
# }

# Save event ID
export EVENT_ID=1
```

### 2.2 Get Organizer's Events

```bash
curl -X GET http://localhost:8080/api/events/my-events \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -H "Content-Type: application/json"

# Response: 200 OK
# {
#   "success": true,
#   "message": "Events fetched successfully",
#   "data": {
#     "content": [
#       {
#         "id": 1,
#         "title": "Tech Summit 2025",
#         "status": "PENDING",
#         "completed": false,
#         "capacity": 500,
#         "registeredCount": 0
#       }
#     ],
#     "pageNumber": 0,
#     "pageSize": 10,
#     "totalElements": 1,
#     "totalPages": 1,
#     "last": true
#   }
# }
```

### 2.3 Update Event

```bash
curl -X PUT http://localhost:8080/api/events/$EVENT_ID \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Tech Summit 2025 - Updated",
    "description": "Updated description",
    "capacity": 600
  }'

# Response: 200 OK
```

---

## 3. Admin Event Approval Workflow

### 3.1 Get Pending Events (Admin Only)

```bash
export ADMIN_TOKEN="<admin_jwt_token>"

curl -X GET http://localhost:8080/api/admin/events/pending \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json"

# Response: 200 OK
# {
#   "success": true,
#   "message": "Pending events fetched successfully",
#   "data": {
#     "content": [
#       {
#         "id": 1,
#         "title": "Tech Summit 2025",
#         "status": "PENDING",
#         "capacity": 500
#       }
#     ],
#     ...
#   }
# }
```

### 3.2 Approve Event

```bash
curl -X PUT http://localhost:8080/api/admin/events/$EVENT_ID/approve \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json"

# Response: 200 OK
# {
#   "success": true,
#   "message": "Event approved successfully",
#   "data": {
#     "id": 1,
#     "title": "Tech Summit 2025",
#     "status": "APPROVED"
#   }
# }
```

### 3.3 Reject Event

```bash
curl -X PUT http://localhost:8080/api/admin/events/2/reject \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json"

# Response: 200 OK
# {
#   "success": true,
#   "message": "Event rejected successfully",
#   "data": {
#     "id": 2,
#     "status": "REJECTED"
#   }
# }
```

---

## 4. Student Registration (NEW FEATURE)

### 4.1 Browse Approved Events

```bash
export STUDENT_TOKEN="<student_jwt_token>"

curl -X GET http://localhost:8080/api/events \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json"

# Response: 200 OK - shows only APPROVED events
# {
#   "success": true,
#   "data": {
#     "content": [
#       {
#         "id": 1,
#         "title": "Tech Summit 2025",
#         "date": "2025-04-15",
#         "location": "Manhattan Convention Center",
#         "capacity": 500,
#         "category": "Technology",
#         "status": "APPROVED"
#       }
#     ]
#   }
# }
```

### 4.2 Register for Event

```bash
curl -X POST http://localhost:8080/api/events/$EVENT_ID/register \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json"

# Response: 201 Created
# {
#   "success": true,
#   "message": "Successfully registered for event",
#   "data": {
#     "eventId": 1,
#     "userId": 5,
#     "status": "REGISTERED",
#     "registered": true,
#     "registrationTime": "2025-03-29T15:30:45"
#   }
# }
```

### 4.3 Test Registration Validation Errors

#### Error: Register Twice
```bash
curl -X POST http://localhost:8080/api/events/$EVENT_ID/register \
  -H "Authorization: Bearer $STUDENT_TOKEN"

# Response: 400 Bad Request
# {
#   "success": false,
#   "message": "Already registered for this event"
# }
```

#### Error: Event Capacity Full
```bash
# Assuming another user has filled all 500 capacity slots
curl -X POST http://localhost:8080/api/events/2/register \
  -H "Authorization: Bearer $STUDENT_TOKEN"

# Response: 400 Bad Request
# {
#   "success": false,
#   "message": "Event capacity full"
# }
```

#### Error: Unapproved Event
```bash
# Try to register for PENDING event
curl -X POST http://localhost:8080/api/events/3/register \
  -H "Authorization: Bearer $STUDENT_TOKEN"

# Response: 400 Bad Request
# {
#   "success": false,
#   "message": "Only approved events can accept registrations"
# }
```

#### Error: Unauthorized Role
```bash
# Organizer trying to register (not STUDENT role)
curl -X POST http://localhost:8080/api/events/$EVENT_ID/register \
  -H "Authorization: Bearer $ORGANIZER_TOKEN"

# Response: 403 Forbidden
# {
#   "success": false,
#   "message": "Access Denied"
# }
```

---

## 5. Attendance Tracking (NEW FEATURE)

### 5.1 Mark Attendance for Participant

```bash
# EVENT_ID = 1, STUDENT_ID = 5
curl -X PUT http://localhost:8080/api/events/$EVENT_ID/attendance/5 \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -H "Content-Type: application/json"

# Response: 200 OK
# {
#   "success": true,
#   "message": "Attendance marked successfully",
#   "data": {
#     "userId": 5,
#     "eventId": 1,
#     "attended": true,
#     "markedAt": "2025-03-29T15:35:20"
#   }
# }
```

### 5.2 Mark Attendance Errors

#### Error: Student Not Registered
```bash
curl -X PUT http://localhost:8080/api/events/$EVENT_ID/attendance/999 \
  -H "Authorization: Bearer $ORGANIZER_TOKEN"

# Response: 404 Not Found
# {
#   "success": false,
#   "message": "Registration not found"
# }
```

#### Error: Unauthorized (Student Cannot Mark Others)
```bash
curl -X PUT http://localhost:8080/api/events/$EVENT_ID/attendance/5 \
  -H "Authorization: Bearer $STUDENT_TOKEN"

# Response: 403 Forbidden
# {
#   "success": false,
#   "message": "You can only mark attendance in your own events"
# }
```

### 5.3 Mark Multiple Attendances (Workflow)

```bash
# Mark attendance for multiple students
STUDENT_IDS=(5 6 7 8 9)

for STUDENT_ID in "${STUDENT_IDS[@]}"; do
  curl -X PUT http://localhost:8080/api/events/$EVENT_ID/attendance/$STUDENT_ID \
    -H "Authorization: Bearer $ORGANIZER_TOKEN"
  echo "Marked attendance for student $STUDENT_ID"
done
```

---

## 6. Event Completion (NEW FEATURE)

### 6.1 Complete Event

```bash
# After event is finished, mark as completed
curl -X PUT http://localhost:8080/api/events/$EVENT_ID/complete \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -H "Content-Type: application/json"

# Response: 200 OK
# {
#   "success": true,
#   "message": "Event marked as completed",
#   "data": {
#     "eventId": 1,
#     "title": "Tech Summit 2025",
#     "completed": true,
#     "completedAt": "2025-03-29T16:00:00",
#     "status": "COMPLETED",
#     "totalRegistrations": 150,
#     "attendanceCount": 142
#   }
# }
```

### 6.2 Cannot Complete Same Event Twice (Idempotent)

```bash
# Attempting to complete already completed event
curl -X PUT http://localhost:8080/api/events/$EVENT_ID/complete \
  -H "Authorization: Bearer $ORGANIZER_TOKEN"

# Response: 400 Bad Request
# {
#   "success": false,
#   "message": "Event is already completed"
# }
```

### 6.3 Only Organizer Can Complete (Authorization Check)

```bash
# Student trying to complete event
curl -X PUT http://localhost:8080/api/events/$EVENT_ID/complete \
  -H "Authorization: Bearer $STUDENT_TOKEN"

# Response: 403 Forbidden
# {
#   "success": false,
#   "message": "You can only complete your own events"
# }
```

---

## 7. Prize Management (NEW FEATURE)

### 7.1 Award First Place Prize

```bash
curl -X PUT http://localhost:8080/api/events/$EVENT_ID/participants/5/prize \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "prize": "First Place Winner - $500 Cash Prize"
  }'

# Response: 200 OK
# {
#   "success": true,
#   "message": "Prize awarded successfully",
#   "data": {
#     "userId": 5,
#     "eventId": 1,
#     "prize": "First Place Winner - $500 Cash Prize",
#     "awardedAt": "2025-03-29T16:05:30"
#   }
# }
```

### 7.2 Award Merit Certificate

```bash
curl -X PUT http://localhost:8080/api/events/$EVENT_ID/participants/6/prize \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "prize": "Merit Certificate - Tech Excellence"
  }'

# Response: 200 OK
```

### 7.3 Award Best Team Performance

```bash
curl -X PUT http://localhost:8080/api/events/$EVENT_ID/participants/7/prize \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "prize": "Best Team Performance Award - Event Organizers"
  }'

# Response: 200 OK
```

### 7.4 Award Participation Certificate

```bash
curl -X PUT http://localhost:8080/api/events/$EVENT_ID/participants/8/prize \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "prize": "Participation Certificate"
  }'

# Response: 200 OK
```

### 7.5 Prize Award Errors

#### Error: Student Not Registered
```bash
curl -X PUT http://localhost:8080/api/events/$EVENT_ID/participants/999/prize \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -d '{"prize": "First Place"}'

# Response: 404 Not Found
# {
#   "success": false,
#   "message": "Registration not found"
# }
```

#### Error: Prize Text Too Long
```bash
curl -X PUT http://localhost:8080/api/events/$EVENT_ID/participants/5/prize \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -d '{"prize": "'$(python3 -c "print('x'*501)'")'}'

# Response: 400 Bad Request
# {
#   "success": false,
#   "message": "Prize text exceeds maximum length of 500 characters"
# }
```

#### Error: Unauthorized (Student Cannot Award)
```bash
curl -X PUT http://localhost:8080/api/events/$EVENT_ID/participants/5/prize \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -d '{"prize": "First Place"}'

# Response: 403 Forbidden
# {
#   "success": false,
#   "message": "You can only award prizes in your own events"
# }
```

---

## 8. Complete Event Lifecycle Test Scenario

### Full Workflow Test (Step-by-Step)

```bash
#!/bin/bash

# ==== SETUP ====
ORGANIZER_TOKEN="<organizer_token>"
ADMIN_TOKEN="<admin_token>"
STUDENT1_TOKEN="<student1_token>"
STUDENT2_TOKEN="<student2_token>"

echo "=== Event Management System - Full Lifecycle Test ==="

# Step 1: Organizer creates event
echo "Step 1: Creating event..."
EVENT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/events \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Summit 2025",
    "description": "Testing event lifecycle",
    "date": "2025-04-15",
    "time": "10:00",
    "location": "Test Hall",
    "capacity": 100,
    "category": "Testing",
    "eligibility": "All"
  }')
EVENT_ID=$(echo $EVENT_RESPONSE | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
echo "Created event ID: $EVENT_ID"

# Step 2: Admin approves event
echo "Step 2: Approving event..."
curl -s -X PUT http://localhost:8080/api/admin/events/$EVENT_ID/approve \
  -H "Authorization: Bearer $ADMIN_TOKEN"
echo "Event approved"

# Step 3: Students register
echo "Step 3: Students registering..."
curl -s -X POST http://localhost:8080/api/events/$EVENT_ID/register \
  -H "Authorization: Bearer $STUDENT1_TOKEN"
echo "Student 1 registered"

curl -s -X POST http://localhost:8080/api/events/$EVENT_ID/register \
  -H "Authorization: Bearer $STUDENT2_TOKEN"
echo "Student 2 registered"

# Step 4: Mark attendance
echo "Step 4: Marking attendance..."
curl -s -X PUT http://localhost:8080/api/events/$EVENT_ID/attendance/5 \
  -H "Authorization: Bearer $ORGANIZER_TOKEN"
curl -s -X PUT http://localhost:8080/api/events/$EVENT_ID/attendance/6 \
  -H "Authorization: Bearer $ORGANIZER_TOKEN"
echo "Attendance marked"

# Step 5: Complete event
echo "Step 5: Completing event..."
curl -s -X PUT http://localhost:8080/api/events/$EVENT_ID/complete \
  -H "Authorization: Bearer $ORGANIZER_TOKEN"
echo "Event completed"

# Step 6: Award prizes
echo "Step 6: Awarding prizes..."
curl -s -X PUT http://localhost:8080/api/events/$EVENT_ID/participants/5/prize \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -d '{"prize":"First Place Winner - $500"}'
curl -s -X PUT http://localhost:8080/api/events/$EVENT_ID/participants/6/prize \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -d '{"prize":"Second Place Winner - $300"}'
echo "Prizes awarded"

echo "=== Lifecycle test completed successfully ==="
```

---

## 9. Search and Filter Testing

### 9.1 Search Events

```bash
curl -X GET "http://localhost:8080/api/events/search?keyword=summit" \
  -H "Authorization: Bearer $STUDENT_TOKEN"

# Response: 200 OK with matching events
```

### 9.2 Get Single Event

```bash
curl -X GET http://localhost:8080/api/events/$EVENT_ID \
  -H "Authorization: Bearer $STUDENT_TOKEN"

# Response: 200 OK with event details
```

---

## 10. Error Code Reference

### Common HTTP Status Codes

```
200 OK              - Request successful
201 Created         - Resource created successfully
400 Bad Request     - Validation failed or invalid input
401 Unauthorized    - Missing or invalid token
403 Forbidden       - Insufficient permissions (wrong role)
404 Not Found       - Resource doesn't exist
500 Server Error    - Internal server error
```

### Common Error Messages

```
"Already registered for this event"
→ Student attempting to register twice

"Event capacity full"
→ Event has reached maximum participants

"Only approved events can accept registrations"
→ Event not in APPROVED status

"You can only complete your own events"
→ Non-creator attempting to complete event

"Registration not found"
→ Attempting to mark attendance for non-existent registration

"Access Denied"
→ User role doesn't have permission for operation

"Prize text exceeds maximum length of 500 characters"
→ Prize description too long
```

---

## 11. Performance Testing (Optional)

### Load Test: 1000 Registrations

```bash
#!/bin/bash

echo "Starting load test with 1000 registrations..."
for i in {1..1000}; do
  curl -s -X POST http://localhost:8080/api/events/$EVENT_ID/register \
    -H "Authorization: Bearer $STUDENT_TOKEN" \
    -H "Content-Type: application/json" > /dev/null
  
  if [ $((i % 100)) -eq 0 ]; then
    echo "Processed $i registrations..."
  fi
done
echo "Load test completed"
```

---

## 12. Quick Reference: All Endpoints

| Method | Endpoint | Auth | Feature |
|--------|----------|------|---------|
| POST | /api/events | ORGANIZER | Create event |
| GET | /api/events | PUBLIC | List approved events |
| GET | /api/events/{id} | PUBLIC | Get event details |
| PUT | /api/events/{id} | ORGANIZER | Update event |
| DELETE | /api/events/{id} | ORGANIZER | Delete event |
| GET | /api/events/search | PUBLIC | Search events |
| GET | /api/events/my-events | ORGANIZER | Organizer's events |
| **POST** | **/api/events/{id}/register** | **STUDENT** | **Register for event** |
| **PUT** | **/api/events/{eventId}/attendance/{userId}** | **ORGANIZER** | **Mark attendance** |
| **PUT** | **/api/events/{id}/complete** | **ORGANIZER** | **Complete event** |
| **PUT** | **/api/events/{eventId}/participants/{userId}/prize** | **ORGANIZER** | **Award prize** |
| GET | /api/admin/events/pending | ADMIN | Get pending events |
| PUT | /api/admin/events/{id}/approve | ADMIN | Approve event |
| PUT | /api/admin/events/{id}/reject | ADMIN | Reject event |

---

**Last Updated**: March 29, 2025  
**Version**: 1.0.0
