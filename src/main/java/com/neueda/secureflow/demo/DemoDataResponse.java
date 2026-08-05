package com.neueda.secureflow.demo;

public record DemoDataResponse(
        int transactionsCreated,
        int alertsCreated,
        boolean skipped,
        String message
) {}
