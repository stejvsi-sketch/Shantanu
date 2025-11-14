package com.example.flight;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class FlightCreate {
    @NotBlank
    @Size(min = 5, max = 5)
    @Pattern(regexp = "^[A-Za-z0-9]{5}$", message = "flightNumber must be 5 alphanumeric characters")
    public String flightNumber;

    @NotBlank
    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[A-Z]{3}$", message = "departure must be 3 uppercase letters")
    public String departure;

    @NotBlank
    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[A-Z]{3}$", message = "destination must be 3 uppercase letters")
    public String destination;
}