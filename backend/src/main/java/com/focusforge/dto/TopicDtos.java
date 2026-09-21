package com.focusforge.dto;

import com.focusforge.domain.TopicStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public final class TopicDtos {
    private TopicDtos() {}

    public record TopicDto(Long id, String group, String name, TopicStatus status, String notes, LocalDate completedOn) {}

    public record TopicStatusRequest(@NotNull(message = "Choose a status") TopicStatus status, @Size(max = 1000) String notes) {}

    public record TopicCreateRequest(@NotBlank(message = "Group is required") @Size(max = 100) String group,
                                     @NotBlank(message = "Topic name is required") @Size(max = 150) String name) {}

    /** completionPct = completed/total; readinessPct weights the four levels 0/33/66/100. */
    public record TopicListDto(List<TopicDto> topics, int completed, int total, int completionPct, int readinessPct) {}
}
