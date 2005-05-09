/*
 * Copyright (c) 2005 Sun Microsystems, Inc.  All rights reserved.  U.S.
 * Government Rights - Commercial software.  Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.  Use is subject
 * to license terms.
 *
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and
 * other countries.
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. Tous droits reserves.
 *
 * Droits du gouvernement americain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions
 * en vigueur de la FAR (Federal Acquisition Regulations) et des
 * supplements a celles-ci.  Distribue par des licences qui en
 * restreignent l'utilisation.
 *
 * Cette distribution peut comprendre des composants developpes par des
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE
 * sont des marques de fabrique ou des marques deposees de Sun
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
 */

package dataregistry;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.*;

/**
 * This is the bean class for the LineitemBean enterprise bean.
 */
public abstract class LineItemBean implements EntityBean, LineItemLocalBusiness {
    private EntityContext context;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click on the + sign on the left to edit the code.">
    // TODO Consider creating Transfer Object to encapsulate data
    // TODO Review finder methods
    /**
     * @see EntityBean#setEntityContext(EntityContext)
     */
    public void setEntityContext(EntityContext aContext) {
        context = aContext;
    }
    
    /**
     * @see EntityBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    /**
     * @see EntityBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    /**
     * @see EntityBean#ejbRemove()
     */
    public void ejbRemove() {
        
    }
    
    /**
     * @see EntityBean#unsetEntityContext()
     */
    public void unsetEntityContext() {
        context = null;
    }
    
    /**
     * @see EntityBean#ejbLoad()
     */
    public void ejbLoad() {
        
    }
    
    /**
     * @see EntityBean#ejbStore()
     */
    public void ejbStore() {
        
    }
    // </editor-fold>
    
    
    public abstract Integer getOrderId();
    public abstract void setOrderId(Integer orderId);
    
    public abstract BigDecimal getItemId();
    public abstract void setItemId(BigDecimal itemId);
    
    public abstract BigDecimal getQuantity();
    public abstract void setQuantity(BigDecimal quantity);
    
    public abstract VendorPartLocal getVendorPartNumber();
    public abstract void setVendorPartNumber(VendorPartLocal vendorPartNumber);
    
    
    public LineItemPK ejbCreate(Integer orderId, BigDecimal itemId,
            BigDecimal quantity, OrderLocal ordersBean,
            VendorPartLocal vendorPartNumber)  throws CreateException {
        if (orderId == null) {
            throw new CreateException("The field \"orderId\" must not be null");
        }
        if (itemId == null) {
            throw new CreateException("The field \"itemId\" must not be null");
        }
        if (quantity == null) {
            throw new CreateException("The field \"quantity\" must not be null");
        }
        if (ordersBean == null) {
            throw new CreateException("The field \"ordersBean\" must not be null");
        }
        if (vendorPartNumber == null) {
            throw new CreateException("The field \"vendorPartNumber\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setOrderId(orderId);
        setItemId(itemId);
        setQuantity(quantity);
        
        return null;
    }
    
    public void ejbPostCreate(Integer orderId, BigDecimal itemId, BigDecimal quantity, OrderLocal ordersBean, VendorPartLocal vendorPartNumber) {
        // TODO populate relationships here if appropriate
        setOrderBean(ordersBean);
        setVendorPartNumber(vendorPartNumber);
        
    }
    
    public LineItemPK ejbCreate(OrderLocal ordersBean, BigDecimal quantity, VendorPartLocal vendorPartNumber) throws CreateException {
        //TODO implement ejbCreate
        if (quantity == null) {
            throw new CreateException("The field \"quantity\" must not be null");
        }
        if (ordersBean == null) {
            throw new CreateException("The field \"ordersBean\" must not be null");
        }
        if (vendorPartNumber == null) {
            throw new CreateException("The field \"vendorPartNumber\" must not be null");
        }
        setOrderId(ordersBean.getOrderId());
        setItemId(new BigDecimal(ordersBean.getNexId()));
        setQuantity(quantity);

        return null;
    }
    
    public void ejbPostCreate(OrderLocal ordersBean, BigDecimal quantity, VendorPartLocal vendorPartNumber) throws CreateException {
        //TODO implement ejbPostCreate
        setOrderBean(ordersBean);
        setVendorPartNumber(vendorPartNumber);
    }

    public abstract dataregistry.OrderLocal getOrderBean();

    public abstract void setOrderBean(dataregistry.OrderLocal orderBean);

}
