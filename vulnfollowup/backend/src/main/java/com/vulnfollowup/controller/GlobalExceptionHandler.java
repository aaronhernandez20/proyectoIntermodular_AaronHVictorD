package com.vulnfollowup.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

// Convierte las excepciones de todos los controladores en respuestas HTTP con un
// formato JSON común, para que la web y el Android muestren mensajes comprensibles:
//   { "timestamp": "...", "status": 409, "error": "Conflict", "message": "...", "fields": {...} }
// Ojo: el 401 por token ausente/inválido lo da la cadena de filtros, no esta clase.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // @Valid ha fallado -> 400 con el detalle campo -> mensaje
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fields.put(e.getField(), e.getDefaultMessage()));
        Map<String, Object> body = body(HttpStatus.BAD_REQUEST, "Datos de entrada no válidos");
        body.put("fields", fields);
        return body;
    }

    // JSON mal formado o un enum con un valor que no existe (p. ej. "environment": "PROD")
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> unreadable(Exception ex) {
        return body(HttpStatus.BAD_REQUEST, "Petición mal formada o con valores no permitidos");
    }

    // Reglas de negocio incumplidas (p. ej. proveedor inexistente, CVSS incoherente) -> 400
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> badRequest(IllegalArgumentException ex) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Usuario o contraseña incorrectos en el login -> 401 (mensaje genérico a propósito:
    // no decimos si falló el usuario o la contraseña, para no dar pistas a un atacante)
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> unauthorized(AuthenticationException ex) {
        return body(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
    }

    // Autenticado pero sin el rol necesario (@PreAuthorize) -> 403
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> forbidden(AccessDeniedException ex) {
        return body(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación");
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> notFound(NoSuchElementException ex) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Duplicados detectados en el servicio -> 409
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> conflict(IllegalStateException ex) {
        return body(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Bloqueo optimista: otro usuario modificó el registro antes (@Version) -> 409
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> optimisticLock(ObjectOptimisticLockingFailureException ex) {
        return body(HttpStatus.CONFLICT,
                "El registro ha sido modificado por otro usuario. Recargue e inténtelo de nuevo.");
    }

    // Restricción de la BD incumplida (UNIQUE, CHECK de CVSS...) -> 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> integrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        return body(HttpStatus.CONFLICT, "Los datos incumplen una restricción de la base de datos");
    }

    private static Map<String, Object> body(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
