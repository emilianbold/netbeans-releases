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
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.ui.api.property.ExtensibilityElementPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.api.property.StringAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author skini
 */
public class SoapBindingConfigurator extends ExtensibilityElementConfigurator {
    
    
    private static QName bindingQName = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "binding");
    private static QName operationQName = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "operation");
    
    private static QName[] supportedQNames = {bindingQName, operationQName};
    
    private static String transport = "http://schemas.xmlsoap.org/soap/http";
    
    /** Creates a new instance of SoapHeaderConfigurator */
    public SoapBindingConfigurator() {
    }
    
    @Override
    public Collection<QName> getSupportedQNames() {
        return Arrays.asList(supportedQNames);
    }
    
    @Override
    public Node.Property getProperty(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        Node.Property property = null;
        if (bindingQName.equals(qname)) {
            if ("transport".equals(attributeName)) {
                ExtensibilityElementPropertyAdapter adapter = new ExtensibilityElementPropertyAdapter(extensibilityElement, attributeName, transport);
                try {
                    property = new StringAttributeProperty(adapter, String.class, "getValue", "setValue");
                    property.setName(SOAPBinding.TRANSPORT_URI_PROPERTY);
                    property.setDisplayName(NbBundle.getMessage(SoapAddressConfigurator.class, "PROP_NAME_BINDING_TRANSPORT"));
                } catch (NoSuchMethodException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        return property;
    }
    
    
    
    @Override
    public String getDisplayAttributeName(ExtensibilityElement extensibilityElement, QName qname) {
        return null;
    }

    @Override
    public String getAttributeUniqueValuePrefix(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDefaultValue(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        if (qname.equals(bindingQName)) {
            if ("transport".equals(attributeName)) {
                return transport;
            }
        }
        return null;
    }
    
    @Override
    public String getTypeDisplayName(ExtensibilityElement extensibilityElement, QName qname) {
        if (qname.equals(bindingQName))
            return NbBundle.getMessage(SoapBindingConfigurator.class, "LBL_SoapBinding_TypeDisplayName");
        else if (qname.equals(operationQName)) 
            return NbBundle.getMessage(SoapBindingConfigurator.class, "LBL_SoapOperation_TypeDisplayName");
        return null;
    }
    
}

