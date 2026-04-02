# Event Management System - Architecture & Implementation

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     FRONTEND (React/Vue)                     │
│                  http://localhost:3000                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP/REST APIs
                              │
┌─────────────────────────────────────────────────────────────┐
│                  API Gateway (Port 8080)                     │
│                   Spring Boot Application                    │
├─────────────────────────────────────────────────────────────┤
│  Controllers Layer                                            │
│  ├── AuthController          /api/auth                      │
│  ├── EventController          /api/events                    │
│  ├── AdminController          /api/admin                     │
│  ├── BookingController        /api/bookings                  │
│  ├── RegistrationController   /api/registrations            │
│  └── VenueController          /api/venues                    │
├─────────────────────────────────────────────────────────────┤
│  Service Layer (Business Logic)                              │
│  ├── AuthService             (Register, Login, JWT)         │
│  ├── OtpService              (OTP generation & verification) │
│  ├── EventService            (CRUD, Approval workflow)      │
│  ├── AdminService            (Admin operations)             │
│  ├── UserService             (User management)              │
│  ├── EmailService            (SMTP integration)             │
│  └── Multiple services...                                    │
├─────────────────────────────────────────────────────────────┤
│  Repository Layer (Data Access)                              │
│  ├── UserRepository          (JPA)                          │
│  ├── EventRepository         (JPA)                          │
│  ├── RegistrationRepository  (JPA)                          │
│  ├── BookingRepository       (JPA)                          │
│  └── Other repositories...                                   │
├─────────────────────────────────────────────────────────────┤
│  Security & Cross-Cutting                                    │
│  ├── SecurityConfig          (JWT, CORS, Role-based auth)   │
│  ├── JwtProvider             (Token generation)             │
│  ├── JwtAuthenticationFilter (Request token validation)     │
│  ├── GlobalExceptionHandler  (Centralized error handling)   │
│  └── CorsConfig              (Cross-origin setup)           │
├─────────────────────────────────────────────────────────────┤
│  Database Layer                                              │
│  ├── MySQL 8.0                                              │
│  ├── HikariCP Connection Pool                               │
│  └── Hibernate ORM                                           │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│               External Services                              │
│  ├── Gmail SMTP (Email OTP delivery)                        │
│  └── Database Server (MySQL)                                │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Module Breakdown

### 1. Authentication Module (`com.college.controller.AuthController`)

**Responsibilities:**
- User registration with email validation
- OTP-based email verification
- Passwordless login via OTP
- JWT token generation

**Key Flows:**
```
Register → OTP sent → OTP verified → Account active → Can login
         ↓
Login → OTP sent → OTP verified → JWT token → Authenticated access
```

#### Classes:
- **AuthController** - HTTP request handling
- **AuthService** - Business logic for auth operations
- **OtpService** - OTP generation, validation, expiry management
- **JwtProvider** - JWT token creation and validation

---

### 2. Event Management Module

#### Event Creation & Approval Workflow
```
Organizer creates event (PENDING)
         ↓
Admin reviews in pending list
         ↓
Admin approves/rejects
         ↓
If APPROVED → Visible to all students
If REJECTED → Only visible to creator
```

#### Classes:
- **EventController** - Event CRUD endpoints
- **EventService** - Event business logic
- **AdminController** - Admin approval endpoints
- **AdminService** - Admin operations
- **Event Entity** - JPA entity with status (PENDING, APPROVED, REJECTED)

---

### 3. Email Module (`com.college.service.EmailService`)

**Responsibilities:**
- SMTP configuration validation
- OTP email composition
- Asynchronous email sending
- Error handling and logging

**Flow:**
```
User registers/logins
        ↓
OtpService calls EmailService.sendOtpEmail()
        ↓
EmailService.sendOtpEmail() (Async with @Async)
        ↓
JavaMailSender sends via Gmail SMTP
        ↓
Email delivered to user inbox
```

---

### 4. Role-Based Access Control (RBAC)

**User Roles:**
| Role | Permissions |
|------|-----------|
| **STUDENT** | View approved events, participate, view history |
| **ORGANIZER** | Create events, edit own, delete own |
| **ADMIN** | Approve events, reject events, manage users |

**Implementation:**
- Spring Security `@PreAuthorize` annotations
- Role-based routing
- Controller-level access control

