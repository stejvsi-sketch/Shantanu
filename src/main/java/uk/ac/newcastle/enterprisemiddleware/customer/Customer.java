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

// Customer class - this is the entity for customers in database
// represents a customer who can make bookings
@Entity
@Table(name = "customer", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@NamedQueries({
    @NamedQuery(name = Customer.FIND_ALL, query = "SELECT c FROM Customer c ORDER BY c.name ASC"),
    @NamedQuery(name = Customer.FIND_BY_EMAIL, query = "SELECT c FROM Customer c WHERE c.email = :email")
})
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L; // for serialization
    
    // constants for named queries
    public static final String FIND_ALL = "Customer.findAll";
    public static final String FIND_BY_EMAIL = "Customer.findByEmail";

    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto generated
    private Long id; // customer id

    @NotNull // cant be null
    @Size(min = 1, max = 50) // name length
    @Pattern(regexp = "[A-Za-z-' ]+", message = "Name must contain only letters, spaces, hyphens, and apostrophes") // regex for name
    @Column(name = "name") // database column
    private String name; // customer name

    @NotNull // required field
    @Email(message = "The email address must be in the format user@domain.com") // email validator
    @Column(name = "email") // column name in db
    private String email; // customer email

    @NotNull // cant be null
    @Pattern(regexp = "^0[0-9]{10}$", message = "Phone number must start with 0 and be 11 digits long") // UK phone number format
    @Column(name = "phone_number") // database column name
    private String phoneNumber; // customer phone number

    @JsonIgnore  // dont include bookings when converting to json
    @OneToMany(mappedBy = "customer", cascade = CascadeType.REMOVE, orphanRemoval = true) // one customer has many bookings
    private List<Booking> bookings = new ArrayList<>(); // list of bookings



    // getter for id
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

    // equals method - compares customers based on email
    // needed for JPA to work properly
    @Override
    public boolean equals(Object o) {
        // check if same object
        if(this == o) {
            return true;
        }
        // check if null
        if(o == null) {
            return false;
        }
        // check if its a Customer object
        if(!(o instanceof Customer)) {
            return false;
        }
        // cast to Customer
        Customer customer = (Customer) o;
        // compare emails
        if(email == null) {
            if(customer.email != null) {
                return false;
            }
        } else if(!email.equals(customer.email)) {
            return false;
        }
        return true;
    }

    // hashcode method - based on email
    @Override
    public int hashCode() {
        int result = 17; // prime number
        if(email != null) {
            result = 31 * result + email.hashCode(); // another prime number
        }
        return result;
    }
}
