/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.FauxRefactoringElement;
import org.netbeans.modules.xml.refactoring.XMLRefactoringPlugin;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.RefactoringUtil;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.refactoring.xsd.RenameSchemaReferenceVisitor;
import org.netbeans.modules.xml.wsdl.refactoring.xsd.SchemaUsageRefactoringEngine;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Sonali Kochar
 */
public class WSDLRenameRefactoringPlugin extends WSDLRefactoringPlugin  {
    
    private RenameRefactoring rename;
          
    public void cancelRequest() {
    }
    
    public Problem fastCheckParameters() {
        Referenceable obj = rename.getRefactoringSource().lookup(Referenceable.class);
        ErrorItem error = null;
        if(obj instanceof Model) {
           error = RefactoringUtil.precheck((Model)obj, rename.getNewName());
        } else if(obj instanceof Nameable) {
           error = RefactoringUtil.precheck((Nameable)obj, rename.getNewName());
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
    public WSDLRenameRefactoringPlugin(RenameRefactoring refactoring) {
        this.rename = refactoring;
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
       Referenceable obj = rename.getRefactoringSource().lookup(Referenceable.class);
       if( obj == null)
           return null;
       if( !((obj instanceof Model) ||  (obj instanceof Nameable)) )
            return null;
      
        Model model = SharedUtils.getModel(obj);
        ErrorItem error = RefactoringUtil.precheckTarget(model, true);
        if(error != null)
            return new Problem(isFatal(error), error.getMessage());
       
        if(obj instanceof Model)
            error  = RefactoringUtil.precheck((Model)model, rename.getNewName());
        else if(obj instanceof Nameable)
            error = RefactoringUtil.precheck((Nameable)obj, rename.getNewName());
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
        Referenceable obj = rename.getRefactoringSource().lookup(Referenceable.class);
        //System.out.println("WSDLRenameRefactoringPluging : prepare");
        if(obj == null)
            return null;
        if( !((obj instanceof Model) ||  (obj instanceof Nameable)) )
            return null;

        //get the session obj to pass to the FindSchemaUsageVisitor
        this.session = refactoringElements.getSession();
        //get the transaction object 
        this.transaction = rename.getContext().lookup(XMLRefactoringTransaction.class);
        
        fireProgressListenerStart(ProgressEvent.START, -1);
        this.findErrors = new ArrayList<ErrorItem>();
        Set<Component> searchRoots = new HashSet<Component>();
        
        //is the usage scope local
        if(transaction.isLocal())
            searchRoots = SharedUtils.getLocalSearchRoots(obj);
        else {
            //do we have any given search roots??
            Component searchRoot = rename.getContext().lookup(Component.class);
        
            if(searchRoot == null )
                searchRoots = getSearchRoots(obj);
            else
                searchRoots.add(searchRoot);
        }
        
        
        List<WSDLRefactoringElement> elements = new ArrayList<WSDLRefactoringElement>();
        for (Component root : searchRoots) {
            List<WSDLRefactoringElement> founds = find(obj, root);
            if (founds != null && founds.size() > 0 ) {
                elements.addAll(founds);
            }
        }
        
                
        if(elements.size() > 0) {
            List<Model> models = getModels(elements);
            List<ErrorItem> errors = RefactoringUtil.precheckUsageModels(models, true);
            if(errors !=null && errors.size() > 0 ){
                return processErrors(errors);
              } 
        } 
        
        //get the transaction object and register it to recieve the commit signal
        transaction.register((XMLRefactoringPlugin)this, elements);
        refactoringElements.registerTransaction(transaction);
        
         for (WSDLRefactoringElement ug : elements) {
             refactoringElements.add(rename, ug);
             ug.addTransactionObject(transaction);
             fireProgressListenerStep();
         }
       
        //add a faux refactoring element to represent the target/object being refactored
        //this element is to be added to the bag only as it will not participate in actual refactoring
        Model mod = SharedUtils.getModel(obj);
        FileObject fo = mod.getModelSource().getLookup().lookup(FileObject.class);
       if ( WSDL_MIME_TYPE.equals(FileUtil.getMIMEType(fo))) {
           refactoringElements.add(rename, new FauxRefactoringElement(obj, NbBundle.getMessage(WSDLRenameRefactoringPlugin.class, "LBL_Rename")));
       }
         
        //were there any errors during find??
        if(findErrors.size() > 0)
            return processErrors(findErrors);
        
        fireProgressListenerStop();
        return null;
    }

    /** Does the change for a given refactoring.
     * @param refactoringElements Collection of refactoring elements 
     */
    public void doRefactoring(List<RefactoringElementImplementation> elements) throws IOException {
        Map<Model, Set<RefactoringElementImplementation>> modelsInRefactoring = getModelMap(elements);
        Set<Model> models = modelsInRefactoring.keySet();
        Referenceable obj = rename.getRefactoringSource().lookup(Referenceable.class);
        for(Model model: models) {
            if (model instanceof WSDLModel) {
                if (obj instanceof WSDLComponent) {
                    new WSDLRenameReferenceVisitor().refactor(model, modelsInRefactoring.get(model), rename);
                } else if (obj instanceof WSDLModel ) {
                    new WSDLRefactoringEngine()._refactorUsages(model, modelsInRefactoring.get(model), rename );                    
                } else if(obj instanceof ReferenceableSchemaComponent){
                    new RenameSchemaReferenceVisitor().rename(model, modelsInRefactoring.get(model), rename);
                }else{
                    new SchemaUsageRefactoringEngine()._refactorUsages(model, modelsInRefactoring.get(model), rename);
                }
                    
            }       
            
            
        }
    }

    public String getModelReference(Component component) {
        if (component instanceof Import) {
            return ((Import)component).getLocation();
        }
        return null;
    }
}
