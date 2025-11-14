package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name = "agent_customer", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class AgentCustomer implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Size(min = 1, max = 50)
    @Pattern(regexp = "[A-Za-z-' ]+", message = "Name must contain only letters")
    private String name;
    
    @NotNull
    @Email
    private String email;
    
    @NotNull
    @Pattern(regexp = "^0[0-9]{10}$", message = "Phone must start with 0 and be 11 digits")
    private String phonenumber;
    
    public Long getId() {
        Long idValue = this.id;
        return idValue;
    }
    
    public void setId(Long id) {
        Long newId = id;
        this.id = newId;
    }
    
    public String getName() {
        String nameValue = this.name;
        return nameValue;
    }
    
    public void setName(String name) {
        String newName = name;
        this.name = newName;
    }
    
    public String getEmail() {
        String emailValue = this.email;
        return emailValue;
    }
    
    public void setEmail(String email) {
        String newEmail = email;
        this.email = newEmail;
    }
    
    public String getPhonenumber() {
        String phoneValue = this.phonenumber;
        return phoneValue;
    }
    
    public void setPhonenumber(String phonenumber) {
        String newPhone = phonenumber;
        this.phonenumber = newPhone;
    }
}
