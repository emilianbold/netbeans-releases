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
package org.netbeans.modules.compapp.casaeditor.model.casa.impl;

import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class CasaEndpointImpl extends CasaComponentImpl 
        implements CasaEndpoint {

    public CasaEndpointImpl(CasaModel model, Element element) {
        super(model, element);
    }
    
    public CasaEndpointImpl(CasaModel model) {
        this(model, createElementNS(model, CasaQName.ENDPOINT));
    }
    
    public void accept(CasaComponentVisitor visitor) {
        visitor.visit(this);
    }

    public boolean isConsumes() {
        return Boolean.valueOf(getAttribute(CasaAttribute.IS_CONSUME));
    }
    
    public String getName() {
        return getAttribute(CasaAttribute.NAME);
    }
    
    public void setName(String name) {
        setAttribute(NAME_PROPERTY, CasaAttribute.NAME, name);
    }
    
    public String getEndpointName() {
        return getAttribute(CasaAttribute.ENDPOINT_NAME);
    }

    public void setEndpointName(String endpointName) {
        setAttribute(ENDPOINT_NAME_PROPERTY, CasaAttribute.ENDPOINT_NAME, endpointName);        
    }
    
    public QName getInterfaceQName() {
        String attrValue = getAttribute(CasaAttribute.INTERFACE_NAME);
        return getQName(attrValue);
    }
    
    public void setInterfaceQName(QName qname) { // REFACTOR ME
        String namespace = qname.getNamespaceURI();
        String prefix = qname.getPrefix();
        String localPart = qname.getLocalPart();

        if (namespace != null && !namespace.equals("")) {
            String existingPrefix = lookupPrefix(namespace);
            if (existingPrefix == null) {
                AbstractDocumentComponent root =
                        (AbstractDocumentComponent) getModel().getRootComponent();
                existingPrefix = root.lookupPrefix(namespace);
                if (existingPrefix == null) {
                    if (prefix == null || prefix.equals("")) {
                        prefix = "ns"; //NOI18N
                    }
                    prefix = ensureUnique(prefix, namespace);
                    root.addPrefix(prefix, namespace);
                } else {
                    prefix = existingPrefix;
                }
            } else {
                prefix = existingPrefix;
            }
        }

        String qName;
        if (//(prefix == null || prefix.trim().length() == 0) &&
            (localPart == null || localPart.trim().length() == 0)) {
            qName = "";
        } else {
            qName = prefix + ":" + localPart;
        }
        setAttribute(INTERFACE_NAME_PROPERTY, CasaAttribute.INTERFACE_NAME, qName);     
    }

    public QName getServiceQName() {
        String attrValue = getAttribute(CasaAttribute.SERVICE_NAME);
        return getQName(attrValue);
    }
        
    public void setServiceQName(QName qname) {        
                        
        String namespace = qname.getNamespaceURI();
        String prefix = qname.getPrefix();
        String localPart = qname.getLocalPart();

        if (namespace != null && !namespace.equals("")) {
            String existingPrefix = lookupPrefix(namespace);
            if (existingPrefix == null) {
                AbstractDocumentComponent root =
                        (AbstractDocumentComponent) getModel().getRootComponent();
                existingPrefix = root.lookupPrefix(namespace);
                if (existingPrefix == null) {
                    if (prefix == null || prefix.equals("")) {
                        prefix = "ns"; //NOI18N
                    }
                    prefix = ensureUnique(prefix, namespace);
                    root.addPrefix(prefix, namespace);
                } else {
                    prefix = existingPrefix;
                }
            } else {
                prefix = existingPrefix;
            }
        }

        String qName;
        if (//(prefix == null || prefix.trim().length() == 0) &&
            (localPart == null || localPart.trim().length() == 0)) {
            qName = "";
        } else {
            qName = prefix + ":" + localPart;
        }
        setAttribute(SERVICE_NAME_PROPERTY, CasaAttribute.SERVICE_NAME, qName);  
    }
    
    private QName getQName(String prefixedName) {
        assert prefixedName != null;
        
        String localPart;
        String namespaceURI;
        String prefix;
        
        int colonIndex = prefixedName.indexOf(":");
        if (colonIndex != -1) {
            prefix = prefixedName.substring(0, colonIndex);
            localPart = prefixedName.substring(colonIndex + 1);
            namespaceURI = getPeer().lookupNamespaceURI(prefix);
        } else {
            prefix = "";
            localPart = prefixedName;
            namespaceURI = "";
        }
        
        return new QName(namespaceURI, localPart, prefix);
    }   

}