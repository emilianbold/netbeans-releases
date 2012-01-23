/*
 * Copyright (c) 2011, Oracle. All rights reserved.
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
 * * Neither the name of Oracle nor the names of its contributors
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
package embedded.telephonedirectory.server;



import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a CDI bean responsible for all database related operations.
 */
@SessionScoped
public class PersistenceManager implements Serializable {

    @Inject
    Person person;

    @PersistenceUnit(unitName = "TelephoneDirectory")
    private EntityManagerFactory emf;

    @Resource
    UserTransaction utx;

    private static Logger logger = Logger.getLogger(PersistenceManager.class.getName());

    /**
     * Stores a {@link PersonEntity} into the database.
     *
     * @return true on success, false otherwise.
     */
    public boolean persist() {
        // EntityManager em = getEntityManager();
        EntityManager em = emf.createEntityManager();
        try {
            utx.begin();
            em.persist(person.getPersonEntity());
            utx.commit();
            return true;
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception txe) {
                logger.log(Level.WARNING, txe.getMessage(), txe);
            }
            logger.log(Level.WARNING, ex.getMessage(), ex);
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all the {@link PersonEntity} entries from the database.
     *
     * @return All the {@link PersonEntity} entries available the database.
     */
    public List<PersonEntity> getAllEntries() {
        EntityManager em = emf.createEntityManager();
        List allEntries = new ArrayList();
        try {
            Query q = em.createNamedQuery("allentries");
            allEntries = q.getResultList();
        } finally {
            em.close();
        }
        return allEntries;
    }

    /**
     * Retrieves all the {@link PersonEntity} entries from the database,
     * sorted by name.
     *
     * @return All the {@link PersonEntity} entries available in the database,
     *         sorted by name.
     */
    public List<PersonEntity> getSortedEntries() {
        EntityManager em = emf.createEntityManager();
        List sortedEntries = new ArrayList();
        try {
            Query q = em.createNamedQuery("sortedentries");
            sortedEntries = q.getResultList();
        } finally {
            em.close();
        }
        return sortedEntries;
    }

    /**
     * Retrieves all the {@link PersonEntity} entries from the database
     * which match the given location.
     *
     * @param location Location as search key
     * @return All the {@link PersonEntity} entries from the database,
     *         which match the given location.
     */
    public List<PersonEntity> getByLocation(String location) {
        EntityManager em = emf.createEntityManager();
        List seachedEntries = new ArrayList();
        try {
            Query q = em.createNamedQuery("searchbyloc");
            location = location.toUpperCase();
            q.setParameter("someplace", location);
            seachedEntries = q.getResultList();
        } finally {
            em.close();
        }
        return seachedEntries;
    }

    /**
     * Retrieves all the {@link PersonEntity} entries from the database
     * which match the given name.
     *
     * @param name Name as search key
     * @return All the {@link PersonEntity} entries from the database,
     *         which match the given name.
     */

    public List<PersonEntity> getByName(String name) {
        EntityManager em = emf.createEntityManager();
        List seachedEntries = new ArrayList();
        try {
            name = name.toUpperCase();
            Query q = em.createNamedQuery("searchbyname");
            q.setParameter("somename", name);
            seachedEntries = q.getResultList();
        } finally {
            em.close();
        }
        return (seachedEntries);
    }
}
