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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.FromPartContainer;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer;
import org.netbeans.modules.bpel.model.api.ToPartContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class InvokeImpl extends InvokeReceiveReplyCommonImpl implements Invoke {



    InvokeImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    InvokeImpl( BpelBuilderImpl builder) {
        super(builder, BpelElements.INVOKE.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Invoke#getPatternedCorrelationContainer()
     */
    public PatternedCorrelationContainer getPatternedCorrelationContainer() {
        return getChild( PatternedCorrelationContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Invoke#setPatternedCorrelationContainer(org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelationContainer)
     */
    public void setPatternedCorrelationContainer(
            PatternedCorrelationContainer value )
    {
        setChild( value , PatternedCorrelationContainer.class ,
                BpelTypesEnum.CATCH,
                BpelTypesEnum.CATCH_ALL,
                BpelTypesEnum.COMPENSATION_HANDLER,
                BpelTypesEnum.TO_PARTS,
                BpelTypesEnum.FROM_PARTS
                );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Invoke#removePatternedCorrelationContainer()
     */
    public void removePatternedCorrelationContainer() {
        removeChild( PatternedCorrelationContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Invoke#getInputVariable()
     */
    public BpelReference<VariableDeclaration> getInputVariable() {
        return getBpelReference( 
                BpelAttributes.INPUT_VARIABLE , VariableDeclaration.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Invoke#setInputVariable(org.netbeans.modules.soa.model.bpel20.references.VariableReference)
     */
    public void setInputVariable( BpelReference<VariableDeclaration> value ) {
        setBpelReference( BpelAttributes.INPUT_VARIABLE , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Invoke#removeInputVariable()
     */
    public void removeInputVariable() {
        removeReference( BpelAttributes.INPUT_VARIABLE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Invoke#getOutputVariable()
     */
    public BpelReference<VariableDeclaration> getOutputVariable() {
        return getBpelReference( 
                BpelAttributes.OUTPUT_VARIABLE , VariableDeclaration.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Invoke#setOutputVariable(org.netbeans.modules.soa.model.bpel20.references.VariableReference)
     */
    public void setOutputVariable( BpelReference<VariableDeclaration> value ) {
        setBpelReference( BpelAttributes.OUTPUT_VARIABLE , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Invoke#removeOutputVariable()
     */
    public void removeOutputVariable() {
        removeReference( BpelAttributes.OUTPUT_VARIABLE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Invoke.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#getCatches()
     */
    public Catch[] getCatches() {
        readLock();
        try {
            List<Catch> list = getChildren(Catch.class);
            return list.toArray(new Catch[list.size()]);
        }
        finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#getCatch(int)
     */
    public Catch getCatch( int i ) {
        return getChild(Catch.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#removeCatch(int)
     */
    public void removeCatch( int i ) {
        removeChild(Catch.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#setCatches(org.netbeans.modules.soa.model.bpel20.api.Catch[])
     */
    public void setCatches( Catch[] catches ) {
        setArrayBefore(catches, Catch.class, BpelTypesEnum.CATCH_ALL,
                BpelTypesEnum.COMPENSATION_HANDLER ,
                BpelTypesEnum.TO_PARTS, 
                BpelTypesEnum.FROM_PARTS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#setCatch(org.netbeans.modules.soa.model.bpel20.api.Catch,
     *      int)
     */
    public void setCatch( Catch catc, int i ) {
        setChildAtIndex(catc, Catch.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#addCatch(org.netbeans.modules.soa.model.bpel20.api.Catch)
     */
    public void addCatch( Catch catc ) {
        addChildBefore(catc, Catch.class, BpelTypesEnum.CATCH_ALL,
                BpelTypesEnum.COMPENSATION_HANDLER ,
                BpelTypesEnum.TO_PARTS, 
                BpelTypesEnum.FROM_PARTS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#insertCatch(org.netbeans.modules.soa.model.bpel20.api.Catch,
     *      int)
     */
    public void insertCatch( Catch catc, int i ) {
        insertAtIndex(catc, Catch.class, i , BpelTypesEnum.CATCH_ALL,
                BpelTypesEnum.COMPENSATION_HANDLER ,
                BpelTypesEnum.TO_PARTS, 
                BpelTypesEnum.FROM_PARTS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#getCatchAll()
     */
    public CatchAll getCatchAll() {
        return getChild( CatchAllImpl.class );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#setCatchAll(org.netbeans.modules.soa.model.bpel20.CatchAll)
     */
    public void setCatchAll( CatchAll value ) {
        setChild( value, CatchAll.class , 
                BpelTypesEnum.COMPENSATION_HANDLER,
                BpelTypesEnum.TO_PARTS,
                BpelTypesEnum.FROM_PARTS );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#removeCatchAll()
     */
    public void removeCatchAll() {
        removeChild(CatchAll.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.FaultHandlers#sizeOfCathes()
     */
    public int sizeOfCathes() {
        readLock();
        try {
            return getChildren(Catch.class).size();
        }
        finally {
            readUnlock();
        }
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CompensationHandlerHolder#getCompensationHandler()
     */
    public CompensationHandler getCompensationHandler() {
        return getChild( CompensationHandler.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CompensationHandlerHolder#setCompensationHandler(org.netbeans.modules.soa.model.bpel20.api.CompensationHandler)
     */
    public void setCompensationHandler( CompensationHandler value ) {
        setChild( value , CompensationHandler.class , 
                BpelTypesEnum.TO_PARTS,
                BpelTypesEnum.FROM_PARTS );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CompensationHandlerHolder#removeCompensationHandler()
     */
    public void removeCompensationHandler() {
        removeChild( CompensationHandler.class );
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
        setChild( value , ToPartContainer.class, BpelTypesEnum.FROM_PARTS );
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
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences(){
        Reference[] refs = super.getReferences();
        Reference[] ret = new Reference[ refs.length + 2];
        System.arraycopy( refs , 0, ret , 2 , refs.length );
        ret[0] = getInputVariable();
        ret[1] = getOutputVariable();
        return ret;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ){
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.ActivityImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element ) {
        if ( BpelElements.FROM_PARTS.getName().equals( element.getLocalName()) ){
            return new FromPartConainerImpl( getModel() , element );
        }
        else if ( BpelElements.TO_PARTS.getName().equals( element.getLocalName())) {
            return new ToPartContainerImpl( getModel() , element );
        }
        else if ( BpelElements.CORRELATIONS_WITH_PATTERN.getName().
                equals( element.getLocalName()) )
        {
            return new PatternedCorrelationContainerImpl( getModel() , element ); 
        }
        else if ( BpelElements.COMPENSATION_HANDLER.getName().equals( 
                element.getLocalName() ) )
        {
            return new CompensationHandlerImpl( getModel() , element );
        }
        else if ( BpelElements.CATCH.getName().equals(element.getLocalName())) {
            return new CatchImpl(getModel(), element);
        }
        else if (BpelElements.CATCH_ALL.getName().equals(element
                .getLocalName()))
        {
            return new CatchAllImpl(getModel(), element);
        }
        return super.create(element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret= new Attribute[ attr.length + 2];
            System.arraycopy( attr , 0 , ret , 2 , attr.length );
            ret[ 0 ] = BpelAttributes.INPUT_VARIABLE;
            ret[ 1 ] = BpelAttributes.OUTPUT_VARIABLE;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( getChildType( entity ).equals( CatchAll.class) 
                || getChildType( entity ).equals( 
                        PatternedCorrelationContainer.class)
                || getChildType( entity ).equals( CompensationHandler.class ))
        {
            return Multiplicity.SINGLE;
        }
        else if ( getChildType( entity ).equals( FromPartContainer.class)
                || getChildType( entity ).equals( ToPartContainer.class) )
        {
            return Multiplicity.SINGLE;
        }
        return super.getMultiplicity(entity);
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();

}
