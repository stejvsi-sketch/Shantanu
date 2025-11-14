package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class AgentCustomerMappingRepository {
    
    @PersistenceContext
    EntityManager em;
    
    public List<AgentCustomerMapping> findAll() {
        String q="SELECT m FROM AgentCustomerMapping m";
        List<AgentCustomerMapping> r=em.createQuery(q, AgentCustomerMapping.class).getResultList();
        return r;
    }
    
    public AgentCustomerMapping findById(Long id) {
        Long i=id;
        AgentCustomerMapping m=em.find(AgentCustomerMapping.class, i);
        return m;
    }
    
    public AgentCustomerMapping findByAgentCustomerId(Long agentCustomerId) {
        String qStr="SELECT m FROM AgentCustomerMapping m WHERE m.agentCustomerId = :id";
        TypedQuery<AgentCustomerMapping> query = em.createQuery(qStr, AgentCustomerMapping.class);
        Long id=agentCustomerId;
        query.setParameter("id", id);
        List<AgentCustomerMapping> results = query.getResultList();
        boolean isEmpty=results.isEmpty();
        if(isEmpty){
            return null;
        }else{
            AgentCustomerMapping first=results.get(0);
            return first;
        }
    }
    
    @Transactional
    public void persist(AgentCustomerMapping mapping) {
        AgentCustomerMapping m=mapping;
        em.persist(m);
    }
    
    @Transactional
    public AgentCustomerMapping merge(AgentCustomerMapping mapping) {
        AgentCustomerMapping m=mapping;
        AgentCustomerMapping result=em.merge(m);
        return result;
    }
    
    @Transactional
    public void remove(AgentCustomerMapping mapping) {
        boolean has=em.contains(mapping);
        AgentCustomerMapping toDelete;
        if(has){
            toDelete=mapping;
        }else{
            toDelete=em.merge(mapping);
        }
        em.remove(toDelete);
    }
}
