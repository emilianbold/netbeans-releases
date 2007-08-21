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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.AddWsOperationHelper;
import org.netbeans.modules.websvc.core._RetoucheUtil;
import org.netbeans.modules.websvc.design.schema2java.OperationGeneratorHelper;
import org.netbeans.modules.websvc.design.util.WSDLUtils;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.Utilities;

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
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/operation.png")));
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(AddOperationAction.class, "Hint_AddOperation"));
        putValue(MNEMONIC_KEY, Integer.valueOf(NbBundle.getMessage(AddOperationAction.class, "LBL_AddOperation_mnem_pos")));
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
                        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.
                                getMessage(AddOperationAction.class, "MSG_AddingOperation", panel.getOperationName())); //NOI18N
                        Task task = new Task(new Runnable() {
                            public void run() {
                                try{
                                    handle.start();
                                    addWSDLOperation(panel);
                                }catch(Exception e){
                                    handle.finish();
                                    ErrorManager.getDefault().notify(e);
                                }finally{
                                    handle.finish();
                                }
                            }
                        });
                        RequestProcessor.getDefault().post(task);
                    }
                }
            });
            
            Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
            dialog.setVisible(true);
        } else { // WS from Java
            try{
                // no need to create new task or progress handle, as strategy does it.
                addJavaMethod();
            }catch(IOException e){
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    private void saveImplementationClass(FileObject implementationClass) throws IOException{
        DataObject dobj = DataObject.find(implementationClass);
        if(dobj.isModified()) {
            SaveCookie cookie = dobj.getCookie(SaveCookie.class);
            if(cookie!=null) cookie.save();
        }
    }
    
    private void addJavaMethod() throws IOException{
        AddWsOperationHelper strategy = new AddWsOperationHelper(getName());
        String className = _RetoucheUtil.getMainClassName(implementationClass);
        if (className != null) {
            strategy.addMethod(implementationClass, className);
            saveImplementationClass(implementationClass);
        }
    }
    
    private void retrieveNewSchemas(Set<Schema> newSchemas) throws FileStateInvalidException,
            URISyntaxException, UnknownHostException, URISyntaxException, IOException{
        JAXWSSupport support = null;
        FileObject localWsdlFolder = null;
        for(Schema schema : newSchemas){
            FileObject schemaFO = schema.getModel().getModelSource().getLookup().lookup(FileObject.class);
            if(schemaFO != null){
                if(support == null){
                    support = JAXWSSupport.getJAXWSSupport(implementationClass);
                    localWsdlFolder = support.getLocalWsdlFolderForService(service.getName(),false);
                }
                WSUtils.retrieveResource(localWsdlFolder, schemaFO.getURL().toURI())                ;
            }
        }
    }
    
    private void addWSDLOperation(AddOperationFromSchemaPanel panel)
            throws IOException, FileStateInvalidException, URISyntaxException, UnknownHostException{
        OperationGeneratorHelper generatorHelper = new OperationGeneratorHelper(wsdlFile);
        WSDLModel wsdlModel = WSDLUtils.getWSDLModel(FileUtil.toFileObject(wsdlFile), true);
        
        Set<Schema> newSchemas = panel.getNewSchemas();
        if(newSchemas.size() > 0){
            changeSchemaLocation(newSchemas, wsdlModel);
            retrieveNewSchemas(newSchemas);
        }
        
        String operationName = panel.getOperationName();
        List<ParamModel> parameterTypes = panel.getParameterTypes();
        ReferenceableSchemaComponent returnType = panel.getReturnType();
        List<ParamModel> faultTypes = panel.getFaultTypes();
        generatorHelper.addWsOperation(generatorHelper.getPortTypeNameFromImpl(implementationClass),
                operationName, parameterTypes, returnType, faultTypes);
        generatorHelper.generateJavaArtifacts(service.getName(), implementationClass, operationName, false);
   
        saveImplementationClass(implementationClass);
    }
    
    private void changeSchemaLocation(Set<Schema> newSchemas, WSDLModel wsdlModel)
            throws FileStateInvalidException, URISyntaxException{
        Definitions definitions = wsdlModel.getDefinitions();
        Types types = definitions.getTypes();
        Collection<Schema> schemas = types.getSchemas();
        for(Schema newSchema : newSchemas){
            FileObject newSchemaFO = newSchema.getModel().getModelSource().getLookup().lookup(FileObject.class);
            URI newSchemaURI = newSchemaFO.getURL().toURI();
            for(Schema schema : schemas){
                Collection<Import> imports = schema.getImports();
                for(Import imp: imports){
                    URI importURI = new URI(imp.getSchemaLocation());
                    if(importURI.equals(newSchemaURI)){
                        try{
                            wsdlModel.startTransaction();
                            imp.setSchemaLocation(newSchemaFO.getNameExt());
                        }finally{
                            wsdlModel.endTransaction();
                        }
                    }
                }
            }
        }
    }
}


