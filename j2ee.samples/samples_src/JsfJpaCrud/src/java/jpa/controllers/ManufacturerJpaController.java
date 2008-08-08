/*
 * Copyright (c) 2008, Sun Microsystems, Inc. All rights reserved.
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

package jpa.controllers;

import java.util.List;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.transaction.UserTransaction;
import jpa.controllers.exceptions.IllegalOrphanException;
import jpa.controllers.exceptions.NonexistentEntityException;
import jpa.controllers.exceptions.PreexistingEntityException;
import jpa.controllers.exceptions.RollbackFailureException;
import jpa.entities.Manufacturer;
import jpa.entities.Product;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author mbohm
 */
public class ManufacturerJpaController {
    @Resource
    private UserTransaction utx = null;
    @PersistenceUnit(unitName = "JsfJpaCrudPU")
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Manufacturer manufacturer) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (manufacturer.getProductCollection() == null) {
            manufacturer.setProductCollection(new ArrayList<Product>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            List<Product> attachedProductCollection = new ArrayList<Product>();
            for (Product productCollectionProductToAttach : manufacturer.getProductCollection()) {
                productCollectionProductToAttach = em.getReference(productCollectionProductToAttach.getClass(), productCollectionProductToAttach.getProductId());
                attachedProductCollection.add(productCollectionProductToAttach);
            }
            manufacturer.setProductCollection(attachedProductCollection);
            em.persist(manufacturer);
            for (Product productCollectionProduct : manufacturer.getProductCollection()) {
                Manufacturer oldManufacturerIdOfProductCollectionProduct = productCollectionProduct.getManufacturerId();
                productCollectionProduct.setManufacturerId(manufacturer);
                productCollectionProduct = em.merge(productCollectionProduct);
                if (oldManufacturerIdOfProductCollectionProduct != null) {
                    oldManufacturerIdOfProductCollectionProduct.getProductCollection().remove(productCollectionProduct);
                    oldManufacturerIdOfProductCollectionProduct = em.merge(oldManufacturerIdOfProductCollectionProduct);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findManufacturer(manufacturer.getManufacturerId()) != null) {
                throw new PreexistingEntityException("Manufacturer " + manufacturer + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Manufacturer manufacturer) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Manufacturer persistentManufacturer = em.find(Manufacturer.class, manufacturer.getManufacturerId());
            Collection<Product> productCollectionOld = persistentManufacturer.getProductCollection();
            Collection<Product> productCollectionNew = manufacturer.getProductCollection();
            List<String> illegalOrphanMessages = null;
            for (Product productCollectionOldProduct : productCollectionOld) {
                if (!productCollectionNew.contains(productCollectionOldProduct)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Product " + productCollectionOldProduct + " since its manufacturerId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Product> attachedProductCollectionNew = new ArrayList<Product>();
            for (Product productCollectionNewProductToAttach : productCollectionNew) {
                productCollectionNewProductToAttach = em.getReference(productCollectionNewProductToAttach.getClass(), productCollectionNewProductToAttach.getProductId());
                attachedProductCollectionNew.add(productCollectionNewProductToAttach);
            }
            productCollectionNew = attachedProductCollectionNew;
            manufacturer.setProductCollection(productCollectionNew);
            manufacturer = em.merge(manufacturer);
            for (Product productCollectionNewProduct : productCollectionNew) {
                if (!productCollectionOld.contains(productCollectionNewProduct)) {
                    Manufacturer oldManufacturerIdOfProductCollectionNewProduct = productCollectionNewProduct.getManufacturerId();
                    productCollectionNewProduct.setManufacturerId(manufacturer);
                    productCollectionNewProduct = em.merge(productCollectionNewProduct);
                    if (oldManufacturerIdOfProductCollectionNewProduct != null && !oldManufacturerIdOfProductCollectionNewProduct.equals(manufacturer)) {
                        oldManufacturerIdOfProductCollectionNewProduct.getProductCollection().remove(productCollectionNewProduct);
                        oldManufacturerIdOfProductCollectionNewProduct = em.merge(oldManufacturerIdOfProductCollectionNewProduct);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = manufacturer.getManufacturerId();
                if (findManufacturer(id) == null) {
                    throw new NonexistentEntityException("The manufacturer with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Manufacturer manufacturer;
            try {
                manufacturer = em.getReference(Manufacturer.class, id);
                manufacturer.getManufacturerId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The manufacturer with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Product> productCollectionOrphanCheck = manufacturer.getProductCollection();
            for (Product productCollectionOrphanCheckProduct : productCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Manufacturer (" + manufacturer + ") cannot be destroyed since the Product " + productCollectionOrphanCheckProduct + " in its productCollection field has a non-nullable manufacturerId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(manufacturer);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Manufacturer> findManufacturerEntities() {
        return findManufacturerEntities(true, -1, -1);
    }

    public List<Manufacturer> findManufacturerEntities(int maxResults, int firstResult) {
        return findManufacturerEntities(false, maxResults, firstResult);
    }

    private List<Manufacturer> findManufacturerEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Manufacturer as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Manufacturer findManufacturer(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Manufacturer.class, id);
        } finally {
            em.close();
        }
    }

    public int getManufacturerCount() {
        EntityManager em = getEntityManager();
        try {
            return ((Long) em.createQuery("select count(o) from Manufacturer as o").getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
