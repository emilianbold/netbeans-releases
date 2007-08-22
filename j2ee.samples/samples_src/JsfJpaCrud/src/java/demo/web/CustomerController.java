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

import demo.model.Customer;
import demo.model.DiscountCode;
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

public class CustomerController {
    
    /** Creates a new instance of CustomerController */
    public CustomerController() {
    }

    private Customer customer;

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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        getPurchaseOrderController().setDetailPurchaseOrders(customer.getPurchaseOrderCollection());
    }

    public DataModel getDetailCustomers() {
        return model;
    }

    public void setDetailCustomers(Collection<Customer> m) {
        model = new ListDataModel(new ArrayList(m));
    }

    public String destroyFromDiscountCode() {
        // TODO check
        // String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Object id = customer.getDiscountCode().getDiscountCode();
        destroy();
        EntityManager em = getEntityManager();
        getDiscountCodeController().setDiscountCode(em.find(DiscountCode.class, id));
        em.close();
        return "discountCode_detail";
    }

    private DiscountCodeController getDiscountCodeController() {
        FacesContext context = FacesContext.getCurrentInstance();
        return (DiscountCodeController) context.getApplication().getELResolver().getValue(context.getELContext(), null, "discountCode");
    }

    public String createFromDiscountCodeSetup() {
        this.customer = new Customer();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        EntityManager em = getEntityManager();
        customer.setDiscountCode(em.find(DiscountCode.class, id));
        em.close();
        return "customer_create";
    }

    public String createFromDiscountCode() {
        create();
        getDiscountCodeController().setDiscountCode(customer.getDiscountCode());
        return "discountCode_detail";
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
        FacesContext context = FacesContext.getCurrentInstance();
        return (PurchaseOrderController) context.getApplication().getELResolver().getValue(context.getELContext(), null, "purchaseOrder");
    }

    public String createFromPurchaseOrderSetup() {
        this.customer = new Customer();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        EntityManager em = getEntityManager();
        if (customer.getPurchaseOrderCollection() == null) {
            customer.setPurchaseOrderCollection(new ArrayList());
        }
        customer.getPurchaseOrderCollection().add(em.find(PurchaseOrder.class, id));
        em.close();
        return "customer_create";
    }

    public String createFromPurchaseOrder() {
        create();
        getPurchaseOrderController().setPurchaseOrder(customer.getPurchaseOrderCollection().iterator().next());
        return "purchaseOrder_detail";
    }

    public String createSetup() {
        this.customer = new Customer();
        return "customer_create";
    }

    public String create() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(customer);

            //update property discountCode of entity DiscountCode
            DiscountCode discountCode=customer.getDiscountCode();
            if (discountCode != null) {
                discountCode = em.merge(discountCode);
                discountCode.getCustomerCollection().add(customer);
                discountCode=em.merge(discountCode);
            }

            //update property purchaseOrderCollection of entity PurchaseOrder
            for(PurchaseOrder purchaseOrderCollection : customer.getPurchaseOrderCollection()){
                    purchaseOrderCollection = em.merge(purchaseOrderCollection);
                    purchaseOrderCollection.setCustomerId(customer);
                    purchaseOrderCollection=em.merge(purchaseOrderCollection);
                }
                
            utx.commit();
            addSuccessMessage("Customer was successfully created.");
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
        return "customer_list";
    }

    public String detailSetup() {
        setCustomerFromRequestParam();
        return "customer_detail";
    }

    public String editSetup() {
        setCustomerFromRequestParam();
        return "customer_edit";
    }

    public String edit() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            customer = em.merge(customer);

            DiscountCode discountCodeOld = em.find(Customer.class, customer.getCustomerId()).getDiscountCode();
            Collection<PurchaseOrder> purchaseOrderCollectionsOld = em.find(Customer.class, customer.getCustomerId()).getPurchaseOrderCollection();

            //update property discountCode of entity DiscountCode
            DiscountCode discountCodeNew=customer.getDiscountCode();
            if(discountCodeNew != null) {
                discountCodeNew.getCustomerCollection().add(customer);
                discountCodeNew=em.merge(discountCodeNew);
            }
            if(discountCodeOld != null) {
                discountCodeOld.getCustomerCollection().remove(customer);
                discountCodeOld=em.merge(discountCodeOld);
            }

            //update property purchaseOrderCollection of entity PurchaseOrder
            Collection <PurchaseOrder> purchaseOrderCollectionsNew = customer.getPurchaseOrderCollection();
            for(PurchaseOrder purchaseOrderCollectionNew : purchaseOrderCollectionsNew) {
                    purchaseOrderCollectionNew.setCustomerId(customer);
                    purchaseOrderCollectionNew=em.merge(purchaseOrderCollectionNew);
                }
            for(PurchaseOrder purchaseOrderCollectionOld : purchaseOrderCollectionsOld) {
                    purchaseOrderCollectionOld.setCustomerId(null);
                    purchaseOrderCollectionOld=em.merge(purchaseOrderCollectionOld);
                }

            utx.commit();
            addSuccessMessage("Customer was successfully updated.");
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
        return "customer_list";
    }

    public String destroy() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            Customer customer = getCustomerFromRequestParam();
            customer = em.merge(customer);

            // TODO: merge
            //update property discountCode of entity DiscountCode
            DiscountCode discountCode = customer.getDiscountCode();
            if (discountCode != null) {
                discountCode = em.merge(discountCode);
                discountCode.getCustomerCollection().remove(customer);
                discountCode=em.merge(discountCode);
            }

            //update property purchaseOrderCollection of entity PurchaseOrder
            Collection<PurchaseOrder> purchaseOrderCollections = customer.getPurchaseOrderCollection();
            for(PurchaseOrder purchaseOrderCollection : purchaseOrderCollections) {
                    purchaseOrderCollection = em.merge(purchaseOrderCollection);
                    purchaseOrderCollection.setCustomerId(null);
                    purchaseOrderCollection=em.merge(purchaseOrderCollection);
                }

            em.remove(customer);
            utx.commit();
            addSuccessMessage("Customer was successfully deleted.");
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
        return "customer_list";
    }

    public Customer getCustomerFromRequestParam() {
        EntityManager em = getEntityManager();
        try{
            Customer o = null;
            if (model != null) {
                o = (Customer) model.getRowData();
                o = em.merge(o);
            } else {
                String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("customerId");
                Integer id = new Integer(param);
                o = em.find(Customer.class, id);
            }
            return o;
        } finally {
            em.close();
        }
    }

    public void setCustomerFromRequestParam() {
        Customer customer = getCustomerFromRequestParam();
        setCustomer(customer);
    }

    public DataModel getCustomers() {
        EntityManager em = getEntityManager();
        try{
            Query q = em.createQuery("select object(o) from Customer as o");
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

    public Customer findCustomer(Integer id) {
        EntityManager em = getEntityManager();
        try{
            Customer o = (Customer) em.find(Customer.class, id);
            return o;
        } finally {
            em.close();
        }
    }

    public javax.faces.model.SelectItem[] getDiscountCodes() {
        EntityManager em = getEntityManager();
        try{
            List <DiscountCode> l = (List <DiscountCode>) em.createQuery("select o from DiscountCode as o").getResultList();
            SelectItem select[] = new SelectItem[l.size()];
            int i = 0;
            for(DiscountCode x : l) {
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
            int count = ((Long) em.createQuery("select count(o) from Customer as o").getSingleResult()).intValue();
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
        return "customer_list";
    }

    public String prev() {
        firstItem -= batchSize;
        if (firstItem < 0) {
            firstItem = 0;
        }
        return "customer_list";
    }
    
}
