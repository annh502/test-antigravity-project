# ☕ JAVA / SPRING BOOT 3.x

## Baseline
- Java 17+ — use `record`, text blocks, sealed classes where appropriate
- Spring Boot 3.x — `jakarta.*` namespace (NOT `javax.*`)
- Constructor injection via `@RequiredArgsConstructor` — no `@Autowired` on fields

---

## Architecture & Layers

```
@RestController  →  validates HTTP, delegates immediately
@Service         →  ALL business logic lives here
@Repository      →  data access only, no business logic
DTO (record)     →  what crosses the API boundary — NEVER expose @Entity directly
```

### Controller Pattern
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody CreateUserRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }
}
```

### Service Pattern
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse findById(Long id) {
        return userRepository.findById(id)
            .map(UserResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        var user = User.builder()
            .email(request.email())
            .hashedPassword(passwordEncoder.encode(request.password()))
            .fullName(request.fullName())
            .build();
        log.info("Creating user with email={}", request.email());
        return UserResponse.from(userRepository.save(user));
    }
}
```

---

## DTO — Prefer Java Records
```java
// Response DTO
public record UserResponse(Long id, String email, String fullName, LocalDateTime createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(),
            user.getFullName(), user.getCreatedAt());
    }
}

// Request DTO
public record CreateUserRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String fullName
) {}
```

---

## Exception Handling — Centralized
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        ResourceNotFoundException ex, HttpServletRequest request
    ) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of("RESOURCE_NOT_FOUND", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex, HttpServletRequest request
    ) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
            .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.ofValidation(errors, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
        Exception ex, HttpServletRequest request
    ) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of("INTERNAL_ERROR", "Unexpected server error", request.getRequestURI()));
    }
}
```

---

## JPA / Database Rules
- `FetchType.LAZY` always — never EAGER
- N+1 prevention: `@EntityGraph` or `JOIN FETCH` for known use cases
- Pagination: always `Pageable` for list queries
- Migrations: **Flyway** or **Liquibase** — never `ddl-auto: create` or `update` in production
- Soft delete: prefer `@Where(clause="deleted=false")` + `deleted_at` column over hard delete

---

## Lombok Policy
| Annotation | Policy |
|---|---|
| `@Slf4j` | ✅ Always OK |
| `@RequiredArgsConstructor` | ✅ Always OK |
| `@Builder` | ✅ OK on non-Entity |
| `@Value` | ✅ OK for immutable classes |
| `@Data` on `@Entity` | ⚠️ Avoid — breaks JPA proxies & equals/hashCode |
| `@SneakyThrows` | ❌ Never — hides checked exceptions |

---

## Validation
- `@Valid` on `@RequestBody` in Controller
- Business rule violations → throw custom exceptions in Service
- Custom constraints → `@interface` + `ConstraintValidator`, never ad-hoc in Service