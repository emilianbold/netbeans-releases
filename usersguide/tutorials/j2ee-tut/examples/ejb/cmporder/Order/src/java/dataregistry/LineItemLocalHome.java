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
 * This is the local-home interface for Lineitem enterprise bean.
 */
public interface LineItemLocalHome extends javax.ejb.EJBLocalHome {
    
    
    
    /**
     *
     */
    dataregistry.LineItemLocal findByPrimaryKey(dataregistry.LineItemPK key)  throws javax.ejb.FinderException;

    public dataregistry.LineItemLocal create(java.lang.Integer orderId, java.math.BigDecimal itemId, java.math.BigDecimal quantity, dataregistry.OrdersLocal ordersBean, dataregistry.VendorPartLocal vendorPartNumber) throws javax.ejb.CreateException;

    java.util.Collection findByOrderId(java.lang.Integer orderId) throws javax.ejb.FinderException;

    java.util.Collection findByItemId(java.math.BigDecimal itemId) throws javax.ejb.FinderException;

    java.util.Collection findByQuantity(java.math.BigDecimal quantity) throws javax.ejb.FinderException;

    dataregistry.LineItemLocal create(dataregistry.OrdersLocal ordersBean, java.math.BigDecimal quantity, dataregistry.VendorPartLocal vendorPartNumber) throws javax.ejb.CreateException;

    java.util.Collection findAll() throws javax.ejb.FinderException;
    
    
}
