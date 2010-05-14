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
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.FromPartContainer;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.OnMessageCommon;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public abstract class OnMessageCommonImpl extends ExtensibleElementsImpl implements
        OnMessageCommon
{

    OnMessageCommonImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    OnMessageCommonImpl( BpelBuilderImpl builder, String tagName ) {
        super(builder, tagName);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLinkReference#getPartnerLink()
     */
    public BpelReference<PartnerLink> getPartnerLink() {
        return getBpelReference( BpelAttributes.PARTNER_LINK , 
                PartnerLink.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLinkReference#setPartnerLink(org.netbeans.modules.soa.model.bpel20.references.BpelReference)
     */
    public void setPartnerLink( BpelReference<PartnerLink> value ) {
        setBpelReference( BpelAttributes.PARTNER_LINK , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PortTypeReference#getPortType()
     */
    public WSDLReference<PortType> getPortType() {
        return getWSDLReference( BpelAttributes.PORT_TYPE, PortType.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PortTypeReference#setPortType(org.netbeans.modules.soa.model.bpel20.references.WSDLReference)
     */
    public void setPortType( WSDLReference<PortType> value ) {
        setWSDLReference( BpelAttributes.PORT_TYPE, value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PortTypeReference#removePortType()
     */
    public void removePortType() {
        removeReference( BpelAttributes.PORT_TYPE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.OperationReference#getOperation()
     */
    public WSDLReference<Operation> getOperation() {
        return getWSDLReference( BpelAttributes.OPERATION , Operation.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.OperationReference#setOperation(org.netbeans.modules.soa.model.bpel20.api.support.WSDLReference)
     */
    public void setOperation( WSDLReference<Operation> value ) {
        setWSDLReference( BpelAttributes.OPERATION , value );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.MessageExchangeSpec#getMessageExchange()
     */
    public BpelReference<MessageExchange> getMessageExchange() {
        return getBpelReference( BpelAttributes.MESSAGE_EXCHANGE , 
                MessageExchange.class);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.MessageExchangeSpec#setMessageExchange(java.lang.String)
     */
    public void setMessageExchange( BpelReference<MessageExchange> ref ) {
        setBpelReference( BpelAttributes.MESSAGE_EXCHANGE , ref );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.MessageExchangeSpec#removeMessageExchange()
     */
    public void removeMessageExchange() {
        removeReference( BpelAttributes.MESSAGE_EXCHANGE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.FromPartContainer#getFromParts()
     */
    public FromPart[] getFromParts() {
        readLock();
        try {
            List<FromPart> list = getChildren( FromPart.class );
            return list.toArray( new FromPart[ list.size() ] );
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.FromPartContainer#getFromPart(int)
     */
    public FromPart getFromPart( int i ) {
        return getChild( FromPart.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.FromPartContainer#setFromPart(org.netbeans.modules.soa.model.bpel20.api.FromPart, int)
     */
    public void setFromPart( FromPart part, int i ) {
        setChildAtIndex( part , FromPart.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.FromPartContainer#insertFromPart(org.netbeans.modules.soa.model.bpel20.api.FromPart, int)
     */
    public void insertFromPart( FromPart part, int i ) {
        insertAtIndex( part , FromPart.class , i , 
                BpelTypesEnum.ACTIVITIES_GROUP);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.FromPartContainer#addFromPart(org.netbeans.modules.soa.model.bpel20.api.FromPart)
     */
    public void addFromPart( FromPart part ) {
        addChildBefore( part, FromPart.class , BpelTypesEnum.ACTIVITIES_GROUP );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.FromPartContainer#setFromParts(org.netbeans.modules.soa.model.bpel20.api.FromPart[])
     */
    public void setFromParts( FromPart[] parts ) {
        setArrayBefore( parts, FromPart.class , 
                BpelTypesEnum.ACTIVITIES_GROUP );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.FromPartContainer#sizeOfFromParts()
     */
    public int sizeOfFromParts() {
        readLock();
        try {
            return getChildren( FromPart.class ).size();
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.FromPartContainer#removeFromPart(int)
     */
    public void removeFromPart( int i ) {
        removeChild( FromPart.class );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationsHolder#getCorrelationContainer()
     */
    public CorrelationContainer getCorrelationContainer() {
        return getChild( CorrelationContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationsHolder#removeCorrelationContainer()
     */
    public void removeCorrelationContainer() {
        removeChild( CorrelationContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationsHolder#setCorrelationContainer(org.netbeans.modules.soa.model.bpel20.api.CorrelationContainer)
     */
    public void setCorrelationContainer( CorrelationContainer value ) {
        setChild( value , CorrelationContainer.class , BpelTypesEnum.FROM_PARTS,
                BpelTypesEnum.ACTIVITIES_GROUP );
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
        setChild( value, FromPartContainer.class, BpelTypesEnum.ACTIVITIES_GROUP );
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences(){
        return new Reference[] { getPartnerLink() , getPortType() , 
                getOperation(), getMessageExchange() } ;    // Fix for #81457
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.InvokeReceiveReplyCommonImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        if ( BpelElements.FROM_PARTS.getName().equals( element.getLocalName()) ){
            return new FromPartConainerImpl( getModel() , element );
        }
        else if ( BpelElements.CORRELATIONS.getName().equals( 
                element.getLocalName()) )
        {
            return new CorrelationContainerImpl( getModel() , element );
        }
        return super.create(element);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity ) {
        if ( getChildType( entity ).equals( CorrelationContainer.class)  ) {
            return Multiplicity.SINGLE;
        }
        else if ( getChildType( entity ).equals( FromPartContainer.class)) {
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
            Attribute[] ret = new Attribute[ attr.length + 4];
            System.arraycopy( attr , 0 , ret , 4 , attr.length );
            ret[ 0 ] = BpelAttributes.PARTNER_LINK;
            ret[ 1 ] = BpelAttributes.PORT_TYPE;
            ret[ 2 ] = BpelAttributes.OPERATION;
            ret[ 3 ] = BpelAttributes.MESSAGE_EXCHANGE;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = new AtomicReference<Attribute[]>();
}
