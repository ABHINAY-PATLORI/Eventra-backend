# Quick Start Guide

Get the College Event Management System backend running in 5 minutes!

## Prerequisites

- Java 17+
- Maven 3.6+
- MySQL 8.0+

## Step 1: Database Setup (2 minutes)

```bash
# Open MySQL command line
mysql -u root -p

# Run this SQL
CREATE DATABASE college_events;
CREATE USER 'college_user'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON college_events.* TO 'college_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

## Step 2: Configure Application Properties (1 minute)

Edit `src/main/resources/application.properties`:

```properties
# Database connection
spring.datasource.username=college_user
spring.datasource.password=password123
```

## Step 3: Build and Run (2 minutes)

```bash
# Navigate to project directory
cd c:\mini-project\Backend

# Build the project
mvn clean package

# Run the application
java -jar target/event-management-system-1.0.0.jar
```

Or directly with Maven:

```bash
mvn spring-boot:run
```

**Application ready at:** `http://localhost:8080/api`

## Test the API

### 1. Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "role": "STUDENT"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

Copy the `token` from response.

### 3. Get All Events

```bash
curl -X GET "http://localhost:8080/api/events?page=0&size=10" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

## What's Next?

- Read [README.md](README.md) for complete API documentation
- Check [DEPLOYMENT.md](DEPLOYMENT.md) for production deployment
- Run [API_TESTING_GUIDE.sh](API_TESTING_GUIDE.sh) for comprehensive API testing

## Folder Structure

```
Backend/
├── src/
│   ├── main/
│   │   ├── java/com/college/
│   │   │   ├── controller/        # REST endpoints
│   │   │   ├── service/           # Business logic
│   │   │   ├── entity/            # Database entities
│   │   │   ├── repository/        # Data access
│   │   │   ├── security/          # JWT & auth
│   │   │   ├── exception/         # Error handling
│   │   │   ├── config/            # Spring configs
│   │   │   ├── dto/               # Data transfer objects
│   │   │   └── EventManagementApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/
├── pom.xml                         # Maven dependencies
├── README.md                       # Complete documentation
├── DEPLOYMENT.md                   # Deployment guide
├── API_TESTING_GUIDE.sh           # API testing examples
└── .gitignore
```

## Key Features

✅ JWT Authentication & Authorization  
✅ Role-based access control (Student, Organizer, Admin)  
✅ Event management (CRUD operations)  
✅ Event registration system  
✅ Admin event approval workflow  
✅ Pagination & sorting  
✅ Input validation  
✅ Global exception handling  
✅ CORS configuration  
✅ Production-ready code  

## Common Commands

```bash
# Run tests
mvn test

# Build without tests
mvn clean package -DskipTests

# Run with debug mode
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"

# Check dependencies
mvn dependency:tree

# Format code
mvn spotless:apply
```

## Default Ports

- **API Server**: 8080
- **MySQL**: 3306

## Troubleshooting

**Q: Application won't start?**
A: Check if port 8080 is already in use. Use `lsof -i :8080` to find and kill the process.

**Q: Database connection error?**
A: Verify MySQL is running and credentials in `application.properties` are correct.

**Q: Maven build fails?**
A: Run `mvn clean` first, then `mvn install`.

## Admin Setup

By default, regular users register as STUDENT. To create an ADMIN:

1. Register as STUDENT
2. Update database: `UPDATE users SET role='ADMIN' WHERE email='yourmail@example.com';`

## Next Steps

1. ✅ Application is running
2. 📖 Read complete [README.md](README.md)
3. 🧪 Run API tests from [API_TESTING_GUIDE.sh](API_TESTING_GUIDE.sh)
4. 🚀 Deploy to production following [DEPLOYMENT.md](DEPLOYMENT.md)
5. 🔗 Connect with React frontend
6. 📊 Monitor application health
7. 📈 Scale for production load

## API Base URL

```
http://localhost:8080/api
```

## Support

For detailed API documentation and examples, see [README.md](README.md)  
For production deployment, see [DEPLOYMENT.md](DEPLOYMENT.md)

Happy coding! 🚀
