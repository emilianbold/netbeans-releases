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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.undo.UndoManager;
import javax.xml.namespace.QName;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxwsruntimemodel.JavaWsdlMapper;
import org.netbeans.modules.websvc.wsitconf.util.UndoManagerHolder;
import org.netbeans.modules.websvc.wsitconf.WSITEditor;
import org.netbeans.modules.websvc.wsitconf.util.AbstractTask;
import org.netbeans.modules.websvc.wsitconf.util.SourceUtils;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyReference;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author MartinG
 */
public class WSITModelSupport {
    
    /* prefix for config wsdls */
    public static final String CONFIG_WSDL_CLIENT_PREFIX = "wsit-client";       //NOI18N
    public static final String CONFIG_WSDL_SERVICE_PREFIX = "wsit-";            //NOI18N
    
    /* extensions for config wsdls */
    public static final String MAIN_CONFIG_EXTENSION = "xml";                   //NOI18N
    public static final String CONFIG_WSDL_EXTENSION = "xml";                   //NOI18N
    
    private static final Logger logger = Logger.getLogger(WSITModelSupport.class.getName());
    
    /** Creates a new instance of WSITModelSupport */
    public WSITModelSupport() {
    }
    
    public static WSDLModel getModel(Node node, JaxWsModel jaxWsModel, UndoManagerHolder umHolder, boolean create, Collection<FileObject> createdFiles) throws MalformedURLException, Exception {
        
        WSDLModel model = null;
        
        //is it a client node?
        Client client = node.getLookup().lookup(Client.class);
        
        //is it a service node?
        Service service = node.getLookup().lookup(Service.class);
        
        if (client != null) { //it is a client
            FileObject srcRoot = node.getLookup().lookup(FileObject.class);
            Project p = FileOwnerQuery.getOwner(srcRoot);
            model = getModelForClient(p, client, create, createdFiles);
        } else if (service != null) {  //it is a service
            FileObject implClass = node.getLookup().lookup(FileObject.class);
            if (jaxWsModel == null) {
                logger.log(Level.INFO, "JAX-WS Model is null: " + node);
                return null;
            }
            Project p = FileOwnerQuery.getOwner(jaxWsModel.getJaxWsFile());
            model = getModelForService(service, implClass, p, create, createdFiles);
        } else { //neither a client nor a service, get out of here
            logger.log(Level.INFO, "Unable to identify node type: " + node);
        }
        
        if ((model != null) && (umHolder != null) && (umHolder.getUndoManager() == null)) {
            UndoManager undoManager = new UndoManager();
            model.addUndoableEditListener(undoManager);  //maybe use WeakListener instead
            umHolder.setUndoManager(undoManager);
        }
        return model;
    }
    
    public static WSDLModel getModelForService(Service service, FileObject implClass, Project p, boolean create, Collection<FileObject> createdFiles) {
        try {
            String wsdlUrl = service.getWsdlUrl();
            if (wsdlUrl == null) { // WS from Java
                if (implClass == null) {
                    logger.log(Level.INFO, "Implementation class is null");
                    return null;
                }
                JAXWSSupport supp = JAXWSSupport.getJAXWSSupport(implClass);
                return getModelForServiceFromJava(implClass, supp, create, createdFiles);
            } else {
                if (p == null) return null;
                JAXWSSupport supp = JAXWSSupport.getJAXWSSupport(p.getProjectDirectory());
                return getModelForServiceFromWsdl(supp, service);
            }
        } catch (IOException ex) {
            logger.log(Level.INFO, null, ex);
        } catch (Exception e) {
            logger.log(Level.INFO, null, e);
        }
        return null;
    }
    
