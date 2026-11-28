package com.vulnfollowup.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

// Los 401 y 403 que da la CADENA DE FILTROS (antes de llegar al controlador) no pasan por
// GlobalExceptionHandler, y por defecto Spring los devuelve sin cuerpo. Esta clase hace que
// salgan con el mismo JSON que el resto de errores, para que la web y el Android muestren
// siempre un mensaje comprensible. (Fallo detectado en las pruebas del 26/11.)
@Component
public class JsonSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonSecurityErrorHandler.class);
    private final ObjectMapper objectMapper;

    public JsonSecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 401: sin token, token caducado o firma no válida
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        write(response, HttpStatus.UNAUTHORIZED, "Autenticación requerida o token no válido");
    }

    // 403: token válido pero el rol no tiene permiso
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        // Log de no repudio: quién intentó qué (nunca el token)
        log.warn("Acceso denegado: usuario '{}' -> {} {}", request.getUserPrincipal() != null
                ? request.getUserPrincipal().getName() : "anónimo", request.getMethod(), request.getRequestURI());
        write(response, HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación");
    }

    private void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
