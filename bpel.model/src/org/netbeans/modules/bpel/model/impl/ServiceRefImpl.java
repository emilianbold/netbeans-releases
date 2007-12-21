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
import org.netbeans.modules.bpel.model.api.ServiceRef;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class ServiceRefImpl extends BpelContainerImpl implements ServiceRef {

    ServiceRefImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    ServiceRefImpl( BpelBuilderImpl builder) {
        super( builder, BpelElements.SERVICE_REF );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.ServiceRef#getReferenceScheme()
     */
    public String getReferenceScheme() {
        return getAttribute( BpelAttributes.REFERENCE_SCHEME );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.ServiceRef#setReferenceScheme(java.lang.String)
     */
    public void setReferenceScheme( String value ) throws VetoException {
        setBpelAttribute( BpelAttributes.REFERENCE_SCHEME , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.ServiceRef#removeReferenceScheme()
     */
    public void removeReferenceScheme() {
        removeAttribute( BpelAttributes.REFERENCE_SCHEME );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return ServiceRef.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelEntity#accept(org.netbeans.modules.bpel.model.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelEntityImpl#getDomainAttributes()
     */
    @Override
    protected Attribute[] getDomainAttributes()
    {
        if ( myAttributes.get() == null ){
            Attribute[] ret = new Attribute[]{ BpelAttributes.REFERENCE_SCHEME };
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }

    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();
}
