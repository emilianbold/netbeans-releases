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

package org.netbeans.modules.websvc.design.view.actions;

import java.awt.event.ActionEvent;
import java.io.File;
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
import org.netbeans.modules.websvc.core.AddOperationCookie;
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
import org.netbeans.modules.xml.wsdl.model.PortType;
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
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.Utilities;

/**
 *
 * @author Ajit Bhate
 */
public class AddOperationAction extends AbstractAction implements AddOperationCookie {
    
    private FileObject implementationClass;
    private Service service;
    private File wsdlFile;
    /**
     * Creates a new instance of AddOperationAction
     * @param implementationClass fileobject of service implementation class
     */
    public AddOperationAction(Service service, FileObject implementationClass) {
        super(getName());
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage
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
            final String targetPortType =  OperationGeneratorHelper.getPortTypeNameFromImpl(implementationClass);
            boolean closeDialog = false;
            DialogDescriptor desc = new DialogDescriptor(panel,
                    NbBundle.getMessage(AddOperationAction.class, "TTL_AddWsOperation"));
            while (!closeDialog) {
                DialogDisplayer.getDefault().notify(desc);
                if (desc.getValue() == DialogDescriptor.OK_OPTION) {
                    if (wsdlOperationExists(panel.getWSDLModel(), panel.getOperationName(), targetPortType)) {
                        // wsdl port operation with this name already exists
                        DialogDisplayer.getDefault().notify(
                                new DialogDescriptor.Message(
                                    NbBundle.getMessage(AddOperationAction.class, "TXT_OperationExists")));
                    } else {
                        closeDialog = true;
                        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.
                                getMessage(AddOperationAction.class, "MSG_AddingOperation", panel.getOperationName())); //NOI18N
                        Task task = new Task(new Runnable() {
                            public void run() {
                                try{
                                    handle.start();
                                    addWSDLOperation(panel, targetPortType);
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
                } else {
                    closeDialog = true;
                }
            }
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
    
    private void addWSDLOperation(AddOperationFromSchemaPanel panel, String targetPortType)
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
        generatorHelper.addWsOperation(targetPortType,
                operationName, parameterTypes, returnType, faultTypes);
        saveWsdlFile(FileUtil.toFileObject(wsdlFile));
        generatorHelper.generateJavaArtifacts(service.getName(), implementationClass, operationName, false);
   
        saveImplementationClass(implementationClass);
    }
    
    private void saveWsdlFile(FileObject wsdlFile) throws DataObjectNotFoundException, IOException{
        DataObject wsdlDO = DataObject.find(wsdlFile);
        SaveCookie saveCookie = wsdlDO.getCookie(SaveCookie.class);
        if (saveCookie != null) {
            saveCookie.save();
        }
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
    
    private boolean wsdlOperationExists(WSDLModel wsdlModel, String operationName, String targetPortType) {
        assert wsdlModel != null;
        Collection<PortType> portTypes = wsdlModel.getDefinitions().getPortTypes();
        PortType port = null;
        for (PortType portType:portTypes) {
            if (portType.getName().equals(targetPortType)) {
                port = portType;
                break;
            }
        }
        if (port != null) {
            Collection<Operation> operations = port.getOperations();
            for (Operation op:operations) {
                if (op.getName().equals(operationName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addOperation(FileObject implementationClass) {
        actionPerformed(null);
    }

    public boolean isEnabledInEditor(FileObject implClass) {
        return service != null && service.getWsdlUrl() != null;
    }
}
