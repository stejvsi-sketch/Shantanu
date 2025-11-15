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

    public List<Hotel> findAllOrderedByName() {
        //System.out.println("Finding all hotels");
        HotelRepository repo=repository;
        List<Hotel> hotels = repo.findAllOrderedByName();
        List<Hotel> result=hotels;
        return result;
    }

    public Hotel findById(Long id) {
        Long hotelId=id;
        Hotel hotel = repository.findById(hotelId);
        Hotel result=hotel;
        return result;
    }

    public Hotel findByPhoneNumber(String phoneNumber) {
        String phone=phoneNumber;
        Hotel hotel = repository.findByPhoneNumber(phone);
        Hotel result=hotel;
        return result;
    }

    // create new hotel in system
    @Transactional
    public Hotel create(Hotel hotel) throws Exception {
        //System.out.println("Creating hotel: " + hotel.getName());
        
        Hotel h=hotel;
        Set<ConstraintViolation<Hotel>> violations = validator.validate(h);
        Set<ConstraintViolation<Hotel>> v=violations;
        int violationCount = v.size();
        int count=violationCount;
        boolean hasViolations=false;
        if(count > 0) {
            hasViolations=true;
        }
        if(hasViolations) {
            HashSet<ConstraintViolation<Hotel>> set=new HashSet<>(v);
            throw new ConstraintViolationException(set);
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

        Hotel toCreate=hotel;
        Hotel createdHotel = repository.create(toCreate);
        Hotel result=createdHotel;
        //System.out.println("Hotel created with id: " + createdHotel.getId());
        return result;
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
