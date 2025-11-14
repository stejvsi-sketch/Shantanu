package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

// repository for agent customers
@ApplicationScoped
public class AgentCustomerRepository {
    
    @PersistenceContext
    EntityManager em;
    
    public List<AgentCustomer> findAll() {
        return em.createQuery("SELECT a FROM AgentCustomer a", AgentCustomer.class).getResultList();
    }
    
    public AgentCustomer findById(Long id) {
        return em.find(AgentCustomer.class, id);
    }
    
    public AgentCustomer findByEmail(String email) {
        TypedQuery<AgentCustomer> query = em.createQuery(
            "SELECT a FROM AgentCustomer a WHERE a.email = :email", AgentCustomer.class);
        query.setParameter("email", email);
        List<AgentCustomer> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Transactional
    public void persist(AgentCustomer customer) {
        em.persist(customer);
    }
    
    @Transactional
    public AgentCustomer merge(AgentCustomer customer) {
        return em.merge(customer);
    }
    
    @Transactional
    public void remove(AgentCustomer customer) {
        em.remove(em.contains(customer) ? customer : em.merge(customer));
    }
}
