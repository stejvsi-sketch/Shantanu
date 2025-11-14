package uk.ac.newcastle.enterprisemiddleware.booking;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static uk.ac.newcastle.enterprisemiddleware.util.TestDataGenerator.*;

@QuarkusTest
public class BookingRestServiceTest {

    private String getFutureDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 30);
        Date futureDate = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(futureDate);
    }

    @Test
    public void testRetrieveAllBookings() {
        given()
            .when().get("/api/bookings")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    public void testCreateValidBooking() {
        String bookingJson = "{\"customer\":{\"id\":1},\"hotel\":{\"id\":1},\"bookingDate\":\"" + getFutureDate() + "\"}";
        
        given()
            .contentType(ContentType.JSON)
            .body(bookingJson)
            .when().post("/api/bookings")
            .then()
            .statusCode(201)
            .body("customer.id", equalTo(1))
            .body("hotel.id", equalTo(1));
    }

    @Test
    public void testCreateBookingWithPastDate() {
        String bookingJson = "{\"customer\":{\"id\":1},\"hotel\":{\"id\":1},\"bookingDate\":\"2020-01-01\"}";
        
        given()
            .contentType(ContentType.JSON)
            .body(bookingJson)
            .when().post("/api/bookings")
            .then()
            .statusCode(400);
    }

    @Test
    public void testCreateBookingWithNonExistentCustomer() {
        String bookingJson = "{\"customer\":{\"id\":999},\"hotel\":{\"id\":1},\"bookingDate\":\"" + getFutureDate() + "\"}";
        
        given()
            .contentType(ContentType.JSON)
            .body(bookingJson)
            .when().post("/api/bookings")
            .then()
            .statusCode(404);
    }

    @Test
    public void testCreateBookingWithNonExistentHotel() {
        String bookingJson = "{\"customer\":{\"id\":1},\"hotel\":{\"id\":999},\"bookingDate\":\"" + getFutureDate() + "\"}";
        
        given()
            .contentType(ContentType.JSON)
            .body(bookingJson)
            .when().post("/api/bookings")
            .then()
            .statusCode(404);
    }

    @Test
    public void testRetrieveBookingsByCustomerId() {
        given()
            .when().get("/api/bookings/customer/1")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    public void testCancelBooking() {
        String bookingJson = "{\"customer\":{\"id\":2},\"hotel\":{\"id\":2},\"bookingDate\":\"" + getFutureDate() + "\"}";
        
        int bookingId = given()
            .contentType(ContentType.JSON)
            .body(bookingJson)
            .when().post("/api/bookings")
            .then()
            .statusCode(201)
            .extract().path("id");
        
        given()
            .when().delete("/api/bookings/" + bookingId)
            .then()
            .statusCode(204);
        
        given()
            .when().get("/api/bookings/" + bookingId)
            .then()
            .statusCode(404);
    }

    @Test
    public void testDeleteCustomerCascadesBookings() {
        int hotelId = createHotel("NE7777");
        int customerId = createCustomer();
        String bookingJson = "{\"customer\":{\"id\":" + customerId + "},\"hotel\":{\"id\":" + hotelId + "},\"bookingDate\":\"" + getFutureDate() + "\"}";
        
        int bookingId = given()
            .contentType(ContentType.JSON)
            .body(bookingJson)
            .when().post("/api/bookings")
            .then()
            .statusCode(201)
            .extract().path("id");
        
        given()
            .when().delete("/api/customers/" + customerId)
            .then()
            .statusCode(204);
        
        given()
            .when().get("/api/bookings/" + bookingId)
            .then()
            .statusCode(404);
    }

    @Test
    public void testDeleteHotelCascadesBookings() {
        int customerId = createCustomer();
        int hotelId = createHotel("NE6666");
        String bookingJson = "{\"customer\":{\"id\":" + customerId + "},\"hotel\":{\"id\":" + hotelId + "},\"bookingDate\":\"" + getFutureDate() + "\"}";
        
        int bookingId = given()
            .contentType(ContentType.JSON)
            .body(bookingJson)
            .when().post("/api/bookings")
            .then()
            .statusCode(201)
            .extract().path("id");
        
        given()
            .when().delete("/api/hotels/" + hotelId)
            .then()
            .statusCode(204);
        
        given()
            .when().get("/api/bookings/" + bookingId)
            .then()
            .statusCode(404);
    }

    private int createCustomer() {
        String email = uniqueEmail("cascade.customer");
        String phone = uniquePhone("078");
        String customerJson = "{\"name\":\"Rohan Mehta\",\"email\":\"" + email + "\",\"phoneNumber\":\"" + phone + "\"}";

        return given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/api/customers")
            .then()
            .statusCode(201)
            .extract().path("id");
    }

    private int createHotel(String postcode) {
        String phone = uniquePhone("018");
        String hotelJson = "{\"name\":\"Cascade Hotel\",\"phoneNumber\":\"" + phone + "\",\"postcode\":\"" + postcode + "\"}";

        return given()
            .contentType(ContentType.JSON)
            .body(hotelJson)
            .when().post("/api/hotels")
            .then()
            .statusCode(201)
            .extract().path("id");
    }
}
