package com.neueda.secureflow.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neueda.secureflow.common.PageResponse;
import com.neueda.secureflow.transaction.dto.CreateTransactionRequest;
import com.neueda.secureflow.transaction.dto.TransactionCreatedResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {
    @Mock TransactionService service;

    @Test
    void delegatesCreateAndSearch() {
        var controller = new TransactionController(service);
        var request = new CreateTransactionRequest("ACC", "PAYEE", BigDecimal.TEN,
                "INR", Instant.parse("2026-08-02T10:00:00Z"), null);
        var created = new TransactionCreatedResponse(null, List.of());
        var page = new PageResponse<com.neueda.secureflow.transaction.dto.TransactionResponse>(
                List.of(), 0, 20, 0, 0);
        when(service.create(request)).thenReturn(created);
        when(service.search("acc", null, null, null, null, 0, 20)).thenReturn(page);

        assertThat(controller.create(request)).isSameAs(created);
        assertThat(controller.search("acc", null, null, null, null, 0, 20)).isSameAs(page);
        verify(service).create(request);
    }
}
