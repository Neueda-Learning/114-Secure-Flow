# System Architecture

## System context

```mermaid
flowchart LR
    Operator["Fraud operations analyst"] -->|Uses browser| SecureFlow["SecureFlow"]
    APIClient["API client"] -->|REST/JSON| SecureFlow
    SecureFlow -->|Reads and writes| MySQL[("MySQL 8.4")]
    GitHub["GitHub Actions"] -->|Tests and packages| SecureFlow
    GitHub -->|Publishes image| GHCR["GitHub Container Registry"]
```

The static dashboard and backend are packaged together. The application has no
external runtime dependency beyond MySQL.

## Container and component view

```mermaid
flowchart TB
    UI["Static dashboard<br/>HTML + CSS + JavaScript"] -->|Relative HTTP/JSON| Controllers["REST controllers"]
    Controllers --> TransactionService["Transaction service"]
    Controllers --> AlertService["Alert service"]
    Controllers --> Dashboard["Dashboard summary"]
    TransactionService --> TransactionRepository["Transaction repository"]
    TransactionService --> MonitoringService["Monitoring service"]
    MonitoringService --> Rules["Amount / Velocity / New Payee rules"]
    MonitoringService --> AlertService
    AlertService --> AlertRepository["Alert repository"]
    Dashboard --> TransactionRepository
    Dashboard --> AlertRepository
    TransactionRepository --> DB[("MySQL")]
    AlertRepository --> DB
```

## Transaction and alert sequence

```mermaid
sequenceDiagram
    participant U as Operator/API client
    participant C as TransactionController
    participant T as TransactionService
    participant D as MySQL
    participant M as MonitoringService
    participant A as AlertService
    U->>C: POST /api/transactions
    C->>T: validated request
    T->>D: save transaction
    T->>M: evaluate transaction context
    M->>D: read recent/account-payee history
    M->>A: create each matching alert
    A->>D: save alert, links, and initial history
    T-->>U: 201 transaction + generated alerts
```

## Database model

```mermaid
erDiagram
    TRANSACTIONS {
        bigint id PK
        varchar account_id
        varchar payee_id
        decimal amount
        char currency
        datetime transaction_time
        varchar description
        datetime created_at
    }
    ALERTS {
        bigint id PK
        varchar rule_type
        varchar severity
        varchar status
        varchar account_id
        datetime created_at
        varchar resolution_notes
    }
    ALERT_STATUS_HISTORY {
        bigint id PK
        bigint alert_id FK
        varchar previous_status
        varchar new_status
        datetime changed_at
        varchar note
    }
    ALERT_TRANSACTIONS {
        bigint alert_id FK
        bigint transaction_id FK
    }
    ALERTS ||--o{ ALERT_STATUS_HISTORY : records
    ALERTS ||--o{ ALERT_TRANSACTIONS : contains
    TRANSACTIONS ||--o{ ALERT_TRANSACTIONS : triggers
```

## Linux deployment view

```mermaid
flowchart LR
    Browser -->|TCP 8080| App["SecureFlow container<br/>non-root, read-only"]
    App -->|private network, TCP 3306| DB["MySQL 8.4 container"]
    DB --> Volume[("Persistent Docker volume")]
```

Docker publishes only port `8080`. MySQL remains on the private Compose network.
Both services have health checks, bounded memory/logging, and automatic restart
policies.
