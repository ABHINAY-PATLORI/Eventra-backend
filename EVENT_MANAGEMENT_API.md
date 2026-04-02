# Event Management System - Complete Backend API Guide

## 🚀 System Overview

A production-ready Spring Boot backend for college event management with:
- **Email OTP Authentication** (Register & Login via OTP)
- **Role-Based Access Control** (STUDENT, ORGANIZER, ADMIN)
- **Event Approval Workflow** (PENDING → APPROVED/REJECTED)
- **Event Participation Tracking**
- **CORS Enabled** for frontend integration

---

## 🔐 Authentication System (Email OTP Based)

### 1. Register - Send OTP
**Endpoint:** `POST /api/auth/register`

**Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secure123",
  "role": "STUDENT"
}
```

**Response:**
```json
{
  "success": true,
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

**Notes:**
- OTP sent to email (requires SMTP configuration)
- OTP expires in 5 minutes
- User not verified until OTP is validated
- Roles: STUDENT (default), ORGANIZER, ADMIN

---

### 2. Verify Registration OTP
**Endpoint:** `POST /api/auth/verify-otp`

**Request:**
```json
{
  "email": "john@example.com",
  "otp": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP verified successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "STUDENT",
    "verified": true,
    "passwordAuthentication": false
  }
}
```

---

### 3. Login - Send OTP to Registered Email
**Endpoint:** `POST /api/auth/send-otp`

**Request:**
```json
{
  "email": "john@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP sent successfully"
}
```

---

### 4. Verify Login OTP
**Endpoint:** `POST /api/auth/verify-otp` (same as registration)

**Request:**
```json
{
  "email": "john@example.com",
  "otp": "654321"
}
```

**Response:** (JWT Token received)

---

### 5. Resend OTP
**Endpoint:** `POST /api/auth/resend-otp`

**Request:**
```json
{
  "email": "john@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP resent successfully"
}
```

---

## 📅 Event Management APIs

### Student APIs (Approved Events Only)

#### 1. Get All Approved Events
**Endpoint:** `GET /api/events`

**Query Parameters:**
- `page` (default: 0)
- `size` (default: 10)
- `sort` (default: asc)

**Response:**
```json
{
  "success": true,
  "message": "Events fetched successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Java Masterclass 2026",
        "description": "Learn advanced Java concepts",
        "date": "2026-04-15",
        "time": "14:00:00",
        "location": "Auditorium A",
        "capacity": 150,
        "imageUrl": "https://...",
        "status": "APPROVED",
        "createdBy": "organizer@college.com"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

#### 2. Search Events by Title
**Endpoint:** `GET /api/events/search?title=Java`

**Query Parameters:**
- `title` (required)
- `page` (default: 0)
- `size` (default: 10)
- `sort` (default: asc)

---

#### 3. Get Event Details
**Endpoint:** `GET /api/events/{id}`

---

#### 4. Participate in Event
**Endpoint:** `POST /api/events/{id}/participate`

**Response:**
```json
{
  "success": true,
  "message": "Registered for event successfully"
}
```

---

#### 5. View Participation History
**Endpoint:** `GET /api/student/history`

---

### Organizer APIs (Create & Manage Events)

#### 1. Create Event
**Endpoint:** `POST /api/events` (Requires ORGANIZER or ADMIN role)

**Request:**
```json
{
  "title": "React Workshop",
  "description": "Learn React from basics",
  "date": "2026-05-10",
  "time": "10:00:00",
  "location": "Lab 2",
  "capacity": 50,
  "imageUrl": "https://..."
}
```

**Response:**
```json
{
  "success": true,
  "message": "Event created successfully",
  "data": {
    "id": 2,
    "title": "React Workshop",
    "status": "PENDING",
    "createdBy": "organizer@college.com"
  }
}
```

---

#### 2. Update Event
**Endpoint:** `PUT /api/events/{id}`

---

#### 3. Delete Event
**Endpoint:** `DELETE /api/events/{id}`

---

### Admin APIs (Approval Workflow)

#### 1. Get All Pending Events
**Endpoint:** `GET /api/admin/events/pending` (Requires ADMIN role)

---

#### 2. Approve Event
**Endpoint:** `PUT /api/admin/events/{id}/approve`

**Response:**
```json
{
  "success": true,
  "message": "Event approved successfully",
  "data": {
    "id": 1,
    "title": "Java Masterclass 2026",
    "status": "APPROVED"
  }
}
```

---

#### 3. Reject Event
**Endpoint:** `PUT /api/admin/events/{id}/reject`

---

#### 4. Get All Users
**Endpoint:** `GET /api/admin/users`

---

## 👥 User Roles

| Role | Permissions |
|------|-----------|
| STUDENT | View approved events, participate in events, view history |
| ORGANIZER | Create events, edit own events, delete own events |
| ADMIN | View all events, approve/reject events, manage users |

---

## 🔧 Technical Stack

```
Spring Boot 3.2.0
Java 21
Spring Security (JWT + OTP)
Spring Data JPA
Hibernate 6.3.1
MySQL 8.0
JavaMailSender (SMTP)
Lombok
Validation (Jakarta)
```

---

## 📧 Email Configuration

Update `.env` or `application.properties`:

```properties
# Gmail SMTP
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=your-email@gmail.com
```

**Gmail Setup:**
1. Enable 2FA on Gmail account
2. Generate "App Password" (16 characters)
3. Use App Password in `MAIL_PASSWORD`

---

## 🔄 OTP Flow

```
1. User enters email → /api/auth/register
   ↓
2. Backend generates 6-digit OTP
   ↓
3. OTP sent to email (expires in 5 minutes)
   ↓
4. User enters OTP → /api/auth/verify-otp
   ↓
5. OTP validated & JWT token generated
   ↓
6. User authenticated & can access APIs
```

---

## 🚀 Database Schema

### Users Table
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role ENUM('STUDENT', 'ORGANIZER', 'ADMIN') DEFAULT 'STUDENT',
  otp VARCHAR(6),
  otp_expiry TIMESTAMP,
  is_verified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);
```

### Events Table
```sql
CREATE TABLE events (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  date DATE NOT NULL,
  time TIME NOT NULL,
  location VARCHAR(255) NOT NULL,
  capacity INT NOT NULL,
  image_url VARCHAR(500),
  status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
  created_by BIGINT,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (created_by) REFERENCES users(id)
);
```

### Registrations Table
```sql
CREATE TABLE registrations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  event_id BIGINT NOT NULL,
  status VARCHAR(50),
  prize VARCHAR(255),
  timestamp TIMESTAMP,
  cancelled_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (event_id) REFERENCES events(id)
);
```

---

## 🧪 Testing APIs with cURL

### Register New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "test123",
    "role": "STUDENT"
  }'
