package com.focusforge.web;

import com.focusforge.service.DemoDataService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.focusforge.common.CurrentUser.id;

@RestController
@RequestMapping("/api/demo")
public class DemoController {
    private final DemoDataService demo;

    public DemoController(DemoDataService demo) { this.demo = demo; }

    @GetMapping
    public Map<String, Boolean> status() { return Map.of("active", demo.hasDemo(id())); }

    @PostMapping
    public Map<String, Boolean> load() {
        demo.load(id());
        return Map.of("active", true);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void purge() { demo.purge(id()); }
}
