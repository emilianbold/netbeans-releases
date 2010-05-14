/*
 * WSDLRefactoringPlugin.java
 *
 * Created on February 20, 2007, 2:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.iep.editor.refactoring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPModelFactory;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.XMLRefactoringPlugin;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
//import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
//import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
//import org.netbeans.modules.xml.wsdl.refactoring.xsd.FindSchemaUsageVisitor;
//import org.netbeans.modules.xml.wsdl.refactoring.xsd.SchemaUsageRefactoringEngine;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * 
 */
public abstract class IEPRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin, XMLRefactoringPlugin {
    
    /**
     * Creates a new instance of WSDLRefactoringPlugin
     */
    List<ErrorItem> findErrors;
    RefactoringSession session;
    XMLRefactoringTransaction transaction;
    public static final String WSDL_MIME_TYPE = "text/x-iep+xml";
    
    public IEPRefactoringPlugin() {
    }
    
    public List<RefactoringElementImplementation> find(Referenceable target, Component searchRoot){
        if (target instanceof Model) {
            return findUsages((Model) target, searchRoot);
        } 
        else if (target instanceof Component) {
//            return findUsages((Component) target, searchRoot);
            return null;
        } else {
            return null;
        }
    }
                    
    public List<RefactoringElementImplementation> findUsages(Model target, Component searchRoot) {
        List<RefactoringElementImplementation> elements = new ArrayList<RefactoringElementImplementation>();
        
        if (target instanceof IEPModel && searchRoot instanceof Definitions) {
            Definitions definitions = (Definitions) searchRoot;
            IEPModel iepModel = (IEPModel) target;
            File iepWsdlFile = iepModel.getWsdlFile();
            //find out if wsdl file is in same directory
            //and if wsdl file name is same as iep file name
            //if so then we need to rename wsdl file if iep file
            //is renamed
            WSDLModel wsdlModel = definitions.getModel();
            FileObject wsdlFile = wsdlModel.getModelSource().getLookup().lookup(FileObject.class);
            FileObject iepWsdlFileObject = FileUtil.toFileObject(iepWsdlFile);
            if(wsdlFile.getNameExt().equals(iepWsdlFileObject.getNameExt())) {
                //we have found iep's wsdl file
                //(1)we need to rename it
                //(2) we need to rename the target namespace
                elements.add(new WSDLRefactoringElement(searchRoot.getModel(), target, definitions));
                elements.add(new IEPRefactoringElement(searchRoot.getModel(), target, definitions));
                
                
                
            }
            
//            String namespace = ((WSDLModel)target).getDefinitions().getTargetNamespace();
//           // UsageGroup ug = new UsageGroup(this, searchRoot.getModel(), (WSDLModel) target);
//            for (Import i : definitions.getImports()) {
//                Model imported = null;
//                if (namespace.equals(i.getNamespace())) { 
//                    try {
//                        imported = i.getImportedWSDLModel();
//                    } catch(CatalogModelException ex) {
//                        findErrors.add(new ErrorItem(searchRoot, ex.getMessage()));
//                    }
//                }
//                if ( imported == target) {
//                    elements.add(new IEPRefactoringElement(searchRoot.getModel(), target, i));
//                }
//            }
        }
        
        
        
//        SchemaUsageRefactoringEngine engine = new SchemaUsageRefactoringEngine();
//        List<IEPRefactoringElement> elem = engine.findUsages(target, searchRoot);
//        if(elem != null)
//            elements.addAll(elem);
        
        if(elements.size() > 0 )
            return elements;
        else
            return Collections.emptyList();
    }
  
    
//     public List<IEPRefactoringElement> findUsages(Component target, Component searchRoot) {
//        List<IEPRefactoringElement> elements = new ArrayList<IEPRefactoringElement>();
//        List<IEPRefactoringElement> temp = null;
//              
//        if (target instanceof ReferenceableWSDLComponent && searchRoot instanceof Definitions) {
//            temp = new FindWSDLUsageVisitor().findUsages((ReferenceableWSDLComponent)target, (Definitions)searchRoot) ;
//            if(temp != null && temp.size() > 0 )
//                elements.addAll(temp);
//        }
//       
//        if (target instanceof ReferenceableSchemaComponent && searchRoot instanceof Definitions) {
//            temp = new FindSchemaUsageVisitor().findUsages( (ReferenceableSchemaComponent)target, (Definitions)searchRoot, session, transaction);
//            if(temp != null && temp.size() > 0)
//                elements.addAll(temp);
//        }
//        
//        if (elements.size() == 0) {
//            return Collections.emptyList();
//        } else {
//            return elements;
//        }
//    }
     
