# VulnFollowUp API — HTTP contract

This is the contract shared by the web client (Angular) and the Android app.
Both clients talk **only** to this API; they never access the database.

- Base URL (local): `http://localhost:8080`
- Android emulator: `http://10.0.2.2:8080`
- Interactive docs (Swagger UI): `http://localhost:8080/swagger-ui.html`
- Format: JSON, UTF-8. Dates `YYYY-MM-DD`, timestamps ISO-8601.

Status legend: ✅ implemented · 🔜 planned (shape agreed, use mock data until it is ready)

---

## 1. Authentication

All endpoints except login, `/actuator/health` and Swagger require a JWT:

```
Authorization: Bearer <accessToken>
```

The token expires after 120 minutes (configurable). When the API returns `401`, the client must go back to the login screen.

### ✅ `POST /api/auth/login`

Request:
```json
{ "username": "analyst", "password": "analyst123" }
```
Response `200`:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresAt": "2026-11-24T20:15:00Z",
  "username": "analyst",
  "fullName": "Ana Analista",
  "roles": ["ANALYST"]
}
```
Wrong credentials → `401`.

### ✅ `GET /api/auth/me`
Returns `{ "username", "fullName", "roles" }` for the current token.

### Test users

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `analyst` | `analyst123` | ANALYST |
| `viewer` | `viewer123` | VIEWER |

### Permissions

| Action | ADMIN | ANALYST | VIEWER |
|---|:-:|:-:|:-:|
| Read anything (`GET`) | ✔ | ✔ | ✔ |
| Create/edit assets and vulnerabilities, change status | ✔ | ✔ | ✘ (403) |
| Create/edit ICT providers | ✔ | ✘ (403) | ✘ (403) |

Roles are always checked in the backend. Hiding buttons in the UI is only for usability.

---

## 2. Error format (all endpoints)

```json
{
  "timestamp": "2026-11-24T18:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Datos de entrada no válidos",
  "fields": { "name": "must not be blank" }
}
```

| Code | When |
|---|---|
| 400 | Validation error (`fields` contains the details), malformed JSON, invalid enum value, incoherent CVSS |
| 401 | No token, invalid or expired token, wrong login |
| 403 | Authenticated but the role is not allowed |
| 404 | Resource not found |
| 409 | Duplicate (e.g. asset name), or **optimistic lock**: someone else modified the record. Reload and retry. |

---

## 3. Assets

Enums:
- `type`: `API`, `SERVER`, `DATABASE`, `WEB_APP`, `MOBILE_APP`, `NETWORK`, `OTHER`
- `environment`: `PRODUCTION`, `STAGING`, `TEST`, `DEVELOPMENT`
- `status`: `ACTIVE`, `INACTIVE`
- `exposure`: `INTERNET`, `INTERNAL`

### ✅ `GET /api/assets?type=&environment=&status=&owner=`
All filters are optional. `owner` is a partial, case-insensitive match.

Response `200`:
```json
[
  {
    "id": 1,
    "name": "API de pagos",
    "type": "API",
    "owner": "Equipo Pagos",
    "environment": "PRODUCTION",
    "status": "ACTIVE",
    "exposure": "INTERNET",
    "providerId": null,
    "providerName": null,
    "version": 0,
    "createdAt": "2026-11-24T18:00:00",
    "updatedAt": "2026-11-24T18:00:00"
  }
]
```

### ✅ `GET /api/assets/{id}`
Returns one asset, or `404`.

### ✅ `POST /api/assets` (ADMIN, ANALYST)
```json
{
  "name": "Servidor de backups",
  "type": "SERVER",
  "owner": "Equipo Sistemas",
  "environment": "PRODUCTION",
  "exposure": "INTERNAL",
  "providerName": null
}
```
Returns `201` with the asset. Duplicate name returns `409`.

### ✅ `PUT /api/assets/{id}` (ADMIN, ANALYST)
Send **all** the fields plus the `version` you received in the GET:
```json
{
  "name": "Servidor de backups",
  "type": "SERVER",
  "owner": "Equipo Sistemas",
  "environment": "PRODUCTION",
  "exposure": "INTERNAL",
  "status": "ACTIVE",
  "version": 0
}
```
If someone saved first, the API returns `409`. Show "modified by another user, reload".

### ✅ `PATCH /api/assets/{id}/deactivate` (ADMIN, ANALYST)
No body. Sets `status` to `INACTIVE`; assets are never deleted.

---

## 4. Vulnerabilities 🔜

Enums:
- `status`: `OPEN`, `PENDING_REMEDIATION`, `RESOLVED`, `CLOSED`
- `severity`: `NONE`, `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- `cvssVersion`: `"3.1"`, `"4.0"`

