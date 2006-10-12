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
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import javax.ejb.EJBException;

/**
 * This is the bean class for the OrdersBean enterprise bean.
 */
public abstract class OrderBean implements javax.ejb.EntityBean, dataregistry.OrderLocalBusiness {
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
    
    public abstract java.lang.String getStatus();
    public abstract void setStatus(java.lang.String status);
    
    public abstract java.sql.Timestamp getLastUpdate();
    public abstract void setLastUpdate(java.sql.Timestamp lastUpdate);
    
    public abstract java.math.BigDecimal getDiscount();
    public abstract void setDiscount(java.math.BigDecimal discount);
    
    public abstract java.lang.String getShipmentInfo();
    public abstract void setShipmentInfo(java.lang.String shipmentInfo);
    
    public abstract java.util.Collection getLineitemBean();
    public abstract void setLineitemBean(java.util.Collection lineitemBean);
    
    
    public java.lang.Integer ejbCreate(java.lang.Integer orderId, java.lang.String status, java.sql.Timestamp lastUpdate, java.math.BigDecimal discount, java.lang.String shipmentInfo)  throws javax.ejb.CreateException {
        if (orderId == null) {
            throw new javax.ejb.CreateException("The field \"orderId\" must not be null");
        }
        if (status == null) {
            throw new javax.ejb.CreateException("The field \"status\" must not be null");
        }
        if (lastUpdate == null) {
            throw new javax.ejb.CreateException("The field \"lastUpdate\" must not be null");
        }
        if (discount == null) {
            throw new javax.ejb.CreateException("The field \"discount\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setOrderId(orderId);
        setStatus(status);
        setLastUpdate(lastUpdate);
        setDiscount(discount);
        setShipmentInfo(shipmentInfo);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.Integer orderId, java.lang.String status, java.sql.Timestamp lastUpdate, java.math.BigDecimal discount, java.lang.String shipmentInfo) {
        // TODO populate relationships here if appropriate
        
    }

    public java.lang.Integer ejbCreate(java.lang.Integer orderId, java.lang.String status, java.math.BigDecimal discount, java.lang.String shipmentInfo) throws javax.ejb.CreateException {
        //TODO implement ejbCreate
        setOrderId(orderId);
        setStatus(status);
        setLastUpdate(new Timestamp(new Date().getTime()));
        setDiscount(discount);
        setShipmentInfo(shipmentInfo);
        
        return null;
    }

    public void ejbPostCreate(java.lang.Integer orderId, java.lang.String status, java.math.BigDecimal doscount, java.lang.String shipmentInfo) throws javax.ejb.CreateException {
        //TODO implement ejbPostCreate
    }

    public double calculateAmmount() {
        //TODO implement calculateAmmount
        double ammount = 0;
        Collection items = getLineitemBean();
                                                                           
        for (Iterator it = items.iterator(); it.hasNext();) {
            LineItemLocal item = (LineItemLocal) it.next();
            VendorPartLocal part = item.getVendorPartNumber();
            ammount += (part.getPrice().doubleValue() * item.getQuantity().doubleValue());
        }
                                                                           
        return (ammount * (100 - getDiscount().intValue())) / 100;

    }

    public void ejbHomeAdjustDiscount(int adjustment) {
        //TODO implement ejbHomeHomeAdjustDiscount
        try {
            Collection orders = ejbSelectAll();
                                                                           
            for (Iterator it = orders.iterator(); it.hasNext();) {
                OrderLocal order = (OrderLocal) it.next();
                int newDiscount = order.getDiscount().intValue() + adjustment;
                order.setDiscount((newDiscount > 0) ? new BigDecimal(newDiscount) : new BigDecimal(0));
            }
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

    }

    public abstract java.util.Collection ejbSelectAll() throws javax.ejb.FinderException;

    public int getNexId() {
        return getLineitemBean().size() + 1;
    }
}
