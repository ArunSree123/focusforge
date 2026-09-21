package com.focusforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class ProjectDtos {
    private ProjectDtos() {}

    public record ProjectRequest(
            @NotBlank(message = "Project name is required") @Size(max = 150) String name,
            @Size(max = 3000) String problemStatement,
            @Size(max = 3000) String features,
            @Size(max = 1000) String techStack,
            @Size(max = 3000) String architecture,
            @Size(max = 3000) String databaseDesign,
            @Size(max = 3000) String apiFlow,
            @Size(max = 3000) String challenges,
            @Size(max = 3000) String solutions,
            @Size(max = 3000) String deployment,
            @Size(max = 3000) String futureImprovements) {}

    public record ChecklistItem(String field, String label, boolean done) {}

    public record ProjectDto(Long id, String name, String problemStatement, String features, String techStack,
                             String architecture, String databaseDesign, String apiFlow, String challenges,
                             String solutions, String deployment, String futureImprovements,
                             List<ChecklistItem> checklist, int readinessPct, boolean demo) {}
}
