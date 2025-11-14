package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.validation.constraints.NotNull;
import java.util.Date;

// DTO for creating hotel booking - 3 parameters
public class HotelBookingCreate {
    @NotNull
    public Long customerId; // which customer
    
    @NotNull
    public Long hotelId; // which hotel
    
    @NotNull
    public Date date; // when
}
