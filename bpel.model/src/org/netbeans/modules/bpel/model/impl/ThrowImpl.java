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
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
import org.netbeans.modules.bpel.model.api.support.Utils;

/**
 * @author ads
 */
public class ThrowImpl extends ActivityImpl implements Throw {

    ThrowImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    ThrowImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.THROW.getName() );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Throw#getFaultVariable()
     */
    public BpelReference<VariableDeclaration> getFaultVariable() {
        return getBpelReference( BpelAttributes.FAULT_VARIABLE_REF , // Fix for #IZ77797 
                VariableDeclaration.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Throw#setFaultVariable(org.netbeans.modules.soa.model.bpel20.references.VariableReference)
     */
    public void setFaultVariable( BpelReference<VariableDeclaration> variable ) {
        setBpelReference( BpelAttributes.FAULT_VARIABLE_REF , variable ); // Fix for #IZ77797
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Throw#removeFaultVariable()
     */
    public void removeFaultVariable() {
        removeReference( BpelAttributes.FAULT_VARIABLE_REF ); // Fix for #IZ77797
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Throw.class;
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
        assert value!=null;
        setBpelAttribute( BpelAttributes.FAULT_NAME, value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences() {
        return new Reference[] { getFaultVariable() };
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.FaultNameReference#isValid(javax.xml.namespace.QName)
     */
    public boolean isValid( QName name ) {
        if ( name == null ) {
            return false;
        }
        return Utils.validate(name);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 2];
            System.arraycopy( attr , 0 , ret , 2 , attr.length );
            ret[ 0 ] = BpelAttributes.FAULT_VARIABLE_REF;   // Fix for #IZ77797
            ret[ 1 ] = BpelAttributes.FAULT_NAME;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = new AtomicReference<Attribute[]>();
}
