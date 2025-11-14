package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

// repository for aggregate bookings
@ApplicationScoped
public class TravelAgentBookingRepository {
    
    @PersistenceContext
    EntityManager em;
    
    public List<TravelAgentBooking> findAll() {
        return em.createQuery("SELECT t FROM TravelAgentBooking t", TravelAgentBooking.class).getResultList();
    }
    
    public TravelAgentBooking findById(Long id) {
        return em.find(TravelAgentBooking.class, id);
    }
    
    public List<TravelAgentBooking> findByCustomerId(Long customerId) {
        TypedQuery<TravelAgentBooking> query = em.createQuery(
            "SELECT t FROM TravelAgentBooking t WHERE t.customerId = :customerId", TravelAgentBooking.class);
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }
    
    @Transactional
    public void persist(TravelAgentBooking booking) {
        em.persist(booking);
    }
    
    @Transactional
    public TravelAgentBooking merge(TravelAgentBooking booking) {
        return em.merge(booking);
    }
    
    @Transactional
    public void delete(TravelAgentBooking booking) {
        em.remove(em.contains(booking) ? booking : em.merge(booking));
    }
}
