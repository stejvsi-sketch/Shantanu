package com.example.flight;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import java.util.stream.Collectors;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<String> errors = exception.getConstraintViolations().stream()
                .map(this::formatViolation)
                .collect(Collectors.toList());
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(errors)
                .build();
    }

    private String formatViolation(ConstraintViolation<?> v) {
        String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "";
        return (path.isEmpty() ? "" : path + ": ") + v.getMessage();
    }
}