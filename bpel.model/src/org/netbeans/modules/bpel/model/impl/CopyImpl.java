/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.model.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.xam.AfterSources;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class CopyImpl extends FromHolderImpl implements Copy, AfterSources {

    CopyImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    CopyImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.COPY.getName() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel2020.api.Copy#getTo()
     */
    public To getTo() {
        return getChild(To.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel2020.api.Copy#setTo(org.netbeans.modules.soa.model.bpel2020.api.To)
     */
    public void setTo( To value ) {
        setChild(value, To.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel2020.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Copy.class;
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit(this);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.Copy#getKeepSrcElementName()
     */
    public TBoolean getKeepSrcElementName() {
        return getBooleanAttribute( BpelAttributes.KEEP_SRC_ELEMENT_NAME );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.Copy#setKeepSrcElementName(org.netbeans.modules.bpel.model.api.support.TBoolean)
     */
    public void setKeepSrcElementName( TBoolean value ) {
        setBpelAttribute( BpelAttributes.KEEP_SRC_ELEMENT_NAME , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.Copy#removeKeepSrcElementName()
     */
    public void removeKeepSrcElementName() {
        removeAttribute( BpelAttributes.KEEP_SRC_ELEMENT_NAME );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.Copy#getIgnoreMissingFromData()
     */
    public TBoolean getIgnoreMissingFromData() {
        return getBooleanAttribute( BpelAttributes.IGNORE_MISSING_FROM_DATA );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.Copy#removeIgnoreMissingFromData()
     */
    public void removeIgnoreMissingFromData() {
        removeAttribute( BpelAttributes.IGNORE_MISSING_FROM_DATA );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.Copy#setIgnoreMissingFromData(org.netbeans.modules.bpel.model.api.support.TBoolean)
     */
    public void setIgnoreMissingFromData( TBoolean value ) {
        setBpelAttribute( BpelAttributes.IGNORE_MISSING_FROM_DATA, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.xdm.impl.BpelContainerImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        if ( BpelElements.TO.getName().equals(element.getLocalName())) {
            return new ToImpl(getModel(), element);
        }
        return super.create( element );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 1];
            System.arraycopy( attr , 0 , ret , 1 , attr.length );
            ret[ 0 ] = BpelAttributes.KEEP_SRC_ELEMENT_NAME;
            myAttributes.compareAndSet( null ,  ret);
        }
        return myAttributes.get();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( getChildType( entity ).equals( To.class) ) {
            return Multiplicity.SINGLE;
        }
        return super.getMultiplicity(entity);
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();


}