CVSS rules (validated by the API and also by the database):

| Score | Severity |
|---|---|
| 0.0 | NONE |
| 0.1 – 3.9 | LOW |
| 4.0 – 6.9 | MEDIUM |
| 7.0 – 8.9 | HIGH |
| 9.0 – 10.0 | CRITICAL |

- Version `3.1` requires a vector starting with `CVSS:3.1/`; version `4.0` requires `CVSS:4.0/`.
- The severity is **derived from the score** by the API. The client can show it in the form, but the API decides.

### 🔜 `GET /api/vulnerabilities`
Query params (all optional, combinable): `severity`, `status`, `assetId`, `assigneeId`, `minScore`, `maxScore`, `q` (text in code/title), `page` (0-based), `size` (default 25), `sort` (e.g. `cvssScore,desc`).

Response `200` (paginated):
```json
{
  "content": [
    {
      "id": 12,
      "code": "CVE-2021-44228",
      "title": "Log4Shell - RCE in Apache Log4j",
      "status": "PENDING_REMEDIATION",
      "cvssVersion": "3.1",
      "cvssScore": 10.0,
      "severity": "CRITICAL",
      "assets": [{ "id": 4, "name": "Servidor de aplicaciones Tomcat" }],
      "assignee": { "id": 2, "username": "analyst", "fullName": "Ana Analista" },
      "targetDate": "2026-12-01"
    }
  ],
  "page": 0,
  "size": 25,
  "totalElements": 23,
  "totalPages": 1
}
```

### 🔜 `GET /api/vulnerabilities/{id}`
Full detail:
```json
{
  "id": 12,
  "code": "CVE-2021-44228",
  "title": "Log4Shell - RCE in Apache Log4j",
  "description": "Apache Log4j2 JNDI features do not protect against attacker controlled LDAP...",
  "publishedDate": "2021-12-10",
  "status": "PENDING_REMEDIATION",
  "cvssVersion": "3.1",
  "cvssVector": "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:C/C:H/I:H/A:H",
  "cvssScore": 10.0,
  "severity": "CRITICAL",
  "assets": [{ "id": 4, "name": "Servidor de aplicaciones Tomcat" }],
  "assignee": { "id": 2, "username": "analyst", "fullName": "Ana Analista" },
  "targetDate": "2026-12-01",
  "references": ["https://nvd.nist.gov/vuln/detail/CVE-2021-44228"],
  "comments": [
    {
      "id": 3,
      "author": "analyst",
      "text": "Patch scheduled for Friday",
      "evidence": "Change request CR-1042",
      "createdAt": "2026-11-24T18:00:00"
    }
  ],
  "version": 2,
  "createdAt": "2026-11-20T10:00:00",
  "updatedAt": "2026-11-24T18:00:00"
}
```

### 🔜 `POST /api/vulnerabilities` (ADMIN, ANALYST) — CU3
```json
{
  "code": "VFU-0021",
  "title": "Weak TLS configuration in payments API",
  "description": "TLS 1.0 is still enabled",
  "publishedDate": "2026-11-20",
  "cvssVersion": "3.1",
  "cvssVector": "CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:N/A:N",
  "cvssScore": 5.9,
  "assetIds": [1],
  "references": []
}
```
It is created with `status: OPEN`. Returns `201`. Incoherent CVSS returns `400` with a clear message.

