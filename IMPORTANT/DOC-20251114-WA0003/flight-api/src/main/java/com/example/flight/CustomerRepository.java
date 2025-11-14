package com.example.flight;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class CustomerRepository {
    @Inject
    EntityManager em;

    @Transactional
    public Customer create(Customer c) {
        em.persist(c);
        return c;
    }

    public Customer findByEmail(String email) {
        var list = em.createQuery("select c from Customer c where c.email = :e", Customer.class)
                .setParameter("e", email)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Customer> listAll() {
        return em.createQuery("select c from Customer c order by c.id", Customer.class).getResultList();
    }

    public Customer findById(Long id) { return em.find(Customer.class, id); }
}