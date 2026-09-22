# FocusForge

Plan → Execute → Track → Analyze → Improve.

## Status
- `backend/` Spring Boot 3.3 / Java 21 / PostgreSQL. Built, tested (`mvn test` green, 15/15), and deployed live on Render.
- `frontend/` React + Vite + TypeScript + Tailwind. Built and compiles cleanly (`npm run build`); see `frontend/README.md` to run it or deploy it to Vercel.

## Run the backend
```bash
docker compose up -d db                       # PostgreSQL 16
cd backend
export JWT_SECRET="$(openssl rand -base64 48)"
mvn test                                      # H2-backed flow tests
mvn spring-boot:run
```
Health check: `GET http://localhost:8080/actuator/health`

## Environment variables
| Name | Required | Default |
|---|---|---|
| `JWT_SECRET` | yes (>= 32 chars) | none, app refuses to start |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | no | `jdbc:postgresql://localhost:5432/focusforge` / `focusforge` / `focusforge` |
| `CORS_ORIGINS` | no | `http://localhost:5173` |
| `APP_TIMEZONE` | no | `Asia/Kolkata` (defines "today" and week boundaries) |
| `JWT_EXPIRATION_HOURS` | no | `72` |
| `DDL_AUTO` | no | `update` (switch to `validate` + migrations for production) |

## Run the frontend
```bash
cd frontend
npm install
npm run dev      # http://localhost:5173, proxies /api to localhost:8080
```
See `frontend/README.md` for the production build and Vercel deployment steps.

## API
All routes except `/api/auth/register`, `/api/auth/login` and `/actuator/health` need `Authorization: Bearer <token>`.
Errors: `{ timestamp, status, message, path, fieldErrors? }`.

| Area | Routes |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`, `GET/PUT /api/settings` |
| Dashboard | `GET /api/dashboard/today` |
| Routine | `GET /api/routines?date`, `POST /api/routines`, `PUT/DELETE /api/routines/{id}`, `POST/DELETE /api/routines/{id}/complete?date`, `PUT /api/routines/wake`, `POST/DELETE /api/routines/morning-items`, `PUT /api/routines/morning-checks` |
| Study | `GET/POST /api/study-sessions`, `PUT/DELETE /api/study-sessions/{id}` |
| Problems | `/api/{dsa\|sql\|java\|problem-solving}/problems` (GET, POST, PUT/{id}, DELETE/{id}) |
| Topics | `/api/{aws\|dsa\|sql\|java\|interview}/topics` (GET, POST, PUT/{id}, DELETE/{id}) |
| Jobs | `GET /api/jobs?company&role&status&location&from&to`, `POST`, `PUT/DELETE /{id}` |
| Projects | `GET/POST /api/projects`, `PUT/DELETE /api/projects/{id}` |
| Fitness | `GET/POST /api/fitness`, `GET /api/fitness/summary`, `PUT/DELETE /{id}` |
| Reflection | `GET/PUT /api/reflections?date` |
| Analytics | `GET /api/analytics/daily`, `/weekly`, `/monthly` |
| Reports | `GET /api/reports/weekly`, `/weekly/pdf`, `/insights`, `GET/PUT /api/reports/plan?weekStart` |
| Demo data | `GET/POST/DELETE /api/demo` |

## Design notes
- Durations are stored in minutes. Weeks start on Monday.
- Every user-owned row has `userId`; every query is scoped by it.
- Demo rows carry `demo = true` and are removed by `DELETE /api/demo` without touching real entries.
- Weekly analysis is rule-based (`RuleBasedInsightGenerator`), built only from recorded numbers. Swap in an LLM by providing another `InsightGenerator` bean.
- A day counts toward the streak at >= 50% overall completion.
