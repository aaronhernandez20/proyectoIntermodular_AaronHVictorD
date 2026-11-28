package com.vulnfollowup.importer;

import java.util.List;

// El fichero tiene errores: no se ha guardado NINGÚN registro (todo o nada).
public class ImportValidationException extends RuntimeException {

    private final transient List<String> errors;

    public ImportValidationException(List<String> errors) {
        super("El fichero tiene " + errors.size() + " error(es); no se ha importado ningún registro");
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
