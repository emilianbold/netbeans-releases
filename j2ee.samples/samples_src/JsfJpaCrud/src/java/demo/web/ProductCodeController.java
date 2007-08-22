/*
 * ProductCodeController.java
 *
 * Created on August 16, 2007, 10:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package demo.web;

import demo.model.Product;
import demo.model.ProductCode;
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

/**
 *
 * @author martinadamek
 */
public class ProductCodeController {
    
    /** Creates a new instance of ProductCodeController */
    public ProductCodeController() {
    }

    private ProductCode productCode;

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

    public ProductCode getProductCode() {
        return productCode;
    }

    public void setProductCode(ProductCode productCode) {
        this.productCode = productCode;
        getProductController().setDetailProducts(productCode.getProductCollection());
    }

    public DataModel getDetailProductCodes() {
        return model;
    }

    public void setDetailProductCodes(Collection<ProductCode> m) {
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
        this.productCode = new ProductCode();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        EntityManager em = getEntityManager();
        if (productCode.getProductCollection() == null) {
            productCode.setProductCollection(new ArrayList());
        }
        productCode.getProductCollection().add(em.find(Product.class, id));
        em.close();
        return "productCode_create";
    }

    public String createFromProduct() {
        create();
        getProductController().setProduct(productCode.getProductCollection().iterator().next());
        return "product_detail";
    }

    public String createSetup() {
        this.productCode = new ProductCode();
        return "productCode_create";
    }

    public String create() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(productCode);

            //update property productCollection of entity Product
            for(Product productCollection : productCode.getProductCollection()){
                    productCollection = em.merge(productCollection);
                    productCollection.setProductCode(productCode);
                    productCollection=em.merge(productCollection);
                }

            utx.commit();
            addSuccessMessage("ProductCode was successfully created.");
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
        return "productCode_list";
    }

    public String detailSetup() {
        setProductCodeFromRequestParam();
        return "productCode_detail";
    }

    public String editSetup() {
        setProductCodeFromRequestParam();
        return "productCode_edit";
    }

    public String edit() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            productCode = em.merge(productCode);

            Collection<Product> productCollectionsOld = em.find(ProductCode.class, productCode.getProdCode()).getProductCollection();

            //update property productCollection of entity Product
            Collection <Product> productCollectionsNew = productCode.getProductCollection();
            for(Product productCollectionNew : productCollectionsNew) {
                    productCollectionNew.setProductCode(productCode);
                    productCollectionNew=em.merge(productCollectionNew);
                }
            for(Product productCollectionOld : productCollectionsOld) {
                    productCollectionOld.setProductCode(null);
                    productCollectionOld=em.merge(productCollectionOld);
                }

            utx.commit();
            addSuccessMessage("ProductCode was successfully updated.");
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
        return "productCode_list";
    }

    public String destroy() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            ProductCode productCode = getProductCodeFromRequestParam();
            productCode = em.merge(productCode);

            //update property productCollection of entity Product
            Collection<Product> productCollections = productCode.getProductCollection();
            for(Product productCollection : productCollections) {
                    productCollection = em.merge(productCollection);
                    productCollection.setProductCode(null);
                    productCollection=em.merge(productCollection);
                }

            em.remove(productCode);
            utx.commit();
            addSuccessMessage("ProductCode was successfully deleted.");
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
        return "productCode_list";
    }

    public ProductCode getProductCodeFromRequestParam() {
        EntityManager em = getEntityManager();
        try{
            ProductCode o = null;
            if (model != null) {
                o = (ProductCode) model.getRowData();
                o = em.merge(o);
            } else {
                String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("productCodeId");
                Integer id = new Integer(param);
                o = em.find(ProductCode.class, id);
            }
            return o;
        } finally {
            em.close();
        }
    }

    public void setProductCodeFromRequestParam() {
        ProductCode productCode = getProductCodeFromRequestParam();
        setProductCode(productCode);
    }

    public DataModel getProductCodes() {
        EntityManager em = getEntityManager();
        try{
            Query q = em.createQuery("select object(o) from ProductCode as o");
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

    public ProductCode findProductCode(String id) {
        EntityManager em = getEntityManager();
        try{
            ProductCode o = (ProductCode) em.find(ProductCode.class, id);
            return o;
        } finally {
            em.close();
        }
    }

    public int getItemCount() {
        EntityManager em = getEntityManager();
        try{
            int count = ((Long) em.createQuery("select count(o) from ProductCode as o").getSingleResult()).intValue();
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
        return "productCode_list";
    }

    public String prev() {
        firstItem -= batchSize;
        if (firstItem < 0) {
            firstItem = 0;
        }
        return "productCode_list";
    }
    
}
