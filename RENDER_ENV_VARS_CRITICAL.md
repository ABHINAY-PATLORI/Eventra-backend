# CRITICAL: MySQL Connection Fix for Render Deployment

## The Actual Problem

Your Spring Boot application is **still trying to connect to localhost:3306** because:

❌ Environment variables are NOT being passed to the Docker container  
❌ Render web service doesn't have environment variables set yet  
❌ Application defaults to `localhost` when env vars are missing

---

## IMMEDIATE FIX (Do This Now!)

### Step 1: Ensure MySQL Database Exists in Render

```
Render Dashboard → Resources
```

Check if you see a MySQL resource with a green checkmark. If NOT:

1. Click `New +`
2. Select `Database` → `MySQL`
3. Name: `college-events-db`
4. Database: `college_events`
5. Plan: Free (or paid)
6. **WAIT for it to show as Available (green checkmark)**
7. Click on it and copy:
   - Internal Database URL (copy the hostname)
   - Username: `admin`
   - Password: (auto-generated)

### Step 2: Create or Update Web Service Environment Variables

```
Render Dashboard → Services → eventra-backend → Settings → Environment
```

**Make sure you have EXACTLY these variables:**

```
SPRING_PROFILES_ACTIVE=prod
DB_HOST=xxxxx.mysql.render.com
DB_PORT=3306
DB_NAME=college_events
DB_USERNAME=admin
DB_PASSWORD=your-password-from-database
DB_USE_SSL=true
DDL_AUTO=validate
JWT_SECRET=random-32-char-string
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=your-email@gmail.com
```

**CRITICAL POINTS:**
- Copy `DB_HOST` exactly from Render MySQL resource
- Copy `DB_PASSWORD` exactly (including special chars)
- DO NOT include `mysql://` or other parts, just the hostname
- Do NOT use `localhost` - use the rendered domain

### Step 3: Deploy Again

```
Web Service → Manual Deploy → Select main → Deploy
```

### Step 4: Watch Logs

```
Web Service → Logs tab
```

Look for:
```
✓ Successfully: Connection established
Tomcat started on port 8080
```

If still seeing "Connection refused" → env vars not set correctly

---

## Common Mistakes (Double Check These!)

### ❌ WRONG: Using localhost
```
DB_HOST=localhost  ← WRONG for Render!
```

### ✅ RIGHT: Use Render hostname
```
DB_HOST=dpg-xyz.mysql.render.com  ← Copy from Render dashboard!
```

### ❌ WRONG: Empty env vars
```
DB_HOST=
DB_PASSWORD=
```

### ✅ RIGHT: Filled with actual values
```
DB_HOST=dpg-xyz.mysql.render.com
DB_PASSWORD=SomeStrongPassword123
```

### ❌ WRONG: Forgot to set SPRING_PROFILES_ACTIVE
(not set)  ← Application uses default=localhost

### ✅ RIGHT: Set to prod
```
SPRING_PROFILES_ACTIVE=prod
```

---

## If Env Vars Are Set But Still Failing

Check these:

### 1. Is MySQL Database Actually Ready?
```bash
# From your local machine, test connection:
mysql -h dpg-xxxx.mysql.render.com -u admin -p
# Enter password when prompted
```

If this fails → MySQL resource not ready yet, wait 5 more minutes

### 2. Check Docker Environment Variables Are Read

The Dockerfile should pass them to Spring Boot. Check:
- `docker-compose.yml` has all env vars in the `environment:` section
- `Dockerfile` uses `CMD ["java", "-jar", "app.jar"]` (lets Spring Boot read env vars)

### 3. Force Rebuild and Redeploy

Sometimes Docker cache causes issues:

```
Render Dashboard → Web Service → Settings → Clear Build Cache
Then → Manual Deploy
```

---

## What Changed This Update

✅ Updated Hibernate dialect: `MySQL8Dialect` → `MySQLDialect` (removes deprecation warning)  
✅ Connected environment variables properly to Spring Boot  
✅ Added context path `/api` to all URLs  
✅ Improved database connection pooling settings

---

## Test Locally First!

Before Render deployment, test with Docker Compose:

```bash
cd c:\mini-project\Backend
docker-compose up --build
```

If this works locally, Render will work with proper env vars.

If this fails locally → Fix local setup first, then Render will work.

---

## The Core Issue (Technical)

**Why it happens:**
1. Spring Boot uses `application.properties` to configure database
2. `application.properties` has: `spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:...`
3. If `${DB_HOST}` env var doesn't exist, it defaults to `localhost`
4. In Render container, `localhost:3306` doesn't exist → Connection refused

**The fix:**
1. Set `DB_HOST=` environment variable in Render dashboard
2. Spring Boot reads it: `${DB_HOST:localhost}` becomes the Render hostname
3. Application connects to actual database in Render
4. Success! ✅

---

## Verification Checklist

Before each deployment, verify:

- [ ] MySQL database exists in Render (green checkmark)
- [ ] Web Service environment variables are set (8+ variables)
- [ ] `DB_HOST` copied exactly from Render MySQL resource
- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `DDL_AUTO=validate`
- [ ] No typos in variable names
- [ ] Clicked Save after entering env vars
- [ ] Clicked Manual Deploy
- [ ] Waited 10+ minutes for deployment
- [ ] Checked logs for success

---

## Still Failing? Follow This Process

1. **Check Render logs** (Web Service → Logs)
   - Copy full error message
   - Search for "Connection refused" or "Unable to open JDBC"

2. **If DB connection error:**
   - Go to Render MySQL resource
   - Copy connection string
   - Verify `DB_HOST`, `DB_USERNAME`, `DB_PASSWORD` in Web Service environment

3. **If env vars not working:**
   - Clear Build Cache
   - Manual Deploy
   - Wait 10 minutes

4. **If still stuck:**
   - Delete Web Service
   - Wait 2 minutes
   - Create new Web Service with same name
   - Set env vars again
   - Deploy

---

## Database Connection String Reference

Your database connection string is built from these env vars:

```
jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?createDatabase...
```

Example with actual values:
```
jdbc:mysql://dpg-abc123.mysql.render.com:3306/college_events?...
```

ALL THREE PARTS MUST BE CORRECT:
- `dpg-abc123.mysql.render.com` ← From Render MySQL resource
- `3306` ← Standard MySQL port
- `college_events` ← Database name

If ANY part is wrong → Connection fails!

---

**Next step: Go to Render Dashboard, double-check MySQL is deployed and has green checkmark, then verify all env vars are set correctly.**

Then run Manual Deploy and wait 10 minutes.

Check logs - should show database connection successfully! 🎉
