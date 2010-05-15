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
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class CorrelationImpl extends ExtensibleElementsImpl implements
        Correlation, ReferenceCollection
{

    CorrelationImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    CorrelationImpl( BpelBuilderImpl builder, String tagName ) {
        super(builder, tagName);
    }

    CorrelationImpl( BpelBuilderImpl builder ) {
        this(builder, BpelElements.CORRELATION.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Correlation#getInitiate()
     */
    public Initiate getInitiate() {
        readLock();
        try {
            String str = getAttribute(BpelAttributes.INITIATE);
            return Initiate.forString(str);
        }
        finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Correlation#setInitiate(boolean)
     */
    public void setInitiate( Initiate value ) {
        setBpelAttribute(BpelAttributes.INITIATE, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Correlation#removeInitiate()
     */
    public void removeInitiate() {
        removeAttribute(BpelAttributes.INITIATE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Correlation.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Correlation#getSet()
     */
    public BpelReference<CorrelationSet> getSet() {
        return getBpelReference(BpelAttributes.SET, CorrelationSet.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Correlation#setSet(org.netbeans.modules.soa.model.bpel20.references.BpelReference)
     */
    public void setSet( BpelReference<CorrelationSet> value ) {
        setBpelReference(BpelAttributes.SET, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences() {
        return new Reference[] { getSet() };
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if (myAttributes.get() == null) {
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[attr.length + 2];
            System.arraycopy(attr, 0, ret, 2, attr.length);
            ret[0] = BpelAttributes.INITIATE;
            ret[1] = BpelAttributes.SET;
            myAttributes.compareAndSet(null, ret);
        }
        return myAttributes.get();
    }

    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();
}
