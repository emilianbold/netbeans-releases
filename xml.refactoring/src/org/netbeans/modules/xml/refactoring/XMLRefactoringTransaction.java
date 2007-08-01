/*
 * XMLRefactoringTransaction.java
 *
 * Created on February 1, 2007, 2:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.refactoring;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.xml.refactoring.impl.UndoRedoProgress;
import org.netbeans.modules.xml.refactoring.spi.RefactoringUtil;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.retriever.catalog.ProjectCatalogSupport;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.EmbeddableRoot;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * Single transaction object, ensuring the order of refactoring change across
 * affected models.  Responsible for undo/redo of refactoring
 *
 * @author Sonali
 */
public class XMLRefactoringTransaction implements Transaction {
    
    AbstractRefactoring request;
    List<RefactoringElementImplementation> elements;
    List<XMLRefactoringPlugin> plugins;
    Referenceable target;
    private Map<Model,UndoManager> undoManagers;
     //For delete refactoring we need to save the model since it wont be available after delete for undo/redo
    Model targetModel;
    boolean isLocal = false;
    ModelSource movedTargetModelSource;
    Map<Model, Set<RefactoringElementImplementation>> modelsInRefactoring;
    private boolean commited = false;
    private UndoManager genericChangeUndoManager;
        
    
    
    /**
     * Creates a new instance of XMLRefactoringTransaction
     */
   
    public XMLRefactoringTransaction(Referenceable target, AbstractRefactoring req){
        this.target = target;
        this.elements = new ArrayList();
        this.plugins = new ArrayList();
        this.request = req;
        this.targetModel = SharedUtils.getModel(target);
              
    }
        
    /**
     * Commits the refactoring changes for all the models
     */   
    public void commit() {
        //when commit is called, there is no way of knowing if its for refactoring or a redo
        //since for rename redo, we use undoManagers, keep a flag and call redo when appropriate
        //System.out.println("COMMIT called");
        try {
            if(commited){
               redo();
            }  else{
               commited=true;
               process();
             } 
        }catch (IOException ioe) {
            String msg = ioe.getMessage();
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
       }
        
    }

    /**
     * Rollbacks the refactoring changes for all the models
     */
    public void rollback() {
       // System.out.println("ROLLBACK called");
        UndoRedoProgress progress = new UndoRedoProgress();
	progress.start();
	try {
	  if(modelsInRefactoring == null)
              modelsInRefactoring = getModels();
            Set<Model> models = modelsInRefactoring.keySet();
                   
            Set<Model> excludedFromSave = RefactoringUtil.getDirtyModels(models, targetModel);
            if (genericChangeUndoManager != null && genericChangeUndoManager.canUndo()) {
                genericChangeUndoManager.undo();
            }
            
            if(undoManagers != null ) {
                for (UndoManager um : undoManagers.values()) {
                    while (um.canUndo()) {
                        um.undo();
                    }
               }
            }
                                
            //fix for issue 108512
           if(undoManagers != null ){
               Set<Model> mods = undoManagers.keySet();
               for(Model m:mods){
                   if(m instanceof AbstractDocumentModel)
                       ((AbstractDocumentModel)m).getAccess().flush();
               }
           }
            
            if(! isLocal)
                RefactoringUtil.save(models, targetModel, excludedFromSave);
         
      } finally {
	   progress.stop();
	}
      
    }
    
