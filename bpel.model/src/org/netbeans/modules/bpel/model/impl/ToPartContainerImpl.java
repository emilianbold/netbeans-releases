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
import org.netbeans.modules.bpel.model.api.ToPart;
import org.netbeans.modules.bpel.model.api.ToPartContainer;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class ToPartContainerImpl extends ExtensibleElementsImpl implements
        ToPartContainer
{

    ToPartContainerImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    ToPartContainerImpl( BpelBuilderImpl builder) {
        super(builder, BpelElements.TO_PARTS.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ToPartContainer#getToParts()
     */
    public ToPart[] getToParts() {
        readLock();
        try {
            List<ToPart> list = getChildren( ToPart.class );
            return list.toArray( new ToPart[ list.size() ]);
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ToPartContainer#getToPart(int)
     */
    public ToPart getToPart( int i ) {
        return getChild( ToPart.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ToPartContainer#setToPart(org.netbeans.modules.soa.model.bpel20.api.ToPart, int)
     */
    public void setToPart( ToPart part, int i ) {
        setChildAtIndex( part, ToPart.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ToPartContainer#insertToPart(org.netbeans.modules.soa.model.bpel20.api.ToPart, int)
     */
    public void insertToPart( ToPart part, int i ) {
        insertAtIndex( part , ToPart.class , i  );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ToPartContainer#addToPart(org.netbeans.modules.soa.model.bpel20.api.ToPart)
     */
    public void addToPart( ToPart part ) {
        addChild( part , ToPart.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ToPartContainer#removeToPart(int)
     */
    public void removeToPart( int i ) {
        removeChild( ToPart.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ToPartContainer#setToPart(org.netbeans.modules.soa.model.bpel20.api.ToPart[])
     */
    public void setToPart( ToPart[] parts ) {
        setArrayBefore( parts , ToPart.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ToPartContainer#sizeOfToParts()
     */
    public int sizeOfToParts() {
        readLock();
        try {
            return getChildren( ToPart.class ).size(); 
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return ToPartContainer.class;
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelEntity#accept(org.netbeans.modules.bpel.model.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.ExtensibleElementsImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        if ( BpelElements.TO_PART.getName().equals( element.getLocalName())) {
            return new ToPartImpl( getModel() , element );
        }
        return super.create(element);
    }

}
