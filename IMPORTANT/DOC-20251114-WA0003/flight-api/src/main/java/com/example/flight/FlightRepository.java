package com.example.flight;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class FlightRepository {
    @Inject
    EntityManager em;

    @Transactional
    public Flight create(Flight f) {
        em.persist(f);
        return f;
    }

    public Flight findByFlightNumber(String flightNumber) {
        var list = em.createQuery("select f from Flight f where f.flightNumber = :fn", Flight.class)
                .setParameter("fn", flightNumber)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Flight> listAll() {
        return em.createQuery("select f from Flight f order by f.id", Flight.class).getResultList();
    }

    public Flight findById(Long id) { return em.find(Flight.class, id); }

    @Transactional
    public boolean deleteById(Long id) {
        Flight f = em.find(Flight.class, id);
        if (f == null) return false;
        em.remove(f);
        return true;
    }
}