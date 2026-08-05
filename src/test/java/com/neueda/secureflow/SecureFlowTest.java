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
class SecureFlowTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    AlertRepository alerts;

    @Autowired
    TransactionRepository transactions;

    @BeforeEach
    void emptyDatabase() {
        alerts.deleteAll();
        transactions.deleteAll();
    }

    @Test
    void createsAndSearchesATransaction() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transaction(" ACC-1 ", " PAYEE-1 ", "250.50")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transaction.accountId").value("ACC-1"))
                .andExpect(jsonPath("$.transaction.currency").value("INR"))
                .andExpect(jsonPath("$.generatedAlerts[0].ruleType").value("NEW_PAYEE"));

        mockMvc.perform(get("/api/transactions")
                        .param("search", "acc-1")
                        .param("minAmount", "200")
                        .param("maxAmount", "300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items[0].payeeId").value("PAYEE-1"));
    }

    @Test
    void amountAndVelocityRulesCreateAlerts() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transaction("BIG", "SHOP", "10000.01")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generatedAlerts[0].ruleType")
                        .value("AMOUNT_THRESHOLD"))
                .andExpect(jsonPath("$.generatedAlerts[1].ruleType")
                        .value("NEW_PAYEE"));

        for (int number = 1; number <= 6; number++) {
            var result = mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(transaction("FAST", "SAME-PAYEE", "10.00")))
                    .andExpect(status().isCreated());

            if (number == 6) {
                result.andExpect(jsonPath("$.generatedAlerts[0].ruleType")
                        .value("VELOCITY"));
            }
        }
    }

    @Test
    void alertMovesThroughTheCompleteStatusFlow() throws Exception {
        String response = mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transaction("LIFE", "NEW-PAYEE", "20.00")))
                .andReturn().getResponse().getContentAsString();

        Number id = JsonPath.read(response, "$.generatedAlerts[0].id");

        changeStatus(id, "CLOSED", "Too early", 409);
        changeStatus(id, "ACKNOWLEDGED", null, 200);
        changeStatus(id, "INVESTIGATING", null, 200);
        changeStatus(id, "CLOSED", "Payment is legitimate", 200);
        changeStatus(id, "ACKNOWLEDGED", null, 409);

        mockMvc.perform(get("/api/alerts/{id}", id.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.history.length()").value(4))
                .andExpect(jsonPath("$.triggeringTransactions.length()").value(1));
    }

    @Test
    void alertCanBeDismissedWithAReason() throws Exception {
        String response = mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transaction("DISMISS", "PAYEE", "20.00")))
                .andReturn().getResponse().getContentAsString();

        Number id = JsonPath.read(response, "$.generatedAlerts[0].id");

        changeStatus(id, "ACKNOWLEDGED", null, 200);
        changeStatus(id, "DISMISSED", "x", 400);
        changeStatus(id, "DISMISSED", "False positive", 200);

        mockMvc.perform(get("/api/alerts").param("status", "DISMISSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    void badRequestsHaveClearErrors() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":\"\",\"currency\":\"EURO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionWithCurrency("A", "B", "10", "EUR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));

        mockMvc.perform(get("/api/transactions")
                        .param("minAmount", "50")
                        .param("maxAmount", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/transactions")
                        .param("from", "2026-08-02T11:00:00Z")
                        .param("to", "2026-08-02T10:00:00Z"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/alerts/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));

        mockMvc.perform(get("/api/alerts").param("status", "WRONG"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dashboardRulesFiltersAndWebPageWork() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transaction("WEB", "PAYEE", "15000.00")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionCountToday").value(1))
                .andExpect(jsonPath("$.activeAlertCount").value(2));

        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/api/alerts").param("severity", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1));

        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("SecureFlow")));
    }

    private void changeStatus(Number id, String status, String notes, int expected)
            throws Exception {
        String noteJson = notes == null ? "null" : "\"" + notes + "\"";
        mockMvc.perform(patch("/api/alerts/{id}/status", id.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\":\"" + status
                                + "\",\"resolutionNotes\":" + noteJson + "}"))
                .andExpect(status().is(expected));
    }

    private String transaction(String account, String payee, String amount) {
        return transactionWithCurrency(account, payee, amount, "INR");
    }

    private String transactionWithCurrency(
            String account, String payee, String amount, String currency) {
        return "{\"accountId\":\"" + account
                + "\",\"payeeId\":\"" + payee
                + "\",\"amount\":" + amount
                + ",\"currency\":\"" + currency
                + "\",\"description\":\"Test payment\"}";
    }
}
