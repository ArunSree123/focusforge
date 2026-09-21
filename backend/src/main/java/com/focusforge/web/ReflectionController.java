package com.focusforge.web;

import com.focusforge.dto.ReflectionDtos.*;
import com.focusforge.service.ReflectionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.focusforge.common.CurrentUser.id;

@RestController
@RequestMapping("/api/reflections")
public class ReflectionController {
    private final ReflectionService service;

    public ReflectionController(ReflectionService service) { this.service = service; }

    @GetMapping
    public ReflectionDto get(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.get(id(), date);
    }

    @PutMapping
    public ReflectionDto save(@Valid @RequestBody ReflectionRequest req) { return service.save(id(), req); }
}
