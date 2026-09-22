<div align="center">

# FAQ Service

**Enterprise Knowledge Base & Onboarding Platform**

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9.11-C71A36?logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-4.4-47A248?logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.0-DC382D?logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker%20Compose-2496ED?logo=docker&logoColor=white)
![CI](https://github.com/ShilovVyacheslav/faq-service/actions/workflows/ci.yml/badge.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

</div>

---

## | Overview

**FAQ Service** is a Spring Boot backend for an internal knowledge base: JWT-secured REST API with role-based access control, PostgreSQL as the system of record, and a scheduled sync job that mirrors FAQ data into MongoDB for a second, independently-built search path — a custom Trie index cached in Redis, run side by side with PostgreSQL's native full-text search so the two approaches can be compared directly.

---

## | Performance

Load-tested the Trie + Redis search path against a 10,000-document dataset: **p50 1.5ms**, 95.7% cache hit rate. Full analysis: [docs/load-test-analysis.md](docs/load-test-analysis.md).

---

## | Getting Started

**API Documentation: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

**Default Admin Account**

- Username: `admin`
- Password: `@Password0`

**Clone the repository:**
```bash
git clone https://github.com/ShilovVyacheslav/faq-service.git
cd faq-service
```

---

<details>
<summary><b>Option 1: Docker (recommended)</b></summary>

<br>

### Option 1: Docker (recommended)

<table>
<colgroup><col style="width: 50%"><col style="width: 50%"></colgroup>
<tr><th>bash / zsh / sh</th><th>PowerShell</th></tr>
<tr><td>

```bash
cp .env.example .env.docker
```

</td><td>

```bash
Copy-Item .env.example .env.docker
```

</td></tr>
</table>

```bash
docker compose --env-file .env.docker up --build -d
```

The app is available at `http://localhost:8080` once all health checks pass.

**Shut down:**

```bash
docker compose --env-file .env.docker down
```

</details>

---

<details>
<summary><b>Option 2: Manual</b></summary>

<br>

### Option 2: Manual

Configure your databases and update the `application-local.yml` file with your connection credentials and run the application:

<table>
<tr><td><strong>bash / zsh / sh</strong></td>
<td>

```bash
chmod +x ./mvnw
./mvnw clean package
java -jar target/faq-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

</td></tr>
<tr><td><strong>PowerShell / cmd</strong></td>
<td>

```bash
.\mvnw.cmd clean package
java -jar target/faq-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

</td></tr>
</table>

</details>

---

## | Features

*   **Authentication & Authorization:** Basic Auth for login, then JWT for all subsequent requests, with role-based access control (Admin/User).
*   **Two search implementations:**
    *   **PostgreSQL GIN Index:** Traditional full-text search using PostgreSQL's powerful GIN indexes.
    *   **MongoDB + Trie + Redis:** A custom-built prefix-based search algorithm for instant responses.
*   **Admin API:** Full CRUD operations for managing FAQs and users via REST.
*   **Scheduled sync:** ShedLock-coordinated jobs keep both search backends in sync.
*   **Containerized:** one docker-compose spins up the app plus Postgres, MongoDB, and Redis.

---

<details>
<summary><b>Exploring the API</b></summary>

### Search Demo
**[http://localhost:8080/search.html](http://localhost:8080/search.html)**

Enter keywords and observe the response times and results from PostgreSQL and MongoDB with the Trie index and cached responses.

<img src="src/main/resources/static/images/search.png">

### Test Coverage
Unit tests cover core business logic, services, and utilities.

<img src="src/main/resources/static/images/tests.png">

</details>

---

## | Disclaimer

This project, **FAQ Service**, is a demonstration artifact and a Minimum Viable Product (MVP).

*   **It is not a commercial product** and is not affiliated with, endorsed by, or a copy of any proprietary system from any existing company.
*   The codebase has been developed as a generic solution to a common business need and **does not contain any proprietary logic, data, or intellectual property** from any specific organization.
*   This project is presented "as-is" for the purposes of evaluation, demonstration, and as a potential starting point for custom development. It may require further hardening, customization, and scaling to meet specific production requirements.