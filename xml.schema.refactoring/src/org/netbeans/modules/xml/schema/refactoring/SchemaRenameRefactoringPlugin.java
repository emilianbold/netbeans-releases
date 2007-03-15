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

package org.netbeans.modules.xml.schema.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImpl;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.FauxRefactoringElement;
import org.netbeans.modules.xml.refactoring.XMLRefactoringPlugin;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.RefactoringUtil;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;



/**
 *
 * @author Sonali Kochar
 */
public class SchemaRenameRefactoringPlugin extends SchemaRefactoringPlugin  implements XMLRefactoringPlugin{
    
    private RenameRefactoring request;
  //  List<RefactoringElementImplementation> elements;
   
    public void cancelRequest() {
        
    }
    
    public Problem fastCheckParameters() {
        Referenceable obj = request.getRefactoringSource().lookup(Referenceable.class);
        ErrorItem error = null;
        if(obj instanceof Model) {
           error = RefactoringUtil.precheck((Model)obj, request.getNewName());
        } else if(obj instanceof Nameable) {
           error = RefactoringUtil.precheck((Nameable)obj, request.getNewName());
        }
                
        if (error != null) {
            Problem p = new Problem(true, error.getMessage());
            return p;
        }
        
        return null;
    }
    
    
    /**
     * Creates a new instance of XMLWhereUsedRefactoringPlugin
     */
    public SchemaRenameRefactoringPlugin(RenameRefactoring refactoring) {
        this.request = refactoring;
    }
    
    /** Checks pre-conditions of the refactoring and returns problems.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem preCheck() {
        return null;
    }
    
    /** Checks parameters of the refactoring.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem checkParameters() {
       Referenceable obj = request.getRefactoringSource().lookup(Referenceable.class);
       if( obj == null)
           return null;
       if( !((obj instanceof Model) ||  (obj instanceof Nameable)) )
            return null;
         
        Model model = SharedUtils.getModel(obj);
        ErrorItem error = RefactoringUtil.precheckTarget(model, true);
        if(error != null)
            return new Problem(isFatal(error), error.getMessage());
       
        if(obj instanceof Model)
            error  = RefactoringUtil.precheck((Model)model, request.getNewName());
        else if(obj instanceof Nameable)
            error = RefactoringUtil.precheck((Nameable)obj, request.getNewName());
        if(error != null)
            return new Problem(isFatal(error), error.getMessage());
        
              
        return null;
       
        
    }
    
    /** Collects refactoring elements for a given refactoring.
     * @param refactoringElements Collection of refactoring elements - the implementation of this method
     * should add refactoring elements to this collections. It should make no assumptions about the collection
     * content.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Referenceable obj = request.getRefactoringSource().lookup(Referenceable.class);
        //System.out.println("SchemaRenameRefactoringPluging : prepare");
        if(obj == null)
            return null;
        if( !((obj instanceof Model) ||  (obj instanceof Nameable)) )
            return null;
             
        fireProgressListenerStart(ProgressEvent.START, -1);
        //get the gloabl XML transaction object
        XMLRefactoringTransaction transaction = request.getContext().lookup(XMLRefactoringTransaction.class);
        
        //check if the scope if local. if the scope is local, restrict search to target model
        Set<Component> searchRoots = new HashSet<Component>();
        this.findErrors = new ArrayList<ErrorItem>();
        if(transaction.isLocal())
            searchRoots = SharedUtils.getLocalSearchRoots(obj);
        else {
            //do we have any given search roots??
            Component searchRoot = request.getContext().lookup(Component.class);
        
            if(searchRoot == null )
                searchRoots = getSearchRoots(obj);
            else
                searchRoots.add(searchRoot);
        }
        
        List<SchemaRefactoringElement> elements = new ArrayList<SchemaRefactoringElement>();
        for (Component root : searchRoots) {
            List<SchemaRefactoringElement> founds = find(obj, root);
            if (founds != null && founds.size() > 0) {
                   elements.addAll(founds);
            }
        }
       
        if(findErrors != null && findErrors.size() > 0)
            return processErrors(findErrors);
        
        if(elements.size() > 0) {
            List<Model> models = getModels(elements);
            List<ErrorItem> errors = RefactoringUtil.precheckUsageModels(models, true);
            if(errors !=null && errors.size() > 0 ){
                return processErrors(errors);
              } 
        } 
        //register with the gloabl XML transaction object
         transaction.register((XMLRefactoringPlugin)this, elements);
        
        //register with the refactoring API
        refactoringElements.registerTransaction(transaction);
        
        if (elements.size() >0 )   {
            for (RefactoringElementImplementation ug : elements) {
              //  System.out.println("SchemaRenameRefactoring::adding element");
                refactoringElements.add(request, ug);
                fireProgressListenerStep();
             }
        }
        
              
        fireProgressListenerStop();
        return null;
    }
    
   /** Does the change for a given refactoring.
     * @param refactoringElements Collection of refactoring elements 
     */
      public void doRefactoring(List<RefactoringElementImplementation> elements) throws IOException {
        Map<Model, Set<RefactoringElementImplementation>> modelsInRefactoring = SharedUtils.getModelMap(elements);
        Set<Model> models = modelsInRefactoring.keySet();
        Referenceable obj = request.getRefactoringSource().lookup(Referenceable.class);
        
        for (Model model : models) {
                if (obj instanceof Nameable ) {
                    new RenameReferenceVisitor().rename(model, modelsInRefactoring.get(model), request);
                } else if (obj instanceof Model && request instanceof RenameRefactoring) {
                    _refactorUsages(model, modelsInRefactoring.get(model), (RenameRefactoring)request);
                }
           
        }
    }   
    
    private void _refactorUsages(Model mod,Set<RefactoringElementImplementation> elements, RenameRefactoring request ) {
        if (mod == null) return;             
        if (! (mod instanceof SchemaModel)) return;
        SchemaModel model = (SchemaModel)mod;
        boolean startTransaction = ! model.isIntransaction();
        try {
            if (startTransaction) {
                model.startTransaction();
            }
            for (RefactoringElementImplementation u : elements) {
                if (u.getComposite() instanceof SchemaModelReference) {
                    SchemaModelReference ref = (SchemaModelReference) u.getComposite();
                    String newLocation = SharedUtils.calculateNewLocationString(ref.getSchemaLocation(), request);
                    ref.setSchemaLocation(newLocation);
                }
            }
        } finally {
            if (startTransaction && model.isIntransaction()) {
                model.endTransaction();
            }
        }
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
  
}

