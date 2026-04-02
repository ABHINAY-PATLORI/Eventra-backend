# MySQL Connection Error - QUICK FIX GUIDE

## The Problem
```
java.net.ConnectException: Connection refused
Unable to open JDBC Connection for DDL execution
```

**Root Cause**: Spring Boot can't connect to MySQL database during deployment.

---

## Step-by-Step Fix for Render.com

### Step 1: Create MySQL Database FIRST
1. Go to https://render.com/dashboard
2. Click `New +` → `Database` → `MySQL`
3. Configure:
   - **Name**: `college-events-db`
   - **Database**: `college_events`
   - **Username**: `admin`
   - **Plan**: Free tier (for testing)
4. **Wait** for database to be ready (green checkmark)
5. Click database → Copy these values:
   ```
   HOST: xxxxxxxx.mysql.render.com
   PORT: 3306
   DATABASE: college_events
   USERNAME: admin
   PASSWORD: (copy this)
   ```

### Step 2: Create Web Service
1. Click `New +` → `Web Service`
2. Connect GitHub repo: `ABHINAY-PATLORI/Eventra-backend`
3. Configure:
   - **Name**: `eventra-backend`
   - **Environment**: `Docker`
   - **Branch**: `main`
   - **Plan**: Free tier

### Step 3: Set Environment Variables (CRITICAL!)
1. In Web Service settings → `Environment` tab
2. Add these variables **EXACTLY** (case-sensitive):

   ```
   DB_HOST=<paste your mysql host here>.mysql.render.com
   DB_PORT=3306
   DB_NAME=college_events
   DB_USERNAME=admin
   DB_PASSWORD=<paste your mysql password here>
   DB_USE_SSL=true
   DB_POOL_SIZE=20
   DB_MIN_IDLE=5
   DDL_AUTO=validate
   
   SPRING_PROFILES_ACTIVE=prod
   SERVER_PORT=8080
   JPA_SHOW_SQL=false
   
   JWT_SECRET=generate-a-random-32-character-string-here-example1234
   
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-specific-password
   MAIL_FROM=your-email@gmail.com
   ```

   **IMPORTANT NOTES:**
   - `DB_HOST` must use `.mysql.render.com` domain
   - `SPRING_PROFILES_ACTIVE` MUST be `prod`
   - `JWT_SECRET` must be a unique random string (use another generator if needed)
   - For mail, use Gmail App Password (not account password)

### Step 4: Deploy
1. In Web Service → Click `Manual Deploy`
2. Select branch: `main`
3. Click `Deploy`
4. **WAIT 5-10 MINUTES** for deployment to complete

### Step 5: Check Logs
1. Web Service → `Logs` tab
2. Look for these SUCCESS indicators:
   ```
   Starting Spring Boot Application
   ✓ Email transporter is ready
   Tomcat started on port 8080
   Connected to database successfully
   ```

3. If you see ERROR:
   ```
   Connection refused
   Unable to open JDBC Connection
   ```
   → Go to Step 3, double-check all env vars, redeploy

---

## If Still Not Working

### Check #1: Is MySQL Ready?
```bash
mysql -h your-host.mysql.render.com -u admin -p
# Enter password when prompted
# You should connect successfully
```

### Check #2: Verify Environment Variables
- Go to Render Dashboard
- Web Service → Settings → Environment
- Verify each variable is exactly as shown above
- Common mistakes:
  - Wrong `DB_HOST` (must have `.mysql.render.com`)
  - Wrong `DB_PASSWORD`
  - Missing `SPRING_PROFILES_ACTIVE=prod`
  - Empty `JWT_SECRET`

### Check #3: Restart Deployment
1. Web Service → Manual Deploy
2. Select `main` branch
3. Click Deploy
4. Wait 10 minutes
5. Check logs again

### Check #4: Try Fresh Deployment
If still failing after 15 minutes:
1. Delete web service
2. Wait 2 minutes
3. Create new web service (same settings)
4. Deploy again

---

## Local Testing (Before Render)

### Test with Docker Compose
```bash
cd c:\mini-project\Backend
docker-compose up --build
```

Expected output after ~30 seconds:
```
eventra-backend | 2026-04-02 12:00:00.000  INFO ... TomcatWebServer : Tomcat started on port(s): 8080
```

If errors → check your local MySQL setup first

### Test API After Deployment
```bash
# Replace <your-render-url> with actual URL
curl https://your-render-url-here/api/health
```

Expected response:
```json
{"status":"UP"}
```

---

## Environment Variable Reference

| Variable | Value | Required | Example |
|----------|-------|----------|---------|
| `DB_HOST` | MySQL host from Render | ✅ YES | `xxx.mysql.render.com` |
| `DB_PORT` | Always 3306 | ✅ YES | `3306` |
| `DB_NAME` | Database name | ✅ YES | `college_events` |
| `DB_USERNAME` | MySQL user | ✅ YES | `admin` |
| `DB_PASSWORD` | MySQL password | ✅ YES | Strong password |
| `SPRING_PROFILES_ACTIVE` | `prod` for production | ✅ YES | `prod` |
| `JWT_SECRET` | Random 32+ chars | ✅ YES | Random string |
| `MAIL_USERNAME` | Gmail address | ✅ YES | `your@gmail.com` |
| `MAIL_PASSWORD` | App password | ✅ YES | 16-char app password |
| `MAIL_FROM` | Reply-from address | ⚠️ MAYBE | `your@gmail.com` |

---

## Common Mistakes & Solutions

### ❌ Using `localhost` for DB_HOST
**Wrong**: `DB_HOST=localhost`
**Right**: `DB_HOST=xxx.mysql.render.com`

### ❌ Using Regular Gmail Password
**Wrong**: `MAIL_PASSWORD=myGmailPassword123`
**Right**: `MAIL_PASSWORD=abcd efgh ijkl mnop` (16-char app password)

### ❌ Missing SPRING_PROFILES_ACTIVE
**Wrong**: Not setting it
**Right**: `SPRING_PROFILES_ACTIVE=prod`

### ❌ Setting DDL_AUTO to wrong value
**Wrong**: `DDL_AUTO=create-drop` (destroys database!)
**Right**: `DDL_AUTO=validate` (safe for production)

### ❌ Deploying before MySQL is ready
**Problem**: "Connection refused"
**Fix**: Wait for green checkmark on MySQL resource, then deploy web service

---

## After Successful Deployment

### Test the API
```bash
# Replace with your actual URL
curl -X POST https://eventra-backend.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@example.com","password":"Test123!"}'
```

### Monitor Logs
- Check logs daily for errors
- Set up uptime monitoring

### Next Steps
- Test all endpoints using Postman
- Configure custom domain (optional)
- Set up monitoring alerts (optional)

---

## Still More Issues?

### Where to Find Help
1. **Render Logs**: Web Service → Logs tab
2. **Application Logs**: Same logs tab (scroll up)
3. **Database Status**: Dashboard → MySQL resource status
4. **Render Docs**: https://render.com/docs/troubleshooting-deploys

### Key Error Files to Check Locally First
1. Run `docker-compose up --build` locally
2. Fix any local errors
3. Then deploy to Render

---

**TL;DR**: Set env vars → Deploy → Check logs → Success! 🎉
