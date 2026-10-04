# RideLink

RideLink is a backend-only ride-sharing platform developed using a microservices architecture with Java, Spring Boot, MongoDB, REST APIs, JWT-based security, Swagger/OpenAPI, Postman, automated testing, and GitHub Actions CI.

The system is divided into four independently executable core microservices:

1. Account Service
2. Driver & Vehicle Service
3. Ride Management Service
4. Fare & Payment Service

Each service is responsible for a separate business capability and maintains its own data without directly accessing another service's database.

---

## Project Overview

RideLink provides the main backend workflow required for a ride-sharing platform.

The system supports:

- Passenger and driver account registration
- Secure authentication using JWT
- User profile and account management
- Driver profile and vehicle management
- Driver availability and service-area management
- Eligible driver discovery
- Ride creation and driver assignment
- Ride lifecycle management
- Fare estimation and final fare calculation
- Simulated payment processing
- Payment status tracking and receipt generation
- Swagger/OpenAPI documentation
- Postman API testing
- Unit and integration testing
- GitHub Actions continuous integration

---

## Microservices Architecture

RideLink follows a microservices architecture in which each service can be developed, tested, and executed independently.

```text
                   API Consumers
          Passenger / Driver / Admin
                       |
        ---------------------------------
        |               |               |
     Swagger          Postman        REST Clients
        |
        v
+-------------------+
|  Account Service  |
| Authentication    |
| Profiles / Roles  |
+-------------------+
        |
     Account DB
     MongoDB


+--------------------------+
| Driver & Vehicle Service |
| Drivers / Vehicles       |
| Availability / Location  |
+--------------------------+
        |
 Driver & Vehicle DB
       MongoDB


+-------------------------+
| Ride Management Service |
| Ride Requests           |
| Driver Assignment       |
| Ride Lifecycle          |
+-------------------------+
        |
      Ride DB
      MongoDB


+------------------------+
| Fare & Payment Service |
| Fare Calculation       |
| Payments / Receipts    |
+------------------------+
        |
 Fare & Payment DB
      MongoDB
```

### Architecture Principles

- Services are independently executable.
- Each service owns its own data.
- Services do not directly access another service's database.
- REST/JSON APIs are used for communication.
- Swagger/OpenAPI documents the service APIs.
- Environment variables are used for secrets and sensitive configuration.
- JWT-based authentication is used for protected APIs where configured.

---

## Repository Structure

```text
RideLink/
│
├── .github/
│   └── workflows/
│
├── account-service/
│
├── driver-vehicle-service/
│
├── ride-service/
│
├── fare-payment-service/
│
├── postman/
│
└── README.md
```

---

# 1. Account Service

The Account Service manages user registration, authentication, profile management, account roles, and account status.

## Main Features

- Passenger and driver registration
- BCrypt password hashing
- JWT-based authentication
- User profile retrieval
- User profile updates
- Role-based access control
- Admin-only role management
- Admin-only account status management
- Suspended and disabled account login prevention
- Swagger/OpenAPI documentation
- Unit testing with JUnit and Mockito

## Service Port

```text
8081
```

Base URL:

```text
http://localhost:8081
```

## API Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/accounts/register` | Public | Register a passenger or driver |
| POST | `/api/accounts/login` | Public | Authenticate an account and receive a JWT |
| GET | `/api/accounts/me` | Authenticated | Retrieve current account profile |
| PUT | `/api/accounts/me` | Authenticated | Update current account profile |
| PATCH | `/api/accounts/{id}/role` | ADMIN | Update account role |
| PATCH | `/api/accounts/{id}/status` | ADMIN | Update account status |

## Supported Roles

```text
PASSENGER
DRIVER
ADMIN
```

Public registration only allows `PASSENGER` and `DRIVER`.

## Account Status Values

```text
ACTIVE
SUSPENDED
DISABLED
```

Only accounts with `ACTIVE` status can successfully log in.

## Environment Configuration

Create a local `.env` file inside:

```text
account-service/
```

Example:

```properties
MONGODB_URI=<your-mongodb-connection-string>
JWT_SECRET=<your-jwt-secret>
```

