package uk.ac.newcastle.enterprisemiddleware.customer;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static uk.ac.newcastle.enterprisemiddleware.util.TestDataGenerator.*;

// test class for customer REST endpoints
// tests if the customer API works correctly
@QuarkusTest
public class CustomerRestServiceTest {

    // test to get all customers from the API
    @Test
    public void testRetrieveAllCustomers() {
        // make GET request to /api/customers
        given()
            .when().get("/api/customers")
            .then()
            .statusCode(200) // should return 200 OK
            .contentType(ContentType.JSON); // should return JSON
    }

    // test creating a customer with valid data
    @Test
    public void testCreateValidCustomer() {
        // generate unique email and phone
        String email = uniqueEmail("arjun.patel");
        String phone = uniquePhone("074");
        // create JSON string for customer
        String customerJson = "{\"name\":\"Arjun Patel\",\"email\":\"" + email + "\",\"phoneNumber\":\"" + phone + "\"}";
        
        // send POST request to create customer
        given()
            .contentType(ContentType.JSON) // set content type
            .body(customerJson) // send customer data
            .when().post("/api/customers") // POST to /api/customers
            .then()
            .statusCode(201) // should return 201 Created
            .body("name", equalTo("Arjun Patel")) // check name
            .body("email", equalTo(email)) // check email
            .body("phoneNumber", equalTo(phone)); // check phone
    }

    // test that bad email gets rejected
    @Test
    public void testCreateCustomerWithInvalidEmail() {
        // email without @ symbol - should fail
        String customerJson = "{\"name\":\"Raj Kumar\",\"email\":\"invalid-email\",\"phoneNumber\":\"07456789012\"}";
        
        // try to create customer with bad email
        given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/api/customers")
            .then()
            .statusCode(400); // should return 400 Bad Request
    }

    // test phone number validation
    @Test
    public void testCreateCustomerWithInvalidPhoneNumber() {
        // phone number too short - needs 11 digits
        String customerJson = "{\"name\":\"Raj Kumar\",\"email\":\"test@example.com\",\"phoneNumber\":\"123\"}";
        
        // send request
        given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/api/customers")
            .then()
            .statusCode(400); // validation should fail
    }

    // test name validation - names cant have numbers
    @Test
    public void testCreateCustomerWithInvalidName() {
        // name has numbers which is not allowed
        String customerJson = "{\"name\":\"Test123\",\"email\":\"test@example.com\",\"phoneNumber\":\"07456789012\"}";
        
        // try to create
        given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/api/customers")
            .then()
            .statusCode(400); // should fail
    }

    // test getting a customer by their id
    @Test
    public void testRetrieveCustomerById() {
        // get customer with id 1 (should exist from import.sql)
        given()
            .when().get("/api/customers/1")
            .then()
            .statusCode(200) // should be OK
            .body("id", equalTo(1)) // id should be 1
            .body("name", notNullValue()); // name should not be null
    }

    // test getting customer that doesnt exist
    @Test
    public void testRetrieveNonExistentCustomer() {
        // try to get customer with id 999 - doesnt exist
        given()
            .when().get("/api/customers/999")
            .then()
            .statusCode(404); // should return 404 Not Found
    }

    // test deleting a customer
    @Test
    public void testDeleteCustomer() {
        // create unique email and phone
        String email = uniqueEmail("delete.customer");
        String phone = uniquePhone("075");
        String customerJson = "{\"name\":\"Vikram Joshi\",\"email\":\"" + email + "\",\"phoneNumber\":\"" + phone + "\"}";
        
        // step 1: create a new customer first
        int customerId = given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/api/customers")
            .then()
            .statusCode(201) // customer created
            .extract().path("id"); // get the id
        
        // step 2: delete the customer
        given()
            .when().delete("/api/customers/" + customerId)
            .then()
            .statusCode(204); // 204 No Content means deleted
        
        // step 3: try to get it again - should be gone
        given()
            .when().get("/api/customers/" + customerId)
            .then()
            .statusCode(404); // should return 404 because its deleted
    }
}
