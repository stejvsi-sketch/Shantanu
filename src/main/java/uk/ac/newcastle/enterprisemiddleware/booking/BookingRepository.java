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
        String qName=Booking.FIND_ALL;
        TypedQuery<Booking> query = em.createNamedQuery(qName, Booking.class);
        List<Booking> results=query.getResultList();
        return results;
    }

    public Booking findById(Long id) {
        Long bookingId=id;
        Booking b=em.find(Booking.class, bookingId);
        return b;
    }

    public List<Booking> findByCustomerId(Long customerId) {
        String qName=Booking.FIND_BY_CUSTOMER;
        TypedQuery<Booking> query = em.createNamedQuery(qName, Booking.class);
        Long cid=customerId;
        query.setParameter("customerId", cid);
        List<Booking> results=query.getResultList();
        return results;
    }

    public List<Booking> findByHotelId(Long hotelId) {
        String qName=Booking.FIND_BY_HOTEL;
        TypedQuery<Booking> query = em.createNamedQuery(qName, Booking.class);
        Long hid=hotelId;
        query.setParameter("hotelId", hid);
        List<Booking> results=query.getResultList();
        return results;
    }

    public Booking create(Booking booking) {
        String msg="Creating booking";
        log.info(msg);
        Booking b=booking;
        em.persist(b);
        return b;
    }

    public Booking update(Booking booking) {
        String msg="Updating booking";
        log.info(msg);
        Booking b=booking;
        Booking updated=em.merge(b);
        return updated;
    }

    public void delete(Booking booking) {
        String msg="Deleting booking";
        log.info(msg);
        Booking b=booking;
        Booking managedBooking = em.merge(b);
        Booking toDelete=managedBooking;
        em.remove(toDelete);
    }
}