    /** Registers the RefactoringElements for each plugin
     * @param plugin that found the usages
     * @param list List of refactoring usages 
     * 
     */
    public void register(XMLRefactoringPlugin plugin, List<? extends RefactoringElementImplementation> list){
        elements.addAll(list);
        plugins.add(plugin);
    }
    
    
    private synchronized void process() throws IOException {
        
       if(modelsInRefactoring == null)
           modelsInRefactoring = getModels();
        Set<Model> models = modelsInRefactoring.keySet();
        //put this code in shared utils
        Set<Model> excludedFromSave = RefactoringUtil.getDirtyModels(models, targetModel);
        GeneralChangeExecutor ce = new GeneralChangeExecutor();
        
        try {            
            if (! isLocal) {
                if(ce.canChange(request.getClass(), target)) {
                //if (target instanceof Model &&  ( (request instanceof RenameRefactoring) || (request instanceof MoveRefactoring)) ) {
                    //file refactoring
                    addUndoableListener(ce);
                    ce.doChange(request);
                }else {
                    addUndoableRefactorListener(targetModel);
                }
              doRefactorTarget();
               
               for (Model model:models) {
                   addUndoableRefactorListener(model);
                }
                
                for (XMLRefactoringPlugin plugin : plugins) {
                    plugin.doRefactoring(elements);
                }
            
                RefactoringUtil.save(models, targetModel, excludedFromSave);
             
            } else { // isLocal
                //Model model = request.getTargetModel();
                if (targetModel != null) {
                    try {
                        addUndoableRefactorListener(targetModel);
                        targetModel.startTransaction();
                        doRefactorTarget();
                        //if the scope is local, then there should be only one plugin with only
                        //the usages in the target model
                         for (XMLRefactoringPlugin plugin : plugins) {
                             plugin.doRefactoring(elements);
                         }
                        
                    } finally {
                        if (targetModel.isIntransaction()) {
                            targetModel.endTransaction();
                            
                        }
                    }
                   
                }
            }
            
                       
        } catch (RuntimeException t) {
            //setStatus(Status.CANCELLING);
            throw t;
        } finally {
            if (! isLocal) {
                removeRefactorUndoEventListeners();
                removeUndoableListener(ce);
                
            }
        }
    }
    
       
    private synchronized void addUndoableRefactorListener(Model model) {
        if (undoManagers == null) {
            undoManagers = new HashMap<Model,UndoManager>();
        }
        // checking against source to eliminate embedded model case
        FileObject source = (FileObject) model.getModelSource().getLookup().lookup(FileObject.class);
        if (source == null) {
            throw new IllegalArgumentException("Could not get source file from provided model"); //NOI18N
        }
        for (Model m : undoManagers.keySet()) {
            FileObject s = (FileObject) m.getModelSource().getLookup().lookup(FileObject.class);
            if (source.equals(s)) {
                return;
            }
        }
        
        if (undoManagers.get(model) == null) {
            UndoManager um = new UndoManager();
            model.addUndoableRefactorListener(um);
            undoManagers.put(model, um);
        }
    }
    
     private synchronized void removeRefactorUndoEventListeners() {
        if (undoManagers == null) return;
        for(Model model : undoManagers.keySet()) {            
            model.removeUndoableRefactorListener(undoManagers.get(model));
        }
        
                  
    }
     
      public void doRefactorTarget() throws IOException {
          if (target instanceof Nameable  && request instanceof RenameRefactoring) {
            SharedUtils.renameTarget((Nameable) target, ((RenameRefactoring)request).getNewName());
        } else if (target instanceof NamedReferenceable && request instanceof SafeDeleteRefactoring) {
            SharedUtils.deleteTarget((NamedReferenceable) target);
        } else if(target instanceof Model) {
            //just do nothing
        }
      }
      
      
   /** Registers the embedded usages
     * @param list List of usages 
     * 
     */
      public void register(List<? extends RefactoringElementImplementation> list) {
          if(list != null && list.size()> 0 ){
              elements.addAll(list);
          }
      }
      
      public void setLocalScope() {
          isLocal = true;
      }   
      
      public boolean isLocal(){
          return isLocal;
      }
      
        
       
       public synchronized boolean canUndo() {
        if (undoManagers == null || undoManagers.isEmpty()) {
            return false;
        }
        for (UndoManager um : undoManagers.values()) {
            if (! um.canUndo()) {
                return false; 
            }
        }
        return true;
    }
       
