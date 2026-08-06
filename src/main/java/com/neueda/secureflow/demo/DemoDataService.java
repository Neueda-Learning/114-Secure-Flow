package com.neueda.secureflow.demo;

import com.neueda.secureflow.transaction.TransactionRepository;
import com.neueda.secureflow.transaction.TransactionService;
import com.neueda.secureflow.transaction.dto.CreateTransactionRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoDataService {
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    public DemoDataService(
            TransactionService transactionService,
            TransactionRepository transactionRepository) {
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public DemoDataResponse seedIfEmpty() {
        if (transactionRepository.count() > 0) {
            return new DemoDataResponse(
                    0, 0, true, "Existing data kept. Startup demo data was skipped.");
        }

        return seed();
    }

    @Transactional
    public DemoDataResponse seed() {
        String batch = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        String accountA = id(batch, "A1");
        String accountB = id(batch, "A2");
        String accountC = id(batch, "A3");
        String accountFast = id(batch, "FAST");
        String accountE = id(batch, "A4");
        String accountF = id(batch, "A5");
        String accountH = id(batch, "A6");

        List<CreateTransactionRequest> samples = List.of(
                transaction(accountA, payee(batch, "RENT"), "2100.00", "Rent payment 1"),
                transaction(accountA, payee(batch, "RENT"), "2200.00", "Rent payment 2"),
                transaction(accountA, payee(batch, "RENT"), "2300.00", "Rent payment 3"),
                transaction(accountA, payee(batch, "UTIL"), "860.00", "Utility payment"),
                transaction(accountB, payee(batch, "ELECTRONICS"), "12500.00",
                        "Electronics purchase"),
                transaction(accountC, payee(batch, "TUITION"), "18000.00",
                        "Tuition payment"),
                transaction(accountFast, payee(batch, "TRANSFER"), "200.00", "Transfer 1"),
                transaction(accountFast, payee(batch, "TRANSFER"), "250.00", "Transfer 2"),
                transaction(accountFast, payee(batch, "TRANSFER"), "300.00", "Transfer 3"),
                transaction(accountFast, payee(batch, "TRANSFER"), "350.00", "Transfer 4"),
                transaction(accountFast, payee(batch, "TRANSFER"), "400.00", "Transfer 5"),
                transaction(accountFast, payee(batch, "TRANSFER"), "450.00", "Transfer 6"),
                transaction(accountE, payee(batch, "GROCERIES"), "1450.00", "Groceries 1"),
                transaction(accountE, payee(batch, "GROCERIES"), "1560.00", "Groceries 2"),
                transaction(accountE, payee(batch, "GROCERIES"), "1320.00", "Groceries 3"),
                transaction(accountF, payee(batch, "SUBSCRIPTION"), "499.00", "Subscription 1"),
                transaction(accountF, payee(batch, "SUBSCRIPTION"), "499.00", "Subscription 2"),
                transaction(accountF, payee(batch, "SUBSCRIPTION"), "499.00", "Subscription 3"),
                transaction(accountH, payee(batch, "TRAVEL"), "22000.00", "Travel booking"),
                transaction(accountH, payee(batch, "TRAVEL"), "1200.00", "Travel service")
        );

        int alertsCreated = 0;
        for (CreateTransactionRequest sample : samples) {
            alertsCreated += transactionService.create(sample).generatedAlerts().size();
        }

        return new DemoDataResponse(
                samples.size(),
                alertsCreated,
                false,
                "Added " + samples.size() + " demo transactions and "
                        + alertsCreated + " alerts with current timestamps.");
    }

    private String id(String batch, String value) {
        return "DEMO-" + batch + "-" + value;
    }

    private String payee(String batch, String value) {
        return "PAYEE-" + batch + "-" + value;
    }

    private CreateTransactionRequest transaction(
            String account, String payee, String amount, String description) {
        return new CreateTransactionRequest(
                account,
                payee,
                new BigDecimal(amount),
                "INR",
                description);
    }
}
