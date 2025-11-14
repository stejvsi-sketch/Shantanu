package uk.ac.newcastle.enterprisemiddleware.hotel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.newcastle.enterprisemiddleware.booking.Booking;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Hotel class - represents a hotel in the system
// this is a JPA entity that maps to hotel table in database
@Entity
@Table(name = "hotel", uniqueConstraints = @UniqueConstraint(columnNames = "phone_number"))
@NamedQueries({
    @NamedQuery(name = Hotel.FIND_ALL, query = "SELECT h FROM Hotel h ORDER BY h.name ASC"),
    @NamedQuery(name = Hotel.FIND_BY_PHONE, query = "SELECT h FROM Hotel h WHERE h.phoneNumber = :phoneNumber")
})
public class Hotel implements Serializable {
    private static final long serialVersionUID = 1L; // serialization id
    
    // query name constants
    public static final String FIND_ALL = "Hotel.findAll";
    public static final String FIND_BY_PHONE = "Hotel.findByPhone";

    @Id // primary key for hotel
    @GeneratedValue(strategy = GenerationType.IDENTITY) // database auto generates this
    private Long id; // hotel id number

    @NotNull // required field
    @Size(min = 1, max = 50) // name size limit
    @Column(name = "name") // maps to name column
    private String name; // hotel name


    @NotNull // phone number is required
    @Pattern(regexp = "^0[0-9]{10}$", message = "Phone number must start with 0 and be 11 digits long") // regex pattern for UK phones
    @Column(name = "phone_number") // column in database
    private String phoneNumber; // hotel phone number

    @NotNull // postcode required
    @Size(min = 6, max = 6) // postcode is 6 characters
    @Pattern(regexp = "^[A-Z]{2}[0-9]{4}$", message = "Postcode must be in format: A(A)9(9)9(9)") // postcode pattern like NE1234
    @Column(name = "postcode") // maps to postcode column
    private String postcode; // hotel postcode

    @JsonIgnore // dont include bookings list when converting to json
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.REMOVE, orphanRemoval = true) // one hotel has many bookings
    private List<Booking> bookings = new ArrayList<>(); // list of all bookings for this hotel


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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    // equals method - compares hotels by phone number
    // two hotels are same if they have same phone number
    @Override
    public boolean equals(Object o) {
        // check if same object in memory
        if(this == o) {
            return true;
        }
        // check if object is null
        if(o == null) {
            return false;
        }
        // check if object is a Hotel
        if(!(o instanceof Hotel)) {
            return false;
        }
        // cast to Hotel type
        Hotel hotel = (Hotel) o;
        // compare phone numbers
        if(phoneNumber == null) {
            if(hotel.phoneNumber != null) {
                return false;
            }
        } else if(!phoneNumber.equals(hotel.phoneNumber)) {
            return false;
        }
        return true;
    }

    // hashcode method - uses phone number
    @Override
    public int hashCode() {
        int result = 17; // start with prime number
        if(phoneNumber != null) {
            result = 31 * result + phoneNumber.hashCode(); // multiply by prime and add phone hash
        }
        return result;
    }
}
