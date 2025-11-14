package uk.ac.newcastle.enterprisemiddleware.booking;

import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
//import java.time.LocalDate; // tried using LocalDate but Date works better with JPA

// Booking class - this represents when a customer books a hotel
// stores which customer, which hotel, and what date
@Entity
@Table(name = "booking", uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "hotel_id", "booking_date"}))
@NamedQueries({
    @NamedQuery(name = Booking.FIND_ALL, query = "SELECT b FROM Booking b ORDER BY b.bookingDate ASC"),
    @NamedQuery(name = Booking.FIND_BY_CUSTOMER, query = "SELECT b FROM Booking b WHERE b.customer.id = :customerId ORDER BY b.bookingDate ASC"),
    @NamedQuery(name = Booking.FIND_BY_HOTEL, query = "SELECT b FROM Booking b WHERE b.hotel.id = :hotelId ORDER BY b.bookingDate ASC")
})
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L; // for serialization
    
    // named query constants
    public static final String FIND_ALL = "Booking.findAll";
    public static final String FIND_BY_CUSTOMER = "Booking.findByCustomer";
    public static final String FIND_BY_HOTEL = "Booking.findByHotel";

    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    private Long id; // booking id

    @NotNull // customer is required
    @ManyToOne  // multiple bookings can have same customer
    @JoinColumn(name = "customer_id") // foreign key to customer table
    private Customer customer; // the customer who made the booking

    @NotNull // hotel is required
    @ManyToOne // multiple bookings can be for same hotel
    @JoinColumn(name = "hotel_id") // foreign key to hotel table
    private Hotel hotel; // the hotel being booked


    @NotNull // date is required
    @Future(message = "Booking date must be in the future") // cant book in past
    @Column(name = "booking_date") // column name
    @Temporal(TemporalType.DATE) // stores only date not time
    private Date bookingDate; // date of the booking


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    // equals method - bookings are same if customer, hotel and date are same
    // this prevents duplicate bookings
    @Override
    public boolean equals(Object o) {
        // check if same reference
        if(this == o) {
            return true;
        }
        // check null
        if(o == null) {
            return false;
        }
        // check type
        if(!(o instanceof Booking)) {
            return false;
        }
        // cast to Booking
        Booking booking = (Booking) o;
        // check customer field
        if(customer == null) {
            if(booking.customer != null) {
                return false;
            }
        } else if(!customer.equals(booking.customer)) {
            return false;
        }
        // check hotel field
        if(hotel == null) {
            if(booking.hotel != null) {
                return false;
            }
        } else if(!hotel.equals(booking.hotel)) {
            return false;
        }
        // check date field
        if(bookingDate == null) {
            if(booking.bookingDate != null) {
                return false;
            }
        } else if(!bookingDate.equals(booking.bookingDate)) {
            return false;
        }
        return true;
    }

    // hashcode - combines customer, hotel and date
    @Override
    public int hashCode() {
        int result = 17; // start with prime
        // add customer hash
        if(customer != null) {
            result = 31 * result + customer.hashCode();
        }
        // add hotel hash
        if(hotel != null) {
            result = 31 * result + hotel.hashCode();
        }
        // add date hash
        if(bookingDate != null) {
            result = 31 * result + bookingDate.hashCode();
        }
        return result;
    }
}
