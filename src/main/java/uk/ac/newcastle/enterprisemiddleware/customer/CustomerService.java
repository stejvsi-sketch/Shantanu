package uk.ac.newcastle.enterprisemiddleware.customer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// this is the service class for customers
// handles all the customer stuff
@ApplicationScoped
public class CustomerService {

    @Inject
    Validator validator; // for validation

    @Inject
    CustomerRepository repository; // repository object

    // get all customers
    public List<Customer> findAllOrderedByName() {
        //System.out.println("Finding all customers");
        List<Customer> customers = repository.findAllOrderedByName();
        return customers;
    }

    // find customer by id
    public Customer findById(Long id) {
        Customer customer = repository.findById(id);
        return customer;
    }

    // find by email
    public Customer findByEmail(String email) {
        Customer customer = repository.findByEmail(email);
        return customer;
    }

    // create new customer
    @Transactional
    public Customer create(Customer customer) throws Exception {
        //System.out.println("Creating customer: " + customer.getEmail());
        
        // do validation
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        if(violations.size() > 0) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }
        
        // check if email already exists in database
        String emailToCheck = customer.getEmail();
        Customer existingCustomer = repository.findByEmail(emailToCheck);
        if(existingCustomer != null) {
            // email is already there
            Long customerId = customer.getId();
            if(customerId == null) {
                throw new Exception("Email already exists");
            }
            Long existingId = existingCustomer.getId();
            if(!existingId.equals(customerId)) {
                throw new Exception("Email already exists");
            }
        }

        Customer createdCustomer = repository.create(customer);
        //System.out.println("Customer created with id: " + createdCustomer.getId());
        return createdCustomer;
    }

    // update customer
    @Transactional
    public Customer update(Customer customer) throws Exception {
        // validate the customer
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        if(violations.size() > 0) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }
        
        // check if email is taken by another customer
        String email = customer.getEmail();
        Customer existingCustomer = repository.findByEmail(email);
        if(existingCustomer != null) {
            Long existingId = existingCustomer.getId();
            Long currentId = customer.getId();
            if(!existingId.equals(currentId)) {
                throw new Exception("Email already exists");
            }
        }

        Customer updatedCustomer = repository.update(customer);
        return updatedCustomer;
    }

    @Transactional
    public void delete(Customer customer) {
        repository.delete(customer);
    }
}
