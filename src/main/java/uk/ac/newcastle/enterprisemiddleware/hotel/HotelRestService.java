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

// REST endpoints for hotels
@Path("/hotels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HotelRestService {

    @Inject
    HotelService service; // hotel service

    private Logger log = Logger.getLogger(HotelRestService.class.getName()); // logger for debugging

    // endpoint to get all hotels
    @GET
    @Operation(summary = "Fetch all Hotels", description = "Returns a JSON array of all stored Hotel objects")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Successful retrieval of hotels",
            content = @Content(schema = @Schema(implementation = Hotel.class))),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response retrieveAllHotels() {
        //System.out.println("Getting all hotels from database");
        List<Hotel> hotels = service.findAllOrderedByName();
        Response response = Response.ok(hotels).build();
        return response;
    }

    // get one hotel by id
    @GET
    @Path("/{id:[0-9]+}")
    @Operation(summary = "Fetch a Hotel by id", description = "Returns a single Hotel object based on the provided id")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Hotel found",
            content = @Content(schema = @Schema(implementation = Hotel.class))),
        @APIResponse(responseCode = "404", description = "Hotel with id not found")
    })
    public Response retrieveHotelById(@PathParam("id") long id) {
        //System.out.println("Looking for hotel with id: " + id);
        Hotel hotel = service.findById(id);
        if(hotel == null) {
            String errorMsg = "Hotel with id " + id + " not found";
            throw new WebApplicationException(errorMsg, Response.Status.NOT_FOUND);
        }
        Response response = Response.ok(hotel).build();
        return response;
    }

    // create new hotel endpoint
    @POST
    @Operation(summary = "Create a new Hotel", description = "Creates a new hotel from the provided JSON object")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Hotel created successfully",
            content = @Content(schema = @Schema(implementation = Hotel.class))),
        @APIResponse(responseCode = "400", description = "Invalid Hotel supplied"),
        @APIResponse(responseCode = "409", description = "Hotel with that phone number already exists")
    })
    public Response createHotel(Hotel hotel) {
        //System.out.println("Create hotel request received");
        
        // check if hotel object is null
        if(hotel == null) {
            String msg = "Invalid hotel data";
            throw new WebApplicationException(msg, Response.Status.BAD_REQUEST);
        }

        // set id to null for new hotels
        hotel.setId(null);
        Hotel created = null;
        boolean wasCreated = false;
        
        try {
            created = service.create(hotel);
            wasCreated = true;
        } catch(ConstraintViolationException e) {
            // validation error
            //System.out.println("Validation error: " + e.getMessage());
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", "Validation failed");
            String details = e.getMessage();
            responseObj.put("details", details);
            Response resp = Response.status(Response.Status.BAD_REQUEST).entity(responseObj).build();
            return resp;
        } catch(Exception e) {
            // other errors like duplicate phone
            //System.out.println("Error: " + e.getMessage());
            Map<String, String> responseObj = new HashMap<>();
            String errorMessage = e.getMessage();
            responseObj.put("error", errorMessage);
            Response resp = Response.status(Response.Status.CONFLICT).entity(responseObj).build();
            return resp;
        }
        
        if(wasCreated == true) {
            //System.out.println("Hotel created with id: " + created.getId());
        }
        
        Response resp = Response.status(Response.Status.CREATED).entity(created).build();
        return resp;
    }

    // delete hotel endpoint
    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(summary = "Delete a Hotel", description = "Deletes a hotel and all associated bookings")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "Hotel deleted successfully"),
        @APIResponse(responseCode = "404", description = "Hotel with id not found")
    })
    public Response deleteHotel(@PathParam("id") long id) {
        //System.out.println("Delete hotel request for id: " + id);
        Hotel hotel = service.findById(id);
        if(hotel == null) {
            String msg = "Hotel with id " + id + " not found";
            throw new WebApplicationException(msg, Response.Status.NOT_FOUND);
        }

        service.delete(hotel);
        //System.out.println("Hotel deleted");
        Response resp = Response.noContent().build();
        return resp;
    }
}
