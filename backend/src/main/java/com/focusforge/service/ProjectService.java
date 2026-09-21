package com.focusforge.service;

import com.focusforge.common.NotFoundException;
import com.focusforge.domain.Project;
import com.focusforge.dto.ProjectDtos.*;
import com.focusforge.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProjectService {
    private final ProjectRepository repo;

    public ProjectService(ProjectRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public List<ProjectDto> list(Long userId) {
        return repo.findByUserIdOrderByUpdatedAtDesc(userId).stream().map(this::toDto).toList();
    }

    public ProjectDto create(Long userId, ProjectRequest req) {
        Project p = new Project();
        p.setUserId(userId);
        apply(p, req);
        return toDto(repo.save(p));
    }

    public ProjectDto update(Long userId, Long id, ProjectRequest req) {
        Project p = repo.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Project not found"));
        apply(p, req);
        return toDto(repo.save(p));
    }

    public void delete(Long userId, Long id) {
        repo.delete(repo.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Project not found")));
    }

    private void apply(Project p, ProjectRequest r) {
        p.setName(r.name().trim());
        p.setProblemStatement(StudySessionService.blankToNull(r.problemStatement()));
        p.setFeatures(StudySessionService.blankToNull(r.features()));
        p.setTechStack(StudySessionService.blankToNull(r.techStack()));
        p.setArchitecture(StudySessionService.blankToNull(r.architecture()));
        p.setDatabaseDesign(StudySessionService.blankToNull(r.databaseDesign()));
        p.setApiFlow(StudySessionService.blankToNull(r.apiFlow()));
        p.setChallenges(StudySessionService.blankToNull(r.challenges()));
        p.setSolutions(StudySessionService.blankToNull(r.solutions()));
        p.setDeployment(StudySessionService.blankToNull(r.deployment()));
        p.setFutureImprovements(StudySessionService.blankToNull(r.futureImprovements()));
    }

    /** The interview-explanation checklist is derived from which sections have content, so it can't drift. */
    private ProjectDto toDto(Project p) {
        List<ChecklistItem> list = new ArrayList<>();
        list.add(item("problemStatement", "Problem statement", p.getProblemStatement()));
        list.add(item("features", "Features", p.getFeatures()));
        list.add(item("techStack", "Tech stack", p.getTechStack()));
        list.add(item("architecture", "Architecture", p.getArchitecture()));
        list.add(item("databaseDesign", "Database", p.getDatabaseDesign()));
        list.add(item("apiFlow", "API", p.getApiFlow()));
        list.add(item("challenges", "Challenges", p.getChallenges()));
        list.add(item("solutions", "Solutions", p.getSolutions()));
        list.add(item("deployment", "Deployment", p.getDeployment()));
        list.add(item("futureImprovements", "Future improvements", p.getFutureImprovements()));
        int done = (int) list.stream().filter(ChecklistItem::done).count();
        return new ProjectDto(p.getId(), p.getName(), p.getProblemStatement(), p.getFeatures(), p.getTechStack(),
                p.getArchitecture(), p.getDatabaseDesign(), p.getApiFlow(), p.getChallenges(), p.getSolutions(),
                p.getDeployment(), p.getFutureImprovements(), list, Math.round(done * 100f / list.size()), p.isDemo());
    }

    private ChecklistItem item(String field, String label, String value) {
        return new ChecklistItem(field, label, value != null && !value.isBlank());
    }
}