       private void refactorFile() throws IOException {
           if(modelsInRefactoring == null)
               modelsInRefactoring = getModels();
           Set<Model> all = modelsInRefactoring.keySet();
           FileObject referencedFO = (FileObject) ((Model)target).getModelSource().getLookup().lookup(FileObject.class);
           assert referencedFO != null : "Failed to lookup for file object in model source"; //NOI18N
           RefactoringUtil.saveTargetFile((Model)target, all);
           if(request instanceof RenameRefactoring ) {
               referencedFO = SharedUtils.renameFile(referencedFO,((RenameRefactoring)request).getNewName());
               refreshCatalogModel(referencedFO);
           } else if (request instanceof MoveRefactoring ) {
               FileObject targetFolder = SharedUtils.getOrCreateFolder(((MoveRefactoring)request).getTarget().lookup(URL.class));
               //for move, we do the following
               //first in the target model, update any references to external models
               //then move the target model
               //please note -- we update and then move
               updateTargetModelReferences(targetModel, targetFolder);
               RefactoringUtil.saveTargetFile(targetModel, all);
               referencedFO = SharedUtils.moveFile(referencedFO, targetFolder);
               if(referencedFO != null) {
                   //we have a new file and new model source
                   //keep a reference since we need this do undo
                 this.movedTargetModelSource = Utilities.getModelSource(referencedFO, true); 
                 refreshCatalogModel(referencedFO);
               }
           } else if (request instanceof SingleCopyRefactoring ){
               FileObject targetFolder = SharedUtils.getOrCreateFolder(((SingleCopyRefactoring)request).getTarget().lookup(URL.class));
               
               String newName = ((SingleCopyRefactoring)request).getNewName();
               FileObject newFobj = SharedUtils.copyFile(referencedFO, targetFolder, newName);
               if(newFobj != null){
                   this.movedTargetModelSource = Utilities.getModelSource(newFobj, true);
                   //get the model from the plugin since the domain specific factory will create the model
                   Model newModel =null;
                   for(XMLRefactoringPlugin plugin:plugins){
                        newModel = plugin.getModel(movedTargetModelSource);
                        if(newModel != null){
                            break;
                        }
                   }  
                   if(newModel == null)
                       return;
                   
                   //get the file objects from old model
                   Collection<Component> refs = new ArrayList<Component>();
                   for(XMLRefactoringPlugin plugin:plugins){
                       refs.addAll(plugin.getExternalReferences(targetModel));
                   }
               
                   if(refs.size() > 0 ){
                       boolean startTransaction = ! newModel.isIntransaction();
                       
                       //map schemaLocation to components in the new copied model
                       Collection<Component> newRefs = new ArrayList<Component>();
                       for(XMLRefactoringPlugin plugin:plugins){
                               newRefs.addAll(plugin.getExternalReferences(newModel));
                       }
                       
                       Map<String, Component> map = new HashMap<String, Component>();
                       for(Component r: newRefs){
                            for(XMLRefactoringPlugin plugin:plugins){
                                  String location = plugin.getModelReference(r);
                                  if(location != null){
                                      map.put(location, r);
                                      break;
                                  }
                           }
                       }
                  
                       CatalogModel cat = (CatalogModel) (targetModel).getModelSource().getLookup().lookup(CatalogModel.class);
                       if (startTransaction) {
                           // ((Model)target).startTransaction();
                            newModel.startTransaction();
                       }
                       for(Component ref: refs){
                          try {
                              boolean flag = true;
                                  for(XMLRefactoringPlugin plugin:plugins){
                                  String location = plugin.getModelReference(ref);
                                  if(location != null){
                                     URI uri = new URI(location);
                                     ModelSource source = null;
                                     FileObject fobj = null;
                                     try {
                                         source = cat.getModelSource(uri);
                                         fobj = source.getLookup().lookup(FileObject.class);
                                     } catch (CatalogModelException e){
                                         //this means the model source could be in the same project 
                                         fobj = SharedUtils.getFileObject(targetModel, uri);
                                         //if we have a fobj, we can now have two cases
                                         //the refactoring target model is being moved within the same project
                                         //or the target model is being moved to a different project
                                        flag = SharedUtils.inSameProject(targetFolder, fobj);
                                     
                                    }
                                   if(fobj == null)
                                      break;
                                   Component comp = map.get(location);   
                                   if(flag){
                                       String newLocation = SharedUtils.getReferenceURI(targetFolder, fobj).toString();
                                       if(comp != null)
                                           plugin.setModelReference(comp, newLocation);
                                       
                                   }else {
                                       String newLocation = fobj.getURL().toString();
                                       if(comp != null)
                                           plugin.setModelReference(comp, newLocation);
                                   }
                                   break;
                           }
                        }
                      }catch (URISyntaxException e) {
                          //do nothing. dont update this model reference
                      }
                   }
                                
                  if (startTransaction && (newModel).isIntransaction()) {
                       newModel.endTransaction();
                 }
                 
               }
               RefactoringUtil.saveTargetFile(newModel, all);
              }
           }
   
       }
       
