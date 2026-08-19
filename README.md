# Chronos-Audit ⏱️💳
> Automated Financial Telemetry & Subscription Leak Interception Engine

Chronos-Audit is an automated financial monitoring backend service engineered in Spring Boot. It continuously aggregates multi-source activity signals (DNS lookups, email headers, and bank ledger records) to evaluate recurring merchant transactions, detect dormant subscription leaks (inactive > 30 days), and dispatch proactive alerts.

---

## 🚀 Key Architectural Features

* **Multi-Source Ingress Telemetry**: Ingests and normalizes transaction inputs across email verification headers, DNS telemetry, and core banking feeds.
* **Automated Batch Processing Engine**: Evaluates dormancy thresholds (30-day inactivity windows) on a scheduled cron cadence using Spring `@Scheduled`.
* **Proactive Renewal Detection**: Performs forward-looking sweeps to alert users 48 hours before an active billing cycle charges.
* **Decoupled Asynchronous Dispatcher**: Offloads SMTP network blocking using Spring `@Async` and JavaMailSender to maintain millisecond-level request responsiveness.
* **Optimized JPA & Query Projections**: Employs direct constructor DTO projections and `JOIN FETCH` queries to eliminate Hibernate $N+1$ select overhead and avoid lazy proxy detachment.
* **Centralized Exception Propagation**: Unified `@RestControllerAdvice` delivering RFC 7807 compliant error payloads with custom application diagnostic codes.

---

## 🛠️ Tech Stack & Dependencies

* **Language**: Java 17
* **Framework**: Spring Boot 3.x (Spring Web, Spring Data JPA, Spring Validation, Spring Mail)
* **Database**: MySQL 8.x
* **Documentation**: OpenAPI 3.0 / Swagger UI (Springdoc)
* **Email Sandbox**: Mailtrap / SMTP

---

## 🏛️ Database Schema Design

+------------------+         +-------------------------------+
|      USERS       |         |         SUBSCRIPTIONS         |
+------------------+         +-------------------------------+
| id (PK)          |<---+     | id (PK)                       |
| email (UQ)       |    |     | user_id (FK) -----------------+
| full_name        |    +-----| provider_name                 |
| created_at       |          | monthly_amount                |
+------------------+          | status (ACTIVE, CRITICAL_LEAK)|
| last_interaction_timestamp    |
| next_billing_date             |
+-------------------------------+


---

## 🔌 API Endpoints Summary

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/audit/ingest` | Ingest real-time bank ledger transactions and update touchpoints |
| `GET` | `/api/audit/dashboard/leaks` | Fetch high-performance DTO list of all flagged leak subscriptions |
| `GET` | `/swagger-ui/index.html` | Interactive Swagger API Explorer & schema documentation |

---

## ⚙️ Configuration & Setup

### 1. Database Setup
```sql
CREATE DATABASE chronos_audit_db;

2. Configure application.properties
Update src/main/resources/application.properties with your database and Mailtrap SMTP credentials:

3. Build and Run
Bash
# Clone the repository
git clone [https://github.com/](https://github.com/)<your-username>/chronos-audit.git
cd chronos-audit

# Build project
mvn clean install

# Run application
mvn spring-boot:run