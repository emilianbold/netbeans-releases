
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

package org.netbeans.modules.websvc.wsitconf;

import javax.swing.undo.UndoManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxwsruntimemodel.JavaWsdlMapper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.PolicyReference;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import javax.xml.namespace.QName;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

/**
 *
 * @author Martin Grebac
 */
public class WSITEditor {

    /* extension for config wsdls */
    public static final String CONFIG_WSDL_CLIENT_PREFIX = "wsit-client";       //NOI18N
    public static final String CONFIG_WSDL_SERVICE_PREFIX = "wsit";             //NOI18N
    
    /* extension for config wsdls */
    public static final String CONFIG_WSDL_EXTENSION = "xml";                   //NOI18N

    protected WSDLModel model;

    protected UndoManager undoManager;
    
    /**
     * Creates a new instance of WSITEditor
     */
    public WSITEditor() { }
    
    public WSDLModel getModel(Node node, JaxWsModel jaxWsModel) throws MalformedURLException, Exception {
                        
        //is it a client node?
        Client client = (Client)node.getLookup().lookup(Client.class);

        //is it a service node?
        Service service = (Service)node.getLookup().lookup(Service.class);

        if (client != null) { //it is a client
            return getModelForClient(node, jaxWsModel, client);

        } else if (service != null) {  //it is a service
            try {
                String wsdlUrl = service.getWsdlUrl();
                if (wsdlUrl == null) { // WS from Java
                    model = getModelForServiceFromJava(node, jaxWsModel, service);
                } else {
                    model = getModelForServiceFromWsdl(node, jaxWsModel, service);
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, ioe.getMessage());
            }            
        } else { //neither a client nor a service, get out of here
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Unable to identify node type: " + node); //NOI18N
        }
        
        if ((model != null) && (undoManager == null)) {
            undoManager = new UndoManager();
            undoManager.setLimit(1000);
            model.addUndoableEditListener(undoManager);  //maybe use WeakListener instead
        }
        return model;
    }
    
    /* Retrieves WSDL model for a WS client - always has a wsdl
     */ 
    protected WSDLModel getModelForClient(Node node, JaxWsModel jaxWsModel, Client client) throws IOException {

        FileObject srcRoot = (FileObject) node.getLookup().lookup(FileObject.class);
        JAXWSClientSupport supp = JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
        
        FileObject originalWsdlFolder = supp.getLocalWsdlFolderForClient(client.getName(), false);
        FileObject originalWsdlFO = originalWsdlFolder.getFileObject(client.getLocalWsdlFile());
        
        String configWsdlName = CONFIG_WSDL_CLIENT_PREFIX;

        // check whether config file already exists
        FileObject wsdlFO = srcRoot.getFileObject(configWsdlName, CONFIG_WSDL_EXTENSION);  //NOI18N
        if ((wsdlFO != null) && (wsdlFO.isValid())) {   //NOI18N
            model = getModelFromFO(wsdlFO); 
            return model;
        }

        // config file doesn't exist - copy the wsdl, rename it as a config file and create wsdl model from it
        wsdlFO = FileUtil.copyFile(originalWsdlFO, srcRoot, configWsdlName, CONFIG_WSDL_EXTENSION);
        if (wsdlFO != null) {
            model = getModelFromFO(wsdlFO);
            removePolicies(model);
        }
        return model;
    }
    
    private FileObject getWsdlFO(FileObject folder, String wsdlLocation) {
        String relativePath = wsdlLocation.substring(wsdlLocation.indexOf("/wsdl/") + 6);
        return folder.getFileObject(relativePath);
    }
    
    /* Retrieves WSDL model for a WS from Java - if config file exists, reuses that one, otherwise generates new one
     */ 
    public WSDLModel getModelForServiceFromWsdl(Node node, JaxWsModel jaxWsModel, Service service) throws IOException, Exception {
        
        FileObject wsdlFO = null;
        
        Project p = FileOwnerQuery.getOwner(jaxWsModel.getJaxWsFile());
        JAXWSSupport supp = JAXWSSupport.getJAXWSSupport(p.getProjectDirectory());
        String wsdlLocation = supp.getWsdlLocation(service.getName());

        wsdlFO = getWsdlFO(supp.getWsdlFolder(false), wsdlLocation);
        model = getModelFromFO(wsdlFO);
        
        return model;
    }

