package com.focusforge.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlanServiceTest {
    @Test
    void keepsDefaultWhenLastWeekMetTarget() {
        assertEquals(840, PlanService.propose(900, 840, 30, true));
    }

    @Test
    void addsFifteenPercentToActualWhenBelowTarget() {
        // 600 * 1.15 = 690 -> rounded up to 690 (step 30)
        assertEquals(690, PlanService.propose(600, 840, 30, true));
    }

    @Test
    void neverDropsBelowSixtyPercentOfDefault() {
        // 60% of 840 = 504 -> 510 with step 30
        assertEquals(510, PlanService.propose(0, 840, 30, true));
    }

    @Test
    void usesDefaultWhenNoHistory() {
        assertEquals(840, PlanService.propose(0, 840, 30, false));
    }
}
