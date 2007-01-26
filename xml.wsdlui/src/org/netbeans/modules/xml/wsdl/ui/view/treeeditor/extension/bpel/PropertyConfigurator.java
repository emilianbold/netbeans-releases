/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.extension.bpel;


import java.util.Arrays;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrType;
import org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypeAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypeProvider;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

public class PropertyConfigurator extends
        ExtensibilityElementConfigurator {

    private static QName myQName = BPELQName.PROPERTY.getQName();
    
    private static QName[] supportedQNames = {myQName};

    @Override
    public Collection<QName> getSupportedQNames() {
        return Arrays.asList(supportedQNames);
    }
    
    @Override
    public Property getProperty(ExtensibilityElement extensibilityElement,
            QName qname, String attributeName) {
        Node.Property property = null;
        if (myQName.equals(qname)) {
            if ("type".equals(attributeName)) {//NOI18N
                ElementOrTypeProvider provider = new PropertyElementOrTypeProvider(extensibilityElement);
                try {
                    property = new ElementOrTypeAttributeProperty(provider);
                    property.setName(NbBundle.getMessage(PropertyConfigurator.class, "PROPERTY_ELEMENT_OR_TYPE"));
                    return property;
                } catch (NoSuchMethodException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
            //element is hidden
        }
        return null;
    }

    @Override
    public String getDisplayAttributeName(ExtensibilityElement extensibilityElement, QName qname) {
        return "name"; //NOI18N
    }

    @Override
    public boolean isHidden(ExtensibilityElement element, QName qname, String attributeName) {
        if (myQName.equals(qname)) {
            if ("element".equals(attributeName)) {//NOI18N
                return true;
            }
        }
        return super.isHidden(element, qname, attributeName);
    }
    
    public class PropertyElementOrTypeProvider implements ElementOrTypeProvider {
        
        private ExtensibilityElement extensibilityElement;
        
        public PropertyElementOrTypeProvider(ExtensibilityElement element) {
            extensibilityElement = element;
        }
        
        public void setElementOrType(ElementOrType o) {
            if (o == null) {
                return;
            }
            getModel().startTransaction();
            if (o.isElement()) {
                extensibilityElement.setAttribute("element", o.toString());
                extensibilityElement.setAttribute("type", null);
            } else {
                extensibilityElement.setAttribute("type", o.toString());
                extensibilityElement.setAttribute("element", null);
            }
                getModel().endTransaction();
            
        }

        public ElementOrType getElementOrType() {
            boolean isElement = false;
            String value = extensibilityElement.getAttribute("type");
            if (value == null) {
                value = extensibilityElement.getAttribute("element");
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

    @Override
    public String getAttributeUniqueValuePrefix(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        if (attributeName.equals("name"))//NOI18N
            return NbBundle.getMessage(PropertyConfigurator.class, "PROPERTY_NAME_PREFIX");
        return null;
    }

    @Override
    public String getDefaultValue(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTypeDisplayName(ExtensibilityElement extensibilityElement, QName qname) {
        return NbBundle.getMessage(PropertyConfigurator.class, "LBL_Property_TypeDisplayName");
    }

}
