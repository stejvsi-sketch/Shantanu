package uk.ac.newcastle.enterprisemiddleware.customer;

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

/**
 * REST endpoints for customer CRUD operations
 */
@Path("/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerRestService {

    @Inject
    CustomerService service;

    private Logger log = Logger.getLogger(CustomerRestService.class.getName());


    // GET all customers
    @GET
    @Operation(summary = "Fetch all Customers", description = "Returns a JSON array of all stored Customer objects")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Successful retrieval of customers",
            content = @Content(schema = @Schema(implementation = Customer.class))),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response retrieveAllCustomers() {
        List<Customer> customers = service.findAllOrderedByName();
        //System.out.println("Found " + customers.size() + " customers"); // debug
        return Response.ok(customers).build();
    }

    // GET customer by ID
    @GET
    @Path("/{id:[0-9]+}")
    @Operation(summary = "Fetch a Customer by id", description = "Returns a single Customer object based on the provided id")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Customer found",
            content = @Content(schema = @Schema(implementation = Customer.class))),
        @APIResponse(responseCode = "404", description = "Customer with id not found")
    })
    public Response retrieveCustomerById(@PathParam("id") long id) {
        Customer customer = service.findById(id);
        if(customer == null) {
            throw new WebApplicationException("Customer with id " + id + " not found", Response.Status.NOT_FOUND);
        }
        return Response.ok(customer).build();
    }

    // POST create new customer
    @POST
    @Operation(summary = "Create a new Customer", description = "Creates a new customer from the provided JSON object")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Customer created successfully",
            content = @Content(schema = @Schema(implementation = Customer.class))),
        @APIResponse(responseCode = "400", description = "Invalid Customer supplied"),
        @APIResponse(responseCode = "409", description = "Customer with that email already exists")
    })
    public Response createCustomer(Customer customer) {
        if(customer == null) {
            throw new WebApplicationException("Invalid customer data", Response.Status.BAD_REQUEST);
        }

        customer.setId(null);
        Customer created = null;
        
        try {
            created = service.create(customer);
        } catch(ConstraintViolationException e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", "Validation failed");
            responseObj.put("details", e.getMessage());
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(responseObj).build();
            return response;
        } catch(Exception e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", e.getMessage());
            Response response = Response.status(Response.Status.CONFLICT).entity(responseObj).build();
            return response;
        }
        
        Response response = Response.status(Response.Status.CREATED).entity(created).build();
        return response;
    }

    // DELETE customer (cascades to bookings)
    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(summary = "Delete a Customer", description = "Deletes a customer and all associated bookings")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "Customer deleted successfully"),
        @APIResponse(responseCode = "404", description = "Customer with id not found")
    })
    public Response deleteCustomer(@PathParam("id") long id) {
        Customer customer = service.findById(id);
        if(customer == null) {
            throw new WebApplicationException("Customer with id " + id + " not found", Response.Status.NOT_FOUND);
        }

        service.delete(customer);
        return Response.noContent().build();
    }
}
