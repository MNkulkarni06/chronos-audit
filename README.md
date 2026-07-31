# Chronos-Audit: Automated Subscription Leak Finder Engine

Chronos-Audit is a high-performance backend automation registry designed to optimize recurring transactional ledgers. The platform acts as a regulatory and financial risk engine, evaluating subscription data pipelines in real-time to identify, flag, and mitigate dormant financial exposure ("leakage").

---

## 🏗️ Core System Architecture
The application is built on a decoupled three-tier enterprise layout designed for predictable horizontal scalability and safe transaction boundaries:

*   **API & Documentation Layer:** Managed by Spring Boot Web MVC and fully mapped via OpenAPI 3.0 / Swagger UI for sandbox testing.
*   **Core Automation Layer:** Executes concurrent, time-boxed database evaluation sweeps using Spring's native scheduling engine.
*   **Data Persistence Layer:** Utilizes an optimized connection pool (HikariCP) mapped to a MySQL relational database engine with automated schema updates managed by Hibernate ORM.

---

## ⚡ Automated Leak Evaluation Engine Logic
The core value proposition of the system is its autonomous background audit scheduler. Every execution cycle, the batch engine evaluates record telemetry against a distinct risk assessment matrix:

1.  **Dormancy Detection:** The engine performs optimized timestamp comparisons to isolate records that have not registered active user interactions within a 30-day temporal window.
2.  **Risk Matrix Profiling:** The system analyzes three distinct behavioral signals:
    *   `hasNetworkActivity` (Low-level gateway traffic logs)
    *   `hasEmailActivity` (SSO / transactional correspondence logs)
    *   `hasRecurringCharge` (Active financial ledger debits)
3.  **State Management Optimization:** If zero activity is detected across communication and network pathways while billing persists, the ledger updates the state to `CRITICAL_LEAK (100%)` to halt further financial loss.

---

## 🔌 API Contracts & Interactivity

The entire exposed controller layer is fully integrated with interactive API documentation.

### Interactive Sandbox Playground
When the application context is active, the complete end-to-end endpoint infrastructure can be visualized, modified, and executed live via the web browser:
👉 **Target Path:** `http://localhost:8080/swagger-ui/index.html`

### Primary Audit Execution Endpoint
*   **Route:** `POST /api/audit/evaluate/{id}`
*   **Content-Type:** `application/json`

#### Sample Telemetry Payload
```json
{
  "hasNetworkActivity": false,
  "hasEmailActivity": false,
  "hasRecurringCharge": true
}