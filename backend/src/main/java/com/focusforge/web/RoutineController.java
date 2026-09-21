package com.focusforge.web;

import com.focusforge.dto.RoutineDtos.*;
import com.focusforge.service.RoutineService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.focusforge.common.CurrentUser.id;

@RestController
@RequestMapping("/api/routines")
public class RoutineController {
    private final RoutineService routines;

    public RoutineController(RoutineService routines) { this.routines = routines; }

    @GetMapping
    public RoutineDayDto day(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return routines.day(id(), date);
    }

    @GetMapping("/items")
    public List<RoutineItemDto> items() { return routines.list(id()); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoutineItemDto create(@Valid @RequestBody RoutineItemRequest req) { return routines.create(id(), req); }

    @PutMapping("/{id}")
    public RoutineItemDto update(@PathVariable Long id, @Valid @RequestBody RoutineItemRequest req) {
        return routines.update(id(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long id) { routines.remove(id(), id); }

    @PostMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void complete(@PathVariable Long id,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        routines.complete(id(), id, date);
    }

    @DeleteMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uncomplete(@PathVariable Long id,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        routines.uncomplete(id(), id, date);
    }

    @PutMapping("/wake")
    public WakeDto wake(@RequestBody WakeRequest req) { return routines.setWake(id(), req); }

    @PostMapping("/morning-items")
    @ResponseStatus(HttpStatus.CREATED)
    public MorningItemDto addMorning(@Valid @RequestBody MorningItemRequest req) { return routines.addMorningItem(id(), req); }

    @DeleteMapping("/morning-items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMorning(@PathVariable Long id) { routines.removeMorningItem(id(), id); }

    @PutMapping("/morning-checks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void check(@RequestBody MorningCheckRequest req) { routines.setMorningCheck(id(), req); }
}
