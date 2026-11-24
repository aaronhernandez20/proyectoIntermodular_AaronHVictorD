# VulnFollowUp – API contract (v0.1)

Common contract between the backend (Víctor) and the clients (web: Aarón, Android: Víctor).
Both clients consume **only** this API; neither accesses the database directly.

> Status: **draft for review**. Any change must be agreed by both members and updated here first.

## General rules

| Item | Value |
|---|---|
| Base URL (local) | `http://localhost:8080/api` (Android emulator: `http://10.0.2.2:8080/api`) |
| Format | JSON, UTF-8. Dates `YYYY-MM-DD`, timestamps ISO-8601 (`2026-11-24T18:30:00Z`) |
| Authentication | `Authorization: Bearer <JWT>` on every request except `POST /auth/login` |
| Roles | `ADMIN`, `ANALYST`, `VIEWER` (always checked in the backend) |
| Field names | `camelCase` in English |
| Pagination | `?page=0&size=25&sort=score,desc` → Spring `Page` format (see below) |

### Enumerations

| Enum | Values |
|---|---|
| `Role` | `ADMIN`, `ANALYST`, `VIEWER` |
| `VulnStatus` | `OPEN`, `PENDING_REMEDIATION`, `RESOLVED`, `CLOSED` |
| `Severity` | `NONE`, `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `CvssVersion` | `3.1`, `4.0` |
| `AssetType` | `API`, `SERVER`, `DATABASE`, `WEB_APP`, `NETWORK`, `OTHER` |
| `Environment` | `PRODUCTION`, `STAGING`, `DEVELOPMENT` |
| `AssetStatus` | `ACTIVE`, `INACTIVE` |
| `Criticality` | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |

Severity is **derived from the score** (0.0 → NONE, 0.1–3.9 LOW, 4.0–6.9 MEDIUM, 7.0–8.9 HIGH, 9.0–10.0 CRITICAL).
The backend rejects (400) any record whose version, vector prefix, score and severity are not coherent.

### Error format (all errors)

```json
{
  "timestamp": "2026-11-24T18:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Vector prefix CVSS:3.1 does not match version 4.0",
  "path": "/api/vulnerabilities",
  "fieldErrors": [ { "field": "cvssVector", "message": "must start with CVSS:4.0/" } ]
}
```

| Code | When |
|---|---|
| 400 | Invalid data (validation, incoherent CVSS, invalid status transition) |
| 401 | Missing, invalid or expired token / wrong credentials |
| 403 | Valid token but the role is not allowed |
| 404 | Resource not found |
| 409 | Optimistic locking conflict (`version` outdated) or duplicate code |

### Page format

```json
{ "content": [ ... ], "totalElements": 42, "totalPages": 2, "number": 0, "size": 25 }
```

---

## 1. Authentication

### `POST /auth/login` — public

Request:
```json
{ "username": "analyst", "password": "Analyst123!" }
```
Response 200:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600,
  "user": { "id": 2, "username": "analyst", "fullName": "Ana Analyst", "role": "ANALYST" }
}
```
Response 401: wrong credentials (generic message, never says which field failed).

## 2. Assets

### `GET /assets` — all roles
Filters: `?type=API&environment=PRODUCTION&status=ACTIVE&owner=ana&q=pagos`

Response 200:
```json
[
  {
    "id": 1,
    "name": "API de pagos",
    "type": "API",
    "owner": "Ana Analyst",
    "environment": "PRODUCTION",
    "status": "ACTIVE",
    "providerIds": [1]
  }
]
```

### `GET /assets/{id}` — all roles → one asset (same shape).

### `POST /assets` — ADMIN, ANALYST
```json
{ "name": "Servidor de aplicaciones", "type": "SERVER", "owner": "Luis Admin", "environment": "PRODUCTION", "status": "ACTIVE" }
```
Response 201: created asset with `id`.

### `PUT /assets/{id}` — ADMIN, ANALYST → same body, 200 with updated asset.

### `PATCH /assets/{id}/status` — ADMIN, ANALYST (deactivate / reactivate)
```json
{ "status": "INACTIVE" }
```

## 3. Vulnerabilities

### `GET /vulnerabilities` — all roles (paginated)
Filters (all optional and combinable):
`?severity=CRITICAL&severity=HIGH&status=OPEN&assetId=1&assignee=ana&minScore=7.0&maxScore=10&q=log4j&page=0&size=25&sort=score,desc`

Response 200 (`Page` of summaries):
```json
{
  "content": [
    {
      "id": 12,
      "code": "CVE-2021-44228",
      "title": "Log4Shell – remote code execution in Apache Log4j",
      "cvssVersion": "3.1",
      "score": 10.0,
      "severity": "CRITICAL",
      "status": "PENDING_REMEDIATION",
      "assignee": "Ana Analyst",
      "targetDate": "2026-12-01",
      "assets": [ { "id": 1, "name": "API de pagos" } ]
    }
  ],
  "totalElements": 1, "totalPages": 1, "number": 0, "size": 25
}
```

