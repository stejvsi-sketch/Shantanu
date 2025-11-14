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
    
    @GET
    @Operation(summary = "List all agent customers")
    public List<AgentCustomer> list() {
        List<AgentCustomer> allCustomers=agentCustomers.findAll();
        List<AgentCustomer> result=allCustomers;
        return result;
    }
    
    // create agent customer and propagate to downstream services
    @POST
    @Transactional
    @Operation(summary = "Create agent customer and propagate to downstream services")
    public Response create(AgentCustomerCreate req) {
        String name=req.name;
        boolean nameOk=true;
        if(name==null){ nameOk=false; }
        if(name!=null && name.isBlank()){ nameOk=false; }
        if(!nameOk){
            Map<String,Object> err=Map.of("error", "name is required");
            Response.Status bad=Response.Status.BAD_REQUEST;
            return Response.status(bad).entity(err).build();
        }
        String email=req.email;
        if(email==null || email.isBlank()){
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "email is required")).build();
        }
        String phone=req.phonenumber;
        boolean phoneOk=false;
        if(phone!=null && !phone.isBlank()){ phoneOk=true; }
        if(!phoneOk){
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "phonenumber is required")).build();
        }
        
        String reqEmail=req.email;
        AgentCustomer existing = agentCustomers.findByEmail(reqEmail);
        AgentCustomer found=existing;
        boolean alreadyExists=false;
        if(found!=null){ alreadyExists=true; }
        if(alreadyExists){
            Map<String,Object> errMap=Map.of("error", "Customer with this email already exists");
            Response.Status conflict=Response.Status.CONFLICT;
            Response r=Response.status(conflict).entity(errMap).build();
            return r;
        }
        
        AgentCustomer agentCustomer = new AgentCustomer();
        String n=req.name;
        agentCustomer.setName(n);
        String e=req.email;
        agentCustomer.setEmail(e);
        String p=req.phonenumber;
        agentCustomer.setPhonenumber(p);
        AgentCustomer cust=agentCustomer;
        agentCustomers.persist(cust);
        
        AgentCustomerMapping mapping = new AgentCustomerMapping();
        Long custId=agentCustomer.getId();
        mapping.setAgentCustomerId(custId);
        
        try {
            String n1=req.name;
            String e1=req.email;
            String p1=req.phonenumber;
            DownstreamCustomerCreate hotelReq = new DownstreamCustomerCreate(n1, e1, p1);
            CustomerResult hotelResult = hotelClient.createCustomer(hotelReq);
            Long hid=hotelResult.id;
            mapping.setHotelCustomerId(hid);
        } catch(WebApplicationException wae) {
            int status=wae.getResponse().getStatus();
            if(status == 409) {
                String email1=req.email;
                Long id = findExistingCustomer(hotelClient, email1);
                mapping.setHotelCustomerId(id);
            }
        } catch(Exception ex) {}
        
        try {
            String n2=req.name;
            String e2=req.email;
            String p2=req.phonenumber;
            DownstreamCustomerCreate taxiReq = new DownstreamCustomerCreate(n2, e2, p2);
            CustomerResult taxiResult = taxiClient.createCustomer(taxiReq);
            Long tid=taxiResult.id;
            mapping.setTaxiCustomerId(tid);
        } catch(WebApplicationException wae2) {
            javax.ws.rs.core.Response resp=wae2.getResponse();
            int stat=resp.getStatus();
            if(stat == 409) {
                Long id = findExistingCustomer(taxiClient, req.email);
                mapping.setTaxiCustomerId(id);
            }
        } catch(Exception ex2) {}
        
        try {
            String n3=req.name;
            String e3=req.email;
            String p3=req.phonenumber;
            DownstreamCustomerCreate hotel2Req = new DownstreamCustomerCreate(n3, e3, p3);
            CustomerResult hotel2Result = hotel2Client.createCustomer(hotel2Req);
            Long h2id=hotel2Result.id;
            mapping.setHotel2CustomerId(h2id);
        } catch(WebApplicationException wae3) {
            javax.ws.rs.core.Response r=wae3.getResponse();
            if(r.getStatus() == 409) {
                Long id = findExistingCustomer(hotel2Client, req.email);
                mapping.setHotel2CustomerId(id);
            }
        } catch(Exception ex3) {}
        
        AgentCustomerMapping map=mapping;
        customerMappings.persist(map);
        
        Map<String, Object> result = new HashMap<>();
        Long id1=agentCustomer.getId();
        result.put("id", id1);
        String name1=agentCustomer.getName();
        result.put("name", name1);
        String email1=agentCustomer.getEmail();
        result.put("email", email1);
        String phone1=agentCustomer.getPhonenumber();
        result.put("phonenumber", phone1);
        Long hCid=mapping.getHotelCustomerId();
        Long tCid=mapping.getTaxiCustomerId();
        Long h2Cid=mapping.getHotel2CustomerId();
        result.put("mapping", Map.of(
            "hotelCustomerId", hCid,
            "taxiCustomerId", tCid,
            "hotel2CustomerId", h2Cid
        ));
        
        Response.Status created=Response.Status.CREATED;
        Response resp=Response.status(created).entity(result).build();
        return resp;
    }
    
    private Long findExistingCustomer(Object client, String email) {
        try {
            List<CustomerResult> list;
            boolean isHotel=client instanceof HotelClient;
            boolean isTaxi=client instanceof TaxiClient;
            boolean isHotel2=client instanceof Hotel2Client;
            if(isHotel) {
                HotelClient hc = (HotelClient) client;
                list = hc.listCustomers();
            } else if(isTaxi) {
                TaxiClient tc = (TaxiClient) client;
                list = tc.listCustomers();
            } else if(isHotel2) {
                Hotel2Client h2c = (Hotel2Client) client;
                list = h2c.listCustomers();
            } else {
                return null;
            }
            String emailToFind=email;
            for(int i=0; i<list.size(); i++){
                CustomerResult c=list.get(i);
                String cEmail=c.email;
                boolean match=emailToFind.equalsIgnoreCase(cEmail);
                if(match){
                    Long cid=c.id;
                    return cid;
                }
            }
            return null;
        } catch(Exception e) {
            return null;
        }
    }
}
