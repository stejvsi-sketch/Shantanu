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

/**
 * Service layer for customer operations
 * handles validation and business logic
 */
@ApplicationScoped
public class CustomerService {

    @Inject
    Validator validator;

    @Inject
    CustomerRepository repository;


    // get all customers
    public List<Customer> findAllOrderedByName() {
        return repository.findAllOrderedByName();
    }

    public Customer findById(Long id) {
        return repository.findById(id);
    }

    public Customer findByEmail(String email) {
        return repository.findByEmail(email);
    }


    // create new customer
    @Transactional
    public Customer create(Customer customer) throws Exception {
        validateCustomer(customer);
        
        // check if email already taken
        if(emailAlreadyExists(customer.getEmail(), customer.getId())) {
            throw new Exception("Email already exists");
        }

        return repository.create(customer);
    }

    @Transactional
    public Customer update(Customer customer) throws Exception {
        validateCustomer(customer);
        
        if(emailAlreadyExists(customer.getEmail(), customer.getId())) {
            throw new Exception("Email already exists");
        }

        return repository.update(customer);
    }

    @Transactional
    public void delete(Customer customer) {
        repository.delete(customer);
    }

    // validate customer data
    private void validateCustomer(Customer customer) throws ConstraintViolationException {
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }
    }

    // helper method to check duplicate emails
    private boolean emailAlreadyExists(String email, Long id) {
        Customer existing = repository.findByEmail(email);
        if(existing != null && !existing.getId().equals(id)) {
            return true;
        }
        return false;
    }
}
