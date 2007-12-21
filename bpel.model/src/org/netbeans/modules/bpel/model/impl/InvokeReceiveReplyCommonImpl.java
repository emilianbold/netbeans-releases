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
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.PortTypeReference;
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
 * This class represent intersection functionality for Invoke, Reply, Receive. 
 */
public abstract class InvokeReceiveReplyCommonImpl extends ActivityImpl implements
        PartnerLinkReference, PortTypeReference,OperationReference
{


    InvokeReceiveReplyCommonImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    InvokeReceiveReplyCommonImpl( BpelBuilderImpl builder, String tagName ) {
        super(builder, tagName);
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
        setChild( value , CorrelationContainer.class , 
                BpelTypesEnum.CATCH,
                BpelTypesEnum.CATCH_ALL,
                BpelTypesEnum.COMPENSATION_HANDLER,
                BpelTypesEnum.TO_PARTS,
                BpelTypesEnum.FROM_PARTS
                );
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
        return getWSDLReference( BpelAttributes.PORT_TYPE , PortType.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PortTypeReference#setPortType(org.netbeans.modules.soa.model.bpel20.references.WSDLReference)
     */
    public void setPortType( WSDLReference<PortType> value ) {
        setWSDLReference( BpelAttributes.PORT_TYPE , value );
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
        return getWSDLReference( BpelAttributes.OPERATION , Operation.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.OperationReference#setOperation(org.netbeans.modules.soa.model.bpel20.api.support.WSDLReference)
     */
    public void setOperation( WSDLReference<Operation> value ) {
        setWSDLReference( BpelAttributes.OPERATION , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences(){
        return new Reference[] { getOperation() , getPortType() , getPartnerLink()};
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.ActivityImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        if ( BpelElements.CORRELATIONS.getName().equals( element.getLocalName()) ){
            return new CorrelationContainerImpl( getModel() , element );
        }
        return super.create(element);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( getChildType( entity ).equals( CorrelationContainer.class) ) {
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
            Attribute[] ret = new Attribute[ attr.length + 3];
            System.arraycopy( attr , 0 , ret , 3 , attr.length );
            ret[ 0 ] = BpelAttributes.PARTNER_LINK;
            ret[ 1 ] = BpelAttributes.PORT_TYPE;
            ret[ 2 ] = BpelAttributes.OPERATION;
            myAttributes.compareAndSet( null , ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();
}
