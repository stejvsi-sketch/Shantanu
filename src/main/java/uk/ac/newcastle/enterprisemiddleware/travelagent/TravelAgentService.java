package uk.ac.newcastle.enterprisemiddleware.travelagent;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.booking.BookingService;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerService;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;
import uk.ac.newcastle.enterprisemiddleware.hotel.HotelService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class TravelAgentService {

    @Inject
    TravelAgentRepository repository;

    @Inject
    CustomerService customerService;

    @Inject
    HotelService hotelService;

    @Inject
    BookingService bookingService;

    @Inject
    @RestClient
    TaxiBookingService taxiBookingService;

    @Inject
    @RestClient
    FlightBookingService flightBookingService;

    private Logger log = Logger.getLogger(TravelAgentService.class.getName());

    public List<TravelAgentBooking> findAll() {
        return repository.findAll();
    }

    public TravelAgentBooking findById(Long id) {
        return repository.findById(id);
    }

    public List<TravelAgentBooking> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId);
    }

    @Transactional
    public TravelAgentBooking create(BookingRequest request) throws Exception {
        Customer customer = customerService.findById(request.getCustomerId());
        if (customer == null) {
            throw new Exception("Customer not found");
        }

        Hotel hotel = hotelService.findById(request.getHotelId());
        if (hotel == null) {
            throw new Exception("Hotel not found");
        }

        ExternalBooking taxiBooking = null;
        ExternalBooking flightBooking = null;
        Booking hotelBooking = null;

        try {
            Map<String, Object> taxiBookingData = new HashMap<>();
            taxiBookingData.put("customerId", request.getCustomerId());
            taxiBookingData.put("taxiId", request.getTaxiId());
            taxiBookingData.put("bookingDate", request.getBookingDate());
            taxiBooking = taxiBookingService.createBooking(taxiBookingData);
            log.info("Taxi booking created: " + taxiBooking.getId());

            Map<String, Object> flightBookingData = new HashMap<>();
            flightBookingData.put("customerId", request.getCustomerId());
            flightBookingData.put("flightId", request.getFlightId());
            flightBookingData.put("bookingDate", request.getBookingDate());
            flightBooking = flightBookingService.createBooking(flightBookingData);
            log.info("Flight booking created: " + flightBooking.getId());

            Booking localHotelBooking = new Booking();
            localHotelBooking.setCustomer(customer);
            localHotelBooking.setHotel(hotel);
            localHotelBooking.setBookingDate(request.getBookingDate());
            hotelBooking = bookingService.create(localHotelBooking);
            log.info("Hotel booking created: " + hotelBooking.getId());

            TravelAgentBooking travelAgentBooking = new TravelAgentBooking();
            travelAgentBooking.setCustomerId(request.getCustomerId());
            travelAgentBooking.setHotelBooking(hotelBooking);
            travelAgentBooking.setTaxiBookingId(taxiBooking.getId());
            travelAgentBooking.setFlightBookingId(flightBooking.getId());

            return repository.create(travelAgentBooking);

        } catch (Exception e) {
            log.warning("Booking failed, rolling back: " + e.getMessage());

            if (taxiBooking != null && taxiBooking.getId() != null) {
                try {
                    taxiBookingService.deleteBooking(taxiBooking.getId());
                    log.info("Cancelled taxi booking: " + taxiBooking.getId());
                } catch (Exception ex) {
                    log.severe("Failed to rollback taxi booking: " + ex.getMessage());
                }
            }

            if (flightBooking != null && flightBooking.getId() != null) {
                try {
                    flightBookingService.deleteBooking(flightBooking.getId());
                    log.info("Cancelled flight booking: " + flightBooking.getId());
                } catch (Exception ex) {
                    log.severe("Failed to rollback flight booking: " + ex.getMessage());
                }
            }

            if (hotelBooking != null && hotelBooking.getId() != null) {
                try {
                    bookingService.delete(hotelBooking);
                    log.info("Cancelled hotel booking: " + hotelBooking.getId());
                } catch (Exception ex) {
                    log.severe("Failed to rollback hotel booking: " + ex.getMessage());
                }
            }

            throw new Exception("Failed to create travel agent booking: " + e.getMessage());
        }
    }

    @Transactional
    public void delete(TravelAgentBooking travelAgentBooking) throws Exception {
        try {
            taxiBookingService.deleteBooking(travelAgentBooking.getTaxiBookingId());
            log.info("Deleted taxi booking: " + travelAgentBooking.getTaxiBookingId());
        } catch (Exception e) {
            log.warning("Failed to delete taxi booking: " + e.getMessage());
        }

        try {
            flightBookingService.deleteBooking(travelAgentBooking.getFlightBookingId());
            log.info("Deleted flight booking: " + travelAgentBooking.getFlightBookingId());
        } catch (Exception e) {
            log.warning("Failed to delete flight booking: " + e.getMessage());
        }

        try {
            bookingService.delete(travelAgentBooking.getHotelBooking());
            log.info("Deleted hotel booking: " + travelAgentBooking.getHotelBooking().getId());
        } catch (Exception e) {
            log.warning("Failed to delete hotel booking: " + e.getMessage());
        }

        repository.delete(travelAgentBooking);
    }
}
