# Hotel Booking Service

This is my hotel booking REST API built using Quarkus for the enterprise middleware coursework.

## Author
Shantanu Raj Chaudhary

## What This Project Does

Basically this app lets you manage hotel bookings through a REST API. Here's what it can do:

**Part 1 - Basic CRUD:**
- Create and list customers
- Create and list hotels  
- Make bookings, view them, cancel them
- Validates all the input data (email format, phone numbers, etc.)
- Returns everything as JSON
- Has Swagger UI for testing the API

**Part 2 - Advanced stuff:**
- When you delete a customer, it automatically deletes all their bookings too (cascade delete)
- Guest booking endpoint that creates a customer AND booking in one transaction
- Used JTA for transaction management

**Part 3 - Travel Agent:**
- Can book hotel + taxi + flight all at once
- Makes REST calls to external services
- If any booking fails, it rolls back everything (learned this was tricky to implement!)

## Tech Stack

- **Quarkus** - main framework (way faster than Spring Boot from what I tested)
- **JPA/Hibernate** - for database stuff
- **JAX-RS** - REST endpoints
- **Bean Validation** - validates the data
- **JTA** - transaction management
- **H2** - in-memory database (good for testing)
- **REST Assured** - for writing tests
- **Maven** - dependency management
- **Swagger** - API docs (really useful for testing)

## How to Run

**Build the project:**
```bash
mvn clean package
```

**Run in dev mode:**
```bash
mvn quarkus:dev
```

It starts on `http://localhost:8080`

**View the API docs:**

Swagger UI: http://localhost:8080/q/swagger-ui/

You can test all the endpoints from the Swagger UI, super convenient.

**Run tests:**
```bash
mvn test
```

All the tests should pass (they did for me at least)

## Main Endpoints

**Customers:**
- `GET /api/customers` - get all customers
- `GET /api/customers/{id}` - get one customer
- `POST /api/customers` - create customer
- `DELETE /api/customers/{id}` - delete customer (also deletes their bookings)

**Hotels:**
- `GET /api/hotels` - list hotels
- `GET /api/hotels/{id}` - get hotel details
- `POST /api/hotels` - add new hotel
- `DELETE /api/hotels/{id}` - remove hotel

**Bookings:**
- `GET /api/bookings` - all bookings
- `GET /api/bookings/{id}` - specific booking
- `GET /api/bookings/customer/{customerId}` - bookings for a customer
- `POST /api/bookings` - make a booking
- `DELETE /api/bookings/{id}` - cancel booking

**Guest Bookings:**
- `POST /api/guest-bookings` - creates both customer and booking in one go

**Travel Agent:**
- `GET /api/travel-agent` - list all aggregate bookings
- `POST /api/travel-agent` - book hotel + taxi + flight together
- `DELETE /api/travel-agent/{id}` - cancel everything

## Database

Using H2 in-memory database. There's some sample data loaded from `import.sql` (3 customers and 3 hotels).

## Configuration

The external API URLs are in `application.properties`:
- `taxi-api/mp-rest/url` - for taxi bookings
- `flight-api/mp-rest/url` - for flight bookings

## Some Notes

- Used JPA relationships (@ManyToOne, @OneToMany) so deleting a customer auto-deletes their bookings
- Implemented JTA transactions for the guest booking feature - took a while to figure out
- Error handling returns proper HTTP codes (400 for validation, 404 for not found, 409 for conflicts)
- Used MicroProfile REST Client for calling external services
- Tests cover the main scenarios but could probably add more edge cases

## Issues I Ran Into

- Date validation was annoying because it needs to be in the future
- The cascade delete thing wasn't working at first, had to add `orphanRemoval = true`
- Transaction rollback for travel agent took some time to get right
- H2 database syntax is slightly different from PostgreSQL
