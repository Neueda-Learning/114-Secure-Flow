package com.neueda.secureflow.transaction;

import com.neueda.secureflow.common.PageResponse;
import com.neueda.secureflow.transaction.dto.CreateTransactionRequest;
import com.neueda.secureflow.transaction.dto.CreatedTransactionResponse;
import com.neueda.secureflow.transaction.dto.TransactionResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@Validated
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedTransactionResponse create(
            @Valid @RequestBody CreateTransactionRequest request) {
        return service.create(request);
    }

    @GetMapping
    public PageResponse<TransactionResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @Min(1) Long transactionId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.search(
                search, transactionId, minAmount, maxAmount, from, to, page, size);
    }
}
