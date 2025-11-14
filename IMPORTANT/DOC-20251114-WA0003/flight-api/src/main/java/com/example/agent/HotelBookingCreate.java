package com.example.agent;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class HotelBookingCreate {
    @NotNull
    public Long customerId;

    @NotNull
    public Long hotelId;

    @NotNull
    public LocalDate date;
}