# FAQ Service: Enterprise Knowledge Base & Onboarding Platform

![Java](https://img.shields.io/badge/Java-21-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)
![Spring Security](https://img.shields.io/badge/Spring%20Security-3.3.5-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17.5-informational.svg)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green.svg)
![Redis](https://img.shields.io/badge/Redis-Cache-orange.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## Overview

**FAQ Service** is a high-performance, scalable, and secure backend service designed to power internal knowledge bases and employee onboarding experiences. This enterprise-grade solution enables organizations to centralize Frequently Asked Questions (FAQs), drastically reducing onboarding time, improving information discoverability, and empowering employees with instant access to critical knowledge.

---

## ⚠️ Legal Disclaimer & Notice

This project, **FAQ Service**, is a demonstration artifact and a Minimum Viable Product (MVP).

*   **It is not a commercial product** and is not affiliated with, endorsed by, or a copy of any proprietary system from any existing company.
*   The codebase has been developed as a generic solution to a common business need and **does not contain any proprietary logic, data, or intellectual property** from any specific organization.
*   This project is presented "as-is" for the purposes of evaluation, demonstration, and as a potential starting point for custom development. It may require further hardening, customization, and scaling to meet specific production requirements.

---

## Key Features

*   **🔐 Robust Authentication & Authorization:** Secure JWT-based authentication with role-based access control (Admin/User).
*   **⚡ Multi-Engine Search:** Experience and compare the performance of two distinct search methodologies:
    *   **PostgreSQL GIN Index:** Traditional full-text search using PostgreSQL's powerful GIN indexes.
    *   **MongoDB + Trie + Redis:** A custom-built, ultra-fast prefix-based search algorithm for instant autocomplete and keyword lookup.
*   **📊 Admin Management Portal:** Full CRUD operations for managing FAQs and users through a RESTful API.
*   **🔄 Automated Synchronization:** Scheduled jobs (using ShedLock) to keep search indices and databases in sync.
*   **📚 API First:** Comprehensive OpenAPI 3.0 documentation for easy integration with frontend applications (web, mobile, internal tools).
*   **🐳 Docker Ready:** Containerized for easy deployment and scaling in modern cloud environments.

---

## 🛠 Technology Stack

This project is built with a cutting-edge suite of technologies to ensure performance, reliability, and developer happiness.

| Layer | Technology                                          |
| :--- |:----------------------------------------------------|
| **Framework** | Spring Boot 3.5.5, Spring Security, Spring Data     |
| **Language** | Java 21                                             |
| **Database** | PostgreSQL 17.5 (Primary), MongoDB (Document Store) |
| **Caching** | Redis                                               |
| **Search** | PostgreSQL GIN, MongoDB Custom In-Memory Trie       |
| **Auth** | JWT-authentication, base64 auth                     |
| **API Docs** | Springdoc OpenAPI 2.8.0                             |
| **Task Scheduling** | ShedLock                                            |
| **Database Migration** | Flyway                                              |
| **Mapping** | MapStruct                                           |
| **Logging** | Logback with Logstash encoder                       |
| **Code Quality** | Checkstyle, Lombok                                  |
| **Packaging** | Docker                                              |

---

## 🚦 Getting Started

### Prerequisites

*   Java 21
*   Maven 3.6+
*   Docker & Docker Compose (Recommended)
*   PostgreSQL 17+
*   MongoDB 7+
*   Redis

### Installation & Deployment

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/ShilovVyacheslav/faq-service.git
    cd faq-service
    ```

2. **Manual Setup (Alternative):**
    *   Configure your databases and update the `application-Local.yml` file with your connection strings.
    *   Build and run the application:
    ```bash
    ./mvnw clean package
    java -jar target/faq-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=Local
    ```

---

## 🔍 Exploring the API & Search Demo

### 1. API Documentation (Swagger UI)
Once the application is running, interact with the full API specification via Swagger UI:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

You can authenticate, retrieve a JWT token, and test all available endpoints for FAQ and user management in Postman.

### 2. Search Performance Demo
This project includes a dedicated page to visually compare the performance and results of the different search implementations.
👉 **[http://localhost:8080/search.html](http://localhost:8080/search.html)**

Enter keywords and observe the response times and results from PostgreSQL and MongoDB with the Trie index and cached responses.

---

## 🤝 Contributing

This is an MVP. Contributions, issues, and feature requests are welcome! Feel free to check the issues page.

---

## 💡 Why This Project?

Every company, regardless of size, struggles with knowledge silos and inefficient onboarding. This service provides a ready-to-deploy, technologically advanced solution to this universal problem. It's not just a FAQ system; it's a productivity multiplier engineered for the modern enterprise.

**Ready to accelerate your onboarding and unlock your company's knowledge?** This codebase is the perfect foundation.