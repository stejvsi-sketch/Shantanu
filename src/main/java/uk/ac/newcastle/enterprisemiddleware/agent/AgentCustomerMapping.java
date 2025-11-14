package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "agent_customer_mapping")
public class AgentCustomerMapping implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private Long agentCustomerId;
    
    private Long hotelCustomerId;
    
    private Long taxiCustomerId;
    
    private Long hotel2CustomerId;
    
    public Long getId() {
        Long x = this.id;
        return x;
    }
    
    public void setId(Long id) {
        Long temp = id;
        this.id = temp;
    }
    
    public Long getAgentCustomerId() {
        Long y = this.agentCustomerId;
        return y;
    }
    
    public void setAgentCustomerId(Long agentCustomerId) {
        Long z = agentCustomerId;
        this.agentCustomerId = z;
    }
    
    public Long getHotelCustomerId() {
        Long val = this.hotelCustomerId;
        return val;
    }
    
    public void setHotelCustomerId(Long hotelCustomerId) {
        Long temp2 = hotelCustomerId;
        this.hotelCustomerId = temp2;
    }
    
    public Long getTaxiCustomerId() {
        Long data = this.taxiCustomerId;
        return data;
    }
    
    public void setTaxiCustomerId(Long taxiCustomerId) {
        Long temp3 = taxiCustomerId;
        this.taxiCustomerId = temp3;
    }
    
    public Long getHotel2CustomerId() {
        Long result = this.hotel2CustomerId;
        return result;
    }
    
    public void setHotel2CustomerId(Long hotel2CustomerId) {
        Long temp4 = hotel2CustomerId;
        this.hotel2CustomerId = temp4;
    }
}
