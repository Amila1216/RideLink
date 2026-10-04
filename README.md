# RideLink

RideLink is a Java/Spring Boot project focused on a driver and vehicle management service for a ride-sharing platform. The repository currently contains the core implementation for the Driver & Vehicle service, along with a few related folders that appear to be incomplete or duplicated work in progress.

## Project status

The active, working service in this repository is:

- driver-vehicle-service/

The following folders currently look incomplete or are not active application sources:

- ride-service/
- ride-service 2/
- account-service/

## Repository structure

```text
RideLink/
├── README.md
├── account-service/
│   └── target/
├── driver-vehicle-service/
│   ├── data/
│   ├── src/
│   ├── target/
│   ├── ELIGIBLE-DRIVER-API.md
│   └── pom.xml
├── ride-service/
│   └── ride-service/
│       └── target/
└── ride-service 2/
    └── ride-service/
        └── src/
```

## Main service: Driver & Vehicle Service

The Driver & Vehicle service is the implemented Spring Boot application in this repository. It exposes driver and vehicle APIs, manages availability and service area info, and supports the ride-matching flow that searches for eligible drivers.

### Key capabilities

- Create and update driver profiles
- Manage driver availability and service area
- Add, update, and retrieve vehicles for a driver
- Search eligible drivers for a specified service area
- Expose Swagger/OpenAPI documentation
- Store driver and vehicle data in MongoDB

### Core endpoints

The application runs on port 8083 and uses the base path:

- http://localhost:8083

Main routes include:

- GET /
- POST /api/v1/drivers
- GET /api/v1/drivers/{driverId}
- PUT /api/v1/drivers/{driverId}
- PUT /api/v1/drivers/{driverId}/availability
- GET /api/v1/drivers/{driverId}/availability
- PUT /api/v1/drivers/{driverId}/service-area
- GET /api/v1/drivers/eligible
- POST /api/v1/drivers/{driverId}/vehicles
- GET /api/v1/drivers/{driverId}/vehicles/{vehicleId}

### API examples

The implemented service is the Driver & Vehicle service under [driver-vehicle-service](driver-vehicle-service). The following examples assume the app is running locally on port 8083.

#### 1) Create a driver

```bash
curl -X POST http://localhost:8083/api/v1/drivers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Johnson",
    "email": "alice.johnson@example.com",
    "phoneNumber": "+1234567890",
    "licenseNumber": "DL-1001",
    "driverAvailability": "AVAILABLE"
  }'
```

#### 2) Add a vehicle to a driver

```bash
curl -X POST http://localhost:8083/api/v1/drivers/1/vehicles \
  -H "Content-Type: application/json" \
  -d '{
    "make": "Toyota",
    "model": "Corolla",
    "year": 2022,
    "licensePlate": "ABC-123",
    "color": "Silver",
    "vehicleType": "SEDAN",
    "registrationNumber": "REG-1001"
  }'
```

#### 3) Search eligible drivers

```bash
curl "http://localhost:8083/api/v1/drivers/eligible?serviceArea=Colombo&requireLocation=true"
```

#### 4) Update driver availability

```bash
curl -X PUT http://localhost:8083/api/v1/drivers/1/availability \
  -H "Content-Type: application/json" \
  -d '{
    "status": "AVAILABLE"
  }'
```

#### 5) Get driver details

```bash
curl http://localhost:8083/api/v1/drivers/1
```

#### 6) Health/root endpoint

```bash
curl http://localhost:8083/
```

> Other folders in this repository, such as [account-service](account-service), [ride-service](ride-service), and [ride-service 2](ride-service%202), do not currently contain runnable application source code in this workspace state, so no concrete API examples are available for them yet.

### API documentation

Swagger UI is enabled at:

- http://localhost:8083/swagger-ui.html

OpenAPI JSON is available at:

- http://localhost:8083/v3/api-docs

Detailed driver eligibility service contract documentation is included in:

- driver-vehicle-service/ELIGIBLE-DRIVER-API.md

## Technology stack

- Java 21
- Spring Boot 3.3.3
- Spring Web
- Spring Data MongoDB
- MongoDB Atlas
- Spring Validation
- Springdoc OpenAPI
- Maven

## Configuration

The application configuration is defined in:

- driver-vehicle-service/src/main/resources/application.properties

Important configuration details:

- Application name: driver-vehicle-service
- Server port: 8083
- MongoDB database: ridelink
- MongoDB connection URI: loaded from `driver-vehicle-service/.env` in the local profile
- JWT admin credentials and signing key: loaded from the same ignored local `.env`
- Swagger path: /swagger-ui.html
- OpenAPI docs path: /v3/api-docs

The local profile imports `driver-vehicle-service/.env`, which is excluded from Git. Set `MONGODB_URI` there or provide it through the process environment. Never commit the URI. For example:

```bash
MONGODB_URI=mongodb+srv://<username>:<password>@<cluster-host>/?appName=Cluster0
```

The service selects the `ridelink` database. The local `.env` also contains a randomly generated administrator password and Base64 JWT signing key. Keep the file private and do not commit it. The signing key must remain stable across restarts so issued tokens remain verifiable until expiry.

`POST /api/v1/auth/token` accepts the configured administrator username and password as JSON and returns a 15-minute Bearer token. Include that token in `Authorization: Bearer <token>` for the other API routes. All driver and vehicle routes require an administrator token; there is no public account registration.

```bash
curl -X POST http://localhost:8083/api/v1/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"<AUTH_ADMIN_PASSWORD from .env>"}'
```

For deployed environments, set `MONGODB_URI`, `AUTH_ADMIN_USERNAME`, `AUTH_ADMIN_PASSWORD`, and `JWT_SECRET` through the platform's secret manager rather than copying the local `.env`. Rotate the MongoDB password shared in chat in Atlas and update the local `.env`.

## Run the project

From the project root, run:

```bash
cd driver-vehicle-service
mvn spring-boot:run
```

To build the project:

```bash
cd driver-vehicle-service
mvn clean package
```

To run the test suite:

```bash
cd driver-vehicle-service
mvn test
```

## Notes

- This repository is a work in progress and includes both active and partial service folders.
- The most complete and runnable implementation is the Driver & Vehicle service under driver-vehicle-service.
- The ride-service and account-service directories appear to be incomplete or staging folders and may require additional implementation before they can be used as production services.

## Suggested next steps

1. Confirm which service should be treated as the primary RideLink backend.
2. Decide whether ride-service and account-service should be completed or removed from the repo.
3. Add a proper parent Maven project if the team intends to manage multiple microservices in one repository.
4. Standardize naming and folder structure across all services for easier onboarding and deployment.

## Contributors

This project is currently a local Spring Boot service prototype and is not yet fully organized as a multi-service production monorepo.
