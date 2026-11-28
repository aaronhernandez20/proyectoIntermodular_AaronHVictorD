# Evidencias – Víctor de Lucas Fernández

Capturas de las pruebas de la parte de Víctor (backend, Android, Docker), para la memoria y la defensa.

| Nº | Archivo | Qué demuestra | Fase |
|---|---|---|---|
| 01 | `01-tests-junit-22-ok.png` | `mvn test` / IntelliJ: 22 tests JUnit en verde (CvssValidatorTest, VulnStatusTest, JwtServiceTest), incluidos los 2 casos inválidos del enunciado | Fase 2 |
| 02 | `02-docker-backend-arrancado-cu1.png` | `docker compose up`: PostgreSQL + backend arrancados, Flyway crea la BD y CU1 carga los 10 activos de `data/assets.json` | Fase 2 |
| 03 | `03-health-up.png` | `GET /actuator/health` devuelve `{"status":"UP"}`: el servicio está levantado (primer paso de la demo) | Fase 2 |
| 04 | `04-login-analyst-200.png` | `POST /api/auth/login` como analyst: 200 con token JWT, caducidad y rol ANALYST | Fase 2 |
| 06 | `06-cvss-40-con-vector-31-400.png` | Ejemplo del enunciado: versión 4.0 con vector `CVSS:3.1/...` → 400 "Valoración CVSS incoherente" con el error explicado. En las cabeceras se ve además `content-encoding: gzip` (mejora de Sostenibilidad) | Fase 2 |
| 10 | `10-tests-junit-26-ok.png` | 26 tests JUnit en verde tras la Fase 3: se suman los 4 de `VulnerabilityImportValidatorTest`, que usan los ficheros reales de `data/` (fichero correcto sin errores, fichero con los 6 errores detectados) | Fase 3 |
| 11 | `11-cu1-carga-10-activos-23-vulnerabilidades.png` | CU1 con la BD limpia (`docker compose down -v` + `up --build`): se crean los 10 activos y el importador transaccional carga las 23 vulnerabilidades de `data/vulnerabilities_ok.json` | Fase 3 |
| 12 | `12-sin-token-401-json.png` | Petición sin token a `/api/vulnerabilities`: 401 con el JSON de error común (antes salía sin cuerpo; arreglado con `JsonSecurityErrorHandler`) | Fase 3 |
| 13 | `13-dashboard-viewer-23-vulnerabilidades.png` | `GET /api/dashboard/summary` como VIEWER (CU5): 23 en total, 9 críticas, 4 altas, 8 medias, 2 bajas; 17 pendientes y 1 vencida (ProxyLogon, fecha objetivo 25/11). Respuesta comprimida con gzip | Fase 3 |

## Capturas pendientes (siguiente paso: Docker + Swagger/Postman)
- `05-viewer-post-activo-403.png` – un VIEWER no puede crear activos
- `07-cvss-vector-mal-formado-400.png` – el backend rechaza un vector con formato incorrecto
- `08-cambio-estado-sin-evidencia-400.png` – no se puede resolver sin evidencia
- `09-edicion-concurrente-409.png` – bloqueo optimista

Nombre de los archivos: `NN-que-se-prueba-resultado.png`.
