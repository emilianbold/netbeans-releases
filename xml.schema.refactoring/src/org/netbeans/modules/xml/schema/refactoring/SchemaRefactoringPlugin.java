/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.XMLRefactoringPlugin;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.visitor.FindUsageVisitor;
import org.netbeans.modules.xml.schema.model.visitor.Preview;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Sonali
 */
public abstract class SchemaRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin, XMLRefactoringPlugin {
    
    /** Creates a new instance of SchemaRefactoringPlugin */
    List<ErrorItem> findErrors;
    public static final String XSD_MIME_TYPE = "application/x-schema+xml";
        
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
    
    /**
     * @param component the component to check for model reference.
     * @return the reference string if this component is a reference to an 
     * external model, for example, the schema <import> component, 
     * otherwise returns null.
     */
     public String getModelReference(Component component) {
        if (component instanceof SchemaModelReference) {
            return ((SchemaModelReference)component).getSchemaLocation();
        }
        return null;
    }

    public void setModelReference(Component component, String location) {
        //do nothing
    }

    public Collection<Component> getExternalReferences(Model model) {
        Collection<Component> refs = new ArrayList<Component>();
        if(model instanceof SchemaModel) {
            refs.addAll(((SchemaModel)model).getSchema().getSchemaReferences());
        }
        return refs;
    }
   
    public Model getModel(ModelSource source) {
       FileObject fo = source.getLookup().lookup(FileObject.class);
       if ( XSD_MIME_TYPE.equals(FileUtil.getMIMEType(fo))) {
           SchemaModel model = SchemaModelFactory.getDefault().getModel(source);
           return model;
       }
       return null;
    }
    
}
