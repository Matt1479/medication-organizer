package com.matt1479.medicationorganizer.organizers.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.matt1479.medicationorganizer.organizers.TimeOfDay;

public record CompartmentResponse(
        Long id,
        int positionIndex,
        DayOfWeek dayOfWeek,
        TimeOfDay timeOfDay,
        LocalTime timeFrom,
        LocalTime timeTo) {
}
