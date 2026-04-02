# Spring Boot Backend - Setup & Run Guide

## ✅ Fixed Issues

### 1. **CORS Configuration**
   - ✅ Added support for `http://localhost:5173` (Vite frontend)
   - ✅ Added support for `http://localhost:3000` (React/Node frontend)
   - ✅ Enabled credentials for cross-origin requests

### 2. **API Endpoint Paths**
   All endpoints now use the `/api` prefix:
   - ✅ `POST /api/auth/register`
   - ✅ `POST /api/auth/login`
   - ✅ `POST /api/auth/verify-otp`
   - ✅ `GET /api/events`
   - ✅ `GET /api/registrations`
   - ✅ `GET /api/admin`

### 3. **New Resources Created**

#### **Venues** (`/api/venues`)
   - `GET /api/venues` - List all venues
   - `GET /api/venues/{id}` - Get venue details
   - `GET /api/venues/search?name=...` - Search venues
   - `POST /api/venues` - Create venue (Admin)
   - `PUT /api/venues/{id}` - Update venue (Admin)
   - `DELETE /api/venues/{id}` - Delete venue (Admin)

#### **Bookings** (`/api/bookings`)
   - `GET /api/bookings` - All bookings (Admin)
   - `GET /api/bookings/my-bookings` - My bookings
   - `GET /api/bookings/{id}` - Get booking details
   - `POST /api/bookings` - Create booking
   - `PUT /api/bookings/{id}` - Update booking
   - `PUT /api/bookings/{id}/confirm` - Confirm booking (Admin)
   - `PUT /api/bookings/{id}/cancel` - Cancel booking
   - `DELETE /api/bookings/{id}` - Delete booking (Admin)

#### **Budgets** (`/api/budgets`)
   - `GET /api/budgets` - All budgets (Admin)
   - `GET /api/budgets/my-budgets` - My budgets
   - `GET /api/budgets/event/{eventId}` - Event budgets
   - `GET /api/budgets/{id}` - Get budget details
   - `POST /api/budgets` - Create budget
   - `PUT /api/budgets/{id}` - Update budget
   - `PUT /api/budgets/{id}/approve` - Approve budget (Admin)
   - `DELETE /api/budgets/{id}` - Delete budget

#### **Users** (`/api/users`)
   - `GET /api/users` - All users (Admin)
   - `GET /api/users/me` - My profile
   - `PUT /api/users/me` - Update my profile
   - `GET /api/users/search?q=...` - Search users (Admin)
   - `GET /api/users/role/{role}` - Users by role (Admin)
   - `GET /api/users/{id}` - Get user by ID (Admin)

---

## 📋 Prerequisites

- Java 21 (LTS)
- Maven 3.9+
- MySQL 8.0+ (or configured database)
- Git (optional)

## 🚀 Quick Start

### Step 1: Configure Database
Edit `src/main/resources/application.properties`:

```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/event_management
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Server Configuration
server.port=8080
spring.application.name=event-management-system
```

### Step 2: Create Database
```sql
CREATE DATABASE IF NOT EXISTS event_management;
USE event_management;
```

### Step 3: Build the Project
```bash
cd c:\mini-project\Backend
mvn clean install -DskipTests
```

### Step 4: Run the Application

**Option A: Using Maven Wrapper (Recommended)**
```bash
cd c:\mini-project\Backend
mvn spring-boot:run
```

**Option B: Using Java directly**
```bash
cd c:\mini-project\Backend
mvn clean package -DskipTests
java -jar target/event-management-system-1.0.0.jar
```

The application will start at: **http://localhost:8080**

---

## 🧪 Testing Endpoints

### 1. Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "Password@123",
    "phoneNumber": "+1234567890"
  }'
```

### 2. Login User
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Password@123"
  }'
```

### 3. Get All Events
```bash
curl http://localhost:8080/api/events?page=0&size=10
```

### 4. Get All Venues
```bash
curl http://localhost:8080/api/venues?page=0&size=10
```

### 5. Get My Profile (with JWT token)
```bash
curl -H "Authorization: Bearer <JWT_TOKEN>" \
  http://localhost:8080/api/users/me
```

---

## 🔒 Security

- **JWT Authentication**: All protected endpoints require a valid JWT token
- **Role-Based Access Control**: STUDENT, ORGANIZER, ADMIN roles
- **CORS Enabled**: For frontend at localhost:5173 and localhost:3000
- **HTTPS Ready**: Configure in application.properties for production

---

## 📁 Project Structure

```
src/main/java/com/college/
├── controller/         # REST Controllers
├── service/           # Business Logic
├── repository/        # Database Access
├── entity/           # JPA Entities
├── dto/              # Data Transfer Objects
├── config/           # Spring Configuration
├── exception/        # Custom Exceptions
└── security/         # JWT & Security

src/main/resources/
└── application.properties  # Configuration
```

---

## 🛠️ Build Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Create JAR package
mvn clean package

# Run the application
mvn spring-boot:run

# View dependencies
mvn dependency:tree

# Check for Compiler errors
mvn clean verify
```

---

## 🚨 Troubleshooting

### Port 8080 Already In Use
```bash
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process
taskkill /PID <PID> /F

# Or change port in application.properties
server.port=8081
```

### Database Connection Error
- Ensure MySQL is running
- Check database credentials in application.properties
- Verify database exists: `CREATE DATABASE event_management;`

### CORS Issues
- Verify CORS config allows your frontend URL
- Check that Content-Type header is set to: `application/json`

### Build Fails
```bash
# Clear Maven cache
mvn clean
rmdir /s %USERPROFILE%\.m2\repository

# Rebuild
mvn clean install -DskipTests
```

---

## 📝 Environment Setup

### Development Profile
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Production Build
```bash
mvn clean package -DskipTests -Pprod
java -jar target/event-management-system-1.0.0.jar
```

---

## ✨ Key Features

✅ User authentication with JWT  
✅ Role-based access control  
✅ Event management with approval workflow  
✅ Venue management  
✅ Booking system  
✅ Budget allocation  
✅ User profile management  
✅ CORS support for multiple frontends  
✅ Pagination and sorting  
✅ Search capabilities  
✅ Global exception handling  
✅ Comprehensive logging  

---

## 💡 Next Steps

1. **Connect Frontend**: Update frontend API calls to use `/api/*` paths
2. **Configure Database**: Set up MySQL with proper credentials
3. **Run Tests**: Execute test suite: `mvn test`
4. **Deploy**: Build JAR and deploy to server
5. **Monitor**: Check logs in `target/` directory

---

## 📞 Support

For issues or questions:
1. Check application logs
2. Verify database connection
3. Ensure Java 21 is installed
4. Check port availability
5. Review CORS configuration

---

**Last Updated**: March 2026  
**Status**: ✅ Ready for Development
