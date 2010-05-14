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

import java.util.List;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.AfterSources;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class PatternedCorrelationContainerImpl extends ExtensibleElementsImpl
        implements PatternedCorrelationContainer, AfterSources
{

    PatternedCorrelationContainerImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    PatternedCorrelationContainerImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.CORRELATIONS_WITH_PATTERN.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelationContainer#getPatternedCorrelations()
     */
    public PatternedCorrelation[] getPatternedCorrelations() {
        readLock();
        try {
            List<PatternedCorrelation> list = getChildren(
                    PatternedCorrelation.class);
            return list.toArray(new PatternedCorrelation[list.size()]);
        }
        finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelationContainer#getPatternedCorrelation(int)
     */
    public PatternedCorrelation getPatternedCorrelation( int i ) {
        return getChild(PatternedCorrelation.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelationContainer#setPatternedCorrelations(org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelation[])
     */
    public void setPatternedCorrelations( PatternedCorrelation[] correlations )
    {
        setArrayBefore(correlations, PatternedCorrelation.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelationContainer#setPatternedCorrelation(org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelation,
     *      int)
     */
    public void setPatternedCorrelation( PatternedCorrelation correlation, int i )
    {
        setChildAtIndex(correlation, PatternedCorrelation.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelationContainer#insertPatternedCorrelation(org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelation,
     *      int)
     */
    public void insertPatternedCorrelation( PatternedCorrelation correlation,
            int i )
    {
        insertAtIndex(correlation, PatternedCorrelation.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelationContainer#addPatternedCorrelation(org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelation)
     */
    public void addPatternedCorrelation( PatternedCorrelation correlation ) {
        addChild(correlation, PatternedCorrelation.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelationContainer#removePatternedCorrelation(int)
     */
    public void removePatternedCorrelation( int i ) {
        removeChild(PatternedCorrelation.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PatternedCorrelationContainer#sizeOfPatternedCorrelation()
     */
    public int sizeOfPatternedCorrelation() {
        readLock();
        try {
            return getChildren(PatternedCorrelation.class).size();
        }
        finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return PatternedCorrelationContainer.class;
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.xdm.impl.BpelContainerImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        if (BpelElements.CORRELATION_WITH_PATTERN.getName().equals(
                element.getLocalName()))
        {
            return new PatternedCorrelationImpl(getModel(), element);
        }
        return super.create(element);
    }

}
