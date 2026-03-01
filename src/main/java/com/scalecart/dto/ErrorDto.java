package com.scalecart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDto(
    String code,
    String message,
    Instant timestamp,
    List<FieldErrorDto> details
) {
    public static ErrorDto of(String code, String message) {
        return new ErrorDto(code, message, Instant.now(), null);
    }

    public static ErrorDto of(String code, String message, List<FieldErrorDto> details) {
        return new ErrorDto(code, message, Instant.now(), details);
    }

    public record FieldErrorDto(String field, String message) {}
}
