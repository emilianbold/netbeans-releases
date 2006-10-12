package org.demo;
import java.util.Collection;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

public class ProductCodeController {

    private ProductCode productCode;

    @Resource
    private UserTransaction utx;

    @PersistenceUnit(unitName = "SjsasJSFTestPU")
    private EntityManagerFactory emf;

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    private int batchSize = 20;

    private int firstItem = 0;

    public String createSetup() {
        this.productCode = new ProductCode();
        return "productCode_create";
    }

    public String create() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.persist(productCode);
            utx.commit();
            addSuccessMessage("ProductCode was successfully created.");
        } catch (Exception ex) {
            try {
                addErrorMessage(ex.getLocalizedMessage());
                utx.rollback();
            } catch (Exception e) {
                addErrorMessage(e.getLocalizedMessage());
            }
        }
        em.close();
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
            productCode = em.merge(productCode);
            utx.commit();
            addSuccessMessage("ProductCode was successfully updated.");
        } catch (Exception ex) {
            try {
                addErrorMessage(ex.getLocalizedMessage());
                utx.rollback();
            } catch (Exception e) {
                addErrorMessage(e.getLocalizedMessage());
            }
        }
        em.close();
        return "productCode_list"; 
    }

    public String destroy() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            ProductCode productCode = getProductCodeFromRequestParam();
            productCode = em.merge(productCode);
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
        }
        em.close();
        return "productCode_list"; 
    }

    public ProductCode getProductCodeFromRequestParam() {
        EntityManager em = getEntityManager();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("prodCode");
        String id = param;
        ProductCode o = em.find(ProductCode.class, id);
        em.close();
        return o;
    }

    public void setProductCodeFromRequestParam() {
        ProductCode productCode = getProductCodeFromRequestParam();
        setProductCode(productCode);
    }

    public Collection getProductCodes() {
        EntityManager em = getEntityManager();
        Query q = em.createQuery("select object(o) from ProductCode as o");
        q.setMaxResults(batchSize);
        q.setFirstResult(firstItem);
        Collection c = q.getResultList();
        em.close();
        return c;
    }

    public ProductCode getProductCode() {
        return productCode;
    }

    public void setProductCode(ProductCode productCode) {
        this.productCode = productCode;
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
        ProductCode o = (ProductCode) em.find(ProductCode.class, id);
        em.close();
        return o;
    }

    public int getItemCount() {
        EntityManager em = getEntityManager();
        int count = ((Long) em.createQuery("select count(o) from ProductCode as o").getSingleResult()).intValue();em.close();
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
