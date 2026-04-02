#!/bin/bash
# API Testing Guide for College Event Management System
# Run these curl commands to test the backend API
# 
# Prerequisites:
# - Backend running on http://localhost:8080/api
# - Set BASE_URL environment variable: export BASE_URL="http://localhost:8080/api"

BASE_URL="${BASE_URL:-http://localhost:8080/api}"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}College Event Management System - API Testing${NC}\n"

# ============================================================================
# AUTHENTICATION TESTS
# ============================================================================

echo -e "${GREEN}1. REGISTER NEW STUDENT${NC}"
STUDENT_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "password": "Password123!",
    "role": "STUDENT"
  }')
echo "$STUDENT_RESPONSE" | jq '.'
# Extract and save token for later use
STUDENT_AUTH_TOKEN=$(echo "$STUDENT_RESPONSE" | jq -r '.data // empty')
echo ""

echo -e "${GREEN}2. REGISTER NEW ORGANIZER${NC}"
ORGANIZER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bob Smith",
    "email": "bob@example.com",
    "password": "Password123!",
    "role": "ORGANIZER"
  }')
echo "$ORGANIZER_RESPONSE" | jq '.'
echo ""

echo -e "${GREEN}3. LOGIN STUDENT${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "Password123!"
  }')
echo "$LOGIN_RESPONSE" | jq '.'
# Extract token for authenticated requests
STUDENT_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.token // empty')
echo -e "${YELLOW}Token: $STUDENT_TOKEN${NC}\n"

echo -e "${GREEN}4. LOGIN ORGANIZER${NC}"
ORG_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "bob@example.com",
    "password": "Password123!"
  }')
echo "$ORG_LOGIN_RESPONSE" | jq '.'
ORG_TOKEN=$(echo "$ORG_LOGIN_RESPONSE" | jq -r '.data.token // empty')
echo -e "${YELLOW}Organizer Token: $ORG_TOKEN${NC}\n"

# Note: For ADMIN user, you need to manually update the database or use the role change API
# For testing purposes, assume we have an ADMIN token
ADMIN_TOKEN="$ORG_TOKEN"  # In production, this would be different

# ============================================================================
# EVENT TESTS
# ============================================================================

echo -e "${GREEN}5. CREATE EVENT (ORGANIZER)${NC}"
EVENT_RESPONSE=$(curl -s -X POST "$BASE_URL/events" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ORG_TOKEN" \
  -d '{
    "title": "Advanced Java Programming Workshop",
    "description": "Learn advanced Java concepts including streams, lambdas, and concurrency",
    "eventDate": "2024-05-15T10:00:00",
    "location": "Engineering Building - Room 401",
    "capacity": 50,
    "imageUrl": "https://example.com/java-workshop.jpg"
  }')
echo "$EVENT_RESPONSE" | jq '.'
# Extract event ID for later use
EVENT_ID=$(echo "$EVENT_RESPONSE" | jq -r '.data.id // empty')
echo -e "${YELLOW}Event ID: $EVENT_ID${NC}\n"

echo -e "${GREEN}6. GET ALL APPROVED EVENTS${NC}"
curl -s -X GET "$BASE_URL/events?page=0&size=10&sort=asc" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

echo -e "${GREEN}7. SEARCH EVENTS BY TITLE${NC}"
curl -s -X GET "$BASE_URL/events/search?title=Java&page=0&size=10" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

echo -e "${GREEN}8. GET EVENT BY ID${NC}"
curl -s -X GET "$BASE_URL/events/$EVENT_ID" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

echo -e "${GREEN}9. UPDATE EVENT (ORGANIZER)${NC}"
curl -s -X PUT "$BASE_URL/events/$EVENT_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ORG_TOKEN" \
  -d '{
    "title": "Advanced Java Programming Workshop - Updated",
    "description": "Learn advanced Java concepts including streams, lambdas, and concurrency patterns",
    "eventDate": "2024-05-15T10:00:00",
    "location": "Engineering Building - Room 401",
    "capacity": 60,
    "imageUrl": "https://example.com/java-workshop-v2.jpg"
  }' | jq '.'
echo ""

echo -e "${GREEN}10. GET MY EVENTS (ORGANIZER)${NC}"
curl -s -X GET "$BASE_URL/events/my-events/list?page=0&size=10" \
  -H "Authorization: Bearer $ORG_TOKEN" | jq '.'
echo ""

# ============================================================================
# REGISTRATION TESTS
# ============================================================================

echo -e "${GREEN}11. REGISTER FOR EVENT (STUDENT)${NC}"
REG_RESPONSE=$(curl -s -X POST "$BASE_URL/registrations/$EVENT_ID" \
  -H "Authorization: Bearer $STUDENT_TOKEN")
echo "$REG_RESPONSE" | jq '.'
REGISTRATION_ID=$(echo "$REG_RESPONSE" | jq -r '.data.id // empty')
echo ""

