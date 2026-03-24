---
trigger: always_on
---

# 📡 API DESIGN CONVENTIONS
> Attach this file manually when designing new endpoints or reviewing API contracts.

---

## URL Structure
```
✅ GET    /api/v1/users              list (paginated)
✅ GET    /api/v1/users/{id}         get one
✅ POST   /api/v1/users              create
✅ PUT    /api/v1/users/{id}         full update
✅ PATCH  /api/v1/users/{id}         partial update
✅ DELETE /api/v1/users/{id}         delete

✅ GET    /api/v1/users/{id}/orders  nested resource (shallow nesting only)

❌ /api/v1/getUser          no verbs in URL
❌ /api/v1/user             use plural nouns
❌ /v1/users                always include /api prefix
❌ /users                   always include version
```

---

## Response Shapes

### Paginated List
```json
{
  "success": true,
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 143,
    "totalPages": 8
  }
}
```

### Single Resource
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "Nguyen Van A",
    "createdAt": "2025-01-01T00:00:00Z"
  }
}
```

### Error
```json
{
  "success": false,
  "code": "RESOURCE_NOT_FOUND",
  "message": "User with id 123 not found",
  "timestamp": "2025-01-01T00:00:00Z",
  "path": "/api/v1/users/123"
}
```

### Validation Error
```json
{
  "success": false,
  "code": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "errors": [
    { "field": "email", "message": "must be a valid email" },
    { "field": "password", "message": "must be at least 8 characters" }
  ],
  "timestamp": "2025-01-01T00:00:00Z",
  "path": "/api/v1/users"
}
```

---

## Pagination Query Params
```
GET /api/v1/users?page=0&size=20&sort=createdAt,desc

page   → 0-indexed (Spring Boot / FastAPI convention)
size   → default 20, max 100 — enforce server-side
sort   → field,direction — multiple: sort=name,asc&sort=createdAt,desc
```
⚠️ Element Plus Pagination is **1-indexed** — always subtract 1 before sending to API.

---

## Filtering
```
GET /api/v1/users?status=ACTIVE&role=ADMIN
GET /api/v1/users?search=nguyen
GET /api/v1/orders?createdFrom=2025-01-01&createdTo=2025-12-31&status=PENDING
```
- Filters via query params for GET — never in request body
- Date range: ISO-8601 UTC always — never timestamps, never dd/MM/yyyy

---

## HTTP Status Codes
| Code | When |
|---|---|
| 200 OK | GET, PUT, PATCH success |
| 201 Created | POST success — include `Location` header |
| 204 No Content | DELETE success |
| 400 Bad Request | Malformed request, validation failed |
| 401 Unauthorized | Missing or expired token |
| 403 Forbidden | Authenticated but lacks permission |
| 404 Not Found | Resource doesn't exist |
| 409 Conflict | Duplicate (email, code, etc.) |
| 422 Unprocessable | Business rule violation |
| 500 Server Error | Unexpected — should never be intentional |

---

## Standard Error Codes
```
RESOURCE_NOT_FOUND       404
DUPLICATE_EMAIL          409
DUPLICATE_RESOURCE       409
VALIDATION_FAILED        400
UNAUTHORIZED             401
FORBIDDEN                403
BUSINESS_RULE_VIOLATED   422
INTERNAL_ERROR           500
```

---

## Security Headers (all responses)
```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Content-Security-Policy: default-src 'self'
```