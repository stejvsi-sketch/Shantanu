package uk.ac.newcastle.enterprisemiddleware.agent;

// DTO for creating customer in downstream services
public class DownstreamCustomerCreate {
    public String name;
    public String email;
    public String phoneNumber;
    public String phonenumber; // some services use this
    
    public DownstreamCustomerCreate() {}
    
    public DownstreamCustomerCreate(String name, String email, String phonenumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phonenumber;
        this.phonenumber = phonenumber;
    }
}
