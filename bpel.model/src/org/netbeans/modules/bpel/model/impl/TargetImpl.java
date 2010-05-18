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

import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Link;
import org.netbeans.modules.bpel.model.api.Target;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class TargetImpl extends ExtensibleElementsImpl implements Target {

    TargetImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    TargetImpl( BpelBuilderImpl builder, String tagName ) {
        super(builder, tagName);
    }
    
    TargetImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.TARGET.getName() );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Target.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.LinkReference#getLink()
     */
    public BpelReference<Link> getLink() {
        return getBpelReference( BpelAttributes.LINK_NAME , Link.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.LinkReference#setLink(org.netbeans.modules.soa.model.bpel20.references.BpelReference)
     */
    public void setLink( BpelReference<Link> link ) {
        setBpelReference( BpelAttributes.LINK_NAME , link );
    }    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences(){
        return new Reference[] { getLink() };
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 1];
            System.arraycopy( attr , 0 , ret , 1 , attr.length );
            ret[ 0 ] = BpelAttributes.LINK_NAME;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes =
        new AtomicReference<Attribute[]>();

}
