/*
 * PersistenceService.java
 *
 * Created on August 17, 2007, 4:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package customerdb.service;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 * Utility class for dealing with persistence.
 *
 * @author Peter Liu
 */
public class PersistenceService {
    private static String DEFAULT_PU = "CustomerDBPU";
    
    private static ThreadLocal<PersistenceService> instance = new ThreadLocal<PersistenceService>() {
        protected PersistenceService initialValue() {
            return new PersistenceService(DEFAULT_PU);
        }
    };
    
    private EntityManagerFactory emf;
    private EntityManager em;
    
    private PersistenceService(String puName) {
        try {
            this.emf = Persistence.createEntityManagerFactory(puName);
            this.em = emf.createEntityManager();
        } catch (RuntimeException ex) {
            if (emf != null) {
                emf.close();
            }
            
            throw ex;
        }
    }
    
    /**
     * Returns an instance of PersistenceService.
     *
     * @return an instance of PersistenceService
     */
    public static PersistenceService getInstance() {
        return instance.get();
    }
    
    private static void removeInstance() {
        instance.remove();
    }
    
    /**
     * Refreshes the state of the given entity from the database.
     *
     * @param entity the entity to refresh
     */
    public void refreshEntity(Object entity) {
        em.refresh(entity);
    }
    
    /**
     * Merges the state of the given entity into the current persistence context.
     *
     * @param entity the entity to merge
     * @return the merged entity
     */
    public <T> T mergeEntity(T entity) {
        return (T) em.merge(entity);
    }
    
    /**
     * Makes the given entity managed and persistent.
     *
     * @param entity the entity to persist
     */
    public void persistEntity(Object entity) {
        em.persist(entity);
    }
    
    /**
     * Removes the entity instance.
     *
     * @param entity the entity to remove
     */
    public void removeEntity(Object entity) {
        em.remove(entity);
    }
    
    /**
     * Resolves the given entity to the actual entity instance in the current persistence context.
     *
     * @param entity the entity to resolve
     * @return the resolved entity
     */
    public <T> T resolveEntity(T entity) {
        entity = mergeEntity(entity);
        em.refresh(entity);
        
        return entity;
    }
    
    /**
     * Returns an instance of Query for executing a named query.
     *
     * @param query the named query
     * @return an instance of Query
     */
    public Query createNamedQuery(String query) {
        return em.createNamedQuery(query);
    }
    
    /**
     * Returns an instance of Query for executing a query.
     *
     * @param query the query string
     * @return an instance of Query
     */
    public Query createQuery(String query) {
        return em.createQuery(query);
    }
    
    /**
     * Begins a resource transaction.
     */
    public void beginTx() {
        EntityTransaction tx = em.getTransaction();
        
        if (!tx.isActive()) {
            tx.begin();
        }
    }
    
    /**
     * Commits a resource transaction.
     */
    public void commitTx() {
        EntityTransaction tx = em.getTransaction();
        
        if (tx.isActive()) {
            tx.commit();
        }
    }
    
    /**
     * Rolls back a resource transaction.
     */
    public void rollbackTx() {
        EntityTransaction tx = em.getTransaction();
        
        if (tx.isActive()) {
            tx.rollback();
        }
    }
    
    /**
     * Closes this instance.
     */
    public void close() {
        if (em != null && em.isOpen()) {
            rollbackTx();
            em.close();
        }
        
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        
        removeInstance();
    }
}
