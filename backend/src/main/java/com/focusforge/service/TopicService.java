package com.focusforge.service;

import com.focusforge.common.NotFoundException;
import com.focusforge.domain.TopicArea;
import com.focusforge.domain.TopicProgress;
import com.focusforge.domain.TopicStatus;
import com.focusforge.dto.TopicDtos.*;
import com.focusforge.repository.TopicProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TopicService {
    private final TopicProgressRepository repo;
    private final UserSetupService setup;
    private final Dates dates;

    public TopicService(TopicProgressRepository repo, UserSetupService setup, Dates dates) {
        this.repo = repo;
        this.setup = setup;
        this.dates = dates;
    }

    public static TopicArea areaFor(String area) {
        try {
            return TopicArea.valueOf(area.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Unknown topic area: " + area);
        }
    }

    public TopicListDto list(Long userId, String area) {
        setup.ensureTopics(userId);
        return summarize(repo.findByUserIdAndAreaOrderBySortOrderAscIdAsc(userId, areaFor(area)));
    }

    public TopicDto updateStatus(Long userId, String area, Long id, TopicStatusRequest req) {
        TopicProgress t = find(userId, area, id);
        TopicStatus previous = t.getStatus();
        t.setStatus(req.status());
        if (req.notes() != null) t.setNotes(StudySessionService.blankToNull(req.notes()));
        if (req.status() == TopicStatus.COMPLETED && previous != TopicStatus.COMPLETED) {
            t.setCompletedOn(dates.today());
        } else if (req.status() != TopicStatus.COMPLETED) {
            t.setCompletedOn(null);
        }
        t.setDemo(false); // once the user touches a topic it is their data, not demo data
        return toDto(repo.save(t));
    }

    public TopicDto create(Long userId, String area, TopicCreateRequest req) {
        TopicArea a = areaFor(area);
        int next = repo.findByUserIdAndAreaOrderBySortOrderAscIdAsc(userId, a).size();
        TopicProgress t = new TopicProgress();
        t.setUserId(userId);
        t.setArea(a);
        t.setGroup(req.group().trim());
        t.setName(req.name().trim());
        t.setSortOrder(next);
        return toDto(repo.save(t));
    }

    public void delete(Long userId, String area, Long id) {
        repo.delete(find(userId, area, id));
    }

    /** Summary used by the dashboard. AWS hands-on labs are excluded from the topic count. */
    public TopicListDto summarize(List<TopicProgress> topics) {
        List<TopicProgress> counted = topics.stream()
                .filter(t -> !TopicCatalog.HANDS_ON_GROUP.equals(t.getGroup())).toList();
        int done = (int) counted.stream().filter(t -> t.getStatus() == TopicStatus.COMPLETED).count();
        int weight = counted.stream().mapToInt(t -> switch (t.getStatus()) {
            case NOT_STARTED -> 0;
            case LEARNING -> 33;
            case PRACTICING -> 66;
            case COMPLETED -> 100;
        }).sum();
        int total = counted.size();
        return new TopicListDto(topics.stream().map(this::toDto).toList(), done, total,
                total == 0 ? 0 : Math.round(done * 100f / total), total == 0 ? 0 : Math.round(weight / (float) total));
    }

    private TopicProgress find(Long userId, String area, Long id) {
        TopicArea a = areaFor(area);
        return repo.findByIdAndUserId(id, userId).filter(t -> t.getArea() == a)
                .orElseThrow(() -> new NotFoundException("Topic not found"));
    }

    private TopicDto toDto(TopicProgress t) {
        return new TopicDto(t.getId(), t.getGroup(), t.getName(), t.getStatus(), t.getNotes(), t.getCompletedOn());
    }
}
