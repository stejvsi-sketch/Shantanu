package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.validation.constraints.NotNull;
import java.util.Date;

// Request for guest booking - creates customer + books across services
public class GuestBookingRequest {
    @NotNull
    public String name; // customer name
    
    @NotNull
    public String email; // customer email
    
    @NotNull
    public String phonenumber; // customer phone
    
    @NotNull
    public Date date; // booking date
    
    @NotNull
    public Long hotelId; // which hotel in our service
    
    @NotNull
    public Long taxiId; // which taxi
    
    @NotNull
    public Long hotel2Id; // which hotel in second hotel service
}
