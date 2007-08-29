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
/*
 * CustomizationWSEditor.java
 *
 * Created on March 9, 2006, 4:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.core.ui;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.undo.UndoManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditor;
import org.netbeans.modules.websvc.customization.multiview.WSCustomizationTopComponent;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.customization.multiview.SaveableSectionInnerPanel;
import org.netbeans.modules.websvc.jaxws.api.JaxWsRefreshCookie;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Roderico Cruz
 */
public class CustomizationWSEditor implements WSEditor{
    private WSCustomizationTopComponent wsTopComponent;
    private boolean wsdlIsDirty;
    private boolean jaxwsDirty;
    private Definitions primaryDefinitions;
    private UndoManager undoManager;
    
    /**
     * Creates a new instance of CustomizationWSEditor
     */
    public CustomizationWSEditor() {
    }
    
    private void saveAndRefresh(Node node, JaxWsModel jaxWsModel){
        Collection<SaveableSectionInnerPanel> panels = wsTopComponent.getPanels();
        for(SaveableSectionInnerPanel panel : panels){
            panel.save();
            if(!wsdlIsDirty){
                wsdlIsDirty = panel.wsdlIsDirty();
            }
            if(!jaxwsDirty){
                jaxwsDirty = panel.jaxwsIsDirty();
            }
        }
        
        try{
            if(wsdlIsDirty){
                Set<WSDLModel> modelSet = wsdlModels.keySet();
                for(WSDLModel wsdlModel : modelSet){
                    ModelSource ms = wsdlModel.getModelSource();
                    FileObject fo = (FileObject)ms.getLookup().lookup(FileObject.class);
                    DataObject wsdlDO = DataObject.find(fo);
                    SaveCookie wsdlSaveCookie = (SaveCookie)wsdlDO.getCookie(SaveCookie.class);
                    if(wsdlSaveCookie != null){
                        wsdlSaveCookie.save();
                    }
                }
            }
            if(jaxwsDirty){
                jaxWsModel.write();
            }
            if(wsdlIsDirty  || jaxwsDirty){
                JaxWsRefreshCookie refreshCookie =
                        (JaxWsRefreshCookie)node.getCookie(JaxWsRefreshCookie.class);
                refreshCookie.refreshService(false);
            }
        }catch(Exception e){
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public void save( final Node node, final JaxWsModel jaxWsModel) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle
                ( NbBundle.getMessage(CustomizationWSEditor.class, "TXT_Refreshing")); //NOI18N
        handle.start(100);
        handle.switchToIndeterminate();
        Runnable r = new Runnable(){
            public void run(){
                try{
                    saveAndRefresh(node, jaxWsModel);
                    removeListeners();
                }finally{
                    handle.finish();
                }
            }
        };
        RequestProcessor.getDefault().post(r);
    }
    
    
    public JComponent createWSEditorComponent(Node node, JaxWsModel jaxWsModel) {
        try{
            initializeModels(node);
        }catch(Exception e){
            ErrorManager.getDefault().notify(e);
            return null;
        }
        
        wsTopComponent =  new WSCustomizationTopComponent(node, getWSDLModels(), primaryDefinitions,
                jaxWsModel);
        return wsTopComponent;
    }
    
    public String getTitle() {
        return NbBundle.getMessage(CustomizationWSEditor.class, "TITLE_WSDL_CUSTOMIZATION");
    }
    
    public Set<WSDLModel> getWSDLModels(){
        return wsdlModels.keySet();
    }
    
    private Map<WSDLModel, Boolean> wsdlModels = new HashMap<WSDLModel, Boolean>();
    
    private void initializeModels(Node node) throws Exception {
        if(wsdlModels.isEmpty()){
            undoManager = new UndoManager();
            WSDLModel primaryModel = getPrimaryModel(node);
            populateAllModels(primaryModel);
            Set<WSDLModel> modelSet = wsdlModels.keySet();
            for(WSDLModel wsdlModel: modelSet){
                wsdlModel.addUndoableEditListener(undoManager);
            }
        }
    }
    
    private void removeListeners(){
        Set<WSDLModel> modelSet = wsdlModels.keySet();
        for(WSDLModel wsdlModel: modelSet){
            wsdlModel.removeUndoableEditListener(undoManager);
        }
    }
    
    private DataObject getDataObjectOfModel(WSDLModel wsdlModel){
        ModelSource ms = wsdlModel.getModelSource();
        return (DataObject)ms.getLookup().lookup(DataObject.class);
    }
    
    private boolean modelExists(final WSDLModel wsdlModel){
        if(wsdlModels.size() == 0) return false;
        DataObject modelDobj = getDataObjectOfModel(wsdlModel);
        if(!modelDobj.isValid()) return true;
        Set<WSDLModel> wsdls = wsdlModels.keySet();
        for(WSDLModel wsdl : wsdls){
            DataObject dobj = getDataObjectOfModel(wsdl);
            if(!dobj.isValid()) continue;
            if(modelDobj.equals(dobj)){
                return true;
            }
        }
        return false;
    }
    
    private void populateAllModels(WSDLModel wsdlModel)throws Exception{
        if(modelExists(wsdlModel)) return;
        DataObject dobj = getDataObjectOfModel(wsdlModel);
        if(!dobj.isValid()) return;
        Definitions definitions = wsdlModel.getDefinitions();
        if(definitions.getImports().size() == 0){
            wsdlModels.put(wsdlModel, Boolean.valueOf(dobj.isModified()));
            return;
        }else{
            wsdlModels.put(wsdlModel, Boolean.valueOf(dobj.isModified()));
            Set<WSDLModel> modelSet = getImportedModels(definitions);
            for(WSDLModel wModel: modelSet){
                populateAllModels(wModel);
            }
        }
    }
    
    private Set<WSDLModel> getImportedModels(Definitions definitions) throws CatalogModelException{
        Set<WSDLModel> importedModels = new HashSet<WSDLModel>();
        Collection<Import> importedWsdls = definitions.getImports();
        for(Import importedWsdl : importedWsdls){
            WSDLModel wsdlModel = importedWsdl.getImportedWSDLModel();
            importedModels.add(wsdlModel);
        }
        return importedModels;
    }
    
    private WSDLModel getPrimaryModel(Node node)
            throws MalformedURLException, Exception{
        WSDLModel model = null;
        //is it a client node?
        Client client = (Client)node.getLookup().lookup(Client.class);
        //is it a service node?
        Service service = (Service)node.getLookup().lookup(Service.class);
        FileObject srcRoot = (FileObject)node.getLookup().lookup(FileObject.class);
        assert srcRoot != null;
        FileObject wsdlFO = null;
        if(client != null){ //its a client
            JAXWSClientSupport support = JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
            wsdlFO =
                    support.getLocalWsdlFolderForClient(client.getName(),false).getFileObject(client.getLocalWsdlFile());
        } else if (service != null && service.getWsdlUrl()!=null){  //its a service from wsdl
            JAXWSSupport support = JAXWSSupport.getJAXWSSupport(srcRoot);
            wsdlFO =
                    support.getLocalWsdlFolderForService(service.getName(),false).getFileObject(service.getLocalWsdlFile());
        } else{ //neither a client nor a service, get out of here
            throw new Exception("Unable to identify node type");
        }
        
        if(wsdlFO!=null){ //found the wsdl
            ModelSource ms = Utilities.getModelSource(wsdlFO, true);
            model = WSDLModelFactory.getDefault().getModel(ms);
        } else{ //wsdl not found, throw an exception
            throw new Exception("WSDL file not found");
        }
        primaryDefinitions = model.getDefinitions();
        return model;
    }
    
    
    public void cancel(Node node, JaxWsModel jaxWsModel) {
        if(undoManager != null) {
            while(undoManager.canUndo()){
                undoManager.undo();
            }
        }
        
        try{
            Set<WSDLModel>modelSet = wsdlModels.keySet();
            for(WSDLModel wsdlModel : modelSet){
                ModelSource ms = wsdlModel.getModelSource();
                FileObject fo = (FileObject)ms.getLookup().lookup(FileObject.class);
                DataObject wsdlDobj = DataObject.find(fo);
                wsdlDobj.setModified(wsdlModels.get(wsdlModel));
            }
        }catch(DataObjectNotFoundException e){
            ErrorManager.getDefault().notify(e);
        }
        removeListeners();
    }
    
    public String getDescription() {
        return NbBundle.getMessage(CustomizationWSEditor.class, "WSDL_CUSTOMIZE_DESC");
    }
}
