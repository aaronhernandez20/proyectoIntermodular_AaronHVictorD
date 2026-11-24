package com.vulnfollowup.repository;

import com.vulnfollowup.entity.Asset;
import com.vulnfollowup.entity.AssetEnums.AssetStatus;
import com.vulnfollowup.entity.AssetEnums.AssetType;
import com.vulnfollowup.entity.AssetEnums.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

// JpaSpecificationExecutor permite combinar filtros opcionales en una sola consulta
// (si el filtro no llega, simplemente no se añade al WHERE). Es la misma técnica
// que usaremos para los filtros combinados de vulnerabilidades.
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    // EntityGraph: trae el proveedor en la misma consulta (JOIN) y evita el
    // problema N+1 (una consulta extra por cada activo del listado).
    @Override
    @EntityGraph(attributePaths = "provider")
    List<Asset> findAll(Specification<Asset> spec, Sort sort);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // ---- Filtros reutilizables. Devuelven null si no hay valor: Spring lo ignora. ----

    static Specification<Asset> hasType(AssetType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    static Specification<Asset> hasEnvironment(Environment environment) {
        return (root, query, cb) -> environment == null ? null : cb.equal(root.get("environment"), environment);
    }

    static Specification<Asset> hasStatus(AssetStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    // Búsqueda parcial y sin distinguir mayúsculas: "segur" encuentra "Equipo Seguridad"
    static Specification<Asset> ownerContains(String owner) {
        return (root, query, cb) -> owner == null || owner.isBlank() ? null
                : cb.like(cb.lower(root.get("owner")), "%" + owner.toLowerCase() + "%");
    }
}
