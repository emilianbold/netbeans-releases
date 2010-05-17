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

import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.AfterSources;
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
public class CatchImpl extends ActivityHolderImpl implements Catch, AfterSources
{


    CatchImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    CatchImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.CATCH.getName());
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Catch#getFaultMessageType()
     */
    public WSDLReference<Message> getFaultMessageType() {
        return getWSDLReference( BpelAttributes.FAULT_MESSAGE_TYPE, 
                Message.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Catch#setFaultMessageType(org.netbeans.modules.soa.model.bpel20.references.GlobalReference)
     */
    public void setFaultMessageType( WSDLReference<Message> message ) {
        setWSDLReference( BpelAttributes.FAULT_MESSAGE_TYPE,
                message );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Catch#removeFaultMessageType()
     */
    public void removeFaultMessageType() {
        removeReference( BpelAttributes.FAULT_MESSAGE_TYPE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Catch#getFaultElement()
     */
    public SchemaReference<GlobalElement> getFaultElement() {
        return getSchemaReference( BpelAttributes.FAULT_ELEMENT , 
                GlobalElement.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Catch#setFaultElement(org.netbeans.modules.soa.model.bpel20.references.GlobalReference)
     */
    public void setFaultElement( SchemaReference<GlobalElement> element ) {
        setSchemaReference( BpelAttributes.FAULT_ELEMENT , element );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Catch#removeFaultElement()
     */
    public void removeFaultElement() {
        removeReference( BpelAttributes.FAULT_ELEMENT );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Catch#setFaultVariable(org.netbeans.modules.soa.model.bpel20.references.FaultVariableReference)
     */
    public void setFaultVariable( String value ) throws VetoException {
        setBpelAttribute( BpelAttributes.FAULT_VARIABLE , value );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Catch.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Catch#removeFaultName()
     */
    public void removeFaultName() {
        removeAttribute( BpelAttributes.FAULT_NAME );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Catch#getFaultVariable()
     */
    public String getFaultVariable() {
        return getAttribute( BpelAttributes.FAULT_VARIABLE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Catch#removeFaultVariable()
     */
    public void removeFaultVariable() {
        removeAttribute( BpelAttributes.FAULT_VARIABLE );
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
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableDeclaration#getMessageType()
     */
    public WSDLReference<Message> getMessageType() {
        return getFaultMessageType();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableDeclaration#getElement()
     */
    public SchemaReference<GlobalElement> getElement() {
        return getFaultElement();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableDeclaration#getType()
     */
    public SchemaReference<GlobalType> getType() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableDeclaration#getVariableName()
     */
    public String getVariableName() {
        return getFaultVariable();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences(){
        return new Reference[]{ getFaultMessageType() , getFaultElement() };
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
            ret[ 0 ] = BpelAttributes.FAULT_VARIABLE;
            ret[ 1 ] = BpelAttributes.FAULT_MESSAGE_TYPE;
            ret[ 2 ] = BpelAttributes.FAULT_ELEMENT;
            ret[ 3 ] = BpelAttributes.FAULT_NAME;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes =
        new AtomicReference<Attribute[]>();

}
