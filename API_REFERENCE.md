# API Reference Guide

**Base URL**: `http://localhost:8080/api`

---

## 🔐 Authentication

### Register User
```
POST /auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "Password@123",
  "phoneNumber": "+1234567890"
}

Response: 201 Created
{
  "status": true,
  "message": "User registered successfully. OTP sent to email",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "STUDENT",
    "verified": false
  }
}
```

### Login User
```
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "Password@123"
}

Response: 200 OK
{
  "status": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "role": "STUDENT"
    }
  }
}
```

### Verify OTP
```
POST /auth/verify-otp
Content-Type: application/json

{
  "email": "john@example.com",
  "otp": "123456"
}

Response: 200 OK
```

---

## 👥 Users

### Get My Profile
```
GET /users/me
Authorization: Bearer <JWT_TOKEN>

Response: 200 OK
{
  "status": true,
  "message": "Profile fetched successfully",
  "data": { ... }
}
```

### Update My Profile
```
PUT /users/me
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "name": "John Updated"
}
```

### Get All Users (Admin)
```
GET /users?page=0&size=10&sort=asc
Authorization: Bearer <JWT_TOKEN>

Response: 200 OK
{
  "data": {
    "content": [...],
    "totalElements": 50,
    "totalPages": 5,
    "pageNumber": 0,
    "pageSize": 10,
    "isLast": false
  }
}
```

### Search Users (Admin)
```
GET /users/search?q=john&page=0&size=10
Authorization: Bearer <JWT_TOKEN>
```

### Get Users by Role (Admin)
```
GET /users/role/STUDENT?page=0&size=10
Authorization: Bearer <JWT_TOKEN>
```

---

## 📅 Events

### Get All Events
```
GET /events?page=0&size=10&sort=asc

Response: 200 OK
```

### Get Event by ID
```
GET /events/{id}
```

### Search Events
```
GET /events/search?title=python&page=0&size=10
```

### Create Event
```
POST /events
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
Role: ORGANIZER, ADMIN

{
  "title": "Python Workshop",
  "description": "Learn Python programming",
  "date": "2024-04-15",
  "time": "14:30:00",
  "location": "Main Hall",
  "capacity": 50,
  "imageUrl": "https://..."
}
```

### Update Event
```
PUT /events/{id}
Authorization: Bearer <JWT_TOKEN>
Role: ORGANIZER (own events), ADMIN
```

### Delete Event
```
DELETE /events/{id}
Authorization: Bearer <JWT_TOKEN>
Role: ORGANIZER (own events), ADMIN
```

---

## 🏛️ Venues

### Get All Venues
```
GET /venues?page=0&size=10&sort=asc

Response: 200 OK
{
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Main Assembly Hall",
        "address": "123 College Ave",
        "capacity": 500,
        "city": "New York",
        "contactPhone": "+1-800-123-4567",
        "active": true
      }
    ]
  }
}
```

### Get Venue by ID
```
GET /venues/{id}
```

### Search Venues
```
GET /venues/search?name=assembly&page=0&size=10
```

### Create Venue (Admin)
```
POST /venues
Authorization: Bearer <JWT_TOKEN>
Role: ADMIN
Content-Type: application/json

{
  "name": "Conference Room A",
  "description": "Professional conference room",
  "address": "456 Business Park",
  "capacity": 100,
  "city": "Boston",
  "state": "MA",
  "zipCode": "02101",
  "contactPhone": "+1-800-555-1234",
  "contactEmail": "venue@example.com"
}
```

### Update Venue (Admin)
```
PUT /venues/{id}
Authorization: Bearer <JWT_TOKEN>
Role: ADMIN
```

### Delete Venue (Admin)
```
DELETE /venues/{id}
Authorization: Bearer <JWT_TOKEN>
Role: ADMIN
```

---

## 📅 Bookings

### Get All Bookings (Admin)
```
GET /bookings?page=0&size=10&sort=asc
Authorization: Bearer <JWT_TOKEN>
Role: ADMIN

Response: 200 OK
{
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 1,
        "userName": "john@example.com",
        "venueId": 1,
        "venueName": "Main Hall",
        "bookingDate": "2024-04-15",
        "startTime": "14:00",
        "endTime": "17:00",
        "attendees": 50,
        "status": "CONFIRMED",
        "totalCost": 500.00
      }
    ]
  }
}
```

### Get My Bookings
```
GET /bookings/my-bookings
Authorization: Bearer <JWT_TOKEN>
Role: STUDENT, ORGANIZER, ADMIN
```

### Get Booking by ID
```
GET /bookings/{id}
Authorization: Bearer <JWT_TOKEN>
```

### Create Booking
```
POST /bookings
Authorization: Bearer <JWT_TOKEN>
Role: STUDENT, ORGANIZER, ADMIN
Content-Type: application/json

{
  "venueId": 1,
  "eventId": 1,
  "bookingDate": "2024-04-15",
  "startTime": "14:00:00",
  "endTime": "17:00:00",
  "attendees": 50,
  "purpose": "Workshop Event",
  "totalCost": 500.00,
  "notes": "Special requirements..."
}
```

### Update Booking
```
PUT /bookings/{id}
Authorization: Bearer <JWT_TOKEN>
```

