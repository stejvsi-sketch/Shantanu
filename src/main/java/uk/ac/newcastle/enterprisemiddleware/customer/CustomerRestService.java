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

// REST API for customers
@Path("/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerRestService {

    @Inject
    CustomerService service; // service layer

    private Logger log = Logger.getLogger(CustomerRestService.class.getName()); // logger


    // get all customers endpoint
    @GET
    @Operation(summary = "Fetch all Customers", description = "Returns a JSON array of all stored Customer objects")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Successful retrieval of customers",
            content = @Content(schema = @Schema(implementation = Customer.class))),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response retrieveAllCustomers() {
        //System.out.println("Getting all customers");
        List<Customer> customers = service.findAllOrderedByName();
        int customerCount = customers.size();
        //System.out.println("Found " + customerCount + " customers");
        Response response = Response.ok(customers).build();
        return response;
    }

    // get customer by id endpoint
    @GET
    @Path("/{id:[0-9]+}")
    @Operation(summary = "Fetch a Customer by id", description = "Returns a single Customer object based on the provided id")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Customer found",
            content = @Content(schema = @Schema(implementation = Customer.class))),
        @APIResponse(responseCode = "404", description = "Customer with id not found")
    })
    public Response retrieveCustomerById(@PathParam("id") long id) {
        //System.out.println("Looking for customer with id: " + id);
        Customer customer = service.findById(id);
        if(customer == null) {
            //System.out.println("Customer not found");
            String errorMessage = "Customer with id " + id + " not found";
            throw new WebApplicationException(errorMessage, Response.Status.NOT_FOUND);
        }
        //System.out.println("Customer found: " + customer.getName());
        Response response = Response.ok(customer).build();
        return response;
    }

    // create new customer endpoint
    @POST
    @Operation(summary = "Create a new Customer", description = "Creates a new customer from the provided JSON object")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Customer created successfully",
            content = @Content(schema = @Schema(implementation = Customer.class))),
        @APIResponse(responseCode = "400", description = "Invalid Customer supplied"),
        @APIResponse(responseCode = "409", description = "Customer with that email already exists")
    })
    public Response createCustomer(Customer customer) {
        //System.out.println("Creating customer");
        
        // check if customer object is null
        if(customer == null) {
            String msg = "Invalid customer data";
            throw new WebApplicationException(msg, Response.Status.BAD_REQUEST);
        }

        // make sure id is null for new customer
        customer.setId(null);
        
        Customer created = null;
        boolean success = false;
        
        try {
            created = service.create(customer);
            success = true;
        } catch(ConstraintViolationException e) {
            // validation error
            //System.out.println("Validation failed: " + e.getMessage());
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", "Validation failed");
            String details = e.getMessage();
            responseObj.put("details", details);
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(responseObj).build();
            return response;
        } catch(Exception e) {
            // other error like duplicate email
            //System.out.println("Error: " + e.getMessage());
            Map<String, String> responseObj = new HashMap<>();
            String errMsg = e.getMessage();
            responseObj.put("error", errMsg);
            Response response = Response.status(Response.Status.CONFLICT).entity(responseObj).build();
            return response;
        }
        
        if(success) {
            //System.out.println("Customer created successfully");
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
