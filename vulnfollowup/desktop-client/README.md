# VulnFollowUp – Web client (desktop-client)

SPA for the ADMIN and ANALYST profiles. Created with the same template as `ionic start blank --type=angular-standalone`:
**Ionic 9 + Angular 22 (Standalone components, Signals, Reactive Forms)**, Vitest for unit tests, ESLint and Capacitor.
It consumes **only** the VulnFollowUp REST API (contract: `docs/api/README.md`); it never accesses the database.

Author: Aarón Hernández Moreno.

## Requirements

- Node.js 22 or newer (`node -v`) and npm
- Ionic CLI: `npm install -g @ionic/cli`
- The backend running on `http://localhost:8080` (see the root README: `docker compose up --build`)

## Install and run

```bash
cd desktop-client
npm install
ionic serve          # http://localhost:8100
```

The API URL is configured in `src/environments/environment.ts` (`apiUrl`).
Test users: `admin / admin123`, `analyst / analyst123`, `viewer / viewer123` (synthetic, development only).
The production build (`environment.prod.ts`) uses `/api`, which Nginx forwards to the backend container.

## Tests, lint and build

```bash
npm test             # unit tests with Vitest (27 tests)
npm run lint         # ESLint (angular-eslint)
ionic build          # production build in www/
```

## Structure

```
src/
├── app/
│   ├── app.component.*      root component (ion-app + ion-router-outlet)
│   ├── app.routes.ts        lazy-loaded routes with guards
│   ├── core/
│   │   ├── guards/          auth.guard.ts (authGuard, guestGuard), role.guard.ts
│   │   ├── interceptors/    auth.interceptor.ts (JWT), error.interceptor.ts (401/403/404/409/5xx)
│   │   ├── services/        auth, notification (toasts), language (ES/EN)
│   │   └── utils/           cvss.util.ts (version ↔ vector ↔ score ↔ severity)
│   ├── models/              *.model.ts interfaces and enums: same fields as the backend DTOs
│   ├── components/          shell (side menu by role), page-placeholder
│   └── pages/               login, dashboard, vulnerabilities, assets, providers, import, forbidden
├── assets/i18n/             es.json / en.json
├── environments/            environment.ts / environment.prod.ts
├── theme/variables.scss     colour palette (docs/di/style-guide.md)
├── global.scss              Ionic CSS + accessibility helpers
└── main.ts                  bootstrap: IonicRouteStrategy, router, HttpClient, translations
```

## Design decisions

| Decision | Reason | Alternative discarded |
|---|---|---|
| Ionic + Angular Standalone (`ionic start` template) | Framework and project structure used in class; Ionic components include accessibility (ARIA, focus) | Angular Material: good option but new for the team |
| Signals for the session | Simple reactive state without extra libraries | NgRx: too complex for this size |
| JWT in `sessionStorage` | The session ends when the browser closes | `localStorage`: persists after closing |
| Functional interceptors | One place for the token and for user-friendly errors | Handling errors in every component |
| Lazy-loaded pages | Smaller initial download (sustainability) | One big bundle |
| ngx-translate (ES/EN) | Change language at runtime with JSON files | Angular i18n: one build per language |
| Roles checked in guards **and** in the backend | The UI hides what you cannot do; the API is the real security | Trusting the client |
| Light theme only (for now) | The palette is contrast-checked for light mode | Ionic's automatic dark palette (not checked yet) |