```java
@PostMapping("/events")
@PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
public ResponseEntity<?> createEvent(...) { ... }

@PutMapping("/admin/events/{id}/approve")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> approveEvent(...) { ... }
```

---

## 📊 Database Schema

### Entities & Relationships

```
User (1) ──────────→ (Many) Event (created by)
User (1) ──────────→ (Many) Registration (participated in)
Event (1) ─────────→ (Many) Registration (event registrations)
Event (1) ─────────→ (Many) Booking (venue booking)
Venue (1) ─────────→ (Many) Event (hosted in)
Budget ────────────→ Event (allocated for)
```

### Key Tables

```sql
-- Users
users: id, name, email, password, role, otp, otp_expiry, verified

-- Events  
events: id, title, description, date, time, location, capacity, status, created_by

-- Registrations (Participation)
registrations: id, user_id, event_id, status, prize, timestamp

-- Venues
venues: id, name, location, capacity

-- Bookings
bookings: id, event_id, venue_id, user_id, status

-- Budget
budgets: id, event_id, title, total_amount, allocated_amount, spent_amount, category
```

---

## 🔐 Security Implementation

### JWT Authentication Flow

```
1. User registers/verifies OTP
   ↓
2. JwtProvider generates JWT:
   - Header: { "alg": "HS256", "typ": "JWT" }
   - Payload: { "sub": "email", "iat": timestamp, "exp": expiry }
   - Signature: HMAC-SHA256(secret)
   ↓
3. Token sent to client
   ↓
4. Client includes in Authorization header:
   "Authorization: Bearer <jwt_token>"
   ↓
5. JwtAuthenticationFilter validates token
   ↓
6. On valid token → Grant access
   On invalid → 401 Unauthorized
```

### OTP Security

```
1. SecureRandom generates 6-digit OTP
2. OTP stored in User entity with expiry timestamp
3. OTP deleted immediately after successful verification
4. Failed attempts logged for audit trail
5. Expired OTPs removed after 5 minutes
6. Re-registration generates new OTP
```

### CORS Configuration

```
Allowed Origins:
├── http://localhost:3000  (React frontend)
├── http://localhost:5173  (Vite frontend)
└── http://localhost:8080  (API itself)

Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
Allowed Headers: Content-Type, Authorization, X-Requested-With
Credentials: true
Max Age: 3600s (1 hour)
```

---

## 🛠️ OTP System Details

### OTP Generation
```
✓ Algorithm: SecureRandom with cryptographic entropy
✓ Length: 6 digits (000000-999999)
✓ Entropy: ~20 bits of security
✓ Generation time: < 1ms
```

### OTP Lifecycle

```
User Clicks Register/Login
        ↓
OtpService.generateOtp() → "482916"
        ↓
OtpEntry created:
  - email: user@example.com
  - otp: "482916"
  - expiryTime: now + 5 minutes
        ↓
OtpEntry stored in User entity
        ↓
EmailService.sendOtpEmail() async called
        ↓
User enters OTP
        ↓
OtpService.verifyOtp() checks:
  ✓ OTP exists
  ✓ Not expired
  ✓ Matches user input
        ↓
✓ Verification successful
        ↓
OTP cleared from database
User marked as verified
JWT token generated
        ↓
OTP lifecycle complete ✓
```

---

## 🌐 REST API Design

### Standard Response Format

**Success Response (200-201):**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

**Error Response (4xx-5xx):**
```json
{
  "success": false,
  "message": "Error description",
  "statusCode": 400,
  "timestamp": "2026-03-29T10:43:02"
}
```

### HTTP Status Codes Used

| Code | Meaning |
|------|---------|
| 200 | OK - Request successful |
| 201 | Created - Resource created |
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Missing/Invalid token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource doesn't exist |
| 409 | Conflict - Duplicate email, etc. |
| 500 | Internal Server Error |

---

## 🔄 Event Approval Workflow (Admin)

```
Step 1: Organizer creates event
        Event status = PENDING
                ↓
Step 2: Admin views pending events
        GET /api/admin/events/pending
                ↓
Step 3: Admin reviews event details
                ↓
Step 4: Admin takes decision:
        
   Option A: APPROVE
   ├─ PUT /api/admin/events/{id}/approve
   ├─ Event status → APPROVED
   └─ Visible to all students
   
   Option B: REJECT
   ├─ PUT /api/admin/events/{id}/reject
   ├─ Event status → REJECTED
   └─ Not visible to students
```

