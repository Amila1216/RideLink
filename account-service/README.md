# RideLink Account Service

The Account Service manages user registration, authentication, profile management, account roles, and account status for the RideLink microservices application.

## Features

- Passenger and driver account registration
- Secure password hashing using BCrypt
- JWT-based authentication
- Authenticated profile retrieval and profile updates
- Role-based access control
- Admin-only account role management
- Admin-only account status management
- Suspended and disabled account login prevention
- Swagger/OpenAPI API documentation
- Unit testing with JUnit and Mockito

## Technology Stack

- Java 17
- Spring Boot 4.1.1
- Spring Security
- Spring Data MongoDB
- MongoDB Atlas
- JWT
- Maven
- Springdoc OpenAPI
- JUnit 5
- Mockito
- Postman

## Service Port

The Account Service runs on:

```text
http://localhost:8081
```

## Prerequisites

Before running the service, install:

- Java 17 or later
- Maven or use the included Maven Wrapper
- MongoDB Atlas or another MongoDB instance
- Postman for API testing

## Environment Configuration

Create a `.env` file inside the `account-service` directory.

Example:

```properties
MONGODB_URI=<your-mongodb-connection-string>
JWT_SECRET=<your-jwt-secret>
```

Do not commit the `.env` file or real credentials to GitHub.

## Run the Service

From the `account-service` directory:

```bash
mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

## Run Tests

Windows:

```bash
mvnw.cmd test
```

macOS or Linux:

```bash
./mvnw test
```

The Account Service test suite includes application context testing and meaningful unit tests for registration, authentication, role management, status management, and profile updates.

Current verified result:

```text
Tests run: 10
Failures: 0
Errors: 0
BUILD SUCCESS
```

## Swagger / OpenAPI

After starting the service, Swagger UI is available at:

```text
http://localhost:8081/swagger-ui/index.html
```

OpenAPI JSON documentation:

```text
http://localhost:8081/v3/api-docs
```

Swagger supports JWT Bearer authentication for protected endpoints.

## API Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/accounts/register` | Public | Register a passenger or driver |
| POST | `/api/accounts/login` | Public | Authenticate an account and receive a JWT |
| GET | `/api/accounts/me` | Authenticated | Get the current account profile |
| PUT | `/api/accounts/me` | Authenticated | Update the current account profile |
| PATCH | `/api/accounts/{id}/role` | ADMIN | Update an account role |
| PATCH | `/api/accounts/{id}/status` | ADMIN | Update an account status |

## Supported Roles

- `PASSENGER`
- `DRIVER`
- `ADMIN`

Public registration only allows `PASSENGER` and `DRIVER`.

## Supported Account Status Values

- `ACTIVE`
- `SUSPENDED`
- `DISABLED`

Only accounts with `ACTIVE` status can log in.

## Authentication

Protected endpoints require a JWT Bearer token.

Example header:

```text
Authorization: Bearer <JWT_TOKEN>
```

Admin-only endpoints require a token belonging to an account with the `ADMIN` role.

## Postman Collection

The Account Service Postman collection is available at:

```text
postman/RideLink-Account-Service.postman_collection.json
```

The non-sensitive local environment file is available at:

```text
postman/RideLink-Local.postman_environment.json
```

The environment uses the following variables:

```text
account_base_url
test_email
test_password
account_token
admin_token
account_id
```

Sensitive variables such as JWT tokens and passwords are intentionally left empty in the exported environment.

## Security Notes

- Passwords are stored using BCrypt hashing.
- JWT authentication is used for protected endpoints.
- Role and status management endpoints require the `ADMIN` role.
- Suspended and disabled accounts cannot log in.
- MongoDB credentials and JWT secrets are loaded from environment configuration and must not be committed to source control.