### Confirm Booking (Admin)
```
PUT /bookings/{id}/confirm
Authorization: Bearer <JWT_TOKEN>
Role: ADMIN
```

### Cancel Booking
```
PUT /bookings/{id}/cancel
Authorization: Bearer <JWT_TOKEN>
```

### Delete Booking (Admin)
```
DELETE /bookings/{id}
Authorization: Bearer <JWT_TOKEN>
Role: ADMIN
```

---

## 💰 Budgets

### Get All Budgets (Admin)
```
GET /budgets?page=0&size=10&sort=asc
Authorization: Bearer <JWT_TOKEN>
Role: ADMIN

Response: 200 OK
{
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Conference Budget",
        "totalAmount": 10000.00,
        "allocatedAmount": 5000.00,
        "spentAmount": 3000.00,
        "remainingAmount": 5000.00,
        "utilizationPercentage": 30.0,
        "status": "ACTIVE"
      }
    ]
  }
}
```

### Get My Budgets
```
GET /budgets/my-budgets
Authorization: Bearer <JWT_TOKEN>
Role: ORGANIZER, ADMIN
```

### Get Event Budgets
```
GET /budgets/event/{eventId}
Authorization: Bearer <JWT_TOKEN>
Role: ORGANIZER, ADMIN
```

### Get Budget by ID
```
GET /budgets/{id}
Authorization: Bearer <JWT_TOKEN>
Role: ORGANIZER, ADMIN
```

### Create Budget
```
POST /budgets
Authorization: Bearer <JWT_TOKEN>
Role: ORGANIZER, ADMIN
Content-Type: application/json

{
  "title": "Annual Conference Budget",
  "description": "Budget for annual conference 2024",
  "eventId": 1,
  "totalAmount": 50000.00,
  "allocatedAmount": 30000.00,
  "category": "Event Planning",
  "notes": "Includes venue, catering, equipment"
}
```

### Update Budget
```
PUT /budgets/{id}
Authorization: Bearer <JWT_TOKEN>
Role: ORGANIZER, ADMIN
```

### Approve Budget (Admin)
```
PUT /budgets/{id}/approve
Authorization: Bearer <JWT_TOKEN>
Role: ADMIN
```

### Delete Budget
```
DELETE /budgets/{id}
Authorization: Bearer <JWT_TOKEN>
Role: ORGANIZER, ADMIN
```

---

## 📝 Registrations

### Register for Event
```
POST /registrations/{eventId}
Authorization: Bearer <JWT_TOKEN>
Role: STUDENT

Response: 201 Created
{
  "status": true,
  "message": "Registered successfully",
  "data": {
    "id": 1,
    "eventId": 1,
    "userId": 1,
    "status": "CONFIRMED"
  }
}
```

### Unregister from Event
```
DELETE /registrations/{eventId}
Authorization: Bearer <JWT_TOKEN>
Role: STUDENT
```

### Get My Registrations
```
GET /registrations/my-events?page=0&size=10
Authorization: Bearer <JWT_TOKEN>
Role: STUDENT
```

### Get Event Registrations (Admin)
```
GET /registrations/events/{eventId}?page=0&size=10
Authorization: Bearer <JWT_TOKEN>
Role: ADMIN
```

---

## 🛡️ Admin

### Get All Users (Admin)
```
GET /admin/users?page=0&size=10
Authorization: Bearer <JWT_TOKEN>
Role: ADMIN
```

### Get Pending Events (Admin)
```
GET /admin/events/pending?page=0&size=10
Authorization: Bearer <JWT_TOKEN>
Role: ADMIN
```

### Approve Event (Admin)
```
PUT /admin/events/{id}/approve
Authorization: Bearer <JWT_TOKEN>
Role: ADMIN
```

### Reject Event (Admin)
```
PUT /admin/events/{id}/reject
Authorization: Bearer <JWT_TOKEN>
Role: ADMIN
```

### Delete User (Admin)
```
DELETE /admin/users/{id}
Authorization: Bearer <JWT_TOKEN>
Role: ADMIN
```

---

## 📊 Response Format

### Success Response
```json
{
  "status": true,
  "message": "Operation successful",
  "data": { ... }
}
```

### Error Response
```json
{
  "status": false,
  "message": "Error description",
  "timestamp": "2024-03-27T10:30:00",
  "error": "Bad Request"
}
```

### Paginated Response
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "pageNumber": 0,
  "pageSize": 10,
  "isLast": false
}
```

---

## 🔑 Authorization

All protected endpoints require:
```
Authorization: Bearer <JWT_TOKEN>
```

Obtain token from: `POST /auth/login`

---

## 📋 Roles

| Role | Description | Permissions |
|------|-------------|-------------|
| STUDENT | Regular user | Create bookings, register for events |
| ORGANIZER | Event creator | Manage own events, budgets, venues |
| ADMIN | System administrator | Full access to all resources |

---

## ✅ Status Codes

| Code | Meaning |
|------|---------|
| 200 | OK - Request successful |
| 201 | Created - Resource created |
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Missing/invalid token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 500 | Internal Server Error |

---

## 🌐 CORS Headers

Frontend requests from:
- `http://localhost:3000`
- `http://localhost:5173`

Are allowed with credentials.

---

**Last Updated**: March 27, 2026  
**Version**: 1.0.0
