package uk.ac.newcastle.enterprisemiddleware.hotel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class HotelService {

    @Inject
    Validator validator;

    @Inject
    HotelRepository repository;

    public List<Hotel> findAllOrderedByName() {
        List<Hotel> hotels = repository.findAllOrderedByName();
        return hotels;
    }

    public Hotel findById(Long id) {
        Hotel hotel = repository.findById(id);
        return hotel;
    }

    public Hotel findByPhoneNumber(String phoneNumber) {
        Hotel hotel = repository.findByPhoneNumber(phoneNumber);
        return hotel;
    }

    @Transactional
    public Hotel create(Hotel hotel) throws Exception {
        // validation
        Set<ConstraintViolation<Hotel>> violations = validator.validate(hotel);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }
        
        // check phone number
        Hotel existingHotel = repository.findByPhoneNumber(hotel.getPhoneNumber());
        if(existingHotel != null) {
            if(hotel.getId() == null || !existingHotel.getId().equals(hotel.getId())) {
                throw new Exception("Phone number already exists");
            }
        }

        Hotel createdHotel = repository.create(hotel);
        return createdHotel;
    }

    @Transactional
    public Hotel update(Hotel hotel) throws Exception {
        // validation
        Set<ConstraintViolation<Hotel>> violations = validator.validate(hotel);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }
        
        // check phone number
        Hotel existingHotel = repository.findByPhoneNumber(hotel.getPhoneNumber());
        if(existingHotel != null) {
            if(!existingHotel.getId().equals(hotel.getId())) {
                throw new Exception("Phone number already exists");
            }
        }

        Hotel updatedHotel = repository.update(hotel);
        return updatedHotel;
    }

    @Transactional
    public void delete(Hotel hotel) {
        repository.delete(hotel);
    }
}
