package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class BookingRepository {

    @Inject
    EntityManager em;

    private Logger log = Logger.getLogger(BookingRepository.class.getName());

    public List<Booking> findAllOrderedByDate() {
        TypedQuery<Booking> query = em.createNamedQuery(Booking.FIND_ALL, Booking.class);
        return query.getResultList();
    }

    public Booking findById(Long id) {
        return em.find(Booking.class, id);
    }

    public List<Booking> findByCustomerId(Long customerId) {
        TypedQuery<Booking> query = em.createNamedQuery(Booking.FIND_BY_CUSTOMER, Booking.class);
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    public List<Booking> findByHotelId(Long hotelId) {
        TypedQuery<Booking> query = em.createNamedQuery(Booking.FIND_BY_HOTEL, Booking.class);
        query.setParameter("hotelId", hotelId);
        return query.getResultList();
    }

    public Booking create(Booking booking) {
        log.info("Creating booking");
        em.persist(booking);
        return booking;
    }

    public Booking update(Booking booking) {
        log.info("Updating booking");
        return em.merge(booking);
    }

    public void delete(Booking booking) {
        log.info("Deleting booking");
        Booking managedBooking = em.merge(booking);
        em.remove(managedBooking);
    }
}
