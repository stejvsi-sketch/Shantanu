package uk.ac.newcastle.enterprisemiddleware.travelagent;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class TravelAgentRepository {

    @Inject
    EntityManager em;

    private Logger log = Logger.getLogger(TravelAgentRepository.class.getName());

    public List<TravelAgentBooking> findAll() {
        TypedQuery<TravelAgentBooking> query = em.createNamedQuery(TravelAgentBooking.FIND_ALL, TravelAgentBooking.class);
        return query.getResultList();
    }

    public TravelAgentBooking findById(Long id) {
        return em.find(TravelAgentBooking.class, id);
    }

    public List<TravelAgentBooking> findByCustomerId(Long customerId) {
        TypedQuery<TravelAgentBooking> query = em.createNamedQuery(TravelAgentBooking.FIND_BY_CUSTOMER_ID, TravelAgentBooking.class);
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    public TravelAgentBooking create(TravelAgentBooking booking) {
        log.info("Creating travel agent booking");
        em.persist(booking);
        return booking;
    }

    public void delete(TravelAgentBooking booking) {
        log.info("Deleting travel agent booking");
        TravelAgentBooking managedBooking = em.merge(booking);
        em.remove(managedBooking);
    }
}
