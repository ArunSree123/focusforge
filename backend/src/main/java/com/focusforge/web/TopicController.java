package com.focusforge.web;

import com.focusforge.dto.TopicDtos.*;
import com.focusforge.service.TopicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.focusforge.common.CurrentUser.id;

/** Serves /api/{aws|dsa|sql|java|interview}/topics. */
@RestController
@RequestMapping("/api/{area}/topics")
public class TopicController {
    private final TopicService service;

    public TopicController(TopicService service) { this.service = service; }

    @GetMapping
    public TopicListDto list(@PathVariable String area) { return service.list(id(), area); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TopicDto create(@PathVariable String area, @Valid @RequestBody TopicCreateRequest req) {
        return service.create(id(), area, req);
    }

    @PutMapping("/{topicId}")
    public TopicDto update(@PathVariable String area, @PathVariable Long topicId, @Valid @RequestBody TopicStatusRequest req) {
        return service.updateStatus(id(), area, topicId, req);
    }

    @DeleteMapping("/{topicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String area, @PathVariable Long topicId) { service.delete(id(), area, topicId); }
}