       private void refreshCatalogModel( FileObject referencedFO) {
        //   Map<Model, Set<RefactoringElementImplementation>> modelsInRefactoring = SharedUtils.getModelMap(elements);
           if(modelsInRefactoring == null) 
                modelsInRefactoring = getModels();
           boolean addedEntry = false;
           Set<Model> models = modelsInRefactoring.keySet();
           for (Model ug : models) {
            FileObject referencingFO = ug.getModelSource().getLookup().lookup(FileObject.class);
            ProjectCatalogSupport pcs = SharedUtils.getCatalogSupport(referencingFO);
            if (pcs == null) continue;
            Set<RefactoringElementImplementation> elems = modelsInRefactoring.get(ug);
            for (Component uc : getRefactorComponents(elems)) {
                String reference = getModelReference(uc);
                if (reference == null) continue;
                try {
                     if (pcs != null && pcs.removeCatalogEntry(new URI(reference))) {
                            pcs.createCatalogEntry(referencingFO, referencedFO);
                            addedEntry = true;
                        }
                   //special case for move/copy refactoring when a referencedFO is being moved to a subproject
                   //in this case, there is no catalogEntry and a new one needs to be created
                    if(request instanceof MoveRefactoring || request instanceof SingleCopyRefactoring) {
                        if(pcs != null && !addedEntry){
                            Project targetProject = FileOwnerQuery.getOwner(referencedFO);
                            Project project = FileOwnerQuery.getOwner(referencingFO);
                           if( SharedUtils.getProjectReferences(project).contains(targetProject) ){
                               pcs.createCatalogEntry(referencingFO, referencedFO);
                           }
                        }
                    }
                } catch(Exception ex) {
                    Logger.getLogger(SharedUtils.class.getName()).log(Level.FINE, ex.getMessage());
                }
            }
        }
       }
           
       private List<Component> getRefactorComponents(Set<RefactoringElementImplementation> elementImpls) {
            List<Component> ret = new ArrayList<Component>();
            for(RefactoringElementImplementation element:elementImpls){
                if(element.isEnabled())
                    ret.add( (Component)element.getLookup().lookup(Component.class));
            }
            return ret;
        }
        
        private String getModelReference(Component comp){
            String ref = null;
            for(XMLRefactoringPlugin plugin:plugins){
                ref = plugin.getModelReference(comp);
                if(ref != null)
                    return ref;
            }
            return ref;
        }
        
