package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.validation.constraints.NotNull;

public class Hotel2BookingCreate {
    @NotNull
    public String globalBookingId;

    @NotNull
    public Long hotelId;

    @NotNull
    public String bookingDate;
}
