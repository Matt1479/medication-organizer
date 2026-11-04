package com.matt1479.medicationorganizer.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeEmailRequest(
    @NotBlank
    @Size(min = 8, max = 32)
    String currentPassword,

    @NotBlank
    @Email
    @Size(min = 3, max = 254)
    String newEmail
) {}
