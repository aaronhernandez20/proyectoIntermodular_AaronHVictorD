package com.vulnfollowup.service;

import com.vulnfollowup.dto.AssetDTOs.AssetRequest;
import com.vulnfollowup.dto.AssetDTOs.AssetResponse;
import com.vulnfollowup.dto.AssetDTOs.AssetUpdateRequest;
import com.vulnfollowup.entity.Asset;
import com.vulnfollowup.entity.AssetEnums.AssetStatus;
import com.vulnfollowup.entity.AssetEnums.AssetType;
import com.vulnfollowup.entity.AssetEnums.Environment;
import com.vulnfollowup.entity.IctProvider;
import com.vulnfollowup.mapper.AssetMapper;
import com.vulnfollowup.repository.AssetRepository;
import com.vulnfollowup.repository.IctProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

import static com.vulnfollowup.repository.AssetRepository.hasEnvironment;
import static com.vulnfollowup.repository.AssetRepository.hasStatus;
import static com.vulnfollowup.repository.AssetRepository.hasType;
import static com.vulnfollowup.repository.AssetRepository.ownerContains;

// Capa de negocio de activos (CU2). Por defecto las transacciones son de solo lectura;
// los métodos que escriben llevan su propio @Transactional.
// El controlador nunca toca repositorios: arquitectura controller -> service -> repository.
@Service
@Transactional(readOnly = true)
public class AssetService {

    private static final Logger log = LoggerFactory.getLogger(AssetService.class);
    private static final String ENTITY = "ASSET";

    private final AssetRepository assetRepository;
    private final IctProviderRepository providerRepository;
    private final AssetMapper mapper;
    private final AuditService auditService;

    public AssetService(AssetRepository assetRepository, IctProviderRepository providerRepository,
                        AssetMapper mapper, AuditService auditService) {
        this.assetRepository = assetRepository;
        this.providerRepository = providerRepository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    // GET /api/assets?type=&environment=&status=&owner=  (todos los filtros son opcionales)
    public List<AssetResponse> search(AssetType type, Environment environment, AssetStatus status,
                                      String owner, String username) {
        Specification<Asset> spec = Specification.where(hasType(type))
                .and(hasEnvironment(environment))
                .and(hasStatus(status))
                .and(ownerContains(owner));
        List<AssetResponse> result = assetRepository.findAll(spec, Sort.by("name"))
                .stream().map(mapper::toDto).toList();
        log.info("Usuario '{}' consulta activos: {} resultados", username, result.size());
        return result;
    }

    public AssetResponse findById(Long id) {
        return mapper.toDto(getAsset(id));
    }

    // Si dos peticiones llegan a la vez, las dos pueden pasar el exists...; lo que de
    // verdad impide el duplicado es el UNIQUE de la columna name (-> 409 en el handler).
    @Transactional
    public AssetResponse create(AssetRequest dto, String username) {
        if (assetRepository.existsByNameIgnoreCase(dto.name().trim())) {
            throw new IllegalStateException("Ya existe un activo con nombre '" + dto.name() + "'");
        }
        Asset asset = mapper.toEntity(dto);
        asset.setProvider(resolveProvider(dto.providerName()));
        Asset saved = assetRepository.save(asset);
        auditService.record(ENTITY, saved.getId(), "CREATE", username, "name=" + saved.getName());
        log.info("Usuario '{}' crea el activo '{}' (id {})", username, saved.getName(), saved.getId());
        return mapper.toDto(saved);
    }

    // PUT con bloqueo optimista: el cliente envía la versión que leyó en el GET
    @Transactional
    public AssetResponse update(Long id, AssetUpdateRequest dto, String username) {
        Asset asset = getAsset(id);
        if (!asset.getVersion().equals(dto.version())) {
            log.warn("Conflicto de versión: usuario '{}' envía versión {} del activo {} (actual {})",
                    username, dto.version(), id, asset.getVersion());
            throw new ObjectOptimisticLockingFailureException(Asset.class, id);
        }
        if (assetRepository.existsByNameIgnoreCaseAndIdNot(dto.name().trim(), id)) {
            throw new IllegalStateException("Ya existe otro activo con nombre '" + dto.name() + "'");
        }
        asset.update(dto.name().trim(), dto.type(), dto.owner().trim(), dto.environment(),
                dto.exposure(), dto.status());
        // saveAndFlush: el UPDATE ... WHERE version = ? se lanza ya, dentro de la transacción
        Asset saved = assetRepository.saveAndFlush(asset);
        auditService.record(ENTITY, id, "UPDATE", username, "status=" + saved.getStatus());
        log.info("Usuario '{}' actualiza el activo {} (versión {})", username, id, saved.getVersion());
        return mapper.toDto(saved);
    }

    // Desactivar en vez de borrar: el activo puede tener vulnerabilidades e historial
    // de auditoría que hay que conservar como evidencia.
    @Transactional
    public AssetResponse deactivate(Long id, String username) {
        Asset asset = getAsset(id);
        if (asset.getStatus() == AssetStatus.INACTIVE) {
            return mapper.toDto(asset);   // idempotente: desactivar dos veces no es un error
        }
        asset.deactivate();
        Asset saved = assetRepository.saveAndFlush(asset);
        auditService.record(ENTITY, id, "DEACTIVATE", username, null);
        log.info("Usuario '{}' desactiva el activo {}", username, id);
        return mapper.toDto(saved);
    }

    private Asset getAsset(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el activo con id " + id));
    }

    private IctProvider resolveProvider(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            return null;
        }
        return providerRepository.findByNameIgnoreCase(providerName.trim())
                .orElseThrow(() -> new IllegalArgumentException("No existe el proveedor TIC '" + providerName + "'"));
    }
}
