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


/**
 * This is the local-home interface for Vendor enterprise bean.
 */
public interface VendorLocalHome extends javax.ejb.EJBLocalHome {
    
    
    
    /**
     *
     */
    dataregistry.VendorLocal findByPrimaryKey(dataregistry.VendorKey key)  throws javax.ejb.FinderException;

    public dataregistry.VendorLocal create(int vendorId, java.lang.String name, java.lang.String address, java.lang.String contact, java.lang.String phone) throws javax.ejb.CreateException;

    java.util.Collection findByVendorId(java.lang.Integer vendorId) throws javax.ejb.FinderException;

    java.util.Collection findByName(java.lang.String name) throws javax.ejb.FinderException;

    java.util.Collection findByAddress(java.lang.String address) throws javax.ejb.FinderException;

    java.util.Collection findByContact(java.lang.String contact) throws javax.ejb.FinderException;

    java.util.Collection findByPhone(java.lang.String phone) throws javax.ejb.FinderException;

    java.util.Collection findByPartialName(java.lang.String name) throws javax.ejb.FinderException;

    java.util.Collection findByOrder(java.lang.Integer orderId) throws javax.ejb.FinderException;
    
    
}
