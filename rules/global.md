# 🌐 GLOBAL AI RULES
> Paste this into your IDE's global AI instruction settings.
> These rules apply to ALL projects, ALL the time.

---

## 🔒 SECURITY — ALWAYS ON

### Deny List — NEVER execute
```
Network:   curl, wget, nc, ncat, telnet
Remote:    ssh, scp, ftp, sftp
Git:       git push (to unknown remotes), git remote add <unknown>
Privilege: chmod, chown, sudo, su, passwd
Shell:     export, alias, eval, exec, source
Packages:  pip install / npm install anything NOT in existing requirements.txt or package.json
```

### Secret Zero-Knowledge — NEVER read or print content from
```
.env  .env.*  .env.local  .env.production
application.yml  application.properties  application-*.yml  application-*.properties
secrets.*  *secret*  *credential*  *password*
*.pem  *.key  *.p12  *.jks  *.pfx
```
→ Always use placeholders: `YOUR_DB_PASSWORD`, `YOUR_SECRET_KEY`

### Mandatory Review — STOP and ask user before touching
- Auth / Authorization / JWT / OAuth logic
- Payment or financial calculations
- Encryption, hashing, key management
- Role / Permission / ACL changes
- PII data: email, phone, password, national ID, address
- Changes spanning > 3 files simultaneously
- Any infrastructure or deployment config

---

## 📐 CORE STANDARDS — ALWAYS ON

### Clean Code
- Functions: < 50 lines, single responsibility
- DRY: extract repeated logic (≥ 2 occurrences) into shared utility
- KISS: simplest solution that works — no premature optimization
- Naming: intention-revealing, no cryptic abbreviations

### Contemplation Depth (scale to complexity)
- **Simple** — getter, formatter, mapper: brief 2–3 sentence check
- **Medium** — new feature, new endpoint: full walkthrough
- **Complex** — auth, refactor, new module: deep dive, question every assumption

### Unified API Error Shape
All APIs must return this on error:
```json
{
  "success": false,
  "code": "RESOURCE_NOT_FOUND",
  "message": "User with id 123 not found",
  "timestamp": "2025-01-01T00:00:00Z",
  "path": "/api/v1/users/123"
}
```
- `code`: SCREAMING_SNAKE_CASE, machine-readable
- `message`: human-readable
- `timestamp`: ISO-8601 UTC, always

### API Design — Non-negotiable
- All endpoints prefixed `/api/v1/`
- List endpoints always support pagination: `?page=0&size=20&sort=field,direction`
- Filters via query params for GET — never request body
- Dates: ISO-8601 UTC only

### Logging
- NEVER log: password, token, api_key, or any PII
- ERROR → exceptions, integration failures
- INFO  → business events (user created, order placed)
- DEBUG → dev-only, never in production
- Format: structured JSON (ELK / Loki compatible)