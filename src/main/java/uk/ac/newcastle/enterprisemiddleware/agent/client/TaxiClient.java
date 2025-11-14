package uk.ac.newcastle.enterprisemiddleware.agent.client;

import uk.ac.newcastle.enterprisemiddleware.agent.BookingResult;
import uk.ac.newcastle.enterprisemiddleware.agent.CustomerResult;
import uk.ac.newcastle.enterprisemiddleware.agent.DownstreamCustomerCreate;
import uk.ac.newcastle.enterprisemiddleware.agent.TaxiBookingCreate;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

// REST client for taxi service (external)
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "taxi-api")
public interface TaxiClient {
    
    @POST
    @Path("/customers")
    CustomerResult createCustomer(DownstreamCustomerCreate req);
    
    @GET
    @Path("/customers")
    List<CustomerResult> listCustomers();
    
    @POST
    @Path("/bookings")
    BookingResult createBooking(TaxiBookingCreate req);
    
    @DELETE
    @Path("/bookings/{id}")
    void cancelBooking(@PathParam("id") Long bookingId);
}
