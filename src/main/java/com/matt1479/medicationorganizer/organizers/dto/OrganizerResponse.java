package com.matt1479.medicationorganizer.organizers.dto;

import java.time.Instant;
import java.util.List;

public record OrganizerResponse(
        Long id,
        String productCode,
        String name,
        int daysCovered,
        int compartmentsPerDay,
        String description,
        String qrCode,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        List<CompartmentResponse> compartments) {
}
