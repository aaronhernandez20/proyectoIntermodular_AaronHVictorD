# VulnFollowUp

> Platform to manage and prioritise vulnerabilities — Proyecto Intermodular 2º DAM (2026-2027), Instituto Nebrija.

## Authors
- Aarón Hernández Moreno
- Víctor de Lucas Fernández

## Description
_TODO_

## Architecture
_TODO_ (backend REST API + desktop/web client + Android app + relational database)

## Repository structure
```
backend/          REST API and ICT providers (PSP + AD + SGE)
desktop-client/   Desktop client / web SPA (DI)
android-app/      Android app (PMDM)
database/         SQL scripts and initial data
data/             JSON/CSV files for initial load
docs/             Documentation (API, English, IPE, SGE, digitalisation, sustainability)
```

## Requirements
- Docker Desktop (recommended), **or**
- Java 21 + Maven 3.9 + PostgreSQL 16 (to run the backend without Docker)

## Installation and configuration
```bash
cd vulnfollowup
cp .env.example .env        # then edit DB_PASSWORD and JWT_SECRET
docker compose up --build   # PostgreSQL + backend
```
- API: http://localhost:8080 (health check: `/actuator/health`)
- API docs (Swagger UI): http://localhost:8080/swagger-ui.html
- API contract with examples: [`docs/api/README.md`](docs/api/README.md)
- Data model: [`database/README.md`](database/README.md)

On start-up the backend creates the database with Flyway and loads `data/assets.json` (use case CU1).

Run the backend tests: `cd backend && mvn test`

## Usage
_TODO_

## Test users
| User | Role | Password |
|------|------|----------|
| admin | ADMIN | admin123 |
| analyst | ANALYST | analyst123 |
| viewer | VIEWER | viewer123 |

These are synthetic test accounts. Passwords are stored as BCrypt hashes.
