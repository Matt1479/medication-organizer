package com.matt1479.medicationorganizer.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeDisplayNameRequest(
    @NotBlank
    @Size(min = 8, max = 32)
    String currentPassword,

    @Size(max = 32)
    String newDisplayName
) {}
