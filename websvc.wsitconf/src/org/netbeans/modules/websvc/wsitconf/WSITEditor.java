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

import java.util.Collection;
import javax.swing.undo.UndoManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfileRegistry;
import org.netbeans.modules.websvc.wsitconf.ui.ErrorTopComponent;
import org.netbeans.modules.websvc.wsitconf.ui.service.ServiceTopComponent;
import org.netbeans.modules.websvc.wsitconf.ui.client.ClientTopComponent;
import org.netbeans.modules.websvc.wsitconf.util.UndoManagerHolder;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import javax.swing.JComponent;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditor;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Martin Grebac
 */
public class WSITEditor implements WSEditor, UndoManagerHolder {

    private UndoManager undoManager;
    private Collection<FileObject> createdFiles = new LinkedList();
            
    /**
     * Creates a new instance of WSITEditor
     */
    public WSITEditor() { 
        populateProfileRegistry();
    }

    public String getTitle() {
        return NbBundle.getMessage(WSITEditor.class, "EDITOR_TITLE"); //NOI18N
    }

    public JComponent createWSEditorComponent(Node node, JaxWsModel jaxWsModel) {

        WSDLModel clientWsdlModel;
        WSDLModel wsdlModel;

        //is it a client node?
        Client client = (Client)node.getLookup().lookup(Client.class);
        //is it a service node?
        Service service = (Service)node.getLookup().lookup(Service.class);
        
        Project p = null;
        if (jaxWsModel != null) {
            p = FileOwnerQuery.getOwner(jaxWsModel.getJaxWsFile());
        }
        
        boolean wsitSupported = false;
        if (client != null){ //its a client
            if (p != null) {
                JAXWSClientSupport wscs = JAXWSClientSupport.getJaxWsClientSupport(p.getProjectDirectory());
                if (wscs != null) {
                    wsitSupported = Util.isWsitSupported(p);
                    if (wsitSupported) {
                        try {
                            clientWsdlModel = WSITModelSupport.getModel(node, jaxWsModel, this, true, createdFiles);
                            wsdlModel = WSITModelSupport.getServiceModelForClient(wscs, client);
                            return new ClientTopComponent(client, jaxWsModel, clientWsdlModel, wsdlModel, node);
                        } catch(Exception e){
                            ErrorManager.getDefault().notify(e);
                        }
                    } else {
                        return new ErrorTopComponent(NbBundle.getMessage(WSITEditor.class, "TXT_WSIT_NotDetected", getServerName(p)));
                    }
                }
            }
        } else {
            if (p != null) {
                JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(p.getProjectDirectory());
                if (wss != null) {
                    wsitSupported = Util.isWsitSupported(p);
                    if (wsitSupported) {
                        try {
                            wsdlModel = WSITModelSupport.getModel(node, jaxWsModel, this, true, createdFiles);
                            return new ServiceTopComponent(service, jaxWsModel, wsdlModel, node, getUndoManager());
                        } catch(Exception e){
                            ErrorManager.getDefault().notify(e);
                        }
                    } else {
                        return new ErrorTopComponent(NbBundle.getMessage(WSITEditor.class, "TXT_WSIT_NotDetected", getServerName(p)));
                    }
                }
            }
        }
        return new ErrorTopComponent(NbBundle.getMessage(WSITEditor.class, "TXT_WSIT_NotSupported"));
    }

