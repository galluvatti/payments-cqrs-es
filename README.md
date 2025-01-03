# CQRS and Event Sourcing with Spring Boot and Kafka
This is a sample CQRS and Event Sourcing application that simulates a credit card payment lifecycle: Authorization, Capture and Refund.

It is composed of two separate modules, one that implement the Command side and one that implements the Query side, each one with its own database.

Tech Stack used:

- **Kotlin**
- **JDK 17**
- **Spring Boot 3.4.1**
- **Spring Kafka**
- **Spring Web**
- **Spring Data MongoDB**
- **Spring Data JPA**
- **Gradle** (build system)

The application interacts with external services like Apache Kafka, MongoDB, MySQL, 
and Zookeeper, which are orchestrated using Docker Compose.


## Payments Gateway Command API Application

---

### Features

- Implements a **CQRS (Command Query Responsibility Segregation)** architecture.
- Uses **Event Sourcing** to persist events in MongoDB.
- Publishes domain events to **Apache Kafka** for asynchronous communication.
- Provides REST APIs for handling commands.

---

## Payments Query API Application

### Features

- Implements a **CQRS (Command Query Responsibility Segregation)** architecture.
- Listen to Domain Events from **Apache Kafka** and use them to update Payment entity
- Uses MySQL as read database
- Provides REST APIs for handling queries.

---

## External Services Overview

### Kafka + Zookeeper
Kafka is used as an event bus for publishing domain events. Zookeeper is required for managing Kafka brokers.

### MongoDB
MongoDB serves as the event store for persisting domain events and snapshots.

### MySQL
MySQL is used as the read database in the CQRS architecture to store query-side projections.

---
## Getting Started

### Prerequisites

Before running the application, ensure you have the following installed:

1. **Docker** and **Docker Compose** (to run external services).
2. **JDK 17** (for running the application).
3. **Gradle** (for building the project). You can use anyway the Gradle wrapper inside each application if you prefer to not install it

---

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


### 2. Build and Run the Applications

To build and run the applications, you can use Gradle or the Gradle wrapper provided inside each application.

To start Command application:

```sh
cd paymentgateway
./gradlew clean build
./gradlew bootRun
```

The application will start on port `5001` by default. You can eventually change it from the application.yml

To start Query application:

```sh
cd payments-query
./gradlew clean build
./gradlew bootRun
```

The application will start on port `5002` by default. You can eventually change it from the application.yml

---

## API Endpoints Testing

Please use the Postman collections, both for Command API and Query API

---






