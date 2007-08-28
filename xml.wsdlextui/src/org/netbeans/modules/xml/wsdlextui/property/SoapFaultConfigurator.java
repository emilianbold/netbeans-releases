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

import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.ui.api.property.ExtensibilityElementPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.api.property.ReadOnlyProperty;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author skini
 */
public class SoapFaultConfigurator extends ExtensibilityElementConfigurator {
    
    
    private static QName faultQName = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "fault");
    
    private static QName[] supportedQNames = {faultQName};
    /** Creates a new instance of SoapHeaderConfigurator */
    public SoapFaultConfigurator() {
    }
    
    @Override
    public Collection<QName> getSupportedQNames() {
        return Arrays.asList(supportedQNames);
    }
    
    @Override
    public Node.Property getProperty(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        Node.Property property = null;
        if (faultQName.equals(qname)) {
            if ("name".equals(attributeName)) {
                String displayName = NbBundle.getMessage(SoapFaultConfigurator.class, "PROP_NAME_FAULT_NAME");
                property = new ReadOnlyProperty(new ExtensibilityElementPropertyAdapter(extensibilityElement, "name"), SOAPFault.NAME_PROPERTY, String.class, displayName, null);
            }
        }
        return property;
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
        if ("name".equals(attributeName)) {
            BindingFault fault = (BindingFault) extensibilityElement.getParent();
            return fault.getName();
        }
        return null;
    }
    
    @Override
    public String getTypeDisplayName(ExtensibilityElement extensibilityElement, QName qname) {
        return NbBundle.getMessage(SoapFaultConfigurator.class, "LBL_SoapFault_TypeDisplayName");
    }
}

