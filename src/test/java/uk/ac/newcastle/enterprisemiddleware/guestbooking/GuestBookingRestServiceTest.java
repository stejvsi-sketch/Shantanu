package uk.ac.newcastle.enterprisemiddleware.guestbooking;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class GuestBookingRestServiceTest {

    private String getFutureDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 30);
        Date futureDate = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(futureDate);
    }

    @Test
    public void testCreateValidGuestBooking() {
        String guestBookingJson = "{\"customer\":{\"name\":\"Amit Singh\",\"email\":\"guest@test.com\",\"phoneNumber\":\"07901234567\"}," +
                                  "\"booking\":{\"hotel\":{\"id\":1},\"bookingDate\":\"" + getFutureDate() + "\"}}";
        
        given()
            .contentType(ContentType.JSON)
            .body(guestBookingJson)
            .when().post("/api/guest-bookings")
            .then()
            .statusCode(201)
            .body("customer", notNullValue())
            .body("hotel", notNullValue());
    }

    @Test
    public void testCreateGuestBookingWithInvalidCustomer() {
        String guestBookingJson = "{\"customer\":{\"name\":\"Invalid123\",\"email\":\"invalid-email\",\"phoneNumber\":\"123\"}," +
                                  "\"booking\":{\"hotel\":{\"id\":1},\"bookingDate\":\"" + getFutureDate() + "\"}}";
        
        given()
            .contentType(ContentType.JSON)
            .body(guestBookingJson)
            .when().post("/api/guest-bookings")
            .then()
            .statusCode(400);
    }

    @Test
    public void testCreateGuestBookingWithInvalidBooking() {
        String guestBookingJson = "{\"customer\":{\"name\":\"Neha Reddy\",\"email\":\"valid@test.com\",\"phoneNumber\":\"07012345678\"}," +
                                  "\"booking\":{\"hotel\":{\"id\":1},\"bookingDate\":\"2020-01-01\"}}";
        
        given()
            .contentType(ContentType.JSON)
            .body(guestBookingJson)
            .when().post("/api/guest-bookings")
            .then()
            .statusCode(anyOf(is(400), is(500)));
    }

    @Test
    public void testGuestBookingTransactionRollback() {
        String guestBookingJson = "{\"customer\":{\"name\":\"Rollback Test\",\"email\":\"shantanu.raj@example.com\",\"phoneNumber\":\"07123456780\"}," +
                                  "\"booking\":{\"hotel\":{\"id\":1},\"bookingDate\":\"" + getFutureDate() + "\"}}";
        
        given()
            .contentType(ContentType.JSON)
            .body(guestBookingJson)
            .when().post("/api/guest-bookings")
            .then()
            .statusCode(anyOf(is(409), is(500)));
        
        given()
            .when().get("/api/customers")
            .then()
            .statusCode(200)
            .body("findAll { it.email == 'shantanu.raj@example.com' }.size()", equalTo(1));
    }

    @Test
    public void testCreateGuestBookingWithNonExistentHotel() {
        String guestBookingJson = "{\"customer\":{\"name\":\"Raj Kumar\",\"email\":\"test.hotel@example.com\",\"phoneNumber\":\"07234567891\"}," +
                                  "\"booking\":{\"hotel\":{\"id\":999},\"bookingDate\":\"" + getFutureDate() + "\"}}";
        
        given()
            .contentType(ContentType.JSON)
            .body(guestBookingJson)
            .when().post("/api/guest-bookings")
            .then()
            .statusCode(404);
    }
}
