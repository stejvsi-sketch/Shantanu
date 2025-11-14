package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class TaxiBookingCreate {
    @NotNull
    public Long customerId;
    
    @NotNull
    public Long taxiId;
    
    @NotNull
    public Date date;
}
