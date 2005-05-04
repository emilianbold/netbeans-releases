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
 * This is the local-home interface for Part enterprise bean.
 */
public interface PartLocalHome extends javax.ejb.EJBLocalHome {
    
    
    
    /**
     *
     */
    dataregistry.PartLocal findByPrimaryKey(dataregistry.PartPK key)  throws javax.ejb.FinderException;

    public dataregistry.PartLocal create(java.lang.String partNumber, java.math.BigDecimal revision, java.lang.String description, java.sql.Timestamp revisionDate, dataregistry.PartLocal partBean) throws javax.ejb.CreateException;

    java.util.Collection findByPartNumber(java.lang.String partNumber) throws javax.ejb.FinderException;

    java.util.Collection findByRevision(java.math.BigDecimal revision) throws javax.ejb.FinderException;

    java.util.Collection findByDescription(java.lang.String description) throws javax.ejb.FinderException;

    dataregistry.PartLocal create(java.lang.String partNumber, int revision, java.lang.String description, java.util.Date revisionDate, java.lang.String specification, java.io.Serializable drawing) throws javax.ejb.CreateException;
    
    
}
