package org.demo;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

public class CustomerController {

    private Customer customer;

    @Resource
    private UserTransaction utx;

    @PersistenceUnit(unitName = "SjsasJSFTestPU")
    private EntityManagerFactory emf;

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    private int batchSize = 20;

    private int firstItem = 0;

    public String destroyFromDiscountCode() {
        Object id = customer.getDiscountCode().getDiscountCode();
        destroy();
        EntityManager em = getEntityManager();
        getDiscountCodeController().setDiscountCode(em.find(DiscountCode.class, id));
        em.close();
        return "discountCode_detail";
    }

    private DiscountCodeController getDiscountCodeController() {
        return (DiscountCodeController) FacesContext.getCurrentInstance().
            getExternalContext().getSessionMap().get("discountCode");
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

    public String createSetup() {
        this.customer = new Customer();
        return "customer_create";
    }

    public String create() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.persist(customer);
            
            //update property discountCode of entity DiscountCode
            DiscountCode discountCode=customer.getDiscountCode();
            if (discountCode != null) {
                discountCode = em.merge(discountCode);
                discountCode.getCustomer().add(customer);
                discountCode=em.merge(discountCode);
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
        }
        em.close();
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
            
            DiscountCode discountCodeOld = em.find(Customer.class, customer.getCustomerId()).getDiscountCode();
            customer = em.merge(customer);
            
            //update property discountCode of entity DiscountCode
            DiscountCode discountCodeNew=customer.getDiscountCode();
            if(discountCodeNew != null) {
                discountCodeNew.getCustomer().add(customer);
                discountCodeNew=em.merge(discountCodeNew);
            }
            if(discountCodeOld != null) {
                discountCodeOld.getCustomer().remove(customer);
                discountCodeOld=em.merge(discountCodeOld);
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
        }
        em.close();
        return "customer_list"; 
    }

    public String destroy() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            Customer customer = getCustomerFromRequestParam();
            customer = em.merge(customer);
            
            //update property discountCode of entity DiscountCode
            DiscountCode discountCode = customer.getDiscountCode();
            if (discountCode != null) {
                discountCode = em.merge(discountCode);
                discountCode.getCustomer().remove(customer);
                discountCode=em.merge(discountCode);
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
        }
        em.close();
        return "customer_list"; 
    }

    public Customer getCustomerFromRequestParam() {
        EntityManager em = getEntityManager();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("customerId");
        Integer id = new Integer(param);
        Customer o = em.find(Customer.class, id);
        em.close();
        return o;
    }

    public void setCustomerFromRequestParam() {
        Customer customer = getCustomerFromRequestParam();
        setCustomer(customer);
    }

    public Collection getCustomers() {
        EntityManager em = getEntityManager();
        Query q = em.createQuery("select object(o) from Customer as o");
        q.setMaxResults(batchSize);
        q.setFirstResult(firstItem);
        Collection c = q.getResultList();
        em.close();
        return c;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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
        Customer o = (Customer) em.find(Customer.class, id);
        em.close();
        return o;
    }

    public javax.faces.model.SelectItem[] getDiscountCodes() {
        EntityManager em = getEntityManager();
        List <DiscountCode> l = (List <DiscountCode>) em.createQuery("select o from DiscountCode as o").getResultList();
        em.close();
        SelectItem select[] = new SelectItem[l.size()];
        int i = 0;
        for(DiscountCode x : l) {
                select[i++] = new SelectItem(x);
            }
            return select;
    }

    public int getItemCount() {
        EntityManager em = getEntityManager();
        int count = ((Long) em.createQuery("select count(o) from Customer as o").getSingleResult()).intValue();em.close();
        return count;
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
