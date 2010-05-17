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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.extension.bpel;


import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.ui.api.property.ComboBoxPropertyEditor;
import org.netbeans.modules.xml.wsdl.ui.api.property.ExtensibilityElementPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.api.property.MessageAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.api.property.MessageProvider;
import org.netbeans.modules.xml.wsdl.ui.api.property.PartAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

public class PropertyAliasConfigurator extends
        ExtensibilityElementConfigurator {
    
    private static QName myQName = BPELQName.PROPERTY_ALIAS.getQName();
    private static QName queryQName = BPELQName.QUERY.getQName();
    private static QName propertyQName = BPELQName.PROPERTY.getQName();
    
    private static QName[] supportedQNames = {myQName, queryQName};

    @Override
    public Collection<QName> getSupportedQNames() {
        return Arrays.asList(supportedQNames);
    }
    
    @Override
    public Property getProperty(ExtensibilityElement extensibilityElement,
            QName qname, String attributeName) {
        Node.Property property = null;
        if (myQName.equals(qname)) {
            if ("messageType".equals(attributeName)) {//NOI18N
                try {
                    property = new MessageAttributeProperty(new ExtensibilityElementPropertyAdapter(extensibilityElement, attributeName), extensibilityElement, String.class, "getValue", "setValue");
                    property.setName(PropertyAlias.MESSAGE_TYPE_PROPERTY);
                    property.setDisplayName(NbBundle.getMessage(PropertyConfigurator.class, "PROPERTY_NAME_PA_MESSAGETYPE"));
                } catch (NoSuchMethodException e) {
                    ErrorManager.getDefault().notify(e);
                }
            } else if ("part".equals(attributeName)) {//NOI18N
                MessageProvider prov = new MessageProviderImpl(extensibilityElement);
                try {
                    property = new PartAttributeProperty(prov, extensibilityElement.getModel(), new ExtensibilityElementPropertyAdapter(extensibilityElement, attributeName), String.class, "getValue", "setValue", false);
                    property.setName(PropertyAlias.PART_PROPERTY);
                    property.setDisplayName(NbBundle.getMessage(PropertyConfigurator.class, "PROPERTY_NAME_PA_PART"));
                } catch (NoSuchMethodException e) {
                    ErrorManager.getDefault().notify(e);
                }
            } else if ("propertyName".equals(attributeName)) {//NOI18N
                try {
                    property = new PropertyNameProperty(new ExtensibilityElementPropertyAdapter(extensibilityElement, attributeName), String.class, "getValue", "setValue");
                    property.setName(PropertyAlias.PROPERTY_NAME_PROPERTY);
                    property.setDisplayName(NbBundle.getMessage(PropertyConfigurator.class, "PROPERTY_NAME_PROPERTY_NAME"));
                } catch (NoSuchMethodException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        return property;
    }

    @Override
    public String getDisplayAttributeName(ExtensibilityElement extensibilityElement, QName qname) {
        return "propertyName"; //NOI18N
    }


    
    static class MessageProviderImpl implements MessageProvider {
        private ExtensibilityElement element;
        
        public MessageProviderImpl (ExtensibilityElement elem) {
            element = elem;
        }
        
        public String getMessage() {
            return element.getAttribute("messageType");//NOI18N
        }

        public Message getWSDLMessage() {
            return null;
        }
        
    }
    
    
    class PropertyNameProperty extends PropertySupport.Reflection {

        ExtensibilityElementPropertyAdapter adapter;
        
        public PropertyNameProperty(ExtensibilityElementPropertyAdapter instance, Class valueType, String getter, String setter) throws NoSuchMethodException {
            super(instance, valueType, getter, setter);
            adapter = instance;
        }
        
        
        @Override
        public PropertyEditor getPropertyEditor() {
            ArrayList<String> list = new ArrayList<String>();
            list.add("");
            WSDLModel model = adapter.getExtensibilityElement().getModel();
            addToPropertyList(list, model);
            
            //Get from imported wsdls too.
            Collection<WSDLModel> importedModels = Utility.getImportedDocuments(model);
            for (WSDLModel imp : importedModels ) {
                addToPropertyList(list, imp);
            }
            
            return new ComboBoxPropertyEditor(list.toArray(new String[list.size()]));
        }
        
        private void addToPropertyList(List<String> propertyList, WSDLModel model) {
            List<ExtensibilityElement> ees = model.getDefinitions().getExtensibilityElements();
            String prefix = Utility.getNamespacePrefix(model.getDefinitions().getTargetNamespace(), 
                    adapter.getExtensibilityElement());
            for (ExtensibilityElement ee : ees) {
                if (propertyQName.equals(ee.getQName())) {
                    propertyList.add(prefix + ":" + ee.getAttribute("name"));
                }
            }
        }
        
        @Override
        public boolean canWrite() {
            return XAMUtils.isWritable(adapter.getExtensibilityElement().getModel());
        }
        
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
        if (qname.equals(myQName))
            return NbBundle.getMessage(PropertyAliasConfigurator.class, "LBL_PropertyAlias_TypeDisplayName");
        else if (qname.equals(queryQName)) 
            return NbBundle.getMessage(PropertyAliasConfigurator.class, "LBL_PropertyAliasQuery_TypeDisplayName");
        return null;
    }

}
