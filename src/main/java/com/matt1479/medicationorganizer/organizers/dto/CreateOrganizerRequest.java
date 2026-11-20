package com.matt1479.medicationorganizer.organizers.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateOrganizerRequest(
    @NotBlank
    String productCode,

    @NotBlank
    String name,

    @Min(1)
    int daysCovered,

    @Min(1)
    int compartmentsPerDay,

    String description,

    String qrCode,

    @NotEmpty
    List<@NotNull @Valid CompartmentRequest> compartments
) {}
