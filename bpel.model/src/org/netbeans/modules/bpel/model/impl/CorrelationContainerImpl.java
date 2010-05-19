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
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.AfterSources;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class CorrelationContainerImpl extends ExtensibleElementsImpl implements
        CorrelationContainer, AfterSources
{

    CorrelationContainerImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    CorrelationContainerImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.CORRELATIONS.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationContainer#getCorrelations()
     */
    public Correlation[] getCorrelations() {
        readLock();
        try {
            List<Correlation> list = getChildren(Correlation.class);
            return list.toArray(new Correlation[list.size()]);
        }
        finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationContainer#getCorrelation(int)
     */
    public Correlation getCorrelation( int i ) {
        return getChild(Correlation.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationContainer#setCorrelations(org.netbeans.modules.soa.model.bpel20.api.Correlation[])
     */
    public void setCorrelations( Correlation[] correlations ) {
        setArrayBefore(correlations, Correlation.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationContainer#setCorrelation(org.netbeans.modules.soa.model.bpel20.api.Correlation,
     *      int)
     */
    public void setCorrelation( Correlation correlation, int i ) {
        setChildAtIndex(correlation, Correlation.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationContainer#insertCorrelation(org.netbeans.modules.soa.model.bpel20.api.Correlation,
     *      int)
     */
    public void insertCorrelation( Correlation correlation, int i ) {
        insertAtIndex(correlation, Correlation.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationContainer#addCorrelation(org.netbeans.modules.soa.model.bpel20.api.Correlation)
     */
    public void addCorrelation( Correlation correlation ) {
        addChild(correlation, Correlation.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationContainer#removeCorrelation(int)
     */
    public void removeCorrelation( int i ) {
        removeChild(Correlation.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationContainer#sizeOfCorrelations()
     */
    public int sizeOfCorrelations() {
        readLock();
        try {
            return getChildren(Correlation.class).size();
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
        return CorrelationContainer.class;
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
        if (BpelElements.CORRELATION.getName().equals(element.getLocalName())) {
            return new CorrelationImpl(getModel(), element);
        }
        return super.create(element);
    }

}
