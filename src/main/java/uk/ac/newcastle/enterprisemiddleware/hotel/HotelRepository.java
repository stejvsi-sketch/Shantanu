package uk.ac.newcastle.enterprisemiddleware.hotel;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class HotelRepository {

    @Inject
    EntityManager em;

    private Logger log = Logger.getLogger(HotelRepository.class.getName());

    public List<Hotel> findAllOrderedByName() {
        TypedQuery<Hotel> query = em.createNamedQuery(Hotel.FIND_ALL, Hotel.class);
        return query.getResultList();
    }

    public Hotel findById(Long id) {
        return em.find(Hotel.class, id);
    }

    public Hotel findByPhoneNumber(String phoneNumber) {
        TypedQuery<Hotel> query = em.createNamedQuery(Hotel.FIND_BY_PHONE, Hotel.class);
        query.setParameter("phoneNumber", phoneNumber);
        return query.getResultList().stream().findFirst().orElse(null);
    }

    public Hotel create(Hotel hotel) {
        log.info("Creating hotel: " + hotel.getName());
        em.persist(hotel);
        return hotel;
    }

    public Hotel update(Hotel hotel) {
        log.info("Updating hotel: " + hotel.getName());
        return em.merge(hotel);
    }

    public void delete(Hotel hotel) {
        log.info("Deleting hotel: " + hotel.getName());
        Hotel managedHotel = em.merge(hotel);
        em.remove(managedHotel);
    }
}
