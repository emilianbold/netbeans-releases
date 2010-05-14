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

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.Variable;
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
 */
public class VariableImpl extends FromHolderImpl implements Variable {


    VariableImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    VariableImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.VARIABLE.getName() );
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.NamedElement#getName()
     */
    public String getName() {
        return getAttribute(BpelAttributes.NAME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.NamedElement#setName(java.lang.String)
     */
    public void setName( String value ) throws VetoException {
        assert value != null;
        setBpelAttribute(BpelAttributes.NAME, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Variable.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Variable#getElement()
     */
    public SchemaReference<GlobalElement> getElement() {
        return getSchemaReference( BpelAttributes.ELEMENT ,
                GlobalElement.class );
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
     * @see org.netbeans.modules.soa.model.bpel20.api.Variable#removeType()
     */
    public void removeType() {
        removeReference( BpelAttributes.TYPE );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.MessageTypeReference#removeMessageType()
     */
    public void removeMessageType() {
        removeReference( BpelAttributes.MESSAGE_TYPE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Variable#removeFrom()
     */
    public void removeFrom() {
        removeChild( From.class );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.MessageTypeSpec#getMessageType()
     */
    public WSDLReference<Message> getMessageType() {
        return getWSDLReference( BpelAttributes.MESSAGE_TYPE , Message.class);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.MessageTypeSpec#setMessageType(org.netbeans.modules.soa.model.bpel20.references.WSDLReference)
     */
    public void setMessageType( WSDLReference<Message> value ) {
        setWSDLReference( BpelAttributes.MESSAGE_TYPE , value );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Variable#getType()
     */
    public SchemaReference<GlobalType> getType() {
        return getSchemaReference( BpelAttributes.TYPE , GlobalType.class );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Variable#setType(org.netbeans.modules.soa.model.bpel20.references.SchemaReference)
     */
    public void setType( SchemaReference<GlobalType> value ) {
        setSchemaReference( BpelAttributes.TYPE , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences() {
        return new Reference[]{ getMessageType() , getType() , getElement() };
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableDeclaration#getVariableName()
     */
    public String getVariableName() {
        return getName();
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit(this);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 4];
            System.arraycopy( attr , 0 , ret , 4 , attr.length );
            ret[ 0 ] = BpelAttributes.NAME;
            ret[ 1 ] = BpelAttributes.ELEMENT;
            ret[ 2 ] = BpelAttributes.MESSAGE_TYPE;
            ret[ 3 ] = BpelAttributes.TYPE;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();

}
