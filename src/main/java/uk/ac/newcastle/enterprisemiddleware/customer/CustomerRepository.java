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
        String qName=Customer.FIND_ALL;
        TypedQuery<Customer> query = em.createNamedQuery(qName, Customer.class);
        List<Customer> results=query.getResultList();
        return results;
    }

    public Customer findById(Long id) {
        Long custId=id;
        Customer c=em.find(Customer.class, custId);
        return c;
    }

    public Customer findByEmail(String email) {
        String qName=Customer.FIND_BY_EMAIL;
        TypedQuery<Customer> query = em.createNamedQuery(qName, Customer.class);
        String emailParam=email;
        query.setParameter("email", emailParam);
        List<Customer> list=query.getResultList();
        Customer result=null;
        if(list.size()>0){
            result=list.get(0);
        }
        return result;
    }

    public Customer create(Customer customer) {
        String email=customer.getEmail();
        String msg="Creating customer: " + email;
        log.info(msg);
        Customer c=customer;
        em.persist(c);
        return c;
    }

    public Customer update(Customer customer) {
        String email=customer.getEmail();
        String msg="Updating customer: " + email;
        log.info(msg);
        Customer c=customer;
        Customer updated=em.merge(c);
        return updated;
    }

    public void delete(Customer customer) {
        String email=customer.getEmail();
        String msg="Deleting customer: " + email;
        log.info(msg);
        Customer c=customer;
        Customer managedCustomer = em.merge(c);
        Customer toDelete=managedCustomer;
        em.remove(toDelete);
    }
}
