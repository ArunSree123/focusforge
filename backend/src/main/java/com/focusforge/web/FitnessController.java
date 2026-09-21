package com.focusforge.web;

import com.focusforge.dto.FitnessDtos.*;
import com.focusforge.service.FitnessService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.focusforge.common.CurrentUser.id;

@RestController
@RequestMapping("/api/fitness")
public class FitnessController {
    private final FitnessService service;

    public FitnessController(FitnessService service) { this.service = service; }

    @GetMapping
    public List<FitnessDto> list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.list(id(), from, to);
    }

    @GetMapping("/summary")
    public FitnessSummary summary(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.summary(id(), date);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FitnessDto create(@Valid @RequestBody FitnessRequest req) { return service.create(id(), req); }

    @PutMapping("/{id}")
    public FitnessDto update(@PathVariable Long id, @Valid @RequestBody FitnessRequest req) { return service.update(id(), id, req); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id(), id); }
}
