# 🐍 PYTHON / FASTAPI + PYDANTIC V2

## Baseline
- Python 3.11+
- FastAPI (latest stable)
- Pydantic v2 — syntax differs significantly from v1, do NOT mix
- SQLAlchemy 2.0 async
- Alembic for migrations
- pytest + pytest-asyncio for testing

---

## Type Hints — Mandatory on Everything
```python
# ✅ All functions: full type hints, always
async def get_user(user_id: int, db: AsyncSession) -> UserResponse:
    ...

# ✅ Annotated for dependency injection
from typing import Annotated
from fastapi import Depends

DBSession = Annotated[AsyncSession, Depends(get_db)]
CurrentUser = Annotated[User, Depends(get_current_user)]

async def update_profile(
    user_id: int,
    body: UpdateProfileRequest,
    db: DBSession,
    current_user: CurrentUser,
) -> UserResponse:
    ...
```

---

## Pydantic v2 — Correct Syntax
```python
from pydantic import BaseModel, field_validator, ConfigDict

# Request schema
class CreateUserRequest(BaseModel):
    email: str
    password: str
    full_name: str

    @field_validator("email")
    @classmethod
    def validate_email(cls, v: str) -> str:
        if "@" not in v:
            raise ValueError("Invalid email format")
        return v.lower().strip()

# Response schema — from ORM
class UserResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)  # NOT class Config

    id: int
    email: str
    full_name: str
    created_at: datetime
```

---

## Async First
```python
# ✅ async def for all I/O-bound operations (DB, HTTP, file)
@router.get("/{user_id}", response_model=UserResponse)
async def get_user(user_id: int, db: DBSession) -> UserResponse:
    user = await user_service.get_by_id(db, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return UserResponse.model_validate(user)

# ✅ sync def only for CPU-bound — FastAPI runs it in threadpool automatically
def compute_something_heavy(data: list[int]) -> int:
    return sum(data)
```

---

## Application Lifespan
```python
# ✅ Use lifespan context manager — @app.on_event is deprecated
from contextlib import asynccontextmanager

@asynccontextmanager
async def lifespan(app: FastAPI):
    # startup
    await init_db_pool()
    yield
    # shutdown
    await close_db_pool()

app = FastAPI(title="My API", lifespan=lifespan)
```

---

## Service Layer Pattern
```python
# ✅ All business logic in Service class, not in router
class UserService:

    async def get_by_id(self, db: AsyncSession, user_id: int) -> User | None:
        result = await db.execute(select(User).where(User.id == user_id))
        return result.scalar_one_or_none()

    async def create(self, db: AsyncSession, request: CreateUserRequest) -> User:
        existing = await self.get_by_email(db, request.email)
        if existing:
            raise HTTPException(status_code=409, detail="Email already registered")

        user = User(
            email=request.email,
            full_name=request.full_name,
            hashed_password=hash_password(request.password),
        )
        db.add(user)
        await db.commit()
        await db.refresh(user)
        logger.info("user_created", email=request.email)
        return user

user_service = UserService()
```

---

## Centralized Exception Handling
```python
# ✅ Custom exceptions
class ResourceNotFoundException(Exception):
    def __init__(self, resource: str, resource_id: int):
        self.resource = resource
        self.resource_id = resource_id

# ✅ Register handlers on app
@app.exception_handler(ResourceNotFoundException)
async def not_found_handler(
    request: Request, exc: ResourceNotFoundException
) -> JSONResponse:
    return JSONResponse(
        status_code=404,
        content={
            "success": False,
            "code": "RESOURCE_NOT_FOUND",
            "message": f"{exc.resource} with id {exc.resource_id} not found",
            "timestamp": datetime.utcnow().isoformat() + "Z",
            "path": request.url.path,
        },
    )
```

---

## SQLAlchemy 2.0 Async
```python
# ✅ Session dependency
async def get_db() -> AsyncGenerator[AsyncSession, None]:
    async with AsyncSessionLocal() as session:
        try:
            yield session
        except Exception:
            await session.rollback()
            raise

# ✅ Query syntax (2.0 style — no session.query())
result = await db.execute(
    select(User)
    .where(User.status == UserStatus.ACTIVE)
    .order_by(User.created_at.desc())
    .offset(page * size)
    .limit(size)
)
users = result.scalars().all()
```

---

## Migrations — Alembic
- **NEVER** `Base.metadata.create_all()` in production
- Every schema change = new Alembic migration
- Naming: `YYYYMMDD_HHMM_short_description.py`
- Always review auto-generated migrations before applying