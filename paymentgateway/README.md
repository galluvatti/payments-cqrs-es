# Payments Gateway Command API Application

This is a **Command API** application built using the following technology stack:

- **Kotlin**
- **JDK 17**
- **Spring Boot 3.4.1**
- **Spring Kafka**
- **Spring Web**
- **Spring Data MongoDB**
- **Gradle** (build system)

The application interacts with external services like Apache Kafka, MongoDB, MySQL, and Zookeeper, which are orchestrated using Docker Compose.
See README.md in the root of the repository
---

## Use Cases

**Use Case 1**

A merchant wants to authorize a customer credit card for a specific amount and currency. The system must perform some basic fraud checks (no need to call external services): customer email or pan must not be presents in our blacklist. The merchant who want to perform a payment must also provide cardholder data and customer order basic info such as the id of the order and a short description of the good that is being paid. We want to track both successfull authorized payment and failed ones.

**Use Case 2**

Given an authorized payment, a merchant wants to capture a specific amount (this is usually the case for physical goods, when merchant authorize the amount on order confirmation and then capture the amount when the good is shipped from warehouse). The amount to be captured can be equal or less than the authorized amount. This is also the moment where we calculate payment fees for the merchant. For simplicity we can consider that we individually negotiated, with each merchant, a fixed % fee.

**Use Case 3** 

Given a captured payment, a merchant wants to refund a customer for a specific amount. The refund amount can be equal or less than the captured amount. By company policy, refund is possible only if no more than 30 days have passed from the capture.

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

The application will start on port `5001` by default. You can eventually change it from the application.yml

---

## API Endpoints Testing

Please use the Postman collection included in this codebase


---