       private void undoRenameFile() throws IOException {
        CatalogModel cat = (CatalogModel) ((Model)target).getModelSource().getLookup().lookup(CatalogModel.class);
        FileObject fo = (FileObject) ((Model)target).getModelSource().getLookup().lookup(FileObject.class);
        if(request instanceof RenameRefactoring ) {
            String oldName = request.getContext().lookup(String.class);
            if(oldName == null || oldName.equals("")) {
                throw new IOException("Unable to undo refactoring. Cannot retrieve old file name"); //not i118N
            }
            fo = SharedUtils.renameFile(fo, oldName);
            refreshCatalogModel(fo);
        } else if(request instanceof MoveRefactoring) {
            URL url = ((MoveRefactoring)request).getContext().lookup(URL.class);
            if(url == null)
                throw new IOException("Unable to undo refactoring. Cannot retrieve original package location");
            FileObject origFolder = SharedUtils.getOrCreateFolder(url);
            //to undo the move, do the following
            // first get the model for the new file
            // update the external references, if any, in the new model
            // move the model back to the original location
            if(this.movedTargetModelSource != null){
                Model model = null;
                
                //get the model from the plugin since the domain specific factory will create the model
                for(XMLRefactoringPlugin plugin:plugins){
                    model = plugin.getModel(movedTargetModelSource);
                    if(model != null){
                        break;
                    }
                 }    
            
                //update embedded references to external models
                if(model!= null) {
                    if(modelsInRefactoring == null)
                        modelsInRefactoring = getModels();
                    Set<Model> all = modelsInRefactoring.keySet();
                    updateTargetModelReferences(model, origFolder);
                    RefactoringUtil.saveTargetFile(model, all);
                }
                
                //finally, move the file
                FileObject movedFile = movedTargetModelSource.getLookup().lookup(FileObject.class);
                fo = SharedUtils.moveFile(movedFile, origFolder);
                if(fo != null) {
                    ModelSource temp = Utilities.getModelSource(fo, true); 
                    model = null;
                    //after the file is moved, we need to do one final thing.
                    //for redo, we need to point to the right target model
                    //which is the newly moved file
                    for(XMLRefactoringPlugin plugin:plugins){
                        model = plugin.getModel(temp);
                        if(model != null){
                            targetModel = model;
                            break;
                        }
                    }
                 refreshCatalogModel(fo);
                   //addUndoableRefactorListener(movedTargetModelSource);
               }
            } else
                throw new IOException("Unable to undo Move Refactoring");
        } else if(request instanceof SingleCopyRefactoring){
            FileObject fobj = this.movedTargetModelSource.getLookup().lookup(FileObject.class);
            fobj.delete();
        }
        
    }
    
       
    public String refactorForPreview(Model model){
        try {
             if(modelsInRefactoring == null )
                 modelsInRefactoring = getModels();
             Model mod = null;
                     
             //This takes care of WSDL model with embedded schema imports
             if(model instanceof DocumentModel) {
                 Component c = ((DocumentModel)model).getRootComponent();
                 if(c instanceof EmbeddableRoot) {
                     if(  ( (EmbeddableRoot)c).getForeignParent() != null )
                        mod = ( (EmbeddableRoot)c).getForeignParent().getModel();
                     
                 }
             } 
             
            if(mod == null)
               mod = model;
             Set<RefactoringElementImplementation> elements = modelsInRefactoring.get(mod);
             ArrayList<RefactoringElementImplementation> elementsForRefactoring = new ArrayList<RefactoringElementImplementation>(elements);
             
             //for file rename, we dont need to refactor the target
             if( !( (target instanceof Model) && ( (request instanceof RenameRefactoring) || (request instanceof MoveRefactoring))) ) {
                 targetModel.startTransaction();
                 doRefactorTarget();
             }
            
             if(targetModel != mod) {
                 mod.startTransaction();
             }
             for (XMLRefactoringPlugin plugin : plugins) {
                 plugin.doRefactoring(elementsForRefactoring);
             }
         
             String refactoredString = ( (AbstractDocumentModel)mod).getAccess().getCurrentDocumentText(); 
             if( !(target instanceof Model && request instanceof RenameRefactoring)) {
                 ( (AbstractModel)targetModel).rollbackTransaction();
              }
            ((AbstractModel)mod).rollbackTransaction();
                       
            return refactoredString;
                  
        }  catch (Exception e){
            String msg = e.getMessage();
            e.printStackTrace();
       }
        return "";
      
    }
    
        
     private Map<Model, Set<RefactoringElementImplementation>> getModels(){
        Map<Model, Set<RefactoringElementImplementation>> results = new HashMap<Model, Set<RefactoringElementImplementation>>();
        for(RefactoringElementImplementation element:elements){
           Component comp = element.getLookup().lookup(Component.class);
           Model model = null;
           //First group the RE by Foreign Model, if no Foreign Model, then group by Model
           //This takes care of WSDL model with embedded schema imports
           if(comp.getModel() instanceof DocumentModel) {
               Component c = ((DocumentModel)comp.getModel()).getRootComponent();
               if(c instanceof EmbeddableRoot) {
                     if(  ( (EmbeddableRoot)c).getForeignParent() != null )
                        model = ( (EmbeddableRoot)c).getForeignParent().getModel();
                     
               }
           } 
           if(model == null)
               model = comp.getModel();
           if(model == null)
               continue;
           Set<RefactoringElementImplementation> elementsInModel = results.get(model);
           if(elementsInModel == null){
               elementsInModel = new HashSet<RefactoringElementImplementation>();
               elementsInModel.add(element);
               results.put(model, elementsInModel);
           } else
               elementsInModel.add(element);
        }
        return results;
    }
     
     
     public synchronized void redo() throws CannotRedoException {
             
            if(modelsInRefactoring == null)
              modelsInRefactoring = getModels();
            Set<Model> models = modelsInRefactoring.keySet();
            
            Set<Model> excludedFromSave = RefactoringUtil.getDirtyModels(models, targetModel);
            
            if (genericChangeUndoManager != null && genericChangeUndoManager.canRedo()) {
                genericChangeUndoManager.redo();
            }
            
            if(undoManagers != null ){
                for (UndoManager um : undoManagers.values()) {
                    while (um.canRedo()) {
                        um.redo();
                    }
                }
            }           
            //fix for issue 108512
            if(undoManagers != null) {
                Set<Model> mods = undoManagers.keySet();
                for(Model m:mods){
                    if(m instanceof AbstractDocumentModel)
                        ((AbstractDocumentModel)m).getAccess().flush();
                }
            }

            if (!isLocal) {
                RefactoringUtil.save(models, targetModel, excludedFromSave);
            }
        
    }
     
