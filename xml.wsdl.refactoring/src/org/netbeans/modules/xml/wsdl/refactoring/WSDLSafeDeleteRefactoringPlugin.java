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

package org.netbeans.modules.xml.wsdl.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.Action;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.ProblemDetailsFactory;
import org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.FauxRefactoringElement;
import org.netbeans.modules.xml.refactoring.XMLRefactoringPlugin;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.RefactoringUtil;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.refactoring.ui.WhereUsedQueryUI;
import org.netbeans.modules.xml.refactoring.ui.views.WhereUsedView;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;



/**
 *
 * @author Sonali Kochar
 */
public class WSDLSafeDeleteRefactoringPlugin extends WSDLRefactoringPlugin {
    
   private SafeDeleteRefactoring delete;
  
    
    public void cancelRequest() {
        
    }
    
    public Problem fastCheckParameters() {
        return null;
        
    }
    
   
    
    
    /**
     * Creates a new instance of XMLWhereUsedRefactoringPlugin
     */
    public WSDLSafeDeleteRefactoringPlugin(SafeDeleteRefactoring refactoring) {
        delete = refactoring;
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
        NamedReferenceable obj = delete.getRefactoringSource().lookup(NamedReferenceable.class);
       
        if(obj == null)
            return null;
     
        Model model = obj.getModel();
        //Fix for NPE 125763
        if(model == null)
            return null;
        ErrorItem error = RefactoringUtil.precheckTarget(model, true);
        if (error != null ){
           return new Problem(isFatal(error), error.getMessage());
        } 
        
        return null;       
        
    }
    
       
    /** Collects refactoring elements for a given refactoring.
     * @param refactoringElements Collection of refactoring elements - the implementation of this method
     * should add refactoring elements to this collections. It should make no assumptions about the collection
     * content.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        NamedReferenceable obj = delete.getRefactoringSource().lookup(NamedReferenceable.class);
       
        if( obj == null )
            return null;
       
        //get the session obj to pass to the FindSchemaUsageVisitor
        session = refactoringElements.getSession();
        fireProgressListenerStart(ProgressEvent.START, -1);
        RefactoringSession inner = RefactoringSession.create("delete");
        Referenceable ref = (Referenceable)obj;
        WhereUsedQuery query = new WhereUsedQuery(Lookups.singleton(ref));
        query.prepare(inner);
        WhereUsedView wuv = new WhereUsedView((Referenceable) obj);
        WhereUsedQueryUI ui = new WhereUsedQueryUI((Referenceable) obj);
        
        this.findErrors = new ArrayList<ErrorItem>();
        Set<Component> searchRoots = getSearchRoots(obj);
       
        List<WSDLRefactoringElement> elements = new ArrayList<WSDLRefactoringElement>();
        for (Component root : searchRoots) {
            List<WSDLRefactoringElement> founds = find(obj, root);
            if (founds != null) {
                   elements.addAll(founds);
            }
        }
       
        List<ErrorItem> allErrors = new ArrayList<ErrorItem>();
        if(findErrors.size() <= 0) {
            if(elements !=null && elements.size() > 0) {
                List<Model> models = getModels(elements);
                List<ErrorItem> errors = RefactoringUtil.precheckUsageModels(models, true);
                if(errors !=null && errors.size() > 0 ){
                    allErrors.addAll(errors);
                } 
                
                errors = SharedUtils.addCascadeDeleteErrors(models, WSDLModel.class);
                if(errors != null && errors.size() > 0) {
                    allErrors.addAll(errors);
                }
            } 
        } else
            allErrors.addAll(findErrors);
       
        
       //get the transaction model and register it to receive the commit event
        XMLRefactoringTransaction transaction = delete.getContext().lookup(XMLRefactoringTransaction.class);
        transaction.register((XMLRefactoringPlugin)this, elements);
        refactoringElements.registerTransaction(transaction);
        
        for (WSDLRefactoringElement element : elements) {
             //System.out.println("WSDLSafeDeleteRefactoring::adding element");
             element.addTransactionObject(transaction);
             refactoringElements.add(delete, element);
             fireProgressListenerStep();
        }
       
       //add a faux refactoring element to represent the target/object being refactored
        //this element is to be added to the bag only as it will not participate in actual refactoring
        Model mod = SharedUtils.getModel(obj);
        //Fix for NPE 125763
        if(mod != null) {
            FileObject fo = mod.getModelSource().getLookup().lookup(FileObject.class);
            if ( WSDL_MIME_TYPE.equals(FileUtil.getMIMEType(fo))) {
               refactoringElements.add(delete, new FauxRefactoringElement(obj, NbBundle.getMessage(WSDLSafeDeleteRefactoringPlugin.class, "LBL_Delete")));
            }
        }
       if(allErrors.size() > 0) {
            Problem problem = processErrors(allErrors, ui, inner);
            fireProgressListenerStop();
            return problem;
        }
        
        fireProgressListenerStop();
        
      return null;
    }

        
    public Problem processErrors(List<ErrorItem> errorItems, WhereUsedQueryUI ui, RefactoringSession inner) {
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
                parent = new Problem(isFatal(error), error.getMessage(),ProblemDetailsFactory.createProblemDetails(new ProblemDetailsImplemen(ui, inner)));
                child = parent;
                head = parent;
                //the 5.5.1 code shows only the first error
                //the comments from original code
                //TODO straighten out usability issue of Safe Delete whether to allow 
                // cascade delete where possible
                // for now just a hack to show only first one assuming all entailed cascade delete not supported.
                break;
                //continue;
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
    
    
    private class ProblemDetailsImplemen implements ProblemDetailsImplementation {
        
        private RefactoringUI ui;
        private RefactoringSession rs;
        
        public ProblemDetailsImplemen(RefactoringUI ui, RefactoringSession rs) {
            this.ui = ui;
            this.rs = rs;
        }
        
        public void showDetails(Action callback, Cancellable parent) {
            parent.cancel();
            UI.openRefactoringUI(ui, rs, callback);
        }
        
        public String getDetailsHint() {
            return NbBundle.getMessage(WSDLSafeDeleteRefactoringPlugin.class, "LBL_ShowUsages");
                        
        }
    
}
     /** Does the change for a given refactoring.
     * @param refactoringElements Collection of refactoring elements 
     */
    public void doRefactoring(List<RefactoringElementImplementation> elements) throws IOException{
       // no supports for cascade delete or reset reference at this time.
    }      
    
     public String getModelReference(Component component) {
        if (component instanceof Import) {
            return ((Import)component).getLocation();
        }
        return null;
    }
    
    
}

