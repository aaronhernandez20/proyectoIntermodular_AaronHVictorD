package com.vulnfollowup.bootstrap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vulnfollowup.dto.AssetDTOs.AssetRequest;
import com.vulnfollowup.importer.ImportValidationException;
import com.vulnfollowup.importer.VulnerabilityImportRecord;
import com.vulnfollowup.importer.VulnerabilityImporter;
import com.vulnfollowup.repository.AssetRepository;
import com.vulnfollowup.repository.VulnerabilityRepository;
import com.vulnfollowup.service.AssetService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// CU1 - Preparar el entorno.
// Al arrancar: Flyway ya ha creado/comprobado la BD y los usuarios de prueba (V1, V2).
// Aquí se cargan los ficheros iniciales de data/ SOLO si la tabla está vacía, así
// reiniciar el backend no duplica nada.
//
// Todo va en UNA transacción: si un solo activo del fichero es inválido no se guarda
// ninguno (rollback) y el log dice exactamente qué registro falla y por qué.
@Component
public class InitialDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialDataLoader.class);
    private static final String SYSTEM_USER = "system";

    private final AssetRepository assetRepository;
    private final AssetService assetService;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final Path assetsFile;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final VulnerabilityImporter vulnerabilityImporter;
    private final Path vulnerabilitiesFile;
    // Transacción programática: @Transactional no funciona en métodos llamados desde la
    // propia clase (la llamada no pasa por el proxy de Spring), así que la abrimos a mano.
    private final TransactionTemplate transaction;

    public InitialDataLoader(AssetRepository assetRepository, AssetService assetService,
                             ObjectMapper objectMapper, Validator validator,
                             @Value("${vulnfollowup.data.assets-file}") String assetsFile,
                             VulnerabilityRepository vulnerabilityRepository,
                             VulnerabilityImporter vulnerabilityImporter,
                             @Value("${vulnfollowup.data.vulnerabilities-file}") String vulnerabilitiesFile,
                             PlatformTransactionManager transactionManager) {
        this.transaction = new TransactionTemplate(transactionManager);
        this.assetRepository = assetRepository;
        this.assetService = assetService;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.assetsFile = Path.of(assetsFile);
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.vulnerabilityImporter = vulnerabilityImporter;
        this.vulnerabilitiesFile = Path.of(vulnerabilitiesFile);
    }

    // Orden de CU1: primero los activos y después las vulnerabilidades, que los referencian.
    // Cada carga va en su propia transacción.
    @Override
    public void run(ApplicationArguments args) throws IOException {
        loadAssets();
        loadVulnerabilities();
    }

    // Carga data/vulnerabilities_ok.json (>= 20 registros) con el importador transaccional.
    // Si el fichero tiene errores, no se guarda ninguno y el backend sigue arrancando.
    void loadVulnerabilities() throws IOException {
        if (vulnerabilityRepository.count() > 0) {
            log.info("CU1: ya hay vulnerabilidades en la BD, no se carga {}", vulnerabilitiesFile);
            return;
        }
        if (!Files.exists(vulnerabilitiesFile)) {
            log.warn("CU1: no se encuentra el fichero de vulnerabilidades {} (configurable con VULNERABILITIES_FILE)",
                    vulnerabilitiesFile.toAbsolutePath());
            return;
        }
        List<VulnerabilityImportRecord> records =
                objectMapper.readValue(vulnerabilitiesFile.toFile(), new TypeReference<>() {
                });
        try {
            int n = vulnerabilityImporter.importAll(records, SYSTEM_USER);
            log.info("CU1: cargadas {} vulnerabilidades desde {}", n, vulnerabilitiesFile.getFileName());
        } catch (ImportValidationException e) {
            log.error("CU1: {} -> {}", vulnerabilitiesFile.getFileName(), e.getMessage());
        }
    }

    void loadAssets() throws IOException {
        if (assetRepository.count() > 0) {
            log.info("CU1: ya hay activos en la BD, no se carga {}", assetsFile);
            return;
        }
        if (!Files.exists(assetsFile)) {
            log.warn("CU1: no se encuentra el fichero de activos {} (configurable con ASSETS_FILE)",
                    assetsFile.toAbsolutePath());
            return;
        }

        List<AssetRequest> assets = objectMapper.readValue(assetsFile.toFile(), new TypeReference<>() {
        });

        // 1) Validar TODO antes de guardar nada
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < assets.size(); i++) {
            Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assets.get(i));
            for (ConstraintViolation<AssetRequest> v : violations) {
                errors.add("registro " + (i + 1) + ": " + v.getPropertyPath() + " " + v.getMessage());
            }
        }
        if (!errors.isEmpty()) {
            errors.forEach(e -> log.error("CU1: {} -> {}", assetsFile.getFileName(), e));
            // El backend sigue arrancando (se pueden crear activos desde la API),
            // pero no se guarda ni un registro del fichero erróneo.
            log.error("CU1: {} tiene {} error(es); no se ha cargado ningún activo",
                    assetsFile.getFileName(), errors.size());
            return;
        }

        // 2) Guardar todos en UNA transacción: si algo falla aquí, se deshace todo (rollback)
        transaction.executeWithoutResult(status -> assets.forEach(a -> assetService.create(a, SYSTEM_USER)));
        log.info("CU1: cargados {} activos desde {}", assets.size(), assetsFile.getFileName());
    }
}
