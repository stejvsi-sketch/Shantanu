package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class BookingCreate {
    
    @NotNull
    public Long customerId;
    
    @NotNull
    public Long hotelId;
    
    @NotNull
    @Future
    public Date date;
}
