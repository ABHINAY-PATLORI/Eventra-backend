# Render.com Deployment Guide

## Critical: Environment Variables Setup

The most common deployment error is MySQL connection failure. This MUST be configured correctly:

### Required Environment Variables

**MUST SET in Render Dashboard:**

```
# Database (from Render MySQL resource)
DB_HOST=<your-mysql-host>.mysql.render.com
DB_PORT=3306
DB_NAME=college_events
DB_USERNAME=<admin or custom user>
DB_PASSWORD=<your-database-password>

# Application
SPRING_PROFILES_ACTIVE=prod          # MUST be 'prod' for production
DB_USE_SSL=true                       # MUST be true for production
DDL_AUTO=validate                     # NEVER use 'create-drop' in prod

# JWT (Generate random 32+ character string)
JWT_SECRET=<generate-random-string-here>

# Mail Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-specific-password
MAIL_FROM=your-email@gmail.com
```

---

### Step 1: Create Database (MySQL)

1. Go to https://dashboard.render.com
2. Click `New` → `Database` → `MySQL`
3. Configure:
   - **Name**: `college-events-db`
   - **Database**: `college_events`
   - **Username**: `admin`
   - **Region**: Same as your web service region
   - **Plan**: Free (for testing) or Paid

4. Copy the connection details:
   - **Host**: `xxx.mysql.render.com`
   - **Port**: `3306`
   - **Database**: `college_events`
   - **Username**: `admin`
   - **Password**: (auto-generated)

### Step 2: Create Web Service

1. Go to https://dashboard.render.com
2. Click `New` → `Web Service`
3. Connect your GitHub repository
4. Configure:
   - **Name**: `eventra-backend`
   - **Environment**: `Docker`
   - **Branch**: `main`
   - **Region**: Same as database (default: Ohio)

### Step 3: Set Environment Variables

In Render Dashboard → `eventra-backend` → `Environment`:

```
# Database (from Step 1)
DB_HOST=xxx.mysql.render.com
DB_PORT=3306
DB_NAME=college_events
DB_USERNAME=admin
DB_PASSWORD=your-database-password

# Application
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=generate-random-32-char-string-here
DDL_AUTO=validate

# Mail (Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-specific-password
MAIL_FROM=your-email@gmail.com
```

### Step 4: Deploy

1. Click `Create Web Service`
2. Render will:
   - Build Docker image from Dockerfile
   - Push to Render registry
   - Deploy to container
   - Assign public URL
3. Check logs: `Logs` tab

---

## Important Configuration

### Before Deploying

```bash
# 1. In your GitHub repo, ensure files exist:
# ✓ Dockerfile (using environment variables)
# ✓ docker-compose.yml (optional, for local dev)
# ✓ application-prod.properties (with env vars)
# ✓ application.properties (with defaults)
# ✓ .dockerignore

git push origin main
```

### Critical: Database Connection String

Render requires full database connection string. The `Dockerfile` and `application-prod.properties` handle this via environment variables.

### Verify Deployment

1. Check the deployment URL (e.g., `https://eventra-backend.onrender.com`)
2. Test health endpoint:
```bash
curl https://eventra-backend.onrender.com/health
```

---

## Troubleshooting Render Deployment

### Error: "Unable to open JDBC Connection for DDL execution"

**Cause**: Database not accessible or credentials wrong

**Fix**:
1. Go to Render Dashboard → Database
2. Copy **exact** connection credentials
3. Update environment variables (re-deploy auto-triggers)
4. Check database is in same region

### Error: "Connection refused" or "Unable to open JDBC Connection"

**This is the most common deployment error.**

**Exact fixes (in order):**

1. **Verify database exists first**
   - Go to Render Dashboard → Resources
   - Confirm MySQL database is showing as "Available"
   - If not, wait 2-3 minutes for deployment to complete

2. **Double-check all environment variables**
   ```
   DB_HOST=<your-mysql>.mysql.render.com  (NOT localhost!)
   DB_PORT=3306
   DB_USERNAME=admin
   DB_PASSWORD=<exact password from database>
   SPRING_PROFILES_ACTIVE=prod
   ```

3. **Test database connection directly**
   - From your local machine:
   ```bash
   mysql -h <your-mysql>.mysql.render.com -u admin -p
   # (Enter password when prompted)
   ```
   - If this works, database is accessible
   - If not, database may not be fully deployed yet

4. **Restart web service**
   - Go to Render Dashboard
   - Service → Manual Deploy → Deploy latest commit
   - Wait 5-10 minutes for deployment

5. **Check logs in real-time**
   - Service → Logs tab
   - Look for these good signs:
   ```
   Starting Spring Boot Application
   ✓ Email transporter is ready
   Tomcat started on port 8080
   ```
   - Look for these error signs:
   ```
   Connection refused
   Communications link failure
   Unable to open JDBC Connection
   ```

6. **If still failing after 10 minutes**
   - Delete service and recreate
   - Ensure database is deployed first
   - Wait 5 minutes
   - Create web service
   - Set env vars again
   - Deploy

---

### Error: "Cannot resolve reference to bean 'jpaSharedEM_entityManagerFactory'"

