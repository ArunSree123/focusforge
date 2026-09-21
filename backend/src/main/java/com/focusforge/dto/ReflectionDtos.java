package com.focusforge.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class ReflectionDtos {
    private ReflectionDtos() {}

    public record ReflectionRequest(LocalDate date,
                                    @Min(1) @Max(5) Integer mood, @Min(1) @Max(5) Integer energy, @Min(1) @Max(5) Integer focus,
                                    @Size(max = 2000) String wentWell, @Size(max = 2000) String distractions,
                                    @Size(max = 2000) String improveTomorrow) {}

    public record ReflectionDto(LocalDate date, Integer mood, Integer energy, Integer focus,
                                String wentWell, String distractions, String improveTomorrow, boolean exists) {}
}
