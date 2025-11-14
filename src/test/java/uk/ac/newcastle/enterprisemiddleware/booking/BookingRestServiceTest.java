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
        Calendar c=cal;
        int days=30;
        c.add(Calendar.DAY_OF_YEAR, days);
        Date futureDate = c.getTime();
        Date d=futureDate;
        String pattern="yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        SimpleDateFormat formatter=sdf;
        String result=formatter.format(d);
        return result;
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
        String date=getFutureDate();
        String bookingJson = "{\"customerId\":1,\"hotelId\":1,\"date\":\"" + date + "\"}";
        String json=bookingJson;
        
        given()
            .contentType(ContentType.JSON)
            .body(json)
            .when().post("/api/bookings")
            .then()
            .statusCode(201)
            .body("id", notNullValue());
    }

    @Test
    public void testCreateBookingWithPastDate() {
        // test past date validation
        String bookingJson = "{\"customerId\":1,\"hotelId\":1,\"date\":\"2020-01-01\"}";
        
        given()
            .contentType(ContentType.JSON)
            .body(bookingJson)
            .when().post("/api/bookings")
            .then()
            .statusCode(400);
    }

    @Test
    public void testCreateBookingWithNonExistentCustomer() {
        // customer 999 doesnt exist
        String bookingJson = "{\"customerId\":999,\"hotelId\":1,\"date\":\"" + getFutureDate() + "\"}";
        
        given()
            .contentType(ContentType.JSON)
            .body(bookingJson)
            .when().post("/api/bookings")
            .then()
            .statusCode(404);
    }

    @Test
    public void testCreateBookingWithNonExistentHotel() {
        // hotel 999 doesnt exist
        String bookingJson = "{\"customerId\":1,\"hotelId\":999,\"date\":\"" + getFutureDate() + "\"}";
        
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
        // create booking then cancel it
        String bookingJson = "{\"customerId\":2,\"hotelId\":2,\"date\":\"" + getFutureDate() + "\"}";
        
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
        // create hotel and customer, then delete customer and check cascade
        int hotelId = createHotel("NE7777");
        int customerId = createCustomer();
        String bookingJson = "{\"customerId\":" + customerId + ",\"hotelId\":" + hotelId + ",\"date\":\"" + getFutureDate() + "\"}";
        
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
        // create customer and hotel, then delete hotel and check cascade
        int customerId = createCustomer();
        int hotelId = createHotel("NE6666");
        String bookingJson = "{\"customerId\":" + customerId + ",\"hotelId\":" + hotelId + ",\"date\":\"" + getFutureDate() + "\"}";
        
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
