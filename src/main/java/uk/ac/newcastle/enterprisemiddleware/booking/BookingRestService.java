package uk.ac.newcastle.enterprisemiddleware.booking;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerService;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;
import uk.ac.newcastle.enterprisemiddleware.hotel.HotelService;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

// REST endpoints for booking operations
@Path("/bookings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BookingRestService {

    @Inject
    BookingService service;

    @Inject
    CustomerService customerService;

    @Inject
    HotelService hotelService;

    private Logger log = Logger.getLogger(BookingRestService.class.getName());


    // get all bookings
    @GET
    @Operation(summary = "Fetch all Bookings", description = "Returns a JSON array of all stored Booking objects")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Successful retrieval of bookings",
            content = @Content(schema = @Schema(implementation = Booking.class))),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response retrieveAllBookings() {
        List<Booking> bookings = service.findAllOrderedByDate();
        return Response.ok(bookings).build();
    }

    @GET
    @Path("/{id:[0-9]+}")
    @Operation(summary = "Fetch a Booking by id", description = "Returns a single Booking object based on the provided id")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Booking found",
            content = @Content(schema = @Schema(implementation = Booking.class))),
        @APIResponse(responseCode = "404", description = "Booking with id not found")
    })
    public Response retrieveBookingById(@PathParam("id") long id) {
        Booking booking = service.findById(id);
        if(booking == null) {
            throw new WebApplicationException("Booking with id " + id + " not found", Response.Status.NOT_FOUND);
        }
        return Response.ok(booking).build();
    }

    // get bookings for specific customer
    @GET
    @Path("/customer/{customerId:[0-9]+}")
    @Operation(summary = "Fetch all Bookings for a Customer", description = "Returns bookings for a specific customer")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Successful retrieval of customer bookings",
            content = @Content(schema = @Schema(implementation = Booking.class))),
        @APIResponse(responseCode = "404", description = "Customer not found")
    })
    public Response retrieveBookingsByCustomerId(@PathParam("customerId") long customerId) {
        Customer customer = customerService.findById(customerId);
        if(customer == null) {
            throw new WebApplicationException("Customer with id " + customerId + " not found", Response.Status.NOT_FOUND);
        }
        List<Booking> bookings = service.findByCustomerId(customerId);
        return Response.ok(bookings).build();
    }

    // create booking
    @POST
    @Operation(summary = "Create a new Booking", description = "Creates a new booking from the provided JSON object")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Booking created successfully",
            content = @Content(schema = @Schema(implementation = Booking.class))),
        @APIResponse(responseCode = "400", description = "Invalid Booking supplied"),
        @APIResponse(responseCode = "404", description = "Customer or Hotel not found"),
        @APIResponse(responseCode = "409", description = "Booking already exists for this date")
    })
    public Response createBooking(Booking booking) {
        if(booking == null) {
            throw new WebApplicationException("Invalid booking data", Response.Status.BAD_REQUEST);
        }

        // check customer is set
        if(booking.getCustomer() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Customer is required");
            Response resp = Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            return resp;
        }
        if(booking.getCustomer().getId() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Customer is required");
            Response resp = Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            return resp;
        }

        // check hotel is set
        if(booking.getHotel() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Hotel is required");
            Response resp = Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            return resp;
        }
        if(booking.getHotel().getId() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Hotel is required");
            Response resp = Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            return resp;
        }

        // get customer from database
        Long customerId = booking.getCustomer().getId();
        Customer customer = customerService.findById(customerId);
        if(customer == null) {
            throw new WebApplicationException("Customer not found", Response.Status.NOT_FOUND);
        }

        // get hotel from database
        Long hotelId = booking.getHotel().getId();
        Hotel hotel = hotelService.findById(hotelId);
        if(hotel == null) {
            throw new WebApplicationException("Hotel not found", Response.Status.NOT_FOUND);
        }

        booking.setId(null);
        booking.setCustomer(customer);
        booking.setHotel(hotel);
        
        Booking created = null;
        try {
            created = service.create(booking);
        } catch(ConstraintViolationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Validation failed");
            error.put("details", e.getMessage());
            Response resp = Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            return resp;
        } catch(Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            Response resp = Response.status(Response.Status.CONFLICT).entity(error).build();
            return resp;
        }
        
        Response resp = Response.status(Response.Status.CREATED).entity(created).build();
        return resp;
    }

    // cancel booking
    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(summary = "Cancel a Booking", description = "Deletes/cancels a booking")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "Booking cancelled successfully"),
        @APIResponse(responseCode = "404", description = "Booking with id not found")
    })
    public Response deleteBooking(@PathParam("id") long id) {
        Booking booking = service.findById(id);
        if(booking == null) {
            throw new WebApplicationException("Booking with id " + id + " not found", Response.Status.NOT_FOUND);
        }

        service.delete(booking);
        return Response.noContent().build();
    }
}
