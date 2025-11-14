package com.example.flight;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "flights", uniqueConstraints = {
        @UniqueConstraint(name = "uk_flight_number", columnNames = {"flight_number"})
})
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 5, max = 5)
    @Pattern(regexp = "^[A-Za-z0-9]{5}$", message = "flightNumber must be 5 alphanumeric characters")
    @Column(name = "flight_number", nullable = false, length = 5)
    private String flightNumber;

    @NotBlank
    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[A-Z]{3}$", message = "departure must be 3 uppercase letters")
    @Column(nullable = false, length = 3)
    private String departure;

    @NotBlank
    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[A-Z]{3}$", message = "destination must be 3 uppercase letters")
    @Column(nullable = false, length = 3)
    private String destination;

    public Long getId() { return id; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getDeparture() { return departure; }
    public void setDeparture(String departure) { this.departure = departure; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
}