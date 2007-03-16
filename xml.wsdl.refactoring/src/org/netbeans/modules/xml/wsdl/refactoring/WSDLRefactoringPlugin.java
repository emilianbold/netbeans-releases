/*
 * WSDLRefactoringPlugin.java
 *
 * Created on February 20, 2007, 2:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.XMLRefactoringPlugin;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.visitor.FindUsageVisitor;
import org.netbeans.modules.xml.schema.model.visitor.Preview;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.refactoring.xsd.FindSchemaUsageVisitor;
import org.netbeans.modules.xml.wsdl.refactoring.xsd.SchemaUsageRefactoringEngine;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Sonali
 */
public abstract class WSDLRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {
    
    /**
     * Creates a new instance of WSDLRefactoringPlugin
     */
    List<ErrorItem> findErrors;
    RefactoringSession session;
    XMLRefactoringTransaction transaction;
    
    public WSDLRefactoringPlugin() {
    }
    
    public List<WSDLRefactoringElement> find(Referenceable target, Component searchRoot){
        if (target instanceof Model) {
            return findUsages((Model) target, searchRoot);
        } else if (target instanceof Component) {
            return findUsages((Component) target, searchRoot);
        } else {
            return null;
        }
    }
                    
    public List<WSDLRefactoringElement> findUsages(Model target, Component searchRoot) {
        List<WSDLRefactoringElement> elements = new ArrayList<WSDLRefactoringElement>();
        if (target instanceof WSDLModel && searchRoot instanceof Definitions) {
            Definitions definitions = (Definitions) searchRoot;
            String namespace = ((WSDLModel)target).getDefinitions().getTargetNamespace();
           // UsageGroup ug = new UsageGroup(this, searchRoot.getModel(), (WSDLModel) target);
            for (Import i : definitions.getImports()) {
                Model imported = null;
                if (namespace.equals(i.getNamespace())) { 
                    try {
                        imported = i.getImportedWSDLModel();
                    } catch(CatalogModelException ex) {
                        findErrors.add(new ErrorItem(searchRoot, ex.getMessage()));
                    }
                }
                if ( imported == target) {
                    elements.add(new WSDLRefactoringElement(searchRoot.getModel(), target, i));
                }
            }
        }
        
        SchemaUsageRefactoringEngine engine = new SchemaUsageRefactoringEngine();
        List<WSDLRefactoringElement> elem = engine.findUsages(target, searchRoot);
        if(elem != null)
            elements.addAll(elem);
        
        if(elements.size() > 0 )
            return elements;
        else
            return Collections.emptyList();
    }
  
    
     public List<WSDLRefactoringElement> findUsages(Component target, Component searchRoot) {
        List<WSDLRefactoringElement> elements = new ArrayList<WSDLRefactoringElement>();
        List<WSDLRefactoringElement> temp = null;
              
        if (target instanceof ReferenceableWSDLComponent && searchRoot instanceof Definitions) {
            temp = new FindWSDLUsageVisitor().findUsages((ReferenceableWSDLComponent)target, (Definitions)searchRoot) ;
            if(temp != null && temp.size() > 0 )
                elements.addAll(temp);
        }
       
        if (target instanceof ReferenceableSchemaComponent && searchRoot instanceof Definitions) {
            temp = new FindSchemaUsageVisitor().findUsages( (ReferenceableSchemaComponent)target, (Definitions)searchRoot, session, transaction);
            if(temp != null && temp.size() > 0)
                elements.addAll(temp);
        }
        
        if (elements.size() == 0) {
            return Collections.emptyList();
        } else {
            return elements;
        }
    }
     
     public List<Model> getModels(List<WSDLRefactoringElement> elements){
         List<Model> models = new ArrayList<Model>();
         for(WSDLRefactoringElement element:elements){
             models.add( ((Component)element.getComposite()).getModel());
         }
         return models;
     }
     
     public  Set<Component> getSearchRoots(Referenceable target) {
       Set<Component>  searchRoots = new HashSet<Component>();
       Set<FileObject> files = SharedUtils.getSearchFiles(target);
        for (FileObject file : files) {
            WSDLRefactoringEngine engine = new WSDLRefactoringEngine();           
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
           Model model = ((Component)element.getComposite()).getModel();
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
     
}
