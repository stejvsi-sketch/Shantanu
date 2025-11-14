package uk.ac.newcastle.enterprisemiddleware.customer;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static uk.ac.newcastle.enterprisemiddleware.util.TestDataGenerator.*;

// Tests for customer REST endpoints
@QuarkusTest
public class CustomerRestServiceTest {

    // test getting all customers
    @Test
    public void testRetrieveAllCustomers() {
        given()
            .when().get("/api/customers")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    // create customer with valid data
    @Test
    public void testCreateValidCustomer() {
        String email = uniqueEmail("arjun.patel");
        String phone = uniquePhone("074");
        String customerJson = "{\"name\":\"Arjun Patel\",\"email\":\"" + email + "\",\"phoneNumber\":\"" + phone + "\"}";
        
        given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/api/customers")
            .then()
            .statusCode(201)
            .body("name", equalTo("Arjun Patel"))
            .body("email", equalTo(email))
            .body("phoneNumber", equalTo(phone));
    }

    // should reject bad email format
    @Test
    public void testCreateCustomerWithInvalidEmail() {
        String customerJson = "{\"name\":\"Raj Kumar\",\"email\":\"invalid-email\",\"phoneNumber\":\"07456789012\"}";
        
        given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/api/customers")
            .then()
            .statusCode(400);
    }

    @Test
    public void testCreateCustomerWithInvalidPhoneNumber() {
        String customerJson = "{\"name\":\"Raj Kumar\",\"email\":\"test@example.com\",\"phoneNumber\":\"123\"}";
        
        given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/api/customers")
            .then()
            .statusCode(400); // should fail validation
    }

    @Test
    public void testCreateCustomerWithInvalidName() {
        String customerJson = "{\"name\":\"Test123\",\"email\":\"test@example.com\",\"phoneNumber\":\"07456789012\"}";
        
        given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/api/customers")
            .then()
            .statusCode(400); // names cant have numbers
    }

    // get customer by id
    @Test
    public void testRetrieveCustomerById() {
        given()
            .when().get("/api/customers/1")
            .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("name", notNullValue());
    }

    @Test
    public void testRetrieveNonExistentCustomer() {
        given()
            .when().get("/api/customers/999")
            .then()
            .statusCode(404); // should return 404 for non-existent id
    }

    // test delete - creates customer then deletes it
    @Test
    public void testDeleteCustomer() {
        String email = uniqueEmail("delete.customer");
        String phone = uniquePhone("075");
        String customerJson = "{\"name\":\"Vikram Joshi\",\"email\":\"" + email + "\",\"phoneNumber\":\"" + phone + "\"}";
        
        // first create a customer
        int customerId = given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/api/customers")
            .then()
            .statusCode(201)
            .extract().path("id");
        
        // then delete it
        given()
            .when().delete("/api/customers/" + customerId)
            .then()
            .statusCode(204);
        
        // verify its gone
        given()
            .when().get("/api/customers/" + customerId)
            .then()
            .statusCode(404);
    }
}
