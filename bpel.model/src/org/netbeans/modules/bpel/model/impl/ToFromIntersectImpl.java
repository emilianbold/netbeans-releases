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

import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.PartReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.PropertyReference;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public abstract class ToFromIntersectImpl extends ExtensibleElementsImpl 
    implements PartnerLinkReference, VariableReference, PartReference, 
    PropertyReference, ContentElement
{


    ToFromIntersectImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    ToFromIntersectImpl( BpelBuilderImpl builder, String tagName ) {
        super(builder, tagName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.From#removePartnerLink()
     */
    public void removePartnerLink() {
        removeReference( BpelAttributes.PARTNER_LINK );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLinkReference#getPartnerLink()
     */
    public BpelReference<PartnerLink> getPartnerLink() {
        return getBpelReference( BpelAttributes.PARTNER_LINK , 
                PartnerLink.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartnerLinkReference#setPartnerLink(org.netbeans.modules.soa.model.bpel20.references.BpelReference)
     */
    public void setPartnerLink( BpelReference<PartnerLink> value ) {
        setBpelReference( BpelAttributes.PARTNER_LINK , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableSpec#getVariable()
     */
    public BpelReference<VariableDeclaration> getVariable() {
        return getBpelReference( BpelAttributes.VARIABLE_REF , 
                VariableDeclaration.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableSpec#removeVariable()
     */
    public void removeVariable() {
        removeReference( BpelAttributes.VARIABLE_REF );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableSpec#setVariable(org.netbeans.modules.soa.model.bpel20.references.VariableReference)
     */
    public void setVariable( BpelReference<VariableDeclaration> value ) {
        setBpelReference( BpelAttributes.VARIABLE_REF ,value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartReference#getPart()
     */
    public WSDLReference<Part> getPart() {
        return getWSDLReference( BpelAttributes.PART , Part.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PartReference#setPart(org.netbeans.modules.soa.model.bpel20.api.support.WSDLReference)
     */
    public void setPart( WSDLReference<Part> part ) {
        setWSDLReference( BpelAttributes.PART, part );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PropertyReference#getProperty()
     */
    public WSDLReference<CorrelationProperty> getProperty() {
        return getWSDLReference( BpelAttributes.PROPERTY , 
                CorrelationProperty.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PropertyReference#setProperty(org.netbeans.modules.soa.model.bpel20.references.WSDLReference)
     */
    public void setProperty( WSDLReference<CorrelationProperty> property ) {
        setWSDLReference( BpelAttributes.PROPERTY ,  property );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.PropertyReference#removeProperty()
     */
    public void removeProperty() {
        removeReference( BpelAttributes.PROPERTY );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ContentElement#getContent()
     */
    public String getContent() {
        return getCorrectedText();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ContentElement#setContent(java.lang.String)
     */
    public void setContent( String content ) throws VetoException {
        setText( content );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.From#removePart()
     */
    public void removePart() {
        removeReference( BpelAttributes.PART );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ExpressionLanguageSpec#getExpressionLanguage()
     */
    public String getExpressionLanguage() {
        return getAttribute( BpelAttributes.EXPRESSION_LANGUAGE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ExpressionLanguageSpec#setExpressionLanguage(java.lang.String)
     */
    public void setExpressionLanguage( String value ) throws VetoException {
        setBpelAttribute( BpelAttributes.EXPRESSION_LANGUAGE , value );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ExpressionLanguageSpec#removeExpressionLanguage()
     */
    public void removeExpressionLanguage() {
        removeAttribute( BpelAttributes.EXPRESSION_LANGUAGE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences(){
        return new Reference[] { getPart() , getPartnerLink() , getProperty(),
                getVariable() };
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 6];
            System.arraycopy( attr , 0 , ret , 6 , attr.length );
            ret[ 0 ] = BpelAttributes.PARTNER_LINK;
            ret[ 1 ] = BpelAttributes.VARIABLE_REF;
            ret[ 2 ] = BpelAttributes.PART;
            ret[ 3 ] = BpelAttributes.PROPERTY;
            ret[ 4 ] = BpelAttributes.EXPRESSION_LANGUAGE;
            ret[ 5 ] = BpelAttributes.CONTENT;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();
}
