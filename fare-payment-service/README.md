# RideLink - Fare & Payment Service

The Fare & Payment Service is one of the microservices in the RideLink backend system. It is responsible for fare estimation, final fare calculation, simulated payment processing, payment status tracking, and receipt generation.

## Features

- Estimate ride fare using distance and estimated duration
- Calculate the final fare using actual distance and duration
- Validate ride status with the Ride Service before final fare calculation
- Process simulated payments
- Retrieve payment status
- Generate payment receipts
- JWT-based authentication
- Input validation and error handling
- Swagger/OpenAPI documentation
- MongoDB persistence

## Fare Calculation

The fare is calculated using:

- Base Fare: LKR 100
- Distance Rate: LKR 30 per km
- Time Rate: LKR 5 per minute

Formula:

`Total Fare = Base Fare + Distance Fare + Time Fare`

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/fares/estimate` | Estimate the ride fare |
| POST | `/api/fares/final` | Calculate the final fare for a completed ride |
| POST | `/api/payments` | Process a simulated payment |
| GET | `/api/payments/ride/{rideId}/status` | Get payment status |
| GET | `/api/payments/ride/{rideId}/receipt` | Get payment receipt |

## Inter-Service Communication

The Fare & Payment Service communicates with the Ride Service before calculating the final fare.

The final fare is calculated only when the related ride has the `COMPLETED` status.

## Technology Stack

- Java
- Spring Boot
- Spring Security
- JWT
- Spring Data MongoDB
- MongoDB
- REST API
- Swagger / OpenAPI
- Maven
- JUnit and Mockito

## Service Configuration

Default service port:

`8084`

MongoDB database:

`ridelink_fare_payment_db`

Required environment variable:

`JWT_SECRET`

The JWT secret must not be committed to GitHub.

## Running the Service

Set the required JWT secret in your local environment and run:

```powershell
.\mvnw spring-boot:run