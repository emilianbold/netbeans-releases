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
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.AfterImport;
import org.netbeans.modules.bpel.model.xam.AfterSources;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class CorrelationSetContainerImpl extends ExtensibleElementsImpl implements
        CorrelationSetContainer, AfterImport, AfterSources
{

    CorrelationSetContainerImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    CorrelationSetContainerImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.CORRELATION_SETS.getName() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationSetContainer#getCorrelationSets()
     */
    public CorrelationSet[] getCorrelationSets() {
        readLock();
        try {
            List<CorrelationSet> list = 
                getChildren(CorrelationSet.class);
            return list.toArray(new CorrelationSet[list.size()]);
        }
        finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationSetContainer#getCorrelationSet(int)
     */
    public CorrelationSet getCorrelationSet( int i ) {
        return getChild(CorrelationSet.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationSetContainer#removeCorrelationSet(int)
     */
    public void removeCorrelationSet( int i ) {
        removeChild(CorrelationSet.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationSetContainer#addCorrelationSet(org.netbeans.modules.soa.model.bpel20.api.CorrelationSet)
     */
    public void addCorrelationSet( CorrelationSet set ) {
        addChild(set, CorrelationSet.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationSetContainer#insertCorrelationSet(org.netbeans.modules.soa.model.bpel20.api.CorrelationSet,
     *      int)
     */
    public void insertCorrelationSet( CorrelationSet set, int i ) {
        insertAtIndex(set, CorrelationSet.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationSetContainer#setCorrelationSets(org.netbeans.modules.soa.model.bpel20.api.CorrelationSet[])
     */
    public void setCorrelationSets( CorrelationSet[] set ) {
        setArrayBefore(set, CorrelationSet.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationSetContainer#setCorrelationSet(org.netbeans.modules.soa.model.bpel20.api.CorrelationSet,
     *      int)
     */
    public void setCorrelationSet( CorrelationSet set, int i ) {
        setChildAtIndex(set, CorrelationSet.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.CorrelationSetContainer#sizeOfCorrelationSet()
     */
    public int sizeOfCorrelationSet() {
        readLock();
        try {
            return getChildren(CorrelationSet.class).size();
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
        return CorrelationSetContainer.class;
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.impl.BpelContainerImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        if ( BpelElements.CORRELATION_SET.getName().equals(element.getLocalName())) {
            return new CorrelationSetImpl(getModel(), element);
        }
        return super.create( element );
    }

}
