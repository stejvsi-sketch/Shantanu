package uk.ac.newcastle.enterprisemiddleware.guestbooking;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.booking.BookingService;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerService;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;
import uk.ac.newcastle.enterprisemiddleware.hotel.HotelService;

import javax.inject.Inject;
import javax.transaction.*;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Path("/guest-bookings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GuestBookingRestService {

    @Inject
    CustomerService customerService;

    @Inject
    BookingService bookingService;

    @Inject
    HotelService hotelService;

    @Inject
    UserTransaction userTransaction;

    private Logger log = Logger.getLogger(GuestBookingRestService.class.getName());

    @POST
    @Operation(summary = "Create a guest booking", 
        description = "Creates a new customer and booking in a single transaction")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Guest booking created successfully",
            content = @Content(schema = @Schema(implementation = Booking.class))),
        @APIResponse(responseCode = "400", description = "Invalid data supplied"),
        @APIResponse(responseCode = "404", description = "Hotel not found"),
        @APIResponse(responseCode = "409", description = "Conflict - customer email or booking already exists"),
        @APIResponse(responseCode = "500", description = "Transaction failed")
    })
    public Response createGuestBooking(GuestBooking guestBooking) {
        if (guestBooking == null || guestBooking.getCustomer() == null || guestBooking.getBooking() == null) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", "Both customer and booking data are required");
            return Response.status(Response.Status.BAD_REQUEST).entity(responseObj).build();
        }

        Customer customer = guestBooking.getCustomer();
        Booking booking = guestBooking.getBooking();

        if (booking.getHotel() == null || booking.getHotel().getId() == null) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", "Hotel ID is required in booking");
            return Response.status(Response.Status.BAD_REQUEST).entity(responseObj).build();
        }

        Hotel hotel = hotelService.findById(booking.getHotel().getId());
        if (hotel == null) {
            throw new WebApplicationException("Hotel not found", Response.Status.NOT_FOUND);
        }

        try {
            userTransaction.begin();
            log.info("Starting guest booking transaction");

            customer.setId(null);
            Customer createdCustomer = customerService.create(customer);
            log.info("Created customer ID: " + createdCustomer.getId());

            booking.setId(null);
            booking.setCustomer(createdCustomer);
            booking.setHotel(hotel);
            Booking createdBooking = bookingService.create(booking);
            log.info("Created booking ID: " + createdBooking.getId());

            userTransaction.commit();
            log.info("Transaction committed");

            return Response.status(Response.Status.CREATED).entity(createdBooking).build();

        } catch (ConstraintViolationException e) {
            try {
                userTransaction.rollback();
                log.warning("Rolled back - validation error");
            } catch (Exception rollbackEx) {
                log.severe("Failed to rollback transaction: " + rollbackEx.getMessage());
            }
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", "Validation failed");
            responseObj.put("details", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(responseObj).build();

        } catch (Exception e) {
            try {
                userTransaction.rollback();
                log.warning("Rolled back - " + e.getMessage());
            } catch (Exception rollbackEx) {
                log.severe("Failed to rollback transaction: " + rollbackEx.getMessage());
            }
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", "Transaction failed");
            responseObj.put("details", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseObj).build();
        }
    }
}
