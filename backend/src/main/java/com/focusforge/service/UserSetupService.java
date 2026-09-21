package com.focusforge.service;

import com.focusforge.domain.*;
import com.focusforge.repository.MorningItemRepository;
import com.focusforge.repository.RoutineItemRepository;
import com.focusforge.repository.TopicProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Creates the default routine, morning checklist and topic catalog for a new user. */
@Service
public class UserSetupService {
    private final RoutineItemRepository routineItems;
    private final MorningItemRepository morningItems;
    private final TopicProgressRepository topics;

    public UserSetupService(RoutineItemRepository routineItems, MorningItemRepository morningItems,
                            TopicProgressRepository topics) {
        this.routineItems = routineItems;
        this.morningItems = morningItems;
        this.topics = topics;
    }

    @Transactional
    public void seedDefaults(Long userId) {
        int order = 0;
        for (Defaults.RoutineDefault d : Defaults.ROUTINE) {
            RoutineItem it = new RoutineItem();
            it.setUserId(userId);
            it.setRoutineKey(d.key());
            it.setTitle(d.title());
            it.setTargetMinutes(d.minutes());
            it.setTargetCount(d.count());
            it.setScheduledTime(d.time());
            it.setSortOrder(order++);
            routineItems.save(it);
        }
        order = 0;
        for (String title : Defaults.MORNING) {
            MorningItem m = new MorningItem();
            m.setUserId(userId);
            m.setTitle(title);
            m.setSortOrder(order++);
            morningItems.save(m);
        }
        ensureTopics(userId);
    }

    @Transactional
    public void ensureTopics(Long userId) {
        if (topics.existsByUserId(userId)) return;
        List<TopicProgress> rows = new ArrayList<>();
        for (Map.Entry<TopicArea, Map<String, List<String>>> area : TopicCatalog.ALL.entrySet()) {
            int order = 0;
            for (Map.Entry<String, List<String>> group : area.getValue().entrySet()) {
                for (String name : group.getValue()) {
                    TopicProgress t = new TopicProgress();
                    t.setUserId(userId);
                    t.setArea(area.getKey());
                    t.setGroup(group.getKey());
                    t.setName(name);
                    t.setSortOrder(order++);
                    rows.add(t);
                }
            }
        }
        topics.saveAll(rows);
    }
}
