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

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CompletionCondition;
import org.netbeans.modules.bpel.model.api.FinalCounterValue;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class ForEachImpl extends ActivityImpl implements ForEach {
    
    static final String UNSAIGNED_INT = "unsignedInt";

    ForEachImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    ForEachImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.FOR_EACH.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ForEachIterator#getStartCounterValue()
     */
    public StartCounterValue getStartCounterValue() {
        return getChild( StartCounterValue.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ForEachIterator#setStartCounterValue(org.netbeans.modules.soa.model.bpel20.api.StartCounterValue)
     */
    public void setStartCounterValue( StartCounterValue expression ) {
        setChild( expression , StartCounterValue.class , 
                BpelTypesEnum.FINAL_COUNTER_VALUE,
                BpelTypesEnum.COMPLETION_CONDITION,
                BpelTypesEnum.SCOPE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ForEachIterator#getFinalCounterValue()
     */
    public FinalCounterValue getFinalCounterValue() {
        return getChild( FinalCounterValue.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ForEachIterator#setFinalCounterValue(org.netbeans.modules.soa.model.bpel20.api.FinalCounterValue)
     */
    public void setFinalCounterValue( FinalCounterValue expression ) {
        setChild( expression , FinalCounterValue.class,
                BpelTypesEnum.COMPLETION_CONDITION,
                BpelTypesEnum.SCOPE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ForEach#getCompletionCondition()
     */
    public CompletionCondition getCompletionCondition() {
        return getChild( CompletionCondition.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ForEach#setCompletionCondition(org.netbeans.modules.soa.model.bpel20.api.CompletionCondition)
     */
    public void setCompletionCondition( CompletionCondition condition ) {
        setChild( condition , CompletionCondition.class , BpelTypesEnum.SCOPE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ForEach#removeCompletionCondition()
     */
    public void removeCompletionCondition() {
        removeChild( CompletionCondition.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ForEach#getCounterName()
     */
    public String getCounterName() {
        return getAttribute( BpelAttributes.COUNTER_NAME );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ForEach#setCounterName(java.lang.String)
     */
    public void setCounterName( String value ) throws VetoException {
        setBpelAttribute( BpelAttributes.COUNTER_NAME , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ForEach#getParallel()
     */
    public TBoolean getParallel() {
        return getBooleanAttribute( BpelAttributes.PARALLEL );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ForEach#setParallel(org.netbeans.modules.soa.model.bpel20.api.support.TBoolean)
     */
    public void setParallel( TBoolean value ) {
        setBpelAttribute( BpelAttributes.PARALLEL , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {        
        return ForEach.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ScopeHolder#getScope()
     */
    public Scope getScope() {
        return getChild( Scope.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ScopeHolder#setScope(org.netbeans.modules.soa.model.bpel20.api.Scope)
     */
    public void setScope( Scope scope ) {
        setChild( scope , Scope.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ScopeHolder#removeScope()
     */
    public void removeScope() {
        removeChild( Scope.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableDeclaration#getMessageType()
     */
    public WSDLReference<Message> getMessageType() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableDeclaration#getElement()
     */
    public SchemaReference<GlobalElement> getElement() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableDeclaration#getType()
     */
    public SchemaReference<GlobalType> getType() {
        if ( getCounterName() != null ) {
            return getUnsignedIntRef();
        }
        else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableDeclaration#getVariableName()
     */
    public String getVariableName() {
        return getCounterName();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.ActivityImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element ) {
        if ( BpelElements.SCOPE.getName().equals( element.getLocalName()) ){
            return new ScopeImpl( getModel() , element );
        }
        else if ( BpelElements.COMPLETION_CONDITION.getName().
                equals(element.getLocalName()))
        {
            return new CompletionConditionImpl( getModel() , element );
        }
        else if ( BpelElements.START_COUNTER_VALUE.getName().equals( 
                element.getLocalName()) )
        {
            return new StartCounterValueImpl( getModel() , element );
        }
        else if ( BpelElements.FINAL_COUNTER_VALUE.getName().equals( 
                element.getLocalName()) )
        {
            return new FinalCounterValueImpl( getModel() , element );
        }
        return super.create(element);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 2];
            System.arraycopy( attr , 0 , ret , 2 , attr.length );
            ret[ 0 ] = BpelAttributes.PARALLEL;
            ret[ 1 ] = BpelAttributes.COUNTER_NAME;
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
        if ( getChildType( entity).equals( Scope.class) ||
                getChildType( entity).equals( StartCounterValue.class))
        {
            return Multiplicity.SINGLE;
        }
        if ( getChildType( entity).equals( FinalCounterValue.class) ||
                getChildType( entity).equals( CompletionCondition.class))
        {
            return Multiplicity.SINGLE;
        }
        return super.getMultiplicity(entity);
    }
    
    private SchemaReference<GlobalType> getUnsignedIntRef() {
        assert LazyHolder.myUnsignedType!= null;
        return createSchemaReference( LazyHolder.myUnsignedType, GlobalType.class);
    }
    
    private static class LazyHolder {
        static {
            Collection<GlobalSimpleType> collection = 
                SchemaModelFactory.getDefault().getPrimitiveTypesModel().getSchema().
                    getSimpleTypes();
            for (GlobalSimpleType type : collection) {
                if ( UNSAIGNED_INT.equals(type.getName())) {
                    myUnsignedType = type;
                    break;
                }
            }
        }
        
        private static GlobalType myUnsignedType;
    }
    
    private static AtomicReference<Attribute[]> myAttributes =
        new AtomicReference<Attribute[]>();
        
}
