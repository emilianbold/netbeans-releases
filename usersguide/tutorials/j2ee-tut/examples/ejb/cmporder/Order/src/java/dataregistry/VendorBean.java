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

import javax.ejb.*;

/**
 * This is the bean class for the VendorBean enterprise bean.
 */
public abstract class VendorBean implements javax.ejb.EntityBean, dataregistry.VendorLocalBusiness {
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
    
    
    public abstract int getVendorId();
    public abstract void setVendorId(int vendorId);
    
    public abstract java.lang.String getName();
    public abstract void setName(java.lang.String name);
    
    public abstract java.lang.String getAddress();
    public abstract void setAddress(java.lang.String address);
    
    public abstract java.lang.String getContact();
    public abstract void setContact(java.lang.String contact);
    
    public abstract java.lang.String getPhone();
    public abstract void setPhone(java.lang.String phone);
    
    public abstract java.util.Collection getVendorPartBean();
    public abstract void setVendorPartBean(java.util.Collection vendorPartBean);
    
    
    public dataregistry.VendorKey ejbCreate(int vendorId, java.lang.String name, java.lang.String address, java.lang.String contact, java.lang.String phone)  throws javax.ejb.CreateException {
        if (name == null) {
            throw new javax.ejb.CreateException("The field \"name\" must not be null");
        }
        if (address == null) {
            throw new javax.ejb.CreateException("The field \"address\" must not be null");
        }
        if (contact == null) {
            throw new javax.ejb.CreateException("The field \"contact\" must not be null");
        }
        if (phone == null) {
            throw new javax.ejb.CreateException("The field \"phone\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setVendorId(vendorId);
        setName(name);
        setAddress(address);
        setContact(contact);
        setPhone(phone);
        
        return null;
    }
    
    public void ejbPostCreate(int vendorId, java.lang.String name, java.lang.String address, java.lang.String contact, java.lang.String phone) {
        // TODO populate relationships here if appropriate
        
    }
}
