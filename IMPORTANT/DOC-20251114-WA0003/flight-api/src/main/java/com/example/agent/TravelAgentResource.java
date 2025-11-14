package com.example.agent;

import com.example.agent.client.HotelClient;
import com.example.agent.client.TaxiClient;
import com.example.flight.FlightOperations;
import com.example.flight.Booking;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    FlightOperations flightOps;

    @Inject
    @RestClient
    TaxiClient taxiClient;

    @Inject
    @RestClient
    HotelClient hotelClient;

    @GET
    @Operation(summary = "List aggregate bookings")
    @APIResponse(responseCode = "200", description = "List of bookings")
    public List<TravelAgentBooking> list(@QueryParam("customerId") Long customerId) {
        if (customerId != null) {
            return repository.findByCustomerId(customerId);
        }
        return repository.findAll();
    }

    @POST
    @Transactional
    @Operation(summary = "Create an aggregate booking across Flight (local), Taxi & Hotel (remote)")
    @APIResponse(responseCode = "201", description = "Aggregate booking created")
    @APIResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = Map.class)))
    @APIResponse(responseCode = "409", description = "Conflict from downstream service")
    @APIResponse(responseCode = "422", description = "Unprocessable entity from downstream service")
    public Response create(@Valid TravelAgentRequest req) {
        Map<String, Object> error = new HashMap<>();
        String validation = validate(req);
        if (validation != null) {
            error.put("message", validation);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        // Map agent customer to downstream IDs
        Long taxiCustomerId = null;
        Long hotelCustomerId = null;
        Long flightCustomerId = null;
        AgentCustomerMapping map = customerMappings.findByAgentCustomerId(req.customerId);
        if (map != null) {
            taxiCustomerId = map.getTaxiCustomerId();
            hotelCustomerId = map.getHotelCustomerId();
            flightCustomerId = map.getFlightCustomerId();
        } else {
            Map<String, Object> details = new HashMap<>();
            details.put("message", "Customer must be created via agent before booking. Create at /api/agent/customers then retry.");
            details.put("customerId", req.customerId);
            return Response.status(422).entity(details).build();
        }

        if (taxiCustomerId == null || hotelCustomerId == null || flightCustomerId == null) {
            Map<String, Object> details = new HashMap<>();
            details.put("message", "Downstream customers not provisioned for all services");
            java.util.List<String> missing = new java.util.ArrayList<>();
            if (taxiCustomerId == null) missing.add("taxi");
            if (hotelCustomerId == null) missing.add("hotel");
            if (flightCustomerId == null) missing.add("flight");
            details.put("missing", missing);
            return Response.status(422).entity(details).build();
        }

        Long taxiBookingId = null;
        Long hotelBookingId = null;
        Long flightBookingId = null;

        try {
            // Taxi booking (remote)
            TaxiBookingCreate taxiReq = new TaxiBookingCreate();
            taxiReq.customerId = taxiCustomerId;
            taxiReq.taxiId = req.taxiId;
            taxiReq.date = req.date;
            BookingResult taxiResult = taxiClient.createBooking(taxiReq);
            taxiBookingId = taxiResult != null ? taxiResult.id : null;

            // Hotel booking (remote)
            HotelBookingCreate hotelReq = new HotelBookingCreate();
            hotelReq.customerId = hotelCustomerId;
            hotelReq.hotelId = req.hotelId;
            hotelReq.date = req.date;
            BookingResult hotelResult = hotelClient.createBooking(hotelReq);
            hotelBookingId = hotelResult != null ? hotelResult.id : null;

            // Flight booking (local)
            Booking flightBooking = flightOps.createFlightBooking(flightCustomerId, req.flightId, req.date);
            flightBookingId = flightBooking != null ? flightBooking.getId() : null;

            TravelAgentBooking agg = new TravelAgentBooking();
            agg.setCustomerId(req.customerId);
            agg.setDate(req.date);
            agg.setTaxiBookingId(taxiBookingId);
            agg.setHotelBookingId(hotelBookingId);
            agg.setFlightBookingId(flightBookingId);
            repository.persist(agg);

            return Response.status(Response.Status.CREATED).entity(agg).build();

        } catch (WebApplicationException wae) {
            // Compensate
            if (flightBookingId != null) safeCancelFlight(flightBookingId);
            if (hotelBookingId != null) safeCancelHotel(hotelBookingId);
            if (taxiBookingId != null) safeCancelTaxi(taxiBookingId);
            Map<String, Object> downstream = new HashMap<>();
            downstream.put("message", "Downstream error: " + wae.getMessage());
            downstream.put("failedService", inferFailedService(taxiBookingId, hotelBookingId, flightBookingId));
            return Response.status(wae.getResponse().getStatus()).entity(downstream).build();
        } catch (Exception e) {
            if (flightBookingId != null) safeCancelFlight(flightBookingId);
            if (hotelBookingId != null) safeCancelHotel(hotelBookingId);
            if (taxiBookingId != null) safeCancelTaxi(taxiBookingId);
            Map<String, Object> downstream = new HashMap<>();
            downstream.put("message", "Agent orchestration error: " + e.getMessage());
            downstream.put("failedService", inferFailedService(taxiBookingId, hotelBookingId, flightBookingId));
            return Response.status(Response.Status.BAD_GATEWAY).entity(downstream).build();
        }
    }

    @POST
    @Path("/guest")
    @Transactional
    @Operation(summary = "Guest aggregate booking: create customer + book across services atomically")
    @APIResponse(responseCode = "201", description = "Guest aggregate booking created")
    @APIResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = Map.class)))
    @APIResponse(responseCode = "409", description = "Conflict from downstream service")
    @APIResponse(responseCode = "422", description = "Unprocessable entity from downstream service")
    public Response createGuest(@Valid GuestBookingRequest req) {
        Map<String, Object> error = new HashMap<>();
        String validation = validateGuest(req);
        if (validation != null) {
            error.put("message", validation);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        // Find or create agent customer
        AgentCustomer agentCustomer = agentCustomers.findByEmail(req.email);
        if (agentCustomer == null) {
            agentCustomer = new AgentCustomer();
            agentCustomer.setName(req.name);
            agentCustomer.setEmail(req.email);
            agentCustomer.setPhonenumber(req.phonenumber);
            agentCustomers.persist(agentCustomer);
        }

        // Ensure downstream mappings exist
        AgentCustomerMapping map = customerMappings.findByAgentCustomerId(agentCustomer.getId());
        if (map == null) {
            map = new AgentCustomerMapping();
            map.setAgentCustomerId(agentCustomer.getId());
        }

        if (map.getTaxiCustomerId() == null) {
            Long id = propagateGuestCustomerToClient(taxiClient, req.name, req.email, req.phonenumber);
            map.setTaxiCustomerId(id);
        }
        if (map.getHotelCustomerId() == null) {
            Long id = propagateGuestCustomerToClient(hotelClient, req.name, req.email, req.phonenumber);
            map.setHotelCustomerId(id);
        }
        if (map.getFlightCustomerId() == null) {
            var fc = flightOps.ensureCustomerByEmail(req.name, req.email, req.phonenumber);
            map.setFlightCustomerId(fc != null ? fc.getId() : null);
        }

        if (map.getId() == null) customerMappings.persist(map);

        if (map.getTaxiCustomerId() == null || map.getHotelCustomerId() == null || map.getFlightCustomerId() == null) {
            Map<String, Object> details = new HashMap<>();
            details.put("message", "Downstream customers not provisioned for all services");
            java.util.List<String> missing = new java.util.ArrayList<>();
            if (map.getTaxiCustomerId() == null) missing.add("taxi");
            if (map.getHotelCustomerId() == null) missing.add("hotel");
            if (map.getFlightCustomerId() == null) missing.add("flight");
            details.put("missing", missing);
            return Response.status(422).entity(details).build();
        }

        Long taxiBookingId = null;
        Long hotelBookingId = null;
        Long flightBookingId = null;

        try {
            // Taxi booking (remote)
            TaxiBookingCreate taxiReq2 = new TaxiBookingCreate();
            taxiReq2.customerId = map.getTaxiCustomerId();
            taxiReq2.taxiId = req.taxiId;
            taxiReq2.date = req.date;
            BookingResult taxiResult = taxiClient.createBooking(taxiReq2);
            taxiBookingId = taxiResult != null ? taxiResult.id : null;

            // Hotel booking (remote)
            HotelBookingCreate hotelReq2 = new HotelBookingCreate();
            hotelReq2.customerId = map.getHotelCustomerId();
            hotelReq2.hotelId = req.hotelId;
            hotelReq2.date = req.date;
            BookingResult hotelResult = hotelClient.createBooking(hotelReq2);
            hotelBookingId = hotelResult != null ? hotelResult.id : null;

            // Flight booking (local)
            Booking flightBooking = flightOps.createFlightBooking(map.getFlightCustomerId(), req.flightId, req.date);
            flightBookingId = flightBooking != null ? flightBooking.getId() : null;

            TravelAgentBooking agg = new TravelAgentBooking();
            agg.setCustomerId(agentCustomer.getId());
            agg.setDate(req.date);
            agg.setTaxiBookingId(taxiBookingId);
            agg.setHotelBookingId(hotelBookingId);
            agg.setFlightBookingId(flightBookingId);
            repository.persist(agg);

            return Response.status(Response.Status.CREATED).entity(agg).build();

        } catch (WebApplicationException wae) {
            if (flightBookingId != null) safeCancelFlight(flightBookingId);
            if (hotelBookingId != null) safeCancelHotel(hotelBookingId);
            if (taxiBookingId != null) safeCancelTaxi(taxiBookingId);
            Map<String, Object> downstream = new HashMap<>();
            downstream.put("message", "Downstream error: " + wae.getMessage());
            downstream.put("failedService", inferFailedService(taxiBookingId, hotelBookingId, flightBookingId));
            return Response.status(wae.getResponse().getStatus()).entity(downstream).build();
        } catch (Exception e) {
            if (flightBookingId != null) safeCancelFlight(flightBookingId);
            if (hotelBookingId != null) safeCancelHotel(hotelBookingId);
            if (taxiBookingId != null) safeCancelTaxi(taxiBookingId);
            Map<String, Object> downstream = new HashMap<>();
            downstream.put("message", "Agent orchestration error: " + e.getMessage());
            downstream.put("failedService", inferFailedService(taxiBookingId, hotelBookingId, flightBookingId));
            return Response.status(Response.Status.BAD_GATEWAY).entity(downstream).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Cancel an aggregate booking and downstream bookings")
    @APIResponse(responseCode = "204", description = "Cancelled")
    @APIResponse(responseCode = "404", description = "Not found")
    public Response cancel(@PathParam("id") Long id) {
        TravelAgentBooking agg = repository.findById(id);
        if (agg == null) return Response.status(Response.Status.NOT_FOUND).build();

        if (agg.getFlightBookingId() != null) safeCancelFlight(agg.getFlightBookingId());
        if (agg.getHotelBookingId() != null) safeCancelHotel(agg.getHotelBookingId());
        if (agg.getTaxiBookingId() != null) safeCancelTaxi(agg.getTaxiBookingId());

        repository.delete(agg);
        return Response.noContent().build();
    }

    @GET
    @Path("/customers/{id}/bookings")
    @Operation(summary = "Live consolidated bookings across Taxi, Hotel, Flight for a customer")
    @APIResponse(responseCode = "200", description = "Aggregated bookings")
    public Response liveCustomerBookings(@PathParam("id") Long customerId) {
        AgentCustomerMapping map = customerMappings.findByAgentCustomerId(customerId);
        if (map == null) {
            Map<String, Object> details = new HashMap<>();
            details.put("message", "Downstream customers not provisioned for all services for this agent customer");
            return Response.status(404).entity(details).build();
        }

        CustomerBookingsAggregate aggregate = new CustomerBookingsAggregate();
        aggregate.customerId = customerId;
        Map<String, String> errors = new HashMap<>();

        // Taxi
        try {
            aggregate.taxi = taxiClient.listCustomerBookings(map.getTaxiCustomerId());
        } catch (Exception e) {
            errors.put("taxi", e.getMessage());
        }

        // Hotel
        try {
            aggregate.hotel = hotelClient.listCustomerBookings(map.getHotelCustomerId());
        } catch (Exception e) {
            errors.put("hotel", e.getMessage());
        }

        // Flight (local)
        try {
            List<Booking> flightBookings = flightOps.listFlightBookingsForCustomer(map.getFlightCustomerId());
            aggregate.flight = flightBookings.stream().map(this::toFlightDto).toList();
        } catch (Exception e) {
            errors.put("flight", e.getMessage());
        }

        if (!errors.isEmpty()) aggregate.errors = errors;
        return Response.ok(aggregate).build();
    }

    private String validate(TravelAgentRequest req) {
        if (req.customerId == null || req.customerId <= 0) return "customerId must be positive";
        if (req.date == null) return "date is required";
        if (req.taxiId == null || req.taxiId <= 0) return "taxiId must be positive";
        if (req.hotelId == null || req.hotelId <= 0) return "hotelId must be positive";
        if (req.flightId == null || req.flightId <= 0) return "flightId must be positive";
        return null;
    }

    private String validateGuest(GuestBookingRequest req) {
        if (req.name == null || req.name.isBlank()) return "name is required";
        if (req.email == null || req.email.isBlank()) return "email is required";
        if (req.phonenumber == null || req.phonenumber.isBlank()) return "phonenumber is required";
        if (req.date == null) return "date is required";
        if (req.taxiId == null || req.taxiId <= 0) return "taxiId must be positive";
        if (req.hotelId == null || req.hotelId <= 0) return "hotelId must be positive";
        if (req.flightId == null || req.flightId <= 0) return "flightId must be positive";
        return null;
    }

    private void safeCancelTaxi(Long id) {
        try { taxiClient.cancelBooking(id); } catch (Exception ignored) {}
    }

    private void safeCancelHotel(Long id) {
        try { hotelClient.cancelBooking(id); } catch (Exception ignored) {}
    }

    private void safeCancelFlight(Long id) {
        try { flightOps.cancelFlightBooking(id); } catch (Exception ignored) {}
    }

    private String inferFailedService(Long taxiBookingId, Long hotelBookingId, Long flightBookingId) {
        if (flightBookingId == null) return "flight";
        if (hotelBookingId == null) return "hotel";
        if (taxiBookingId == null) return "taxi";
        return "unknown";
    }

    private Long propagateGuestCustomerToClient(Object client, String name, String email, String phonenumber) {
        DownstreamCustomerCreate dto = new DownstreamCustomerCreate(name, email, phonenumber);
        try {
            CustomerResult created;
            if (client instanceof TaxiClient tc) {
                created = tc.createCustomer(dto);
            } else if (client instanceof HotelClient hc) {
                created = hc.createCustomer(dto);
            } else {
                return null;
            }
            return created != null ? created.id : null;
        } catch (jakarta.ws.rs.WebApplicationException wae) {
            if (wae.getResponse().getStatus() == 409) {
                List<CustomerResult> list;
                if (client instanceof TaxiClient tc) {
                    list = tc.listCustomers();
                } else if (client instanceof HotelClient hc) {
                    list = hc.listCustomers();
                } else {
                    return null;
                }
                return list.stream()
                        .filter(x -> email.equalsIgnoreCase(x.email))
                        .map(x -> x.id)
                        .findFirst()
                        .orElse(null);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private FlightBookingDto toFlightDto(Booking b) {
        FlightBookingDto dto = new FlightBookingDto();
        dto.id = b.getId();
        dto.customerId = b.getCustomer().getId();
        dto.flightId = b.getFlight().getId();
        dto.date = b.getDate();
        return dto;
    }
}