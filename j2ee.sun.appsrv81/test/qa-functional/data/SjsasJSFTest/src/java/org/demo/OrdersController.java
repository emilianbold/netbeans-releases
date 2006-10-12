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

public class OrdersController {

    private Orders orders;

    @Resource
    private UserTransaction utx;

    @PersistenceUnit(unitName = "SjsasJSFTestPU")
    private EntityManagerFactory emf;

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    private int batchSize = 20;

    private int firstItem = 0;

    public String destroyFromCustomer() {
        Object id = orders.getCustomerId().getCustomerId();
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
        this.orders = new Orders();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        EntityManager em = getEntityManager();
        orders.setCustomerId(em.find(Customer.class, id));
        em.close();
        return "orders_create";
    }

    public String createFromCustomer() {
        create();
        getCustomerController().setCustomer(orders.getCustomerId());
        return "customer_detail";
    }

    public String destroyFromProduct() {
        Object id = orders.getProductId().getProductId();
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
        this.orders = new Orders();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        EntityManager em = getEntityManager();
        orders.setProductId(em.find(Product.class, id));
        em.close();
        return "orders_create";
    }

    public String createFromProduct() {
        create();
        getProductController().setProduct(orders.getProductId());
        return "product_detail";
    }

    public String createSetup() {
        this.orders = new Orders();
        return "orders_create";
    }

    public String create() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.persist(orders);
            
            //update property customerId of entity Customer
            Customer customerId=orders.getCustomerId();
            if (customerId != null) {
                customerId = em.merge(customerId);
                customerId.getOrders().add(orders);
                customerId=em.merge(customerId);
            }
            
            
            //update property productId of entity Product
            Product productId=orders.getProductId();
            if (productId != null) {
                productId = em.merge(productId);
                productId.getOrders().add(orders);
                productId=em.merge(productId);
            }
            
            utx.commit();
            addSuccessMessage("Orders was successfully created.");
        } catch (Exception ex) {
            try {
                addErrorMessage(ex.getLocalizedMessage());
                utx.rollback();
            } catch (Exception e) {
                addErrorMessage(e.getLocalizedMessage());
            }
        }
        em.close();
        return "orders_list"; 
    }

    public String detailSetup() {
        setOrdersFromRequestParam();
        return "orders_detail";
    }

    public String editSetup() {
        setOrdersFromRequestParam();
        return "orders_edit";
    }

    public String edit() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            
            Customer customerIdOld = em.find(Orders.class, orders.getOrderNum()).getCustomerId();
            
            Product productIdOld = em.find(Orders.class, orders.getOrderNum()).getProductId();
            orders = em.merge(orders);
            
            //update property customerId of entity Customer
            Customer customerIdNew=orders.getCustomerId();
            if(customerIdNew != null) {
                customerIdNew.getOrders().add(orders);
                customerIdNew=em.merge(customerIdNew);
            }
            if(customerIdOld != null) {
                customerIdOld.getOrders().remove(orders);
                customerIdOld=em.merge(customerIdOld);
            }
            
            //update property productId of entity Product
            Product productIdNew=orders.getProductId();
            if(productIdNew != null) {
                productIdNew.getOrders().add(orders);
                productIdNew=em.merge(productIdNew);
            }
            if(productIdOld != null) {
                productIdOld.getOrders().remove(orders);
                productIdOld=em.merge(productIdOld);
            }
            utx.commit();
            addSuccessMessage("Orders was successfully updated.");
        } catch (Exception ex) {
            try {
                addErrorMessage(ex.getLocalizedMessage());
                utx.rollback();
            } catch (Exception e) {
                addErrorMessage(e.getLocalizedMessage());
            }
        }
        em.close();
        return "orders_list"; 
    }

    public String destroy() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            Orders orders = getOrdersFromRequestParam();
            orders = em.merge(orders);
            
            //update property customerId of entity Customer
            Customer customerId = orders.getCustomerId();
            if (customerId != null) {
                customerId = em.merge(customerId);
                customerId.getOrders().remove(orders);
                customerId=em.merge(customerId);
            }
            
            
            //update property productId of entity Product
            Product productId = orders.getProductId();
            if (productId != null) {
                productId = em.merge(productId);
                productId.getOrders().remove(orders);
                productId=em.merge(productId);
            }
            
            em.remove(orders);
            utx.commit();
            addSuccessMessage("Orders was successfully deleted.");
        } catch (Exception ex) {
            try {
                addErrorMessage(ex.getLocalizedMessage());
                utx.rollback();
            } catch (Exception e) {
                addErrorMessage(e.getLocalizedMessage());
            }
        }
        em.close();
        return "orders_list"; 
    }

    public Orders getOrdersFromRequestParam() {
        EntityManager em = getEntityManager();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("orderNum");
        Integer id = new Integer(param);
        Orders o = em.find(Orders.class, id);
        em.close();
        return o;
    }

    public void setOrdersFromRequestParam() {
        Orders orders = getOrdersFromRequestParam();
        setOrders(orders);
    }

    public Collection getOrderss() {
        EntityManager em = getEntityManager();
        Query q = em.createQuery("select object(o) from Orders as o");
        q.setMaxResults(batchSize);
        q.setFirstResult(firstItem);
        Collection c = q.getResultList();
        em.close();
        return c;
    }

    public Orders getOrders() {
        return orders;
    }

    public void setOrders(Orders orders) {
        this.orders = orders;
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

    public Orders findOrders(Integer id) {
        EntityManager em = getEntityManager();
        Orders o = (Orders) em.find(Orders.class, id);
        em.close();
        return o;
    }

    public javax.faces.model.SelectItem[] getCustomerIds() {
        EntityManager em = getEntityManager();
        List <Customer> l = (List <Customer>) em.createQuery("select o from Customer as o").getResultList();
        em.close();
        SelectItem select[] = new SelectItem[l.size()];
        int i = 0;
        for(Customer x : l) {
                select[i++] = new SelectItem(x);
            }
            return select;
    }

    public javax.faces.model.SelectItem[] getProductIds() {
        EntityManager em = getEntityManager();
        List <Product> l = (List <Product>) em.createQuery("select o from Product as o").getResultList();
        em.close();
        SelectItem select[] = new SelectItem[l.size()];
        int i = 0;
        for(Product x : l) {
                select[i++] = new SelectItem(x);
            }
            return select;
    }

    public int getItemCount() {
        EntityManager em = getEntityManager();
        int count = ((Long) em.createQuery("select count(o) from Orders as o").getSingleResult()).intValue();em.close();
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
        return "orders_list";
    }

    public String prev() {
        firstItem -= batchSize;
        if (firstItem < 0) {
            firstItem = 0;
        }
        return "orders_list";
    }
}
