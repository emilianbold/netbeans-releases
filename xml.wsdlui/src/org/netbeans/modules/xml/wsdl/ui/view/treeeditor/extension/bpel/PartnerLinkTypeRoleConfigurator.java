/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator.class)
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
