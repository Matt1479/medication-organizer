package com.matt1479.medicationorganizer.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank
    @Email
    @Size(min = 3, max = 254)
    String email,

    @Size(max = 32)
    String displayName,

    @NotBlank
    @Size(min = 8, max = 32)
    String password
) {}
