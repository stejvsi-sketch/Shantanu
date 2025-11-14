package com.example.flight;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "bookings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_booking_flight_date", columnNames = {"flight_id", "booking_date"})
})
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @NotNull
    @Future
    @Column(name = "booking_date", nullable = false)
    private LocalDate date;

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Flight getFlight() { return flight; }
    public void setFlight(Flight flight) { this.flight = flight; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}