    protected static WSDLModel getModelFromFO(FileObject wsdlFO, boolean editable) {
        WSDLModel model = null;
        ModelSource ms = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(wsdlFO, editable);
        try {
            model = WSDLModelFactory.getDefault().getModel(ms);
            if (model != null) {
                model.sync();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return model;
    }
    
    /* Retrieves WSDL model for a WS client - always has a wsdl
     */
    public static WSDLModel getModelForClient(Project p, Client client, boolean create, Collection<FileObject> createdFiles) throws IOException {
        
        WSDLModel model = null;
        FileObject srcFolder = WSITEditor.getClientConfigFolder(p);
        FileObject catalogfo = Utilities.getProjectCatalogFileObject(p);
        ModelSource catalogms = Utilities.getModelSource(catalogfo, false);
        
        try {
            CatalogModel cm = Utilities.getCatalogModel(catalogms);
            ModelSource originalms = cm.getModelSource(URI.create(client.getWsdlUrl()));
            FileObject originalWsdlFO = Utilities.getFileObject(originalms);
            WSDLModel originalwsdlmodel = getModelFromFO(originalWsdlFO, true);
            
            // check whether config file already exists
            FileObject configFO = srcFolder.getFileObject(originalWsdlFO.getName(), CONFIG_WSDL_EXTENSION);
            if ((configFO != null) && (configFO.isValid())) {
                return getModelFromFO(configFO, true);
            }
            
            if (create) {
                // check whether main config file exists
                FileObject mainConfigFO = srcFolder.getFileObject(CONFIG_WSDL_CLIENT_PREFIX, MAIN_CONFIG_EXTENSION);
                if (mainConfigFO == null) {
                    mainConfigFO = createMainConfig(srcFolder, createdFiles);
                }
                
                copyImports(originalwsdlmodel, srcFolder, createdFiles);
                
                // import the model from client model
                WSDLModel mainModel = getModelFromFO(mainConfigFO, true);
                mainModel.startTransaction();
                try {
                    WSDLComponentFactory wcf = mainModel.getFactory();
                    
                    FileObject configName = Utilities.getFileObject(originalwsdlmodel.getModelSource());
                    configFO = srcFolder.getFileObject(configName.getName(), CONFIG_WSDL_EXTENSION);
                    
                    boolean importFound = false;
                    Collection<Import> imports = mainModel.getDefinitions().getImports();
                    for (Import i : imports) {
                        if (i.getLocation().equals(configFO.getNameExt())) {
                            importFound = true;
                            break;
                        }
                    }
                    model = getModelFromFO(configFO, true);
                    if (!importFound) {
                        org.netbeans.modules.xml.wsdl.model.Import imp = wcf.createImport();
                        imp.setLocation((configFO).getNameExt());
                        imp.setNamespace(model.getDefinitions().getTargetNamespace());
                        Definitions def = mainModel.getDefinitions();
                        def.setName("mainclientconfig"); //NOI18N
                        def.addImport(imp);
                    }
                } finally {
                    mainModel.endTransaction();
                }
                
                DataObject mainConfigDO = DataObject.find(mainConfigFO);
                if ((mainConfigDO != null) && (mainConfigDO.isModified())) {
                    SaveCookie wsdlSaveCookie = mainConfigDO.getCookie(SaveCookie.class);
                    if(wsdlSaveCookie != null){
                        wsdlSaveCookie.save();
                    }
                    mainConfigDO.setModified(false);
                }
                
                DataObject configDO = DataObject.find(configFO);
                if ((configDO != null) && (configDO.isModified())) {
                    SaveCookie wsdlSaveCookie = configDO.getCookie(SaveCookie.class);
                    if(wsdlSaveCookie != null){
                        wsdlSaveCookie.save();
                    }
                    configDO.setModified(false);
                }
            }
            
        } catch (CatalogModelException ex) {
            logger.log(Level.INFO, null, ex);
        }
        
        return model;
    }
    
    private static void copyImports(final WSDLModel model, final FileObject srcFolder, Collection<FileObject> createdFiles) throws CatalogModelException {
        
        FileObject modelFO = Utilities.getFileObject(model.getModelSource());
        
        try {
            FileObject configFO = FileUtil.copyFile(modelFO, srcFolder, modelFO.getName(), CONFIG_WSDL_EXTENSION);
            if (createdFiles != null) {
                createdFiles.add(configFO);
            }
            
            WSDLModel newModel = getModelFromFO(configFO, true);
            
            removePolicies(newModel);
            removeTypes(newModel);
            
            Collection<Import> oldImports = model.getDefinitions().getImports();
            Collection<Import> newImports = newModel.getDefinitions().getImports();
            Iterator<Import> newImportsIt = newImports.iterator();
            for (Import i : oldImports) {
                WSDLModel oldImportedModel = i.getImportedWSDLModel();
                FileObject oldImportFO = Utilities.getFileObject(oldImportedModel.getModelSource());
                newModel.startTransaction();
                try {
                    newImportsIt.next().setLocation(oldImportFO.getName() + "." + CONFIG_WSDL_EXTENSION);
                } finally {
                    newModel.endTransaction();
                }
                copyImports(oldImportedModel, srcFolder, createdFiles);
            }
        } catch (IOException e) {
            // ignore - this happens when files are imported recursively
            logger.log(Level.FINE, null, e);
        }
    }
    
    /** Creates new empty main client configuration file
     *
     */
    private static FileObject createMainConfig(FileObject folder, Collection<FileObject> createdFiles) {
        FileObject mainConfig = null;
        try {
            mainConfig = FileUtil.createData(folder, CONFIG_WSDL_CLIENT_PREFIX + "." + MAIN_CONFIG_EXTENSION); //NOI18N
            if ((mainConfig != null) && (mainConfig.isValid()) && !(mainConfig.isVirtual())) {
                if (createdFiles != null) {
                    createdFiles.add(mainConfig);
                }
                FileWriter fw = new FileWriter(FileUtil.toFile(mainConfig));
                fw.write(NbBundle.getMessage(WSITEditor.class, "EMPTY_WSDL"));       //NOI18N
                fw.close();
                mainConfig.refresh(true);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return mainConfig;
    }
    
    public static WSDLModel getMainClientModel(FileObject folder) {
        WSDLModel model = null;
        if (folder != null) {
            FileObject mainConfig = folder.getFileObject(CONFIG_WSDL_CLIENT_PREFIX, MAIN_CONFIG_EXTENSION); //NOI18N
            if (mainConfig != null) {
                model = getModelFromFO(mainConfig, true);
            }
        }
        return model;
    }
    
    /* Retrieves WSDL model for a WS from Java - if config file exists, reuses that one, otherwise generates new one
     */
    public static WSDLModel getServiceModelForClient(JAXWSClientSupport supp, Client client) throws IOException, Exception {
        FileObject originalWsdlFolder = supp.getLocalWsdlFolderForClient(client.getName(), false);
        FileObject originalWsdlFO = originalWsdlFolder.getFileObject(client.getLocalWsdlFile());
        
        if ((originalWsdlFO != null) && (originalWsdlFO.isValid())) {   //NOI18N
            return getModelFromFO(originalWsdlFO, false);
        }
        return null;
    }
    
    private static WSDLModel getModelForServiceFromWsdl(JAXWSSupport supp, Service service) throws IOException, Exception {
        String wsdlLocation = service.getLocalWsdlFile();
        FileObject wsdlFO = supp.getLocalWsdlFolderForService(service.getName(),false).getFileObject(File.separator + wsdlLocation);
        return getModelFromFO(wsdlFO, true);
    }
    
    /* Retrieves WSDL model for a WS from Java - if config file exists, reuses that one, otherwise generates new one
     */
    public static WSDLModel getModelForServiceFromJava(FileObject jc, JAXWSSupport supp, boolean create, Collection<FileObject> createdFiles) throws IOException {
        
        WSDLModel model = null;
        String configWsdlName = CONFIG_WSDL_SERVICE_PREFIX;
        
        try {
            if (jc == null) return null;
            final java.lang.String[] result = new java.lang.String[1];
            
            JavaSource js = JavaSource.forFileObject(jc);
            js.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws java.io.IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                    result[0] = sourceUtils.getTypeElement().getQualifiedName().toString();
                }
            }, true);
            
            configWsdlName += result[0];
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        // check whether config file already exists
        if (supp.getWsdlFolder(false) != null) {
            FileObject wsdlFO = supp.getWsdlFolder(false).getParent().getFileObject(configWsdlName, CONFIG_WSDL_EXTENSION);  //NOI18N
            if ((wsdlFO != null) && (wsdlFO.isValid())) {   //NOI18N
                return getModelFromFO(wsdlFO, true);
            }
        }
        
        if (create) {
            // config file doesn't exist - generate empty file
            FileObject wsdlFolder = supp.getWsdlFolder(true).getParent();
            FileObject wsdlFO = wsdlFolder.getFileObject(configWsdlName, CONFIG_WSDL_EXTENSION);   //NOI18N
            if ((wsdlFO == null) || !(FileUtil.toFile(wsdlFO).exists())) {
                wsdlFO = wsdlFolder.createData(configWsdlName, CONFIG_WSDL_EXTENSION);  //NOI18N
                if (createdFiles != null) {
                    createdFiles.add(wsdlFO);
                }
                FileWriter fw = new FileWriter(FileUtil.toFile(wsdlFO));
                fw.write(NbBundle.getMessage(WSITEditor.class, "EMPTY_WSDL"));       //NOI18N
                fw.close();
                wsdlFO.refresh(true);
            }
            
            // and fill it with values
            model = createModelFromFO(wsdlFO, jc);
            wsdlFO.refresh(true);
        }
        
        return model;
    }
    
    private static WSDLModel createModelFromFO(FileObject wsdlFO, FileObject jc) {
        WSDLModel model = null;
        ModelSource ms = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(wsdlFO, true);
        try {
            model = WSDLModelFactory.getDefault().getModel(ms);
            if (model != null) {
                Definitions d = model.getDefinitions();
                if (d != null) {
                    
                    model.startTransaction();
                    try {
                        WSDLComponentFactory wcf = model.getFactory();
                        QName serviceQName = JavaWsdlMapper.getServiceName(jc);
                        String serviceLocalName = serviceQName.getLocalPart();
                        String serviceTargetNamespace = serviceQName.getNamespaceURI();
                        
                        d.setName(serviceLocalName);
                        d.setTargetNamespace(serviceTargetNamespace);
                        
                        // create service element and add it to definitions
                        org.netbeans.modules.xml.wsdl.model.Service s = wcf.createService();
                        s.setName(serviceLocalName);
                        d.addService(s);
                        
                        // create port and add it to the service element
                        org.netbeans.modules.xml.wsdl.model.Port port = wcf.createPort();
                        QName portName = JavaWsdlMapper.getPortName(jc, serviceTargetNamespace);
                        if (portName != null) {
                            port.setName(portName.getLocalPart());
                        }
                        s.addPort(port);
                        
                        // create binding and add it to the service element
                        org.netbeans.modules.xml.wsdl.model.Binding binding = wcf.createBinding();
                        String bindingName = JavaWsdlMapper.getBindingName(jc, serviceTargetNamespace);
                        binding.setName(bindingName);
                        d.addBinding(binding);
                        
                        // attach binding to the port
                        port.setBinding(binding.createReferenceTo(binding, org.netbeans.modules.xml.wsdl.model.Binding.class));
                        
                        // create portType and add it to the definitions element
                        org.netbeans.modules.xml.wsdl.model.PortType portType = wcf.createPortType();
                        QName portTypeName = JavaWsdlMapper.getPortTypeName(jc);
                        portType.setName(portTypeName.getLocalPart());
                        d.addPortType(portType);
                        
                        // create operations and add them to the binding element
                        List<String> bindingOperationNames = JavaWsdlMapper.getOperationNames(jc);
                        for (String name : bindingOperationNames) {
                            
                            org.netbeans.modules.xml.wsdl.model.BindingOperation bindingOperation = wcf.createBindingOperation();
                            bindingOperation.setName(name);
                            binding.addBindingOperation(bindingOperation);
                            
                            org.netbeans.modules.xml.wsdl.model.Message inputMsg = wcf.createMessage();
                            inputMsg.setName(name);
                            d.addMessage(inputMsg);
                            
                            org.netbeans.modules.xml.wsdl.model.Message outMsg = wcf.createMessage();
                            outMsg.setName(name + "Response");                  //NOI18N
                            d.addMessage(outMsg);
                            
                            org.netbeans.modules.xml.wsdl.model.RequestResponseOperation oper = wcf.createRequestResponseOperation();
                            oper.setName(name);
                            portType.addOperation(oper);
                            
                            List<String> faults = JavaWsdlMapper.getOperationFaults(jc, name);
                            for (String faultstr : faults) {
                                org.netbeans.modules.xml.wsdl.model.Message fMsg = wcf.createMessage();
                                fMsg.setName(faultstr);
                                d.addMessage(fMsg);
                                
                                org.netbeans.modules.xml.wsdl.model.Fault fault = wcf.createFault();
                                fault.setName(faultstr);
                                oper.addFault(fault);
                                fault.setMessage(fault.createReferenceTo(fMsg, org.netbeans.modules.xml.wsdl.model.Message.class));
                            }
                            
                            org.netbeans.modules.xml.wsdl.model.Input input = wcf.createInput();
                            oper.setInput(input);
                            input.setMessage(input.createReferenceTo(inputMsg, org.netbeans.modules.xml.wsdl.model.Message.class));
                            
                            org.netbeans.modules.xml.wsdl.model.Output out = wcf.createOutput();
                            oper.setOutput(out);
                            out.setMessage(out.createReferenceTo(outMsg, org.netbeans.modules.xml.wsdl.model.Message.class));
                            
                            org.netbeans.modules.xml.wsdl.model.BindingOutput bindingOutput = wcf.createBindingOutput();
                            bindingOperation.setBindingOutput(bindingOutput);
                            org.netbeans.modules.xml.wsdl.model.BindingInput bindingInput = wcf.createBindingInput();
                            bindingOperation.setBindingInput(bindingInput);
                            
                            //add faults
                            List<String> operationFaults = JavaWsdlMapper.getOperationFaults(jc, name);
                            for (String fault : operationFaults) {
                                org.netbeans.modules.xml.wsdl.model.BindingFault bindingFault = wcf.createBindingFault();
                                bindingFault.setName(fault);
                                bindingOperation.addBindingFault(bindingFault);
                            }
                        }
                        
                        // attach portType to the binding
                        binding.setType(binding.createReferenceTo(portType, org.netbeans.modules.xml.wsdl.model.PortType.class));
                    } finally {
                        model.endTransaction();
                    }
                    
                    DataObject dO = DataObject.find(wsdlFO);
                    SaveCookie sc = dO.getCookie(SaveCookie.class);
                    sc.save();
                    dO.setModified(false);
                }
            }
        } catch (IOException ex) {
            logger.log(Level.INFO, null, ex);
        }
        return model;
    }
    
    private static void removeTypes(WSDLModel model) {
        model.startTransaction();
        try {
            Definitions d = model.getDefinitions();
            Types t = d.getTypes();
            if (t != null) {
                t.getSchemas().retainAll(new ArrayList());
            }
        } finally {
            model.endTransaction();
        }
    }
    
    private static void removePolicies(WSDLModel model) {
        model.startTransaction();
        try {
            removePolicyElements(model.getDefinitions());
        } finally {
            model.endTransaction();
        }
    }
    
    public static boolean isServiceFromWsdl(Node node) {
        if (node != null) {
            Service service = node.getLookup().lookup(Service.class);
            return isServiceFromWsdl(service);
        }
        return false;
    }
    
    public static boolean isServiceFromWsdl(Service service) {
        if (service != null) { //it is a service
            String wsdlUrl = service.getWsdlUrl();
            if (wsdlUrl != null) { // it is a web service from wsdl
                return true;
            }
        }
        return false;
    }
    
    /* transaction has to be started before this method is called */
    private static void removePolicyElements(WSDLComponent c) {
        List<Policy> policies = c.getExtensibilityElements(Policy.class);
        for (Policy p : policies) {
            c.removeExtensibilityElement(p);
        }
        List<PolicyReference> policyReferences = c.getExtensibilityElements(PolicyReference.class);
        for (PolicyReference pr : policyReferences) {
            c.removeExtensibilityElement(pr);
        }
        List<WSDLComponent> children = c.getChildren();
        for (WSDLComponent ch : children) {
            removePolicyElements(ch);
        }
    }
    
    public static void fillImportedBindings(final WSDLModel model, Collection<Binding> bindings, Set<FileObject> traversedModels) {
        FileObject modelFO = Util.getFOForModel(model);
        // avoid neverending recursion for recursive imports
        if (traversedModels.contains(modelFO)) {
            return;
        }
        traversedModels.add(modelFO);
        
        Collection<Binding> importedBindings = model.getDefinitions().getBindings();
        bindings.addAll(importedBindings);
        
        Collection<Import> imports = model.getDefinitions().getImports();
        for (Import i : imports) {
            WSDLModel importedModel;
            try {
                importedModel = i.getImportedWSDLModel();
                fillImportedBindings(importedModel, bindings, traversedModels);
            } catch (CatalogModelException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static void save(WSDLComponent c) {
        WSDLModel model = c.getModel();
        save(model);
    }
    
    public synchronized static void save(WSDLModel model) {
        try {
            if (model != null) {
                Collection<Import> imports = model.getDefinitions().getImports();
                for (Import i : imports) {
                    WSDLModel importedModel = i.getImportedWSDLModel();
                    save(importedModel);
                }

                FileObject wsdlFO = Utilities.getFileObject(model.getModelSource());
                if (wsdlFO == null) {
                    logger.log(Level.INFO, "Cannot find fileobject in lookup for: " + model.getModelSource());
                }
                DataObject wsdlDO = DataObject.find(wsdlFO);
                if ((wsdlDO != null) && (wsdlDO.isModified())) {
                    SaveCookie wsdlSaveCookie = wsdlDO.getCookie(SaveCookie.class);
                    if(wsdlSaveCookie != null){
                        wsdlSaveCookie.save();
                    }
                    wsdlDO.setModified(false);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }
    
    public static Binding getBinding(Service service, FileObject implClass, Project project, boolean create, Collection<FileObject> createdFiles) {
        String portName = service.getPortName();
        String serviceName = service.getServiceName();
        WSDLModel model = WSITModelSupport.getModelForService(service, implClass, project, create, createdFiles);
        if (model == null) return null;
        Definitions definitions = model.getDefinitions();
        Collection<Binding> bindings = definitions.getBindings();
        if ((bindings == null) || (bindings.isEmpty())) return null;
        if (bindings.size() == 1) return bindings.iterator().next();
        Collection<org.netbeans.modules.xml.wsdl.model.Service> services = definitions.getServices();
        for (org.netbeans.modules.xml.wsdl.model.Service s : services) {
            if (serviceName.equals(s.getName())) {
                Collection<Port> ports = s.getPorts();
                for (Port p : ports) {
                    if (portName.equals(p.getName())) {
                        QName b = p.getBinding().getQName();
                        return model.findComponentByName(b, Binding.class);
                    }
                }
            }
        }
        return null;
    }
}
