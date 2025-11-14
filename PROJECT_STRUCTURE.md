# Hotel Booking Service - Project Structure

## Overview
Complete implementation of a hotel booking REST API service with all three parts of the coursework requirements.

## Directory Structure

```
hotel-booking-service/
├── pom.xml                                 # Maven build configuration
├── README.md                               # Project documentation
├── REPORT.md                               # Coursework report
├── PROJECT_STRUCTURE.md                    # This file
├── .gitignore                              # Git ignore rules
├── run-dev.bat                             # Script to run in dev mode
├── run-tests.bat                           # Script to run tests
│
├── src/
│   ├── main/
│   │   ├── java/uk/ac/newcastle/enterprisemiddleware/
│   │   │   ├── Application.java            # Main application class with OpenAPI config
│   │   │   │
│   │   │   ├── customer/
│   │   │   │   ├── Customer.java           # Customer entity with validation
│   │   │   │   ├── CustomerRepository.java # Customer data access layer
│   │   │   │   ├── CustomerService.java    # Customer business logic
│   │   │   │   └── CustomerRestService.java # Customer REST endpoints
│   │   │   │
│   │   │   ├── hotel/
│   │   │   │   ├── Hotel.java              # Hotel entity with validation
│   │   │   │   ├── HotelRepository.java    # Hotel data access layer
│   │   │   │   ├── HotelService.java       # Hotel business logic
│   │   │   │   └── HotelRestService.java   # Hotel REST endpoints
│   │   │   │
│   │   │   ├── booking/
│   │   │   │   ├── Booking.java            # Booking entity with relationships
│   │   │   │   ├── BookingRepository.java  # Booking data access layer
│   │   │   │   ├── BookingService.java     # Booking business logic
│   │   │   │   └── BookingRestService.java # Booking REST endpoints
│   │   │   │
│   │   │   ├── guestbooking/
│   │   │   │   ├── GuestBooking.java       # Non-persistent DTO
│   │   │   │   └── GuestBookingRestService.java # Transactional endpoint with JTA
│   │   │   │
│   │   │   └── travelagent/
│   │   │       ├── TravelAgentBooking.java # Aggregate booking entity
│   │   │       ├── BookingRequest.java     # Request DTO
│   │   │       ├── ExternalBooking.java    # Response DTO for external services
│   │   │       ├── TaxiBookingService.java # REST client for taxi service
│   │   │       ├── FlightBookingService.java # REST client for flight service
│   │   │       ├── TravelAgentRepository.java # Travel agent data access
│   │   │       ├── TravelAgentService.java # Travel agent business logic
│   │   │       └── TravelAgentRestService.java # Travel agent REST endpoints
│   │   │
│   │   └── resources/
│   │       ├── application.properties      # Application configuration
│   │       └── import.sql                  # Sample data initialization
│   │
│   └── test/
│       ├── java/uk/ac/newcastle/enterprisemiddleware/
│       │   ├── customer/
│       │   │   └── CustomerRestServiceTest.java # Customer endpoint tests
│       │   ├── hotel/
│       │   │   └── HotelRestServiceTest.java # Hotel endpoint tests
│       │   ├── booking/
│       │   │   └── BookingRestServiceTest.java # Booking endpoint tests
│       │   └── guestbooking/
│       │       └── GuestBookingRestServiceTest.java # Guest booking tests
│       │
│       └── resources/
│           └── application.properties      # Test configuration
```

## Component Summary

### Part 1: Core REST API (8 files)
- **Entities**: Customer, Hotel, Booking
- **Repositories**: 3 data access classes
- **Services**: 3 business logic classes
- **REST Services**: 3 JAX-RS endpoint classes

### Part 2: Advanced Features (2 files)
- **GuestBooking**: Non-persistent bean
- **GuestBookingRestService**: JTA transactional endpoint
- **Cascading**: Implemented in Customer and Hotel entities

### Part 3: Travel Agent (8 files)
- **TravelAgentBooking**: Aggregate entity
- **BookingRequest/ExternalBooking**: DTOs
- **REST Clients**: 2 interfaces for external services
- **Repository/Service/RestService**: 3 implementation classes

### Configuration (2 files)
- **application.properties**: Main configuration
- **import.sql**: Sample data

### Testing (5 files)
- **REST Assured tests**: 4 test classes covering all endpoints
- **test/application.properties**: Test configuration

### Documentation (4 files)
- **README.md**: Project overview and usage
- **REPORT.md**: Coursework report
- **PROJECT_STRUCTURE.md**: This structure guide
- **pom.xml**: Maven dependencies and build config

### Utilities (3 files)
- **.gitignore**: Version control exclusions
- **run-dev.bat**: Development mode launcher
- **run-tests.bat**: Test runner

## Total Files Created

- **Java Source Files**: 23
- **Test Files**: 4
- **Configuration Files**: 3
- **Documentation Files**: 4
- **Build Files**: 1 (pom.xml)
- **Utility Scripts**: 3

**Total: 38 files**

## Key Features

### Validation
- Email format validation
- UK phone number validation (0 + 10 digits)
- UK postcode validation (XX9999)
- Name validation (alphabetic characters only)
- Future date validation for bookings
- Uniqueness constraints

### Relationships
- Customer to Booking (One-to-Many with cascade delete)
- Hotel to Booking (One-to-Many with cascade delete)
- TravelAgentBooking to Booking (Many-to-One)

### Transactions
- Automatic transaction management in service layer
- Manual JTA transaction demarcation in GuestBooking
- Compensating transactions in TravelAgent

### REST Clients
- MicroProfile REST Client for Taxi service
- MicroProfile REST Client for Flight service
- Error handling and rollback coordination

### Documentation
- OpenAPI/Swagger annotations on all endpoints
- README with usage instructions
- Coursework report

## Build and Run

1. **Build Project**: `mvn clean package`
2. **Run Dev Mode**: `mvn quarkus:dev` or `run-dev.bat`
3. **Run Tests**: `mvn test` or `run-tests.bat`
4. **Access Swagger**: http://localhost:8080/q/swagger-ui/

## Testing Coverage

- Create/Read operations for all entities
- Validation error handling
- Not found scenarios
- Cascading deletion verification
- Transaction rollback testing
- Duplicate prevention tests

## API Documentation

All endpoints documented with:
- Operation summaries and descriptions
- Request/response schemas
- HTTP status codes and meanings
- Example usage

Access at: http://localhost:8080/q/swagger-ui/
