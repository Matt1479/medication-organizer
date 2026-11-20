package com.matt1479.medicationorganizer.organizers.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateUserOrganizerRequest(
    @NotNull
    @Min(1)
    Long organizerId,

    String displayName
) {}
