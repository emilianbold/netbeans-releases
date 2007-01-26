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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

/**
 * @authro Nam Nguyen
 * 
 * changed by
 * @author ads
 */
public class BPELElementFactoryProvider {
    
    public static class CorrelationPropertyFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(BPELQName.PROPERTY.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new CorrelationPropertyImpl(context.getModel(), element);
        }
    }

    public static class PartnerLinkTypeFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(BPELQName.PARTNER_LINK_TYPE.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new PartnerLinkTypeImpl(context.getModel(), element);
        }
    }

    public static class RoleFactory  extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(BPELQName.ROLE.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RoleImpl(context.getModel(), element);
        }
    }

    public static class PropertyAliasFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(BPELQName.PROPERTY_ALIAS.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new PropertyAliasImpl(context.getModel(), element);
        }
    }
    
    public static class QueryFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(BPELQName.QUERY.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new QueryImpl(context.getModel(), element);
        }
    }

    public static class DocumentationFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            Set<QName> set = new HashSet<QName>();
            set.add( BPELQName.DOCUMENTATION_VARPROP.getQName() );
            set.add( BPELQName.DOCUMENTATION_PLNK.getQName() );
            return set;
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new DocumentationImpl(context.getModel(), element);
        }
    }
    
}
