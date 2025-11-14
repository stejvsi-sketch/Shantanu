package uk.ac.newcastle.enterprisemiddleware.booking;

import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
//import java.time.LocalDate; // tried using LocalDate but Date works better with JPA

// represents a hotel booking
@Entity
@Table(name = "booking", uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "hotel_id", "booking_date"}))
@NamedQueries({
    @NamedQuery(name = Booking.FIND_ALL, query = "SELECT b FROM Booking b ORDER BY b.bookingDate ASC"),
    @NamedQuery(name = Booking.FIND_BY_CUSTOMER, query = "SELECT b FROM Booking b WHERE b.customer.id = :customerId ORDER BY b.bookingDate ASC"),
    @NamedQuery(name = Booking.FIND_BY_HOTEL, query = "SELECT b FROM Booking b WHERE b.hotel.id = :hotelId ORDER BY b.bookingDate ASC")
})
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final String FIND_ALL = "Booking.findAll";
    public static final String FIND_BY_CUSTOMER = "Booking.findByCustomer";
    public static final String FIND_BY_HOTEL = "Booking.findByHotel";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne  // many bookings can belong to one customer
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;


    @NotNull
    @Future(message = "Booking date must be in the future") // makes sure you cant book in the past
    @Column(name = "booking_date")
    @Temporal(TemporalType.DATE)
    private Date bookingDate;


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

    // custom equals method for checking duplicate bookings
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        if(!customer.equals(booking.customer)) return false;
        if(!hotel.equals(booking.hotel)) return false;
        return bookingDate.equals(booking.bookingDate);
    }

    @Override
    public int hashCode() {
        int result = customer.hashCode();
        result = 31 * result + hotel.hashCode();
        result = 31 * result + bookingDate.hashCode();
        return result;
    }
}