    /* Retrieves WSDL model for a WS from Java - if config file exists, reuses that one, otherwise generates new one
     */ 
    private WSDLModel getModelForServiceFromJava(Node node, JaxWsModel jaxWsModel, Service service) throws IOException, Exception {
        
        Project p = FileOwnerQuery.getOwner(jaxWsModel.getJaxWsFile());
        JAXWSSupport supp = JAXWSSupport.getJAXWSSupport(p.getProjectDirectory());

//        FileObject srcRoot = (FileObject) node.getLookup().lookup(FileObject.class);
        
        /* !!!!!!!!!!!! HACK TO WORKAROUND BUG IN GLASSFISH & WSIT INTEGRATION !!!!!!!!!!!!!!! */
//        WebModule wm = WebModule.getWebModule(p.getProjectDirectory());
//        FileObject webxml = wm.getDeploymentDescriptor();
        
//        WebApp webapp = DDProvider.getDefault().getDDRoot(webxml);
//        if (WebApp.VERSION_2_5.equals(webapp.getVersion())) {
//            webapp.g
//        }
        
        /* !!!!!!!!!!!! END OF HACK !!!!!!!!!!!!!!! */

        JavaClass jc = (JavaClass)node.getLookup().lookup(JavaClass.class);

        String configWsdlName = CONFIG_WSDL_SERVICE_PREFIX;

        // check whether config file already exists
        if (supp.getWsdlFolder(false) != null) {
            FileObject wsdlFO = supp.getWsdlFolder(false).getParent().getFileObject(configWsdlName, CONFIG_WSDL_EXTENSION);  //NOI18N
            if ((wsdlFO != null) && (wsdlFO.isValid())) {   //NOI18N
                model = getModelFromFO(wsdlFO); 
                return model;
            }
        }

        // config file doesn't exist - generate empty file
        FileObject wsdlFolder = supp.getWsdlFolder(true).getParent();
        FileObject wsdlFO = wsdlFolder.getFileObject(configWsdlName, CONFIG_WSDL_EXTENSION);   //NOI18N
        if ((wsdlFO == null) || !(FileUtil.toFile(wsdlFO).exists())) {
            wsdlFO = wsdlFolder.createData(configWsdlName, CONFIG_WSDL_EXTENSION);  //NOI18N
            FileWriter fw = new FileWriter(FileUtil.toFile(wsdlFO));
            fw.write(NbBundle.getMessage(WSITEditor.class, "EMPTY_WSDL"));       //NOI18N
            fw.close();
            wsdlFO.refresh(true);
        }

        // and fill it with values
        model = createModelFromFO(wsdlFO, jc);
        wsdlFO.refresh(true);
        return model;
    }
    
    private void removePolicies(WSDLModel model) {
        model.startTransaction();
        removePolicyElements(model.getDefinitions());
            model.endTransaction();
    }
    
