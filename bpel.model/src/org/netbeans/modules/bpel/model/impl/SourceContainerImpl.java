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

import java.util.List;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Source;
import org.netbeans.modules.bpel.model.api.SourceContainer;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.AfterTargets;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class SourceContainerImpl extends ExtensibleElementsImpl implements
        BpelEntity, SourceContainer, AfterTargets
{

    SourceContainerImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    SourceContainerImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.SOURCES.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return SourceContainer.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.SourceContainer#getSources()
     */
    public Source[] getSources() {
        readLock();
        try {
            List<Source> list = getChildren( Source.class );
            return list.toArray( new Source[ list.size() ]);
        }
        finally {
            readUnlock();
        }       
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.SourceContainer#getSource(int)
     */
    public Source getSource( int i ) {
        return getChild( Source.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.SourceContainer#removeSource(int)
     */
    public void removeSource( int i ) {
        removeChild( Source.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.SourceContainer#setSource(org.netbeans.modules.soa.model.bpel20.api.Source, int)
     */
    public void setSource( Source source, int i ) {
        setChildAtIndex( source , Source.class ,  i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.SourceContainer#addSource(org.netbeans.modules.soa.model.bpel20.api.Source)
     */
    public void addSource( Source source ) {
        addChild( source , Source.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.SourceContainer#setSources(org.netbeans.modules.soa.model.bpel20.api.Source[])
     */
    public void setSources( Source[] source ) {
        setArrayBefore( source , Source.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.SourceContainer#insertSource(org.netbeans.modules.soa.model.bpel20.api.Source, int)
     */
    public void insertSource( Source source, int i ) {
        setChildAtIndex( source , Source.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.SourceContainer#sizeOfSource()
     */
    public int sizeOfSource() {
        readLock();
        try {
            return getChildren( Source.class ).size();
        }
        finally {
            readUnlock();
        }
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.ExtensibleElementsImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element ) {
        if ( BpelElements.SOURCE.getName().equals( element.getLocalName() )){
            return new SourceImpl( getModel() , element );
        }
        return super.create(element);
    }

}
