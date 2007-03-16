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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.xml.refactoring.impl.UndoRedoProgress;
import org.netbeans.modules.xml.refactoring.spi.RefactoringUtil;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.retriever.catalog.ProjectCatalogSupport;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;

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
        //System.out.println("COMMIT called");
        try {
           process();
        } catch (IOException ioe) {
            String msg = ioe.getMessage();
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
       }
        
    }

    /**
     * Rollbacks the refactoring changes for all the models
     */
    public void rollback() {
      //  System.out.println("ROLLBACK called");
        UndoRedoProgress progress = new UndoRedoProgress();
	progress.start();
	try {
	    Map<Model, Set<RefactoringElementImplementation>> modelsInRefactoring = getModels();
            Set<Model> models = modelsInRefactoring.keySet();
        
            Set<Model> excludedFromSave = RefactoringUtil.getDirtyModels(models, targetModel);
            if(undoManagers != null ) {
                for (UndoManager um : undoManagers.values()) {
                    while (um.canUndo()) {
                        um.undo();
                    }
               }
            }
            
            //is it undo of FileRenameRefactoring??
            if (target instanceof Model && request instanceof RenameRefactoring) {
                undoRenameFile();
            }
            
            RefactoringUtil.save(models, targetModel, excludedFromSave);
          
                 
      } catch (IOException ioe) {
            String msg = ioe.getMessage();
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
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
       
        Map<Model, Set<RefactoringElementImplementation>> modelsInRefactoring = getModels();
        Set<Model> models = modelsInRefactoring.keySet();
        //put this code in shared utils
        Set<Model> excludedFromSave = RefactoringUtil.getDirtyModels(models, targetModel);
                    
        try {            
            if (! isLocal) {
                if (target instanceof Model && request instanceof RenameRefactoring) {
                    //file refactoring
                    refactorFile(models);
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
                        targetModel.startTransaction();
                        //if the scope if local, then there should be only one plugin with only
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
                
            }
        }
    }
    
    private Map<Model, Set<RefactoringElementImplementation>> getModels(){
        Map<Model, Set<RefactoringElementImplementation>> results = new HashMap<Model, Set<RefactoringElementImplementation>>();
        for(RefactoringElementImplementation element:elements){
           Model model = ((Component)element.getLookup().lookup(Component.class)).getModel();
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
       
       private void refactorFile(Set<Model> all) throws IOException {
           FileObject referencedFO = (FileObject) ((Model)target).getModelSource().getLookup().lookup(FileObject.class);
           assert referencedFO != null : "Failed to lookup for file object in model source"; //NOI18N
           RefactoringUtil.saveTargetFile((Model)target, all);
           referencedFO = SharedUtils.renameFile(referencedFO,((RenameRefactoring)request).getNewName());
           refreshCatalogModel(referencedFO);
   
       }
       
       private void refreshCatalogModel( FileObject referencedFO) {
           Map<Model, Set<RefactoringElementImplementation>> modelsInRefactoring = getModels();
           Set<Model> models = modelsInRefactoring.keySet();
           for (Model ug : models) {
            FileObject referencingFO = ug.getModelSource().getLookup().lookup(FileObject.class);
            ProjectCatalogSupport pcs = SharedUtils.getCatalogSupport(referencingFO);
            if (pcs == null) continue;
            for (Component uc : getRefactorComponents()) {
                String reference = getModelReference(uc);
                if (reference == null) continue;
                try {
                    if (pcs != null && pcs.removeCatalogEntry(new URI(reference))) {
                        pcs.createCatalogEntry(referencingFO, referencedFO);
                    }
                } catch(Exception ex) {
                    Logger.getLogger(SharedUtils.class.getName()).log(Level.FINE, ex.getMessage());
                }
            }
        }
       }
           
        private List<Component> getRefactorComponents() {
            List<Component> ret = new ArrayList<Component>();
            for(RefactoringElementImplementation element:elements){
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
        fo = SharedUtils.renameFile(fo, ((RenameRefactoring)request).getNewName());
        refreshCatalogModel(fo);
    }
      
       
       
  }
