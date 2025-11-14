package com.example.flight;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/api/flights")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FlightResource {

    @Inject
    FlightRepository repo;

    @POST
    public Response create(@Valid FlightCreate req) {
        if (req.departure != null && req.destination != null && req.departure.equals(req.destination)) {
            return Response.status(422).entity("destination must be different from departure").build();
        }
        if (repo.findByFlightNumber(req.flightNumber) != null) {
            return Response.status(409).entity("Flight with this flight number already exists").build();
        }
        Flight f = new Flight();
        f.setFlightNumber(req.flightNumber);
        f.setDeparture(req.departure);
        f.setDestination(req.destination);
        repo.create(f);
        return Response.created(URI.create("/api/flights/" + f.getId())).entity(f).build();
    }

    @GET
    public List<Flight> list() {
        return repo.listAll();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = repo.deleteById(id);
        if (!deleted) return Response.status(404).build();
        return Response.noContent().build();
    }
}