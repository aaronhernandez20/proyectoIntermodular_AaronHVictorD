# Datos iniciales (CU1)

El backend carga estos ficheros al arrancar, **solo si la tabla correspondiente está vacía**, así que reiniciar no duplica nada.

| Fichero | Contenido | Uso |
|---|---|---|
| `assets.json` | 10 activos de prueba (API de pagos, portal web, base de datos…) | Carga inicial de activos |
| `vulnerabilities_ok.json` | **23 vulnerabilidades** (11 CVE reales + 12 sintéticas) | Carga inicial de vulnerabilidades. Requisito: al menos 20 |
| `vulnerabilities_with_errors.json` | 3 registros correctos + 6 con errores típicos | Demo de la importación con rollback: no se guarda ninguno |

## Origen de los datos

- **CVE reales (11):** CVE-2021-44228 (Log4Shell), CVE-2014-0160 (Heartbleed), CVE-2017-5638 (Apache Struts), CVE-2019-0708 (BlueKeep), CVE-2021-26855 (ProxyLogon), CVE-2022-22965 (Spring4Shell), CVE-2023-34362 (MOVEit), CVE-2020-1472 (Zerologon), CVE-2021-34527 (PrintNightmare), CVE-2024-3094 (xz) y CVE-2023-44487 (HTTP/2 Rapid Reset). El vector y la puntuación CVSS 3.1 se comprobaron en la API pública de NVD (NIST) el 28/11/2026. Las descripciones son resúmenes propios en español.
- **Sintéticas (VFU-0001 a VFU-0012):** casos inventados y verosímiles de una empresa financiera. La puntuación se calculó a partir del vector con la fórmula oficial de CVSS 3.1 Base, así que vector, puntuación y severidad son coherentes.
- **Estados:** hay vulnerabilidades en los 4 estados del ciclo (OPEN, PENDING_REMEDIATION, RESOLVED y CLOSED). Las pendientes tienen responsable y las resueltas o cerradas tienen evidencia, igual que exige la API.
- Todos los datos son **públicos o sintéticos**: no hay datos personales ni de clientes reales.

## Errores del fichero `vulnerabilities_with_errors.json`

| Registro | Error |
|---|---|
| VFU-0901 | Versión 4.0 con vector `CVSS:3.1/...` (ejemplo del enunciado) |
| VFU-0902 | Severidad LOW con puntuación 9.8 (ejemplo del enunciado) |
| VFU-0903 | Vector sin la métrica S (Scope) |
| VFU-0904 | Activo que no existe |
| VFU-0905 | Puntuación 11.5 (fuera de 0–10) |
| CVE-2021-44228 | Código duplicado dentro del mismo fichero |

El test `VulnerabilityImportValidatorTest` comprueba con estos mismos ficheros que el correcto no tiene errores y que el erróneo detecta los 6.
