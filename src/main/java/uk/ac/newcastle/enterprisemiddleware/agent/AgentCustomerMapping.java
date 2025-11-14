package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.persistence.*;
import java.io.Serializable;

// Maps one agent customer to customer IDs in the 3 downstream services
// so we can book across all services for one customer
@Entity
@Table(name = "agent_customer_mapping")
public class AgentCustomerMapping implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // mapping id
    
    @Column(unique = true)
    private Long agentCustomerId; // our agent customer id
    
    private Long hotelCustomerId; // customer id in hotel service
    
    private Long taxiCustomerId; // customer id in taxi service
    
    private Long hotel2CustomerId; // customer id in second hotel service
    
    // getters setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAgentCustomerId() {
        return agentCustomerId;
    }
    
    public void setAgentCustomerId(Long agentCustomerId) {
        this.agentCustomerId = agentCustomerId;
    }
    
    public Long getHotelCustomerId() {
        return hotelCustomerId;
    }
    
    public void setHotelCustomerId(Long hotelCustomerId) {
        this.hotelCustomerId = hotelCustomerId;
    }
    
    public Long getTaxiCustomerId() {
        return taxiCustomerId;
    }
    
    public void setTaxiCustomerId(Long taxiCustomerId) {
        this.taxiCustomerId = taxiCustomerId;
    }
    
    public Long getHotel2CustomerId() {
        return hotel2CustomerId;
    }
    
    public void setHotel2CustomerId(Long hotel2CustomerId) {
        this.hotel2CustomerId = hotel2CustomerId;
    }
}
