package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class HotelBookingCreate {
    @NotNull
    public Long customerId;
    
    @NotNull
    public Long hotelId;
    
    @NotNull
    public Date date;
}
