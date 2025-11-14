package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.util.Date;

// DTO for creating bookings - just needs 3 things
public class BookingCreate {
    
    @NotNull
    public Long customerId; // which customer
    
    @NotNull
    public Long hotelId; // which hotel
    
    @NotNull
    @Future // has to be future date
    public Date date; // when
}
