# Time Sheet System (TSS) – Project Description

## Project Overview

The **Time Sheet Management System** is a web-based enterprise application developed using **Java** to digitalize the management of employee time sheets at a university. The system replaces paper forms and spreadsheets with a secure, centralized platform that manages employment contracts, working hours, approvals, reporting, and archival of time sheets.

The application is intended for university student assistants, supervisors, assistants, secretaries, and administrators. It ensures compliance with German minimum wage regulations by recording working hours, vacation, and sick leave while providing an efficient workflow for reviewing, signing, and archiving time sheets.

---
# Setup

## Prerequisites

-   Java 21+
-   Maven 3.8+
-   Node.js 14.18+ with npm
-   PostgreSQL (or Docker)
-   Docker & Docker Compose (recommended)

## Start Infrastructure

``` bash
docker-compose up -d postgres mailhog
```

Services: - PostgreSQL: `localhost:5432` - MailHog UI:
`http://localhost:8025`

## Backend

``` bash
mvn clean package -DskipTests
java -jar target/TimeSheet_Maangement_System-1.0.0.jar
```

Backend URL: - http://localhost:8080

## Frontend

``` bash
cd frontend
npm install
ng serve --port 4200
```

Frontend URL: - http://localhost:4200

## Default Login

-   Username: `admin`
-   Password: `password123`

## Important Configuration

### Database

``` properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tssdb
spring.datasource.username=postgres
spring.datasource.password=<password>
```

### JWT

``` properties
jwt.secret=<minimum-42-character-secret>
jwt.expiration-ms=86400000
```

### Mail

``` properties
spring.mail.host=localhost
spring.mail.port=1025
```

## Verify

-   Frontend opens at `http://localhost:4200`
-   Backend responds at `http://localhost:8080`
-   MailHog opens at `http://localhost:8025`
-   Login succeeds with the default credentials.
