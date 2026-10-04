# FixIt Campus: Campus Issue Tracker

A web application for reporting and resolving campus facility problems.
Full documentation: [docs/PROJECT_DOCUMENTATION.md](docs/PROJECT_DOCUMENTATION.md)

## Tech stack
Spring Boot 4 (Java 21), PostgreSQL, MongoDB, RabbitMQ, Flyway, JWT security, React (prototype), Docker Compose.

## Run it locally

Requirements: Git, Java 21, Docker Desktop.

1. Create your environment file and set your own passwords (letters and numbers only):
```
   copy .env.example .env
```
2. Start PostgreSQL, MongoDB, RabbitMQ and Mailpit:
```
   docker compose up -d
```
3. Start the API (Flyway creates the tables on first start):
```
   cd backend
   mvnw spring-boot:run
```
   The API runs on http://localhost:8080. PostgreSQL is mapped to host port 5433.
4. Open the front-end prototype: open `frontend/index.html` in a browser.

Useful pages: RabbitMQ at http://localhost:15672 and Mailpit at http://localhost:8025 (check `docker-compose.yml` for the port).

## Repository layout
```
backend/    Spring Boot API (controller, service, repository, model, security, messaging)
frontend/   Responsive React prototype
docs/       Requirements, data model, architecture, status
```
