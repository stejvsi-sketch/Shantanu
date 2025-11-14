package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

// Aggregate booking - stores references to bookings across 3 services
// one agent booking = 1 hotel booking + 1 taxi booking + 1 hotel2 booking
@Entity
@Table(name = "travel_agent_booking")
public class TravelAgentBooking implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // our aggregate booking id
    
    private Long customerId; // which agent customer made this booking
    
    @Temporal(TemporalType.DATE)
    private Date date; // booking date
    
    private Long hotelBookingId; // booking id in our hotel service
    
    private Long taxiBookingId; // booking id in taxi service
    
    private Long hotel2BookingId; // booking id in second hotel service
    
    // getters setters
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
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public Long getHotelBookingId() {
        return hotelBookingId;
    }
    
    public void setHotelBookingId(Long hotelBookingId) {
        this.hotelBookingId = hotelBookingId;
    }
    
    public Long getTaxiBookingId() {
        return taxiBookingId;
    }
    
    public void setTaxiBookingId(Long taxiBookingId) {
        this.taxiBookingId = taxiBookingId;
    }
    
    public Long getHotel2BookingId() {
        return hotel2BookingId;
    }
    
    public void setHotel2BookingId(Long hotel2BookingId) {
        this.hotel2BookingId = hotel2BookingId;
    }
}
