package com.vulnfollowup.cvss;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Reglas CVSS Base del proyecto (apartado 3 del enunciado).
// No calculamos la puntuación a partir del vector (para eso está la calculadora oficial
// de FIRST); comprobamos que versión, vector, puntuación y severidad son COHERENTES.
//
// Es Java puro, sin Spring: así se prueba con un test unitario rapidísimo y lo reutilizan
// el alta de vulnerabilidades, las nuevas valoraciones y el importador JSON.
// La web de Aarón aplica las mismas reglas (cvss.util.ts) solo por comodidad del usuario;
// la validación que cuenta es esta, la del backend.
public final class CvssValidator {

    public static final String V3_1 = "3.1";
    public static final String V4_0 = "4.0";

    // Vector Base v3.1: las 8 métricas base en su orden oficial
    private static final Pattern VECTOR_31 = Pattern.compile(
            "^CVSS:3\\.1/AV:[NALP]/AC:[LH]/PR:[NLH]/UI:[NR]/S:[UC]/C:[NLH]/I:[NLH]/A:[NLH]$");

    // Vector Base v4.0: las 11 métricas base en su orden oficial
    private static final Pattern VECTOR_40 = Pattern.compile(
            "^CVSS:4\\.0/AV:[NALP]/AC:[LH]/AT:[NP]/PR:[NLH]/UI:[NPA]/VC:[HLN]/VI:[HLN]/VA:[HLN]/SC:[HLN]/SI:[HLN]/SA:[HLN]$");

    private static final BigDecimal TEN = BigDecimal.TEN;

    private CvssValidator() {
    }

    // Escala cualitativa (tabla 2 del enunciado)
    public static Severity severityFromScore(BigDecimal score) {
        if (score.signum() == 0) return Severity.NONE;
        if (score.compareTo(new BigDecimal("4.0")) < 0) return Severity.LOW;
        if (score.compareTo(new BigDecimal("7.0")) < 0) return Severity.MEDIUM;
        if (score.compareTo(new BigDecimal("9.0")) < 0) return Severity.HIGH;
        return Severity.CRITICAL;
    }

    // Devuelve la lista de errores en lenguaje claro. Lista vacía = datos coherentes.
    // declaredSeverity es opcional: si llega (p. ej. en el fichero JSON), debe cuadrar con la puntuación.
    public static List<String> validate(String version, String vector, BigDecimal score, Severity declaredSeverity) {
        List<String> errors = new ArrayList<>();
        String v = vector == null ? "" : vector.trim();

        if (version == null || version.isBlank()) {
            errors.add("La versión CVSS es obligatoria");
        } else if (!V3_1.equals(version) && !V4_0.equals(version)) {
            errors.add("Versión CVSS no soportada: " + version + " (solo 3.1 o 4.0)");
        }
        if (v.isEmpty()) {
            errors.add("El vector CVSS es obligatorio");
        }

        if (errors.isEmpty()) {
            String declaredByVector = versionFromVector(v);
            if (declaredByVector != null && !declaredByVector.equals(version)) {
                // Caso del enunciado: versión 4.0 con vector "CVSS:3.1/..."
                errors.add("El vector es de la versión " + declaredByVector + " pero se ha indicado la versión " + version);
            } else if (!(V3_1.equals(version) ? VECTOR_31 : VECTOR_40).matcher(v).matches()) {
                errors.add("El vector no tiene el formato Base de CVSS " + version
                        + (V3_1.equals(version) ? " (AV/AC/PR/UI/S/C/I/A)" : " (AV/AC/AT/PR/UI/VC/VI/VA/SC/SI/SA)"));
            }
        }

        if (score == null) {
            errors.add("La puntuación CVSS es obligatoria");
        } else if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(TEN) > 0) {
            errors.add("La puntuación debe estar entre 0.0 y 10.0");
        } else if (score.stripTrailingZeros().scale() > 1) {
            errors.add("La puntuación solo puede tener un decimal");
        } else if (declaredSeverity != null && declaredSeverity != severityFromScore(score)) {
            // Caso del enunciado: severidad "Baja" con 9,8
            errors.add("La severidad " + declaredSeverity + " no corresponde a la puntuación " + score
                    + " (debería ser " + severityFromScore(score) + ")");
        }
        return errors;
    }

    private static String versionFromVector(String vector) {
        if (vector.startsWith("CVSS:3.1/")) return V3_1;
        if (vector.startsWith("CVSS:4.0/")) return V4_0;
        return null;
    }
}
