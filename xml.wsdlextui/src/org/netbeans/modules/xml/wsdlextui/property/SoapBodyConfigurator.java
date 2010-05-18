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

package org.netbeans.modules.xml.wsdlextui.property;


import java.util.Arrays;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.ui.api.property.ExtensibilityElementPropertyAdapter;
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
public class SoapBodyConfigurator extends ExtensibilityElementConfigurator {


    private static QName myQName = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "body");

    private static QName[] supportedQNames = {myQName};
    /** Creates a new instance of SoapBodyConfigurator */
    public SoapBodyConfigurator() {
    }

    @Override
    public Collection<QName> getSupportedQNames() {
        return Arrays.asList(supportedQNames);
    }

    @Override
    public Node.Property getProperty(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        Node.Property property = null;
        if (myQName.equals(qname)) {
            if ("parts".equals(attributeName)) {
                try {
                    property = new PartAttributeProperty(new SoapBodyMessageProvider(extensibilityElement), extensibilityElement.getModel(), new ExtensibilityElementPropertyAdapter(extensibilityElement, attributeName), String.class, "getValue", "setValue", true);
                    property.setName(SOAPBody.PARTS_PROPERTY);
                    property.setDisplayName(NbBundle.getMessage(SoapAddressConfigurator.class, "PROP_NAME_BODY_PARTS"));
                } catch (NoSuchMethodException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        return property;
    }


    static class SoapBodyMessageProvider implements MessageProvider {
        private ExtensibilityElement element;

        public SoapBodyMessageProvider(ExtensibilityElement elem) {
            element = elem;
        }

        public String getMessage() {
            return null;
        }

        public Message getWSDLMessage() {
            WSDLComponent component = element.getParent();
            Message message = null;
            if (component instanceof BindingInput) {
                BindingInput bi = (BindingInput)component;
                if (bi.getInput() != null) {
                    Input input = bi.getInput().get();
                    if (input != null && input.getMessage() != null) {
                        message = input.getMessage().get();
                        return message;
                    }
                }
            } else if (component instanceof BindingOutput) {
                BindingOutput bi = (BindingOutput)component;
                if (bi.getOutput() != null) {
                    Output output = bi.getOutput().get();
                    if (output != null && output.getMessage() != null) {
                        message = output.getMessage().get();
                        return message;
                    }
                }
            }
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
        return NbBundle.getMessage(SoapBodyConfigurator.class, "LBL_SoapBody_TypeDisplayName");
    }
}
