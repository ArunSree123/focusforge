package com.focusforge.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Walks the flows from the product brief end to end against an in-memory database. */
@SpringBootTest
@AutoConfigureMockMvc
class FocusForgeFlowsTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String token;

    private JsonNode call(MockHttpServletRequestBuilder req, String body, int expected) throws Exception {
        if (token != null) req.header("Authorization", "Bearer " + token);
        if (body != null) req.contentType(MediaType.APPLICATION_JSON).content(body);
        MvcResult r = mvc.perform(req).andExpect(status().is(expected)).andReturn();
        String s = r.getResponse().getContentAsString();
        return s.isBlank() ? json.createObjectNode() : json.readTree(s);
    }

    private String register(String name) throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        token = null;
        JsonNode res = call(post("/api/auth/register"),
                "{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"password123\",\"loadDemo\":false}", 201);
        token = res.get("token").asText();
        return email;
    }

    @Test
    void protectedEndpointsRequireSignIn() throws Exception {
        token = null;
        JsonNode err = call(get("/api/dashboard/today"), null, 401);
        assertEquals(401, err.get("status").asInt());
    }

    @Test
    void duplicateEmailAndBadCredentialsAreRejected() throws Exception {
        String email = register("Arun");
        token = null;
        call(post("/api/auth/register"), "{\"name\":\"A\",\"email\":\"" + email + "\",\"password\":\"password123\"}", 409);
        call(post("/api/auth/login"), "{\"email\":\"" + email + "\",\"password\":\"wrong-password\"}", 401);
        call(post("/api/auth/login"), "{\"email\":\"" + email + "\",\"password\":\"password123\"}", 200);
    }

    @Test
    void dailyRoutineFlows() throws Exception {
        register("Arun");
        String today = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata")).toString();
        JsonNode day = call(get("/api/routines"), null, 200);
        assertEquals(9, day.get("items").size());                 // 1: routine exists
        assertEquals(4, day.get("morning").size());

        call(put("/api/routines/wake"), "{\"actual\":\"06:08\"}", 200);          // wake-up log
        JsonNode wake = call(get("/api/routines"), null, 200).get("wake");
        assertEquals(8, wake.get("diffMinutes").asInt());

        long morningId = day.get("morning").get(0).get("id").asLong();
        call(put("/api/routines/morning-checks"), "{\"itemId\":" + morningId + ",\"done\":true}", 204);

        long javaId = 0;                                                        // 1: update target
        for (JsonNode i : day.get("items")) if (i.get("key").asText().equals("JAVA")) javaId = i.get("id").asLong();
        call(put("/api/routines/" + javaId), "{\"title\":\"Java\",\"targetMinutes\":90,\"targetCount\":0,\"scheduledTime\":\"10:30\"}", 200);
        call(post("/api/routines/" + javaId + "/complete"), null, 204);         // 2: mark complete
        JsonNode score = call(get("/api/analytics/daily?date=" + today), null, 200);
        boolean javaDone = false;
        for (JsonNode i : score.get("items")) if (i.get("key").asText().equals("JAVA")) javaDone = i.get("completed").asBoolean();
        assertTrue(javaDone);
    }

    @Test
    void invalidStudySessionGetsStructuredError() throws Exception {
        register("Arun");
        JsonNode err = call(post("/api/study-sessions"), "{\"category\":\"JAVA\",\"durationMinutes\":0}", 400);
        assertEquals("Invalid study session duration", err.get("message").asText());
        assertEquals("/api/study-sessions", err.get("path").asText());
        assertNotNull(err.get("timestamp"));
    }

    @Test
    void learningModulesAndCareerFlows() throws Exception {
        register("Arun");
        call(post("/api/study-sessions"), "{\"category\":\"JAVA\",\"durationMinutes\":120,\"topic\":\"Streams\"}", 201);   // 3, 4
        call(post("/api/sql/problems"), "{\"title\":\"Rank Scores\",\"difficulty\":\"MEDIUM\",\"topic\":\"Window Functions\",\"attempts\":1,\"timeMinutes\":25,\"solved\":true}", 201); // 5
        call(post("/api/dsa/problems"), "{\"title\":\"Two Sum\",\"problemNumber\":1,\"difficulty\":\"EASY\",\"attempts\":1,\"timeMinutes\":10,\"solved\":true}", 201);  // 6
        JsonNode dsa = call(get("/api/dsa/problems"), null, 200);
        assertEquals(1, dsa.get("stats").get("solvedToday").asInt());
        assertEquals(1, dsa.get("stats").get("easyToday").asInt());

        JsonNode aws = call(get("/api/aws/topics"), null, 200);                                                            // 7
        long topicId = aws.get("topics").get(0).get("id").asLong();
        call(put("/api/aws/topics/" + topicId), "{\"status\":\"COMPLETED\"}", 200);
        assertEquals(1, call(get("/api/aws/topics"), null, 200).get("completed").asInt());

        call(post("/api/jobs"), "{\"company\":\"Zoho\",\"title\":\"Java Developer\",\"dateApplied\":\"" + LocalDate.now(java.time.ZoneId.of("Asia/Kolkata")) + "\",\"status\":\"APPLIED\"}", 201); // 8
        JsonNode jobs = call(get("/api/jobs?company=zo"), null, 200);
        assertEquals(1, jobs.get("jobs").size());
        assertEquals(1, jobs.get("stats").get("appliedToday").asInt());

        call(post("/api/projects"), "{\"name\":\"FocusForge\",\"problemStatement\":\"x\",\"techStack\":\"Java\"}", 201);    // 9
        assertEquals(20, call(get("/api/projects"), null, 200).get(0).get("readinessPct").asInt());

        JsonNode interview = call(get("/api/interview/topics"), null, 200);                                                // 10
        call(put("/api/interview/topics/" + interview.get("topics").get(0).get("id").asLong()), "{\"status\":\"COMPLETED\"}", 200);
    }

    @Test
    void fitnessAndScorecardFlows() throws Exception {
        register("Arun");
        call(post("/api/fitness"), "{\"type\":\"GYM\",\"startTime\":\"18:00\",\"endTime\":\"20:00\",\"workoutType\":\"PUSH\","
                + "\"exercises\":[{\"name\":\"Bench press\",\"sets\":4,\"reps\":8,\"weightKg\":60}]}", 201);               // 11
        call(post("/api/fitness"), "{\"type\":\"WALK\",\"durationMinutes\":30,\"steps\":3000}", 201);                       // 12
        call(post("/api/fitness"), "{\"type\":\"BASKETBALL\",\"durationMinutes\":60}", 201);                                // 13
        JsonNode today = call(get("/api/dashboard/today"), null, 200);                                                      // 14
        assertEquals(1, today.get("fitness").get("gymSessions").asInt());
        assertEquals(120, today.get("score").get("gymMinutes").asInt());
        assertTrue(today.get("score").get("walked").asBoolean());
        call(post("/api/fitness"), "{\"type\":\"GYM\",\"durationMinutes\":0}", 400);
    }

    @Test
    void reportsInsightsPlanAndPdf() throws Exception {
        register("Arun");
        JsonNode empty = call(get("/api/reports/insights"), null, 200);
        assertFalse(empty.get("hasEnoughData").asBoolean());
        assertEquals("Not enough data to generate a reliable insight yet.", empty.get("message").asText());

        call(post("/api/demo"), null, 200);
        JsonNode weekly = call(get("/api/reports/weekly"), null, 200);                                                      // 16, 17
        assertTrue(weekly.get("hasData").asBoolean());
        assertEquals(10, weekly.get("comparison").size());
        JsonNode analytics = call(get("/api/analytics/weekly"), null, 200);                                                 // 15
        assertEquals(7, analytics.get("days").size());
        assertTrue(call(get("/api/analytics/monthly"), null, 200).get("days").size() >= 28);

        assertTrue(call(get("/api/reports/insights"), null, 200).get("hasEnoughData").asBoolean());                         // 18

        LocalDate next = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata")).with(TemporalAdjusters.next(DayOfWeek.MONDAY));                                    // 19
        JsonNode plan = call(get("/api/reports/plan?weekStart=" + next), null, 200);
        assertFalse(plan.get("saved").asBoolean());
        call(put("/api/reports/plan?weekStart=" + next), json.writeValueAsString(plan), 200);
        assertTrue(call(get("/api/reports/plan?weekStart=" + next), null, 200).get("saved").asBoolean());

        MvcResult pdf = mvc.perform(get("/api/reports/weekly/pdf").header("Authorization", "Bearer " + token))   // 20
                .andExpect(status().isOk()).andReturn();
        assertTrue(new String(pdf.getResponse().getContentAsByteArray(), 0, 4).startsWith("%PDF"));

        call(delete("/api/demo"), null, 204);
        assertFalse(call(get("/api/demo"), null, 200).get("active").asBoolean());
    }

    @Test
    void usersCannotSeeEachOthersData() throws Exception {
        register("A");
        JsonNode job = call(post("/api/jobs"), "{\"company\":\"Zoho\",\"title\":\"Dev\",\"dateApplied\":\"" + LocalDate.now(java.time.ZoneId.of("Asia/Kolkata")) + "\",\"status\":\"APPLIED\"}", 201);
        register("B");
        call(put("/api/jobs/" + job.get("id").asLong()), "{\"company\":\"X\",\"title\":\"Y\",\"dateApplied\":\"" + LocalDate.now(java.time.ZoneId.of("Asia/Kolkata")) + "\",\"status\":\"APPLIED\"}", 404);
        assertEquals(0, call(get("/api/jobs"), null, 200).get("jobs").size());
    }
}
