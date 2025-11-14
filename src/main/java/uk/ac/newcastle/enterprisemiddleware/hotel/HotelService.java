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

// hotel service class
@ApplicationScoped
public class HotelService {

    @Inject
    Validator validator; // for validating hotels

    @Inject
    HotelRepository repository; // repository for hotels

    // get all hotels from database
    public List<Hotel> findAllOrderedByName() {
        //System.out.println("Finding all hotels");
        List<Hotel> hotels = repository.findAllOrderedByName();
        return hotels;
    }

    // find hotel by its id
    public Hotel findById(Long id) {
        Hotel hotel = repository.findById(id);
        return hotel;
    }

    // find hotel by phone number
    public Hotel findByPhoneNumber(String phoneNumber) {
        Hotel hotel = repository.findByPhoneNumber(phoneNumber);
        return hotel;
    }

    // create new hotel in system
    @Transactional
    public Hotel create(Hotel hotel) throws Exception {
        //System.out.println("Creating hotel: " + hotel.getName());
        
        // do validation on hotel
        Set<ConstraintViolation<Hotel>> violations = validator.validate(hotel);
        int violationCount = violations.size();
        if(violationCount > 0) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }
        
        // check if phone number is already used by another hotel
        String phoneNumber = hotel.getPhoneNumber();
        Hotel existingHotel = repository.findByPhoneNumber(phoneNumber);
        if(existingHotel != null) {
            // phone number is already in database
            Long hotelId = hotel.getId();
            if(hotelId == null) {
                // new hotel, so phone number is duplicate
                throw new Exception("Phone number already exists");
            } else {
                // updating hotel
                Long existingHotelId = existingHotel.getId();
                if(!existingHotelId.equals(hotelId)) {
                    throw new Exception("Phone number already exists");
                }
            }
        }

        Hotel createdHotel = repository.create(hotel);
        //System.out.println("Hotel created with id: " + createdHotel.getId());
        return createdHotel;
    }

    // update hotel information
    @Transactional
    public Hotel update(Hotel hotel) throws Exception {
        // validate hotel data
        Set<ConstraintViolation<Hotel>> violations = validator.validate(hotel);
        int numViolations = violations.size();
        if(numViolations > 0) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }
        
        // make sure phone number not used by other hotel
        String phoneNum = hotel.getPhoneNumber();
        Hotel existingHotel = repository.findByPhoneNumber(phoneNum);
        if(existingHotel != null) {
            Long existingId = existingHotel.getId();
            Long currentId = hotel.getId();
            if(!existingId.equals(currentId)) {
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