```

### Get Events
```bash
curl http://localhost:8080/api/events
```

### Search Events
```bash
curl "http://localhost:8080/api/events/search?title=Java"
```

---

## ⚙️ Server Configuration

- **Port:** 8080
- **Base URL:** http://localhost:8080
- **Database:** MySQL on localhost:3306
- **CORS:** Enabled for http://localhost:3000, http://localhost:5173

---

## 🛡️ Security Features

✅ Email OTP Authentication (6-digit, 5-min expiry)  
✅ JWT Token-based Authorization  
✅ Role-Based Access Control (RBAC)  
✅ CORS enabled for frontend  
✅ Password Encryption (BCrypt)  
✅ Secure OTP generation (SecureRandom)  
✅ Stateless session management  

---

## 📝 Example Workflow

### 1. Student Registration & Login

```
Step 1: POST /api/auth/register
Body: { name, email, password, role: "STUDENT" }
↓
OTP sent to email

Step 2: POST /api/auth/verify-otp
Body: { email, otp }
↓
JWT Token received → User verified

Step 3: GET /api/events
Header: Authorization: Bearer <JWT_TOKEN>
↓
View approved events

Step 4: POST /api/events/{id}/participate
Header: Authorization: Bearer <JWT_TOKEN>
↓
Successfully registered for event
```

### 2. Organizer Event Creation

```
Step 1: POST /api/events
Header: Authorization: Bearer <JWT_TOKEN>
Role: ORGANIZER
Body: { title, date, time, location, capacity }
↓
Event created with status: PENDING

Step 2: Admin reviews at /api/admin/events/pending
↓
PUT /api/admin/events/{id}/approve
↓
Event now APPROVED and visible to students
```

---

## 🔗 CORS Configuration

Allows requests from:
- `http://localhost:3000`
- `http://localhost:5173`
- `http://localhost:8080`

---

## 📞 Support & Notes

- All timestamps in UTC+5:30 (IST)
- Email OTP valid for 5 minutes only
- Failed OTP attempts show remaining time
- Passwords must be at least 8 characters
- Event capacity cannot be exceeded
- Organizers can only edit/delete own events
