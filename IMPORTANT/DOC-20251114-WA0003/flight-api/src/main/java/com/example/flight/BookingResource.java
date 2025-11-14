package com.example.flight;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingResource {

    @Inject
    BookingRepository bookingRepo;

    @Inject
    CustomerRepository customerRepo;

    @Inject
    FlightRepository flightRepo;

    @POST
    @Operation(hidden = true)
    public Response create(@Valid BookingCreate req) {
        var customer = customerRepo.findById(req.customerId);
        if (customer == null) {
            return Response.status(400).entity("Invalid customerId").build();
        }

        var flight = flightRepo.findById(req.flightId);
        if (flight == null) {
            return Response.status(400).entity("Invalid flightId").build();
        }

        if (bookingRepo.findByFlightAndDate(flight, req.date) != null) {
            return Response.status(409).entity("Booking already exists for flight and date").build();
        }

        Booking b = new Booking();
        b.setCustomer(customer);
        b.setFlight(flight);
        b.setDate(req.date);
        bookingRepo.create(b);

        return Response.created(URI.create("/api/bookings/" + b.getId())).entity(b).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = bookingRepo.deleteById(id);
        if (!deleted) return Response.status(404).build();
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/delete")
    public Response deletePost(@PathParam("id") Long id) {
        boolean deleted = bookingRepo.deleteById(id);
        if (!deleted) return Response.status(404).build();
        return Response.noContent().build();
    }

    @GET
    public Response list(@QueryParam("customerId") Long customerId) {
        if (customerId != null) {
            var customer = customerRepo.findById(customerId);
            if (customer == null) {
                return Response.status(400).entity("Invalid customerId").build();
            }
            return Response.ok(bookingRepo.listByCustomerId(customerId)).build();
        }
        return Response.ok(bookingRepo.listAll()).build();
    }
}