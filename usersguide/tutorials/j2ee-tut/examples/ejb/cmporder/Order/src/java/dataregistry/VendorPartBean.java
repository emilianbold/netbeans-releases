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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.*;

/**
 * This is the bean class for the VendorPartBean enterprise bean.
 */
public abstract class VendorPartBean implements javax.ejb.EntityBean, dataregistry.VendorPartLocalBusiness {
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
    
    public abstract java.lang.String getDescription();
    public abstract void setDescription(java.lang.String description);
    
    public abstract java.lang.Double getPrice();
    public abstract void setPrice(java.lang.Double price);
    
    public abstract java.util.Collection getLineitemBean();
    public abstract void setLineitemBean(java.util.Collection lineitemBean);
    
    public abstract dataregistry.PartLocal getPartBean();
    public abstract void setPartBean(dataregistry.PartLocal partBean);
    
    
    public java.lang.Object ejbCreate(java.lang.String description, java.lang.Double price, dataregistry.PartLocal partBean, dataregistry.VendorLocal vendorId)  throws javax.ejb.CreateException {
        if (price == null) {
            throw new javax.ejb.CreateException("The field \"price\" must not be null");
        }
        if (partBean == null) {
            throw new javax.ejb.CreateException("The field \"partBean\" must not be null");
        }
        if (vendorId == null) {
            throw new javax.ejb.CreateException("The field \"vendorId\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setDescription(description);
        setPrice(price);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.String description, java.lang.Double price, dataregistry.PartLocal partBean, dataregistry.VendorLocal vendorId) {
        // TODO populate relationships here if appropriate
        setPartBean(partBean);
        setVendor(vendorId);
        
    }
    
    public java.lang.Object ejbCreate(java.lang.String description, double price, dataregistry.PartLocal part) throws javax.ejb.CreateException {
        //TODO implement ejbCreate
            setDescription(description);
            setPrice(new Double(price));
            
        return null;
    }
    
    public void ejbPostCreate(java.lang.String description, double price, dataregistry.PartLocal part) throws javax.ejb.CreateException {
        //TODO implement ejbPostCreate
        setPartBean(part);
    }
    
    public Double ejbHomeGetAvgPrice() throws FinderException {
        //TODO implement ejbHomeGetAvgPrice
        return ejbSelectAvgPrice();
    }
    
    public abstract Double ejbSelectAvgPrice() throws javax.ejb.FinderException;
    
    public Double ejbHomeGetTotalPricePerVendor(int vendorId) throws FinderException {
        //TODO implement ejbHomeGetTotalPricePerVendor
        return ejbSelectTotalPricePerVendor(vendorId);
    }
    
    public abstract Double ejbSelectTotalPricePerVendor(int vendorId) throws javax.ejb.FinderException;
    
    public abstract dataregistry.VendorLocal getVendor();
    
    public abstract void setVendor(dataregistry.VendorLocal vendor);

}
