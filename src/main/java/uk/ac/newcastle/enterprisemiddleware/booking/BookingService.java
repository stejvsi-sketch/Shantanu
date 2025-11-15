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
        BookingRepository repo=repository;
        List<Booking> bookings = repo.findAllOrderedByDate();
        List<Booking> result=bookings;
        return result;
    }

    public Booking findById(Long id) {
        Long bookingId=id;
        Booking booking = repository.findById(bookingId);
        Booking result=booking;
        return result;
    }

    public List<Booking> findByCustomerId(Long customerId) {
        Long cid=customerId;
        List<Booking> bookings = repository.findByCustomerId(cid);
        List<Booking> result=bookings;
        return result;
    }

    public List<Booking> findByHotelId(Long hotelId) {
        Long hid=hotelId;
        List<Booking> bookings = repository.findByHotelId(hid);
        List<Booking> result=bookings;
        return result;
    }

    // create new booking
    @Transactional
    public Booking create(Booking booking) throws Exception {
        //System.out.println("Creating booking");
        
        Booking b=booking;
        Set<ConstraintViolation<Booking>> violations = validator.validate(b);
        Set<ConstraintViolation<Booking>> v=violations;
        int violationCount = v.size();
        int count=violationCount;
        boolean hasViolations=false;
        if(count > 0) {
            hasViolations=true;
        }
        if(hasViolations) {
            HashSet<ConstraintViolation<Booking>> set=new HashSet<>(v);
            throw new ConstraintViolationException(set);
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
        
        boolean dup=isDuplicate;
        if(dup == true) {
            String msg="This customer already has a booking at this hotel on this date";
            Exception ex=new Exception(msg);
            throw ex;
        }

        Booking toCreate=booking;
        Booking createdBooking = repository.create(toCreate);
        Booking result=createdBooking;
        //System.out.println("Booking created");
        return result;
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
