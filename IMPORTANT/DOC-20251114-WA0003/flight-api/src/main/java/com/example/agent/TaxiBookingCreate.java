package com.example.agent;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class TaxiBookingCreate {
    @NotNull
    public Long customerId;

    @NotNull
    public Long taxiId;

    @NotNull
    public LocalDate date;
}