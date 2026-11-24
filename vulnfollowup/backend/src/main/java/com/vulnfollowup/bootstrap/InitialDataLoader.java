package com.vulnfollowup.bootstrap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vulnfollowup.dto.AssetDTOs.AssetRequest;
import com.vulnfollowup.repository.AssetRepository;
import com.vulnfollowup.service.AssetService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    public InitialDataLoader(AssetRepository assetRepository, AssetService assetService,
                             ObjectMapper objectMapper, Validator validator,
                             @Value("${vulnfollowup.data.assets-file}") String assetsFile) {
        this.assetRepository = assetRepository;
        this.assetService = assetService;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.assetsFile = Path.of(assetsFile);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
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

        // 2) Guardar (si algo falla aquí, @Transactional deshace todo)
        assets.forEach(a -> assetService.create(a, SYSTEM_USER));
        log.info("CU1: cargados {} activos desde {}", assets.size(), assetsFile.getFileName());
    }
}
