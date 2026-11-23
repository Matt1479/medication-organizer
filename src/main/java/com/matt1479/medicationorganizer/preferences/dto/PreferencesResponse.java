package com.matt1479.medicationorganizer.preferences.dto;

import java.time.Instant;

public record PreferencesResponse(
        Long id,
        Long userId,
        boolean simplifiedMode,
        int textScale,
        boolean highContrast,
        Instant updatedAt) {
}
