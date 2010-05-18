/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xml.wsdl.ui.api.property;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
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

    public void setValue(ElementOrTypeOrMessagePart newValue) {
        if (newValue == null || (newValue.getType() == null &&
                newValue.getElement() == null &&
                newValue.getMessagePart() == null)) {
            return;
        }
        ElementOrTypeOrMessagePart oldValue = getValue(); 
        if (oldValue != null && newValue.toString().equals(oldValue)) return; 
        if (getModel().startTransaction()) {
            try {
                ParameterType pType = newValue.getParameterType();

                switch (pType) {
                case ELEMENT:
                    Utility.addNamespacePrefix(newValue.getElement().getModel().getSchema(), extensibilityElement.getModel(), null);
                    Utility.addSchemaImport(newValue.getElement(), extensibilityElement.getModel());
                    extensibilityElement.setAttribute(elementAttributeName, newValue.toString());
                    extensibilityElement.setAttribute(typeAttributeName, null);
                    extensibilityElement.setAttribute(messageAttributeName, null);
                    extensibilityElement.setAttribute(partAttributeName, null);
                    break;
                case TYPE:
                    Utility.addNamespacePrefix(newValue.getType().getModel().getSchema(), extensibilityElement.getModel(), null);
                    Utility.addSchemaImport(newValue.getType(), extensibilityElement.getModel());
                    extensibilityElement.setAttribute(elementAttributeName, null);
                    extensibilityElement.setAttribute(typeAttributeName, newValue.toString());
                    extensibilityElement.setAttribute(messageAttributeName, null);
                    extensibilityElement.setAttribute(partAttributeName, null);
                    break;
                case MESSAGEPART:
                    Part part = newValue.getMessagePart();
                    Message message = (Message)part.getParent();
                    Utility.addNamespacePrefix(part, extensibilityElement.getModel(), null);
                    Utility.addWSDLImport(part, extensibilityElement.getModel());
                    QName qname = new QName(message.getName());
                    
                    String tns = message.getModel().getDefinitions().getTargetNamespace();
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
            ActionHelper.selectNode(extensibilityElement);
        }
    }

    public ElementOrTypeOrMessagePart getValue() {
        if (extensibilityElement.getModel() == null) { //this seems to happen during deletion.
            return null;
        }
        
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
            return new ElementOrTypeOrMessagePart(getQName(value), extensibilityElement.getModel(), part);
        } else {
            return null;
        }
        
        QName qname = getQName(value);
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
