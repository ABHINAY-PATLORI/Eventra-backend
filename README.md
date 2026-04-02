# College Event Management System - Backend

A production-ready Spring Boot backend for managing college events with JWT authentication, role-based authorization, and comprehensive event management features.

## Project Structure

```
src/main/java/com/college/
├── controller/          # REST API controllers
├── service/            # Business logic services
├── entity/             # JPA entities
├── repository/         # Data access layer
├── dto/               # Data Transfer Objects
├── security/          # JWT and authentication
├── config/            # Spring configurations
├── exception/         # Custom exceptions and global error handler
└── EventManagementApplication.java  # Main class

src/main/resources/
├── application.properties  # Application configuration
└── ...
```

## Technologies Used

- **Java 17** - Programming language
- **Spring Boot 3.2.0** - Framework
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - ORM and data persistence
- **MySQL 8.0** - Database
- **JWT (JJWT 0.12.3)** - Token-based authentication
- **Lombok** - Reduce boilerplate code
- **Maven** - Build tool

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Git

## Setup Instructions

### 1. Clone and Build

```bash
cd c:\mini-project\Backend
```

### 2. Configure Database

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/college_events?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Generate JAR and Run

```bash
mvn clean package
java -jar target/event-management-system-1.0.0.jar
```

Or run directly:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

#### Register User
```
POST /auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "role": "STUDENT"  // Optional: STUDENT (default), ORGANIZER
}

Response: 201 Created
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "STUDENT",
    "createdAt": "2024-03-26T10:30:00"
  }
}
```

#### Login User
```
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}

Response: 200 OK
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "STUDENT"
  }
}
```

### Event Endpoints

#### Get All Approved Events
```
GET /events?page=0&size=10&sort=asc
Authorization: Bearer {token}

Response: 200 OK
{
  "success": true,
  "message": "Events fetched successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Java Workshop",
        "description": "Learn Java fundamentals",
        "eventDate": "2024-04-15T10:00:00",
        "location": "Room 101",
        "capacity": 50,
        "imageUrl": "https://...",
        "status": "APPROVED",
        "registeredCount": 25,
        "hasCapacity": true,
        "createdAt": "2024-03-26T10:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 15,
    "totalPages": 2,
    "last": false
  }
}
```

#### Search Events by Title
```
GET /events/search?title=Java&page=0&size=10&sort=asc
Authorization: Bearer {token}

Response: 200 OK
```

#### Get Event by ID
```
GET /events/{id}
Authorization: Bearer {token}

Response: 200 OK
{
  "success": true,
  "message": "Event fetched successfully",
  "data": {
    "id": 1,
    "title": "Java Workshop",
    ...
  }
}
```

#### Create Event (ORGANIZER/ADMIN Only)
```
POST /events
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Python Workshop",
  "description": "Learn Python programming",
  "eventDate": "2024-05-01T14:00:00",
  "location": "Hall A",
  "capacity": 100,
  "imageUrl": "https://example.com/image.jpg"
}

Response: 201 Created
```

#### Update Event (Own Events or ADMIN)
```
PUT /events/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Updated Workshop Title",
  ...
}

Response: 200 OK
```

#### Delete Event (Own Events or ADMIN)
```
DELETE /events/{id}
Authorization: Bearer {token}

Response: 200 OK
{
  "success": true,
  "message": "Event deleted successfully"
}
```

#### Get My Events (ORGANIZER)
```
GET /events/my-events/list?page=0&size=10
Authorization: Bearer {token}

Response: 200 OK
```

### Registration Endpoints

#### Register for Event (STUDENT Only)
```
POST /registrations/{eventId}
Authorization: Bearer {token}

Response: 201 Created
{
  "success": true,
  "message": "Registered successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "userName": "John Doe",
    "eventId": 1,
    "eventTitle": "Java Workshop",
    "status": "REGISTERED",
    "registeredAt": "2024-03-26T11:00:00"
  }
}
```

#### Unregister from Event (STUDENT Only)
```
DELETE /registrations/{eventId}
Authorization: Bearer {token}

Response: 200 OK
{
  "success": true,
  "message": "Unregistered successfully"
}
```

#### Get My Registrations (STUDENT Only)
```
GET /registrations/my-events/list?page=0&size=10
Authorization: Bearer {token}

Response: 200 OK
```

#### Get Event Registrations (ADMIN Only)
```
GET /registrations/events/{eventId}?page=0&size=10
Authorization: Bearer {token}

Response: 200 OK
```

### Admin Endpoints

#### Get All Users (ADMIN Only)
```
GET /admin/users?page=0&size=10
Authorization: Bearer {token}

Response: 200 OK
```

#### Get Pending Events (ADMIN Only)
```
GET /admin/events/pending?page=0&size=10
Authorization: Bearer {token}

Response: 200 OK
```

#### Approve Event (ADMIN Only)
```
PUT /admin/events/{id}/approve
Authorization: Bearer {token}

Response: 200 OK
{
  "success": true,
  "message": "Event approved successfully",
  "data": {
    "id": 1,
    "status": "APPROVED",
    ...
  }
}
```

