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

// customer service
@ApplicationScoped
public class CustomerService {

    @Inject
    Validator validator;

    @Inject
    CustomerRepository repository;

    public List<Customer> findAllOrderedByName() {
        List<Customer> customers = repository.findAllOrderedByName();
        return customers;
    }

    public Customer findById(Long id) {
        Customer customer = repository.findById(id);
        return customer;
    }

    public Customer findByEmail(String email) {
        Customer customer = repository.findByEmail(email);
        return customer;
    }

    @Transactional
    public Customer create(Customer customer) throws Exception {
        // validate first
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }
        
        // check email not already used
        Customer existingCustomer = repository.findByEmail(customer.getEmail());
        if(existingCustomer != null) {
            if(customer.getId() == null || !existingCustomer.getId().equals(customer.getId())) {
                throw new Exception("Email already exists");
            }
        }

        Customer createdCustomer = repository.create(customer);
        return createdCustomer;
    }

    @Transactional
    public Customer update(Customer customer) throws Exception {
        // validate
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }
        
        // check email
        Customer existingCustomer = repository.findByEmail(customer.getEmail());
        if(existingCustomer != null) {
            if(!existingCustomer.getId().equals(customer.getId())) {
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
