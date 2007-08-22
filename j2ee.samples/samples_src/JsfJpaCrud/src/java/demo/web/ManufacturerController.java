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
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

public class ManufacturerController {
    
    /** Creates a new instance of ManufacturerController */
    public ManufacturerController() {
    }

    private Manufacturer manufacturer;

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

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
        getProductController().setDetailProducts(manufacturer.getProductCollection());
    }

    public DataModel getDetailManufacturers() {
        return model;
    }

    public void setDetailManufacturers(Collection<Manufacturer> m) {
        model = new ListDataModel(new ArrayList(m));
    }

    public String destroyFromProduct() {
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        destroy();
        EntityManager em = getEntityManager();
        getProductController().setProduct(em.find(Product.class, id));
        em.close();
        return "product_detail";
    }

    private ProductController getProductController() {
        return (ProductController) FacesContext.getCurrentInstance().
            getExternalContext().getSessionMap().get("product");
    }

    public String createFromProductSetup() {
        this.manufacturer = new Manufacturer();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        EntityManager em = getEntityManager();
        if (manufacturer.getProductCollection() == null) {
            manufacturer.setProductCollection(new ArrayList());
        }
        manufacturer.getProductCollection().add(em.find(Product.class, id));
        em.close();
        return "manufacturer_create";
    }

    public String createFromProduct() {
        create();
        getProductController().setProduct(manufacturer.getProductCollection().iterator().next());
        return "product_detail";
    }

    public String createSetup() {
        this.manufacturer = new Manufacturer();
        return "manufacturer_create";
    }

    public String create() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(manufacturer);

            //update property productCollection of entity Product
            for(Product productCollection : manufacturer.getProductCollection()){
                    productCollection = em.merge(productCollection);
                    productCollection.setManufacturerId(manufacturer);
                    productCollection=em.merge(productCollection);
                }

            utx.commit();
            addSuccessMessage("Manufacturer was successfully created.");
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
        return "manufacturer_list";
    }

    public String detailSetup() {
        setManufacturerFromRequestParam();
        return "manufacturer_detail";
    }

    public String editSetup() {
        setManufacturerFromRequestParam();
        return "manufacturer_edit";
    }

    public String edit() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            manufacturer = em.merge(manufacturer);

            Collection<Product> productCollectionsOld = em.find(Manufacturer.class, manufacturer.getManufacturerId()).getProductCollection();

            //update property productCollection of entity Product
            Collection <Product> productCollectionsNew = manufacturer.getProductCollection();
            for(Product productCollectionNew : productCollectionsNew) {
                    productCollectionNew.setManufacturerId(manufacturer);
                    productCollectionNew=em.merge(productCollectionNew);
                }
            for(Product productCollectionOld : productCollectionsOld) {
                    productCollectionOld.setManufacturerId(null);
                    productCollectionOld=em.merge(productCollectionOld);
                }

            utx.commit();
            addSuccessMessage("Manufacturer was successfully updated.");
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
        return "manufacturer_list";
    }

    public String destroy() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            Manufacturer manufacturer = getManufacturerFromRequestParam();
            manufacturer = em.merge(manufacturer);

            //update property productCollection of entity Product
            Collection<Product> productCollections = manufacturer.getProductCollection();
            for(Product productCollection : productCollections) {
                    productCollection = em.merge(productCollection);
                    productCollection.setManufacturerId(null);
                    productCollection=em.merge(productCollection);
                }

            em.remove(manufacturer);
            utx.commit();
            addSuccessMessage("Manufacturer was successfully deleted.");
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
        return "manufacturer_list";
    }

    public Manufacturer getManufacturerFromRequestParam() {
        EntityManager em = getEntityManager();
        try{
            Manufacturer o = null;
            if (model != null) {
                o = (Manufacturer) model.getRowData();
                o = em.merge(o);
            } else {
                String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("manufacturerId");
                Integer id = new Integer(param);
                o = em.find(Manufacturer.class, id);
            }
            return o;
        } finally {
            em.close();
        }
    }

    public void setManufacturerFromRequestParam() {
        Manufacturer manufacturer = getManufacturerFromRequestParam();
        setManufacturer(manufacturer);
    }

    public DataModel getManufacturers() {
        EntityManager em = getEntityManager();
        try{
            Query q = em.createQuery("select object(o) from Manufacturer as o");
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

    public Manufacturer findManufacturer(Integer id) {
        EntityManager em = getEntityManager();
        try{
            Manufacturer o = (Manufacturer) em.find(Manufacturer.class, id);
            return o;
        } finally {
            em.close();
        }
    }

    public int getItemCount() {
        EntityManager em = getEntityManager();
        try{
            int count = ((Long) em.createQuery("select count(o) from Manufacturer as o").getSingleResult()).intValue();
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
        return "manufacturer_list";
    }

    public String prev() {
        firstItem -= batchSize;
        if (firstItem < 0) {
            firstItem = 0;
        }
        return "manufacturer_list";
    }
    
}
