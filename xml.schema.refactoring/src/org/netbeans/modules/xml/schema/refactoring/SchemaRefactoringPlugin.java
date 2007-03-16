/*
 * SchemaRefactoringPlugin.java
 *
 * Created on February 20, 2007, 2:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.ProblemDetailsFactory;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.XMLRefactoringPlugin;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.refactoring.ui.WhereUsedQueryUI;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.visitor.FindUsageVisitor;
import org.netbeans.modules.xml.schema.model.visitor.Preview;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Sonali
 */
public abstract class SchemaRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {
    
    /** Creates a new instance of SchemaRefactoringPlugin */
    List<ErrorItem> findErrors;
        
    public SchemaRefactoringPlugin() {
    }
    
    public List<SchemaRefactoringElement> find(Referenceable target, Component searchRoot){
        if (target instanceof Model) {
            return findUsages((Model) target, searchRoot);
        } else if (target instanceof Component) {
            return findUsages((Component) target, searchRoot);
        } else {
            return null;
        }
    }
                    
    public List<SchemaRefactoringElement> findUsages(Model target, Component searchRoot){
        
        if (! (target instanceof SchemaModel) || ! (searchRoot instanceof Schema)) {
            return null;
        }
        SchemaModel targetModel = (SchemaModel) target;
        Schema schema = (Schema) searchRoot;
        List<SchemaModelReference> refs = new ArrayList<SchemaModelReference>();
        refs.addAll(schema.getImports());
        refs.addAll(schema.getIncludes());
        refs.addAll(schema.getRedefines());
        for (SchemaModelReference ref : refs) {
            SchemaModel importedModel = null;
            try {
                importedModel = ref.resolveReferencedModel();
            } catch (CatalogModelException e) {
                findErrors.add(new ErrorItem(searchRoot, e.getMessage()));
            }
            if (targetModel.equals(importedModel)) {
                return Collections.singletonList(new SchemaRefactoringElement(ref));
                
            }
        }
        return Collections.emptyList();
    }
  
    
     public List<SchemaRefactoringElement> findUsages(Component target, Component searchRoot) {
        if (! (target instanceof ReferenceableSchemaComponent) || ! (searchRoot instanceof Schema)) {
            return Collections.emptyList();
        }
        
        ReferenceableSchemaComponent referenceable = (ReferenceableSchemaComponent) target;
        Schema schema = (Schema) searchRoot;
        List<SchemaRefactoringElement> elements = new ArrayList<SchemaRefactoringElement>();
        Preview p = new FindUsageVisitor().findUsages(Collections.singleton(schema), referenceable);
        if (! p.getUsages().keySet().isEmpty()) {
         //   u = new UsageGroup(this, schema.getModel(), referenceable);
            for (SchemaComponent c : p.getUsages().keySet()) {
               // u.addItem(c);
                elements.add(new SchemaRefactoringElement(c));
            }
        }
        if (elements.size() == 0) {
            return Collections.emptyList();
        } else {
            return elements;
        }
    }
     
     public List<Model> getModels(List<SchemaRefactoringElement> elements){
         List<Model> models = new ArrayList<Model>();
         for(RefactoringElementImplementation element:elements){
             models.add( (element.getLookup().lookup(Component.class)).getModel());
         }
         return models;
     }
     
          
     public  Set<Component> getSearchRoots(Referenceable target) {
       Set<Component>  searchRoots = new HashSet<Component>();
       Set<FileObject> files = SharedUtils.getSearchFiles(target);
        for (FileObject file : files) {
            //SchemaRefactoringEngine engine = new SchemaRefactoringEngine();           
            try {
                Component root = RefactoringUtil.getSchema(file);
                searchRoots.add(root);
            } catch (IOException ex) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, ex.getMessage());
            }  
       }
        
        return searchRoots;
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
       
    public boolean isFatal(ErrorItem error){
        if(error.getLevel() == ErrorItem.Level.FATAL)
            return true;
        else
            return false;
   } 
   
}
