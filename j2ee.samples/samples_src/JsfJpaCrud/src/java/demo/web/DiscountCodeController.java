/*
 * DiscountCodeController.java
 *
 * Created on August 16, 2007, 10:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package demo.web;

import demo.model.Customer;
import demo.model.DiscountCode;
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
public class DiscountCodeController {
    
    /** Creates a new instance of DiscountCodeController */
    public DiscountCodeController() {
    }

    private DiscountCode discountCode;

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

    public DiscountCode getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(DiscountCode discountCode) {
        this.discountCode = discountCode;
        getCustomerController().setDetailCustomers(discountCode.getCustomerCollection());
    }

    public DataModel getDetailDiscountCodes() {
        return model;
    }

    public void setDetailDiscountCodes(Collection<DiscountCode> m) {
        model = new ListDataModel(new ArrayList(m));
    }

    public String destroyFromCustomer() {
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
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
        this.discountCode = new DiscountCode();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        EntityManager em = getEntityManager();
        if (discountCode.getCustomerCollection() == null) {
            discountCode.setCustomerCollection(new ArrayList());
        }
        discountCode.getCustomerCollection().add(em.find(Customer.class, id));
        em.close();
        return "discountCode_create";
    }

    public String createFromCustomer() {
        create();
        getCustomerController().setCustomer(discountCode.getCustomerCollection().iterator().next());
        return "customer_detail";
    }

    public String createSetup() {
        this.discountCode = new DiscountCode();
        return "discountCode_create";
    }

    public String create() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(discountCode);

            //update property customerCollection of entity Customer
            for(Customer customerCollection : discountCode.getCustomerCollection()){
                    customerCollection = em.merge(customerCollection);
                    customerCollection.setDiscountCode(discountCode);
                    customerCollection=em.merge(customerCollection);
                }

            utx.commit();
            addSuccessMessage("DiscountCode was successfully created.");
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
        return "discountCode_list";
    }

    public String detailSetup() {
        setDiscountCodeFromRequestParam();
        return "discountCode_detail";
    }

    public String editSetup() {
        setDiscountCodeFromRequestParam();
        return "discountCode_edit";
    }

    public String edit() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            discountCode = em.merge(discountCode);

            Collection<Customer> customerCollectionsOld = em.find(DiscountCode.class, discountCode.getDiscountCode()).getCustomerCollection();
            
            //update property customerCollection of entity Customer
            Collection <Customer> customerCollectionsNew = discountCode.getCustomerCollection();
            for(Customer customerCollectionNew : customerCollectionsNew) {
                    customerCollectionNew.setDiscountCode(discountCode);
                    customerCollectionNew=em.merge(customerCollectionNew);
                }
            for(Customer customerCollectionOld : customerCollectionsOld) {
                    customerCollectionOld.setDiscountCode(null);
                    customerCollectionOld=em.merge(customerCollectionOld);
                }

            utx.commit();
            addSuccessMessage("DiscountCode was successfully updated.");
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
        return "discountCode_list";
    }

    public String destroy() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            DiscountCode discountCode = getDiscountCodeFromRequestParam();
            discountCode = em.merge(discountCode);

            //update property customerCollection of entity Customer
            Collection<Customer> customerCollections = discountCode.getCustomerCollection();
            for(Customer customerCollection : customerCollections) {
                    customerCollection = em.merge(customerCollection);
                    customerCollection.setDiscountCode(null);
                    customerCollection=em.merge(customerCollection);
                }

            em.remove(discountCode);
            utx.commit();
            addSuccessMessage("DiscountCode was successfully deleted.");
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
        return "discountCode_list";
    }

    public DiscountCode getDiscountCodeFromRequestParam() {
        EntityManager em = getEntityManager();
        try{
            DiscountCode o = null;
            if (model != null) {
                o = (DiscountCode) model.getRowData();
                o = em.merge(o);
            } else {
                String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("discountCodeId");
                Integer id = new Integer(param);
                o = em.find(DiscountCode.class, id);
            }
            return o;
        } finally {
            em.close();
        }
    }

    public void setDiscountCodeFromRequestParam() {
        DiscountCode discountCode = getDiscountCodeFromRequestParam();
        setDiscountCode(discountCode);
    }

    public DataModel getDiscountCodes() {
        EntityManager em = getEntityManager();
        try{
            Query q = em.createQuery("select object(o) from DiscountCode as o");
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

    public DiscountCode findDiscountCode(String id) {
        EntityManager em = getEntityManager();
        try{
            DiscountCode o = (DiscountCode) em.find(DiscountCode.class, id);
            return o;
        } finally {
            em.close();
        }
    }

    public int getItemCount() {
        EntityManager em = getEntityManager();
        try{
            int count = ((Long) em.createQuery("select count(o) from DiscountCode as o").getSingleResult()).intValue();
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
        return "discountCode_list";
    }

    public String prev() {
        firstItem -= batchSize;
        if (firstItem < 0) {
            firstItem = 0;
        }
        return "discountCode_list";
    }
    
}
