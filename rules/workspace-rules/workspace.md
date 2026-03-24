# 📦 WORKSPACE RULES — [PROJECT NAME]
> Attach this file at the start of every AI session in this project.
> Then attach additional rule files based on what you're working on (see guide below).

---

## 🗂️ Project Overview
```
Type:       Monorepo
Backend 1:  Java / Spring Boot 3.x      → /backend-java/
Backend 2:  Python / FastAPI            → /backend-python/
Frontend:   Vue 3 + Element Plus        → /frontend/
```

> ✏️ Update the paths above to match your actual folder names.

---

## 📎 Rule Files — Attach Based on Task

| You are working on...              | Attach these files                          |
|------------------------------------|---------------------------------------------|
| Java service, controller, entity   | `java-spring.md`                            |
| Python endpoint, service, model    | `python-fastapi.md`                         |
| Vue component, composable, store   | `vue-base.md` + `element-plus.md`           |
| Any test file                      | `testing.md`                                |
| Designing or reviewing API contract| `api-design.md`                             |
| Full-stack feature (common)        | `java-spring.md` + `vue-base.md` + `element-plus.md` |

> 💡 Tip: You don't need to attach all files every time.
> Attach only what's relevant to keep the AI context focused.

---

## 🏗️ Architecture Overview

```
Request → Vue 3 (Element Plus)
              ↓ HTTP /api/v1/
         Spring Boot / FastAPI
              ↓
         Service Layer (business logic)
              ↓
         Repository / SQLAlchemy
              ↓
         PostgreSQL / MySQL
```

### Layer Responsibilities
| Layer       | Java              | Python           | Rule                        |
|-------------|-------------------|------------------|-----------------------------|
| HTTP        | @RestController   | @router          | No business logic here      |
| Business    | @Service          | Service class    | All logic lives here        |
| Data        | @Repository / JPA | SQLAlchemy 2.0   | No business logic here      |
| DTO/Schema  | record / class    | Pydantic model   | Never expose Entity/ORM raw |

---

## 📁 Folder Conventions

### Frontend (`/frontend/src/`)
```
api/          → axios instance + per-resource api modules (userApi.ts, orderApi.ts)
components/   → PascalCase Vue components (UserProfileCard.vue)
composables/  → useXxx.ts (useUser.ts, usePagination.ts)
stores/       → Pinia stores, useXxxStore.ts (useAuthStore.ts)
types/        → TypeScript interfaces & types (user.ts, api.ts)
views/        → Page-level components mapped to routes
```

### Backend Java (`/backend-java/src/`)
```
controller/   → @RestController classes
service/      → @Service classes (business logic)
repository/   → @Repository interfaces
entity/       → @Entity JPA classes
dto/          → Request/Response records
exception/    → Custom exceptions + @RestControllerAdvice
config/       → Spring @Configuration classes
```

### Backend Python (`/backend-python/`)
```
routers/      → FastAPI router files
services/     → Business logic classes
models/       → SQLAlchemy ORM models
schemas/      → Pydantic request/response schemas
repositories/ → DB query logic (optional layer)
core/         → Config, dependencies, lifespan
```

---

## ✅ Definition of Done (per task)
Before considering any task complete, verify:
- [ ] Business logic is in Service layer, not Controller/Router
- [ ] No Entity/ORM model exposed directly in API response
- [ ] Error handling follows unified error response shape
- [ ] New endpoint has `/api/v1/` prefix
- [ ] List endpoint supports pagination
- [ ] At least one unit test for new Service logic
- [ ] No hardcoded secrets or credentials
- [ ] No PII in logs