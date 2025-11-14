package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.validation.constraints.NotNull;

// DTO for creating agent customer
public class AgentCustomerCreate {
    @NotNull
    public String name;
    
    @NotNull
    public String email;
    
    @NotNull
    public String phonenumber;
}
