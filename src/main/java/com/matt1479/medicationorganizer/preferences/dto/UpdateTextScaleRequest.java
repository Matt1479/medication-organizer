package com.matt1479.medicationorganizer.preferences.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateTextScaleRequest(
        @Min(100)
        @Max(300)
        int textScale) {
}
