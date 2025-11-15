package uk.ac.newcastle.enterprisemiddleware.agent;

import uk.ac.newcastle.enterprisemiddleware.agent.client.Hotel2Client;
import uk.ac.newcastle.enterprisemiddleware.agent.client.HotelClient;
import uk.ac.newcastle.enterprisemiddleware.agent.client.TaxiClient;
import uk.ac.newcastle.enterprisemiddleware.agent.Hotel2BookingCreate;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// agent orchestrator - books across 3 services
@Path("/api/agent/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TravelAgentResource {
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    
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
        Long cid=customerId;
        boolean hasCustomerId=false;
        if(cid!=null){
            hasCustomerId=true;
        }
        List<TravelAgentBooking> result;
        if(hasCustomerId){
            result=repository.findByCustomerId(cid);
        }else{
            result=repository.findAll();
        }
        return result;
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
        Map<String, Object> err=error;
        
        String validation = validate(req);
        String v=validation;
        boolean isValid=true;
        if(v!=null){
            isValid=false;
        }
        if(!isValid){
            err.put("message", v);
            Response.Status badReq=Response.Status.BAD_REQUEST;
            Response r=Response.status(badReq).entity(err).build();
            return r;
        }
        
        Long reqCustId=req.customerId;
        AgentCustomerMapping map = customerMappings.findByAgentCustomerId(reqCustId);
        AgentCustomerMapping mapping=map;
        boolean foundMap=true;
        if(mapping==null){
            foundMap=false;
        }
        if(!foundMap){
            Map<String, Object> details = new HashMap<>();
            String msg="Customer must be created via agent before booking";
            details.put("message", msg);
            Long cId=req.customerId;
            details.put("customerId", cId);
            int status=422;
            Response resp=Response.status(status).entity(details).build();
            return resp;
        }
        
        Long hotelCustomerId = map.getHotelCustomerId();
        Long hCustId=hotelCustomerId;
        Long taxiCustomerId = map.getTaxiCustomerId();
        Long tCustId=taxiCustomerId;
        Long hotel2CustomerId = map.getHotel2CustomerId();
        Long h2CustId=hotel2CustomerId;
        
        boolean allExist=true;
        if(hCustId==null){
            allExist=false;
        }
        if(tCustId==null){
            allExist=false;
        }
        if(h2CustId==null){
            allExist=false;
        }
        if(!allExist){
            Map<String, Object> details = new HashMap<>();
            String message="Downstream customers not provisioned for all services";
            details.put("message", message);
            List<String> missing = new ArrayList<>();
            if(hCustId==null){
                String h="hotel";
                missing.add(h);
            }
            if(tCustId==null){
                String t="taxi";
                missing.add(t);
            }
            if(h2CustId==null){
                String h2="hotel2";
                missing.add(h2);
            }
            details.put("missing", missing);
            int stat=422;
            Response response=Response.status(stat).entity(details).build();
            return response;
        }
        
        Long hotelBookingId = null;
        Long taxiBookingId = null;
        Long hotel2BookingId = null;
        String globalBookingId = UUID.randomUUID().toString();
        String bookingDate = formatDate(req.date);
        
        try {
            HotelBookingCreate hotelReq = new HotelBookingCreate();
            Long hCid=hotelCustomerId;
            hotelReq.customerId = hCid;
            Long hId=req.hotelId;
            hotelReq.hotelId = hId;
            java.util.Date d=req.date;
            hotelReq.date = d;
            BookingResult hotelResult = hotelClient.createBooking(hotelReq);
            BookingResult hRes=hotelResult;
            boolean hResNull=false;
            if(hRes==null){
                hResNull=true;
            }
            if(hResNull){
                hotelBookingId=null;
            }else{
                Long hBid=hRes.id;
                hotelBookingId=hBid;
            }
            //System.out.println("Hotel booking created: " + hotelBookingId);
            
            TaxiBookingCreate taxiReq = new TaxiBookingCreate();
            taxiReq.globalId = globalBookingId;
            Long tId=req.taxiId;
            taxiReq.taxiId = tId;
            taxiReq.bookingDate = bookingDate;
            BookingResult taxiResult = taxiClient.createBooking(taxiReq);
            BookingResult tRes=taxiResult;
            if(tRes==null){
                taxiBookingId=null;
            }else{
                taxiBookingId=tRes.id;
            }
            //System.out.println("Taxi booking created: " + taxiBookingId);
            
            Hotel2BookingCreate hotel2Req = new Hotel2BookingCreate();
            hotel2Req.customerId = h2CustId;
            hotel2Req.globalBookingId = globalBookingId;
            Long h2Id=req.hotel2Id;
            hotel2Req.hotelId = h2Id;
            hotel2Req.bookingDate = bookingDate;
            BookingResult hotel2Result = hotel2Client.createBooking(hotel2Req);
            BookingResult h2Res=hotel2Result;
            if(h2Res!=null){
                hotel2BookingId=h2Res.id;
            }else{
                hotel2BookingId=null;
            }
            //System.out.println("Hotel2 booking created: " + hotel2BookingId);
            
            TravelAgentBooking agg = new TravelAgentBooking();
            Long custId=req.customerId;
            agg.setCustomerId(custId);
            java.util.Date aggDate=req.date;
            agg.setDate(aggDate);
            Long hBid=hotelBookingId;
            agg.setHotelBookingId(hBid);
            Long tBid=taxiBookingId;
            agg.setTaxiBookingId(tBid);
            Long h2Bid=hotel2BookingId;
            agg.setHotel2BookingId(h2Bid);
            TravelAgentBooking aggBooking=agg;
            repository.persist(aggBooking);
            //System.out.println("Aggregate booking saved: " + agg.getId());
            
            Response.Status created=Response.Status.CREATED;
            TravelAgentBooking entity=agg;
            Response response=Response.status(created).entity(entity).build();
            return response;
            
        } catch(WebApplicationException wae) {
            //System.out.println("Error - rolling back bookings");
            Long h2Bid=hotel2BookingId;
            boolean h2Null=false;
            if(h2Bid==null){ h2Null=true; }
            if(!h2Null){ safeCancelHotel2(h2Bid); }
            
            Long tBid=taxiBookingId;
            if(tBid!=null){ safeCancelTaxi(tBid); }
            
            Long hBid=hotelBookingId;
            if(hBid!=null){ safeCancelHotel(hBid); }
            
            Map<String, Object> downstream = new HashMap<>();
            String msg="Downstream error: " + wae.getMessage();
            downstream.put("message", msg);
            String failed=inferFailedService(hotelBookingId, taxiBookingId, hotel2BookingId);
            downstream.put("failedService", failed);
            javax.ws.rs.core.Response waResp=wae.getResponse();
            int stat=waResp.getStatus();
            Response resp=Response.status(stat).entity(downstream).build();
            return resp;
        } catch(Exception e) {
            //System.out.println("Error - rolling back bookings: " + e.getMessage());
            if(hotel2BookingId != null){
                Long h2=hotel2BookingId;
                safeCancelHotel2(h2);
            }
            if(taxiBookingId != null){
                Long t=taxiBookingId;
                safeCancelTaxi(t);
            }
            if(hotelBookingId != null){
                Long h=hotelBookingId;
                safeCancelHotel(h);
            }
            
            Map<String, Object> downstream = new HashMap<>();
            String errMsg="Agent orchestration error: " + e.getMessage();
            downstream.put("message", errMsg);
            String failedSvc=inferFailedService(hotelBookingId, taxiBookingId, hotel2BookingId);
            downstream.put("failedService", failedSvc);
            Response.Status badGateway=Response.Status.BAD_GATEWAY;
            Response r=Response.status(badGateway).entity(downstream).build();
            return r;
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
        Long aggId=id;
        TravelAgentBooking agg = repository.findById(aggId);
        TravelAgentBooking booking=agg;
        boolean found=true;
        if(booking==null){
            found=false;
        }
        if(!found){
            Response.Status notFound=Response.Status.NOT_FOUND;
            Response r=Response.status(notFound).build();
            return r;
        }
        
        Long hBid=agg.getHotelBookingId();
        if(hBid!=null){
            safeCancelHotel(hBid);
        }
        Long tBid=agg.getTaxiBookingId();
        if(tBid!=null){
            safeCancelTaxi(tBid);
        }
        Long h2Bid=agg.getHotel2BookingId();
        if(h2Bid!=null){
            safeCancelHotel2(h2Bid);
        }
        
        TravelAgentBooking toDelete=agg;
        repository.delete(toDelete);
        Response r=Response.noContent().build();
        return r;
    }
    
    private String validate(TravelAgentRequest req) {
        Long cid=req.customerId;
        boolean cidBad=false;
        if(cid==null){ cidBad=true; }
        if(cid!=null && cid<=0){ cidBad=true; }
        if(cidBad){ return "customerId must be positive"; }
        
        java.util.Date dt=req.date;
        if(dt==null){ return "date is required"; }
        
        Long hid=req.hotelId;
        if(hid==null || hid<=0){ return "hotelId must be positive"; }
        
        Long tid=req.taxiId;
        if(tid==null || tid<=0){ return "taxiId must be positive"; }
        
        Long h2id=req.hotel2Id;
        if(h2id==null || h2id<=0){ return "hotel2Id must be positive"; }
        
        return null;
    }
    
    // safe cancel methods - dont throw exceptions
    private void safeCancelHotel(Long id) {
        try {
            Long hid=id;
            hotelClient.cancelBooking(hid);
        } catch(Exception ignored) {}
    }
    
    private void safeCancelTaxi(Long id) {
        try {
            Long tid=id;
            taxiClient.cancelBooking(tid);
        } catch(Exception ignored) {}
    }
    
    private void safeCancelHotel2(Long id) {
        try {
            Long h2id=id;
            hotel2Client.cancelBooking(h2id);
        } catch(Exception ignored) {}
    }
    
    private String inferFailedService(Long hotelBookingId, Long taxiBookingId, Long hotel2BookingId) {
        Long hid=hotelBookingId;
        boolean hNull=false;
        if(hid==null){ hNull=true; }
        if(hNull){ return "hotel"; }
        
        Long tid=taxiBookingId;
        if(tid==null){ return "taxi"; }
        
        Long h2id=hotel2BookingId;
        if(h2id==null){ return "hotel2"; }
        
        String unknown="unknown";
        return unknown;
    }
    
    private String formatDate(java.util.Date date) {
        if(date == null){
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(ISO_DATE);
    }
}