### `GET /vulnerabilities/{id}` — all roles (full detail)
```json
{
  "id": 12,
  "code": "CVE-2021-44228",
  "title": "Log4Shell – remote code execution in Apache Log4j",
  "description": "Apache Log4j2 JNDI features do not protect against attacker-controlled LDAP...",
  "publishedDate": "2021-12-10",
  "references": ["https://nvd.nist.gov/vuln/detail/CVE-2021-44228"],
  "cvssVersion": "3.1",
  "cvssVector": "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:C/C:H/I:H/A:H",
  "score": 10.0,
  "severity": "CRITICAL",
  "assessmentDate": "2026-11-20",
  "status": "PENDING_REMEDIATION",
  "assignee": "Ana Analyst",
  "targetDate": "2026-12-01",
  "assets": [ { "id": 1, "name": "API de pagos" } ],
  "comments": [
    { "id": 3, "author": "analyst", "text": "Patch 2.17.1 scheduled", "evidence": "Ticket CHG-123", "createdAt": "2026-11-21T10:00:00Z" }
  ],
  "version": 4
}
```
`version` is the optimistic-locking counter: the client must send it back when modifying.

### `POST /vulnerabilities` — ADMIN, ANALYST
```json
{
  "code": "VFU-0021",
  "title": "Weak TLS configuration",
  "description": "The server accepts TLS 1.0 connections.",
  "publishedDate": "2026-11-24",
  "references": [],
  "cvssVersion": "3.1",
  "cvssVector": "CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:N/A:N",
  "score": 3.7,
  "severity": "LOW",
  "assetIds": [2]
}
```
Response 201: full detail with `status: "OPEN"`. 400 if CVSS incoherent, 409 if `code` already exists.

### `PUT /vulnerabilities/{id}` — ADMIN, ANALYST → edit data (same body as POST + `"version": 4`).

### `PATCH /vulnerabilities/{id}/status` — ADMIN, ANALYST (CU4)
```json
{
  "status": "RESOLVED",
  "assignee": "Ana Analyst",
  "targetDate": "2026-12-01",
  "comment": "Library updated to 2.17.1",
  "evidence": "Commit a1b2c3 / screenshot in ticket CHG-123",
  "version": 4
}
```
Allowed transitions: `OPEN → PENDING_REMEDIATION → RESOLVED → CLOSED` (and back to `OPEN` to reopen).
Response 200: full detail. **409** if `version` is outdated (another user changed it first).

### `POST /vulnerabilities/{id}/assessment` — ADMIN, ANALYST (new CVSS assessment)
```json
{ "cvssVersion": "4.0", "cvssVector": "CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:H/VI:H/VA:H/SC:N/SI:N/SA:N", "score": 9.3, "severity": "CRITICAL" }
```

## 4. Import (CU1 / AD)

### `POST /imports/vulnerabilities` — ADMIN, ANALYST (multipart `file` = JSON)
Response 202:
```json
{ "jobId": "b7f3...", "status": "RUNNING" }
```
### `GET /imports/{jobId}`
```json
{ "jobId": "b7f3...", "status": "FAILED", "processed": 15, "imported": 0,
  "errors": [ { "record": 15, "message": "Vector prefix CVSS:3.1 does not match version 4.0" } ] }
```
`status`: `RUNNING`, `COMPLETED`, `FAILED` (FAILED ⇒ full rollback, nothing saved).

## 5. Dashboard

### `GET /dashboard/summary` — all roles
```json
{
  "total": 24,
  "bySeverity": { "NONE": 0, "LOW": 3, "MEDIUM": 8, "HIGH": 7, "CRITICAL": 6 },
  "byStatus": { "OPEN": 10, "PENDING_REMEDIATION": 6, "RESOLVED": 5, "CLOSED": 3 },
  "overdue": 2
}
```

## 6. ICT providers (SGE / DORA)

### `GET /providers` — all roles
```json
[
  {
    "id": 1,
    "name": "HostingSur S.L.",
    "service": "Cloud hosting",
    "criticality": "HIGH",
    "contractRef": "CTR-2025-014",
    "reviewDate": "2027-03-01",
    "contact": "soporte@hostingsur.example",
    "assets": [ { "id": 1, "name": "API de pagos" } ],
    "atRisk": true
  }
]
```
### `GET /providers/{id}` — all roles → one provider.
### `POST /providers` — ADMIN only / `PUT /providers/{id}` — ADMIN only
```json
{ "name": "HostingSur S.L.", "service": "Cloud hosting", "criticality": "HIGH", "contractRef": "CTR-2025-014",
  "reviewDate": "2027-03-01", "contact": "soporte@hostingsur.example", "assetIds": [1] }
```
### `GET /providers/{id}/report` — all roles
```json
{
  "provider": { "id": 1, "name": "HostingSur S.L.", "criticality": "HIGH", "reviewDate": "2027-03-01" },
  "atRisk": true,
  "riskReasons": ["Critical provider with CRITICAL vulnerabilities pending"],
  "pending": [
    { "id": 12, "code": "VFU-0012", "title": "...", "score": 9.8, "severity": "CRITICAL",
      "status": "PENDING_REMEDIATION", "targetDate": "2026-12-01", "assetName": "API de pagos" }
  ]
}
```
`pending` is ordered by severity (desc) and then target date (asc).
**Business rule (atRisk):** provider with criticality HIGH/CRITICAL that has CRITICAL vulnerabilities not RESOLVED/CLOSED, **or** whose `reviewDate` has passed.

## 7. Reports (PSP – child process)

### `POST /reports` — ADMIN, ANALYST
```json
{ "format": "CSV" }
```
Response 202 `{ "jobId": "...", "status": "RUNNING" }`; `GET /reports/{jobId}` → status + `downloadUrl` when `COMPLETED`,
or a readable `message` when the child process fails.

## Test users (development only, synthetic)

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin123!` | ADMIN |
| `analyst` | `Analyst123!` | ANALYST |
| `viewer` | `Viewer123!` | VIEWER |
