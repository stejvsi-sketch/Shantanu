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

// booking service handles all booking business logic
@ApplicationScoped
public class BookingService {

    @Inject
    Validator validator;

    @Inject
    BookingRepository repository;

    public List<Booking> findAllOrderedByDate() {
        return repository.findAllOrderedByDate();
    }

    public Booking findById(Long id) {
        return repository.findById(id);
    }

    // get bookings for a specific customer
    public List<Booking> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId);
    }

    public List<Booking> findByHotelId(Long hotelId) {
        return repository.findByHotelId(hotelId);
    }


    // create new booking - checks for duplicates first
    @Transactional
    public Booking create(Booking booking) throws Exception {
        validateBooking(booking);

        // prevent duplicate bookings
        if(bookingAlreadyExists(booking)) {
            throw new Exception("This customer already has a booking at this hotel on this date");
        }

        return repository.create(booking);
    }

    @Transactional
    public Booking update(Booking booking) throws Exception {
        validateBooking(booking);
        return repository.update(booking);
    }

    @Transactional
    public void delete(Booking booking) {
        repository.delete(booking);
    }

    private void validateBooking(Booking booking) throws ConstraintViolationException {
        Set<ConstraintViolation<Booking>> violations = validator.validate(booking);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }
    }

    // check if same customer already booked same hotel on same date
    private boolean bookingAlreadyExists(Booking booking) {
        List<Booking> customerBookings = repository.findByCustomerId(booking.getCustomer().getId());
        for(Booking existingBooking : customerBookings) {
            if(existingBooking.getHotel().getId().equals(booking.getHotel().getId()) &&
                existingBooking.getBookingDate().equals(booking.getBookingDate()) &&
                !existingBooking.getId().equals(booking.getId())) {
                return true;
            }
        }
        return false;
    }
}
