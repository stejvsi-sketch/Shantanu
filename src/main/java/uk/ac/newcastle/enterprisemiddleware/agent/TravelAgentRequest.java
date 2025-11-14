package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.validation.constraints.NotNull;
import java.util.Date;

// Request for creating aggregate booking
// agent customer already exists
public class TravelAgentRequest {
    @NotNull
    public Long customerId; // our agent customer id
    
    @NotNull
    public Date date; // booking date for all services
    
    @NotNull
    public Long hotelId; // which hotel in our service
    
    @NotNull
    public Long taxiId; // which taxi in taxi service
    
    @NotNull
    public Long hotel2Id; // which hotel in second hotel service
}