    /* transaction has to be started before this method is called */
    private void removePolicyElements(WSDLComponent c) {
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
    
    private WSDLModel createModelFromFO(FileObject wsdlFO, JavaClass jc) {
        WSDLModel model = null;
        ModelSource ms = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(wsdlFO, true);
        try {
            model = WSDLModelFactory.getDefault().getModel(ms);
            if (model != null) {
                Definitions d = model.getDefinitions();
                if (d != null) {
                    
                    model.startTransaction();

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

                        // add input/output messages
//                        int operationDirection = JavaWsdlMapper.getParamDirections(jc, name);

//                        if (operationDirection == JavaWsdlMapper.OUTPUTINPUT) {
                            org.netbeans.modules.xml.wsdl.model.Message inputMsg = wcf.createMessage();
                            inputMsg.setName(name);
                            d.addMessage(inputMsg);

                            org.netbeans.modules.xml.wsdl.model.Message outMsg = wcf.createMessage();
                            outMsg.setName(name + "Response");                  //NOI18N
                            d.addMessage(outMsg);

                            org.netbeans.modules.xml.wsdl.model.RequestResponseOperation oper = wcf.createRequestResponseOperation();
                            oper.setName(name);
                            portType.addOperation(oper);
                            
                            org.netbeans.modules.xml.wsdl.model.Input input = wcf.createInput();
                            oper.setInput(input);
                            input.setMessage(input.createReferenceTo(inputMsg, org.netbeans.modules.xml.wsdl.model.Message.class));

                            org.netbeans.modules.xml.wsdl.model.Output out = wcf.createOutput();
                            oper.setOutput(out);
                            out.setMessage(out.createReferenceTo(outMsg, org.netbeans.modules.xml.wsdl.model.Message.class));
                            
//                        } else if (operationDirection == JavaWsdlMapper.OUTPUT) {
//
//                            org.netbeans.modules.xml.wsdl.model.Message outMsg = wcf.createMessage();
//                            outMsg.setName(name + "Response");                  //NOI18N
//                            d.addMessage(outMsg);
//
//                            org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation oper = wcf.createSolicitResponseOperation();
//                            oper.setName(name);
//                            portType.addOperation(oper);
//                            
//                            org.netbeans.modules.xml.wsdl.model.Output out = wcf.createOutput();
//                            oper.setOutput(out);
//                            out.setMessage(out.createReferenceTo(outMsg, org.netbeans.modules.xml.wsdl.model.Message.class));
//                            
//                        } else if (operationDirection == JavaWsdlMapper.INPUT) {
//                            org.netbeans.modules.xml.wsdl.model.Message inputMsg = wcf.createMessage();
//                            inputMsg.setName(name);
//                            d.addMessage(inputMsg);
//
//                            org.netbeans.modules.xml.wsdl.model.OneWayOperation oper = wcf.createOneWayOperation();
//                            oper.setName(name);
//                            portType.addOperation(oper);
//                            
//                            org.netbeans.modules.xml.wsdl.model.Input input = wcf.createInput();
//                            oper.setInput(input);
//                            input.setMessage(input.createReferenceTo(inputMsg, org.netbeans.modules.xml.wsdl.model.Message.class));
//                        }

//                        if ((operationDirection == JavaWsdlMapper.OUTPUT) || (operationDirection == JavaWsdlMapper.OUTPUTINPUT)) {
                            org.netbeans.modules.xml.wsdl.model.BindingOutput bindingOutput = wcf.createBindingOutput();
                            bindingOperation.setBindingOutput(bindingOutput);
//                        }
//                        if ((operationDirection == JavaWsdlMapper.INPUT) || (operationDirection == JavaWsdlMapper.OUTPUTINPUT)) {
                            org.netbeans.modules.xml.wsdl.model.BindingInput bindingInput = wcf.createBindingInput();
                            bindingOperation.setBindingInput(bindingInput);
//                        }
                        //add faults
                        List<String> operationFaults = JavaWsdlMapper.getOperationFaults(jc, name);
                        for (String fault : operationFaults) {
                            org.netbeans.modules.xml.wsdl.model.BindingFault bindingFault = wcf.createBindingFault();
                            bindingOperation.addBindingFault(bindingFault);
                        }
                    }

                    // attach portType to the binding
                    binding.setType(binding.createReferenceTo(portType, org.netbeans.modules.xml.wsdl.model.PortType.class));
                    
                    model.endTransaction();

                    DataObject dO = DataObject.find(wsdlFO);
                    SaveCookie sc = (SaveCookie) dO.getCookie(SaveCookie.class);
                    sc.save();
                    dO.setModified(false);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return model;
    }

    private WSDLModel getModelFromFO(FileObject wsdlFO) {
        WSDLModel model = null;
        ModelSource ms = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(wsdlFO, true);
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

    static protected boolean isWsitSupported(Project p) {

        // check if the wsimport class is already present - this means we don't need to add the library
        SourceGroup[] sgs = ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
        FileObject wsimportFO = classPath.findResource("com/sun/xml/ws/policy/Policy.class"); // NOI18N
        
        if (wsimportFO != null) {
            return true;
        }
        
        J2eeModuleProvider mp = (J2eeModuleProvider)p.getLookup().lookup(J2eeModuleProvider.class);
        if (mp != null) {
            String sID = mp.getServerInstanceID();
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(sID);
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIT); //NOI18N
        }
        return false;
    }
}
