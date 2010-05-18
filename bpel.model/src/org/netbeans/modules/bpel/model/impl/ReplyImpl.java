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

import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.ToPartContainer;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class ReplyImpl extends ReplyReceiveIntersectImpl implements Reply {


    ReplyImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    
    ReplyImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.REPLY.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Catch#removeFaultName()
     */
    public void removeFaultName() {
        removeAttribute( BpelAttributes.FAULT_NAME );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultNameReference#getFaultName()
     */
    public QName getFaultName() {
        return getQNameAttribute( BpelAttributes.FAULT_NAME );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultNameReference#setFaultName(javax.xml.namespace.QName)
     */
    public void setFaultName( QName value ) throws VetoException {
        setBpelAttribute( BpelAttributes.FAULT_NAME, value );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.ToPartsHolder#getToPartContaner()
     */
    public ToPartContainer getToPartContaner() {
        return getChild( ToPartContainer.class );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.ToPartsHolder#removeToPartContainer()
     */
    public void removeToPartContainer() {
        removeChild( ToPartContainer.class );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.ToPartsHolder#setToPartContainer(org.netbeans.modules.bpel.model.api.ToPartContainer)
     */
    public void setToPartContainer( ToPartContainer value ) {
        setChild( value , ToPartContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Reply.class;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor )
    {
        visitor.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.InvokeReceiveReplyCommonImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        if ( BpelElements.TO_PARTS.getName().equals( element.getLocalName())) {
            return new ToPartContainerImpl( getModel() , element );
        }
        return super.create(element);
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.InvokeReceiveReplyCommonImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( entity.getElementType().equals( ToPartContainer.class )) {
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
            ret[ 0 ] = BpelAttributes.FAULT_NAME;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();

}
