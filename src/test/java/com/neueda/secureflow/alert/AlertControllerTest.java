package com.neueda.secureflow.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.neueda.secureflow.alert.dto.AlertDetailResponse;
import com.neueda.secureflow.alert.dto.AlertSummaryResponse;
import com.neueda.secureflow.alert.dto.UpdateAlertStatusRequest;
import com.neueda.secureflow.common.PageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {
    @Mock AlertService service;
    @Mock AlertDetailResponse detail;

    @Test
    void delegatesListDetailAndTransition() {
        var controller = new AlertController(service);
        var page = new PageResponse<AlertSummaryResponse>(List.of(), 0, 20, 0, 0);
        var request = new UpdateAlertStatusRequest(AlertStatus.ACKNOWLEDGED, null);
        when(service.search(AlertStatus.OPEN, AlertSeverity.HIGH, 0, 20)).thenReturn(page);
        when(service.get(3)).thenReturn(detail);
        when(service.transition(3, AlertStatus.ACKNOWLEDGED, null)).thenReturn(detail);

        assertThat(controller.search(AlertStatus.OPEN, AlertSeverity.HIGH, 0, 20)).isSameAs(page);
        assertThat(controller.get(3)).isSameAs(detail);
        assertThat(controller.transition(3, request)).isSameAs(detail);
    }
}
