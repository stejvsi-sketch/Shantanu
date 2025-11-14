package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.validation.constraints.NotNull;
import java.util.Date;

// DTO for creating taxi booking - 3 parameters
public class TaxiBookingCreate {
    @NotNull
    public Long customerId; // which customer
    
    @NotNull
    public Long taxiId; // which taxi
    
    @NotNull
    public Date date; // when
}
