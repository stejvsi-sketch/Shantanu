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

/*
 * Hotel entity class
 * stores hotel info like name, phone, postcode
 */
@Entity
@Table(name = "hotel", uniqueConstraints = @UniqueConstraint(columnNames = "phone_number"))
@NamedQueries({
    @NamedQuery(name = Hotel.FIND_ALL, query = "SELECT h FROM Hotel h ORDER BY h.name ASC"),
    @NamedQuery(name = Hotel.FIND_BY_PHONE, query = "SELECT h FROM Hotel h WHERE h.phoneNumber = :phoneNumber")
})
public class Hotel implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final String FIND_ALL = "Hotel.findAll";
    public static final String FIND_BY_PHONE = "Hotel.findByPhone";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "name")
    private String name;


    @NotNull
    @Pattern(regexp = "^0[0-9]{10}$", message = "Phone number must start with 0 and be 11 digits long") // same pattern as customer
    @Column(name = "phone_number")
    private String phoneNumber;

    @NotNull
    @Size(min = 6, max = 6)
    @Pattern(regexp = "^[A-Z]{2}[0-9]{4}$", message = "Postcode must be in format: A(A)9(9)9(9)") // TODO: maybe simplify this regex?
    @Column(name = "postcode")
    private String postcode;

    @JsonIgnore // avoids circular reference issues
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();


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

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Hotel)) return false;
        Hotel hotel = (Hotel) o;
        return phoneNumber.equals(hotel.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(phoneNumber);
    }
}
