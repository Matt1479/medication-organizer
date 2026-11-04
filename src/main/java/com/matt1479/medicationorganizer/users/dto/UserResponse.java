package com.matt1479.medicationorganizer.users.dto;

import java.time.Instant;
import java.util.Set;

import com.matt1479.medicationorganizer.users.Role;

public record UserResponse(
        Long id,
        String email,
        String displayName,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt,
        Set<Role> roles) {
}
