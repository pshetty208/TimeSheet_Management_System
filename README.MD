# Time Sheet Management System

A student project for managing employee contracts, working hours, timesheet approvals, reminders, printing, and archiving.

## Tech stack

- Java 21, Spring Boot, Spring Security, Spring Data JPA
- Angular and TypeScript
- PostgreSQL
- Maven, npm, Docker Compose

## Run

Requirements: Java 21, Maven, Node.js with npm, and Docker.

```bash
docker compose up -d postgres mailhog
mvn spring-boot:run
```

In another terminal:

```bash
cd frontend
npm install
npm start
```

Open `http://localhost:4200` and sign in with username `admin` and password `password123`.
