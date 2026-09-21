package com.focusforge.web;

import com.focusforge.dto.DashboardDtos.TodayDto;
import com.focusforge.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.focusforge.common.CurrentUser.id;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) { this.dashboard = dashboard; }

    @GetMapping("/today")
    public TodayDto today() { return dashboard.today(id()); }
}
