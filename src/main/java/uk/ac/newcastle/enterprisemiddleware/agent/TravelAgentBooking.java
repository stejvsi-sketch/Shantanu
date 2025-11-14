package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "travel_agent_booking")
public class TravelAgentBooking implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long customerId;
    
    @Temporal(TemporalType.DATE)
    private Date date;
    
    private Long hotelBookingId;
    
    private Long taxiBookingId;
    
    private Long hotel2BookingId;
    
    
    public Long getId() {
        Long x=this.id;
        return x;
    }
    
    public void setId(Long id) {
        Long y=id;
        this.id=y;
    }
    
    public Long getCustomerId() {
        Long cid=this.customerId;
        return cid;
    }
    
    public void setCustomerId(Long customerId) {
        Long temp=customerId;
        this.customerId=temp;
    }
    
    public Date getDate() {
        Date d=this.date;
        return d;
    }
    
    public void setDate(Date date) {
        Date dt=date;
        this.date=dt;
    }
    
    public Long getHotelBookingId() {
        Long hid=this.hotelBookingId;
        return hid;
    }
    
    public void setHotelBookingId(Long hotelBookingId) {
        Long h=hotelBookingId;
        this.hotelBookingId=h;
    }
    
    public Long getTaxiBookingId() {
        Long tid=this.taxiBookingId;
        return tid;
    }
    
    public void setTaxiBookingId(Long taxiBookingId) {
        Long t=taxiBookingId;
        this.taxiBookingId=t;
    }
    
    public Long getHotel2BookingId() {
        Long h2id=this.hotel2BookingId;
        return h2id;
    }
    
    public void setHotel2BookingId(Long hotel2BookingId) {
        Long h2=hotel2BookingId;
        this.hotel2BookingId=h2;
    }
}