Do not commit real credentials or secrets to GitHub.

## Run Account Service

Windows:

```bash
cd account-service
mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
cd account-service
./mvnw spring-boot:run
```

## Swagger

Swagger UI:

```text
http://localhost:8081/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8081/v3/api-docs
```

## Testing

Run:

```bash
mvnw.cmd test
```

Verified Account Service test result:

```text
Tests run: 10
Failures: 0
Errors: 0
BUILD SUCCESS
```

---

# 2. Driver & Vehicle Service

The Driver & Vehicle Service manages driver profiles, vehicles, availability, simulated location information, service areas, and eligible-driver discovery.

## Main Features

- Create and update driver profiles
- Retrieve driver information
- Manage driver availability
- Manage driver service areas
- Store simulated driver location information
- Register vehicles
- Update and retrieve vehicle information
- Search for eligible drivers
- Swagger/OpenAPI documentation
- MongoDB persistence
- Validation and exception handling

## Service Port

```text
8083
```

Base URL:

```text
http://localhost:8083
```

## Main API Routes

```text
GET  /
POST /api/v1/drivers
GET  /api/v1/drivers/{driverId}
PUT  /api/v1/drivers/{driverId}

PUT  /api/v1/drivers/{driverId}/availability
GET  /api/v1/drivers/{driverId}/availability

PUT  /api/v1/drivers/{driverId}/service-area

GET  /api/v1/drivers/eligible

POST /api/v1/drivers/{driverId}/vehicles
GET  /api/v1/drivers/{driverId}/vehicles/{vehicleId}
```

Additional endpoints and exact request/response models are available through Swagger/OpenAPI.

## Eligible Driver Search

The service provides an eligible-driver API that can be used by the Ride Management Service when searching for an appropriate driver and vehicle.

Example:

```bash
curl "http://localhost:8083/api/v1/drivers/eligible?serviceArea=Colombo&requireLocation=true"
```

## Driver Availability

Example availability values include:

```text
AVAILABLE
```

Driver availability can be changed through the driver availability endpoint.

## Environment Configuration

Local sensitive configuration must be stored outside source control.

Typical environment variables include:

```properties
MONGODB_URI=<your-mongodb-connection-string>
AUTH_ADMIN_USERNAME=<local-admin-username>
AUTH_ADMIN_PASSWORD=<local-admin-password>
JWT_SECRET=<your-jwt-secret>
```

Do not commit `.env` files or real credentials.

## Run Driver & Vehicle Service

```bash
cd driver-vehicle-service
mvn spring-boot:run
```

If the Maven Wrapper is available:

```bash
mvnw.cmd spring-boot:run
```

## Swagger

Swagger UI:

```text
http://localhost:8083/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8083/v3/api-docs
```

Additional eligible-driver contract documentation is available in:

```text
driver-vehicle-service/ELIGIBLE-DRIVER-API.md
```

---

# 3. Ride Management Service

The Ride Management Service manages the main ride workflow of the RideLink platform.

## Main Responsibilities

- Create ride requests
- Store passenger and ride information
- Assign drivers to rides
- Manage ride status transitions
- Track the ride lifecycle
- Retrieve ride information and ride history
- Communicate with other RideLink services when required

## Ride Lifecycle

The expected ride workflow is:

```text
REQUESTED
    ↓
ASSIGNED
    ↓
ACCEPTED
    ↓
IN_PROGRESS
    ↓
COMPLETED
```

A ride may also move to:

```text
CANCELLED
```

depending on the supported workflow and validation rules.

## Inter-Service Role

The Ride Management Service acts as a central coordinator in the ride workflow.

Typical interactions include:

```text
Ride Management Service
        |
        | Search eligible drivers
        v
Driver & Vehicle Service
```

and:

```text
Ride Management Service
        |
        | Ride/fare related information
        v
Fare & Payment Service
```

Refer to the Ride Service Swagger/OpenAPI documentation for the final implemented endpoint paths and request/response models.

## Run Ride Service

```bash
cd ride-service
```

Then use the Maven Wrapper or Maven configuration included with the service:

