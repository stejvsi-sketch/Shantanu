package uk.ac.newcastle.enterprisemiddleware.travelagent;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/travel-agent")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TravelAgentRestService {

    @Inject
    TravelAgentService service;

    private Logger log = Logger.getLogger(TravelAgentRestService.class.getName());

    @GET
    @Operation(summary = "Fetch all Travel Agent Bookings", 
        description = "Returns a JSON array of all travel agent bookings")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Successful retrieval",
            content = @Content(schema = @Schema(implementation = TravelAgentBooking.class))),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response retrieveAllBookings() {
        List<TravelAgentBooking> bookings = service.findAll();
        return Response.ok(bookings).build();
    }

    @GET
    @Path("/{id:[0-9]+}")
    @Operation(summary = "Fetch a Travel Agent Booking by id", 
        description = "Returns a single travel agent booking")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Booking found",
            content = @Content(schema = @Schema(implementation = TravelAgentBooking.class))),
        @APIResponse(responseCode = "404", description = "Booking not found")
    })
    public Response retrieveBookingById(@PathParam("id") long id) {
        TravelAgentBooking booking = service.findById(id);
        if (booking == null) {
            throw new WebApplicationException("Travel agent booking not found", Response.Status.NOT_FOUND);
        }
        return Response.ok(booking).build();
    }

    @GET
    @Path("/customer/{customerId:[0-9]+}")
    @Operation(summary = "Fetch Travel Agent Bookings by customer", 
        description = "Returns all travel agent bookings for a specific customer")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Successful retrieval",
            content = @Content(schema = @Schema(implementation = TravelAgentBooking.class))),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response retrieveBookingsByCustomerId(@PathParam("customerId") long customerId) {
        List<TravelAgentBooking> bookings = service.findByCustomerId(customerId);
        return Response.ok(bookings).build();
    }

    @POST
    @Operation(summary = "Create a Travel Agent Booking", 
        description = "Creates a new travel agent booking with hotel, taxi, and flight")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Booking created successfully",
            content = @Content(schema = @Schema(implementation = TravelAgentBooking.class))),
        @APIResponse(responseCode = "400", description = "Invalid data supplied"),
        @APIResponse(responseCode = "404", description = "Customer or commodity not found"),
        @APIResponse(responseCode = "500", description = "Booking creation failed")
    })
    public Response createBooking(BookingRequest request) {
        if (request == null) {
            throw new WebApplicationException("Invalid booking request", Response.Status.BAD_REQUEST);
        }

        try {
            TravelAgentBooking created = service.create(request);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (Exception e) {
            log.severe("Failed to create travel agent booking: " + e.getMessage());
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", "Failed to create booking");
            responseObj.put("details", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseObj).build();
        }
    }

    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(summary = "Cancel a Travel Agent Booking", 
        description = "Cancels a travel agent booking and all associated commodity bookings")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "Booking cancelled successfully"),
        @APIResponse(responseCode = "404", description = "Booking not found"),
        @APIResponse(responseCode = "500", description = "Failed to cancel booking")
    })
    public Response deleteBooking(@PathParam("id") long id) {
        TravelAgentBooking booking = service.findById(id);
        if (booking == null) {
            throw new WebApplicationException("Travel agent booking not found", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(booking);
            return Response.noContent().build();
        } catch (Exception e) {
            log.severe("Failed to delete travel agent booking: " + e.getMessage());
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", "Failed to cancel booking");
            responseObj.put("details", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseObj).build();
        }
    }
}
