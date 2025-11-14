package com.example.agent.client;

import com.example.agent.BookingResult;
import com.example.agent.DownstreamCustomerCreate;
import com.example.agent.CustomerResult;
import com.example.agent.TaxiBookingCreate;
import com.example.agent.TaxiBookingDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.List;

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

    @GET
    @Path("/customers/{id}/bookings")
    List<TaxiBookingDto> listCustomerBookings(@PathParam("id") Long customerId);

    @DELETE
    @Path("/bookings/{id}")
    void cancelBooking(@PathParam("id") Long bookingId);
}