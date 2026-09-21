package com.focusforge.dto;

import com.focusforge.domain.Difficulty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public final class ProblemDtos {
    private ProblemDtos() {}

    public record ProblemRequest(
            @NotBlank(message = "Problem name is required") @Size(max = 200) String title,
            Integer problemNumber,
            @Size(max = 500) String url,
            @NotNull(message = "Choose a difficulty") Difficulty difficulty,
            @Size(max = 100) String topic,
            @Min(value = 1, message = "Attempts must be at least 1") @Max(999) int attempts,
            @Min(value = 0, message = "Time cannot be negative") @Max(1440) int timeMinutes,
            boolean solved,
            LocalDate date,
            @Size(max = 2000) String notes,
            @Size(max = 5000) String solution) {}

    public record ProblemDto(Long id, String kind, String title, Integer problemNumber, String url,
                             Difficulty difficulty, String topic, int attempts, int timeMinutes,
                             boolean solved, LocalDate date, String notes, String solution, boolean demo) {}

    public record ProblemStats(int attempted, int solved, double successRate, int avgSolveMinutes,
                               int solvedToday, int easyToday, int mediumToday, int hardToday,
                               long lifetimeSolved) {}

    public record ProblemListDto(List<ProblemDto> problems, ProblemStats stats) {}
}
