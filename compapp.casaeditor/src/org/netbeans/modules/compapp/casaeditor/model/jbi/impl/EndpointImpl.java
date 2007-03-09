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
package org.netbeans.modules.compapp.casaeditor.model.jbi.impl;

import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Endpoint;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Consumes;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Provides;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public abstract class EndpointImpl extends JBIComponentImpl implements Endpoint {

    /** Creates a new instance of EndpointImpl */
    public EndpointImpl(JBIModel model, Element element) {
        super(model, element);
    }

    public String getInterfaceName() {
        return getAttribute(JBIAttributes.INTERFACE_NAME);
    }

    public void setInterfaceName(String interfaceName) {
        setAttribute(INTERFACE_NAME_PROPERTY,
                JBIAttributes.INTERFACE_NAME, interfaceName);
    }

    public String getServiceName() {
        return getAttribute(JBIAttributes.SERVICE_NAME);
    }

    public void setServiceName(String serviceName) {
        setAttribute(SERVICE_NAME_PROPERTY,
                JBIAttributes.SERVICE_NAME, serviceName);
    }

    public String getEndpointName() {
        return getAttribute(JBIAttributes.ENDPOINT_NAME);
    }

    public void setEndpointName(String endpointName) {
        setAttribute(ENDPOINT_NAME_PROPERTY,
                JBIAttributes.ENDPOINT_NAME, endpointName);
    }

    // Convenience method
    public QName getInterfaceQName() {
        String interfaceName = getInterfaceName();
        return getQName(interfaceName);
    }

    public QName getServiceQName() {
        String serviceName = getServiceName();
        return getQName(serviceName);
    }

    public boolean isPair(Endpoint anotherEndpoint) {
        boolean ret = false;

        if ((this instanceof Consumes) && (anotherEndpoint instanceof Provides) ||
                (this instanceof Provides) && (anotherEndpoint instanceof Consumes)) {

                QName interfaceQName = getInterfaceQName();
                QName anotherInterfaceQName = anotherEndpoint.getInterfaceQName();
                ret = interfaceQName.equals(anotherInterfaceQName);
        }

        return ret;
    }

    // REFACTOR
    public void setServiceQName(QName qname) {
        String namespace = qname.getNamespaceURI();
        String prefix = qname.getPrefix();
        String localPart = qname.getLocalPart();
//        setAnyAttribute(qname, localPart);  // FIXME: what about event firing???

        String existingPrefix = lookupPrefix(namespace);
        if (existingPrefix == null) {
            AbstractDocumentComponent root = (AbstractDocumentComponent) getModel().getRootComponent();
            existingPrefix = root.lookupPrefix(namespace);
            if (existingPrefix == null) {
                if (prefix == null) {
                    prefix = "ns"; // NOI18N
                }
                prefix = ensureUnique(prefix, namespace);
                root.addPrefix(prefix, namespace);
            } else {
                prefix = existingPrefix;
            }
        } else {
            prefix = existingPrefix;
        }

        setServiceName(prefix + Constants.COLON_STRING + localPart);
    }

    public void setInterfaceQName(QName qname) {
        String namespace = qname.getNamespaceURI();
        String prefix = qname.getPrefix();
        String localPart = qname.getLocalPart();
//        setAnyAttribute(qname, localPart);  // FIXME: what about event firing???

        String existingPrefix = lookupPrefix(namespace);
        if (existingPrefix == null) {
            AbstractDocumentComponent root = (AbstractDocumentComponent) getModel().getRootComponent();
            existingPrefix = root.lookupPrefix(namespace);
            if (existingPrefix == null) {
                if (prefix == null) {
                    prefix = "ns"; // NOI18N
                }
                prefix = ensureUnique(prefix, namespace);
                root.addPrefix(prefix, namespace);
            } else {
                prefix = existingPrefix;
            }
        } else {
            prefix = existingPrefix;
        }

        setInterfaceName(prefix + Constants.COLON_STRING + localPart);
    }
}
