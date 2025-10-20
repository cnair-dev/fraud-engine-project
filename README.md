# Fraud Rule Engine

A config-driven, composable fraud-detection microservice built with Spring Boot 3 (Java 17) and PostgreSQL 15.
It evaluates financial transactions using configurable rule weights, producing normalized risk scores and auditable flag records.
---

## Overview

This project implements a realistic fraud-scoring pipeline for financial and fintech systems.  
It evaluates transactions through configurable, weighted rules to determine a composite risk score, decision band, and reason codes.

### Key Features

- Configurable JSON ruleset (`fraud-ruleset.json`)
- Eight composable detection rules:
    - Amount Spike
    - Velocity
    - MCC / Risk Category
    - Geo-Velocity
    - New Device
    - Merchant Reputation
    - Account Risk / Tenure
    - Time of Day
- Weighted composite scoring normalized to a 0–100 scale
- Decision bands: APPROVE / REVIEW / DECLINE
- Persistence of flagged transactions with full JSONB audit trail
- Basic Authentication for API access
- Swagger UI and Actuator endpoints for observability
- Fully containerized environment with Docker Compose

---

## Architecture

| Layer | Responsibility |
|-------|----------------|
| Controllers | REST APIs for transaction evaluation and flag retrieval |
| Services | Orchestrate rule evaluation, scoring, and persistence |
| Rule Engine | Applies configurable, weighted rules and emits reason codes |
| Repositories | Spring Data JPA repositories for persistence |
| Database | PostgreSQL 15 with Flyway migrations and JSONB support |

---

## Technology Stack

| Component | Version / Tool |
|------------|----------------|
| Java | 17 (Temurin) |
| Spring Boot | 3.3.3 |
| PostgreSQL | 15-alpine |
| ORM | Spring Data JPA / Hibernate 6 |
| Build | Maven 3.9.11 |
| Testing | JUnit 5 with Testcontainers |
| API Docs | springdoc-openapi 2.6.0 |
| Containerization | Docker and Docker Compose |

---

## Running with Docker Compose

### 1. Build and Start

```bash
docker compose up --build
```

This builds the application image and starts both services:

fraud_app: Spring Boot application (port 8080)

fraud_db: PostgreSQL database (port 5432)

Once containers are running, verify application health:

```bash
curl -u admin:adminpass http://localhost:8080/actuator/health
```

### Database Seed Verification

The seed automatically loads demo data during app startup.

Check counts:
```bash
docker exec -it fraud_db psql -U fraud -d frauddb -c "SELECT COUNT(*) FROM customers;"
docker exec -it fraud_db psql -U fraud -d frauddb -c "SELECT COUNT(*) FROM transactions;"
docker exec -it fraud_db psql -U fraud -d frauddb -c "SELECT COUNT(*) FROM flagged_transactions;"
```
Expected results:
```
10 customers
≈250 transactions
≈40 flagged transactions
```

### 3. Evaluate a Transaction
```bash
curl -u admin:adminpass -H "Content-Type: application/json" -d '{
  "txnId": "b1a2d3c4-e5f6-7890-abcd-ef1234567890",
  "customerId": "11111111-2222-3333-4444-555555555555",
  "amount": 9500,
  "currency": "ZAR",
  "merchantId": "M12345",
  "mcc": "5812",
  "timestamp": "2025-10-19T14:25:00Z"
}' http://localhost:8080/api/v1/transactions/evaluate
```

### 4. Retrieve Flagged Transactions
```bash
curl -u admin:adminpass \
"http://localhost:8080/api/v1/transactions/flags?customerId=11111111-2222-3333-4444-555555555555"
```
Optional filters:
```makefile
&decision=REVIEW
&minScore=40
&from=2025-10-19T00:00:00Z
&to=2025-10-20T00:00:00Z
&reason=AMOUNT_SPIKE
```
### 5. Batch Evaluation
```bash
curl -u admin:adminpass -H "Content-Type: application/json" -d '{
  "transactions": [
    {
      "txnId": "11111111-1111-1111-1111-111111111111",
      "customerId": "11111111-2222-3333-4444-555555555555",
      "amount": 9500,
      "currency": "ZAR",
      "merchantId": "M12345",
      "mcc": "5812",
      "timestamp": "2025-10-19T14:25:00Z"
    },
    {
      "txnId": "22222222-2222-2222-2222-222222222222",
      "customerId": "11111111-2222-3333-4444-555555555555",
      "amount": 12000,
      "currency": "ZAR",
      "merchantId": "M12346",
      "mcc": "6011",
      "timestamp": "2025-10-19T15:25:00Z"
    }
  ]
}' http://localhost:8080/api/v1/transactions/batch-evaluate
```
### Testing Through Swagger UI
Once the containers are running, Swagger UI is accessible at:
```bash
http://localhost:8080/swagger-ui.html
```
## Credentials

Use the following credentials when accessing the API or Swagger UI:
```
Username: admin
Password: adminpass
```

You can test and inspect:

POST /api/v1/transactions/evaluate

POST /api/v1/transactions/batch-evaluate

GET /api/v1/transactions/flags (supports pagination and reason filter)

Swagger will generate request and response schemas automatically.

---

## Observability
Health endpoint: /actuator/health

Info endpoint: /actuator/info

Metrics endpoint: /actuator/metrics

Structured JSON logging includes traceId, txnId, and customerId fields for correlation.


