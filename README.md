# JobTracker API

A full-stack job application tracker — Spring Boot 3 REST API with a built-in interactive frontend, JWT authentication, PostgreSQL persistence, and automatic status history logging.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square)
![JWT](https://img.shields.io/badge/Auth-JWT-yellow?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?style=flat-square)

---

## What it does
<img width="1903" height="817" alt="image" src="https://github.com/user-attachments/assets/b9e57f8c-ae9e-400a-8449-26da6739458e" />


- Register / login with JWT-secured accounts
- Add job applications with company, title, URL, status, dates and notes
- Move applications through a 7-stage pipeline with one API call — every change is logged automatically to `status_history`
- Query applications with filters: status, date range, search by company or title — all paginated
- View live analytics: response rate, interview conversion rate, offer rate
- Serve a stunning animated frontend at `http://localhost:8080/` with no extra setup

---

## Tech stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.3 |
| Persistence | Spring Data JPA / Hibernate 6 |
| Database | PostgreSQL 16 |
| Security | Spring Security + JWT (jjwt 0.12) |
| Filtering | JPA Specifications |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Frontend | Vanilla JS + Tailwind CSS (served as static resource) |
| Containerisation | Docker + Docker Compose |
| Build | Maven 3.9 |

---

## Project structure

```
src/main/java/com/jobtracker/
├── config/          SecurityConfig.java
├── controller/      AuthController  ApplicationController  StatsController
├── dto/             auth/  application/  stats/  ErrorResponse
├── entity/          User  JobApplication  StatusHistory
├── enums/           ApplicationStatus
├── exception/       GlobalExceptionHandler  ResourceNotFoundException  UnauthorizedException
├── repository/      UserRepository  JobApplicationRepository  StatusHistoryRepository
│                    JobApplicationSpecifications
├── security/        JwtTokenProvider  JwtAuthenticationFilter  UserDetailsServiceImpl
└── service/         AuthService  ApplicationService  StatsService

src/main/resources/
├── application.yml
└── static/index.html    ← built-in frontend
```

---

## Quick start — Docker (recommended)

```bash
git clone <repo-url>
cd job-tracker
docker compose up --build
```

App → `http://localhost:8080`  
Swagger → `http://localhost:8080/swagger-ui/index.html`

Stop and remove volumes:
```bash
docker compose down -v
```

---

## Quick start — local (Maven + PostgreSQL)

**Prerequisites:** JDK 17+, Maven 3.9+, PostgreSQL running locally.

```bash
# 1. Create the database
psql -U postgres -c "CREATE DATABASE jobtracker;"

# 2. Set environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=jobtracker
export DB_USER=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=your-secret-key-minimum-32-characters-long
export JWT_EXPIRATION_MS=86400000   # 24 hours

# 3. Run
mvn spring-boot:run
```

On Windows (PowerShell):
```powershell
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="your-secret-key-minimum-32-characters-long"
~\tools\maven\bin\mvn.cmd spring-boot:run
```

---

## Environment variables

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `jobtracker` | Database name |
| `DB_USER` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | *(base64 key)* | Signing secret — **change in production** |
| `JWT_EXPIRATION_MS` | `86400000` | Token TTL in ms (default 24 h) |

---

## Data model

```
User
  id · email · password · createdAt

JobApplication
  id · userId · company · jobTitle · jobUrl
  status · appliedDate · followUpDate · notes
  createdAt · updatedAt

StatusHistory
  id · applicationId · oldStatus · newStatus · changedAt
```

**Status enum:** `APPLIED → SCREENING → INTERVIEW → TECHNICAL → OFFER / REJECTED / WITHDRAWN`

Every `PATCH /api/applications/{id}/status` call automatically inserts a `StatusHistory` row.

---

## API reference

All endpoints except `/api/auth/**` require:
```
Authorization: Bearer <token>
```

### Auth

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"password123"}'
```

Response (both):
```json
{ "token": "eyJ...", "email": "you@example.com", "userId": 1 }
```

### Applications

```bash
# List (paginated + filtered)
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/applications?status=INTERVIEW&search=google&page=0&size=20"

# Get one
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/applications/1

# Create
curl -X POST http://localhost:8080/api/applications \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "company": "Anthropic",
    "jobTitle": "Senior Software Engineer",
    "jobUrl": "https://anthropic.com/careers",
    "status": "APPLIED",
    "appliedDate": "2026-05-08",
    "notes": "Referred by a friend"
  }'

# Full update
curl -X PUT http://localhost:8080/api/applications/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"company":"Anthropic","jobTitle":"Staff Engineer","appliedDate":"2026-05-08"}'

# Status change only (auto-logs to StatusHistory)
curl -X PATCH http://localhost:8080/api/applications/1/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"INTERVIEW"}'

# Delete
curl -X DELETE http://localhost:8080/api/applications/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Query parameters for GET /api/applications:**

| Param | Type | Default | Description |
|---|---|---|---|
| `status` | enum | — | Filter by status |
| `search` | string | — | Match company or job title |
| `startDate` | ISO date | — | Applied date ≥ |
| `endDate` | ISO date | — | Applied date ≤ |
| `page` | int | `0` | Page index (0-based) |
| `size` | int | `20` | Results per page |
| `sortBy` | string | `appliedDate` | Field to sort by |
| `sortDir` | `asc`/`desc` | `desc` | Sort direction |

### Stats

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/stats/summary
```

```json
{
  "totalApplications": 45,
  "appliedCount": 20,
  "screeningCount": 10,
  "interviewCount": 8,
  "technicalCount": 3,
  "offerCount": 2,
  "rejectedCount": 1,
  "withdrawnCount": 1,
  "responseRate": 53.33,
  "interviewConversionRate": 45.83,
  "offerRate": 4.44
}
```

| Metric | Formula |
|---|---|
| `responseRate` | `(screening + interview + technical + offer + rejected) / total × 100` |
| `interviewConversionRate` | `(interview + technical) / responded × 100` |
| `offerRate` | `offer / total × 100` |

---

## Error responses

```json
{
  "timestamp": "2026-05-08T14:00:00",
  "status": 400,
  "error": "Validation Error",
  "messages": ["company: Company is required"]
}
```

| Status | Scenario |
|---|---|
| `400` | Validation failure |
| `401` | Bad credentials |
| `403` | Accessing another user's resource |
| `404` | Resource not found |
| `500` | Unexpected error |

---

## Frontend

The app ships a built-in frontend served at `http://localhost:8080/`:

- Animated landing page with scroll reveals, floating cards, and a particle network
- Three-step "How it works" walkthrough
- Live features bento grid populated from the authenticated user's data
- Embedded full app: register → dashboard → add applications → change statuses → analytics
- No build step — plain HTML/CSS/JS bundled as a Spring Boot static resource

---

## Running tests

```bash
mvn test
```

Uses H2 in-memory database via the `test` Spring profile — no PostgreSQL required.

---

## Interactive API docs

Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`.

Click **Authorize**, enter `Bearer <your-token>`, and every endpoint becomes interactive.