---

## 📧 Email Service Integration

### Gmail SMTP Configuration

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

### Setup Steps

1. **Enable 2-Factor Authentication**: https://myaccount.google.com/security
2. **Generate App Password**: https://myaccount.google.com/apppasswords
3. **Use App Password as** `MAIL_PASSWORD` in environment

---

## 🗂️ Project Structure

```
src/main/java/com/college/
├── EventManagementApplication.java (Main entry point)
├── config/
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   └── WebMvcConfig.java
├── controller/
│   ├── AuthController.java
│   ├── EventController.java
│   ├── AdminController.java
│   ├── UserController.java
│   ├── BookingController.java
│   ├── RegistrationController.java
│   ├── BudgetController.java
│   └── VenueController.java
├── service/
│   ├── AuthService.java
│   ├── OtpService.java
│   ├── EventService.java
│   ├── AdminService.java
│   ├── UserService.java
│   ├── EmailService.java
│   ├── BookingService.java
│   ├── RegistrationService.java
│   ├── VenueService.java
│   └── BudgetService.java
├── repository/
│   ├── UserRepository.java
│   ├── EventRepository.java
│   ├── RegistrationRepository.java
│   ├── BookingRepository.java
│   ├── BudgetRepository.java
│   ├── VenueRepository.java
│   └── ...
├── entity/
│   ├── User.java
│   ├── Event.java
│   ├── Registration.java
│   ├── Booking.java
│   ├── Budget.java
│   ├── Venue.java
│   └── OtpEntry.java
├── dto/
│   ├── UserDTO.java
│   ├── EventDTO.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── RegisterRequest.java
│   ├── SendOtpRequest.java
│   ├── VerifyOtpRequest.java
│   ├── PageResponse.java
│   ├── ApiResponse.java
│   └── ... (other DTOs)
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── BadRequestException.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   ├── ForbiddenException.java
│   └── EmailDeliveryException.java
└── security/
    ├── SecurityConfig.java
    ├── JwtProvider.java
    ├── JwtAuthenticationFilter.java
    ├── CustomUserDetailsService.java
    └── ...

src/main/resources/
├── application.properties
├── application-dev.properties
└── schema.sql
```

---

## 🚀 Deployment Checklist

- [ ] Update JWT secret in production
- [ ] Configure Gmail App Password for email
- [ ] Set database credentials
- [ ] Configure CORS for production domain
- [ ] Enable HTTPS
- [ ] Set `DDL_AUTO=validate` (not `update`)
- [ ] Configure proper logging levels
- [ ] Enable rate limiting
- [ ] Setup database backups
- [ ] Test all APIs in production environment
- [ ] Monitor error logs and metrics
- [ ] Setup SSL certificates
- [ ] Configure firewall rules

---

## 📈 Performance Considerations

- **Connection pooling**: HikariCP with 10 connections
- **Query optimization**: Lazy loading, proper indexing
- **Async operations**: Email sending non-blocking
- **Caching**: Can be added for event listings
- **Database**: Indexed on email, eventId, userId
- **Pagination**: Default 10 items per page

---

## 🧪 Testing Strategy

### Unit Tests
- Service layer logic
- OTP generation and validation
- JWT token operations
- Exception handling

### Integration Tests
- Full API workflows
- Database operations
- Email service integration

### E2E Tests
- Complete user journeys
- Multi-step workflows
- Role-based access

---

## 📝 Logging Strategy

| Level | Usage |
|-------|-------|
| DEBUG | Detailed operation flow, authentication events |
| INFO | User registration, API requests, email sends |
| WARN | Deprecated features, configuration issues |
| ERROR | Authentication failures, database errors |

---

## 🔄 Continuous Integration/Deployment

```bash
# Build
mvn clean install -DskipTests

# Run Tests
mvn test

# Package
mvn package

# Docker Push
docker build -t event-management:1.0 .
docker push registry/event-management:1.0

# Deploy
kubernetes apply -f deployment.yaml
```
