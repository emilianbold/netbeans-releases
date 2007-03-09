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
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;

public class ElementOrTypeOrMessagePartProvider {

    private final ExtensibilityElement extensibilityElement;
    private final String typeAttributeName;
    private final String elementAttributeName;
    private final String messageAttributeName;
    private final String partAttributeName;

    public ElementOrTypeOrMessagePartProvider(ExtensibilityElement extensibilityElement, 
            String elementAttributeName, 
            String typeAttributeName,
            String messageAttributeName,
            String partAttributeName) {
        this.extensibilityElement = extensibilityElement;
        this.elementAttributeName = elementAttributeName;
        this.typeAttributeName = typeAttributeName;
        this.messageAttributeName = messageAttributeName;
        this.partAttributeName = partAttributeName;
    }

    public void setValue(ElementOrTypeOrMessagePart o) {
        if (o == null) {
            return;
        }
        
        
        if (getModel().startTransaction()) {
            try {
                ParameterType pType = o.getParameterType();

                switch (pType) {
                case ELEMENT:
                    Utility.addNamespacePrefix(o.getElement().getModel().getSchema(), extensibilityElement.getModel(), null);
                    extensibilityElement.setAttribute(elementAttributeName, o.toString());
                    extensibilityElement.setAttribute(typeAttributeName, null);
                    extensibilityElement.setAttribute(messageAttributeName, null);
                    extensibilityElement.setAttribute(partAttributeName, null);
                    break;
                case TYPE:
                    Utility.addNamespacePrefix(o.getType().getModel().getSchema(), extensibilityElement.getModel(), null);
                    extensibilityElement.setAttribute(elementAttributeName, null);
                    extensibilityElement.setAttribute(typeAttributeName, o.toString());
                    extensibilityElement.setAttribute(messageAttributeName, null);
                    extensibilityElement.setAttribute(partAttributeName, null);
                    break;
                case MESSAGEPART:
                    Part part = o.getMessagePart();
                    Message message = (Message)part.getParent();
                    String tns = message.getModel().getDefinitions().getTargetNamespace();
                    QName qname = new QName(message.getName());
                    if (tns != null) {
                        String prefix = Utility.getNamespacePrefix(tns, extensibilityElement.getModel()); 
                        qname = prefix != null ? new QName(tns, message.getName(), prefix) :
                        new QName(tns, message.getName());
                    }
                    
                    extensibilityElement.setAttribute(messageAttributeName, Utility.fromQNameToString(qname));
                    extensibilityElement.setAttribute(partAttributeName, part.getName());
                    extensibilityElement.setAttribute(elementAttributeName, null);
                    extensibilityElement.setAttribute(typeAttributeName, null);
                    break;
                case NONE:
                    extensibilityElement.setAttribute(elementAttributeName, null);
                    extensibilityElement.setAttribute(typeAttributeName, null);
                    extensibilityElement.setAttribute(typeAttributeName, null);
                    extensibilityElement.setAttribute(elementAttributeName, null);
                }
            } finally {                
                getModel().endTransaction();
            }
        }
    }

    public ElementOrTypeOrMessagePart getValue() {
        ParameterType parameterType = ParameterType.NONE;

        String message = extensibilityElement.getAttribute(messageAttributeName);
        String part = extensibilityElement.getAttribute(partAttributeName);
        String type = extensibilityElement.getAttribute(typeAttributeName);
        String element = extensibilityElement.getAttribute(elementAttributeName);

        String value = null;
        
        if (element != null) {
            parameterType = ParameterType.ELEMENT;
            value = element;
        } else if (type != null) {
            value = type;
            parameterType = ParameterType.TYPE;
        } else if (message != null) {
            parameterType = ParameterType.MESSAGEPART;
            value = message;
        }
        
        
        
        if (extensibilityElement.getModel() == null) { //this seems to happen during deletion.
            return null;
        }
        
        QName qname = getQName(value);
        if (parameterType == ParameterType.MESSAGEPART) {
            return new ElementOrTypeOrMessagePart(qname, extensibilityElement.getModel(), part);
        }
        return new ElementOrTypeOrMessagePart(qname, extensibilityElement.getModel(), parameterType);
    }

    private QName getQName(String value) {
        if (value != null && value.trim().length() > 0) {
            String[] parts = value.split(":");
            if (parts != null && parts.length == 2) {
                String prefix = parts[0];
                String localPart = parts[1];
                String namespace = Utility.getNamespaceURI(prefix, extensibilityElement);
                return new QName(namespace, localPart, prefix);
            }
            return new QName(getModel().getDefinitions().getTargetNamespace(), value);
        }
        return new QName("");
    }
    
    public WSDLModel getModel() {
        return extensibilityElement.getModel();
    }

    public static enum ParameterType {

        ELEMENT, TYPE, MESSAGEPART, NONE
    }
}
