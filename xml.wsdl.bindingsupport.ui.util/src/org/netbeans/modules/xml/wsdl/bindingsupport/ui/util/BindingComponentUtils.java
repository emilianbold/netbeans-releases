/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.wsdl.bindingsupport.ui.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.wsdl.util.BindingUtils;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;

/**
 *
 * @author jalmero
 */
public class BindingComponentUtils {

    /**
     * Selected Type
     */
    private static GlobalType mType = null;

    /**
     * Selected Schema Component
     */
    private static GlobalElement mSchemaComponent = null;

    /**
     * Prefix name space
     */
    private static String mPrefixNameSpace = "";
    
    /**
     * Allow the user to select either an Element or Type
     * @param wsdlModel
     * @return
     */
    public static boolean browseForElementOrType(Project proj,
            WSDLModel wsdlModel, SchemaComponent schemaComponent) {

        // reset values
        mType = null;
        mSchemaComponent = null;
        mPrefixNameSpace = null;

        // bring up existing Element or Type dialog feature from WSDL Editor
        boolean status = BindingUtils.launchElementOrType(proj, wsdlModel,
                schemaComponent);
        if (status) {
            mType = BindingUtils.getElementOrType();
            mSchemaComponent = BindingUtils.getSchemaComponent();
            mPrefixNameSpace = BindingUtils.getPrefixNameSpace();
        }
        return status;
    }

    /**
     * Return the selected component
     * @return GlobalElement null if none of this type is selected
     */
    public static GlobalElement getSchemaComponent() {
	return mSchemaComponent;
    }

    /**
     * Return the selected element type
     * @return GlobalType null if none of this type is selected
     */
    public static GlobalType getElementOrType() {
	return mType ;
    }

    /**
     * Return the prefix name space
     * @return String name space (eg xsd:long)
     */
    public static String getPrefixNameSpace() {
        return mPrefixNameSpace;
    }
    
    public static void addSchemaImport(SchemaComponent component, WSDLModel model) {
        if (component != null) {
            Utility.addSchemaImport(component, model);
            Utility.addNamespacePrefix(component.getModel().getSchema(), model, null);
        }
    }
    
    /**
     * Create a PartnerLinkType object based on the given PortType
     * @param portType
     * @param wsdlModel
     * @return ExtensibilityElement 
     */
    public static ExtensibilityElement createPartnerLinkType(PortType portType,
            WSDLModel wsdlModel) {
        return createPartnerLinkType(portType, wsdlModel, true);
    }
    
    /**
     * Create a PartnerLinkType object based on the given PortType.  If the
     * to-be generated name already exists, based on the stillCreateifExists
     * flag, then a unique name will be used.  Otherwise, it will not
     * create the partner link type object
     * @param portType
     * @param wsdlModel
     * @param stillCreateIfExists
     * @return ExtensibilityElement 
     */
    public static ExtensibilityElement createPartnerLinkType(PortType portType,
            WSDLModel wsdlModel, boolean stillCreateIfExists) {
        ExtensibilityElement partnerLinkTypeElement = null;
        if ((portType != null) && (wsdlModel != null)) {
            String portTypeName = portType.getName();
            String wsdlDefinitionName = wsdlModel.getDefinitions().getName();
            QName partnerLinkTypeQName = BindingUtils.getPartnerLinkTypeQName();
            QName partnerLinkTypeRoleQName = BindingUtils.
                    getPartnerLinkTypeRoleQName();
            
            if (portTypeName != null && wsdlDefinitionName != null) {
                String portTypeNamespace = portType.getModel().
                        getDefinitions().getTargetNamespace();
                if (portTypeNamespace != null) {
                    String prefix = ((AbstractDocumentComponent) wsdlModel.
                            getDefinitions()).lookupPrefix(portTypeNamespace);
                    if (prefix != null) {
                        ExtensibilityElement partnerLinkType = 
                                (ExtensibilityElement) wsdlModel.getFactory().
                                create(wsdlModel.getDefinitions(), 
                                partnerLinkTypeQName);
                        ExtensibilityElement partnerLinkTypeRole = 
                                (ExtensibilityElement) wsdlModel.getFactory().
                                create(partnerLinkType, 
                                partnerLinkTypeRoleQName);
                        partnerLinkType.addExtensibilityElement(partnerLinkTypeRole);
                        String partnerLinkTypeName = NameGenerator.
                                generateUniquePartnerLinkType(wsdlDefinitionName, 
                                partnerLinkTypeQName, wsdlModel, stillCreateIfExists);
                        if (partnerLinkTypeName != null) {
                            partnerLinkType.setAttribute("name", partnerLinkTypeName);    //NOI18N
                            partnerLinkTypeRole.setAttribute("name", portTypeName + "Role"); //NOI18N
                            partnerLinkTypeRole.setAttribute("portType", prefix + ":" + portTypeName); //NOI18N
                            partnerLinkTypeElement = partnerLinkType;
                        }
                    }
                }
            }

            if (partnerLinkTypeElement != null) {
                try {
                    if (!wsdlModel.isIntransaction()) {
                        wsdlModel.startTransaction();
                    }
                    wsdlModel.getDefinitions().
                            addExtensibilityElement(partnerLinkTypeElement);
                    List<WSDLComponent> children = 
                            partnerLinkTypeElement.getChildren();
                    if (children != null && children.size() > 0) {
                        WSDLComponent role = children.get(0);
                        Element pltElement = partnerLinkTypeElement.getPeer();
                        Element roleElement = role.getPeer();
                        Comment comment = wsdlModel.getAccess().
                                getDocumentRoot().createComment(BindingUtils.getPartnerLinkTypeComment());
                        wsdlModel.getAccess().insertBefore(pltElement, comment,
                                roleElement,
                                (AbstractDocumentComponent) partnerLinkTypeElement);
                    }
                } finally {
                    if (wsdlModel.isIntransaction()) {
                        wsdlModel.endTransaction();
                    }
                }
            }
        }
        return partnerLinkTypeElement;
    }    
    
