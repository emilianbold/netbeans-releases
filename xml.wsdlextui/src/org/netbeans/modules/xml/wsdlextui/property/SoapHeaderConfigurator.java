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

package org.netbeans.modules.xml.wsdlextui.property;


import java.util.Arrays;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.ui.api.property.ExtensibilityElementPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.api.property.MessageAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.api.property.MessageProvider;
import org.netbeans.modules.xml.wsdl.ui.api.property.PartAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author skini
 */
public class SoapHeaderConfigurator extends ExtensibilityElementConfigurator {
    
    
    private static QName headerQName = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "header");
    private static QName headerFaultQName = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "headerfault");
    
    private static QName[] supportedQNames = {headerQName, headerFaultQName};
    /** Creates a new instance of SoapHeaderConfigurator */
    public SoapHeaderConfigurator() {
    }
    
    @Override
    public Collection<QName> getSupportedQNames() {
        return Arrays.asList(supportedQNames);
    }
    
    @Override
    public Node.Property getProperty(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        Node.Property property = null;
        if (headerQName.equals(qname) || headerFaultQName.equals(qname)) {
            if ("message".equals(attributeName)) {
                try {
                    property = new MessageAttributeProperty(new ExtensibilityElementPropertyAdapter(extensibilityElement, attributeName), extensibilityElement, String.class, "getValue", "setValue");
                    property.setName(SOAPHeader.MESSAGE_PROPERTY);
                    property.setDisplayName(NbBundle.getMessage(SoapAddressConfigurator.class, "PROP_NAME_HEADER_MESSAGE"));
                } catch (NoSuchMethodException e) {
                    ErrorManager.getDefault().notify(e);
                }
            } else if ("part".equals(attributeName)) {
                MessageProvider prov = new SoapHeaderMessageProvider(extensibilityElement);
                try {
                    property = new PartAttributeProperty(prov, extensibilityElement.getModel(), new ExtensibilityElementPropertyAdapter(extensibilityElement, attributeName), String.class, "getValue", "setValue", false);
                    property.setName(SOAPHeader.PART_PROPERTY);
                    property.setDisplayName(NbBundle.getMessage(SoapAddressConfigurator.class, "PROP_NAME_HEADER_PART"));
                } catch (NoSuchMethodException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        return property;
    }
    
    
    static class SoapHeaderMessageProvider implements MessageProvider {
        private ExtensibilityElement element;
        
        public SoapHeaderMessageProvider(ExtensibilityElement elem) {
            element = elem;
        }
        
        public String getMessage() {
            return element.getAttribute("message");
        }

        public Message getWSDLMessage() {
            return null;
        }
        
    }
    
    
    @Override
    public String getDisplayAttributeName(ExtensibilityElement extensibilityElement, QName qname) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAttributeUniqueValuePrefix(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDefaultValue(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String getTypeDisplayName(ExtensibilityElement extensibilityElement, QName qname) {
        if (qname.equals(headerQName))
            return NbBundle.getMessage(SoapHeaderConfigurator.class, "LBL_SoapHeader_TypeDisplayName");
        else if (qname.equals(headerFaultQName))  
            return NbBundle.getMessage(SoapHeaderConfigurator.class, "LBL_SoapHeaderFault_TypeDisplayName");
        return null;
    }
    
}

