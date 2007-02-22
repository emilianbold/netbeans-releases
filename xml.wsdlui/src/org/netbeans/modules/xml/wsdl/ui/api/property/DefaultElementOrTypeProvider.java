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
package org.netbeans.modules.xml.wsdl.ui.api.property;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;

public class DefaultElementOrTypeProvider implements ElementOrTypeProvider {

    private final ExtensibilityElement extensibilityElement;
    private final String typeAttributeName;
    private final String elementAttributeName;

    public DefaultElementOrTypeProvider(ExtensibilityElement extensibilityElement, String elementAttributeName, String typeAttributeName) {
        this.extensibilityElement = extensibilityElement;
        this.elementAttributeName = elementAttributeName;
        this.typeAttributeName = typeAttributeName;
    }

    public void setElementOrType(ElementOrType o) {
        if (o == null) {
            return;
        }
        getModel().startTransaction();
        if (o.isElement()) {
            extensibilityElement.setAttribute(elementAttributeName, o.toString());
            extensibilityElement.setAttribute(typeAttributeName, null);
        } else {
            extensibilityElement.setAttribute(typeAttributeName, o.toString());
            extensibilityElement.setAttribute(elementAttributeName, null);
        }
        getModel().endTransaction();
    }

    public ElementOrType getElementOrType() {
        boolean isElement = false;
        String value = extensibilityElement.getAttribute(typeAttributeName);
        if (value == null) {
            value = extensibilityElement.getAttribute(elementAttributeName);
        } else {
            isElement = false;
        }
        if (value != null) {
            isElement = true;
        }
        
        if (extensibilityElement.getModel() == null) { //this seems to happen during deletion.
            return null;
        }
        
        if (value != null && value.trim().length() > 0) {
            String[] parts = value.split(":");
            if (parts != null && parts.length == 2) {
                String prefix = parts[0];
                String localPart = parts[1];
                String namespace = Utility.getNamespaceURI(prefix, extensibilityElement);
                return new ElementOrType(new QName(namespace, localPart, prefix), extensibilityElement.getModel(), isElement);
            }
            return new ElementOrType(new QName(value), extensibilityElement.getModel(), false);
        }
        
        return new ElementOrType(new QName(""), extensibilityElement.getModel(), false);
    }

    public WSDLModel getModel() {
        return extensibilityElement.getModel();
    }

}
