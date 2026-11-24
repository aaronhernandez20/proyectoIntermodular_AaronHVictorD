# VulnFollowUp – Style guide (web client)

Author: Aarón Hernández Moreno · Module: Desarrollo de Interfaces · Version 0.1 (Phase 1)

Visual base of the web client (Ionic 9 + Angular 22 Standalone). The colours are defined as CSS variables
in `desktop-client/src/theme/variables.scss`, so changing a colour here means changing it in one place only.

## 1. Design principles

1. **Clarity before decoration.** It is a daily work tool for ADMIN and ANALYST: dense information, few distractions.
2. **Colour never alone.** Severity and status always show a colour **and** a text label (WCAG 1.4.1).
3. **Same words everywhere.** The severity scale and the status names are the same in the web, the Android app and the API.
4. **Bilingual from day one.** All texts live in `src/assets/i18n/es.json` and `src/assets/i18n/en.json`.

## 2. Colour palette

Contrast ratios calculated with the WCAG 2.1 formula (minimum 4.5:1 for normal text, 3:1 for UI components).

| Token | Hex | Use | Contrast with white text | AA |
|---|---|---|---|---|
| `--ion-color-primary` | `#1d4ed8` | Main buttons, links, toolbar | 6.70:1 | ✅ |
| `--ion-color-primary-tint` | `#1e40af` | Active menu item | 8.72:1 | ✅ |
| `--vfu-navy` | `#0b2545` | Brand, ADMIN chip, skip link | 15.39:1 | ✅ |
| `--ion-color-secondary` | `#0f766e` | Secondary actions | 5.47:1 | ✅ |
| `--ion-color-danger` | `#b91c1c` | Errors, logout | 6.47:1 | ✅ |
| `--ion-color-success` | `#15803d` | Confirmations | 5.02:1 | ✅ |
| `--ion-color-medium` | `#475569` | Secondary text, VIEWER chip | 7.58:1 | ✅ |
| `--ion-text-color` on `--ion-background-color` | `#0f172a` / `#f8fafc` | Body text | 17.06:1 | ✅ |

### Severity scale (CVSS Base, statement table 2)

| Severity | Score | Token | Hex | Contrast (white text) |
|---|---|---|---|---|
| None / Ninguna | 0.0 | `--vfu-sev-none` | `#475569` | 7.58:1 ✅ |
| Low / Baja | 0.1 – 3.9 | `--vfu-sev-low` | `#15803d` | 5.02:1 ✅ |
| Medium / Media | 4.0 – 6.9 | `--vfu-sev-medium` | `#a16207` | 4.92:1 ✅ |
| High / Alta | 7.0 – 8.9 | `--vfu-sev-high` | `#c2410c` | 5.18:1 ✅ |
| Critical / Crítica | 9.0 – 10.0 | `--vfu-sev-critical` | `#b91c1c` | 6.47:1 ✅ |

### Remediation status

| Status | Token | Hex |
|---|---|---|
| OPEN / Abierta | `--vfu-status-open` | `#b91c1c` |
| PENDING_REMEDIATION / Pendiente | `--vfu-status-pending` | `#a16207` |
| RESOLVED / Resuelta | `--vfu-status-resolved` | `#15803d` |
| CLOSED / Cerrada | `--vfu-status-closed` | `#475569` |

### Keyboard focus

Two-tone ring: 3 px amber `#f59e0b` + 6 px navy `#0b2545`. Amber alone on white only reaches 2.15:1
(below the 3:1 of WCAG 1.4.11), so the navy halo guarantees visibility on light and dark backgrounds.

## 3. Typography

| Element | Font | Size | Weight |
|---|---|---|---|
| Family | Inter → Segoe UI → Roboto → Arial (system fallback, no download) | – | – |
| Page title (h1) | | 1.25 – 1.5 rem | 600 |
| Section title (h2) | | 1.1 rem | 600 |
| Body | | 1 rem (16 px) | 400 |
| Help / error notes | | 0.85 rem | 400 |

Text sizes use `rem`, so they grow with the browser zoom (WCAG 1.4.4, up to 200 %).

## 4. Components

| Component | Ionic base | Rules |
|---|---|---|
| Primary button | `ion-button` (solid, primary) | One per screen; icon + text, icon with `aria-hidden` |
| Secondary button | `ion-button fill="outline"` | Language, logout, cancel |
| Text field | `ion-input fill="outline" labelPlacement="floating"` | Always a visible label; error below the field linked with `aria-describedby` |
| Error message | `ion-note color="danger"` | Form-level errors use `role="alert"` |
| Notifications | `ion-toast` (top, 4 s, OK button) | Network, 401, 403, 404, 409 and 5xx errors |
| Side menu | `ion-split-pane` + `ion-menu` | Fixed on screens ≥ 768 px; items filtered by role; `aria-current="page"` on the active item |
| Role chip | `ion-chip` | ADMIN navy, ANALYST blue, VIEWER grey |
| Card | `ion-card` | Groups related content |

## 5. Accessibility checklist applied (WCAG 2.1 AA)

- [x] Contrast ≥ 4.5:1 for every text colour pair (1.4.3) and ≥ 3:1 for the focus ring (1.4.11).
- [x] "Skip to main content" link, visible with the first Tab (2.4.1).
- [x] Visible keyboard focus on every interactive element (2.4.7).
- [x] `<html lang>` updated when the language changes (3.1.1).
- [x] Labels on every field, `autocomplete` on login fields (1.3.5, 3.3.2).
- [x] Errors described in text, next to the field (3.3.1) and announced (`role="alert"`).
- [x] Decorative icons hidden from screen readers (`aria-hidden="true"`).
- [x] Navigation landmark (`<nav aria-label>`) and `<main>` on the login page.
- [ ] Pending (Phase 2): tables with `<th scope>`, chart text alternatives, Lighthouse/axe audit report.
