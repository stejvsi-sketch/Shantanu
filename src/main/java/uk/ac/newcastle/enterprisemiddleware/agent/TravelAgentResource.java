package uk.ac.newcastle.enterprisemiddleware.agent;

import uk.ac.newcastle.enterprisemiddleware.agent.client.Hotel2Client;
import uk.ac.newcastle.enterprisemiddleware.agent.client.HotelClient;
import uk.ac.newcastle.enterprisemiddleware.agent.client.TaxiClient;
import uk.ac.newcastle.enterprisemiddleware.booking.BookingService;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerService;
import uk.ac.newcastle.enterprisemiddleware.hotel.HotelService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// agent orchestrator - books across 3 services
@Path("/api/agent/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TravelAgentResource {
    
    @Inject
    TravelAgentBookingRepository repository;
    
    @Inject
    AgentCustomerRepository agentCustomers;
    
    @Inject
    AgentCustomerMappingRepository customerMappings;
    
    @Inject
    BookingService bookingService; // our local booking service
    
    @Inject
    CustomerService customerService; // our local customer service
    
    @Inject
    HotelService hotelService; // our local hotel service
    
    @Inject
    @RestClient
    HotelClient hotelClient; // client to our own service
    
    @Inject
    @RestClient
    TaxiClient taxiClient; // client to taxi service
    
    @Inject
    @RestClient
    Hotel2Client hotel2Client; // client to second hotel service
    
    // get all aggregate bookings
    @GET
    @Operation(summary = "List all aggregate bookings")
    @APIResponse(responseCode = "200", description = "List of bookings")
    public List<TravelAgentBooking> list(@QueryParam("customerId") Long customerId) {
        //System.out.println("Getting aggregate bookings");
        if(customerId != null) {
            return repository.findByCustomerId(customerId);
        }
        return repository.findAll();
    }
    
    // create aggregate booking - books across 3 services
    @POST
    @Transactional
    @Operation(summary = "Create aggregate booking across Hotel, Taxi and Hotel2")
    @APIResponse(responseCode = "201", description = "Aggregate booking created")
    @APIResponse(responseCode = "400", description = "Validation error")
    @APIResponse(responseCode = "409", description = "Conflict from downstream service")
    public Response create(@Valid TravelAgentRequest req) {
        //System.out.println("Creating aggregate booking");
        Map<String, Object> error = new HashMap<>();
        
        // validate request
        String validation = validate(req);
        if(validation != null) {
            error.put("message", validation);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        
        // get customer mapping
        AgentCustomerMapping map = customerMappings.findByAgentCustomerId(req.customerId);
        if(map == null) {
            Map<String, Object> details = new HashMap<>();
            details.put("message", "Customer must be created via agent before booking");
            details.put("customerId", req.customerId);
            return Response.status(422).entity(details).build();
        }
        
        // check all mappings exist
        Long hotelCustomerId = map.getHotelCustomerId();
        Long taxiCustomerId = map.getTaxiCustomerId();
        Long hotel2CustomerId = map.getHotel2CustomerId();
        
        if(hotelCustomerId == null || taxiCustomerId == null || hotel2CustomerId == null) {
            Map<String, Object> details = new HashMap<>();
            details.put("message", "Downstream customers not provisioned for all services");
            List<String> missing = new ArrayList<>();
            if(hotelCustomerId == null) missing.add("hotel");
            if(taxiCustomerId == null) missing.add("taxi");
            if(hotel2CustomerId == null) missing.add("hotel2");
            details.put("missing", missing);
            return Response.status(422).entity(details).build();
        }
        
        Long hotelBookingId = null;
        Long taxiBookingId = null;
        Long hotel2BookingId = null;
        
        try {
            // call hotel service (our service)
            HotelBookingCreate hotelReq = new HotelBookingCreate();
            hotelReq.customerId = hotelCustomerId;
            hotelReq.hotelId = req.hotelId;
            hotelReq.date = req.date;
            BookingResult hotelResult = hotelClient.createBooking(hotelReq);
            hotelBookingId = hotelResult != null ? hotelResult.id : null;
            //System.out.println("Hotel booking created: " + hotelBookingId);
            
            // call taxi service
            TaxiBookingCreate taxiReq = new TaxiBookingCreate();
            taxiReq.customerId = taxiCustomerId;
            taxiReq.taxiId = req.taxiId;
            taxiReq.date = req.date;
            BookingResult taxiResult = taxiClient.createBooking(taxiReq);
            taxiBookingId = taxiResult != null ? taxiResult.id : null;
            //System.out.println("Taxi booking created: " + taxiBookingId);
            
            // call second hotel service
            HotelBookingCreate hotel2Req = new HotelBookingCreate();
            hotel2Req.customerId = hotel2CustomerId;
            hotel2Req.hotelId = req.hotel2Id;
            hotel2Req.date = req.date;
            BookingResult hotel2Result = hotel2Client.createBooking(hotel2Req);
            hotel2BookingId = hotel2Result != null ? hotel2Result.id : null;
            //System.out.println("Hotel2 booking created: " + hotel2BookingId);
            
            // save aggregate booking
            TravelAgentBooking agg = new TravelAgentBooking();
            agg.setCustomerId(req.customerId);
            agg.setDate(req.date);
            agg.setHotelBookingId(hotelBookingId);
            agg.setTaxiBookingId(taxiBookingId);
            agg.setHotel2BookingId(hotel2BookingId);
            repository.persist(agg);
            //System.out.println("Aggregate booking saved: " + agg.getId());
            
            return Response.status(Response.Status.CREATED).entity(agg).build();
            
        } catch(WebApplicationException wae) {
            // rollback - cancel any successful bookings
            //System.out.println("Error - rolling back bookings");
            if(hotel2BookingId != null) safeCancelHotel2(hotel2BookingId);
            if(taxiBookingId != null) safeCancelTaxi(taxiBookingId);
            if(hotelBookingId != null) safeCancelHotel(hotelBookingId);
            
            Map<String, Object> downstream = new HashMap<>();
            downstream.put("message", "Downstream error: " + wae.getMessage());
            downstream.put("failedService", inferFailedService(hotelBookingId, taxiBookingId, hotel2BookingId));
            return Response.status(wae.getResponse().getStatus()).entity(downstream).build();
        } catch(Exception e) {
            // rollback
            //System.out.println("Error - rolling back bookings: " + e.getMessage());
            if(hotel2BookingId != null) safeCancelHotel2(hotel2BookingId);
            if(taxiBookingId != null) safeCancelTaxi(taxiBookingId);
            if(hotelBookingId != null) safeCancelHotel(hotelBookingId);
            
            Map<String, Object> downstream = new HashMap<>();
            downstream.put("message", "Agent orchestration error: " + e.getMessage());
            downstream.put("failedService", inferFailedService(hotelBookingId, taxiBookingId, hotel2BookingId));
            return Response.status(Response.Status.BAD_GATEWAY).entity(downstream).build();
        }
    }
    
    // delete aggregate booking - cancels all downstream bookings
    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Cancel aggregate booking and downstream bookings")
    @APIResponse(responseCode = "204", description = "Cancelled")
    @APIResponse(responseCode = "404", description = "Not found")
    public Response cancel(@PathParam("id") Long id) {
        TravelAgentBooking agg = repository.findById(id);
        if(agg == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        // cancel downstream bookings
        if(agg.getHotelBookingId() != null) safeCancelHotel(agg.getHotelBookingId());
        if(agg.getTaxiBookingId() != null) safeCancelTaxi(agg.getTaxiBookingId());
        if(agg.getHotel2BookingId() != null) safeCancelHotel2(agg.getHotel2BookingId());
        
        repository.delete(agg);
        return Response.noContent().build();
    }
    
    // helper to validate request
    private String validate(TravelAgentRequest req) {
        if(req.customerId == null || req.customerId <= 0) return "customerId must be positive";
        if(req.date == null) return "date is required";
        if(req.hotelId == null || req.hotelId <= 0) return "hotelId must be positive";
        if(req.taxiId == null || req.taxiId <= 0) return "taxiId must be positive";
        if(req.hotel2Id == null || req.hotel2Id <= 0) return "hotel2Id must be positive";
        return null;
    }
    
    // safe cancel methods - dont throw exceptions
    private void safeCancelHotel(Long id) {
        try {
            hotelClient.cancelBooking(id);
        } catch(Exception ignored) {}
    }
    
    private void safeCancelTaxi(Long id) {
        try {
            taxiClient.cancelBooking(id);
        } catch(Exception ignored) {}
    }
    
    private void safeCancelHotel2(Long id) {
        try {
            hotel2Client.cancelBooking(id);
        } catch(Exception ignored) {}
    }
    
    // figure out which service failed
    private String inferFailedService(Long hotelBookingId, Long taxiBookingId, Long hotel2BookingId) {
        if(hotelBookingId == null) return "hotel";
        if(taxiBookingId == null) return "taxi";
        if(hotel2BookingId == null) return "hotel2";
        return "unknown";
    }
}
