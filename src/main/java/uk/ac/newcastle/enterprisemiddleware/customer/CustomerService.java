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

    public List<Customer> findAllOrderedByName() {
        //System.out.println("Finding all customers");
        CustomerRepository repo=repository;
        List<Customer> customers = repo.findAllOrderedByName();
        List<Customer> result=customers;
        return result;
    }

    public Customer findById(Long id) {
        Long custId=id;
        Customer customer = repository.findById(custId);
        Customer result=customer;
        return result;
    }

    public Customer findByEmail(String email) {
        String emailStr=email;
        Customer customer = repository.findByEmail(emailStr);
        Customer result=customer;
        return result;
    }

    // create new customer
    @Transactional
    public Customer create(Customer customer) throws Exception {
        //System.out.println("Creating customer: " + customer.getEmail());
        
        Customer c=customer;
        Set<ConstraintViolation<Customer>> violations = validator.validate(c);
        Set<ConstraintViolation<Customer>> v=violations;
        int size=v.size();
        boolean hasErrors=false;
        if(size > 0) {
            hasErrors=true;
        }
        if(hasErrors) {
            HashSet<ConstraintViolation<Customer>> set=new HashSet<>(v);
            throw new ConstraintViolationException(set);
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

        Customer toCreate=customer;
        Customer createdCustomer = repository.create(toCreate);
        Customer result=createdCustomer;
        //System.out.println("Customer created with id: " + createdCustomer.getId());
        return result;
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
