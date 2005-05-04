/*
 * Copyright (c) 2004 Sun Microsystems, Inc.  All rights reserved.  U.S.
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
 * Copyright (c) 2004 Sun Microsystems, Inc. Tous droits reserves.
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
import javax.ejb.FinderException;

/**
 * This is the bean class for the LineitemBean enterprise bean.
 */
public abstract class LineItemBean implements javax.ejb.EntityBean, dataregistry.LineItemLocalBusiness {
    private javax.ejb.EntityContext context;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click on the + sign on the left to edit the code.">
    // TODO Consider creating Transfer Object to encapsulate data
    // TODO Review finder methods
    /**
     * @see javax.ejb.EntityBean#setEntityContext(javax.ejb.EntityContext)
     */
    public void setEntityContext(javax.ejb.EntityContext aContext) {
        context = aContext;
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbRemove()
     */
    public void ejbRemove() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#unsetEntityContext()
     */
    public void unsetEntityContext() {
        context = null;
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbLoad()
     */
    public void ejbLoad() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbStore()
     */
    public void ejbStore() {
        
    }
    // </editor-fold>
    
    
    public abstract java.lang.Integer getOrderId();
    public abstract void setOrderId(java.lang.Integer orderId);
    
    public abstract java.math.BigDecimal getItemId();
    public abstract void setItemId(java.math.BigDecimal itemId);
    
    public abstract java.math.BigDecimal getQuantity();
    public abstract void setQuantity(java.math.BigDecimal quantity);
    
    public abstract dataregistry.OrdersLocal getOrdersBean();
    public abstract void setOrdersBean(dataregistry.OrdersLocal ordersBean);
    
    public abstract dataregistry.VendorPartLocal getVendorPartNumber();
    public abstract void setVendorPartNumber(dataregistry.VendorPartLocal vendorPartNumber);
    
    
    public dataregistry.LineItemPK ejbCreate(java.lang.Integer orderId, java.math.BigDecimal itemId,
            java.math.BigDecimal quantity, dataregistry.OrdersLocal ordersBean,
            dataregistry.VendorPartLocal vendorPartNumber)  throws javax.ejb.CreateException {
        if (orderId == null) {
            throw new javax.ejb.CreateException("The field \"orderId\" must not be null");
        }
        if (itemId == null) {
            throw new javax.ejb.CreateException("The field \"itemId\" must not be null");
        }
        if (quantity == null) {
            throw new javax.ejb.CreateException("The field \"quantity\" must not be null");
        }
        if (ordersBean == null) {
            throw new javax.ejb.CreateException("The field \"ordersBean\" must not be null");
        }
        if (vendorPartNumber == null) {
            throw new javax.ejb.CreateException("The field \"vendorPartNumber\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setOrderId(orderId);
        setItemId(itemId);
        setQuantity(quantity);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.Integer orderId, java.math.BigDecimal itemId, java.math.BigDecimal quantity, dataregistry.OrdersLocal ordersBean, dataregistry.VendorPartLocal vendorPartNumber) {
        // TODO populate relationships here if appropriate
        setOrdersBean(ordersBean);
        setVendorPartNumber(vendorPartNumber);
        
    }
    
    public dataregistry.LineItemPK ejbCreate(dataregistry.OrdersLocal ordersBean, java.math.BigDecimal quantity, dataregistry.VendorPartLocal vendorPartNumber) throws javax.ejb.CreateException {
        //TODO implement ejbCreate
        if (quantity == null) {
            throw new javax.ejb.CreateException("The field \"quantity\" must not be null");
        }
        if (ordersBean == null) {
            throw new javax.ejb.CreateException("The field \"ordersBean\" must not be null");
        }
        if (vendorPartNumber == null) {
            throw new javax.ejb.CreateException("The field \"vendorPartNumber\" must not be null");
        }
        setOrderId(ordersBean.getOrderId());
        setItemId(new BigDecimal(ordersBean.getNexId()));
        setQuantity(quantity);

        return null;
    }
    
    public void ejbPostCreate(dataregistry.OrdersLocal ordersBean, java.math.BigDecimal quantity, dataregistry.VendorPartLocal vendorPartNumber) throws javax.ejb.CreateException {
        //TODO implement ejbPostCreate
        setOrdersBean(ordersBean);
        setVendorPartNumber(vendorPartNumber);
    }

}
