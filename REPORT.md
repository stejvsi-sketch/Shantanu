# Hotel Booking Service - Project Report

**Student:** Shantanu Raj Chaudhary  
**Service Type:** Hotel

## Summary

This report covers the development of a hotel booking REST API service for the Enterprise Software and Services coursework. The project uses Quarkus, JPA, JTA, and follows RESTful design.

## Part 1: Core REST API

### Implementation Overview

The core service implements three main RESTful resources:

1. **Customer Resource** - Manages customer information with validation for name (alphabetic only), email format, and UK phone numbers (starting with 0, 11 digits)

2. **Hotel Resource** - Manages hotel information with validation for phone numbers and UK postcodes (format: XX9999)

3. **Booking Resource** - Links customers to hotels with future date validation and uniqueness constraints

### Technical Decisions

**Entity Design:** Each entity uses JPA annotations with validation constraints:
- Auto-generated IDs
- Named queries for common operations
- equals() and hashCode() based on unique fields

**Repository Pattern:** Data access is separated into repository classes to keep business logic clean.

**Service Layer:** Business validation and transactions are handled in services, not in REST endpoints.

**Validation:** Bean Validation on entities plus custom checks in services for things like duplicate emails.

### Challenges

The hardest part was getting the validation rules right. The phone number regex took a while to figure out to match both UK mobile and landline formats properly.

## Part 2: Advanced Features

### Cascading Deletion

I set up bidirectional relationships between Customer-Booking and Hotel-Booking using JPA annotations:
- Used `@OneToMany` with `cascade = CascadeType.REMOVE` and `orphanRemoval = true`
- Applied `@JsonIgnore` to prevent circular reference issues during JSON serialization

This keeps things consistent - when you delete a Customer or Hotel, all their Bookings get deleted too.

### Guest Booking Transaction

Created a transactional endpoint that creates both a Customer and a Booking atomically using manual JTA transaction demarcation:

```java
userTransaction.begin();
Customer createdCustomer = customerService.create(customer);
Booking createdBooking = bookingService.create(booking);
userTransaction.commit();
```

If anything fails, the transaction rolls back so you don't end up with partial data.

### Benefits of JTA

JTA has some good advantages:
1. **Atomicity** - Everything commits or nothing does
2. **Cross-resource transactions** - Works across multiple databases
3. **Explicit control** - You decide exactly where transactions start and end

The downside is it's more complex and you have to handle rollbacks yourself.

## Part 3: Travel Agent Integration

### Architecture

Built an aggregator service that coordinates bookings across three services:
- Local hotel booking
- Remote taxi booking (via REST client)
- Remote flight booking (via REST client)

### Compensating Transactions

Since distributed transactions are complex, I used a compensation-based approach:
1. Attempt to create all three bookings sequentially
2. If any booking fails, explicitly delete any previously created bookings
3. This keeps things consistent without needing two-phase commit

### Challenges with External Services

Main problems:
- **Service Availability** - External services might be down during development
- **API Compatibility** - Making sure request/response formats match between services
- **Error Handling** - Handling various failure scenarios properly

Using REST clients provided in the same room simplified communication, but in a distributed production environment, additional concerns would include:
- Network latency and timeouts
- Service discovery and load balancing
- API versioning and backward compatibility
- Authentication and authorization

## Testing Strategy

REST Assured tests covering:
- **Happy path scenarios** - Valid data creates expected resources
- **Validation failures** - Invalid data returns 400 with appropriate messages
- **Not found scenarios** - Non-existent resources return 404
- **Cascading behavior** - Deleting parent entities removes children
- **Transaction rollback** - Failed transactions don't leave partial data

Tests are isolated using the in-memory H2 database which resets between test classes.

## Personal Reflection

### What Went Well

- Repository and service layers kept the code organized
- JPA cascading made deletion logic much easier
- Swagger annotations were pretty straightforward
- REST Assured was good for testing

### Difficulties Faced

- JPA relationship annotations took time to understand properly
- Getting the GuestBooking transaction boundaries right needed some debugging
- Finding the right balance for validation rules was tricky

### Learning Outcomes

This project gave me hands-on experience with:
- Enterprise patterns like Repository and Service layers
- Transaction management and ACID
- Handling failures in distributed systems
- Writing proper API tests

## Conclusions

The finished service is a working hotel booking API with proper validation, transactions, and integration with external services. The modular design makes it easy to add more features later if needed.

The main things I learned:
- Test early and test often
- Keep your code organized with clear separations
- Handle errors properly
- Document your APIs well

## Appendices

### API Endpoints Summary

**Customers:** GET, POST, PUT, DELETE at `/api/customers`  
**Hotels:** GET, POST, PUT, DELETE at `/api/hotels`  
**Bookings:** GET, POST, DELETE at `/api/bookings`  
**Guest Bookings:** POST at `/api/guest-bookings`  
**Travel Agent:** GET, POST, DELETE at `/api/travel-agent`

### Technologies Used

- Quarkus 2.16.6
- Hibernate ORM with JPA
- JAX-RS with RESTEasy
- Bean Validation
- JTA for transactions
- H2 in-memory database
- REST Assured for testing
- OpenAPI/Swagger for documentation

### Build Commands

```bash
mvn clean package    # Build the project
mvn quarkus:dev      # Run in development mode
mvn test             # Run all tests
```
