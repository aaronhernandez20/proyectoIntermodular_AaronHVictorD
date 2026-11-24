package com.vulnfollowup.mapper;

import com.vulnfollowup.dto.AssetDTOs.AssetRequest;
import com.vulnfollowup.dto.AssetDTOs.AssetResponse;
import com.vulnfollowup.entity.Asset;
import com.vulnfollowup.entity.IctProvider;
import org.springframework.stereotype.Component;

// Conversión entidad <-> DTO en un único sitio (patrón DTO de Acceso a Datos U2)
@Component
public class AssetMapper {

    public AssetResponse toDto(Asset asset) {
        IctProvider provider = asset.getProvider();
        return new AssetResponse(
                asset.getId(),
                asset.getName(),
                asset.getType(),
                asset.getOwner(),
                asset.getEnvironment(),
                asset.getStatus(),
                asset.getExposure(),
                provider != null ? provider.getId() : null,
                provider != null ? provider.getName() : null,
                asset.getVersion(),
                asset.getCreatedAt(),
                asset.getUpdatedAt());
    }

    public Asset toEntity(AssetRequest dto) {
        return new Asset(dto.name().trim(), dto.type(), dto.owner().trim(),
                dto.environment(), dto.exposure());
    }
}
