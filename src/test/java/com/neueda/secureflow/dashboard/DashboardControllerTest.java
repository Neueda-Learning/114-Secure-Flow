package com.neueda.secureflow.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {
    @Mock DashboardService service;

    @Test
    void returnsServiceSummary() {
        var expected = new DashboardSummaryResponse(1, 2, 3, BigDecimal.TEN);
        when(service.summary()).thenReturn(expected);
        assertThat(new DashboardController(service).summary()).isSameAs(expected);
    }
}
