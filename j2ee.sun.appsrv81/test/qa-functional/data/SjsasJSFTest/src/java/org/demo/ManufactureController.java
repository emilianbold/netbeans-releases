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

public class ManufactureController {

    private Manufacture manufacture;

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
        this.manufacture = new Manufacture();
        return "manufacture_create";
    }

    public String create() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.persist(manufacture);
            utx.commit();
            addSuccessMessage("Manufacture was successfully created.");
        } catch (Exception ex) {
            try {
                addErrorMessage(ex.getLocalizedMessage());
                utx.rollback();
            } catch (Exception e) {
                addErrorMessage(e.getLocalizedMessage());
            }
        }
        em.close();
        return "manufacture_list"; 
    }

    public String detailSetup() {
        setManufactureFromRequestParam();
        return "manufacture_detail";
    }

    public String editSetup() {
        setManufactureFromRequestParam();
        return "manufacture_edit";
    }

    public String edit() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            manufacture = em.merge(manufacture);
            utx.commit();
            addSuccessMessage("Manufacture was successfully updated.");
        } catch (Exception ex) {
            try {
                addErrorMessage(ex.getLocalizedMessage());
                utx.rollback();
            } catch (Exception e) {
                addErrorMessage(e.getLocalizedMessage());
            }
        }
        em.close();
        return "manufacture_list"; 
    }

    public String destroy() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            Manufacture manufacture = getManufactureFromRequestParam();
            manufacture = em.merge(manufacture);
            em.remove(manufacture);
            utx.commit();
            addSuccessMessage("Manufacture was successfully deleted.");
        } catch (Exception ex) {
            try {
                addErrorMessage(ex.getLocalizedMessage());
                utx.rollback();
            } catch (Exception e) {
                addErrorMessage(e.getLocalizedMessage());
            }
        }
        em.close();
        return "manufacture_list"; 
    }

    public Manufacture getManufactureFromRequestParam() {
        EntityManager em = getEntityManager();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("manufactureId");
        Integer id = new Integer(param);
        Manufacture o = em.find(Manufacture.class, id);
        em.close();
        return o;
    }

    public void setManufactureFromRequestParam() {
        Manufacture manufacture = getManufactureFromRequestParam();
        setManufacture(manufacture);
    }

    public Collection getManufactures() {
        EntityManager em = getEntityManager();
        Query q = em.createQuery("select object(o) from Manufacture as o");
        q.setMaxResults(batchSize);
        q.setFirstResult(firstItem);
        Collection c = q.getResultList();
        em.close();
        return c;
    }

    public Manufacture getManufacture() {
        return manufacture;
    }

    public void setManufacture(Manufacture manufacture) {
        this.manufacture = manufacture;
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

    public Manufacture findManufacture(Integer id) {
        EntityManager em = getEntityManager();
        Manufacture o = (Manufacture) em.find(Manufacture.class, id);
        em.close();
        return o;
    }

    public int getItemCount() {
        EntityManager em = getEntityManager();
        int count = ((Long) em.createQuery("select count(o) from Manufacture as o").getSingleResult()).intValue();em.close();
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
        return "manufacture_list";
    }

    public String prev() {
        firstItem -= batchSize;
        if (firstItem < 0) {
            firstItem = 0;
        }
        return "manufacture_list";
    }
}