    public static int inputBindingOperationCount(Binding binding, 
            String operationName) {
        int count = 0;
        BindingInput bindingInput = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(operationName)) {
                    bindingInput = bop.getBindingInput();
                    if (bindingInput != null) {
                        count++;
                    }                    
                }
            }
        }
        return count;         
    }
    
    public static int getInputBindingOperationCount(Port port) {
        int count = 0;
        if ((port != null) && (port.getBinding() != null)) {
            Binding binding = port.getBinding().get();        
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                BindingInput bindingInput = bop.getBindingInput();
                if (bindingInput != null) {
                    count++;
                }
            }        
        }
        return count;
    }
                     
    public static int getInputBindingOperationCount(WSDLComponent wsdlComponent) {
        int count = 0;
        BindingInput bindingInput = null;
        if (wsdlComponent != null) {   
            if (wsdlComponent instanceof Port) {
                return getInputBindingOperationCount((Port) wsdlComponent);
            }
            Definitions defs = wsdlComponent.getModel().getDefinitions();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            while (bindings.hasNext()) {
                Binding binding = bindings.next();
                if (binding.getType() == null
                        || binding.getType().get() == null) {
                    continue;
                }
          
                Collection<BindingOperation> bindingOperations =
                        binding.getBindingOperations();
                for (BindingOperation bop : bindingOperations) {                
                    bindingInput = bop.getBindingInput();
                    if (bindingInput != null) {
                        count++;
                    }                    
                }
            }
        }
        return count;         
    }
    
    public static int getOutputBindingOperationCount(Port port) {
        int count = 0;
        if ((port != null) && (port.getBinding() != null)) {
            Binding binding = port.getBinding().get();        
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                BindingOutput bindingOutput = bop.getBindingOutput();
                if (bindingOutput != null) {
                    count++;
                }
            }        
        }
        return count;
    }
    
    public static int getOutputBindingOperationCount(Binding binding, 
            String operationName) {
        int count = 0;
        BindingOutput bindingOutput = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(operationName)) {
                    bindingOutput = bop.getBindingOutput();
                    if (bindingOutput != null) {
                        count++;
                    }                    
                }
            }
        }
        return count;         
    }    
    
    public static int getOutputBindingOperationCount(WSDLComponent wsdlComponent) {
        int count = 0;
        BindingOutput bindingOutput = null;
        if (wsdlComponent != null) {   
            if (wsdlComponent instanceof Port) {
                return getOutputBindingOperationCount((Port) wsdlComponent);
            }            
            Definitions defs = wsdlComponent.getModel().getDefinitions();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            while (bindings.hasNext()) {
                Binding binding = bindings.next();
                if (binding.getType() == null
                        || binding.getType().get() == null) {
                    continue;
                }
          
                Collection<BindingOperation> bindingOperations =
                        binding.getBindingOperations();
                for (BindingOperation bop : bindingOperations) {                
                    bindingOutput = bop.getBindingOutput();
                    if (bindingOutput != null) {
                        count++;
                    }                    
                }
            }
        }
        return count;         
    }
    
    public static Collection<String> getOperationNames(Binding binding) {
        Collection<String> operationNames = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();        
            Iterator iter = bindingOperations.iterator();
            while (iter.hasNext()) {
                BindingOperation bop = (BindingOperation) iter.next();
                operationNames.add(bop.getName());
            }
        }
        return operationNames;
    }    
}
