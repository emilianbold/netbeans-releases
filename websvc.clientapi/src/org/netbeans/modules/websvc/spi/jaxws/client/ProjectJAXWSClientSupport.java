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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.spi.jaxws.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.GeneratedFilesHelper;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.ClientAlreadyExistsExeption;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public abstract class ProjectJAXWSClientSupport implements JAXWSClientSupportImpl {
    Project project;
    private FileObject clientArtifactsFolder;
    
    /** Creates a new instance of WebProjectJAXWSClientSupport */
    public ProjectJAXWSClientSupport(Project project) {
        this.project=project;
    }
    
    public void removeServiceClient(String serviceName) {
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null && jaxWsModel.removeClient(serviceName)) {
            writeJaxWsModel(jaxWsModel);
        }
    }
    
    public String getWsdlUrl(String serviceName) {
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Client client = jaxWsModel.findClientByName(serviceName);
            if (client!=null) return client.getWsdlUrl();
        }
        return null;
    }
    
    public String addServiceClient(String clientName, String wsdlUrl, String packageName, boolean isJsr109) {
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        String finalClientName=clientName;
        boolean clientAdded=false;
        if (jaxWsModel!=null) {
            Client client=null;
            finalClientName = findProperClientName(clientName, jaxWsModel);
            
            // HACK to enable filesystems to fire events when new folder will be created
            // need to ask for children recursively
            //List subfolders = null;
            clientArtifactsFolder = project.getProjectDirectory().getFileObject("build/generated/wsimport/client"); //NOI18N
            if (clientArtifactsFolder!=null) {
                clientArtifactsFolder.getChildren(true);
            }

            FileObject localWsdl=null;
            try {
                localWsdl = WSUtils.retrieveResource(
                        getLocalWsdlFolderForClient(finalClientName,true),
                        new URI(wsdlUrl));
            } catch (URISyntaxException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(ProjectJAXWSClientSupport.class, "ERR_IncorrectURI", wsdlUrl); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            } catch (UnknownHostException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(ProjectJAXWSClientSupport.class, "ERR_UnknownHost", ex.getMessage()); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(ProjectJAXWSClientSupport.class, "ERR_WsdlRetrieverFailure", wsdlUrl); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
            
            if (localWsdl!=null) {
                Boolean value = jaxWsModel.getJsr109();
                if((value == null || Boolean.TRUE.equals(value)) && !isJsr109){
                    jaxWsModel.setJsr109(Boolean.FALSE);
                } else if (Boolean.FALSE.equals(value) && isJsr109) {
                    jaxWsModel.setJsr109(Boolean.TRUE);
                }
                try {
                    client = jaxWsModel.addClient(finalClientName, wsdlUrl, packageName);
                } catch (ClientAlreadyExistsExeption ex) {
                    //this shouldn't happen
                }
                FileObject xmlResorcesFo = getLocalWsdlFolderForClient(finalClientName,false);
                String localWsdlUrl = FileUtil.getRelativePath(xmlResorcesFo, localWsdl);
                client.setLocalWsdlFile(localWsdlUrl);
                FileObject catalog = getCatalogFileObject();
                if (catalog!=null) client.setCatalogFile(CATALOG_FILE);
                writeJaxWsModel(jaxWsModel);
                clientAdded=true;
                // generate wsdl model immediately
                try {
                    final WsdlModeler modeler = WsdlModelerFactory.getDefault().getWsdlModeler(localWsdl.getURL());
                    if (modeler!=null) {
                        modeler.setPackageName(packageName);
                        modeler.generateWsdlModel(new WsdlModelListener() {
                            public void modelCreated(WsdlModel model) {
                                if (model==null) {
                                    DialogDisplayer.getDefault().notify(new WsImportFailedMessage(modeler.getCreationException()));
                                }
                            }
                        });
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().log(ex.getLocalizedMessage());
                }
            }
            if (clientAdded) {
                if(!isJsr109){
                    try{
                        addJaxWs20Library();
                    } catch(Exception e){  //TODO handle this
                        ErrorManager.getDefault().notify(e);
                    }
                }
                FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
                try {
                    ExecutorTask wsimportTask =
                            ActionUtils.runTarget(buildImplFo,new String[]{"wsimport-client-"+finalClientName},null); //NOI18N
                    if (wsimportTask.result()==0) {
                        if (clientArtifactsFolder==null)
                            clientArtifactsFolder = project.getProjectDirectory().getFileObject("build/generated/wsimport/client"); //NOI18N
                        if (clientArtifactsFolder!=null) {
                            FileObject clientArtifactsFolder1 = clientArtifactsFolder.getFileObject(packageName.replace('.','/'));
                            if (clientArtifactsFolder1!=null) {
                                wsimportTask=
                                    ActionUtils.runTarget(buildImplFo,new String[]{"wsimport-client-compile"},null); //NOI18N
                            }
                        }
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().log(ex.getLocalizedMessage());
                } catch (IllegalArgumentException ex) {
                    ErrorManager.getDefault().log(ex.getLocalizedMessage());
                }
                return finalClientName;
            }
        }
        return null;
    }
    
    private String findProperClientName(String name, JaxWsModel jaxWsModel) {
        String firstName=name.length()==0?NbBundle.getMessage(ProjectJAXWSClientSupport.class,"LBL_defaultClientName"):name;
        if (jaxWsModel.findClientByName(firstName)==null) return firstName;
        for (int i = 1;; i++) {
            String finalName = firstName + "_" + i; // NOI18N
            if (jaxWsModel.findClientByName(finalName)==null)
                return finalName;
        }
    }

    private void writeJaxWsModel(final JaxWsModel jaxWsModel) {
        try {
            final FileObject jaxWsFo = project.getProjectDirectory().getFileObject("nbproject/jax-ws.xml"); //NOI18N
            jaxWsFo.getFileSystem().runAtomicAction(new AtomicAction() {
                public void run() {
                    FileLock lock=null;
                    OutputStream os=null;
                    try {
                        lock = jaxWsFo.lock();
                        os = jaxWsFo.getOutputStream(lock);
                        jaxWsModel.write(os);
                        os.close();
                    } catch (java.io.IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    } finally {
                        if (os!=null) {
                            try {
                                os.close();
                            } catch (IOException ex) {}
                        }
                        if (lock!=null) lock.releaseLock();
                    }
                }
            });
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    public List getServiceClients() {
        List jaxWsClients = new ArrayList();
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Client[] clients = jaxWsModel.getClients();
            for (int i=0;i<clients.length;i++) jaxWsClients.add(clients[i]);
        }
        return jaxWsClients;
    }
    
    /**
     *  return root folder for wsdl artifacts
     */
    public FileObject getLocalWsdlFolderForClient(String clientName, boolean createFolder) {
        return getArtifactsFolder(clientName, createFolder, true);
    }
    
    /**
     *  return folder for local wsdl bindings
     */
    public FileObject getBindingsFolderForClient(String clientName, boolean createFolder) {
        return getArtifactsFolder(clientName, createFolder, false);
    }
    
    private FileObject getArtifactsFolder(String clientName, boolean createFolder, boolean forWsdl) {
        String folderName = forWsdl?"wsdl":"bindings"; //NOI18N
        FileObject root = getXmlArtifactsRoot();
        assert root!=null;
        FileObject wsdlLocalFolder = root.getFileObject(XML_RESOURCES_FOLDER+"/"+CLIENTS_LOCAL_FOLDER+"/"+clientName+"/"+folderName); //NOI18N
        if (wsdlLocalFolder==null && createFolder) {
            try {
                FileObject xmlLocalFolder = root.getFileObject(XML_RESOURCES_FOLDER);
                if (xmlLocalFolder==null) xmlLocalFolder = root.createFolder(XML_RESOURCES_FOLDER);
                FileObject servicesLocalFolder = xmlLocalFolder.getFileObject(CLIENTS_LOCAL_FOLDER);
                if (servicesLocalFolder==null) servicesLocalFolder = xmlLocalFolder.createFolder(CLIENTS_LOCAL_FOLDER);
                FileObject serviceLocalFolder = servicesLocalFolder.getFileObject(clientName);
                if (serviceLocalFolder==null) serviceLocalFolder = servicesLocalFolder.createFolder(clientName);
                wsdlLocalFolder=serviceLocalFolder.getFileObject(folderName);
                if (wsdlLocalFolder==null) wsdlLocalFolder = serviceLocalFolder.createFolder(folderName);
            } catch (IOException ex) {
                return null;
            }
        }
        return wsdlLocalFolder;
    }
    
    /** return root folder for xml artifacts
     */
    protected FileObject getXmlArtifactsRoot() {
        return project.getProjectDirectory();
    }
    
    private FileObject getCatalogFileObject() {
        return project.getProjectDirectory().getFileObject(CATALOG_FILE);
    }
    
    public URL getCatalog() {
        try {
            FileObject catalog = getCatalogFileObject();
            return catalog==null?null:catalog.getURL();
        } catch (FileStateInvalidException ex) {
            return null;
        }
        
    }
    
    protected abstract void addJaxWs20Library() throws Exception;
    
    public abstract FileObject getWsdlFolder(boolean create) throws IOException;

    public String getServiceRefName(Node clientNode) {
        WsdlService service = (WsdlService)clientNode.getLookup().lookup(WsdlService.class);
        String serviceName = service.getName();
        return "service/" + serviceName;
    }

    private class WsImportFailedMessage extends NotifyDescriptor.Message {
        public WsImportFailedMessage(Throwable ex) {
            super(NbBundle.getMessage(ProjectJAXWSClientSupport.class,"TXT_CannotGenerateClient",ex.getLocalizedMessage()),
                    NotifyDescriptor.ERROR_MESSAGE);
        }
        
    }
    
    /** folder where xml client artifacts should be saved, e.g. WEB-INF/wsdl/client/SampleClient
     */
    protected FileObject getWsdlFolderForClient(String name) throws IOException {
        FileObject globalWsdlFolder = getWsdlFolder(true);
        FileObject oldWsdlFolder = globalWsdlFolder.getFileObject("client/"+name); //NOI18N
        if (oldWsdlFolder!=null) {
            FileLock lock = oldWsdlFolder.lock();
            try {
                oldWsdlFolder.delete(lock);
            } finally {
                lock.releaseLock();
            }
        }
        FileObject clientWsdlFolder = globalWsdlFolder.getFileObject("client"); //NOI18N
        if (clientWsdlFolder==null) clientWsdlFolder = globalWsdlFolder.createFolder("client"); //NOI18N
        return clientWsdlFolder.createFolder(name);
    }

}
