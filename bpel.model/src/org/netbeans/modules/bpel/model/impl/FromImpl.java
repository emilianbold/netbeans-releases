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
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.FromChild;
import org.netbeans.modules.bpel.model.api.Literal;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.Roles;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class FromImpl extends ToFromIntersectImpl implements From {

    public FromImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    public FromImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.FROM.getName() );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.From#getEndpointReference()
     */
    public Roles getEndpointReference() {
        readLock();
        try {
            String str = getAttribute( BpelAttributes.ENDPOINT_REFERENCE );
            return Roles.forString( str );
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.From#setEndpointReference(org.netbeans.modules.soa.model.bpel20.api.support.Roles)
     */
    public void setEndpointReference( Roles value ) {
        setBpelAttribute( BpelAttributes.ENDPOINT_REFERENCE , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.From#removeEndpointReference()
     */
    public void removeEndpointReference() {
        removeAttribute( BpelAttributes.ENDPOINT_REFERENCE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.From#removeFromChild()
     */
    public void removeFromChild() {
        removeChild( FromChild.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.From#setFromChild(org.netbeans.modules.bpel.model.api.FromChild)
     */
    public void setFromChild( FromChild child ) {
        setChild( child , FromChild.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.From#getLiteral()
     */
    public FromChild getFromChild() {
        return getChild( FromChild.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return From.class;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.ExtensibleElementsImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element ) {
        if ( BpelElements.LITERAL.getName().equals( element.getLocalName()) ) {
            return new LiteralImpl( getModel() , element );
        }
        if ( BpelElements.QUERY.getName().equals( element.getLocalName()) ) {
            return new QueryImpl( getModel() , element );
        }
        return super.create(element);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 1];
            System.arraycopy( attr , 0 , ret , 1 , attr.length );
            ret[ 0 ] = BpelAttributes.ENDPOINT_REFERENCE;
            myAttributes.compareAndSet( null , ret);
        }
        return myAttributes.get();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( getChildType( entity ).equals( Literal.class) ||
                getChildType( entity ).equals( Query.class) )
        {
            return Multiplicity.SINGLE;
        }
        return super.getMultiplicity(entity);
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();

}
