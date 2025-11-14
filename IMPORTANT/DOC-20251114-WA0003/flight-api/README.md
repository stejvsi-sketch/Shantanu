# Flight API — Service Overview

This service manages flights, customers, and bookings. It validates routes and enforces duplicate protection per flight+date.

![Service Diagram](../architecture_images/openshift-overview_quarkus.png)

## Run
- `cd flight-api && .\mvnw.ps1 quarkus:dev`
- Swagger: `http://localhost:8083/swagger`

## Endpoints
- `POST /api/flights` — create flight (unique 5-char alphanumeric `flightNumber`; `departure`/`destination` 3 uppercase letters, must differ)
- `GET /api/flights` — list flights
- `DELETE /api/flights/{id}` — delete flight
- `POST /api/customers` — create customer (unique `email`)
- `GET /api/customers` — list customers
- `POST /api/bookings` — create booking (`customerId`, `flightId`, future `date`; prevents duplicate flight+date)
- `DELETE /api/bookings/{id}` — cancel booking
- `GET /api/bookings[?customerId=...]` — list bookings, optionally by customer

## Validations
- Flight: `flightNumber` 5 alphanumeric and unique; `departure` and `destination` 3 uppercase letters; cannot be equal.
- Customer: `name` required; `email` valid and unique; `phonenumber` required.
- Booking: `customerId` and `flightId` positive; `date` `yyyy-MM-dd` and future; unique on `flight+date`.

## Quick cURLs
- Create flight:
  - `curl -X POST http://localhost:8083/api/flights -H "Content-Type: application/json" -d '{"flightNumber":"AB123","departure":"NCL","destination":"LHR"}'`
- Create customer:
  - `curl -X POST http://localhost:8083/api/customers -H "Content-Type: application/json" -d '{"name":"Finn","email":"finn@example.com","phonenumber":"01234567890"}'`
- Create booking:
  - `curl -X POST http://localhost:8083/api/bookings -H "Content-Type: application/json" -d '{"customerId":CUSTOMER_ID,"flightId":FLIGHT_ID,"date":"2025-12-01"}'`
- List bookings:
  - `curl "http://localhost:8083/api/bookings?customerId=CUSTOMER_ID"`

## Next
- Add flight capacity and seat assignment. Include route validation rules and scheduling windows.