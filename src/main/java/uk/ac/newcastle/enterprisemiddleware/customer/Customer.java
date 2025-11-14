package uk.ac.newcastle.enterprisemiddleware.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.newcastle.enterprisemiddleware.booking.Booking;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Customer entity - holds customer data
@Entity
@Table(name = "customer", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@NamedQueries({
    @NamedQuery(name = Customer.FIND_ALL, query = "SELECT c FROM Customer c ORDER BY c.name ASC"),
    @NamedQuery(name = Customer.FIND_BY_EMAIL, query = "SELECT c FROM Customer c WHERE c.email = :email")
})
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // query constants
    public static final String FIND_ALL = "Customer.findAll";
    public static final String FIND_BY_EMAIL = "Customer.findByEmail";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    @Pattern(regexp = "[A-Za-z-' ]+", message = "Name must contain only letters, spaces, hyphens, and apostrophes")
    @Column(name = "name")
    private String name;

    @NotNull
    @Email(message = "The email address must be in the format user@domain.com") // email validation from tutorial
    @Column(name = "email")
    private String email;

    @NotNull
    @Pattern(regexp = "^0[0-9]{10}$", message = "Phone number must start with 0 and be 11 digits long") //UK phone format
    @Column(name = "phone_number")
    private String phoneNumber;

    @JsonIgnore  // don't show bookings in customer json response
    @OneToMany(mappedBy = "customer", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();



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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null) {
            return false;
        }
        if(!(o instanceof Customer)) {
            return false;
        }
        Customer customer = (Customer) o;
        if(email == null) {
            if(customer.email != null) {
                return false;
            }
        } else if(!email.equals(customer.email)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        if(email != null) {
            result = 31 * result + email.hashCode();
        }
        return result;
    }
}
