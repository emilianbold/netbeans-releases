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
package org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class QueryImpl extends GenericExtensibilityElement implements Query {

    public QueryImpl( WSDLModel model, Element e ) {
        super(model, e);
    }

    public QueryImpl( WSDLModel model ) {
        this(model, createPrefixedElement(BPELQName.QUERY.getQName(),
                model));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query#getContent()
     */
    public String getContent() {
        return getText();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query#setContent(java.lang.String)
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
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query#getQueryLanguage()
     */
    public String getQueryLanguage() {
        return getAttribute(BPELAttribute.QUERY_LANGUAGE);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query#setQueryLanguage()
     */
    public void setQueryLanguage( String value ) {
        setAttribute( QUERY_LANGUAGE , BPELAttribute.QUERY_LANGUAGE , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query#removeQueryLanguage()
     */
    public void removeQueryLanguage() {
        setAttribute( QUERY_LANGUAGE , BPELAttribute.QUERY_LANGUAGE , null );
    }

    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof PropertyAlias) {
            return true;
        }
        return false;
    }

}
