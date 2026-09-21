package com.focusforge.web;

import com.focusforge.dto.ProblemDtos.*;
import com.focusforge.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.focusforge.common.CurrentUser.id;

/** Serves /api/dsa/problems, /api/sql/problems, /api/java/problems and /api/problem-solving/problems. */
@RestController
@RequestMapping("/api/{area}/problems")
public class ProblemController {
    private final ProblemService service;

    public ProblemController(ProblemService service) { this.service = service; }

    @GetMapping
    public ProblemListDto list(@PathVariable String area) { return service.list(id(), area); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProblemDto create(@PathVariable String area, @Valid @RequestBody ProblemRequest req) {
        return service.create(id(), area, req);
    }

    @PutMapping("/{problemId}")
    public ProblemDto update(@PathVariable String area, @PathVariable Long problemId, @Valid @RequestBody ProblemRequest req) {
        return service.update(id(), area, problemId, req);
    }

    @DeleteMapping("/{problemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String area, @PathVariable Long problemId) { service.delete(id(), area, problemId); }
}