echo -e "${GREEN}12. GET MY REGISTRATIONS (STUDENT)${NC}"
curl -s -X GET "$BASE_URL/registrations/my-events/list?page=0&size=10" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

echo -e "${GREEN}13. UNREGISTER FROM EVENT (STUDENT)${NC}"
curl -s -X DELETE "$BASE_URL/registrations/$EVENT_ID" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

echo -e "${GREEN}14. RE-REGISTER FOR EVENT${NC}"
curl -s -X POST "$BASE_URL/registrations/$EVENT_ID" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

# ============================================================================
# ADMIN TESTS (Note: Requires ADMIN role)
# ============================================================================

echo -e "${GREEN}15. CREATE ANOTHER EVENT FOR ADMIN TESTING${NC}"
EVENT2_RESPONSE=$(curl -s -X POST "$BASE_URL/events" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ORG_TOKEN" \
  -d '{
    "title": "Python Data Science Workshop",
    "description": "Master Python for data science and machine learning",
    "eventDate": "2024-06-01T14:00:00",
    "location": "Tech Center - Room 201",
    "capacity": 40,
    "imageUrl": "https://example.com/python-ds.jpg"
  }')
echo "$EVENT2_RESPONSE" | jq '.'
EVENT2_ID=$(echo "$EVENT2_RESPONSE" | jq -r '.data.id // empty')
echo ""

echo -e "${YELLOW}Note: Admin endpoints require ADMIN role. To test these:${NC}"
echo "1. Manually update user role in database: UPDATE users SET role='ADMIN' WHERE id=X;"
echo "2. Or use the role change API if admin already exists"
echo ""

echo -e "${GREEN}16. GET ALL USERS (ADMIN ONLY)${NC}"
echo "curl -s -X GET \"$BASE_URL/admin/users?page=0&size=10\" \\"
echo "  -H \"Authorization: Bearer \$ADMIN_TOKEN\" | jq '.'"
echo ""

echo -e "${GREEN}17. GET PENDING EVENTS (ADMIN ONLY)${NC}"
echo "curl -s -X GET \"$BASE_URL/admin/events/pending?page=0&size=10\" \\"
echo "  -H \"Authorization: Bearer \$ADMIN_TOKEN\" | jq '.'"
echo ""

echo -e "${GREEN}18. APPROVE EVENT (ADMIN ONLY)${NC}"
echo "curl -s -X PUT \"$BASE_URL/admin/events/$EVENT2_ID/approve\" \\"
echo "  -H \"Authorization: Bearer \$ADMIN_TOKEN\" | jq '.'"
echo ""

echo -e "${GREEN}19. REJECT EVENT (ADMIN ONLY)${NC}"
echo "curl -s -X PUT \"$BASE_URL/admin/events/\$EVENT_ID/reject\" \\"
echo "  -H \"Authorization: Bearer \$ADMIN_TOKEN\" | jq '.'"
echo ""

echo -e "${GREEN}20. GET EVENT REGISTRATIONS (ADMIN ONLY)${NC}"
echo "curl -s -X GET \"$BASE_URL/registrations/events/$EVENT_ID?page=0&size=10\" \\"
echo "  -H \"Authorization: Bearer \$ADMIN_TOKEN\" | jq '.'"
echo ""

# ============================================================================
# ERROR TESTING
# ============================================================================

echo -e "${GREEN}21. TEST - INVALID LOGIN${NC}"
curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "WrongPassword"
  }' | jq '.'
echo ""

echo -e "${GREEN}22. TEST - REGISTER DUPLICATE EMAIL${NC}"
curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson 2",
    "email": "alice@example.com",
    "password": "Password123!"
  }' | jq '.'
echo ""

echo -e "${GREEN}23. TEST - ACCESS WITHOUT TOKEN${NC}"
curl -s -X DELETE "$BASE_URL/registrations/$EVENT_ID" | jq '.'
echo ""

echo -e "${GREEN}24. TEST - EVENT NOT FOUND${NC}"
curl -s -X GET "$BASE_URL/events/99999" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

echo -e "${GREEN}25. TEST - INSUFFICIENT CAPACITY${NC}"
echo "Create event with capacity 1, then multiple students try to register"
echo ""

# ============================================================================
# CLEANUP NOTE
# ============================================================================

echo -e "${YELLOW}============================================================================${NC}"
echo -e "${YELLOW}Testing Complete!${NC}"
echo -e "${YELLOW}============================================================================${NC}"
echo ""
echo "Token information:"
echo "Student Token: $STUDENT_TOKEN"
echo "Organizer Token: $ORG_TOKEN"
echo ""
echo "Key Resource IDs:"
echo "Event 1 ID: $EVENT_ID"
echo "Event 2 ID: $EVENT2_ID"
echo ""
echo "For detailed API documentation, see README.md"
echo ""
