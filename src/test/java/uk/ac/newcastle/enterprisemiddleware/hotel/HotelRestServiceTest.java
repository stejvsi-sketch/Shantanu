package uk.ac.newcastle.enterprisemiddleware.hotel;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static uk.ac.newcastle.enterprisemiddleware.util.TestDataGenerator.*;

// test class for hotel REST API
// makes sure hotel endpoints work
@QuarkusTest
public class HotelRestServiceTest {

    // test getting all hotels from API
    @Test
    public void testRetrieveAllHotels() {
        // send GET request
        given()
            .when().get("/api/hotels")
            .then()
            .statusCode(200) // should be successful
            .contentType(ContentType.JSON); // should return JSON
    }

    // test creating hotel with correct data
    @Test
    public void testCreateValidHotel() {
        // make unique phone number
        String phone = uniquePhone("015");
        // create JSON for hotel data
        String hotelJson = "{\"name\":\"Sunset Resort\",\"phoneNumber\":\"" + phone + "\",\"postcode\":\"NE1234\"}";
        
        // send POST to create hotel
        given()
            .contentType(ContentType.JSON) // JSON content type
            .body(hotelJson) // hotel data
            .when().post("/api/hotels") // POST request
            .then()
            .statusCode(201) // should return 201 Created
            .body("name", equalTo("Sunset Resort")) // verify name
            .body("phoneNumber", equalTo(phone)) // verify phone
            .body("postcode", equalTo("NE1234")); // verify postcode
    }

    // test validation for phone number
    @Test
    public void testCreateHotelWithInvalidPhoneNumber() {
        // phone number is too short - should be 11 digits
        String hotelJson = "{\"name\":\"Test Hotel\",\"phoneNumber\":\"123\",\"postcode\":\"NE1111\"}";
        
        // try to create hotel
        given()
            .contentType(ContentType.JSON)
            .body(hotelJson)
            .when().post("/api/hotels")
            .then()
            .statusCode(400); // should fail with 400
    }

    // test postcode validation - has to match pattern
    @Test
    public void testCreateHotelWithInvalidPostcode() {
        // postcode format is wrong - should be like NE1234
        String hotelJson = "{\"name\":\"Test Hotel\",\"phoneNumber\":\"01678901234\",\"postcode\":\"INVALID\"}";
        
        // send request
        given()
            .contentType(ContentType.JSON)
            .body(hotelJson)
            .when().post("/api/hotels")
            .then()
            .statusCode(400); // should fail validation
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
