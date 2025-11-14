package uk.ac.newcastle.enterprisemiddleware.booking;

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
public class BookingService {

    @Inject
    Validator validator;

    @Inject
    BookingRepository repository;

    public List<Booking> findAllOrderedByDate() {
        List<Booking> bookings = repository.findAllOrderedByDate();
        return bookings;
    }

    public Booking findById(Long id) {
        Booking booking = repository.findById(id);
        return booking;
    }

    public List<Booking> findByCustomerId(Long customerId) {
        List<Booking> bookings = repository.findByCustomerId(customerId);
        return bookings;
    }

    public List<Booking> findByHotelId(Long hotelId) {
        List<Booking> bookings = repository.findByHotelId(hotelId);
        return bookings;
    }

    @Transactional
    public Booking create(Booking booking) throws Exception {
        // do validation
        Set<ConstraintViolation<Booking>> violations = validator.validate(booking);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }

        // check if booking already exists
        List<Booking> customerBookings = repository.findByCustomerId(booking.getCustomer().getId());
        boolean duplicateFound = false;
        for(int i = 0; i < customerBookings.size(); i++) {
            Booking existingBooking = customerBookings.get(i);
            if(existingBooking.getHotel().getId().equals(booking.getHotel().getId())) {
                if(existingBooking.getBookingDate().equals(booking.getBookingDate())) {
                    if(booking.getId() == null) {
                        duplicateFound = true;
                        break;
                    } else {
                        if(!existingBooking.getId().equals(booking.getId())) {
                            duplicateFound = true;
                            break;
                        }
                    }
                }
            }
        }
        
        if(duplicateFound) {
            throw new Exception("This customer already has a booking at this hotel on this date");
        }

        Booking createdBooking = repository.create(booking);
        return createdBooking;
    }

    @Transactional
    public Booking update(Booking booking) throws Exception {
        Set<ConstraintViolation<Booking>> violations = validator.validate(booking);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }
        Booking updatedBooking = repository.update(booking);
        return updatedBooking;
    }

    @Transactional
    public void delete(Booking booking) {
        repository.delete(booking);
    }
}
