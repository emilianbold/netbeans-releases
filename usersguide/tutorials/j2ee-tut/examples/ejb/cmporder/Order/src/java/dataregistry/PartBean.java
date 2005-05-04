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
import java.sql.Timestamp;

/**
 * This is the bean class for the PartBean enterprise bean.
 */
public abstract class PartBean implements javax.ejb.EntityBean, dataregistry.PartLocalBusiness {
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
    
    
    public abstract java.lang.String getPartNumber();
    public abstract void setPartNumber(java.lang.String partNumber);
    
    public abstract java.math.BigDecimal getRevision();
    public abstract void setRevision(java.math.BigDecimal revision);
    
    public abstract java.lang.String getDescription();
    public abstract void setDescription(java.lang.String description);
    
    public abstract java.sql.Timestamp getRevisionDate();
    public abstract void setRevisionDate(java.sql.Timestamp revisionDate);
    
    public abstract dataregistry.PartLocal getBomPart();
    public abstract void setBomPart(dataregistry.PartLocal partBean);
    
    public abstract java.util.Collection getPartBean1();
    public abstract void setPartBean1(java.util.Collection partBean1);
    
    public abstract dataregistry.VendorPartLocal getVendorPartBean();
    public abstract void setVendorPartBean(dataregistry.VendorPartLocal vendorPartBean);
    
    
    public dataregistry.PartPK ejbCreate(java.lang.String partNumber, java.math.BigDecimal revision, java.lang.String description, java.sql.Timestamp revisionDate, dataregistry.PartLocal partBean)  throws javax.ejb.CreateException {
        if (partNumber == null) {
            throw new javax.ejb.CreateException("The field \"partNumber\" must not be null");
        }
        if (revision == null) {
            throw new javax.ejb.CreateException("The field \"revision\" must not be null");
        }
        if (revisionDate == null) {
            throw new javax.ejb.CreateException("The field \"revisionDate\" must not be null");
        }
        if (partBean == null) {
            throw new javax.ejb.CreateException("The field \"partBean\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setPartNumber(partNumber);
        setRevision(revision);
        setDescription(description);
        setRevisionDate(revisionDate);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.String partNumber, java.math.BigDecimal revision, java.lang.String description, java.sql.Timestamp revisionDate, dataregistry.PartLocal partBean) {
        // TODO populate relationships here if appropriate
        setBomPart(partBean);
        
    }
    
    

    public abstract java.io.Serializable getDrawing();

    public abstract void setDrawing(java.io.Serializable drawing);

    public abstract String getSpecification();

    public abstract void setSpecification(String specification);

    public dataregistry.PartPK ejbCreate(java.lang.String partNumber, int revision, java.lang.String description, java.util.Date revisionDate, java.lang.String specification, java.io.Serializable drawing) throws javax.ejb.CreateException {
        if (partNumber == null) {
            throw new javax.ejb.CreateException("The field \"partNumber\" must not be null");
        }
        if (revisionDate == null) {
            throw new javax.ejb.CreateException("The field \"revisionDate\" must not be null");
        }

        
        // TODO add additional validation code, throw CreateException if data is not valid
        setPartNumber(partNumber);
        setRevision(new BigDecimal(revision));
        setDescription(description);
        setRevisionDate(new Timestamp(revisionDate.getTime()));
        setSpecification(specification);
        setDrawing(drawing);
        
        return null;
    }

    public void ejbPostCreate(java.lang.String partNumber, int revision, java.lang.String description, java.util.Date revisionDate, java.lang.String specification, java.io.Serializable drawing) throws javax.ejb.CreateException {
        //TODO implement ejbPostCreate
    }
}
