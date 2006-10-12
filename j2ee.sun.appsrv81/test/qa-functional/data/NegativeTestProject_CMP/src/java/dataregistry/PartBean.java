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

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

/**
 * This is the bean class for the PartBean enterprise bean.
 */
public abstract class PartBean implements EntityBean, PartLocalBusiness {
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
    
    
    public abstract String getPartNumber();
    public abstract void setPartNumber(String partNumber);
    
    public abstract BigDecimal getRevision();
    public abstract void setRevision(BigDecimal revision);
    
    public abstract String getDescription();
    public abstract void setDescription(String description);
    
    public abstract Timestamp getRevisionDate();
    public abstract void setRevisionDate(Timestamp revisionDate);
    
    public abstract PartLocal getBomPart();
    public abstract void setBomPart(PartLocal partBean);
    
    public abstract Collection getPartBean1();
    public abstract void setPartBean1(Collection partBean1);
    
    public abstract VendorPartLocal getVendorPartBean();
    public abstract void setVendorPartBean(VendorPartLocal vendorPartBean);
    
    
    public PartPK ejbCreate(String partNumber, BigDecimal revision, String description, Timestamp revisionDate, PartLocal partBean)  throws CreateException {
        if (partNumber == null) {
            throw new CreateException("The field \"partNumber\" must not be null");
        }
        if (revision == null) {
            throw new CreateException("The field \"revision\" must not be null");
        }
        if (revisionDate == null) {
            throw new CreateException("The field \"revisionDate\" must not be null");
        }
        if (partBean == null) {
            throw new CreateException("The field \"partBean\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setPartNumber(partNumber);
        setRevision(revision);
        setDescription(description);
        setRevisionDate(revisionDate);
        
        return null;
    }
    
    public void ejbPostCreate(String partNumber, BigDecimal revision, String description, Timestamp revisionDate, PartLocal partBean) {
        // TODO populate relationships here if appropriate
        setBomPart(partBean);
        
    }
    
    

    public abstract Serializable getDrawing();

    public abstract void setDrawing(Serializable drawing);

    public abstract String getSpecification();

    public abstract void setSpecification(String specification);

    public PartPK ejbCreate(String partNumber, int revision, String description, java.util.Date revisionDate, String specification, Serializable drawing) throws CreateException {
        if (partNumber == null) {
            throw new CreateException("The field \"partNumber\" must not be null");
        }
        if (revisionDate == null) {
            throw new CreateException("The field \"revisionDate\" must not be null");
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

    public void ejbPostCreate(String partNumber, int revision, String description, java.util.Date revisionDate, String specification, Serializable drawing) throws CreateException {
        //TODO implement ejbPostCreate
    }
}
