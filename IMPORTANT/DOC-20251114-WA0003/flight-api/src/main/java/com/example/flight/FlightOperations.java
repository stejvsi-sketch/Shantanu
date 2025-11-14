package com.example.flight;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDate;
import java.util.List;

/**
 * Local service that wraps Flight persistence operations so agent endpoints
 * can call into the same application without HTTP.
 */
@ApplicationScoped
public class FlightOperations {

    @Inject
    BookingRepository bookingRepo;

    @Inject
    CustomerRepository customerRepo;

    @Inject
    FlightRepository flightRepo;

    /**
     * Returns an existing customer by email or creates a new one.
     */
    @Transactional
    public Customer ensureCustomerByEmail(String name, String email, String phonenumber) {
        if (email == null || email.isBlank()) {
            throw new WebApplicationException("Email is required", 400);
        }
        Customer existing = customerRepo.findByEmail(email);
        if (existing != null) {
            return existing;
        }
        Customer c = new Customer();
        c.setName(name);
        c.setEmail(email);
        c.setPhonenumber(phonenumber);
        return customerRepo.create(c);
    }

    /**
     * Create a flight booking enforcing the same rules as the REST resource:
     * - customerId and flightId must exist
     * - (flight & date) must be unique
     */
    @Transactional
    public Booking createFlightBooking(Long customerId, Long flightId, LocalDate date) {
        if (customerId == null || customerId <= 0) {
            throw new WebApplicationException("Invalid customerId", 400);
        }
        if (flightId == null || flightId <= 0) {
            throw new WebApplicationException("Invalid flightId", 400);
        }
        if (date == null) {
            throw new WebApplicationException("date is required", 400);
        }

        Customer customer = customerRepo.findById(customerId);
        if (customer == null) {
            throw new WebApplicationException("Invalid customerId", 400);
        }
        Flight flight = flightRepo.findById(flightId);
        if (flight == null) {
            throw new WebApplicationException("Invalid flightId", 400);
        }

        if (bookingRepo.findByFlightAndDate(flight, date) != null) {
            throw new WebApplicationException("Booking already exists for flight and date", 409);
        }

        Booking b = new Booking();
        b.setCustomer(customer);
        b.setFlight(flight);
        b.setDate(date);
        return bookingRepo.create(b);
    }

    /**
     * Lists bookings for a given customer.
     */
    public List<Booking> listFlightBookingsForCustomer(Long customerId) {
        return bookingRepo.listByCustomerId(customerId);
    }

    /**
     * Cancels a booking by id.
     * @return true if deleted, false if not found
     */
    @Transactional
    public boolean cancelFlightBooking(Long bookingId) {
        if (bookingId == null || bookingId <= 0) {
            throw new WebApplicationException("Invalid bookingId", 400);
        }
        return bookingRepo.deleteById(bookingId);
    }
}