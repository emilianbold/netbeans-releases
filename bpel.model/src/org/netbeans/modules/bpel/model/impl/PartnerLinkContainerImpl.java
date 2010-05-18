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
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.AfterImport;
import org.netbeans.modules.bpel.model.xam.AfterSources;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class PartnerLinkContainerImpl extends ExtensibleElementsImpl implements
        PartnerLinkContainer, AfterImport, AfterSources
{

    PartnerLinkContainerImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    PartnerLinkContainerImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.PARTNERLINKS.getName() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLinkContainer#getPartnerLinks()
     */
    public PartnerLink[] getPartnerLinks() {
        readLock();
        try {
            List<PartnerLink> list = getChildren(PartnerLink.class);
            return list.toArray(new PartnerLink[list.size()]);
        }
        finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLinkContainer#getPartnerLink(int)
     */
    public PartnerLink getPartnerLink( int i ) {
        return getChild(PartnerLink.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLinkContainer#setPartnerLink(org.netbeans.modules.soa.model.bpel20.api.PartnerLink,
     *      int)
     */
    public void setPartnerLink( PartnerLink link, int i ) {
        setChildAtIndex(link, PartnerLink.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLinkContainer#setPartnerLinks(org.netbeans.modules.soa.model.bpel20.api.PartnerLink[])
     */
    public void setPartnerLinks( PartnerLink[] links ) {
        setArrayBefore(links, PartnerLink.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLinkContainer#addPartnerLink(org.netbeans.modules.soa.model.bpel20.api.PartnerLink)
     */
    public void addPartnerLink( PartnerLink link ) {
        addChild(link, PartnerLink.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLinkContainer#insertPartnerLink(org.netbeans.modules.soa.model.bpel20.api.PartnerLink,
     *      int)
     */
    public void insertPartnerLink( PartnerLink link, int i ) {
        insertAtIndex(link, PartnerLink.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLinkContainer#removePartnerLink(int)
     */
    public void removePartnerLink( int i ) {
        removeChild(PartnerLink.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLinkContainer#sizeOfPartnerLink()
     */
    public int sizeOfPartnerLink() {
        readLock();
        try {
            return getChildren(PartnerLink.class).size();
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
        return PartnerLinkContainer.class;
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
        if ( BpelElements.PARTNER_LINK.getName().equals(element.getLocalName())) {
            return new PartnerLinkImpl(getModel(), element);
        }
        return super.create( element );
    }

}
