package com.focusforge.web;

import com.focusforge.domain.JobStatus;
import com.focusforge.dto.JobDtos.*;
import com.focusforge.service.JobService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.focusforge.common.CurrentUser.id;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService service;

    public JobController(JobService service) { this.service = service; }

    @GetMapping
    public JobListDto list(@RequestParam(required = false) String company,
                           @RequestParam(required = false) String role,
                           @RequestParam(required = false) JobStatus status,
                           @RequestParam(required = false) String location,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.list(id(), company, role, status, location, from, to);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobDto create(@Valid @RequestBody JobRequest req) { return service.create(id(), req); }

    @PutMapping("/{id}")
    public JobDto update(@PathVariable Long id, @Valid @RequestBody JobRequest req) { return service.update(id(), id, req); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id(), id); }
}
