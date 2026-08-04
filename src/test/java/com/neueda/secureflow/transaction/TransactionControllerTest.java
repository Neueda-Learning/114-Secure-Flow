package com.neueda.secureflow.transaction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.neueda.secureflow.monitoring.MonitoringService;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionRepository repository;

    @MockitoBean
    private MonitoringService monitoringService;

    @Test
    void createsAValidTransaction() throws Exception {
        when(repository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(monitoringService.evaluate(any(TransactionEntity.class), any(Boolean.class)))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "ACC-001",
                                  "payeeId": "PAYEE-001",
                                  "amount": 125.50,
                                  "currency": "usd",
                                  "transactionTime": "2026-08-03T10:00:00Z",
                                  "description": "Invoice"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transaction.accountId").value("ACC-001"))
                .andExpect(jsonPath("$.transaction.amount").value(125.50))
                .andExpect(jsonPath("$.transaction.currency").value("USD"))
                .andExpect(jsonPath("$.generatedAlerts").isEmpty());
    }

    @Test
    void rejectsAnInvalidTransaction() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "ACC-001",
                                  "amount": 0,
                                  "currency": "US",
                                  "transactionTime": "2026-08-03T10:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }
}
