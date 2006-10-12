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

import java.util.Collection;
import javax.ejb.*;

/**
 * This is the bean class for the VendorPartBean enterprise bean.
 */
public abstract class VendorPartBean implements EntityBean, VendorPartLocalBusiness {
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
    
    public abstract String getDescription();
    public abstract void setDescription(String description);
    
    public abstract Double getPrice();
    public abstract void setPrice(Double price);
    
    public abstract Collection getLineitemBean();
    public abstract void setLineitemBean(Collection lineitemBean);
    
    public abstract PartLocal getPartBean();
    public abstract void setPartBean(PartLocal partBean);
    
    
    public Object ejbCreate(String description, Double price, PartLocal partBean, VendorLocal vendorId)  throws CreateException {
        if (price == null) {
            throw new CreateException("The field \"price\" must not be null");
        }
        if (partBean == null) {
            throw new CreateException("The field \"partBean\" must not be null");
        }
        if (vendorId == null) {
            throw new CreateException("The field \"vendorId\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setDescription(description);
        setPrice(price);
        
        return null;
    }
    
    public void ejbPostCreate(String description, Double price, PartLocal partBean, VendorLocal vendorId) {
        // TODO populate relationships here if appropriate
        setPartBean(partBean);
        setVendor(vendorId);
        
    }
    
    public Object ejbCreate(String description, double price, PartLocal part) throws CreateException {
        //TODO implement ejbCreate
            setDescription(description);
            setPrice(new Double(price));
            
        return null;
    }
    
    public void ejbPostCreate(String description, double price, PartLocal part) throws CreateException {
        //TODO implement ejbPostCreate
        setPartBean(part);
    }
    
    public Double ejbHomeGetAvgPrice() throws FinderException {
        //TODO implement ejbHomeGetAvgPrice
        return ejbSelectAvgPrice();
    }
    
    public abstract Double ejbSelectAvgPrice() throws FinderException;
    
    public Double ejbHomeGetTotalPricePerVendor(int vendorId) throws FinderException {
        //TODO implement ejbHomeGetTotalPricePerVendor
        return ejbSelectTotalPricePerVendor(vendorId);
    }
    
    public abstract Double ejbSelectTotalPricePerVendor(int vendorId) throws FinderException;
    
    public abstract VendorLocal getVendor();
    
    public abstract void setVendor(VendorLocal vendor);

}
