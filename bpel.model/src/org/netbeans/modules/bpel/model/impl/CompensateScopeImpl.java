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
import org.netbeans.modules.bpel.model.api.CompensateScope;
import org.netbeans.modules.bpel.model.api.CompensationHandlerHolder;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class CompensateScopeImpl extends ActivityImpl implements
        CompensateScope
{

    CompensateScopeImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    CompensateScopeImpl( BpelBuilderImpl builder) {
        super(builder, BpelElements.COMPENSATE_SCOPE.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.CompensateScope#getTarget()
     */
    public BpelReference<CompensationHandlerHolder> getTarget() {
        return getBpelReference( BpelAttributes.TARGET , 
                CompensationHandlerHolder.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.CompensateScope#setTarget(org.netbeans.modules.bpel.model.api.references.BpelReference)
     */
    public void setTarget( BpelReference<CompensationHandlerHolder> ref ) {
        setBpelReference( BpelAttributes.TARGET , ref );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.CompensateScope#removeTarget()
     */
    public void removeTarget() {
        removeReference( BpelAttributes.TARGET );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return CompensateScope.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelEntity#accept(org.netbeans.modules.bpel.model.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences(){
        return new Reference[] { getTarget() } ;
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.ActivityImpl#getDomainAttributes()
     */
    @Override
    protected Attribute[] getDomainAttributes()
    {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 1];
            System.arraycopy( attr , 0 , ret , 1 , attr.length );
            ret[ 0 ] = BpelAttributes.TARGET;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }

    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();
}
