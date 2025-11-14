package uk.ac.newcastle.enterprisemiddleware.agent;

// DTO for customer response from downstream services
public class CustomerResult {
    public Long id; // customer id
    public String name;
    public String email;
    public String phoneNumber;
    public String phonenumber; // some services use this spelling
}
