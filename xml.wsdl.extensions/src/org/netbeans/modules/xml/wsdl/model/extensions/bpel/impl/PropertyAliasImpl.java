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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 * 
 * changed by 
 * @author ads
 */
public class PropertyAliasImpl extends GenericExtensibilityElement implements
        PropertyAlias
{

    public PropertyAliasImpl( WSDLModel model, Element e ) {
        super(model, e);
    }

    public PropertyAliasImpl( WSDLModel model ) {
        this(model, createPrefixedElement(BPELQName.PROPERTY_ALIAS.getQName(),
                model));
    }

    protected String getNamespaceURI() {
        return BPELQName.VARPROP_NS;
    }

    public NamedComponentReference<CorrelationProperty> getPropertyName() {
        return resolveGlobalReference(CorrelationProperty.class,
                BPELAttribute.PROPERTY_NAME);
    }

    public void setPropertyName(
            NamedComponentReference<CorrelationProperty> property )
    {
        setAttribute(PROPERTY_NAME_PROPERTY, BPELAttribute.PROPERTY_NAME,
                property);
    }

    public NamedComponentReference<Message> getMessageType() {
        return resolveGlobalReference(Message.class, BPELAttribute.MESSAGE_TYPE);
    }

    public void setMessageType( NamedComponentReference<Message> type ) {
        setAttribute(MESSAGE_TYPE_PROPERTY, BPELAttribute.MESSAGE_TYPE, type);
    }

    public String getPart() {
        return getAttribute(BPELAttribute.PART);
    }

    public void setPart( String part ) {
        setAttribute(PART_PROPERTY, BPELAttribute.PART, part);
    }

    public Query getQuery() {
        return getChild( Query.class );
    }

    public void setQuery( Query query ) {
        Query q = getQuery();
        if ( q!= null ){
            removeChild(QUERY_PROPERTY, q);
        }
        appendChild(QUERY_PROPERTY, query );
    }

    public void accept( BPELExtensibilityComponent.Visitor v ) {
        v.visit(this);
    }

    public void removeQuery() {
        if ( getQuery()!= null ){
            removeChild( QUERY_PROPERTY, getQuery() );
        }
    }
    
    public void removeQuery( Query query ) {
        if ( query!= null ){
            removeChild( QUERY_PROPERTY, query );
        }
    }
    
    public NamedComponentReference<GlobalType> getType() {
        return resolveSchemaReference(GlobalType.class, BPELAttribute.TYPE);
    }

    public void setType(NamedComponentReference<GlobalType> type) {
        setAttribute(TYPE_PROPERTY, BPELAttribute.TYPE, type);
    }

    public NamedComponentReference<GlobalElement> getElement() {
        return resolveSchemaReference(GlobalElement.class, BPELAttribute.ELEMENT);
    }

    public void setElement( NamedComponentReference<GlobalElement> value ) {
        setAttribute(ELEMENT_PROPERTY, BPELAttribute.ELEMENT, value);        
    }

}