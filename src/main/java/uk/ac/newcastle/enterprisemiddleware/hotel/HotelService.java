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
        return repository.findAllOrderedByName();
    }

    public Hotel findById(Long id) {
        return repository.findById(id);
    }

    public Hotel findByPhoneNumber(String phoneNumber) {
        return repository.findByPhoneNumber(phoneNumber);
    }

    @Transactional
    public Hotel create(Hotel hotel) throws Exception {
        validateHotel(hotel);
        
        if (phoneNumberAlreadyExists(hotel.getPhoneNumber(), hotel.getId())) {
            throw new Exception("Phone number already exists");
        }

        return repository.create(hotel);
    }

    @Transactional
    public Hotel update(Hotel hotel) throws Exception {
        validateHotel(hotel);
        
        if (phoneNumberAlreadyExists(hotel.getPhoneNumber(), hotel.getId())) {
            throw new Exception("Phone number already exists");
        }

        return repository.update(hotel);
    }

    @Transactional
    public void delete(Hotel hotel) {
        repository.delete(hotel);
    }

    private void validateHotel(Hotel hotel) throws ConstraintViolationException {
        Set<ConstraintViolation<Hotel>> violations = validator.validate(hotel);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }
    }

    private boolean phoneNumberAlreadyExists(String phoneNumber, Long id) {
        Hotel existingHotel = repository.findByPhoneNumber(phoneNumber);
        if (existingHotel != null && !existingHotel.getId().equals(id)) {
            return true;
        }
        return false;
    }
}
