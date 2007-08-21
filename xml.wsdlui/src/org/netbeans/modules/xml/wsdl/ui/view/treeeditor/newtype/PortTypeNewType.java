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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype;

import java.awt.Dialog;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;

import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.view.OperationType;
import org.netbeans.modules.xml.wsdl.ui.view.PartAndElementOrTypeTableModel;
import org.netbeans.modules.xml.wsdl.ui.view.OperationConfigurationPanel;
import org.netbeans.modules.xml.wsdl.ui.wizard.PortTypeGenerator;
import org.netbeans.modules.xml.wsdl.ui.wizard.WizardPortTypeConfigurationStep;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

public class PortTypeNewType extends NewType {
    private WSDLComponent mDef = null;

    public PortTypeNewType(WSDLComponent def) {
        mDef = def;
    }
    

    @Override
    public String getName() {
        return NbBundle.getMessage(PortTypeNewType.class, "LBL_NewType_PortType");
    }


    @SuppressWarnings("unused")
    @Override
    public void create() throws IOException {
        WSDLModel model = mDef.getModel();
        

        ModelSource modelSource = model.getModelSource();
        FileObject wsdlFile = modelSource.getLookup().lookup(FileObject.class);
        if(wsdlFile != null) {
            Project project = FileOwnerQuery.getOwner(wsdlFile);
            if(project != null) {
                PortTypePanel panel = new PortTypePanel(project, model);
                
                DialogDescriptor dd = new DialogDescriptor(panel, 
                                                           NbBundle.getMessage(PortTypeNewType.class, "LBL_Create_New_PortType"), 
                                                           true, 
                                                           DialogDescriptor.OK_CANCEL_OPTION,
                                                           DialogDescriptor.OK_OPTION,
                                                           DialogDescriptor.DEFAULT_ALIGN,
                                                           new HelpCtx(PortTypeNewType.class),
                                                           null);
                panel.setDialogDescriptor(dd);
                
                Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
                dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
                dlg.setVisible(true);
                
                if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                    OperationConfigurationPanel ptPanel = panel.getPortTypeConfiguration();
                    Map configurationMap = new HashMap();
                    
                    String portTypeName = ptPanel.getPortTypeName();
                    String operationName = ptPanel.getOperationName();
                    OperationType ot = ptPanel.getOperationType();
                    
                    configurationMap.put(WizardPortTypeConfigurationStep.PORTTYPE_NAME, portTypeName);
                    configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_NAME, operationName);
                    configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_TYPE, ot);
                    
                    String inputMessageName = ptPanel.getInputMessageName();
                    String outputMessageName = ptPanel.getOutputMessageName();
                    String faultMessageName = ptPanel.getFaultMessageName();
                          
                    configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_INPUT_MESSAGE, inputMessageName);
                    configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_OUTPUT_MESSAGE, outputMessageName);
                    configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_FAULT_MESSAGE, faultMessageName);
                    
                    List<PartAndElementOrTypeTableModel.PartAndElementOrType> inputParts = ptPanel.getInputMessageParts();
                    List<PartAndElementOrTypeTableModel.PartAndElementOrType> outputParts = ptPanel.getOutputMessageParts();
                    List<PartAndElementOrTypeTableModel.PartAndElementOrType> faultParts = ptPanel.getFaultMessageParts();
                    Map<String, String> namespaceToPrefixMap = ptPanel.getNamespaceToPrefixMap();
                    
                    configurationMap.put(WizardPortTypeConfigurationStep.NAMESPACE_TO_PREFIX_MAP, namespaceToPrefixMap);
                    
                    //if inputMessage Name is new not an existing message name then populate part names as well
                    if(ptPanel.isNewInputMessage()) {
                        configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_INPUT, inputParts);
                    }
                    
                    //if outputMessage Name is new not an existing message name then populate part names as well
                    if(ptPanel.isNewOutputMessage()) {
                        configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_OUTPUT, outputParts);
                    }
                    
                    //if faultMessage Name is new not an existing message name then populate part names as well
                    if(ptPanel.isNewFaultMessage()) {
                        configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_FAULT, faultParts);
                    }
                    
                    model.startTransaction();
                    PortTypeGenerator ptGen = new PortTypeGenerator(model, configurationMap);
                    ptGen.execute();
                    
//                    SchemaImportsGenerator schemaImportGenerator = new SchemaImportsGenerator(model, configurationMap);
//                    schemaImportGenerator.execute();
//                    
                    model.endTransaction();
                    PortType pt = ptGen.getPortType();
                    ActionHelper.selectNode(pt);
                }
            }
        }
    }
}
