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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.extension.bpel;


import java.util.Arrays;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.ui.api.property.ExtensibilityElementPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.api.property.PortTypeAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

public class PartnerLinkTypeRoleConfigurator extends
        ExtensibilityElementConfigurator {
    
    private static QName myQName = BPELQName.ROLE.getQName();
    
    private static QName[] supportedQNames = {myQName};

    @Override
    public Collection<QName> getSupportedQNames() {
        return Arrays.asList(supportedQNames);
    }
    
    @Override
    public Property getProperty(ExtensibilityElement extensibilityElement,
            QName qname, String attributeName) {
        if (myQName.equals(qname)) {
            if ("portType".equals(attributeName)) {
                ExtensibilityElementPropertyAdapter adapter = new ExtensibilityElementPropertyAdapter(extensibilityElement, attributeName);
                try {
                    Node.Property attrValueProperty = new PortTypeAttributeProperty(adapter, String.class, "getValue", "setValue");
                    attrValueProperty.setName(Role.PORT_TYPE_PROPERTY);
                    attrValueProperty.setDisplayName(NbBundle.getMessage(PartnerLinkTypeRoleConfigurator.class, "PROPERTY_NAME_PORTTYPE"));
                    return attrValueProperty;
                } catch (NoSuchMethodException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        return null;
    }

    @Override
    public String getDisplayAttributeName(ExtensibilityElement extensibilityElement, QName qname) {
        return "name"; //NO I18N
    }


    @Override
    public String getAttributeUniqueValuePrefix(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        // TODO Auto-generated method stub
        if (attributeName.equals("name")) {
            return NbBundle.getMessage(PartnerLinkTypeRoleConfigurator.class, "PARTNERLINKTYPEROLE_NAME_PREFIX");
        }
        return null;
    }

    @Override
    public String getDefaultValue(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTypeDisplayName(ExtensibilityElement extensibilityElement, QName qname) {
        return NbBundle.getMessage(PartnerLinkTypeRoleConfigurator.class, "LBL_PartnerLinkTypeRole_TypeDisplayName");
    }

    @Override
    public String getHtmlDisplayNameDecoration (ExtensibilityElement extensibilityElement, QName qname) {
        String portType = extensibilityElement.getAttribute("portType");
        if (portType == null || portType.trim().length() == 0) {
            return NbBundle.getMessage(PartnerLinkTypeRoleConfigurator.class, "LBL_PortTypeNotSet");
        }
        String[] splits = portType.split(":");
        if (splits.length == 2) {
            String ns = Utility.getNamespaceURI(splits[0], extensibilityElement);
            PortType portTypeObj = extensibilityElement.getModel().findComponentByName(new QName(ns, splits[1]), PortType.class);
            if (portTypeObj != null) {
                return NbBundle.getMessage(PartnerLinkTypeRoleConfigurator.class, "LBL_PortTypeDecorator", 
                        Utility.getNameAndDropPrefixIfInCurrentModel(ns, portTypeObj.getName(), extensibilityElement.getModel()));
            }
        }
        return NbBundle.getMessage(PartnerLinkTypeRoleConfigurator.class, "LBL_PortTypeDecorator", portType);
    }

}
