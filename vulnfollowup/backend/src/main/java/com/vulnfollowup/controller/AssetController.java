package com.vulnfollowup.controller;

import com.vulnfollowup.dto.AssetDTOs.AssetRequest;
import com.vulnfollowup.dto.AssetDTOs.AssetResponse;
import com.vulnfollowup.dto.AssetDTOs.AssetUpdateRequest;
import com.vulnfollowup.entity.AssetEnums.AssetStatus;
import com.vulnfollowup.entity.AssetEnums.AssetType;
import com.vulnfollowup.entity.AssetEnums.Environment;
import com.vulnfollowup.security.CurrentUser;
import com.vulnfollowup.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Capa de presentación de activos: recibe la petición, saca el usuario autenticado
// y delega en el servicio. Cuando llega aquí ya ha pasado los filtros de seguridad.
@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    // Cualquier rol puede consultar (VIEWER incluido)
    @GetMapping
    public List<AssetResponse> list(@RequestParam(required = false) AssetType type,
                                    @RequestParam(required = false) Environment environment,
                                    @RequestParam(required = false) AssetStatus status,
                                    @RequestParam(required = false) String owner) {
        return assetService.search(type, environment, status, owner, CurrentUser.username());
    }

    @GetMapping("/{id}")
    public AssetResponse get(@PathVariable Long id) {
        return assetService.findById(id);
    }

    // CU2 - Registrar un activo: ADMIN o ANALYST. Un VIEWER recibe 403.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public AssetResponse create(@Valid @RequestBody AssetRequest request) {
        return assetService.create(request, CurrentUser.username());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public AssetResponse update(@PathVariable Long id, @Valid @RequestBody AssetUpdateRequest request) {
        return assetService.update(id, request, CurrentUser.username());
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public AssetResponse deactivate(@PathVariable Long id) {
        return assetService.deactivate(id, CurrentUser.username());
    }
}
