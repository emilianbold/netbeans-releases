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
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class OnEventImpl extends OnMessageCommonImpl implements OnEvent {

    OnEventImpl( BpelBuilderImpl builder) {
        super(builder, BpelElements.ON_EVENT.getName() );
    }

    OnEventImpl( BpelModelImpl model, Element element ) {
        super( model , element );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.MessageTypeSpec#getMessageType()
     */
    public WSDLReference<Message> getMessageType() {
        return getWSDLReference( BpelAttributes.MESSAGE_TYPE , Message.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.MessageTypeSpec#setMessageType(javax.xml.namespace.QName)
     */
    public void setMessageType( WSDLReference<Message> value ) {
        setWSDLReference( BpelAttributes.MESSAGE_TYPE , value );
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
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return OnEvent.class;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Variable#getElement()
     */
    public SchemaReference<GlobalElement> getElement() {
        return getSchemaReference( BpelAttributes.ELEMENT ,
                GlobalElement.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.OnEvent#getVariable()
     */
    public String getVariable() {
        return getAttribute( BpelAttributes.VARIABLE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.OnEvent#removeVariable()
     */
    public void removeVariable() {
        removeAttribute( BpelAttributes.VARIABLE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.OnEvent#setVariable(java.lang.String)
     */
    public void setVariable( String value ) throws VetoException {
        setBpelAttribute( BpelAttributes.VARIABLE , value );
    }
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableDeclaration#getVariableName()
     */
    public String getVariableName() {
        return getVariable();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableDeclaration#getType()
     */
    public SchemaReference<GlobalType> getType() {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Variable#setElement(org.netbeans.modules.soa.model.bpel20.references.SchemaReference)
     */
    public void setElement( SchemaReference<GlobalElement> value ) {
        setSchemaReference( BpelAttributes.ELEMENT, value  );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Variable#removeElement()
     */
    public void removeElement() {
        removeReference( BpelAttributes.ELEMENT );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences(){
        Reference[] refs = super.getReferences();
        Reference[] ret = new Reference[ refs.length +1 ];
        System.arraycopy( refs , 0 , ret, 1 , refs.length );
        ret[0]=getMessageType();
        return ret ;
    }

     /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
       visitor.visit( this ); 
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.MessageTypeReference#removeMessageType()
     */
    public void removeMessageType() {
        removeReference( BpelAttributes.MESSAGE_TYPE );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.ExtensibleElementsImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element ) {
        if ( BpelElements.SCOPE.getName().equals( element.getLocalName()) ){
            return new ScopeImpl( getModel() , element );
        }
        return super.create(element);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( getChildType( entity ).equals( Scope.class) ) {
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
            Attribute[] ret = new Attribute[ attr.length + 2];
            System.arraycopy( attr , 0 , ret , 2 , attr.length );
            ret[ 0 ] = BpelAttributes.MESSAGE_TYPE;
            ret[ 1 ] = BpelAttributes.VARIABLE;
            myAttributes.compareAndSet( null , ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();

}
