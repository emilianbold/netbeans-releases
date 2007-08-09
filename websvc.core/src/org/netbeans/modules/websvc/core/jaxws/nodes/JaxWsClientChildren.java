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
package org.netbeans.modules.websvc.core.jaxws.nodes;

/** WSDL children (Service elements)
 *
 * @author mkuchtiak
 */
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.GeneratedFilesHelper;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public class JaxWsClientChildren extends Children.Keys {
    Client client;
    WsdlModel wsdlModel;
    
    public JaxWsClientChildren(Client client) {
        this.client=client;
    }
    
    @Override
    protected void addNotify() {
        final WsdlModeler wsdlModeler = ((JaxWsClientNode)getNode()).getWsdlModeler();
        if (wsdlModeler!=null) {
            wsdlModel = wsdlModeler.getWsdlModel();
            if (wsdlModel==null) {
                wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                    public void modelCreated(WsdlModel model) {
                        wsdlModel=model;
                        ((JaxWsClientNode)getNode()).changeIcon();
                        if (model==null) {
                            DialogDisplayer.getDefault().notify(
                                    new JaxWsUtils.WsImportClientFailedMessage(wsdlModeler.getCreationException()));
                        }
                        updateKeys();
                    }
                });
            } else {
                updateKeys();
            }
        }
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }
       
    private void updateKeys() {
        List keys=null;
        if (wsdlModel!=null) {
            keys=wsdlModel.getServices();
        }
        setKeys(keys==null?new ArrayList():keys);
    }

    protected Node[] createNodes(Object key) {
        if(key instanceof WsdlService) {
            return new Node[] {new ServiceNode((WsdlService)key)};
        }
        return new Node[0];
    }
    
    void refreshKeys(boolean downloadWsdl) {
        this.refreshKeys(downloadWsdl, "");
    }
    
    void refreshKeys(boolean downloadWsdl, String newWsdlUrl) {
        System.out.println("newWsdlUrl = "+newWsdlUrl);
        super.addNotify();
        // copy to local wsdl first
        JAXWSClientSupport support = getJAXWSClientSupport();
        final JaxWsClientNode clientNode = (JaxWsClientNode)getNode();
        if (downloadWsdl) {
            try {
                String clientName = clientNode.getName();
                String oldWsdlUrl = client.getWsdlUrl();
                boolean jaxWsModelChanged=false;
                FileObject localWsdl = null;
                if (newWsdlUrl.length()>0 && !oldWsdlUrl.equals(newWsdlUrl)) {                    
                    localWsdl = WSUtils.retrieveResource(
                        support.getLocalWsdlFolderForClient(clientName,true),
                        new URI(newWsdlUrl));
                    jaxWsModelChanged=true;
                } else {
                    WSUtils.retrieveResource(
                        support.getLocalWsdlFolderForClient(clientName,true),
                        new URI(oldWsdlUrl));                       
                }
                
                if (jaxWsModelChanged) {
                    client.setWsdlUrl(newWsdlUrl);
                    FileObject xmlResourcesFo = support.getLocalWsdlFolderForClient(clientName,false);
                    System.out.println("localWsdl = "+localWsdl);
                    if (xmlResourcesFo!=null) {
                        String localWsdlUrl = FileUtil.getRelativePath(xmlResourcesFo, localWsdl);
                        System.out.println("localWsdlUrl = "+localWsdlUrl);
                        client.setLocalWsdlFile(localWsdlUrl);
                    }
                    
                    clientNode.getJaxWsModel().write();
                }  
                // copy resources to WEB-INF[META-INF]/wsdl/client/${clientName}
                if (client.getWsdlUrl().startsWith("file:")) {
                    FileObject srcRoot = getNode().getLookup().lookup(FileObject.class);
                    Project project = FileOwnerQuery.getOwner(srcRoot);
                    if (project.getLookup().lookup(J2eeModuleProvider.class)!=null) {
                        FileObject xmlResorcesFo = support.getLocalWsdlFolderForClient(clientName,false);
                        if (xmlResorcesFo!=null) {
                            FileObject wsdlFolder = getWsdlFolderForClient(support, clientName);
                            WSUtils.copyFiles(xmlResorcesFo, wsdlFolder);
                        }
                    }
                }              
            } catch (URISyntaxException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (UnknownHostException ex) {
                ErrorManager.getDefault().annotate(ex,
                        NbBundle.getMessage(JaxWsClientChildren.class,"MSG_ConnectionProblem"));
                return;
            } catch (IOException ex) {
                ErrorManager.getDefault().annotate(ex,
                        NbBundle.getMessage(JaxWsClientChildren.class,"MSG_ConnectionProblem"));
                return;
            }
            
        }

        final WsdlModeler wsdlModeler = clientNode.getWsdlModeler();
        clientNode.setModelGenerationFinished(false);
        clientNode.changeIcon();
        if (wsdlModeler!=null) {
            wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                public void modelCreated(WsdlModel model) {
                    wsdlModel=model;
                    clientNode.setModelGenerationFinished(true);
                    clientNode.changeIcon();
                    if (model==null) {
                        DialogDisplayer.getDefault().notify(
                                new JaxWsUtils.WsImportClientFailedMessage(wsdlModeler.getCreationException()));
                    }
                    updateKeys();
                    
                    if (model!=null) {
                        Client client = clientNode.getJaxWsModel().findClientByName(clientNode.getName());
                        if (client!=null) {
                            WsdlService wsdlService = null;
                            boolean jaxWsModelChanged=false;
                            List<WsdlService> wsdlServices = model.getServices();
                            if (wsdlServices!=null && wsdlServices.size()>0) {
                                wsdlService = wsdlServices.get(0);
                            }
                            
                            // test if package name for java artifacts hasn't changed
                            String oldPkgName = client.getPackageName();
                            if (wsdlService!=null && oldPkgName!=null && !client.isPackageNameForceReplace()) {
                                String javaName = wsdlService.getJavaName();
                                int dotPosition = javaName.lastIndexOf(".");
                                if (dotPosition>=0) {
                                    String newPkgName = javaName.substring(0,dotPosition);
                                    if (!oldPkgName.equals(newPkgName)) {
                                        client.setPackageName(newPkgName);
                                        jaxWsModelChanged=true;
                                    }
                                }
                            }

                            // save jax-ws model
                            if (jaxWsModelChanged) {
                                try {
                                    clientNode.getJaxWsModel().write();
                                } catch (IOException ex) {
                                    ErrorManager.getDefault().notify(ErrorManager.ERROR,ex);
                                }
                            }
                        }
                    }
                }
            });
        }
        // re-generate java artifacts
        FileObject srcRoot = getNode().getLookup().lookup(FileObject.class);
        Project project = FileOwnerQuery.getOwner(srcRoot);
        if (project!=null) {
            FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
            try {
                String name = client.getName();
                ExecutorTask wsimportTask =
                    ActionUtils.runTarget(buildImplFo,
                        new String[]{"wsimport-client-clean-"+name,"wsimport-client-"+name},null); //NOI18N
                wsimportTask.waitFinished();
            } catch (IOException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            }
            // refresh client artifacts directory due to code copletion
            String packageName = client.getPackageName().replace(".","/"); //NOI18N
            FileObject clientArtifactsFolder = project.getProjectDirectory().getFileObject("build/generated/wsimport/client/"+packageName); //NOI18N
            if (clientArtifactsFolder!=null) clientArtifactsFolder.refresh();
        }
    }
    
    private JAXWSClientSupport getJAXWSClientSupport() {
        return ((JaxWsClientNode)getNode()).getJAXWSClientSupport();
    }
    
    WsdlModel getWsdlModel() {
        return wsdlModel;
    }
    
    private FileObject getWsdlFolderForClient(JAXWSClientSupport support, String name) throws IOException {
        FileObject globalWsdlFolder = support.getWsdlFolder(true);
        FileObject oldWsdlFolder = globalWsdlFolder.getFileObject("client/"+name);
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
