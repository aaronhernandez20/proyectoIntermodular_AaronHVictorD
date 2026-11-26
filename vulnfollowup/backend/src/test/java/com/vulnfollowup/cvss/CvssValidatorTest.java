package com.vulnfollowup.cvss;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Pruebas de las reglas CVSS del enunciado (apartado 3). Son tests unitarios puros:
// se ejecutan en milisegundos con "mvn test" y sin base de datos.
class CvssValidatorTest {

    private static final String LOG4SHELL_31 = "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:C/C:H/I:H/A:H";
    private static final String EJEMPLO_31 = "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H";
    private static final String EJEMPLO_40 = "CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:H/VI:H/VA:H/SC:N/SI:N/SA:N";

    @Test
    void ejemploValidoDelEnunciado() {
        // Vector 3.1 + 9,8 + Crítica (ejemplo del apartado 3)
        assertThat(CvssValidator.validate("3.1", EJEMPLO_31, new BigDecimal("9.8"), Severity.CRITICAL)).isEmpty();
        assertThat(CvssValidator.validate("3.1", LOG4SHELL_31, new BigDecimal("10.0"), null)).isEmpty();
        assertThat(CvssValidator.validate("4.0", EJEMPLO_40, new BigDecimal("9.3"), null)).isEmpty();
    }

    @Test
    void rechazaVersion40ConVector31() {
        // Primer caso que el enunciado exige rechazar
        List<String> errors = CvssValidator.validate("4.0", EJEMPLO_31, new BigDecimal("9.8"), null);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("versión 3.1").contains("4.0");
    }

    @Test
    void rechazaSeveridadBajaCon98() {
        // Segundo caso que el enunciado exige rechazar
        List<String> errors = CvssValidator.validate("3.1", EJEMPLO_31, new BigDecimal("9.8"), Severity.LOW);
        assertThat(errors).singleElement().asString().contains("CRITICAL");
    }

    @Test
    void rechazaVectorMalFormado() {
        // Falta la métrica S (Scope), obligatoria en 3.1
        assertThat(CvssValidator.validate("3.1", "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/C:H/I:H/A:H",
                new BigDecimal("9.8"), null)).singleElement().asString().contains("formato");
        // Valor inventado en AV
        assertThat(CvssValidator.validate("3.1", "CVSS:3.1/AV:X/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H",
                new BigDecimal("9.8"), null)).isNotEmpty();
    }

    @Test
    void rechazaVersionNoSoportadaYCamposVacios() {
        assertThat(CvssValidator.validate("2.0", EJEMPLO_31, new BigDecimal("9.8"), null)).isNotEmpty();
        assertThat(CvssValidator.validate(null, null, null, null)).hasSize(3);
    }

    @Test
    void rechazaPuntuacionFueraDeRangoOConDosDecimales() {
        assertThat(CvssValidator.validate("3.1", EJEMPLO_31, new BigDecimal("10.5"), null)).isNotEmpty();
        assertThat(CvssValidator.validate("3.1", EJEMPLO_31, new BigDecimal("-1"), null)).isNotEmpty();
        assertThat(CvssValidator.validate("3.1", EJEMPLO_31, new BigDecimal("9.85"), null)).isNotEmpty();
    }

    // Límites exactos de la tabla 2 del enunciado
    @ParameterizedTest
    @CsvSource({
            "0.0, NONE", "0.1, LOW", "3.9, LOW", "4.0, MEDIUM", "6.9, MEDIUM",
            "7.0, HIGH", "8.9, HIGH", "9.0, CRITICAL", "10.0, CRITICAL"})
    void severidadSegunPuntuacion(String score, Severity expected) {
        assertThat(CvssValidator.severityFromScore(new BigDecimal(score))).isEqualTo(expected);
    }
}
