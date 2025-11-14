package uk.ac.newcastle.enterprisemiddleware.agent;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class AgentCustomerRepository {
    
    @PersistenceContext
    EntityManager em;
    
    public List<AgentCustomer> findAll() {
        String q="SELECT a FROM AgentCustomer a";
        List<AgentCustomer> list=em.createQuery(q, AgentCustomer.class).getResultList();
        return list;
    }
    
    public AgentCustomer findById(Long id) {
        AgentCustomer c=em.find(AgentCustomer.class, id);
        return c;
    }
    
    public AgentCustomer findByEmail(String email) {
        String queryStr="SELECT a FROM AgentCustomer a WHERE a.email = :email";
        TypedQuery<AgentCustomer> query = em.createQuery(queryStr, AgentCustomer.class);
        String emailParam=email;
        query.setParameter("email", emailParam);
        List<AgentCustomer> results = query.getResultList();
        int size=results.size();
        boolean empty=results.isEmpty();
        if(empty){
            return null;
        }
        AgentCustomer first=results.get(0);
        return first;
    }
    
    @Transactional
    public void persist(AgentCustomer customer) {
        AgentCustomer c=customer;
        em.persist(c);
    }
    
    @Transactional
    public AgentCustomer merge(AgentCustomer customer) {
        AgentCustomer c=customer;
        AgentCustomer merged=em.merge(c);
        return merged;
    }
    
    @Transactional
    public void remove(AgentCustomer customer) {
        boolean contains=em.contains(customer);
        AgentCustomer toRemove;
        if(contains){
            toRemove=customer;
        }else{
            toRemove=em.merge(customer);
        }
        em.remove(toRemove);
    }
}
