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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * 
 */
package org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class DocumentationImpl extends GenericExtensibilityElement implements Documentation {

    public DocumentationImpl( WSDLModel model, Element e ) {
        super(model, e);
    }

/*    public DocumentationImpl( WSDLModel model, QName qname ) {
        this(model, createPrefixedElement(BPELQName.DOCUMENTATION.getQName(),
                model));
    }*/

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#getContent()
     */
    public String getContent() {
        return getText();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#setContent(java.lang.String)
     */
    public void setContent( String value ) {
        setText( CONTENT_PROPERTY, value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent#accept(org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent.Visitor)
     */
    public void accept( Visitor v ) {
        v.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#getSource()
     */
    public String getSource() {
        return getAttribute(BPELAttribute.SOURCE);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#setSource()
     */
    public void setSource( String value ) {
        setAttribute( SOURCE , BPELAttribute.SOURCE , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#removeSource()
     */
    public void removeSource() {
        setAttribute( SOURCE , BPELAttribute.SOURCE , null );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#getLang()
     */
    public String getLang() {
        return getAttribute( BPELAttribute.LANG );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#removeLang()
     */
    public void removeLang() {
        setAttribute( LANG , BPELAttribute.LANG, null );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#setLang(java.lang.String)
     */
    public void setLang( String value ) {
        setAttribute( LANG , BPELAttribute.LANG, value );
    }

}
