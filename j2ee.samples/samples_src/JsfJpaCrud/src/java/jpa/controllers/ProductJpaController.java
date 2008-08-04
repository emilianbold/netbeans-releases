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
import jpa.entities.ProductCode;
import jpa.entities.PurchaseOrder;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author mbohm
 */
public class ProductJpaController {
    @Resource
    private UserTransaction utx = null;
    @PersistenceUnit(unitName = "JsfJpaCrudPU")
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Product product) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (product.getPurchaseOrderCollection() == null) {
            product.setPurchaseOrderCollection(new ArrayList<PurchaseOrder>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Manufacturer manufacturerId = product.getManufacturerId();
            if (manufacturerId != null) {
                manufacturerId = em.getReference(manufacturerId.getClass(), manufacturerId.getManufacturerId());
                product.setManufacturerId(manufacturerId);
            }
            ProductCode productCode = product.getProductCode();
            if (productCode != null) {
                productCode = em.getReference(productCode.getClass(), productCode.getProdCode());
                product.setProductCode(productCode);
            }
            List<PurchaseOrder> attachedPurchaseOrderCollection = new ArrayList<PurchaseOrder>();
            for (PurchaseOrder purchaseOrderCollectionPurchaseOrderToAttach : product.getPurchaseOrderCollection()) {
                purchaseOrderCollectionPurchaseOrderToAttach = em.getReference(purchaseOrderCollectionPurchaseOrderToAttach.getClass(), purchaseOrderCollectionPurchaseOrderToAttach.getOrderNum());
                attachedPurchaseOrderCollection.add(purchaseOrderCollectionPurchaseOrderToAttach);
            }
            product.setPurchaseOrderCollection(attachedPurchaseOrderCollection);
            em.persist(product);
            if (manufacturerId != null) {
                manufacturerId.getProductCollection().add(product);
                manufacturerId = em.merge(manufacturerId);
            }
            if (productCode != null) {
                productCode.getProductCollection().add(product);
                productCode = em.merge(productCode);
            }
            for (PurchaseOrder purchaseOrderCollectionPurchaseOrder : product.getPurchaseOrderCollection()) {
                Product oldProductIdOfPurchaseOrderCollectionPurchaseOrder = purchaseOrderCollectionPurchaseOrder.getProductId();
                purchaseOrderCollectionPurchaseOrder.setProductId(product);
                purchaseOrderCollectionPurchaseOrder = em.merge(purchaseOrderCollectionPurchaseOrder);
                if (oldProductIdOfPurchaseOrderCollectionPurchaseOrder != null) {
                    oldProductIdOfPurchaseOrderCollectionPurchaseOrder.getPurchaseOrderCollection().remove(purchaseOrderCollectionPurchaseOrder);
                    oldProductIdOfPurchaseOrderCollectionPurchaseOrder = em.merge(oldProductIdOfPurchaseOrderCollectionPurchaseOrder);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findProduct(product.getProductId()) != null) {
                throw new PreexistingEntityException("Product " + product + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Product product) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Product persistentProduct = em.find(Product.class, product.getProductId());
            Manufacturer manufacturerIdOld = persistentProduct.getManufacturerId();
            Manufacturer manufacturerIdNew = product.getManufacturerId();
            ProductCode productCodeOld = persistentProduct.getProductCode();
            ProductCode productCodeNew = product.getProductCode();
            Collection<PurchaseOrder> purchaseOrderCollectionOld = persistentProduct.getPurchaseOrderCollection();
            Collection<PurchaseOrder> purchaseOrderCollectionNew = product.getPurchaseOrderCollection();
            List<String> illegalOrphanMessages = null;
            for (PurchaseOrder purchaseOrderCollectionOldPurchaseOrder : purchaseOrderCollectionOld) {
                if (!purchaseOrderCollectionNew.contains(purchaseOrderCollectionOldPurchaseOrder)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain PurchaseOrder " + purchaseOrderCollectionOldPurchaseOrder + " since its productId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (manufacturerIdNew != null) {
                manufacturerIdNew = em.getReference(manufacturerIdNew.getClass(), manufacturerIdNew.getManufacturerId());
                product.setManufacturerId(manufacturerIdNew);
            }
            if (productCodeNew != null) {
                productCodeNew = em.getReference(productCodeNew.getClass(), productCodeNew.getProdCode());
                product.setProductCode(productCodeNew);
            }
            List<PurchaseOrder> attachedPurchaseOrderCollectionNew = new ArrayList<PurchaseOrder>();
            for (PurchaseOrder purchaseOrderCollectionNewPurchaseOrderToAttach : purchaseOrderCollectionNew) {
                purchaseOrderCollectionNewPurchaseOrderToAttach = em.getReference(purchaseOrderCollectionNewPurchaseOrderToAttach.getClass(), purchaseOrderCollectionNewPurchaseOrderToAttach.getOrderNum());
                attachedPurchaseOrderCollectionNew.add(purchaseOrderCollectionNewPurchaseOrderToAttach);
            }
            purchaseOrderCollectionNew = attachedPurchaseOrderCollectionNew;
            product.setPurchaseOrderCollection(purchaseOrderCollectionNew);
            product = em.merge(product);
            if (manufacturerIdOld != null && !manufacturerIdOld.equals(manufacturerIdNew)) {
                manufacturerIdOld.getProductCollection().remove(product);
                manufacturerIdOld = em.merge(manufacturerIdOld);
            }
            if (manufacturerIdNew != null && !manufacturerIdNew.equals(manufacturerIdOld)) {
                manufacturerIdNew.getProductCollection().add(product);
                manufacturerIdNew = em.merge(manufacturerIdNew);
            }
            if (productCodeOld != null && !productCodeOld.equals(productCodeNew)) {
                productCodeOld.getProductCollection().remove(product);
                productCodeOld = em.merge(productCodeOld);
            }
            if (productCodeNew != null && !productCodeNew.equals(productCodeOld)) {
                productCodeNew.getProductCollection().add(product);
                productCodeNew = em.merge(productCodeNew);
            }
            for (PurchaseOrder purchaseOrderCollectionNewPurchaseOrder : purchaseOrderCollectionNew) {
                if (!purchaseOrderCollectionOld.contains(purchaseOrderCollectionNewPurchaseOrder)) {
                    Product oldProductIdOfPurchaseOrderCollectionNewPurchaseOrder = purchaseOrderCollectionNewPurchaseOrder.getProductId();
                    purchaseOrderCollectionNewPurchaseOrder.setProductId(product);
                    purchaseOrderCollectionNewPurchaseOrder = em.merge(purchaseOrderCollectionNewPurchaseOrder);
                    if (oldProductIdOfPurchaseOrderCollectionNewPurchaseOrder != null && !oldProductIdOfPurchaseOrderCollectionNewPurchaseOrder.equals(product)) {
                        oldProductIdOfPurchaseOrderCollectionNewPurchaseOrder.getPurchaseOrderCollection().remove(purchaseOrderCollectionNewPurchaseOrder);
                        oldProductIdOfPurchaseOrderCollectionNewPurchaseOrder = em.merge(oldProductIdOfPurchaseOrderCollectionNewPurchaseOrder);
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
                Integer id = product.getProductId();
                if (findProduct(id) == null) {
                    throw new NonexistentEntityException("The product with id " + id + " no longer exists.");
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
            Product product;
            try {
                product = em.getReference(Product.class, id);
                product.getProductId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The product with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<PurchaseOrder> purchaseOrderCollectionOrphanCheck = product.getPurchaseOrderCollection();
            for (PurchaseOrder purchaseOrderCollectionOrphanCheckPurchaseOrder : purchaseOrderCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Product (" + product + ") cannot be destroyed since the PurchaseOrder " + purchaseOrderCollectionOrphanCheckPurchaseOrder + " in its purchaseOrderCollection field has a non-nullable productId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Manufacturer manufacturerId = product.getManufacturerId();
            if (manufacturerId != null) {
                manufacturerId.getProductCollection().remove(product);
                manufacturerId = em.merge(manufacturerId);
            }
            ProductCode productCode = product.getProductCode();
            if (productCode != null) {
                productCode.getProductCollection().remove(product);
                productCode = em.merge(productCode);
            }
            em.remove(product);
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

    public List<Product> findProductEntities() {
        return findProductEntities(true, -1, -1);
    }

    public List<Product> findProductEntities(int maxResults, int firstResult) {
        return findProductEntities(false, maxResults, firstResult);
    }

    private List<Product> findProductEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Product as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Product findProduct(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Product.class, id);
        } finally {
            em.close();
        }
    }

    public int getProductCount() {
        EntityManager em = getEntityManager();
        try {
            return ((Long) em.createQuery("select count(o) from Product as o").getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
