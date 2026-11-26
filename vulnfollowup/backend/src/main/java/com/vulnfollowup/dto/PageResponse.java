package com.vulnfollowup.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

// Respuesta paginada con un formato estable y sencillo para la web y el Android
// (no devolvemos el Page de Spring tal cual porque su JSON es enorme y puede cambiar).
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