     public synchronized boolean canRedo() {
        if (undoManagers == null || undoManagers.isEmpty()) {
            return false;
        }
        for (UndoManager um : undoManagers.values()) {
            if (! um.canRedo()) {
                return false; 
            }
        }
        if (genericChangeUndoManager != null && ! genericChangeUndoManager.canRedo()) {
            return false;
        }
        return true;
    }
     
    private synchronized void addUndoableListener(GeneralChangeExecutor executor) {
        genericChangeUndoManager = new UndoManager();
        executor.addUndoableEditListener(genericChangeUndoManager);
    }
    
    private synchronized void removeUndoableListener(GeneralChangeExecutor exec) {
        if (! (exec instanceof GeneralChangeExecutor) || 
            genericChangeUndoManager == null || exec == null) {
            return;
        }
        
        exec.removeUndoableEditListener(genericChangeUndoManager);
    } 
    
    private void updateTargetModelReferences(Model model, FileObject targetFolder)throws IOException {
        //before moving the targetFile, check if the targetFile has any external model references
               Collection<Component> refs = new ArrayList<Component>();
               for(XMLRefactoringPlugin plugin:plugins){
                   refs.addAll(plugin.getExternalReferences(model));
               }
               
               if(refs.size() > 0 ){
                   boolean startTransaction = ! (model).isIntransaction();
                   CatalogModel cat = (CatalogModel) (model).getModelSource().getLookup().lookup(CatalogModel.class);
                   if (startTransaction) {
                       // ((Model)target).startTransaction();
                       model.startTransaction();
                   }
                   for(Component ref: refs){
                      try {
                          boolean flag = true;
                          for(XMLRefactoringPlugin plugin:plugins){
                             String location = plugin.getModelReference(ref);
                             if(location != null){
                                 URI uri = new URI(location);
                                 ModelSource source = null;
                                 FileObject fobj = null;
                                 try {
                                     source = cat.getModelSource(uri);
                                     fobj = source.getLookup().lookup(FileObject.class);
                                 } catch (CatalogModelException e){
                                     //this means the model source could be in the same project 
                                     fobj = SharedUtils.getFileObject(model, uri);
                                     //if we have a fobj, we can now have two cases
                                     //the refactoring target model is being moved within the same project
                                     //or the target model is being moved to a different project
                                     flag = SharedUtils.inSameProject(targetFolder, fobj);
                                     
                                 }
                               
                               if(fobj == null)
                                  break;
                               if(flag){
                                   String newLocation = SharedUtils.getReferenceURI(targetFolder, fobj).toString();
                                   plugin.setModelReference(ref, newLocation);
                                   
                               }else {
                                   String newLocation = fobj.getURL().toString();
                                   plugin.setModelReference(ref, newLocation);
                               }
                               break;
                           }
                        }
                      }catch (URISyntaxException e) {
                          //do nothing. dont update this model reference
                      }
                   }
                 
                
                     if (startTransaction && (model).isIntransaction()) {
                         model.endTransaction();
                     }
                 
               }
}
    
