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

// this handles bookings
@ApplicationScoped
public class BookingService {

    @Inject
    Validator validator; // validates bookings

    @Inject
    BookingRepository repository; // talks to database

    // gets all bookings
    public List<Booking> findAllOrderedByDate() {
        //System.out.println("Getting all bookings");
        List<Booking> bookings = repository.findAllOrderedByDate();
        return bookings;
    }

    // get one booking
    public Booking findById(Long id) {
        Booking booking = repository.findById(id);
        return booking;
    }

    // get bookings for customer
    public List<Booking> findByCustomerId(Long customerId) {
        List<Booking> bookings = repository.findByCustomerId(customerId);
        return bookings;
    }

    // get bookings for hotel
    public List<Booking> findByHotelId(Long hotelId) {
        List<Booking> bookings = repository.findByHotelId(hotelId);
        return bookings;
    }

    // create new booking
    @Transactional
    public Booking create(Booking booking) throws Exception {
        //System.out.println("Creating booking");
        
        // validate booking first
        Set<ConstraintViolation<Booking>> violations = validator.validate(booking);
        int violationCount = violations.size();
        if(violationCount > 0) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }

        // need to check if this booking already exists
        Long customerId = booking.getCustomer().getId();
        List<Booking> customerBookings = repository.findByCustomerId(customerId);
        
        // loop through all bookings to check for duplicates
        boolean isDuplicate = false;
        int numBookings = customerBookings.size();
        for(int i = 0; i < numBookings; i++) {
            Booking existingBooking = customerBookings.get(i);
            Long existingHotelId = existingBooking.getHotel().getId();
            Long newHotelId = booking.getHotel().getId();
            
            // check if same hotel
            if(existingHotelId.equals(newHotelId)) {
                // check if same date
                if(existingBooking.getBookingDate().equals(booking.getBookingDate())) {
                    // check if its a new booking or update
                    Long newBookingId = booking.getId();
                    if(newBookingId == null) {
                        // new booking, so its a duplicate
                        isDuplicate = true;
                        break;
                    } else {
                        // updating existing
                        Long existingBookingId = existingBooking.getId();
                        if(!existingBookingId.equals(newBookingId)) {
                            isDuplicate = true;
                            break;
                        }
                    }
                }
            }
        }
        
        if(isDuplicate == true) {
            throw new Exception("This customer already has a booking at this hotel on this date");
        }

        Booking createdBooking = repository.create(booking);
        //System.out.println("Booking created");
        return createdBooking;
    }

    // update existing booking
    @Transactional
    public Booking update(Booking booking) throws Exception {
        // validate
        Set<ConstraintViolation<Booking>> violations = validator.validate(booking);
        int count = violations.size();
        if(count > 0) {
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
