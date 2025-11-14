package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

// Agent customer - this is OUR customer in the agent service
// different from the downstream service customers
@Entity
@Table(name = "agent_customer", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class AgentCustomer implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // agent customer id
    
    @NotNull
    @Size(min = 1, max = 50)
    @Pattern(regexp = "[A-Za-z-' ]+", message = "Name must contain only letters")
    private String name; // customer name
    
    @NotNull
    @Email
    private String email; // customer email
    
    @NotNull
    @Pattern(regexp = "^0[0-9]{10}$", message = "Phone must start with 0 and be 11 digits")
    private String phonenumber; // customer phone
    
    // getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhonenumber() {
        return phonenumber;
    }
    
    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
}
