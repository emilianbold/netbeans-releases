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
 * PortTypeOperationNewType.java
 *
 * Created on September 8, 2006, 6:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype;


import java.awt.Dialog;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.view.OperationConfigurationPanel;
import org.netbeans.modules.xml.wsdl.ui.view.OperationType;
import org.netbeans.modules.xml.wsdl.ui.view.PartAndElementOrTypeTableModel;
import org.netbeans.modules.xml.wsdl.ui.wizard.OperationGenerator;
import org.netbeans.modules.xml.wsdl.ui.wizard.common.WSDLWizardConstants;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author radval
 */
public class PortTypeOperationNewType extends NewType {
    private PortType mPortType = null;
    
    public PortTypeOperationNewType(WSDLComponent message) {
        mPortType = (PortType) message;
    }
    

    @Override
    public String getName() {
        return NbBundle.getMessage(PortTypeOperationNewType.class, "LBL_NewType_PortTypeOperation");
    }


    @Override
  public void create() throws IOException {
        WSDLModel model = mPortType.getModel();
        

        ModelSource modelSource = model.getModelSource();
        FileObject wsdlFile = modelSource.getLookup().lookup(FileObject.class);
        if(wsdlFile != null) {
            Project project = FileOwnerQuery.getOwner(wsdlFile);
            if(project != null) {
                OperationPanel panel = new OperationPanel(project, mPortType);
                
                DialogDescriptor dd = new DialogDescriptor(panel, 
                                                           NbBundle.getMessage(PortTypeNewType.class, "LBL_Create_New_Operation"), 
                                                           true, 
                                                           DialogDescriptor.OK_CANCEL_OPTION,
                                                           DialogDescriptor.OK_OPTION,
                                                           DialogDescriptor.DEFAULT_ALIGN,
                                                           new HelpCtx(PortTypeOperationNewType.class),
                                                           null);
                panel.setDialogDescriptor(dd);
                
                Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
                dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
                dlg.setVisible(true);
                
                if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                    OperationConfigurationPanel opPanel = panel.getOperationConfigurationPanel();
                    Map configurationMap = new HashMap();
                    
                    String operationName = opPanel.getOperationName();
                    OperationType ot = opPanel.getOperationType();
                    configurationMap.put(WSDLWizardConstants.OPERATION_NAME, operationName);
                    configurationMap.put(WSDLWizardConstants.OPERATION_TYPE, ot);
                    
                    String inputMessageName = opPanel.getInputMessageName();
                    String outputMessageName = opPanel.getOutputMessageName();
                    String faultMessageName = opPanel.getFaultMessageName();
                    
                    configurationMap.put(WSDLWizardConstants.OPERATION_INPUT_MESSAGE, inputMessageName);
                    configurationMap.put(WSDLWizardConstants.OPERATION_OUTPUT_MESSAGE, outputMessageName);
                    configurationMap.put(WSDLWizardConstants.OPERATION_FAULT_MESSAGE, faultMessageName);
                    
                    List<PartAndElementOrTypeTableModel.PartAndElementOrType> inputParts = opPanel.getInputMessageParts();
                    List<PartAndElementOrTypeTableModel.PartAndElementOrType> outputParts = opPanel.getOutputMessageParts();
                    List<PartAndElementOrTypeTableModel.PartAndElementOrType> faultParts = opPanel.getFaultMessageParts();
                    Map<String, String> namespaceToPrefixMap = opPanel.getNamespaceToPrefixMap();
                    
                    configurationMap.put(WSDLWizardConstants.NAMESPACE_TO_PREFIX_MAP, namespaceToPrefixMap);
                   
                    //if inputMessage Name is new not an existing message name then populate part names as well
                    if(opPanel.isNewInputMessage()) {
                        configurationMap.put(WSDLWizardConstants.OPERATION_INPUT, inputParts);
                    }
                    
                    //if outputMessage Name is new not an existing message name then populate part names as well
                    if(opPanel.isNewOutputMessage()) {
                        configurationMap.put(WSDLWizardConstants.OPERATION_OUTPUT, outputParts);
                    }
                    
                    //if faultMessage Name is new not an existing message name then populate part names as well
                    if(opPanel.isNewFaultMessage()) {
                        configurationMap.put(WSDLWizardConstants.OPERATION_FAULT, faultParts);
                    }
                    model.startTransaction();
                    OperationGenerator opGen = new OperationGenerator(model, this.mPortType, configurationMap);
                    opGen.execute();
                    
//                    SchemaImportsGenerator schemaImportGenerator = new SchemaImportsGenerator(model, configurationMap);
//                    schemaImportGenerator.execute();
                    model.endTransaction();
                    Operation operation = opGen.getOperation();
                    ActionHelper.selectNode(operation);
                }
            }
        }
    }
}