```bash
mvnw.cmd spring-boot:run
```

or:

```bash
mvn spring-boot:run
```

---

# 4. Fare & Payment Service

The Fare & Payment Service handles fare estimation, final fare calculation, simulated payment processing, payment status tracking, and receipt generation.

## Main Features

- Estimate ride fare
- Calculate final ride fare
- Validate ride status before final fare calculation
- Process simulated payments
- Retrieve payment status
- Generate payment receipts
- JWT-based authentication
- Input validation
- Exception handling
- Swagger/OpenAPI documentation
- MongoDB persistence

## Service Port

```text
8084
```

Base URL:

```text
http://localhost:8084
```

## Fare Calculation

The fare calculation uses:

```text
Base Fare     = LKR 100
Distance Rate = LKR 30 per km
Time Rate     = LKR 5 per minute
```

Formula:

```text
Total Fare = Base Fare + Distance Fare + Time Fare
```

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/fares/estimate` | Estimate a ride fare |
| POST | `/api/fares/final` | Calculate the final fare |
| POST | `/api/payments` | Process a simulated payment |
| GET | `/api/payments/ride/{rideId}/status` | Retrieve payment status |
| GET | `/api/payments/ride/{rideId}/receipt` | Retrieve payment receipt |

## Inter-Service Communication

Before calculating a final fare, the Fare & Payment Service communicates with the Ride Service to validate the related ride.

A final fare is calculated only when the ride satisfies the required completion condition.

Example:

```text
Fare & Payment Service
        |
        | Check ride information/status
        v
Ride Management Service
```

## MongoDB Database

```text
ridelink_fare_payment_db
```

## Security Configuration

Sensitive values such as JWT secrets must be supplied through environment configuration.

Example:

```properties
JWT_SECRET=<your-jwt-secret>
```

Real secrets must never be committed to GitHub.

## Run Fare & Payment Service

Windows:

```bash
cd fare-payment-service
mvnw.cmd spring-boot:run
```

or, where Maven is used directly:

```bash
mvn spring-boot:run
```

---

# Inter-Service Communication

RideLink services communicate using REST/JSON interfaces.

The main integration paths are:

```text
Ride Management Service
        |
        | Eligible driver / vehicle search
        v
Driver & Vehicle Service
```

and:

```text
Fare & Payment Service
        |
        | Validate ride information/status
        v
Ride Management Service
```

Services must not directly read or modify another service's MongoDB database.

---

# End-to-End Ride Workflow

A typical RideLink workflow is:

1. A passenger registers through the Account Service.
2. The passenger logs in and receives a JWT.
3. A driver profile and vehicle are configured.
4. The driver becomes available.
5. A passenger requests a fare estimate.
6. The passenger creates a ride request.
7. The Ride Management Service searches for an eligible driver.
8. A driver is assigned to the ride.
9. The ride is accepted.
10. The ride progresses to `IN_PROGRESS`.
11. The ride is completed.
12. The Fare & Payment Service calculates the final fare.
13. A simulated payment is processed.
14. Payment status and receipt information can be retrieved.

---

# Technology Stack

The RideLink project uses technologies including:

- Java
- Spring Boot
- Spring Web
- Spring Security
- JWT
- BCrypt
- Spring Data MongoDB
- MongoDB Atlas
- Spring Validation
- Swagger / OpenAPI
- Springdoc OpenAPI
- Maven
- JUnit
- Mockito
- Postman
- Git
- GitHub
- GitHub Actions

Individual microservices may use different compatible Java and Spring Boot versions.

---

# API Documentation

Each service provides Swagger/OpenAPI documentation where configured.

Known local documentation URLs include:

### Account Service

```text
http://localhost:8081/swagger-ui/index.html
http://localhost:8081/v3/api-docs
```

### Driver & Vehicle Service

```text
http://localhost:8083/swagger-ui.html
http://localhost:8083/v3/api-docs
```

For Ride Management and Fare & Payment services, use the Swagger path configured by the corresponding service when it is running.

---

# Postman

Postman is used for manual API testing, workflow validation, authentication testing, and negative scenario testing.

Postman resources are stored under:

```text
postman/
```

The Account Service collection is available at:

```text
postman/RideLink-Account-Service.postman_collection.json
```

The local Postman environment is available at:

```text
postman/RideLink-Local.postman_environment.json
```

Sensitive variables such as passwords, MongoDB credentials, and JWT tokens must not be exported with real values.

---

# Testing

RideLink uses both unit testing and API-level testing.

Testing activities include:

- Service-layer unit tests
- Controller/API testing
- Authentication tests
- Role-based authorization tests
- Validation tests
- Negative scenario testing
- Postman workflow testing
- Inter-service integration testing
- CI-based automated Maven builds and tests

To test an individual Maven service:

```bash
mvn test
```

or on Windows where the Maven Wrapper is included:

```bash
mvnw.cmd test
```

---

# Security

Security measures implemented across the project include:

- JWT-based authentication
- Role-based access control
- BCrypt password hashing in the Account Service
- Protected administrative operations
- Account status validation
- Environment-based secret management
- MongoDB credentials excluded from source control
- JWT secrets excluded from source control
- `.env` files ignored by Git
- Appropriate HTTP status codes
- Input validation and exception handling

Example private configuration:

```properties
MONGODB_URI=<mongodb-uri>
JWT_SECRET=<strong-random-secret>
```

Never commit actual credentials, JWT secrets, passwords, or active tokens to the repository.

---

# Git Workflow

The team follows a shared Git workflow.

Development is performed using feature branches such as:

```text
feature/...
```

Typical workflow:

```text
Feature Branch
      ↓
