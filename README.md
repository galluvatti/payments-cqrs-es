# Payments Command API Application

This is a **Command API** application built using the following technology stack:

- **Kotlin**
- **JDK 17**
- **Spring Boot 3.4.1**
- **Spring Kafka**
- **Spring Web**
- **Spring Data MongoDB**
- **Gradle** (build system)

The application interacts with external services like Apache Kafka, MongoDB, MySQL, and Zookeeper, which are orchestrated using Docker Compose.

---

## Features

- Implements a **CQRS (Command Query Responsibility Segregation)** architecture.
- Uses **Event Sourcing** to persist events in MongoDB.
- Publishes domain events to **Apache Kafka** for asynchronous communication.
- Provides REST APIs for handling commands.

---

## Prerequisites

Before running the application, ensure you have the following installed:

1. **Docker** and **Docker Compose** (to run external services).
2. **JDK 17** (for running the application).
3. **Gradle** (for building the project).

---

## Getting Started


### 1. Start External Services

Use the provided `docker-compose.yml` file to start the required services:

```sh
docker-compose up -d
```

This will start the following services:
- Zookeeper (port: `2181`)
- Kafka (port: `9092`)
- MongoDB (port: `27017`)
- MySQL (port: `3306`)


### 2. Build and Run the Application

To build and run the application, use Gradle:

```sh
./gradlew clean build
./gradlew bootRun
```

The application will start on port `5001` by default. You can eventually change it from the application.yml

---

## API Endpoints Testing

Please use the Postman collection included in this codebase


---

## External Services Overview

### Kafka + Zookeeper
Kafka is used as an event bus for publishing domain events. Zookeeper is required for managing Kafka brokers.

### MongoDB
MongoDB serves as the event store for persisting domain events and snapshots.

### MySQL
MySQL is used as the read database in the CQRS architecture to store query-side projections.

---



