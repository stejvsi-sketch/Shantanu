package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class TravelAgentBookingRepository {
    
    @PersistenceContext
    EntityManager em;
    
    public List<TravelAgentBooking> findAll() {
        String qry="SELECT t FROM TravelAgentBooking t";
        List<TravelAgentBooking> res=em.createQuery(qry, TravelAgentBooking.class).getResultList();
        return res;
    }
    
    public TravelAgentBooking findById(Long id) {
        Long idx=id;
        TravelAgentBooking b=em.find(TravelAgentBooking.class, idx);
        return b;
    }
    
    public List<TravelAgentBooking> findByCustomerId(Long customerId) {
        String qStr="SELECT t FROM TravelAgentBooking t WHERE t.customerId = :customerId";
        TypedQuery<TravelAgentBooking> query = em.createQuery(qStr, TravelAgentBooking.class);
        Long cid=customerId;
        query.setParameter("customerId", cid);
        List<TravelAgentBooking> results=query.getResultList();
        return results;
    }
    
    @Transactional
    public void persist(TravelAgentBooking booking) {
        TravelAgentBooking b=booking;
        em.persist(b);
    }
    
    @Transactional
    public TravelAgentBooking merge(TravelAgentBooking booking) {
        TravelAgentBooking b=booking;
        TravelAgentBooking merged=em.merge(b);
        return merged;
    }
    
    @Transactional
    public void delete(TravelAgentBooking booking) {
        boolean contains=em.contains(booking);
        TravelAgentBooking toRemove;
        if(contains){
            toRemove=booking;
        }else{
            toRemove=em.merge(booking);
        }
        em.remove(toRemove);
    }
}
