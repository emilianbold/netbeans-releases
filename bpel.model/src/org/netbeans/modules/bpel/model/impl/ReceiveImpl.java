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
import org.netbeans.modules.bpel.model.api.FromPartContainer;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class ReceiveImpl extends ReplyReceiveIntersectImpl implements Receive {


    ReceiveImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    ReceiveImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.RECEIVE.getName() );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CreateInstanceActivity#getCreateInstance()
     */
    public TBoolean getCreateInstance() {
        return getBooleanAttribute( BpelAttributes.CREATE_INSTANCE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CreateInstanceActivity#setCreateInstance(org.netbeans.modules.soa.model.bpel20.api.support.TBoolean)
     */
    public void setCreateInstance( TBoolean value ) {
        setBpelAttribute( BpelAttributes.CREATE_INSTANCE , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CreateInstanceActivity#removeCreateInstance()
     */
    public void removeCreateInstance() {
        removeAttribute( BpelAttributes.CREATE_INSTANCE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.FromPartsHolder#getFromPartContaner()
     */
    public FromPartContainer getFromPartContaner() {
        return getChild( FromPartContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.FromPartsHolder#removeFromPartContainer()
     */
    public void removeFromPartContainer() {
        removeChild( FromPartContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.FromPartsHolder#setFromPartContainer(org.netbeans.modules.bpel.model.api.FromPartContainer)
     */
    public void setFromPartContainer( FromPartContainer value ) {
        setChild( value , FromPartContainer.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Receive.class;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.InvokeReceiveReplyCommonImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element ) {
        if ( BpelElements.FROM_PARTS.getName().equals( element.getLocalName()) ){
            return new FromPartConainerImpl( getModel() , element );
        }
        return super.create(element);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.InvokeReceiveReplyCommonImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( entity.getElementType().equals( FromPartContainer.class )) {
            return Multiplicity.SINGLE;
        }
        return super.getMultiplicity(entity);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 1];
            System.arraycopy( attr , 0 , ret , 1 , attr.length );
            ret[ 0 ] = BpelAttributes.CREATE_INSTANCE;
            myAttributes.compareAndSet( null,  ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();



}
