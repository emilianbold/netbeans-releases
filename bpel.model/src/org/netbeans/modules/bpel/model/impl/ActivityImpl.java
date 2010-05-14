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

import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.SourceContainer;
import org.netbeans.modules.bpel.model.api.TargetContainer;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.xam.AfterImport;
import org.netbeans.modules.bpel.model.xam.AfterSources;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public abstract class ActivityImpl extends NamedElementImpl implements 
    Activity, AfterImport, AfterSources
{

    ActivityImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    ActivityImpl( BpelBuilderImpl builder, String tagName ) {
        super(builder, tagName);
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
        setChild( source , SourceContainer.class , BpelTypesEnum.AFTER_SOURCES ); 
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Activity#setTargetContainer(org.netbeans.modules.soa.model.bpel20.api.TargetContainer)
     */
    public void setTargetContainer( TargetContainer target ) {
        setChild( target , TargetContainer.class , BpelTypesEnum.AFTER_TARGETS ); 
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.JoinFailureSuppressor#getSuppressJoinFailure()
     */
    public TBoolean getSuppressJoinFailure() {
        return getBooleanAttribute( BpelAttributes.SUPPRESS_JOIN_FAILURE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.JoinFailureSuppressor#removeSuppressJoinFailure()
     */
    public void removeSuppressJoinFailure() {
        removeAttribute( BpelAttributes.SUPPRESS_JOIN_FAILURE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.JoinFailureSuppressor#setSuppressJoinFailure(org.netbeans.modules.soa.model.bpel20.api.support.TBoolean)
     */
    public void setSuppressJoinFailure( TBoolean value ) {
        setBpelAttribute( BpelAttributes.SUPPRESS_JOIN_FAILURE , value );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.Activity#removeName()
     */
    public void removeName() {
        removeAttribute( BpelAttributes.NAME );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.ExtensibleElementsImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        if ( BpelElements.SOURCES.getName().equals( element.getLocalName())){
            return new SourceContainerImpl( getModel(), element );
        }
        else if ( BpelElements.TARGETS.getName().equals( element.getLocalName())){
            return new TargetContainerImpl( getModel(), element );
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
            ret[ 0 ] = BpelAttributes.SUPPRESS_JOIN_FAILURE;
            myAttributes.compareAndSet( null , attr );
        }
        return myAttributes.get();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( getChildType( entity).equals(SourceContainer.class ) 
                || getChildType( entity).equals(TargetContainer.class ))
        {
            return Multiplicity.SINGLE;
        }
        return super.getMultiplicity(entity);
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();
    
}
