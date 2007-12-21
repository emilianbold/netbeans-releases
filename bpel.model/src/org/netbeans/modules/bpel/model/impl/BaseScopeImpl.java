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

import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public abstract class BaseScopeImpl extends ActivityHolderImpl implements
        BaseScope, NamedElement
{

    BaseScopeImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    BaseScopeImpl( BpelBuilderImpl builder, String tagName ) {
        super(builder, tagName);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#getExitOnStandardFault()
     */
    public TBoolean getExitOnStandardFault() {
        return getBooleanAttribute( BpelAttributes.EXIT_ON_STANDART_FAULT );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#removeExitOnStandardFault()
     */
    public void removeExitOnStandardFault() {
        removeAttribute( BpelAttributes.EXIT_ON_STANDART_FAULT );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#setExitOnStandardFault(org.netbeans.modules.soa.model.bpel20.api.support.TBoolean)
     */
    public void setExitOnStandardFault( TBoolean value ) {        
        setBpelAttribute( BpelAttributes.EXIT_ON_STANDART_FAULT , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#getPartnerLinkContainer()
     */
    public PartnerLinkContainer getPartnerLinkContainer() {
        return getChild( PartnerLinkContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#removePartnerLinkContainer()
     */
    public void removePartnerLinkContainer() {
        removeChild(PartnerLinkContainer.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#setPartnerLinkContainer(org.netbeans.modules.soa.model.bpel20.api.PartnerLinkContainer)
     */
    public void setPartnerLinkContainer( PartnerLinkContainer value ) {
        setChild(value, PartnerLinkContainer.class,
                BpelTypesEnum.ACTIVITIES_GROUP,
                BpelTypesEnum.MESSAGE_EXCHANGE_CONTAINER,
                BpelTypesEnum.VARIABLE_CONTAINER,
                BpelTypesEnum.CORRELATION_SET_CONTAINER,
                BpelTypesEnum.FAULT_HANDLERS,
                BpelTypesEnum.COMPENSATION_HANDLER,
                BpelTypesEnum.EVENT_HANDLERS,
                BpelTypesEnum.TERMINATION_HANDLER
                );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#getVariableContainer()
     */
    public VariableContainer getVariableContainer() {
        return getChild(VariableContainer.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#setVariableContainer(org.netbeans.modules.soa.model.bpel20.api.VariableContainer)
     */
    public void setVariableContainer( VariableContainer value ) {
        setChild(value, VariableContainer.class,
                BpelTypesEnum.ACTIVITIES_GROUP,
                BpelTypesEnum.CORRELATION_SET_CONTAINER,
                BpelTypesEnum.FAULT_HANDLERS,
                BpelTypesEnum.COMPENSATION_HANDLER,
                BpelTypesEnum.EVENT_HANDLERS,
                BpelTypesEnum.TERMINATION_HANDLER
                );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#getCorrelationSetContainer()
     */
    public CorrelationSetContainer getCorrelationSetContainer() {
        return getChild(CorrelationSetContainer.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#setCorrelationSetContainer(org.netbeans.modules.soa.model.bpel20.api.CorrelationSetContainer)
     */
    public void setCorrelationSetContainer( CorrelationSetContainer value ) {
        setChild(value, CorrelationSetContainer.class,
                BpelTypesEnum.ACTIVITIES_GROUP, BpelTypesEnum.FAULT_HANDLERS,
                BpelTypesEnum.COMPENSATION_HANDLER,
                BpelTypesEnum.EVENT_HANDLERS,
                BpelTypesEnum.TERMINATION_HANDLER
                );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#getFaultHandlers()
     */
    public FaultHandlers getFaultHandlers() {
        return getChild(FaultHandlers.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#setFaultHandlers(org.netbeans.modules.soa.model.bpel20.api.FaultHandlers)
     */
    public void setFaultHandlers( FaultHandlers value ) {
        setChild(value, FaultHandlers.class, BpelTypesEnum.ACTIVITIES_GROUP,
                BpelTypesEnum.COMPENSATION_HANDLER,
                BpelTypesEnum.EVENT_HANDLERS, BpelTypesEnum.TERMINATION_HANDLER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#getEventHandlers()
     */
    public EventHandlers getEventHandlers() {
        return getChild(EventHandlers.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#setEventHandlers(org.netbeans.modules.soa.model.bpel20.api.EventHandlers)
     */
    public void setEventHandlers( EventHandlers value ) {
        setChild(value, EventHandlers.class, BpelTypesEnum.ACTIVITIES_GROUP);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#removeVariableContainer()
     */
    public void removeVariableContainer() {
        removeChild(VariableContainer.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#removeFaultHandlers()
     */
    public void removeFaultHandlers() {
        removeChild(FaultHandlers.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#removeEventHandlers()
     */
    public void removeEventHandlers() {
        removeChild(EventHandlers.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BaseScope#removeCorrelationSetContainer()
     */
    public void removeCorrelationSetContainer() {
        removeChild(CorrelationSetContainer.class);
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.NamedElement#getName()
     */
    public String getName() {
        return getAttribute(BpelAttributes.NAME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.NamedElement#setName(java.lang.String)
     */
    public void setName( String value ) throws VetoException {
        assert value != null;
        setBpelAttribute(BpelAttributes.NAME, value);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.JoinFailureSuppressor#getSuppressJoinFailure()
     */
    public TBoolean getSuppressJoinFailure() {
        return getBooleanAttribute( BpelAttributes.SUPPRESS_JOIN_FAILURE );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.JoinFailureSuppressor#setSuppressJoinFailure(boolean)
     */
    public void setSuppressJoinFailure( TBoolean value ) {
        setBpelAttribute( BpelAttributes.SUPPRESS_JOIN_FAILURE, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.JoinFailureSuppressor#removeSuppressJoinFailure()
     */
    public void removeSuppressJoinFailure() {
        removeAttribute(BpelAttributes.SUPPRESS_JOIN_FAILURE);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BaseScope#getMessageExchangeContainer()
     */
    public MessageExchangeContainer getMessageExchangeContainer() {
        return getChild( MessageExchangeContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BaseScope#removeMessageExchangeContainer()
     */
    public void removeMessageExchangeContainer() {
        removeChild( MessageExchangeContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BaseScope#setMessageExchangeContainer(org.netbeans.modules.bpel.model.api.MessageExchangeContainer)
     */
    public void setMessageExchangeContainer( MessageExchangeContainer container ) {
        setChild( container, MessageExchangeContainer.class,
                BpelTypesEnum.ACTIVITIES_GROUP,
                BpelTypesEnum.VARIABLE_CONTAINER,
                BpelTypesEnum.CORRELATION_SET_CONTAINER,
                BpelTypesEnum.FAULT_HANDLERS,
                BpelTypesEnum.COMPENSATION_HANDLER,
                BpelTypesEnum.EVENT_HANDLERS,
                BpelTypesEnum.TERMINATION_HANDLER
                );        
    }

    @Override
    protected BpelEntity create( Element element )
    {
        assert element != null;
        if ( BpelElements.VARIABLES.getName().equals(element.getLocalName())) {
            return new VariableContainerImpl(getModel(), element);
        }
        else if ( BpelElements.FAULT_HANDLERS.getName().equals(
                element.getLocalName()))
        {
            return new FaultHandlersImpl(getModel(), element);
        }
        else if ( BpelElements.EVENT_HANDLERS.getName().equals(
                element.getLocalName()))
        {
            return new EventHandlersImpl(getModel(), element);
        }
        else if ( BpelElements.CORRELATION_SETS.getName().equals(element
                .getLocalName()))
        {
            return new CorrelationSetContainerImpl(getModel(), element);
        }
        else if ( BpelElements.PARTNERLINKS.getName().equals(
                element.getLocalName()))
        {
            return new PartnerLinkContainerImpl(getModel(), element);
        }
        else if ( BpelElements.MESSAGE_EXCHAGES.getName().equals( 
                element.getLocalName()) )
        {
            return new MessageExchangeContainerImpl( getModel(), element );
        }

        return super.create(element);

    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.ActivityHolderImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( getChildType( entity ).equals( PartnerLinkContainer.class) 
                || getChildType( entity ).equals( VariableContainer.class))
        {
            return Multiplicity.SINGLE;
        }
        if ( getChildType( entity ).equals( CorrelationSetContainer.class) 
                || getChildType( entity ).equals( FaultHandlers.class))
        {
            return Multiplicity.SINGLE;
        }
        if ( getChildType( entity ).equals( EventHandlers.class) 
                || getChildType( entity ).equals( MessageExchangeContainer.class))
        {
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
            ret[ 0 ] = BpelAttributes.NAME;
            ret[ 1 ] = BpelAttributes.EXIT_ON_STANDART_FAULT;
            ret[ 2 ] = BpelAttributes.SUPPRESS_JOIN_FAILURE;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();

}
