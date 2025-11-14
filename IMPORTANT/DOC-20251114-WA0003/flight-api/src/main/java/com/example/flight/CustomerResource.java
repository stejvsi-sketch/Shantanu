package com.example.flight;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    CustomerRepository repo;

    @POST
    public Response create(@Valid CustomerCreate req) {
        if (repo.findByEmail(req.email) != null) {
            return Response.status(409).entity("Customer with this email already exists").build();
        }
        Customer c = new Customer();
        c.setName(req.name);
        c.setEmail(req.email);
        c.setPhonenumber(req.phonenumber);
        repo.create(c);
        return Response.created(URI.create("/api/customers/" + c.getId())).entity(c).build();
    }

    @GET
    public List<Customer> list() {
        return repo.listAll();
    }
}