/*
 * PurchaseOrderController.java
 *
 * Created on August 16, 2007, 10:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package demo.web;

import demo.model.Customer;
import demo.model.Product;
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

/**
 *
 * @author martinadamek
 */
public class PurchaseOrderController {
    
    /** Creates a new instance of PurchaseOrderController */
    public PurchaseOrderController() {
    }

    private PurchaseOrder purchaseOrder;

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

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public DataModel getDetailPurchaseOrders() {
        return model;
    }

    public void setDetailPurchaseOrders(Collection<PurchaseOrder> m) {
        model = new ListDataModel(new ArrayList(m));
    }

    public String destroyFromCustomer() {
        // TODO check
        // String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Object id = purchaseOrder.getCustomerId().getCustomerId();
        destroy();
        EntityManager em = getEntityManager();
        getCustomerController().setCustomer(em.find(Customer.class, id));
        em.close();
        return "customer_detail";
    }

    private CustomerController getCustomerController() {
        return (CustomerController) FacesContext.getCurrentInstance().
            getExternalContext().getSessionMap().get("customer");
    }

    public String createFromCustomerSetup() {
        this.purchaseOrder = new PurchaseOrder();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        EntityManager em = getEntityManager();
        purchaseOrder.setCustomerId(em.find(Customer.class, id));
        em.close();
        return "purchaseOrder_create";
    }

    public String createFromCustomer() {
        create();
        getCustomerController().setCustomer(purchaseOrder.getCustomerId());
        return "customer_detail";
    }

    public String destroyFromProduct() {
        // TODO check
        // String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Object id = purchaseOrder.getProductId().getProductId();
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
        this.purchaseOrder = new PurchaseOrder();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        EntityManager em = getEntityManager();
        purchaseOrder.setProductId(em.find(Product.class, id));
        em.close();
        return "purchaseOrder_create";
    }

    public String createFromProduct() {
        create();
        getProductController().setProduct(purchaseOrder.getProductId());
        return "product_detail";
    }

    public String createSetup() {
        this.purchaseOrder = new PurchaseOrder();
        return "purchaseOrder_create";
    }

    public String create() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(purchaseOrder);

            //update property customerId of entity Customer
            Customer customerId=purchaseOrder.getCustomerId();
            if (customerId != null) {
                customerId = em.merge(customerId);
                customerId.getPurchaseOrderCollection().add(purchaseOrder);
                customerId=em.merge(customerId);
            }
            
            //update property productId of entity Product
            Product productId=purchaseOrder.getProductId();
            if (productId != null) {
                productId = em.merge(productId);
                productId.getPurchaseOrderCollection().add(purchaseOrder);
                productId=em.merge(productId);
            }
            
            utx.commit();
            addSuccessMessage("PurchaseOrder was successfully created.");
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
        return "purchaseOrder_list";
    }

    public String detailSetup() {
        setPurchaseOrderFromRequestParam();
        return "purchaseOrder_detail";
    }

    public String editSetup() {
        setPurchaseOrderFromRequestParam();
        return "purchaseOrder_edit";
    }

    public String edit() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            purchaseOrder = em.merge(purchaseOrder);

            Customer customerIdOld = em.find(PurchaseOrder.class, purchaseOrder.getOrderNum()).getCustomerId();
            Product productIdOld = em.find(PurchaseOrder.class, purchaseOrder.getOrderNum()).getProductId();

            //update property customerId of entity Customer
            Customer customerIdNew=purchaseOrder.getCustomerId();
            if(customerIdNew != null) {
                customerIdNew.getPurchaseOrderCollection().add(purchaseOrder);
                customerIdNew=em.merge(customerIdNew);
            }
            if(customerIdOld != null) {
                customerIdOld.getPurchaseOrderCollection().remove(purchaseOrder);
                customerIdOld=em.merge(customerIdOld);
            }
            
            //update property productId of entity Product
            Product productIdNew=purchaseOrder.getProductId();
            if(productIdNew != null) {
                productIdNew.getPurchaseOrderCollection().add(purchaseOrder);
                productIdNew=em.merge(productIdNew);
            }
            if(productIdOld != null) {
                productIdOld.getPurchaseOrderCollection().remove(purchaseOrder);
                productIdOld=em.merge(productIdOld);
            }

            utx.commit();
            addSuccessMessage("PurchaseOrder was successfully updated.");
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
        return "purchaseOrder_list";
    }

    public String destroy() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            PurchaseOrder purchaseOrder = getPurchaseOrderFromRequestParam();
            purchaseOrder = em.merge(purchaseOrder);

            //update property customerId of entity Customer
            Customer customerId = purchaseOrder.getCustomerId();
            if (customerId != null) {
                customerId = em.merge(customerId);
                customerId.getPurchaseOrderCollection().remove(purchaseOrder);
                customerId=em.merge(customerId);
            }
            
            
            //update property productId of entity Product
            Product productId = purchaseOrder.getProductId();
            if (productId != null) {
                productId = em.merge(productId);
                productId.getPurchaseOrderCollection().remove(purchaseOrder);
                productId=em.merge(productId);
            }
            
            em.remove(purchaseOrder);
            utx.commit();
            addSuccessMessage("PurchaseOrder was successfully deleted.");
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
        return "purchaseOrder_list";
    }

    public PurchaseOrder getPurchaseOrderFromRequestParam() {
        EntityManager em = getEntityManager();
        try{
            PurchaseOrder o = null;
            if (model != null) {
                o = (PurchaseOrder) model.getRowData();
                o = em.merge(o);
            } else {
                String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("purchaseOrderId");
                Integer id = new Integer(param);
                o = em.find(PurchaseOrder.class, id);
            }
            return o;
        } finally {
            em.close();
        }
    }

    public void setPurchaseOrderFromRequestParam() {
        PurchaseOrder purchaseOrder = getPurchaseOrderFromRequestParam();
        setPurchaseOrder(purchaseOrder);
    }

    public DataModel getPurchaseOrders() {
        EntityManager em = getEntityManager();
        try{
            Query q = em.createQuery("select object(o) from PurchaseOrder as o");
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

    public PurchaseOrder findPurchaseOrder(Integer id) {
        EntityManager em = getEntityManager();
        try{
            PurchaseOrder o = (PurchaseOrder) em.find(PurchaseOrder.class, id);
            return o;
        } finally {
            em.close();
        }
    }

    public javax.faces.model.SelectItem[] getCustomerIds() {
        EntityManager em = getEntityManager();
        try{
            List <Customer> l = (List <Customer>) em.createQuery("select o from Customer as o").getResultList();
            SelectItem select[] = new SelectItem[l.size()];
            int i = 0;
            for(Customer x : l) {
                    select[i++] = new SelectItem(x);
                }
                return select;
        } finally {
            em.close();
        }
    }

    public javax.faces.model.SelectItem[] getProductIds() {
        EntityManager em = getEntityManager();
        try{
            List <Product> l = (List <Product>) em.createQuery("select o from Product as o").getResultList();
            SelectItem select[] = new SelectItem[l.size()];
            int i = 0;
            for(Product x : l) {
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
            int count = ((Long) em.createQuery("select count(o) from PurchaseOrder as o").getSingleResult()).intValue();
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
        return "purchaseOrder_list";
    }

    public String prev() {
        firstItem -= batchSize;
        if (firstItem < 0) {
            firstItem = 0;
        }
        return "purchaseOrder_list";
    }
    
}