   class GeneralChangeExecutor  {
    private UndoableEditSupport ues;
    
    /** Creates a new instance of GenericChangeExecutor */
    public GeneralChangeExecutor() {
        ues = new UndoableEditSupport(this);
    }
    
    public <T extends AbstractRefactoring> boolean canChange(Class<T> changeType, Referenceable target) {
        if ( (changeType == RenameRefactoring.class || changeType == MoveRefactoring.class || changeType == SingleCopyRefactoring.class) && target instanceof Model) {
            return true;
        }
        return false;
    }
    
    /**
     * Perform the change specified by the refactor request.  Any errors that would
     * fail the overall refactoring should be reported throught #RefactoringRequest.addError
     * Implementation should quietly ignore unsupported refactoring type.
     */
    public void doChange(AbstractRefactoring request) throws IOException {
        if ((request instanceof RenameRefactoring) || (request instanceof MoveRefactoring) || (request instanceof SingleCopyRefactoring) ) {
            refactorFile();
            FileRenameUndoable ue = new FileRenameUndoable(request);
            fireUndoEvent(ue);
        }
    }
    
    
    
    public synchronized void addUndoableEditListener(UndoableEditListener l) {
        ues.addUndoableEditListener(l);
    }
    
    public synchronized void removeUndoableEditListener(UndoableEditListener l) {
        ues.removeUndoableEditListener(l);
    }
    
    protected void fireUndoEvent(UndoableEdit edit) {
	    UndoableEditEvent ue = new UndoableEditEvent(this, edit);
	    for (UndoableEditListener l:ues.getUndoableEditListeners()) {
            l.undoableEditHappened(ue);
	    }
    }
    
     class FileRenameUndoable extends AbstractUndoableEdit {
        private static final long serialVersionUID = -1L;
        private AbstractRefactoring request;
        
        public FileRenameUndoable(AbstractRefactoring request) {
            this.request = request;
        }
        
        public void undo() throws CannotUndoException {
            try {
                undoRenameFile();
                super.undo();
            } catch(IOException ioe) {
                CannotUndoException cue = new CannotUndoException();
                cue.initCause(ioe);
                throw cue;
            }
        }
        
        public void redo() throws CannotRedoException {
            try {
                refactorFile();
                super.redo();
            } catch(IOException ioe) {
                CannotUndoException cue = new CannotUndoException();
                cue.initCause(ioe);
                throw cue;
            }
        }
    }
}

       
  }
