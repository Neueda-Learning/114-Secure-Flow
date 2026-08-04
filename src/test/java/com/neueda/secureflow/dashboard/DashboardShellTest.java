package com.neueda.secureflow.dashboard;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardShellTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesTheDashboardShell() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SecureFlow")))
                .andExpect(content().string(containsString("Active alerts")))
                .andExpect(content().string(containsString("create-transaction-form")))
                .andExpect(content().string(containsString("name=\"accountId\"")))
                .andExpect(content().string(containsString("name=\"payeeId\"")))
                .andExpect(content().string(containsString("name=\"amount\"")))
                .andExpect(content().string(containsString("name=\"currency\"")))
                .andExpect(content().string(containsString("name=\"transactionTime\"")))
                .andExpect(content().string(containsString("submit-transaction")))
                .andExpect(content().string(containsString("demo-high")))
                .andExpect(content().string(containsString("demo-new")))
                .andExpect(content().string(containsString("demo-velocity")))
                .andExpect(content().string(containsString("alert-history-body")))
                .andExpect(content().string(containsString("alert-detail")));
    }
}
