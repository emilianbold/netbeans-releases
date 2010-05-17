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
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.Pattern;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class PatternedCorrelationImpl extends CorrelationImpl implements
        PatternedCorrelation
{

    PatternedCorrelationImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    PatternedCorrelationImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.CORRELATION_WITH_PATTERN.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelation#getPattern()
     */
    public Pattern getPattern() {
        readLock();
        try {
            String str = getAttribute(BpelAttributes.PATTERN);
            return Pattern.forString(str);
        }
        finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelation#setPattern(org.netbeans.modules.soa.model.bpel20.api.support.Pattern)
     */
    public void setPattern( Pattern value ) {
        setBpelAttribute(BpelAttributes.PATTERN, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelation#removePattern()
     */
    public void removePattern() {
        removeAttribute(BpelAttributes.PATTERN);
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit((PatternedCorrelation) this);
    }

    @Override
    public Class<? extends BpelEntity> getElementType()
    {
        return PatternedCorrelation.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if (myAttributes.get() == null) {
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[attr.length + 1];
            System.arraycopy(attr, 0, ret, 1, attr.length);
            ret[0] = BpelAttributes.PATTERN;
            myAttributes.compareAndSet(null, ret);
        }
        return myAttributes.get();
    }

    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();
}
