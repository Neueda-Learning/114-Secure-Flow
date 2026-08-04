package com.neueda.secureflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.neueda.secureflow.alert.AlertRepository;
import com.neueda.secureflow.transaction.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecureFlowApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired AlertRepository alertRepository;
    @Autowired TransactionRepository transactionRepository;

    @BeforeEach
    void cleanDatabase() {
        alertRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    @Test
    void transactionCreatesAlertAndAlertCompletesLifecycle() throws Exception {
        String createdBody = mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson("ACC-API", "PAYEE-NEW", "100.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transaction.accountId").value("ACC-API"))
                .andExpect(jsonPath("$.generatedAlerts[0].ruleType").value("NEW_PAYEE"))
                .andReturn().getResponse().getContentAsString();
        Number alertId = JsonPath.read(createdBody, "$.generatedAlerts[0].id");

        mockMvc.perform(patch("/api/alerts/{id}/status", alertId.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\":\"CLOSED\",\"resolutionNotes\":\"Too early\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Invalid alert transition"));

        transition(alertId, "ACKNOWLEDGED", null, "ACKNOWLEDGED");
        transition(alertId, "INVESTIGATING", null, "INVESTIGATING");
        transition(alertId, "CLOSED", "Confirmed legitimate payment", "CLOSED");

        mockMvc.perform(get("/api/alerts/{id}", alertId.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history.length()").value(4))
                .andExpect(jsonPath("$.triggeringTransactions.length()").value(1));
    }

    @Test
    void highAmountAndVelocityBoundariesWorkEndToEnd() throws Exception {
        mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson("ACC-AMOUNT", "PAYEE-A", "10000.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generatedAlerts.length()").value(1));

        mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson("ACC-AMOUNT", "PAYEE-A", "10000.01")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generatedAlerts[0].ruleType").value("AMOUNT_THRESHOLD"));

        for (int index = 1; index <= 6; index++) {
            var result = mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                            .content(transactionJson("ACC-FAST", "PAYEE-FAST", "10.00")))
                    .andExpect(status().isCreated());
            if (index == 6) {
                result.andExpect(jsonPath("$.generatedAlerts[0].ruleType").value("VELOCITY"));
            }
        }
    }

    @Test
    void listsFiltersRulesSummaryAndStaticDashboard() throws Exception {
        mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson("SEARCH-ME", "PAYEE-X", "40.00")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/transactions").param("search", "search-me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1));
        mockMvc.perform(get("/api/alerts").param("status", "OPEN"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalItems").value(1));
        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.activeAlertCount").value(1));
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("SecureFlow")));
    }

    @Test
    void validationAndMissingResourcesUseProblemDetails() throws Exception {
        mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":\"\",\"currency\":\"EURO\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("Validation failed"));
        mockMvc.perform(get("/api/alerts/999999"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.title").value("Resource not found"));
    }

    private void transition(Number id, String target, String notes, String expected) throws Exception {
        String noteValue = notes == null ? "null" : "\"" + notes + "\"";
        mockMvc.perform(patch("/api/alerts/{id}/status", id.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\":\"" + target + "\",\"resolutionNotes\":" + noteValue + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value(expected));
    }

    private String transactionJson(String account, String payee, String amount) {
        return "{\"accountId\":\"" + account + "\",\"payeeId\":\"" + payee
                + "\",\"amount\":" + amount + ",\"currency\":\"INR\","
                + "\"transactionTime\":\"2026-08-02T10:00:00Z\",\"description\":\"Integration test\"}";
    }
}
