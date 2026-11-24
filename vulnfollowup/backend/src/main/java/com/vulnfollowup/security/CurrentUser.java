package com.vulnfollowup.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// El usuario sale SIEMPRE del contexto de seguridad (rellenado a partir del JWT firmado),
// nunca de un parámetro de la URL o del JSON, que el cliente podría manipular.
public final class CurrentUser {

    private CurrentUser() {
    }

    public static String username() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
