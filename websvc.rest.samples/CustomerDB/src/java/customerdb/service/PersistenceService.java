/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