    public void save(Node node, JaxWsModel jaxWsModel) {
        if (node == null) return;
        try {
            WSDLModel model = WSITModelSupport.getModel(node, jaxWsModel, this, false, createdFiles);
            if (model != null) {
                FileObject wsdlFO = Utilities.getFileObject(model.getModelSource());
                if (wsdlFO == null) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot find fileobject in lookup for: " + model.getModelSource()); //NOI18N
                }
                DataObject wsdlDO = DataObject.find(wsdlFO);
                if ((wsdlDO != null) && (wsdlDO.isModified())) {
                    SaveCookie wsdlSaveCookie = (SaveCookie)wsdlDO.getCookie(SaveCookie.class);
                    if(wsdlSaveCookie != null){
                        wsdlSaveCookie.save();
                    }
                    wsdlDO.setModified(false);
                }
            }
        } catch (Exception e){
            ErrorManager.getDefault().notify(e);
        }
    }

    public void cancel(Node node, JaxWsModel jaxWsModel) {
        if (node == null) return;
        WSDLModel model = null;
        
        // remove imports from main config file if a new config file was imported
        
        FileObject srcRoot = (FileObject) node.getLookup().lookup(FileObject.class);
        if (srcRoot != null) {
            Project p = FileOwnerQuery.getOwner(srcRoot);
            if (p != null) {
                Sources sources = ProjectUtils.getSources(p);
                if (sources != null) {
                    SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    if ((sourceGroups != null) || (sourceGroups.length > 0)) {
                        FileObject srcFolder = sourceGroups[0].getRootFolder();
                        WSDLModel mainModel = WSITModelSupport.getMainClientModel(srcFolder);
                        if (mainModel != null) {
                            Collection<Import> imports = mainModel.getDefinitions().getImports();
                            for (Import i : imports) {
                                try {
                                    WSDLModel importedModel = i.getImportedWSDLModel();
                                    ModelSource importedms = importedModel.getModelSource();
                                    FileObject importedfo = org.netbeans.modules.xml.retriever.catalog.Utilities.getFileObject(importedms);
                                    mainModel.startTransaction();
                                    if (createdFiles.contains(importedfo)) {
                                        mainModel.getDefinitions().removeImport(i);
                                    }
                                    mainModel.endTransaction();
                                    FileObject mainFO = Utilities.getFileObject(mainModel.getModelSource());
                                    if (mainFO == null) {
                                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, 
                                                "Cannot find fileobject in lookup for: " + model.getModelSource()); //NOI18N
                                    }
                                    try {
                                        DataObject mainDO = DataObject.find(mainFO);
                                        if ((mainDO != null) && (mainDO.isModified())) {
                                            SaveCookie wsdlSaveCookie = (SaveCookie)mainDO.getCookie(SaveCookie.class);
                                            if(wsdlSaveCookie != null){
                                                wsdlSaveCookie.save();
                                            }
                                            mainDO.setModified(false);
                                        }
                                    } catch (IOException ioe) {
                                        // ignore - just don't do anything
                                    }
                                } catch (CatalogModelException ex) {
                                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, ex.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // now first undo all edits in the files
        try {
            model = WSITModelSupport.getModel(node, jaxWsModel, this, false, createdFiles);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (model != null) {
            try {
                if (getUndoManager() != null) {
                    while (getUndoManager().canUndo()) {
                        getUndoManager().undo();
                    }
                }
            } catch (Exception e){
                ErrorManager.getDefault().notify(e);
            }
            FileObject fo = org.netbeans.modules.xml.retriever.catalog.Utilities.getFileObject(model.getModelSource());
            DataObject dO = null;
            try {
                dO = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            if (dO != null) {
                try {
                    model.sync();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
                dO.setModified(false);
            }
        }
        
        // and remove all created files during this run
        if ((createdFiles != null) && (createdFiles.size() > 0)) {
            for (FileObject fo : createdFiles) {
                if (fo != null) {
                    try {
                        DataObject dO = DataObject.find(fo);
                        if (dO != null) {
                            dO.delete();
                        }
                    } catch (IOException ex) {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, ex.getMessage()); //NOI18N
                    }
                }
            }
        }
    }

    private String getServerName(Project p) {
        J2eeModuleProvider mp = (J2eeModuleProvider)p.getLookup().lookup(J2eeModuleProvider.class);
        if (mp != null) {
            String sID = mp.getServerInstanceID();
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(sID);
            return j2eePlatform.getDisplayName();
        }
        return null;
    }
    
    public UndoManager getUndoManager() {
        return undoManager;
    }

    public void setUndoManager(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    private SecurityProfileRegistry populateProfileRegistry() {
        SecurityProfileRegistry registry = SecurityProfileRegistry.getDefault();
        if (registry.getSecurityProfiles().isEmpty()) {
            Lookup.Result results = Lookup.getDefault().
                    lookup(new Lookup.Template(SecurityProfile.class));
            Collection<SecurityProfile> profiles = results.allInstances();
            for (SecurityProfile p : profiles) {
                registry.register(p); 
            }
        }
        
        return registry;
    }

}
