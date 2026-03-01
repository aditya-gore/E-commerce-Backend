package com.scalecart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateDto(
    @NotBlank(message = "Name is required")
    @Size(max = 255)
    String name
) {}
