package com.matt1479.medicationorganizer.users.dto;

import com.matt1479.medicationorganizer.users.Role;

import jakarta.validation.constraints.NotNull;

public record RoleRequest(
    @NotNull 
    Role role
) {}
