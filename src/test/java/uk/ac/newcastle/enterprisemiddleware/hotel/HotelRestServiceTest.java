package uk.ac.newcastle.enterprisemiddleware.hotel;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static uk.ac.newcastle.enterprisemiddleware.util.TestDataGenerator.*;

// hotel endpoint tests
@QuarkusTest
public class HotelRestServiceTest {

    // basic test to get all hotels
    @Test
    public void testRetrieveAllHotels() {
        given()
            .when().get("/api/hotels")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    // test creating hotel with valid data
    @Test
    public void testCreateValidHotel() {
        String phone = uniquePhone("015");
        String hotelJson = "{\"name\":\"Sunset Resort\",\"phoneNumber\":\"" + phone + "\",\"postcode\":\"NE1234\"}";
        
        given()
            .contentType(ContentType.JSON)
            .body(hotelJson)
            .when().post("/api/hotels")
            .then()
            .statusCode(201)
            .body("name", equalTo("Sunset Resort"))
            .body("phoneNumber", equalTo(phone))
            .body("postcode", equalTo("NE1234"));
    }

    @Test
    public void testCreateHotelWithInvalidPhoneNumber() {
        String hotelJson = "{\"name\":\"Test Hotel\",\"phoneNumber\":\"123\",\"postcode\":\"NE1111\"}";
        
        given()
            .contentType(ContentType.JSON)
            .body(hotelJson)
            .when().post("/api/hotels")
            .then()
            .statusCode(400); // phone number too short
    }

    // postcode has specific format requirement
    @Test
    public void testCreateHotelWithInvalidPostcode() {
        String hotelJson = "{\"name\":\"Test Hotel\",\"phoneNumber\":\"01678901234\",\"postcode\":\"INVALID\"}";
        
        given()
            .contentType(ContentType.JSON)
            .body(hotelJson)
            .when().post("/api/hotels")
            .then()
            .statusCode(400);
    }

    @Test
    public void testRetrieveHotelById() {
        given()
            .when().get("/api/hotels/1")
            .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("name", notNullValue());
    }

    @Test
    public void testRetrieveNonExistentHotel() {
        given()
            .when().get("/api/hotels/999")
            .then()
            .statusCode(404);
    }

    @Test
    public void testDeleteHotel() {
        String phone = uniquePhone("017");
        String hotelJson = "{\"name\":\"Delete Hotel\",\"phoneNumber\":\"" + phone + "\",\"postcode\":\"NE5555\"}";
        
        int hotelId = given()
            .contentType(ContentType.JSON)
            .body(hotelJson)
            .when().post("/api/hotels")
            .then()
            .statusCode(201)
            .extract().path("id");
        
        given()
            .when().delete("/api/hotels/" + hotelId)
            .then()
            .statusCode(204);
        
        given()
            .when().get("/api/hotels/" + hotelId)
            .then()
            .statusCode(404);
    }
}
