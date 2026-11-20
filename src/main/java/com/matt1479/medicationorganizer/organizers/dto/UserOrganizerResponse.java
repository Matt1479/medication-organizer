package com.matt1479.medicationorganizer.organizers.dto;

import java.time.Instant;

public record UserOrganizerResponse(
        Long id,
        Long userId,
        Long organizerId,
        String displayName,
        boolean active,
        boolean isDefault,
        Instant assignedAt) {
}
