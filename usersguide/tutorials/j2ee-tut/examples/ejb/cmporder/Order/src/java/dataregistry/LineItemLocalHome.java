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
import javax.ejb.*;


/**
 * This is the local-home interface for Lineitem enterprise bean.
 */
public interface LineItemLocalHome extends EJBLocalHome {
    

    LineItemLocal findByPrimaryKey(LineItemPK key)  throws FinderException;

    public LineItemLocal create(Integer orderId, BigDecimal itemId, BigDecimal quantity, OrdersLocal ordersBean, VendorPartLocal vendorPartNumber) throws CreateException;

    java.util.Collection findByOrderId(Integer orderId) throws FinderException;

    java.util.Collection findByItemId(BigDecimal itemId) throws FinderException;

    java.util.Collection findByQuantity(BigDecimal quantity) throws FinderException;

    LineItemLocal create(OrdersLocal ordersBean, BigDecimal quantity, VendorPartLocal vendorPartNumber) throws CreateException;

    java.util.Collection findAll() throws FinderException;
    
    
}
