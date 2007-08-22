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

package demo.web;

import demo.model.Manufacturer;
import demo.model.Product;
import demo.model.ProductCode;
import demo.model.PurchaseOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

public class ProductController {
    
    /** Creates a new instance of ProductController */
    public ProductController() {
    }

    private Product product;

    private DataModel model;

    @Resource
    private UserTransaction utx;

    @PersistenceUnit(unitName = "JsfJpaCrudPU")
    private EntityManagerFactory emf;

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    private int batchSize = 20;

    private int firstItem = 0;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        getPurchaseOrderController().setDetailPurchaseOrders(product.getPurchaseOrderCollection());
    }

    public DataModel getDetailProducts() {
        return model;
    }

    public void setDetailProducts(Collection<Product> m) {
        model = new ListDataModel(new ArrayList(m));
    }

    public String destroyFromManufacturer() {
        // TODO check
        // String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Object id = product.getManufacturerId().getManufacturerId();
        destroy();
        EntityManager em = getEntityManager();
        getManufacturerController().setManufacturer(em.find(Manufacturer.class, id));
        em.close();
        return "manufacturer_detail";
    }

    private ManufacturerController getManufacturerController() {
        return (ManufacturerController) FacesContext.getCurrentInstance().
            getExternalContext().getSessionMap().get("manufacturer");
    }

    public String createFromManufacturerSetup() {
        this.product = new Product();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        EntityManager em = getEntityManager();
        product.setManufacturerId(em.find(Manufacturer.class, id));
        em.close();
        return "product_create";
    }

    public String createFromManufacturer() {
        create();
        getManufacturerController().setManufacturer(product.getManufacturerId());
        return "manufacturer_detail";
    }

    public String destroyFromProductCode() {
        // TODO check
        // String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Object id = product.getProductCode().getProdCode();
        destroy();
        EntityManager em = getEntityManager();
        getProductCodeController().setProductCode(em.find(ProductCode.class, id));
        em.close();
        return "productCode_detail";
    }

    private ProductCodeController getProductCodeController() {
        return (ProductCodeController) FacesContext.getCurrentInstance().
            getExternalContext().getSessionMap().get("productCode");
    }

    public String createFromProductCodeSetup() {
        this.product = new Product();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        EntityManager em = getEntityManager();
        product.setProductCode(em.find(ProductCode.class, id));
        em.close();
        return "product_create";
    }

    public String createFromProductCode() {
        create();
        getProductCodeController().setProductCode(product.getProductCode());
        return "productCode_detail";
    }

    public String destroyFromPurchaseOrder() {
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        destroy();
        EntityManager em = getEntityManager();
        getPurchaseOrderController().setPurchaseOrder(em.find(PurchaseOrder.class, id));
        em.close();
        return "purchaseOrder_detail";
    }

    private PurchaseOrderController getPurchaseOrderController() {
        return (PurchaseOrderController) FacesContext.getCurrentInstance().
            getExternalContext().getSessionMap().get("purchaseOrder");
    }

    public String createFromPurchaseOrderSetup() {
        this.product = new Product();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        EntityManager em = getEntityManager();
        if (product.getPurchaseOrderCollection() == null) {
            product.setPurchaseOrderCollection(new ArrayList());
        }
        product.getPurchaseOrderCollection().add(em.find(PurchaseOrder.class, id));
        em.close();
        return "product_create";
    }

    public String createFromPurchaseOrder() {
        create();
        getPurchaseOrderController().setPurchaseOrder(product.getPurchaseOrderCollection().iterator().next());
        return "purchaseOrder_detail";
    }

    public String createSetup() {
        this.product = new Product();
        return "product_create";
    }

    public String create() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(product);

            //update property manufacturerId of entity Manufacturer
            Manufacturer manufacturerId=product.getManufacturerId();
            if (manufacturerId != null) {
                manufacturerId = em.merge(manufacturerId);
                manufacturerId.getProductCollection().add(product);
                manufacturerId=em.merge(manufacturerId);
            }
            
            
            //update property productCode of entity ProductCode
            ProductCode productCode=product.getProductCode();
            if (productCode != null) {
                productCode = em.merge(productCode);
                productCode.getProductCollection().add(product);
                productCode=em.merge(productCode);
            }
            
            //update property purchaseOrderCollection of entity PurchaseOrder
            for(PurchaseOrder purchaseOrderCollection : product.getPurchaseOrderCollection()){
                    purchaseOrderCollection = em.merge(purchaseOrderCollection);
                    purchaseOrderCollection.setProductId(product);
                    purchaseOrderCollection=em.merge(purchaseOrderCollection);
                }

            utx.commit();
            addSuccessMessage("Product was successfully created.");
        } catch (Exception ex) {
            try {
                addErrorMessage(ex.getLocalizedMessage());
                utx.rollback();
            } catch (Exception e) {
                addErrorMessage(e.getLocalizedMessage());
            }
        } finally {
            em.close();
        }
        return "product_list";
    }

    public String detailSetup() {
        setProductFromRequestParam();
        return "product_detail";
    }

    public String editSetup() {
        setProductFromRequestParam();
        return "product_edit";
    }

    public String edit() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            product = em.merge(product);

            Manufacturer manufacturerIdOld = em.find(Product.class, product.getProductId()).getManufacturerId();
            ProductCode productCodeOld = em.find(Product.class, product.getProductId()).getProductCode();
            Collection<PurchaseOrder> purchaseOrderCollectionsOld = em.find(Product.class, product.getProductId()).getPurchaseOrderCollection();

            //update property manufacturerId of entity Manufacturer
            Manufacturer manufacturerIdNew=product.getManufacturerId();
            if(manufacturerIdNew != null) {
                manufacturerIdNew.getProductCollection().add(product);
                manufacturerIdNew=em.merge(manufacturerIdNew);
            }
            if(manufacturerIdOld != null) {
                manufacturerIdOld.getProductCollection().remove(product);
                manufacturerIdOld=em.merge(manufacturerIdOld);
            }
            
            //update property productCode of entity ProductCode
            ProductCode productCodeNew=product.getProductCode();
            if(productCodeNew != null) {
                productCodeNew.getProductCollection().add(product);
                productCodeNew=em.merge(productCodeNew);
            }
            if(productCodeOld != null) {
                productCodeOld.getProductCollection().remove(product);
                productCodeOld=em.merge(productCodeOld);
            }
            
            //update property purchaseOrderCollection of entity PurchaseOrder
            Collection <PurchaseOrder> purchaseOrderCollectionsNew = product.getPurchaseOrderCollection();
            for(PurchaseOrder purchaseOrderCollectionNew : purchaseOrderCollectionsNew) {
                    purchaseOrderCollectionNew.setProductId(product);
                    purchaseOrderCollectionNew=em.merge(purchaseOrderCollectionNew);
                }
            for(PurchaseOrder purchaseOrderCollectionOld : purchaseOrderCollectionsOld) {
                    purchaseOrderCollectionOld.setProductId(null);
                    purchaseOrderCollectionOld=em.merge(purchaseOrderCollectionOld);
                }

            utx.commit();
            addSuccessMessage("Product was successfully updated.");
        } catch (Exception ex) {
            try {
                addErrorMessage(ex.getLocalizedMessage());
                utx.rollback();
            } catch (Exception e) {
                addErrorMessage(e.getLocalizedMessage());
            }
        } finally {
            em.close();
        }
        return "product_list";
    }

    public String destroy() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            Product product = getProductFromRequestParam();
            product = em.merge(product);

            //update property manufacturerId of entity Manufacturer
            Manufacturer manufacturerId = product.getManufacturerId();
            if (manufacturerId != null) {
                manufacturerId = em.merge(manufacturerId);
                manufacturerId.getProductCollection().remove(product);
                manufacturerId=em.merge(manufacturerId);
            }
            
            //update property productCode of entity ProductCode
            ProductCode productCode = product.getProductCode();
            if (productCode != null) {
                productCode = em.merge(productCode);
                productCode.getProductCollection().remove(product);
                productCode=em.merge(productCode);
            }
            
            //update property purchaseOrderCollection of entity PurchaseOrder
            Collection<PurchaseOrder> purchaseOrderCollections = product.getPurchaseOrderCollection();
            for(PurchaseOrder purchaseOrderCollection : purchaseOrderCollections) {
                    purchaseOrderCollection = em.merge(purchaseOrderCollection);
                    purchaseOrderCollection.setProductId(null);
                    purchaseOrderCollection=em.merge(purchaseOrderCollection);
                }

            em.remove(product);
            utx.commit();
            addSuccessMessage("Product was successfully deleted.");
        } catch (Exception ex) {
            try {
                addErrorMessage(ex.getLocalizedMessage());
                utx.rollback();
            } catch (Exception e) {
                addErrorMessage(e.getLocalizedMessage());
            }
        } finally {
            em.close();
        }
        return "product_list";
    }

    public Product getProductFromRequestParam() {
        EntityManager em = getEntityManager();
        try{
            Product o = null;
            if (model != null) {
                o = (Product) model.getRowData();
                o = em.merge(o);
            } else {
                String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("productId");
                Integer id = new Integer(param);
                o = em.find(Product.class, id);
            }
            return o;
        } finally {
            em.close();
        }
    }

    public void setProductFromRequestParam() {
        Product product = getProductFromRequestParam();
        setProduct(product);
    }

    public DataModel getProducts() {
        EntityManager em = getEntityManager();
        try{
            Query q = em.createQuery("select object(o) from Product as o");
            q.setMaxResults(batchSize);
            q.setFirstResult(firstItem);
            model = new ListDataModel(q.getResultList());
            return model;
        } finally {
            em.close();
        }
    }

    public static void addErrorMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
        FacesContext fc = FacesContext.getCurrentInstance();
        fc.addMessage(null, facesMsg);
    }

    public static void addSuccessMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
        FacesContext fc = FacesContext.getCurrentInstance();
        fc.addMessage("successInfo", facesMsg);
    }

    public Product findProduct(Integer id) {
        EntityManager em = getEntityManager();
        try{
            Product o = (Product) em.find(Product.class, id);
            return o;
        } finally {
            em.close();
        }
    }

    public javax.faces.model.SelectItem[] getManufacturerIds() {
        EntityManager em = getEntityManager();
        try{
            List <Manufacturer> l = (List <Manufacturer>) em.createQuery("select o from Manufacturer as o").getResultList();
            SelectItem select[] = new SelectItem[l.size()];
            int i = 0;
            for(Manufacturer x : l) {
                    select[i++] = new SelectItem(x);
                }
                return select;
        } finally {
            em.close();
        }
    }

    public javax.faces.model.SelectItem[] getProductCodes() {
        EntityManager em = getEntityManager();
        try{
            List <ProductCode> l = (List <ProductCode>) em.createQuery("select o from ProductCode as o").getResultList();
            SelectItem select[] = new SelectItem[l.size()];
            int i = 0;
            for(ProductCode x : l) {
                    select[i++] = new SelectItem(x);
                }
                return select;
        } finally {
            em.close();
        }
    }

    public int getItemCount() {
        EntityManager em = getEntityManager();
        try{
            int count = ((Long) em.createQuery("select count(o) from Product as o").getSingleResult()).intValue();
            return count;
        } finally {
            em.close();
        }
    }

    public int getFirstItem() {
        return firstItem;
    }

    public int getLastItem() {
        int size = getItemCount();
        return firstItem + batchSize > size ? size : firstItem + batchSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public String next() {
        if (firstItem + batchSize < getItemCount()) {
            firstItem += batchSize;
        }
        return "product_list";
    }

    public String prev() {
        firstItem -= batchSize;
        if (firstItem < 0) {
            firstItem = 0;
        }
        return "product_list";
    }
    
}
