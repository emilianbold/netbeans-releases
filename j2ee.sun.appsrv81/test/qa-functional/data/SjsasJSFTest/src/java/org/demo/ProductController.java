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

public class ProductController {

    private Product product;

    @Resource
    private UserTransaction utx;

    @PersistenceUnit(unitName = "SjsasJSFTestPU")
    private EntityManagerFactory emf;

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    private int batchSize = 20;

    private int firstItem = 0;

    public String destroyFromManufacture() {
        Object id = product.getManufactureId().getManufactureId();
        destroy();
        EntityManager em = getEntityManager();
        getManufactureController().setManufacture(em.find(Manufacture.class, id));
        em.close();
        return "manufacture_detail";
    }

    private ManufactureController getManufactureController() {
        return (ManufactureController) FacesContext.getCurrentInstance().
            getExternalContext().getSessionMap().get("manufacture");
    }

    public String createFromManufactureSetup() {
        this.product = new Product();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("relatedId");
        Integer id = new Integer(param);
        EntityManager em = getEntityManager();
        product.setManufactureId(em.find(Manufacture.class, id));
        em.close();
        return "product_create";
    }

    public String createFromManufacture() {
        create();
        getManufactureController().setManufacture(product.getManufactureId());
        return "manufacture_detail";
    }

    public String destroyFromProductCode() {
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

    public String createSetup() {
        this.product = new Product();
        return "product_create";
    }

    public String create() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.persist(product);
            
            //update property manufactureId of entity Manufacture
            Manufacture manufactureId=product.getManufactureId();
            if (manufactureId != null) {
                manufactureId = em.merge(manufactureId);
                manufactureId.getProduct().add(product);
                manufactureId=em.merge(manufactureId);
            }
            
            
            //update property productCode of entity ProductCode
            ProductCode productCode=product.getProductCode();
            if (productCode != null) {
                productCode = em.merge(productCode);
                productCode.getProduct().add(product);
                productCode=em.merge(productCode);
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
        }
        em.close();
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
            
            Manufacture manufactureIdOld = em.find(Product.class, product.getProductId()).getManufactureId();
            
            ProductCode productCodeOld = em.find(Product.class, product.getProductId()).getProductCode();
            product = em.merge(product);
            
            //update property manufactureId of entity Manufacture
            Manufacture manufactureIdNew=product.getManufactureId();
            if(manufactureIdNew != null) {
                manufactureIdNew.getProduct().add(product);
                manufactureIdNew=em.merge(manufactureIdNew);
            }
            if(manufactureIdOld != null) {
                manufactureIdOld.getProduct().remove(product);
                manufactureIdOld=em.merge(manufactureIdOld);
            }
            
            //update property productCode of entity ProductCode
            ProductCode productCodeNew=product.getProductCode();
            if(productCodeNew != null) {
                productCodeNew.getProduct().add(product);
                productCodeNew=em.merge(productCodeNew);
            }
            if(productCodeOld != null) {
                productCodeOld.getProduct().remove(product);
                productCodeOld=em.merge(productCodeOld);
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
        }
        em.close();
        return "product_list"; 
    }

    public String destroy() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            Product product = getProductFromRequestParam();
            product = em.merge(product);
            
            //update property manufactureId of entity Manufacture
            Manufacture manufactureId = product.getManufactureId();
            if (manufactureId != null) {
                manufactureId = em.merge(manufactureId);
                manufactureId.getProduct().remove(product);
                manufactureId=em.merge(manufactureId);
            }
            
            
            //update property productCode of entity ProductCode
            ProductCode productCode = product.getProductCode();
            if (productCode != null) {
                productCode = em.merge(productCode);
                productCode.getProduct().remove(product);
                productCode=em.merge(productCode);
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
        }
        em.close();
        return "product_list"; 
    }

    public Product getProductFromRequestParam() {
        EntityManager em = getEntityManager();
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("productId");
        Integer id = new Integer(param);
        Product o = em.find(Product.class, id);
        em.close();
        return o;
    }

    public void setProductFromRequestParam() {
        Product product = getProductFromRequestParam();
        setProduct(product);
    }

    public Collection getProducts() {
        EntityManager em = getEntityManager();
        Query q = em.createQuery("select object(o) from Product as o");
        q.setMaxResults(batchSize);
        q.setFirstResult(firstItem);
        Collection c = q.getResultList();
        em.close();
        return c;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
        Product o = (Product) em.find(Product.class, id);
        em.close();
        return o;
    }

    public javax.faces.model.SelectItem[] getManufactureIds() {
        EntityManager em = getEntityManager();
        List <Manufacture> l = (List <Manufacture>) em.createQuery("select o from Manufacture as o").getResultList();
        em.close();
        SelectItem select[] = new SelectItem[l.size()];
        int i = 0;
        for(Manufacture x : l) {
                select[i++] = new SelectItem(x);
            }
            return select;
    }

    public javax.faces.model.SelectItem[] getProductCodes() {
        EntityManager em = getEntityManager();
        List <ProductCode> l = (List <ProductCode>) em.createQuery("select o from ProductCode as o").getResultList();
        em.close();
        SelectItem select[] = new SelectItem[l.size()];
        int i = 0;
        for(ProductCode x : l) {
                select[i++] = new SelectItem(x);
            }
            return select;
    }

    public int getItemCount() {
        EntityManager em = getEntityManager();
        int count = ((Long) em.createQuery("select count(o) from Product as o").getSingleResult()).intValue();em.close();
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
