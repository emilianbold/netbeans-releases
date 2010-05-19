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
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.FromPartContainer;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class FromPartConainerImpl extends ExtensibleElementsImpl implements
        FromPartContainer
{

    FromPartConainerImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    FromPartConainerImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.FROM_PARTS.getName());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.FromPartContainer#addFromPart(org.netbeans.modules.bpel.model.api.FromPart)
     */
    public void addFromPart( FromPart part ) {
        addChild(part , FromPart.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.FromPartContainer#getFromPart(int)
     */
    public FromPart getFromPart( int i ) {
        return getChild( FromPart.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.FromPartContainer#getFromParts()
     */
    public FromPart[] getFromParts() {
        readLock();
        try {
            List<FromPart> list = getChildren(FromPart.class);
            return list.toArray(new FromPart[list.size()]);
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.FromPartContainer#insertFromPart(org.netbeans.modules.bpel.model.api.FromPart, int)
     */
    public void insertFromPart( FromPart part, int i ) {
        insertAtIndex(part, FromPart.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.FromPartContainer#removeFromPart(int)
     */
    public void removeFromPart( int i ) {
        removeChild(FromPart.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.FromPartContainer#setFromPart(org.netbeans.modules.bpel.model.api.FromPart, int)
     */
    public void setFromPart( FromPart part, int i ) {
        setChildAtIndex(part, FromPart.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.FromPartContainer#setFromParts(org.netbeans.modules.bpel.model.api.FromPart[])
     */
    public void setFromParts( FromPart[] parts ) {
        setArrayBefore(parts, FromPart.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.FromPartContainer#sizeOfFromParts()
     */
    public int sizeOfFromParts() {
        readLock();
        try {
            return getChildren(FromPart.class).size();
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return FromPartContainer.class;
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
        if (BpelElements.FROM_PART.getName().equals(element.getLocalName())) {
            return new FromPartImpl(getModel(), element);
        }
        return super.create(element);
    }

}
