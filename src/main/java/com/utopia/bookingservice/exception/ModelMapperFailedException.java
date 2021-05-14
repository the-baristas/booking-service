package com.utopia.bookingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ModelMapperFailedException extends ResponseStatusException {
    private static final long serialVersionUID = 1L;

    public ModelMapperFailedException(Throwable cause) {
        super(HttpStatus.FAILED_DEPENDENCY, "ModelMapper dependency failed.",
                cause);
    }
}
