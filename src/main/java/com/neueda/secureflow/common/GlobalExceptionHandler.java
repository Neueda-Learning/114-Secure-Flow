package com.neueda.secureflow.common;

import com.neueda.secureflow.alert.InvalidAlertTransitionException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail notFound(ResourceNotFoundException error) {
        return problem(HttpStatus.NOT_FOUND, "Not found", error.getMessage());
    }

    @ExceptionHandler(InvalidAlertTransitionException.class)
    ProblemDetail conflict(InvalidAlertTransitionException error) {
        return problem(HttpStatus.CONFLICT, "Invalid alert transition", error.getMessage());
    }

    @ExceptionHandler({BadRequestException.class, MethodArgumentNotValidException.class})
    ProblemDetail badRequest(Exception error) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", "Please check the request values");
    }

    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    ProblemDetail malformed(Exception error) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", "A request value has an invalid format");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://secureflow.local/problems/" + status.value()));
        return problem;
    }
}
