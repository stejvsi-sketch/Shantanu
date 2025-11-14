package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

// repository for customer mappings
@ApplicationScoped
public class AgentCustomerMappingRepository {
    
    @PersistenceContext
    EntityManager em;
    
    public List<AgentCustomerMapping> findAll() {
        return em.createQuery("SELECT m FROM AgentCustomerMapping m", AgentCustomerMapping.class).getResultList();
    }
    
    public AgentCustomerMapping findById(Long id) {
        return em.find(AgentCustomerMapping.class, id);
    }
    
    public AgentCustomerMapping findByAgentCustomerId(Long agentCustomerId) {
        TypedQuery<AgentCustomerMapping> query = em.createQuery(
            "SELECT m FROM AgentCustomerMapping m WHERE m.agentCustomerId = :id", AgentCustomerMapping.class);
        query.setParameter("id", agentCustomerId);
        List<AgentCustomerMapping> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Transactional
    public void persist(AgentCustomerMapping mapping) {
        em.persist(mapping);
    }
    
    @Transactional
    public AgentCustomerMapping merge(AgentCustomerMapping mapping) {
        return em.merge(mapping);
    }
    
    @Transactional
    public void remove(AgentCustomerMapping mapping) {
        em.remove(em.contains(mapping) ? mapping : em.merge(mapping));
    }
}
