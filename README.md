# ARI-OS (AR Industries Operating System) — Full-Stack Business Management System

A complete full-stack ERP, plant telemetry, and billing operating system for **AR Industries** (modular metal boxes, conceal fan boxes, and sheet metal press manufacturing).

Built using **Java 21 + Spring Boot 3.3.4 + MySQL** for the backend, integrated with the rich **ARI-OS Single-Page Application (HTML5 / Tailwind CSS / Vanilla JS)** in the same repository.

---

## Architecture & Technology Stack

### Backend Architecture
- **Framework:** Java 21, Spring Boot 3.3.4
- **ORM / Persistence:** Spring Data JPA, Hibernate, MySQL Connector/J
- **Database:** MySQL 8.x running locally on `localhost:3306` (database: `ar_industries_db`)
- **Schema Migrations:** Flyway (`src/main/resources/db/migration/V1__Initial_Schema.sql`)
- **Security:** Spring Security 6, JWT Authentication (JJWT 0.12.6), BCrypt Password Hashing (strength 12)
- **API Documentation:** Springdoc OpenAPI 3.0 / Swagger UI
- **Layered Design:** `Controller -> Service -> Repository -> MySQL`
- **Validation:** Jakarta Bean Validation (`@Valid`, `@NotNull`, `@NotBlank`, etc.)

### Standard Pagination Response
Every collection and listing API supports zero-based pagination returning the strict contract:
```json
{
  "data": [ ... ],
  "totalRecords": 25,
  "currentPage": 0,
  "pageSize": 10
}
```

### Project Structure
- **[`backend/`](file:///backend/)**: Java 21, Spring Boot 3.3.4, Maven REST APIs, Flyway migrations, MySQL integration.
- **[`frontend/`](file:///frontend/)**: Complete Single-Page Application (`index.html`, `api-client.js`, `package.json`, `README.md`).
- **Real-Time Integration:** Real-time connection badge in header (`Backend: Connected (MySQL)`).
- Full offline fallback + automatic online synchronization with Spring Boot APIs.

---

## Prerequisites

1. **Java JDK 21** installed and configured (`java -version`).
2. **Apache Maven 3.8+** installed (`mvn -version`).
3. **MySQL Server 8.x** running on `localhost:3306`.
4. Modern web browser (Chrome, Edge, Firefox).

---

## Quick Start Guide

### 1. Database Setup (MySQL)

Start your local MySQL server and create the database:

```sql
CREATE DATABASE IF NOT EXISTS ar_industries_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Update your MySQL credentials in [application.properties](file:///backend/src/main/resources/application.properties) or pass them via environment variables:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ar_industries_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
spring.datasource.username=root
spring.datasource.password=password
```

Environment variable overrides supported:
- `DB_USERNAME`: MySQL user (defaults to `root`)
- `DB_PASSWORD`: MySQL password (defaults to `password`)
- `JWT_SECRET`: 256-bit secret key for signing tokens

### 2. Build & Test Backend

In the `backend` directory, run:

```bash
cd backend
mvn clean test
```

All unit and integration tests (Pagination, GST Billing calculation, Attendance working hours) will execute.

### 3. Start Backend Server

From the `backend` directory, start the Spring Boot application:

```bash
mvn spring-boot:run
```

The server starts on port `8080`.
On first startup, Flyway automatically creates all database tables and `DataInitializer` seeds:
- Default Admin Account: `admin` / `admin123` (Role: `SUPER_ADMIN`)
- Official AR Industries 2026 Price List (0.7mm, 0.8mm, 1.0mm modular boxes)
- Inventory Stock Records with initial balances
- Default Customer Accounts & Plant Employees

### 4. Access Frontend

Run the static web server from the `frontend` directory:

```bash
cd frontend
python -m http.server 3000
```

Then open [http://localhost:3000](http://localhost:3000) in your browser. (Alternatively, opening `frontend/index.html` directly or opening the root directory will automatically route to the frontend app).

The top header will display the live green badge:
`[●] Backend: Connected (MySQL)` confirming active integration with the Spring Boot server!

---

## REST API Documentation & Swagger

Interactive OpenAPI / Swagger UI is available at:
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Docs:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### API Endpoints Summary

| Module | Method | Endpoint | Description |
| :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/v1/auth/login` | Login and obtain JWT Bearer token |
| | `GET` | `/api/v1/auth/me` | Current authenticated user details |
| **Dashboard** | `GET` | `/api/v1/dashboard` | Aggregated plant telemetry & sales metrics |
| **Customers** | `GET` | `/api/v1/customers` | Paginated customer list with search |
| | `GET` | `/api/v1/customers/{id}` | Customer by ID |
| | `POST` | `/api/v1/customers` | Register customer |
| | `PUT` | `/api/v1/customers/{id}` | Update customer |
| | `DELETE` | `/api/v1/customers/{id}` | Deactivate customer |
| **Price Master** | `GET` | `/api/v1/prices` | Paginated price records with gauge/type filters |
| | `GET` | `/api/v1/prices/lookup` | Auto-rate lookup by thickness, type, size |
| | `POST` | `/api/v1/prices` | Add price record |
| | `PUT` | `/api/v1/prices/{id}` | Update price record |
| **Inventory** | `GET` | `/api/v1/inventory` | Paginated finished goods stock list |
| | `POST` | `/api/v1/inventory/adjust` | Manual stock adjustment (increments/decrements) |
| | `GET` | `/api/v1/inventory/{id}/movements` | Stock audit ledger |
| **Invoices** | `GET` | `/api/v1/invoices` | Paginated tax invoices list |
| | `POST` | `/api/v1/invoices` | Create and finalize GST tax invoice with stock deduction |
| | `POST` | `/api/v1/invoices/{id}/cancel` | Cancel invoice (restores stock & ledger) |
| **Payments** | `GET` | `/api/v1/payments` | Paginated customer payments |
| | `POST` | `/api/v1/payments` | Record payment against invoice/account |
| **Employees** | `GET` | `/api/v1/employees` | Paginated plant employee roster |
| | `POST` | `/api/v1/employees` | Add staff member |
| **Attendance** | `GET` | `/api/v1/attendance` | Paginated shift punch logs |
| | `POST` | `/api/v1/attendance/check-in` | Punch in / mark daily attendance |
| | `POST` | `/api/v1/attendance/check-out/{empId}` | Punch out & calculate working hours |
| | `GET` | `/api/v1/attendance/summary` | Today's present/late/leave counts |
| **Audit Logs** | `GET` | `/api/v1/audit` | Paginated administrative mutation trail |
