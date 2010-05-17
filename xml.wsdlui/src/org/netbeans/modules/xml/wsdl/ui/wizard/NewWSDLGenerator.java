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

/*
 * NewWSDLGenerator.java
 *
 * Created on September 1, 2006, 3:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.wizard;

import org.netbeans.modules.xml.wsdl.ui.wizard.common.PortTypeGenerator;
import org.netbeans.modules.xml.wsdl.ui.wizard.common.BindingGenerator;
import org.netbeans.modules.xml.wsdl.ui.wizard.common.WSDLWizardConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.ui.view.OperationType;
import org.netbeans.modules.xml.wsdl.ui.view.PartAndElementOrTypeTableModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;


/**
 *
 * @author radval
 */
public class NewWSDLGenerator {
    
    private FileObject mWsdlFile;
    
    private TemplateWizard mTemplateWizard;
    
    private WSDLModel mModel;
   
    //private WsdlGenerationUtil mUtil;
    
    /** Creates a new instance of NewWSDLGenerator */
    public NewWSDLGenerator(FileObject newWSDLFile, TemplateWizard templateWizard) {
        this.mWsdlFile = newWSDLFile;
        this.mTemplateWizard = templateWizard;
        ModelSource modelSource = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(this.mWsdlFile, 
			true);
        
        mModel = WSDLModelFactory.getDefault().getModel(modelSource);
        //this.mUtil = new WsdlGenerationUtil(this.mModel);
    }
    
    public void generate() {
        
        if(mModel != null) {
            mModel.startTransaction();
            
            Map configurationMap = new HashMap();
            
            //portType
            String portTypeName = (String) this.mTemplateWizard.getProperty(WSDLWizardConstants.PORTTYPE_NAME);
            String operationName = (String) this.mTemplateWizard.getProperty(WSDLWizardConstants.OPERATION_NAME);
            OperationType ot = (OperationType) this.mTemplateWizard.getProperty(WSDLWizardConstants.OPERATION_TYPE);
            
            configurationMap.put(WSDLWizardConstants.PORTTYPE_NAME, portTypeName);
            configurationMap.put(WSDLWizardConstants.OPERATION_NAME, operationName);
            configurationMap.put(WSDLWizardConstants.OPERATION_TYPE, ot);
           
            //opertion type
            List<PartAndElementOrTypeTableModel.PartAndElementOrType> inputMessageParts = 
                    (List<PartAndElementOrTypeTableModel.PartAndElementOrType>) this.mTemplateWizard.getProperty(WSDLWizardConstants.OPERATION_INPUT);
            
            List<PartAndElementOrTypeTableModel.PartAndElementOrType> outputMessageParts = 
                    (List<PartAndElementOrTypeTableModel.PartAndElementOrType>) this.mTemplateWizard.getProperty(WSDLWizardConstants.OPERATION_OUTPUT);
            
            List<PartAndElementOrTypeTableModel.PartAndElementOrType> faultMessageParts = 
                    (List<PartAndElementOrTypeTableModel.PartAndElementOrType>) this.mTemplateWizard.getProperty(WSDLWizardConstants.OPERATION_FAULT);

            
            Map<String, String> namespaceToPrefixMap = (Map<String, String>) mTemplateWizard.getProperty(WSDLWizardConstants.NAMESPACE_TO_PREFIX_MAP);
            configurationMap.put(WSDLWizardConstants.OPERATION_INPUT, inputMessageParts);
            configurationMap.put(WSDLWizardConstants.OPERATION_OUTPUT, outputMessageParts);
            configurationMap.put(WSDLWizardConstants.OPERATION_FAULT, faultMessageParts);
            configurationMap.put(WSDLWizardConstants.NAMESPACE_TO_PREFIX_MAP, namespaceToPrefixMap);
            //binding
            String bindingName = (String) this.mTemplateWizard.getProperty(WSDLWizardConstants.BINDING_NAME);
            LocalizedTemplateGroup bindingType = (LocalizedTemplateGroup) this.mTemplateWizard.getProperty(WSDLWizardConstants.BINDING_TYPE);
            configurationMap.put(WSDLWizardConstants.BINDING_NAME, bindingName);
            configurationMap.put(WSDLWizardConstants.BINDING_TYPE, bindingType);
           
            //this could be null for a binding which does not have a sub type
            LocalizedTemplate bindingSubType = (LocalizedTemplate) this.mTemplateWizard.getProperty(WSDLWizardConstants.BINDING_SUBTYPE);
            configurationMap.put(WSDLWizardConstants.BINDING_SUBTYPE, bindingSubType);
            
            //service and port
            String serviceName = (String) this.mTemplateWizard.getProperty(WSDLWizardConstants.SERVICE_NAME);
            String servicePortName = (String) this.mTemplateWizard.getProperty(WSDLWizardConstants.SERVICEPORT_NAME);
            configurationMap.put(WSDLWizardConstants.SERVICE_NAME, serviceName);
            configurationMap.put(WSDLWizardConstants.SERVICEPORT_NAME, servicePortName);
            
            mModel.getDefinitions().setName((String) configurationMap.get(WsdlPanel.WSDL_DEFINITION_NAME));
            
            if (namespaceToPrefixMap != null) {
                for (String namespace : namespaceToPrefixMap.keySet()) {
                    ((AbstractDocumentComponent) mModel.getDefinitions()).addPrefix(namespaceToPrefixMap.get(namespace), namespace);
                }
            }
            
            PortTypeGenerator ptGenerator = new PortTypeGenerator(this.mModel, configurationMap);
            ptGenerator.execute();
            PortType pt = ptGenerator.getPortType();
            
            if(pt != null) {
                BindingGenerator bg = new BindingGenerator(this.mModel, pt, configurationMap);
                bg.execute();
            }
            
            mModel.endTransaction();
        }
        
    }
    
    
}
