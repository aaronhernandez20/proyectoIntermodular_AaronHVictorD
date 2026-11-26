# Evidencias – Víctor de Lucas Fernández

Capturas de las pruebas de la parte de Víctor (backend, Android, Docker), para la memoria y la defensa.

| Nº | Archivo | Qué demuestra | Fase |
|---|---|---|---|
| 01 | `01-tests-junit-22-ok.png` | `mvn test` / IntelliJ: 22 tests JUnit en verde (CvssValidatorTest, VulnStatusTest, JwtServiceTest), incluidos los 2 casos inválidos del enunciado | Fase 2 |

## Capturas pendientes (siguiente paso: Docker + Swagger/Postman)
- `02-health-up.png` – `GET /actuator/health` devuelve `UP`
- `03-login-analyst-200.png` – login correcto y token JWT
- `04-viewer-post-activo-403.png` – un VIEWER no puede crear activos
- `05-cvss-40-con-vector-31-400.png` – el backend rechaza versión 4.0 con vector 3.1
- `06-cvss-baja-con-98-400.png` – el backend rechaza severidad incoherente
- `07-cambio-estado-sin-evidencia-400.png` – no se puede resolver sin evidencia
- `08-edicion-concurrente-409.png` – bloqueo optimista

Nombre de los archivos: `NN-que-se-prueba-resultado.png`.
