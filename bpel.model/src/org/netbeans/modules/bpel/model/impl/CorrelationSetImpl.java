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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class CorrelationSetImpl extends NamedElementImpl implements
        CorrelationSet
{

    CorrelationSetImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    CorrelationSetImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.CORRELATION_SET.getName() );
    }



    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationSet#getProperties()
     */
    public List<WSDLReference<CorrelationProperty>> getProperties() {
        return getWSDLReferenceList( BpelAttributes.PROPERTIES , 
                CorrelationProperty.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationSet#setProperties(java.util.List)
     */
    public void setProperties( List<WSDLReference<CorrelationProperty>> list ) {
        setWSDLReferenceList( BpelAttributes.PROPERTIES , 
                CorrelationProperty.class , list );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return CorrelationSet.class;
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences() {
        List<WSDLReference<CorrelationProperty>> list = getProperties();
        if ( list == null ) {
            return EMPTY_REFERENCES;
        }
        return list.toArray( new Reference[ list.size()] );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 1];
            System.arraycopy( attr , 0 , ret , 1 , attr.length );
            ret[ 0 ] = BpelAttributes.PROPERTIES;
            myAttributes.compareAndSet( null , ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();

    private static final Reference[] EMPTY_REFERENCES = new Reference[0];
}