### 🔜 `PUT /api/vulnerabilities/{id}` (ADMIN, ANALYST)
Same body as POST plus `version`.

### 🔜 `PATCH /api/vulnerabilities/{id}/status` (ADMIN, ANALYST) — CU4
```json
{
  "status": "RESOLVED",
  "assigneeId": 2,
  "targetDate": "2026-12-01",
  "comment": "Updated log4j to 2.17.1",
  "evidence": "Deployment #455",
  "version": 2
}
```
Allowed transitions: `OPEN → PENDING_REMEDIATION → RESOLVED → CLOSED`, and `RESOLVED → PENDING_REMEDIATION` (reopen). Any other transition returns `400`. Stale `version` returns `409`.

### 🔜 `POST /api/vulnerabilities/{id}/assessment` (ADMIN, ANALYST)
New CVSS assessment (the previous one is kept in the history):
```json
{ "cvssVersion": "4.0", "cvssVector": "CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:H/VI:H/VA:H/SC:N/SI:N/SA:N", "cvssScore": 9.3 }
```

---

## 5. Dashboard 🔜

### `GET /api/dashboard/summary`
```json
{
  "total": 23,
  "bySeverity": { "CRITICAL": 4, "HIGH": 7, "MEDIUM": 8, "LOW": 3, "NONE": 1 },
  "byStatus": { "OPEN": 9, "PENDING_REMEDIATION": 6, "RESOLVED": 5, "CLOSED": 3 },
  "overdue": 2
}
```

---

## 6. ICT providers (DORA) 🔜

`criticality`: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

### `GET /api/providers`
```json
[
  {
    "id": 1,
    "name": "HostingSur S.L.",
    "serviceProvided": "Cloud hosting",
    "criticality": "HIGH",
    "contractRef": "CTR-2025-014",
    "nextReviewDate": "2027-03-01",
    "contactName": "Laura Gómez",
    "contactEmail": "soporte@hostingsur.example",
    "atRisk": true,
    "assets": [{ "id": 1, "name": "API de pagos" }]
  }
]
```

### `POST /api/providers` · `PUT /api/providers/{id}` (ADMIN only)
Same fields as above, without `id`, `atRisk` and `assets`. Assets are linked with `"assetIds": [1, 4]`.

### `GET /api/providers/{id}/report`
Pending vulnerabilities of the provider's assets, most severe first:
```json
{
  "provider": "HostingSur S.L.",
  "criticality": "HIGH",
  "atRisk": true,
  "riskReasons": ["Critical vulnerability VFU-0012 still PENDING_REMEDIATION"],
  "pending": [
    { "code": "VFU-0012", "title": "...", "cvssScore": 9.8, "severity": "CRITICAL",
      "status": "PENDING_REMEDIATION", "asset": "API de pagos", "targetDate": "2026-12-05" }
  ]
}
```

---

## 7. Imports and reports 🔜

| Method | Path | Use |
|---|---|---|
| `POST` | `/api/imports/vulnerabilities` | Upload a JSON file (multipart, field `file`). Runs in the background and returns `202` with `{ "jobId": "..." }` |
| `GET` | `/api/imports/{jobId}` | `{ "status": "RUNNING|DONE|FAILED", "imported": 20, "errors": ["record 15: vector does not match version"] }`. If one record fails, nothing is saved (rollback). |
| `POST` | `/api/reports` | Generates the CSV/HTML report through an external process and returns `202` with `{ "jobId": "..." }` |
| `GET` | `/api/reports/{jobId}` | Status, plus a download link when it is done |

---

## 8. Health

`GET /actuator/health` returns `{ "status": "UP" }` (public). It is the first step of the demo.
