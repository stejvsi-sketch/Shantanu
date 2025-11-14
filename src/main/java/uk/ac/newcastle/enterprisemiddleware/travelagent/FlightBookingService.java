package uk.ac.newcastle.enterprisemiddleware.travelagent;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/bookings")
@RegisterRestClient(configKey = "flight-api")
public interface FlightBookingService {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ExternalBooking createBooking(Map<String, Object> booking);

    @DELETE
    @Path("/{id}")
    void deleteBooking(@PathParam("id") Long id);
}
