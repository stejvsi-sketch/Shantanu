package com.example.flight;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class BookingCreate {
    @NotNull
    public Long customerId;

    @NotNull
    public Long flightId;

    @NotNull
    @Future
    public LocalDate date;
}