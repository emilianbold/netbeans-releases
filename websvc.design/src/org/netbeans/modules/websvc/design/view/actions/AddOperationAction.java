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

package org.netbeans.modules.websvc.design.view.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.AddWsOperationHelper;
import org.netbeans.modules.websvc.core._RetoucheUtil;
import org.netbeans.modules.websvc.design.schema2java.OperationGeneratorHelper;
import org.netbeans.modules.websvc.design.util.Util;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
public class AddOperationAction extends AbstractAction {
    
    private FileObject implementationClass;
    private Service service;
    private File wsdlFile;
    /**
     * Creates a new instance of AddOperationAction
     * @param implementationClass fileobject of service implementation class
     */
    public AddOperationAction(Service service, FileObject implementationClass) {
        super(getName());
        this.service=service;
        this.implementationClass = implementationClass;
        this.wsdlFile = getWSDLFile();
    }
    
    private static String getName() {
        return NbBundle.getMessage(AddOperationAction.class, "LBL_AddOperation");
    }
    
    private File getWSDLFile(){
        String localWsdlUrl = service.getLocalWsdlFile();
        if (localWsdlUrl!=null) { //WS from e
            JAXWSSupport support = JAXWSSupport.getJAXWSSupport(implementationClass);
            if (support!=null) {
                FileObject localWsdlFolder = support.getLocalWsdlFolderForService(service.getName(),false);
                if (localWsdlFolder!=null) {
                    File wsdlFolder = FileUtil.toFile(localWsdlFolder);
                    return  new File(wsdlFolder.getAbsolutePath()+File.separator+localWsdlUrl);
                }
            }
        }
        return null;
    }
    
    public void actionPerformed(ActionEvent arg0) {
        if(wsdlFile != null && wsdlFile.exists()){
            final AddOperationFromSchemaPanel panel = new AddOperationFromSchemaPanel(wsdlFile);
            DialogDescriptor desc = new DialogDescriptor(panel,
                    NbBundle.getMessage(AddOperationAction.class, "TTL_AddWsOperation"));
            desc.setButtonListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource() == DialogDescriptor.OK_OPTION) {
                        OperationGeneratorHelper generatorHelper = new OperationGeneratorHelper(wsdlFile);
                        
                        WSDLModel wsdlModel = Util.getWSDLModel(FileUtil.toFileObject(wsdlFile), true);
                        String operationName = panel.getOperationName();
                        GlobalElement parameterType = panel.getParameterType();
                        GlobalElement returnType = panel.getReturnType();
                        GlobalElement faultType = panel.getFaultType();
                        
                        generatorHelper.addWsOperation(wsdlModel, operationName, parameterType, returnType, faultType);
                        generatorHelper.generateJavaArtifacts(service, implementationClass, operationName);
                    }
                }
            });
            
            Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
            dialog.setVisible(true);
        } else { // WS from Java
            AddWsOperationHelper strategy = new AddWsOperationHelper(getName());
            try {
                String className = _RetoucheUtil.getMainClassName(implementationClass);
                if (className != null) {
                    strategy.addMethod(implementationClass, className);
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
}
