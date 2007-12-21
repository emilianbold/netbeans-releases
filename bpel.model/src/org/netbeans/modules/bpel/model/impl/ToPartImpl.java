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

/**
 *
 */
package org.netbeans.modules.bpel.model.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ToPart;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.AfterSources;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class ToPartImpl extends BpelEntityImpl implements ToPart, AfterSources {

    ToPartImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    ToPartImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.TO_PART.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ToPart#getFromVariable()
     */
    public BpelReference<VariableDeclaration> getFromVariable() {
        return getBpelReference( BpelAttributes.FROM_VARIABLE , 
                VariableDeclaration.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ToPart#setFromVariable(org.netbeans.modules.soa.model.bpel20.references.VariableReference)
     */
    public void setFromVariable( BpelReference<VariableDeclaration> variable ) {
        setBpelReference( BpelAttributes.FROM_VARIABLE , variable );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return ToPart.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartReference#getPart()
     */
    public WSDLReference<Part> getPart() {
        return getWSDLReference( BpelAttributes.PART , Part.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartReference#setPart(org.netbeans.modules.soa.model.bpel20.api.support.WSDLReference)
     */
    public void setPart( WSDLReference<Part> part ) {
        setWSDLReference( BpelAttributes.PART , part );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences() {
        return new Reference[] { getFromVariable() , getPart() } ;
    }    

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] ret= new Attribute[]{ BpelAttributes.FROM_VARIABLE , 
                    BpelAttributes.PART };
            myAttributes.compareAndSet( null , ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();
}
