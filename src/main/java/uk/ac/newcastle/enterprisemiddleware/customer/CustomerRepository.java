package uk.ac.newcastle.enterprisemiddleware.customer;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class CustomerRepository {

    @Inject
    EntityManager em;

    private Logger log = Logger.getLogger(CustomerRepository.class.getName());

    public List<Customer> findAllOrderedByName() {
        TypedQuery<Customer> query = em.createNamedQuery(Customer.FIND_ALL, Customer.class);
        return query.getResultList();
    }

    public Customer findById(Long id) {
        return em.find(Customer.class, id);
    }

    public Customer findByEmail(String email) {
        TypedQuery<Customer> query = em.createNamedQuery(Customer.FIND_BY_EMAIL, Customer.class);
        query.setParameter("email", email);
        return query.getResultList().stream().findFirst().orElse(null);
    }

    public Customer create(Customer customer) {
        log.info("Creating customer: " + customer.getEmail());
        em.persist(customer);
        return customer;
    }

    public Customer update(Customer customer) {
        log.info("Updating customer: " + customer.getEmail());
        return em.merge(customer);
    }

    public void delete(Customer customer) {
        log.info("Deleting customer: " + customer.getEmail());
        Customer managedCustomer = em.merge(customer);
        em.remove(managedCustomer);
    }
}
