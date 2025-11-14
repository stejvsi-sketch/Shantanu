package uk.ac.newcastle.enterprisemiddleware.travelagent;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "travel_agent_booking")
@NamedQueries({
    @NamedQuery(name = TravelAgentBooking.FIND_ALL, query = "SELECT t FROM TravelAgentBooking t ORDER BY t.id ASC"),
    @NamedQuery(name = TravelAgentBooking.FIND_BY_CUSTOMER_ID, query = "SELECT t FROM TravelAgentBooking t WHERE t.customerId = :customerId ORDER BY t.id ASC")
})
public class TravelAgentBooking implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final String FIND_ALL = "TravelAgentBooking.findAll";
    public static final String FIND_BY_CUSTOMER_ID = "TravelAgentBooking.findByCustomerId";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "customer_id")
    private Long customerId;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "hotel_booking_id")
    private Booking hotelBooking;

    @NotNull
    @Column(name = "taxi_booking_id")
    private Long taxiBookingId;

    @NotNull
    @Column(name = "flight_booking_id")
    private Long flightBookingId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Booking getHotelBooking() {
        return hotelBooking;
    }

    public void setHotelBooking(Booking hotelBooking) {
        this.hotelBooking = hotelBooking;
    }

    public Long getTaxiBookingId() {
        return taxiBookingId;
    }

    public void setTaxiBookingId(Long taxiBookingId) {
        this.taxiBookingId = taxiBookingId;
    }

    public Long getFlightBookingId() {
        return flightBookingId;
    }

    public void setFlightBookingId(Long flightBookingId) {
        this.flightBookingId = flightBookingId;
    }
}
