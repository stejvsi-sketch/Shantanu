package uk.ac.newcastle.enterprisemiddleware.agent;

import uk.ac.newcastle.enterprisemiddleware.agent.client.Hotel2Client;
import uk.ac.newcastle.enterprisemiddleware.agent.client.HotelClient;
import uk.ac.newcastle.enterprisemiddleware.agent.client.TaxiClient;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// agent customer endpoint - manages customers and creates them in downstream services
@Path("/api/agent/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentCustomerResource {
    
    @Inject
    AgentCustomerRepository agentCustomers;
    
    @Inject
    AgentCustomerMappingRepository customerMappings;
    
    @Inject
    @RestClient
    HotelClient hotelClient;
    
    @Inject
    @RestClient
    TaxiClient taxiClient;
    
    @Inject
    @RestClient
    Hotel2Client hotel2Client;
    
    // get all agent customers
    @GET
    @Operation(summary = "List all agent customers")
    public List<AgentCustomer> list() {
        return agentCustomers.findAll();
    }
    
    // create agent customer and propagate to downstream services
    @POST
    @Transactional
    @Operation(summary = "Create agent customer and propagate to downstream services")
    public Response create(AgentCustomerCreate req) {
        // validate
        if(req.name == null || req.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "name is required")).build();
        }
        if(req.email == null || req.email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "email is required")).build();
        }
        if(req.phonenumber == null || req.phonenumber.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "phonenumber is required")).build();
        }
        
        // check if customer already exists
        AgentCustomer existing = agentCustomers.findByEmail(req.email);
        if(existing != null) {
            return Response.status(Response.Status.CONFLICT)
                .entity(Map.of("error", "Customer with this email already exists")).build();
        }
        
        // create agent customer
        AgentCustomer agentCustomer = new AgentCustomer();
        agentCustomer.setName(req.name);
        agentCustomer.setEmail(req.email);
        agentCustomer.setPhonenumber(req.phonenumber);
        agentCustomers.persist(agentCustomer);
        
        // create customer mapping
        AgentCustomerMapping mapping = new AgentCustomerMapping();
        mapping.setAgentCustomerId(agentCustomer.getId());
        
        // propagate to hotel service
        try {
            DownstreamCustomerCreate hotelReq = new DownstreamCustomerCreate(req.name, req.email, req.phonenumber);
            CustomerResult hotelResult = hotelClient.createCustomer(hotelReq);
            mapping.setHotelCustomerId(hotelResult.id);
        } catch(WebApplicationException e) {
            if(e.getResponse().getStatus() == 409) {
                // customer exists - find it
                Long id = findExistingCustomer(hotelClient, req.email);
                mapping.setHotelCustomerId(id);
            }
        } catch(Exception e) {
            // ignore - will be null
        }
        
        // propagate to taxi service
        try {
            DownstreamCustomerCreate taxiReq = new DownstreamCustomerCreate(req.name, req.email, req.phonenumber);
            CustomerResult taxiResult = taxiClient.createCustomer(taxiReq);
            mapping.setTaxiCustomerId(taxiResult.id);
        } catch(WebApplicationException e) {
            if(e.getResponse().getStatus() == 409) {
                Long id = findExistingCustomer(taxiClient, req.email);
                mapping.setTaxiCustomerId(id);
            }
        } catch(Exception e) {
            // ignore
        }
        
        // propagate to hotel2 service
        try {
            DownstreamCustomerCreate hotel2Req = new DownstreamCustomerCreate(req.name, req.email, req.phonenumber);
            CustomerResult hotel2Result = hotel2Client.createCustomer(hotel2Req);
            mapping.setHotel2CustomerId(hotel2Result.id);
        } catch(WebApplicationException e) {
            if(e.getResponse().getStatus() == 409) {
                Long id = findExistingCustomer(hotel2Client, req.email);
                mapping.setHotel2CustomerId(id);
            }
        } catch(Exception e) {
            // ignore
        }
        
        customerMappings.persist(mapping);
        
        // return response
        Map<String, Object> result = new HashMap<>();
        result.put("id", agentCustomer.getId());
        result.put("name", agentCustomer.getName());
        result.put("email", agentCustomer.getEmail());
        result.put("phonenumber", agentCustomer.getPhonenumber());
        result.put("mapping", Map.of(
            "hotelCustomerId", mapping.getHotelCustomerId(),
            "taxiCustomerId", mapping.getTaxiCustomerId(),
            "hotel2CustomerId", mapping.getHotel2CustomerId()
        ));
        
        return Response.status(Response.Status.CREATED).entity(result).build();
    }
    
    // helper to find existing customer by email
    private Long findExistingCustomer(Object client, String email) {
        try {
            List<CustomerResult> list;
            if(client instanceof HotelClient) {
                HotelClient hc = (HotelClient) client;
                list = hc.listCustomers();
            } else if(client instanceof TaxiClient) {
                TaxiClient tc = (TaxiClient) client;
                list = tc.listCustomers();
            } else if(client instanceof Hotel2Client) {
                Hotel2Client h2c = (Hotel2Client) client;
                list = h2c.listCustomers();
            } else {
                return null;
            }
            return list.stream()
                .filter(c -> email.equalsIgnoreCase(c.email))
                .map(c -> c.id)
                .findFirst()
                .orElse(null);
        } catch(Exception e) {
            return null;
        }
    }
}
