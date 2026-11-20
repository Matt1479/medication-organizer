package com.matt1479.medicationorganizer.organizers.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateValueRequest(
    @NotBlank String value
) {}