#### Reject Event (ADMIN Only)
```
PUT /admin/events/{id}/reject
Authorization: Bearer {token}

Response: 200 OK
```

#### Delete User (ADMIN Only)
```
DELETE /admin/users/{id}
Authorization: Bearer {token}

Response: 200 OK
{
  "success": true,
  "message": "User deleted successfully"
}
```

#### Change User Role (ADMIN Only)
```
PUT /admin/users/{id}/role?role=ORGANIZER
Authorization: Bearer {token}

Response: 200 OK
{
  "success": true,
  "message": "User role changed successfully",
  "data": {
    "id": 1,
    "role": "ORGANIZER",
    ...
  }
}
```

## User Roles and Permissions

### STUDENT
- View all approved events
- Register for events
- Unregister from events
- View own registrations
- View own registered events

### ORGANIZER
- Create new events (pending approval by admin)
- View own created events
- Update own events
- Delete own events
- Cannot view admin functions

### ADMIN
- View all users
- View all pending events and approve/reject
- Delete users
- Change user roles
- All ORGANIZER permissions
- Cannot directly register for events as STUDENT

## Authentication

All authenticated endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer {jwt_token}
```

### JWT Token Structure
- **Expiration**: 24 hours
- **Algorithm**: HS512
- **Subject**: User email

## Error Handling

The API uses standard HTTP status codes and returns consistent error responses:

```json
{
  "success": false,
  "message": "Error description",
  "timestamp": 1711434000000
}
```

### Common Status Codes
- `200 OK` - Successful GET/PUT/DELETE
- `201 Created` - Successful POST
- `400 Bad Request` - Validation errors
- `401 Unauthorized` - Authentication failed
- `403 Forbidden` - Authorization failed
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Database Schema

### Users Table
- `id` (PK, AUTO_INCREMENT)
- `name` (VARCHAR 255, NOT NULL)
- `email` (VARCHAR 255, UNIQUE, NOT NULL)
- `password` (VARCHAR 255, NOT NULL)
- `role` (ENUM: STUDENT, ORGANIZER, ADMIN)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### Events Table
- `id` (PK, AUTO_INCREMENT)
- `title` (VARCHAR 255, NOT NULL)
- `description` (TEXT)
- `event_date` (DATETIME, NOT NULL)
- `location` (VARCHAR 255, NOT NULL)
- `capacity` (INT, NOT NULL)
- `image_url` (VARCHAR 500)
- `status` (ENUM: PENDING, APPROVED, REJECTED)
- `created_by_id` (FK to Users, NOT NULL)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### Registrations Table
- `id` (PK, AUTO_INCREMENT)
- `user_id` (FK to Users, NOT NULL)
- `event_id` (FK to Events, NOT NULL)
- `status` (ENUM: REGISTERED, CANCELLED)
- `registered_at` (TIMESTAMP)
- `cancelled_at` (TIMESTAMP)
- UNIQUE KEY (user_id, event_id)

## Security Features

1. **JWT Authentication** - Stateless, token-based authentication
2. **BCrypt Password Encryption** - Secure password hashing
3. **Role-Based Access Control** - Method-level authorization
4. **SQL Injection Prevention** - Using JPA with parameterized queries
5. **CORS Configuration** - Restricted to frontend domain
6. **Exception Handling** - Global exception handler with sensitive info masking
7. **Input Validation** - Bean validation annotations

## Performance Optimizations

1. **Pagination** - All list endpoints support pagination
2. **Database Indexing** - Unique constraints on email, FK indexes
3. **Fetch Strategies** - LazyLoading to prevent N+1 queries
4. **Query Optimization** - Optimized repository methods
5. **Connection Pooling** - HikariCP for database connections

## Development

### Build the Project
```bash
mvn clean package
```

### Run Tests
```bash
mvn test
```

### Generate JavaDoc
```bash
mvn javadoc:javadoc
```

## Production Deployment Checklist

- [ ] Set production database credentials
- [ ] Update JWT secret key
- [ ] Enable HTTPS/SSL
- [ ] Configure CORS for production domain
- [ ] Set appropriate logging levels
- [ ] Enable database backups
- [ ] Configure monitoring and alerting
- [ ] Review security policies
- [ ] Load balancer configuration
- [ ] Database connection pooling tuning

## Future Enhancements

- [ ] Email notifications for event updates
- [ ] Event ratings and reviews
- [ ] Calendar integration
- [ ] SMS notifications
- [ ] File upload for event images
- [ ] Payment integration
- [ ] Advanced search filters
- [ ] Analytics and reporting
- [ ] Real-time notifications using WebSockets
- [ ] API rate limiting
- [ ] Caching with Redis
- [ ] Event categories/tags

## License

This project is licensed under the MIT License.

## Support

For issues and questions, please contact the development team.

---

**Last Updated**: March 26, 2024
