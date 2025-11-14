package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class GuestBookingRequest {
    @NotNull
    public String name;
    
    @NotNull
    public String email;
    
    @NotNull
    public String phonenumber;
    
    @NotNull
    public Date date;
    
    @NotNull
    public Long hotelId;
    
    @NotNull
    public Long taxiId;
    
    @NotNull
    public Long hotel2Id;
}
