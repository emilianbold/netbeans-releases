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
import org.netbeans.modules.bpel.model.api.Link;
import org.netbeans.modules.bpel.model.api.LinkContainer;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.AfterSources;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class LinkContainerImpl extends ExtensibleElementsImpl implements
        LinkContainer, AfterSources
{

    LinkContainerImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    LinkContainerImpl( BpelBuilderImpl builder) {
        super(builder, BpelElements.LINKS.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.LinkContainer#getLinks()
     */
    public Link[] getLinks() {
        readLock();
        try {
            List<Link> list = getChildren(Link.class);
            return list.toArray(new Link[list.size()]);
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.LinkContainer#getLink(int)
     */
    public Link getLink( int i ) {
        return getChild(Link.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.LinkContainer#setLinks(org.netbeans.modules.soa.model.bpel20.api.Link[])
     */
    public void setLinks( Link[] links ) {
        setArrayBefore(links, Link.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.LinkContainer#insertLink(org.netbeans.modules.soa.model.bpel20.api.Link, int)
     */
    public void insertLink( Link link, int i ) {
        insertAtIndex(link, Link.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.LinkContainer#addLink(org.netbeans.modules.soa.model.bpel20.api.Link)
     */
    public void addLink( Link link ) {
        addChild(link, Link.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.LinkContainer#setLink(org.netbeans.modules.soa.model.bpel20.api.Link, int)
     */
    public void setLink( Link link, int i ) {
        setChildAtIndex(link, Link.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.LinkContainer#removeLink(int)
     */
    public void removeLink( int i ) {
        removeChild(Link.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.LinkContainer#sizeOfLink()
     */
    public int sizeOfLink() {
        readLock();
        try {
            return getChildren(Link.class).size();
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return LinkContainer.class;
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
    protected BpelEntity create( Element element )
    {
        if ( BpelElements.LINK.getName().equals( element.getLocalName() )){
            return new LinkImpl( getModel() , element );
        }
        return super.create(element);
    }

}
