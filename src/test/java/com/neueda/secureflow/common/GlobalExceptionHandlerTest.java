package com.neueda.secureflow.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.neueda.secureflow.alert.InvalidAlertTransitionException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsKnownDomainErrorsToProblemDetails() {
        assertThat(handler.notFound(new ResourceNotFoundException("missing")).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(handler.conflict(new InvalidAlertTransitionException("invalid")).getStatus())
                .isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(handler.badRequest(new BadRequestException("bad")).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
