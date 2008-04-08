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

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.SourceContainer;
import org.netbeans.modules.bpel.model.api.TargetContainer;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
import org.netbeans.modules.bpel.model.api.support.Utils;

/**
 * @author ads
 */
public class ScopeImpl extends BaseScopeImpl implements Scope {

    ScopeImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    ScopeImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.SCOPE.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Scope#getTerminationHandler()
     */
    public TerminationHandler getTerminationHandler() {
        return getChild(TerminationHandler.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Scope#setTerminationHandler(org.netbeans.modules.soa.model.bpel20.api.TerminationHandler)
     */
    public void setTerminationHandler( TerminationHandler handler ) {
        setChild( handler , TerminationHandler.class ,  
                BpelTypesEnum.EVENT_HANDLERS, 
                BpelTypesEnum.ACTIVITIES_GROUP );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Scope#removeTerminationHandler()
     */
    public void removeTerminationHandler() {
        removeChild( TerminationHandler.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Scope#getIsolated()
     */
    public TBoolean getIsolated() {
        return getBooleanAttribute(BpelAttributes.ISOLATED) ;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Scope#setIsolated(org.netbeans.modules.soa.model.bpel20.api.support.TBoolean)
     */
    public void setIsolated( TBoolean value ) {
        setBpelAttribute( BpelAttributes.ISOLATED , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Scope#removeIsolated()
     */
    public void removeIsolated() {
        removeAttribute( BpelAttributes.ISOLATED );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Activity#getSourceContainer()
     */
    public SourceContainer getSourceContainer() {
        return getChild( SourceContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Activity#getTargetContainer()
     */
    public TargetContainer getTargetContainer() {
        return getChild( TargetContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Activity#removeSourceContainer()
     */
    public void removeSourceContainer() {
        removeChild( SourceContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Activity#removeTargetContainer()
     */
    public void removeTargetContainer() {
        removeChild( TargetContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Activity#setSourceContainer(org.netbeans.modules.soa.model.bpel20.api.SourceContainer)
     */
    public void setSourceContainer( SourceContainer source ) {
        setChild( source , SourceContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Activity#setTargetContainer(org.netbeans.modules.soa.model.bpel20.api.TargetContainer)
     */
    public void setTargetContainer( TargetContainer target ) {
        setChild( target , TargetContainer.class , 
                BpelTypesEnum.SOURCE_CONTAINER );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CompensationHandlerHolder#getCompensationHandler()
     */
    public CompensationHandler getCompensationHandler() {
        return getChild(CompensationHandler.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CompensationHandlerHolder#setCompensationHandler(org.netbeans.modules.soa.model.bpel20.api.CompensationHandler)
     */
    public void setCompensationHandler( CompensationHandler value ) {
        setChild(value, CompensationHandler.class,
                BpelTypesEnum.ACTIVITIES_GROUP, BpelTypesEnum.EVENT_HANDLERS,
                BpelTypesEnum.TERMINATION_HANDLER );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CompensationHandlerHolder#removeCompensationHandler()
     */
    public void removeCompensationHandler() {
        removeChild(CompensationHandler.class);
    }

    public Class<? extends BpelEntity> getElementType()
    {
        return Scope.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.Activity#removeName()
     */
    public void removeName() {
        removeAttribute( BpelAttributes.NAME );
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit(this);
    }

    @Override
    protected BpelEntity create( Element element )
    {
        BpelEntity entity = Utils.createActivityGroup( getModel() , element );
        if ( entity!= null ){
            return entity;
        }
        if ( BpelElements.SOURCES.getName().equals( element.getLocalName())){
            return new SourceContainerImpl( getModel(), element );
        }
        else if ( BpelElements.TARGETS.getName().equals( element.getLocalName())){
            return new TargetContainerImpl( getModel(), element );
        }
        else if ( BpelElements.COMPENSATION_HANDLER.getName().equals( 
                element.getLocalName())){
            return new CompensationHandlerImpl( getModel(), element );
        }
        else if ( BpelElements.TERMINATION_HANDLER.getName().equals( 
                element.getLocalName()))
        {
            return new TerminationHandlerImpl( getModel(), element );
        }

        return super.create(element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelContainerImpl#getChildType(T)
     */
    @Override
    protected <T extends BpelEntity> Class<? extends BpelEntity> 
        getChildType( T entity )
    {       
        if ( entity instanceof ExtendableActivity ){
            return ExtendableActivity.class;
        }
        return super.getChildType(entity);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( getChildType( entity).equals(ExtendableActivity.class ) 
                || getChildType( entity).equals(TerminationHandler.class )
                || getChildType( entity).equals(CompensationHandler.class ))
        {
            return Multiplicity.SINGLE;
        }
        if ( getChildType( entity).equals(SourceContainer.class ) 
                || getChildType( entity).equals(TargetContainer.class ))
        {
            return Multiplicity.SINGLE;
        }

        return super.getMultiplicity(entity);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 1];
            System.arraycopy( attr , 0 , ret , 1 , attr.length );
            ret[ 0 ] = BpelAttributes.ISOLATED;
            myAttributes =ret;
        }
        return myAttributes;
    }
    
    private static Attribute[] myAttributes;
}
