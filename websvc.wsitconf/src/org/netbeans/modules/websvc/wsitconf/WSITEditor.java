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
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsitconf.ui.ErrorTopComponent;
import org.netbeans.modules.websvc.wsitconf.ui.service.ServiceTopComponent;
import org.netbeans.modules.websvc.wsitconf.ui.client.ClientTopComponent;
import org.netbeans.modules.websvc.wsitconf.util.UndoManagerHolder;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditor;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileLock;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Martin Grebac
 */
public class WSITEditor implements WSEditor, UndoManagerHolder {

    private static final Logger logger = Logger.getLogger(WSITEditor.class.getName());
    
    private UndoManager undoManager;
    private Collection<FileObject> createdFiles = new LinkedList<FileObject>();
            
    /**
     * Creates a new instance of WSITEditor
     */
    public WSITEditor() { }

    public String getTitle() {
        return NbBundle.getMessage(WSITEditor.class, "EDITOR_TITLE"); //NOI18N
    }

    public JComponent createWSEditorComponent(Node node, JaxWsModel jaxWsModel) {

        WSDLModel clientWsdlModel;
        WSDLModel wsdlModel;

        //is it a client node?
        Client client = node.getLookup().lookup(Client.class);
        //is it a service node?
        Service service = node.getLookup().lookup(Service.class);
        
        final Project p;
        if (jaxWsModel != null) {
            p = FileOwnerQuery.getOwner(jaxWsModel.getJaxWsFile());
        } else {
            p = null;
        }
                
        boolean wsitSupported = false;
        if (client != null){ //its a client
            if (p != null) {
                final JAXWSClientSupport wscs = JAXWSClientSupport.getJaxWsClientSupport(p.getProjectDirectory());
                if (wscs != null) {
//                    PropertyChangeListener jaxWsClientListener = new PropertyChangeListener() {
//                        public void propertyChange(PropertyChangeEvent arg0) {
//                            if (arg0 != null) {
//                                Object newV = arg0.getNewValue();
//                                Object oldV = arg0.getOldValue();
//                                if ((oldV != null) && (newV == null)) {  //being removed
//                                    if (oldV instanceof Client) {
//                                        Client c = (Client)oldV;
//                                        
//                                    }
//                                }
//                            }
//                        }
//                    };
//                    jaxWsModel.addPropertyChangeListener(jaxWsClientListener);

                    wsitSupported = Util.isWsitSupported(p);
                    if (wsitSupported) {
                        try {
                            clientWsdlModel = WSITModelSupport.getModel(node, jaxWsModel, this, true, createdFiles);
                            wsdlModel = WSITModelSupport.getServiceModelForClient(wscs, client);
                            return new ClientTopComponent(client, jaxWsModel, clientWsdlModel, wsdlModel, node);
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, null, e);
                        }
                    } else {
                        return new ErrorTopComponent(NbBundle.getMessage(WSITEditor.class, "TXT_WSIT_NotDetected", Util.getServerName(p)));
                    }
                }
            }
        } else {
            if (p != null) {
                final JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(p.getProjectDirectory());
                if (wss != null) {
                    JaxWsModel.ServiceListener jaxWsServiceListener = new JaxWsModel.ServiceListener() {
                        public void serviceAdded(String name, String implementationClass) {}
                        public void serviceRemoved(String name) {
                            if (!wss.isFromWSDL(name)) {
                                JaxWsModel jaxWsModel = p.getLookup().lookup(JaxWsModel.class);
                                Service s = jaxWsModel.findServiceByName(name);
                                String implClass = s.getImplementationClass();
                                String configWsdlName = WSITModelSupport.CONFIG_WSDL_SERVICE_PREFIX + implClass;
                                if ((implClass != null) && (implClass.length() > 0)) {
                                    try {
                                        if (wss.getWsdlFolder(false) != null) {
                                            FileObject wsdlFO = wss.getWsdlFolder(
                                                    false).getParent().getFileObject(configWsdlName, WSITModelSupport.CONFIG_WSDL_EXTENSION);
                                            if ((wsdlFO != null) && (wsdlFO.isValid())) {   //NOI18N
                                                FileLock lock = null;
                                                try {
                                                    lock = wsdlFO.lock();
                                                    wsdlFO.delete(lock);
                                                } finally {
                                                    if (lock != null) {
                                                        lock.releaseLock();
                                                    }
                                                }
                                            }
                                        }
                                    } catch (IOException e) {
                                        // burn
                                    }
                                }
                            }
                        }
                    };
                    jaxWsModel.addServiceListener(jaxWsServiceListener);
                    
                    wsitSupported = Util.isWsitSupported(p);
                    if (wsitSupported) {
                        try {
                            wsdlModel = WSITModelSupport.getModel(node, jaxWsModel, this, true, createdFiles);
                            return new ServiceTopComponent(service, jaxWsModel, wsdlModel, node, getUndoManager());
                        } catch(Exception e){
                            logger.log(Level.SEVERE, null, e);
                        }
                    } else {
                        return new ErrorTopComponent(NbBundle.getMessage(WSITEditor.class, "TXT_WSIT_NotDetected", Util.getServerName(p)));
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
                WSITModelSupport.save(model);
            }
        } catch (Exception e){
            logger.log(Level.SEVERE, null, e);
        }
    }

    public void cancel(Node node, JaxWsModel jaxWsModel) {
        if (node == null) return;
        WSDLModel model = null;
        
        // remove imports from main config file if a new config file was imported
        FileObject srcRoot = node.getLookup().lookup(FileObject.class);
        if (srcRoot != null) {
            Project p = FileOwnerQuery.getOwner(srcRoot);
            if (p != null) {
                FileObject clientConfigFolder = getClientConfigFolder(p);
                WSDLModel mainModel = WSITModelSupport.getMainClientModel(clientConfigFolder);
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
                                logger.log(Level.INFO, "Cannot find fileobject in lookup for: " + mainModel.getModelSource());
                            }
                            try {
                                DataObject mainDO = DataObject.find(mainFO);
                                if ((mainDO != null) && (mainDO.isModified())) {
                                    SaveCookie wsdlSaveCookie = mainDO.getCookie(SaveCookie.class);
                                    if(wsdlSaveCookie != null){
                                        wsdlSaveCookie.save();
                                    }
                                    mainDO.setModified(false);
                                }
                            } catch (IOException ioe) {
                                // ignore - just don't do anything
                            }
                        } catch (CatalogModelException ex) {
                            logger.log(Level.SEVERE, null, ex);
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
                logger.log(Level.INFO, null, e);
            }
            FileObject fo = org.netbeans.modules.xml.retriever.catalog.Utilities.getFileObject(model.getModelSource());
            DataObject dO = null;
            try {
                dO = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            if (dO != null) {
                try {
                    model.sync();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
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
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    public static FileObject getClientConfigFolder(Project p) {
        FileObject folder = null;
        Sources sources = ProjectUtils.getSources(p);
        if (sources == null) return null;
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        Util.ProjectType pt = Util.getProjectType(p);
        switch(pt) {             
            case UNKNOWN: 
            case CLIENT:
            case WEB: {
                    if ((sourceGroups != null) && (sourceGroups.length > 0)) {
                        folder = sourceGroups[0].getRootFolder();
                        if (folder != null) {
                            folder = folder.getFileObject("META-INF");
                        }
                        if ((folder == null) || (!folder.isValid())) {
                            try {
                                folder = sourceGroups[0].getRootFolder().createFolder("META-INF");
                            } catch (IOException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                } break;            
            case EJB: {
                    if ((sourceGroups != null) && (sourceGroups.length > 0)) {
                        folder = sourceGroups[0].getRootFolder().getParent();
                        if (folder != null) {
                            folder = folder.getFileObject("conf");
                        }
                        if ((folder == null) || (!folder.isValid())) {
                            try {
                                folder = sourceGroups[0].getRootFolder().createFolder("conf");
                            } catch (IOException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                } break;
        }
        return folder;
    }
    
    public UndoManager getUndoManager() {
        return undoManager;
    }

    public void setUndoManager(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    public String getDescription() {
        return NbBundle.getMessage(WSITEditor.class, "WSIT_CONFIG_DESC");
    }
}
