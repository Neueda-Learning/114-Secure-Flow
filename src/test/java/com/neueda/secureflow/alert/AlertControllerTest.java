package com.neueda.secureflow.alert;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.neueda.secureflow.common.BadRequestException;
import com.neueda.secureflow.common.PageResponse;
import com.neueda.secureflow.common.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AlertControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private AlertService service;

    @Test
    void returnsStablePageShape() throws Exception {
        when(service.list(null, null, 0, 20))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void mapsLifecycleFailuresToCorrectHttpCodes() throws Exception {
        when(service.transition(eq(1L), eq(AlertStatus.CLOSED), any()))
                .thenThrow(new InvalidAlertTransitionException("Invalid move"));
        when(service.transition(eq(2L), eq(AlertStatus.DISMISSED), any()))
                .thenThrow(new BadRequestException("Notes required"));
        when(service.get(99L)).thenThrow(new ResourceNotFoundException("Missing"));

        mockMvc.perform(patch("/api/alerts/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\":\"CLOSED\",\"resolutionNotes\":\"done\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Invalid alert transition"));
        mockMvc.perform(patch("/api/alerts/2/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\":\"DISMISSED\",\"resolutionNotes\":\"\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/alerts/99"))
                .andExpect(status().isNotFound());
    }
}
