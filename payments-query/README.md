# Payments Query API Application

This is a **Query API** application built using the following technology stack:

- **Kotlin**
- **JDK 17**
- **Spring Boot 3.4.1**
- **Spring Kafka**
- **Spring Web**
- **Spring JPA**
- **Gradle** (build system)

The application interacts with external services like Apache Kafka, MySQL, and Zookeeper, which are orchestrated using Docker Compose.
See README.md in the root of the repository
---

## Use Cases

**Use Case 1**

I want to lookup for all payments, successfully captured, by merchant id.

**Use Case 2**

I want to lookup for all payments, succesfully authorized, with an authorization amount greater than or less than a specified value.

---

## Getting Started


### 1. Start External Services

Use the provided `docker-compose.yml` file in the root to start the required services:

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

The application will start on port `5002` by default. You can eventually change it from the application.yml

---

## API Endpoints Testing

Please use the Postman collection included in this codebase


---