     public List<Model> getModels(List<RefactoringElementImplementation> elements){
         List<Model> models = new ArrayList<Model>();
         for(RefactoringElementImplementation element:elements){
             if(element instanceof IEPRefactoringElement) {
                 models.add( ((Component)element.getLookup().lookup(Component.class)).getModel());
             }
         }
         return models;
     }
     
     public  Set<Component> getSearchRoots(Referenceable target) {
       Set<Component>  searchRoots = new HashSet<Component>();
       Set<FileObject> files = SharedUtils.getSearchFiles(target);
        for (FileObject file : files) {
            IEPRefactoringEngine engine = new IEPRefactoringEngine();           
            try {
                Component root = engine.getSearchRoot(file);
                searchRoots.add(root);
            } catch (IOException ex) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, ex.getMessage());
            }  
       }
        
        return searchRoots;
     }
    
     public Map<Model, Set<RefactoringElementImplementation>> getModelMap(List<RefactoringElementImplementation> elements){
        Map<Model, Set<RefactoringElementImplementation>> results = new HashMap<Model, Set<RefactoringElementImplementation>>();
        for(RefactoringElementImplementation element:elements){
           Component comp = element.getLookup().lookup(Component.class);
           Model model = null;
//           if(comp instanceof org.netbeans.modules.xml.schema.model.Import){
//               //special case of embedded schema import statements in WSDLModel
//               //for embedded schema, group the RE impls by Foreign Model
//                  SchemaModel mod=  (SchemaModel)comp.getModel();
//                  if(mod != null) {
//                      Component wsdlImport =mod.getSchema().getForeignParent();
//                      if(wsdlImport != null) {
//                          model = wsdlImport.getModel();
//                      } 
//               }
//           } else 
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
     
     public boolean isFatal(ErrorItem error){
        if(error.getLevel() == ErrorItem.Level.FATAL)
            return true;
        else
            return false;
   } 
     
     public Problem processErrors(List<ErrorItem> errorItems){
        
        if (errorItems == null || errorItems.size()== 0){
            return null;
        }
        Problem parent = null;
        Problem child = null;
        Problem head = null;
        Iterator<ErrorItem> iterator = errorItems.iterator();
                
        while(iterator.hasNext()) {
            ErrorItem error = iterator.next();
            if(parent == null ){
                parent = new Problem(isFatal(error), error.getMessage());
                child = parent;
                head = parent;
                continue;
            }
            child = new Problem(isFatal(error), error.getMessage());
            parent.setNext(child);
            parent = child;
            
        }
        
       
        return head;
    }
     
     
     
    public String getModelReference(Component component) {
//        if (component instanceof Import) {
//            return ((Import)component).getLocation();
//        }
        return null;
    }

    public void setModelReference(Component component, String location) {
        //do nothing
    }

      
    public Collection<Component> getExternalReferences(Model model) {
        Collection<Component> refs = new ArrayList<Component>();
        if(model instanceof IEPModel){
            //find external referenced wsdl model.
            
//            refs.addAll(((WSDLModel)model).getDefinitions().getImports());
        }
        return refs;
    }
    
    public Model getModel(ModelSource source) {
       FileObject fo = source.getLookup().lookup(FileObject.class);
       if ( WSDL_MIME_TYPE.equals(FileUtil.getMIMEType(fo))) {
           IEPModel model = IEPModelFactory.getDefault().getModel(source);
           return model;
       }
       return null;
    }
}