This means the Hibernate EntityManager couldn't initialize because database connection failed. Same fix as above - verify DB_HOST and credentials.

---

### Build Takes Too Long

Render limits free tier builds. Use paid plan for faster builds.

### Database Connection Timeout

Render free database has limited connections. Remove old connections:

```bash
# In Render Dashboard → Database → Connection String
# Test connection from application logs
```

---

## Deployment Environment Variables

### Production-Safe Defaults

The application has built-in defaults, but for production use actual values:

| Variable | Value | Notes |
|----------|-------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` | **Must be set to prod** |
| `DB_HOST` | `xxx.mysql.render.com` | From Render MySQL |
| `DB_USERNAME` | `admin` | From Render MySQL |
| `DB_PASSWORD` | (Strong password) | From Render MySQL |
| `JWT_SECRET` | (32+ random chars) | Generate with: `openssl rand -base64 32` |
| `MAIL_USERNAME` | Gmail address | Enable App Passwords |
| `MAIL_PASSWORD` | App password (16 chars) | **Not Gmail password** |

---

## Accessing Your Deployed App

### Public URL

Render provides:
- **URL**: https://eventra-backend.onrender.com
- **Status**: Dashboard shows "Live"
- **Logs**: Real-time logs in Dashboard

### Database Access (Optional)

To connect to database directly:
```bash
mysql -h <DB_HOST> -u <DB_USERNAME> -p college_events
```

---

## Monitoring & Logs

### View Logs
1. Go to Render Dashboard
2. Select `eventra-backend`
3. Go to `Logs` tab
4. Filter by error level if needed

### Common Log Patterns

**Successful deployment**:
```
2026-04-02T12:30:45.123Z Starting College Event Management System
✓ Email transporter is ready
🚀 Server running on port 8080
```

**Database connection error**:
```
❌ Error: Unable to open JDBC Connection
Check DB_HOST, DB_USERNAME, DB_PASSWORD
```

### Enable Debug Logging

In Render environment variables:
```
LOGGING_LEVEL_ROOT=DEBUG
LOGGING_LEVEL_COM_COLLEGE=DEBUG
```

---

## Scaling Your Application

### Render Free Plan Limitations
- 1 instance, auto-sleep after 15 min inactivity
- Database: Limited connections
- Storage: Limited

### Upgrade to Pro

```bash
# 1. Go to Render Dashboard
# 2. Service → Settings → Upgrade Plan
# 3. Benefits:
#    - Always-on instance
#    - Higher resource limits
#    - Priority support
```

---

## SSL/HTTPS

Render automatically provides:
- ✅ Free SSL certificate
- ✅ Auto-renewal
- ✅ HTTPS for all traffic

Your app is automatically accessible at:
- `https://eventra-backend.onrender.com`

---

## Database Backups

### Automatic Backups (Paid tier)
- Enabled by default
- Retained for 30 days
- Available in Dashboard

### Manual Backup
```bash
# Download SQL dump
mysql -h <host> -u <user> -p <db> > backup.sql

# Or use Render Dashboard → Database → Backups
```

---

## CI/CD Integration

### Auto-Deploy on Push

Render automatically deploys when you push to GitHub:

```bash
# Make changes
git add .
git commit -m "Update API"
git push origin main

# Render automatically:
# 1. Pulls latest code
# 2. Builds Docker image
# 3. Deploys to production
# 4. Runs health checks
```

### Disable Auto-Deploy
In Render Dashboard → Service → Settings → Autodeploy → Off

---

## Cost Estimation

### Example Monthly Costs

| Resource | Plan | Cost |
|----------|------|------|
| Web Service | Free | $0 (sleeps) |
| Web Service | Starter | $7/month |
| MySQL Database | Free | $0 (limited) |
| MySQL Database | Starter | $15/month |
| **Total** | **Starter** | **~$22/month** |

Render pricing: https://render.com/pricing

---

## Post-Deployment

### Verify Everything Works

```bash
# 1. Health check
curl https://eventra-backend.onrender.com/health

# 2. Test API
curl -X POST https://eventra-backend.onrender.com/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"test@gmail.com"}'

# 3. Check logs
# Via Dashboard → Logs tab
```

### Setup Monitoring

- **Uptime**: Use external monitoring (UptimeRobot)
- **Errors**: Enable Sentry integration
- **Performance**: Use New Relic

---

## Rollback

### Revert to Previous Deployment

In Render Dashboard:
1. Go to `Deploys` tab
2. Find previous working deployment
3. Click `Redeploy`

Or via Git:
```bash
# Revert commit in GitHub
git revert <commit-hash>
git push origin main

# Render auto-deploys the reverted code
```

---

## Next Steps

1. ✅ Setup database in Render
2. ✅ Create web service
3. ✅ Set environment variables
4. ✅ Deploy
5. ✅ Test endpoints
6. ✅ Monitor logs
7. ✅ Setup monitoring tools (optional)
8. ✅ Scale when needed

## Support

- **Render Docs**: https://render.com/docs
- **Database Issues**: Check Render Dashboard → Database → Health
- **Build Errors**: Check Logs tab for detailed error messages
- **GitHub Integration**: Ensure GitHub repo is public or has proper permissions

---

**Your application is now deployed on Render! 🚀**
