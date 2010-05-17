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

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.LinkContainer;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class FlowImpl extends CompositeActivityImpl implements Flow {


    FlowImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    FlowImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.FLOW.getName() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Flow#getLinkContainer()
     */
    public LinkContainer getLinkContainer() {
        return getChild(LinkContainer.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Flow#setLinkContainer(org.netbeans.modules.soa.model.bpel20.api.LinkContainer)
     */
    public void setLinkContainer( LinkContainer value ) {
        setChild(value, LinkContainer.class, BpelTypesEnum.ACTIVITIES_GROUP);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Flow#removeLinkContainer()
     */
    public void removeLinkContainer() {
        removeChild(LinkContainer.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Flow.class;
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit(this);
    }

    @Override
    protected BpelEntity create( Element element )
    {
        if ( BpelElements.LINKS.getName().equals(element.getLocalName())) {
            return new LinkContainerImpl(getModel(), element);
        }
        return super.create(element);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity )
    {
        if ( getChildType( entity).equals( LinkContainer.class) ){
            return Multiplicity.SINGLE;
        }
        return super.getMultiplicity(entity);
    }

}
