/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
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
