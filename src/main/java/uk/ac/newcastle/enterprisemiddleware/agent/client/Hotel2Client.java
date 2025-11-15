package uk.ac.newcastle.enterprisemiddleware.agent.client;

import uk.ac.newcastle.enterprisemiddleware.agent.BookingResult;
import uk.ac.newcastle.enterprisemiddleware.agent.CustomerResult;
import uk.ac.newcastle.enterprisemiddleware.agent.DownstreamCustomerCreate;
import uk.ac.newcastle.enterprisemiddleware.agent.Hotel2BookingCreate;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "hotel2-api")
public interface Hotel2Client {
    
    @POST
    @Path("/customers")
    CustomerResult createCustomer(DownstreamCustomerCreate req);
    
    @GET
    @Path("/customers")
    List<CustomerResult> listCustomers();
    
    @POST
    @Path("/hotel-booking")
    BookingResult createBooking(Hotel2BookingCreate req);
    
    @DELETE
    @Path("/hotel-booking/{id}")
    void cancelBooking(@PathParam("id") Long bookingId);
}
