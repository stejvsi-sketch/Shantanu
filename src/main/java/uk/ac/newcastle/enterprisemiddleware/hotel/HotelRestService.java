package uk.ac.newcastle.enterprisemiddleware.hotel;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

// REST API for hotel management
@Path("/hotels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HotelRestService {

    @Inject
    HotelService service;

    private Logger log = Logger.getLogger(HotelRestService.class.getName());

    // GET all hotels
    @GET
    @Operation(summary = "Fetch all Hotels", description = "Returns a JSON array of all stored Hotel objects")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Successful retrieval of hotels",
            content = @Content(schema = @Schema(implementation = Hotel.class))),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response retrieveAllHotels() {
        List<Hotel> hotels = service.findAllOrderedByName();
        return Response.ok(hotels).build();
    }

    @GET
    @Path("/{id:[0-9]+}")
    @Operation(summary = "Fetch a Hotel by id", description = "Returns a single Hotel object based on the provided id")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Hotel found",
            content = @Content(schema = @Schema(implementation = Hotel.class))),
        @APIResponse(responseCode = "404", description = "Hotel with id not found")
    })
    public Response retrieveHotelById(@PathParam("id") long id) {
        Hotel hotel = service.findById(id);
        if(hotel == null) {
            throw new WebApplicationException("Hotel with id " + id + " not found", Response.Status.NOT_FOUND);
        }
        return Response.ok(hotel).build();
    }

    // POST create new hotel
    @POST
    @Operation(summary = "Create a new Hotel", description = "Creates a new hotel from the provided JSON object")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Hotel created successfully",
            content = @Content(schema = @Schema(implementation = Hotel.class))),
        @APIResponse(responseCode = "400", description = "Invalid Hotel supplied"),
        @APIResponse(responseCode = "409", description = "Hotel with that phone number already exists")
    })
    public Response createHotel(Hotel hotel) {
        if(hotel == null) {
            throw new WebApplicationException("Invalid hotel data", Response.Status.BAD_REQUEST);
        }

        try {
            hotel.setId(null);
            Hotel created = service.create(hotel);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch(ConstraintViolationException e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", "Validation failed");
            responseObj.put("details", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(responseObj).build();
        } catch(Exception e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", e.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(responseObj).build();
        }
    }

    // DELETE hotel
    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(summary = "Delete a Hotel", description = "Deletes a hotel and all associated bookings")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "Hotel deleted successfully"),
        @APIResponse(responseCode = "404", description = "Hotel with id not found")
    })
    public Response deleteHotel(@PathParam("id") long id) {
        Hotel hotel = service.findById(id);
        if(hotel == null) {
            throw new WebApplicationException("Hotel with id " + id + " not found", Response.Status.NOT_FOUND);
        }

        service.delete(hotel);
        return Response.noContent().build();
    }
}
