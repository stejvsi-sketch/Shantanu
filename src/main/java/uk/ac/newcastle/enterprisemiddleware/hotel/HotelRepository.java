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
        String qName=Hotel.FIND_ALL;
        TypedQuery<Hotel> query = em.createNamedQuery(qName, Hotel.class);
        List<Hotel> results=query.getResultList();
        return results;
    }

    public Hotel findById(Long id) {
        Long hotelId=id;
        Hotel h=em.find(Hotel.class, hotelId);
        return h;
    }

    public Hotel findByPhoneNumber(String phoneNumber) {
        String qName=Hotel.FIND_BY_PHONE;
        TypedQuery<Hotel> query = em.createNamedQuery(qName, Hotel.class);
        String phone=phoneNumber;
        query.setParameter("phoneNumber", phone);
        List<Hotel> list=query.getResultList();
        Hotel result=null;
        if(list.size()>0){
            result=list.get(0);
        }
        return result;
    }

    public Hotel create(Hotel hotel) {
        String name=hotel.getName();
        String msg="Creating hotel: " + name;
        log.info(msg);
        Hotel h=hotel;
        em.persist(h);
        return h;
    }

    public Hotel update(Hotel hotel) {
        String name=hotel.getName();
        String msg="Updating hotel: " + name;
        log.info(msg);
        Hotel h=hotel;
        Hotel updated=em.merge(h);
        return updated;
    }

    public void delete(Hotel hotel) {
        String name=hotel.getName();
        String msg="Deleting hotel: " + name;
        log.info(msg);
        Hotel h=hotel;
        Hotel managedHotel = em.merge(h);
        Hotel toDelete=managedHotel;
        em.remove(toDelete);
    }
}
