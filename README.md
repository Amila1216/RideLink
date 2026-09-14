# RideLink

RideLink is a backend microservices project developed for the IT3130 Application Development group assignment.

## Team Members

| Member | Microservice |
|---|---|
| Member 1 | Account Service |
| Member 2 | Driver & Vehicle Service |
| Member 3 | Ride Management Service |
| Member 4 | Fare & Payment Service |

## Technology Stack

- Java
- Spring Boot
- MongoDB Atlas
- Maven
- Postman
- GitHub

## Microservices

### Account Service
Responsible for:
- Passenger and driver registration
- Login and token issuance
- Role management
- Profile management
- Account status management

Port: `8081`

### Driver & Vehicle Service
Port: `8082`

### Ride Management Service
Port: `8083`

### Fare & Payment Service
Port: `8084`

## Project Structure

RideLink/
- account-service/
- driver-service/
- ride-service/
- fare-payment-service/

## Account Service - Current Endpoints

### Register Account

POST `/api/accounts/register`

Example request:

```json
{
  "name": "Test Passenger",
  "email": "passenger@example.com",
  "password": "ExamplePassword123",
  "phone": "0771234567",
  "role": "PASSENGER"
}
