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

// REST API for bookings
@Path("/bookings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BookingRestService {

    @Inject
    BookingService service; // booking service

    @Inject
    CustomerService customerService; // need this to check customers

    @Inject
    HotelService hotelService; // need this to check hotels

    private Logger log = Logger.getLogger(BookingRestService.class.getName()); // for logging


    // endpoint to get all bookings
    @GET
    @Operation(summary = "Fetch all Bookings", description = "Returns a JSON array of all stored Booking objects")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Successful retrieval of bookings",
            content = @Content(schema = @Schema(implementation = Booking.class))),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response retrieveAllBookings() {
        //System.out.println("Getting all bookings");
        List<Booking> bookings = service.findAllOrderedByDate();
        Response response = Response.ok(bookings).build();
        return response;
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

    // create booking - takes 3 params: customerId, hotelId, date
    @POST
    @Operation(summary = "Create a new Booking", description = "Creates a new booking with customerId, hotelId and date")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Booking created successfully",
            content = @Content(schema = @Schema(implementation = Booking.class))),
        @APIResponse(responseCode = "400", description = "Invalid Booking supplied"),
        @APIResponse(responseCode = "404", description = "Customer or Hotel not found"),
        @APIResponse(responseCode = "409", description = "Booking already exists for this date")
    })
    public Response createBooking(BookingCreate bookingReq) {
        // check if request is null
        if(bookingReq == null) {
            throw new WebApplicationException("Invalid booking data", Response.Status.BAD_REQUEST);
        }
        
        // check customer id
        if(bookingReq.customerId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Customer ID is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        
        // check hotel id
        if(bookingReq.hotelId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Hotel ID is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        
        // check date
        if(bookingReq.date == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Date is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        // get the customer from database
        Long customerId = bookingReq.customerId;
        //System.out.println("Looking for customer id: " + customerId);
        Customer customer = customerService.findById(customerId);
        if(customer == null) {
            //System.out.println("Customer not found in database");
            String msg = "Customer not found";
            throw new WebApplicationException(msg, Response.Status.NOT_FOUND);
        }
        //System.out.println("Customer found: " + customer.getName());

        // get the hotel from database
        Long hotelId = bookingReq.hotelId;
        //System.out.println("Looking for hotel id: " + hotelId);
        Hotel hotel = hotelService.findById(hotelId);
        if(hotel == null) {
            //System.out.println("Hotel not found in database");
            String msg = "Hotel not found";
            throw new WebApplicationException(msg, Response.Status.NOT_FOUND);
        }
        //System.out.println("Hotel found: " + hotel.getName());

        // create the booking object
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setHotel(hotel);
        booking.setBookingDate(bookingReq.date);
        
        Booking created = null;
        boolean creationSuccessful = false;
        
        try {
            //System.out.println("Creating booking...");
            created = service.create(booking);
            creationSuccessful = true;
            //System.out.println("Booking created with id: " + created.getId());
        } catch(ConstraintViolationException e) {
            // validation failed
            //System.out.println("Validation error: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Validation failed");
            String details = e.getMessage();
            error.put("details", details);
            Response resp = Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            return resp;
        } catch(Exception e) {
            // some other error
            //System.out.println("Error creating booking: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            String errorMessage = e.getMessage();
            error.put("error", errorMessage);
            Response resp = Response.status(Response.Status.CONFLICT).entity(error).build();
            return resp;
        }
        
        if(creationSuccessful == true) {
            //System.out.println("Success!");
        }
        
        // return simple response with just id for REST client compatibility
        Map<String, Object> result = new HashMap<>();
        result.put("id", created.getId());
        Response resp = Response.status(Response.Status.CREATED).entity(result).build();
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