Commit
      ↓
Push to GitHub
      ↓
Pull Request
      ↓
Peer Review
      ↓
Merge into develop
      ↓
GitHub Actions CI
      ↓
Final develop → main merge
```

The `develop` branch is used for integrated development before the final stable version is merged into `main`.

---

# Continuous Integration

GitHub Actions is used for continuous integration.

The CI workflow builds and tests the four core services:

```text
Account Service
Driver & Vehicle Service
Ride Management Service
Fare & Payment Service
```

The pipeline verifies that the services can compile and their automated test suites can execute successfully before final integration.

Workflow files are stored under:

```text
.github/workflows/
```

---

# Configuration and Secrets

Sensitive configuration must be supplied through local environment files or deployment environment variables.

Examples include:

```text
MONGODB_URI
JWT_SECRET
AUTH_ADMIN_USERNAME
AUTH_ADMIN_PASSWORD
```

Local `.env` files must remain excluded from Git.

Generated build output such as Maven `target/` directories should not be intentionally committed as source code.

---

# Development Guidelines

When contributing to the project:

- Create a separate feature branch.
- Follow consistent Java naming conventions.
- Keep controller, service, repository, DTO, configuration, and model responsibilities separated.
- Validate API input.
- Use appropriate HTTP response codes.
- Add tests for important business logic.
- Update Swagger/OpenAPI when APIs change.
- Do not commit secrets.
- Use pull requests for integration.
- Perform peer reviews before merging major changes.

---

# Main Project Deliverables

The RideLink repository contains or supports the following assignment deliverables:

- Four core microservices
- REST APIs
- MongoDB persistence
- JWT authentication
- Role-based authorization
- Swagger/OpenAPI documentation
- Postman collections
- Unit tests
- Integration testing
- Negative scenario testing
- GitHub Actions CI
- Architecture documentation
- Sequence diagrams
- Technical report
- Git contribution history

---

# Contributors

RideLink was developed as a group project.

The implementation was divided by microservice ownership:

| Responsibility | Service |
|---|---|
| Account and Authentication | Account Service |
| Driver and Vehicle Management | Driver & Vehicle Service |
| Ride Workflow | Ride Management Service |
| Fare and Payment Processing | Fare & Payment Service |

All group members contributed through feature branches, commits, pull requests, testing, reviews, documentation, and integration activities.

---

# License / Academic Use

This repository was developed for academic coursework and demonstration purposes.

It is not intended to be used as a production ride-sharing platform without additional security, infrastructure, monitoring, scalability, deployment, and compliance work.