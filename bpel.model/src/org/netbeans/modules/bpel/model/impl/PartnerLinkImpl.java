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

import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class PartnerLinkImpl extends NamedElementImpl implements PartnerLink {

    PartnerLinkImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    PartnerLinkImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.PARTNER_LINK.getName() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return PartnerLink.class;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLink#getInitializePartnerRole()
     */
    public TBoolean getInitializePartnerRole() {
        return getBooleanAttribute( BpelAttributes.INITIALIZE_PARTNER_ROLE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLink#setInitializePartnerRole(org.netbeans.modules.soa.model.bpel20.api.support.TBoolean)
     */
    public void setInitializePartnerRole( TBoolean value ) {
        setBpelAttribute( BpelAttributes.INITIALIZE_PARTNER_ROLE , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLink#removeInitializePartnerRole()
     */
    public void removeInitializePartnerRole() {
        removeAttribute( BpelAttributes.INITIALIZE_PARTNER_ROLE );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLink#setPartnerLinkType(org.netbeans.modules.soa.model.bpel20.references.WSDLReference)
     */
    public void setPartnerLinkType( WSDLReference<PartnerLinkType> value ) {
        setWSDLReference( BpelAttributes.PARTNER_LINK_TYPE, value  );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLink#setMyRole(org.netbeans.modules.soa.model.bpel20.api.support.WSDLReference)
     */
    public void setMyRole( WSDLReference<Role> role ) {
        setWSDLReference( BpelAttributes.MY_ROLE , role );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLink#setPartnerRole(org.netbeans.modules.soa.model.bpel20.api.support.WSDLReference)
     */
    public void setPartnerRole( WSDLReference<Role> value ) {
        setWSDLReference( BpelAttributes.PARTNER_ROLE , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLink#getMyRole()
     */
    public WSDLReference<Role> getMyRole() {
        return getWSDLReference( BpelAttributes.MY_ROLE , Role.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLink#removeMyRole()
     */
    public void removeMyRole() {
        removeReference( BpelAttributes.MY_ROLE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLink#getPartnerLinkType()
     */
    public WSDLReference<PartnerLinkType> getPartnerLinkType() {
        return getWSDLReference( BpelAttributes.PARTNER_LINK_TYPE ,
                PartnerLinkType.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLink#getPartnerRole()
     */
    public WSDLReference<Role> getPartnerRole() {
        return getWSDLReference( BpelAttributes.PARTNER_ROLE , Role.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLink#removePartnerRole()
     */
    public void removePartnerRole() {
        removeReference( BpelAttributes.PARTNER_ROLE );
    }
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences() {
        return new Reference[] { getPartnerRole() , getMyRole() , 
                getPartnerLinkType()};
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit(this);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 4];
            System.arraycopy( attr , 0 , ret , 4 , attr.length );
            ret[ 0 ] = BpelAttributes.INITIALIZE_PARTNER_ROLE;
            ret[ 1 ] = BpelAttributes.PARTNER_LINK_TYPE;
            ret[ 2 ] = BpelAttributes.MY_ROLE;
            ret[ 3 ] = BpelAttributes.PARTNER_ROLE;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();

}
