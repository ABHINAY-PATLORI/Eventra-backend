# Deployment Guide

This document provides comprehensive deployment instructions for the College Event Management System backend.

## Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Build Process](#build-process)
4. [Production Configuration](#production-configuration)
5. [Database Setup](#database-setup)
6. [Running the Application](#running-the-application)
7. [Docker Deployment](#docker-deployment)
8. [Cloud Deployment](#cloud-deployment)
9. [Monitoring and Maintenance](#monitoring-and-maintenance)
10. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **OS**: Linux, Windows, or macOS
- **Java**: OpenJDK 17 or later
- **Maven**: 3.6.0 or later
- **MySQL**: 8.0 or later
- **Memory**: Minimum 2GB RAM (4GB+ recommended)
- **Disk Space**: Minimum 1GB for application

### Required Software

```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Verify MySQL is running
mysql --version
```

## Local Development Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd c:\mini-project\Backend
```

### 2. Install Dependencies

```bash
mvn clean install
```

### 3. Configure Local Database

```bash
# Login to MySQL
mysql -u root -p

# Create database and user
CREATE DATABASE college_events;
CREATE USER 'college_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON college_events.* TO 'college_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 4. Update Application Properties

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/college_events
spring.datasource.username=college_user
spring.datasource.password=secure_password
```

### 5. Run Application

```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Using IDE (IntelliJ, Eclipse, VS Code)
# Run EventManagementApplication.java

# Option 3: Using JAR
mvn clean package
java -jar target/event-management-system-1.0.0.jar
```

The application will start on `http://localhost:8080/api`

## Build Process

### Clean Build

```bash
mvn clean
```

### Compile and Test

```bash
mvn clean compile test
```

### Build JAR File

```bash
mvn clean package
```

### Build with Skip Tests

```bash
mvn clean package -DskipTests
```

### Build with Specific Profile

```bash
mvn clean package -P dev
mvn clean package -P prod
```

## Production Configuration

### 1. Update Security Configuration

Edit `application.properties`:

```properties
# Change JWT secret (generate new one)
jwt.secret=your-very-long-secure-secret-key-min-64-chars

# Update database credentials
spring.datasource.url=jdbc:mysql://prod-db-server:3306/college_events
spring.datasource.username=prod_user
spring.datasource.password=strong_password

# Connection pool for production
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# Disable SQL logging
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Logging level
logging.level.root=WARN
logging.level.com.college=INFO

# Server SSL Configuration
server.ssl.key-store=classpath:keystore.jks
server.ssl.key-store-password=keystore_password
server.ssl.key-store-type=JKS
```

### 2. Generate SSL Certificate

```bash
# Generate keystore (valid for 365 days)
keytool -genkeypair -alias tomcat \
  -keyalg RSA -keysize 2048 \
  -keystore keystore.jks \
  -validity 365 \
  -storepass keystore_password
```

### 3. Configure CORS for Production

Update `CorsConfig.java`:

```java
registry.addMapping("/**")
        .allowedOrigins("http://yourdomain.com", "https://yourdomain.com")
```

### 4. Disable Actuators if Not Needed

Update `application.properties`:

```properties
management.endpoints.web.exposure.include=
```

## Database Setup

### 1. Create Production Database

```bash
mysql -u root -p << EOF
CREATE DATABASE college_events CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'college_app'@'%' IDENTIFIED BY 'very_strong_password_here';
GRANT ALL PRIVILEGES ON college_events.* TO 'college_app'@'%';
FLUSH PRIVILEGES;
EOF
```

### 2. Configure MySQL for Production

Edit `/etc/mysql/my.cnf` or `my.ini`:

```ini
[mysqld]
# Connection pool
max_connections=1000
max_allowed_packet=256M

# Performance
query_cache_size=512m
query_cache_type=1
innodb_buffer_pool_size=2G
innodb_log_file_size=512M

# Replication (optional)
server_id=1
log_bin=mysql-bin

# Backup (optional)
expire_logs_days=7
```

### 3. Database Backup Strategy

```bash
#!/bin/bash
# backup-db.sh
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="/backup/college_events"
DB_NAME="college_events"
DB_USER="college_app"
DB_PASSWORD="your_password"

mkdir -p $BACKUP_DIR

mysqldump -u $DB_USER -p$DB_PASSWORD $DB_NAME | \
  gzip > $BACKUP_DIR/college_events_$TIMESTAMP.sql.gz

# Keep only last 30 days of backups
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete
```

## Running the Application

### Option 1: Direct JAR Execution

```bash
java -server \
  -Xms1024m \
  -Xmx2048m \
  -XX:+UseG1GC \
  -jar target/event-management-system-1.0.0.jar \
  --spring.profiles.active=prod
```

### Option 2: Systemd Service

Create `/etc/systemd/system/college-events.service`:

```ini
[Unit]
Description=College Event Management System
After=network.target
StartLimitIntervalSec=0

[Service]
Type=simple
Restart=always
RestartSec=1
User=events-app
ExecStart=/usr/bin/java -server \
  -Xms1024m -Xmx2048m \
  -XX:+UseG1GC \
  -jar /opt/college-events/event-management-system-1.0.0.jar \
  --spring.profiles.active=prod
StandardOutput=append:/var/log/college-events/app.log
StandardError=append:/var/log/college-events/error.log

[Install]
WantedBy=multi-user.target
```

Start service:

```bash
sudo systemctl daemon-reload
sudo systemctl start college-events
sudo systemctl enable college-events
```

### Option 3: Docker Deployment

See [Docker Deployment](#docker-deployment) section below.

## Docker Deployment

### 1. Create Dockerfile

```dockerfile
FROM maven:3.8-openjdk-17 as builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/event-management-system-*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-server", "-Xms512m", "-Xmx1024m", "-XX:+UseG1GC", "-jar", "app.jar"]
```

### 2. Build Docker Image

```bash
docker build -t college-events:1.0.0 .
```

### 3. Run Docker Container

```bash
docker run -d \
  --name college-events \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-host:3306/college_events \
  -e SPRING_DATASOURCE_USERNAME=college_app \
  -e SPRING_DATASOURCE_PASSWORD=secure_password \
  -e JWT_SECRET=your-secret-key \
  --restart unless-stopped \
  college-events:1.0.0
```

### 4. Docker Compose Setup

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: college-events-db
    environment:
      MYSQL_DATABASE: college_events
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_USER: college_app
      MYSQL_PASSWORD: app_password
    volumes:
      - mysql-data:/var/lib/mysql
    ports:
      - "3306:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  backend:
    build: .
    container_name: college-events-api
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/college_events
      SPRING_DATASOURCE_USERNAME: college_app
      SPRING_DATASOURCE_PASSWORD: app_password
      JWT_SECRET: your-secret-key-here
    ports:
      - "8080:8080"
    restart: unless-stopped

volumes:
  mysql-data:
```

Run with Docker Compose:

```bash
docker-compose up -d
docker-compose logs -f backend
```

## Cloud Deployment

### AWS Elastic Beanstalk

1. Create `.ebextensions/java.config`:

```yaml
option_settings:
  aws:elasticbeanstalk:container:java:
    JVM Options: -Xms1024m -Xmx2048m -XX:+UseG1GC
  aws:elasticbeanstalk:application:environment:
    SPRING_PROFILES_ACTIVE: prod
```

2. Deploy:

```bash
eb init
eb create college-events-env
eb deploy
```

### Azure App Service

```bash
# Build JAR
mvn clean package

# Login to Azure
az login

# Create resource group
az group create --name college-events-rg --location eastus

# Create App Service plan
az appservice plan create --name college-events-plan \
  --resource-group college-events-rg --sku B2 --is-linux

# Create App Service
az webapp create --resource-group college-events-rg \
  --plan college-events-plan --name college-events \
  --runtime JAVA|17-java17

# Deploy JAR
az webapp deployment source config-zip --resource-group college-events-rg \
  --name college-events --src target/event-management-system-1.0.0.jar
```

### Google Cloud Run

```bash
# Build container
gcloud builds submit --tag gcr.io/PROJECT_ID/college-events

# Deploy
gcloud run deploy college-events \
  --image gcr.io/PROJECT_ID/college-events \
  --platform managed \
  --region us-central1 \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod
```

## Monitoring and Maintenance

### 1. Application Monitoring

Monitor using Spring Actuator:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Application info
curl http://localhost:8080/actuator/info
```

### 2. Log Monitoring

```bash
# Follow logs
tail -f /var/log/college-events/app.log

# Search for errors
grep "ERROR" /var/log/college-events/app.log

# Log statistics
wc -l /var/log/college-events/app.log
```

### 3. Database Monitoring

```bash
# Check MySQL status
mysql -u college_app -p -e "SHOW STATUS;" college_events

# Check table sizes
mysql -u college_app -p college_events << EOF
SELECT 
  TABLE_NAME,
  ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'college_events'
ORDER BY (data_length + index_length) DESC;
EOF
```

### 4. Performance Tuning

```bash
# Check slow queries
mysql -u college_app -p college_events << EOF
SELECT query_time, sql_text FROM mysql.slow_log 
ORDER BY query_time DESC LIMIT 10;
EOF

# Optimize tables
OPTIMIZE TABLE users, events, registrations;
```

### 5. Backup and Recovery

```bash
# Automated daily backup at 2 AM
0 2 * * * /usr/local/bin/backup-db.sh

# Restore from backup
gunzip < college_events_20240326_020000.sql.gz | \
  mysql -u college_app -p college_events
```

## Troubleshooting

### Issue: Database Connection Failed

**Solution:**

```bash
# Check MySQL is running
service mysql status

# Test connection
mysql -u college_app -h localhost -p -e "SELECT 1;"

# Check credentials in application.properties
```

### Issue: Port Already in Use

**Solution:**

```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or use different port
java -jar app.jar --server.port=8081
```

### Issue: Out of Memory

**Solution:**

```bash
# Increase heap size
java -Xms2048m -Xmx4096m -jar app.jar

# Check current memory usage
jps -l
jcmd <pid> VM.native_memory summary
```

### Issue: Slow Queries

**Solution:**

```bash
# Enable slow query log
mysql -u root -p << EOF
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
EOF

# Check slow logs
tail -f /var/log/mysql/slow.log
```

### Issue: JWT Token Expiration

**Solution:**

Update `jwt.expiration` in `application.properties`:

```properties
# 24 hours = 86400000 milliseconds
jwt.expiration=86400000
```

## Deployment Checklist

- [ ] Database created and user set up
- [ ] SSL certificate generated
- [ ] JWT secret key changed
- [ ] CORS origins configured
- [ ] Environment variables set
- [ ] Logging configured
- [ ] Backup strategy implemented
- [ ] Monitoring enabled
- [ ] Load balancer configured
- [ ] Database backups tested
- [ ] Application tested end-to-end
- [ ] Documentation updated
- [ ] Team trained on deployment

## Support

For deployment issues, contact the DevOps team or check logs:

```bash
journalctl -u college-events -f
```

---

Last Updated: March 26, 2024
