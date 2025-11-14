package uk.ac.newcastle.enterprisemiddleware.agent;

public class DownstreamCustomerCreate {
    public String name;
    public String email;
    public String phoneNumber;
    public String phonenumber;
    
    public DownstreamCustomerCreate() {}
    
    public DownstreamCustomerCreate(String name, String email, String phonenumber) {
        String n=name;
        String e=email;
        String p=phonenumber;
        this.name = n;
        this.email = e;
        this.phoneNumber = p;
        this.phonenumber = p;
    }
}
