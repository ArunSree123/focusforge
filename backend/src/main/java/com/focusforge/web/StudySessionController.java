package com.focusforge.web;

import com.focusforge.dto.StudyDtos.*;
import com.focusforge.service.StudySessionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.focusforge.common.CurrentUser.id;

@RestController
@RequestMapping("/api/study-sessions")
public class StudySessionController {
    private final StudySessionService service;

    public StudySessionController(StudySessionService service) { this.service = service; }

    @GetMapping
    public List<StudySessionDto> list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.list(id(), from, to);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudySessionDto create(@Valid @RequestBody StudySessionRequest req) { return service.create(id(), req); }

    @PutMapping("/{id}")
    public StudySessionDto update(@PathVariable Long id, @Valid @RequestBody StudySessionRequest req) {
        return service.update(id(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id(), id); }
}
