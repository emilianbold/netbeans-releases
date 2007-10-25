/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xml.schema.refactoring;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.FauxRefactoringElement;
import org.netbeans.modules.xml.refactoring.XMLRefactoringPlugin;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;




/**
 *
 * @author Sonali Kochar
 */
public class SchemaMoveRefactoringPlugin extends SchemaRefactoringPlugin  implements XMLRefactoringPlugin {
    
    private MoveRefactoring request;
  //  List<RefactoringElementImplementation> elements;
    public static final String XSD_MIME_TYPE = "application/x-schema+xml";
   
    public void cancelRequest() {
        
    }
    
    public Problem fastCheckParameters() {
        URL url = ((MoveRefactoring)request).getTarget().lookup(URL.class);
        if(url == null)
            return null;
        FileObject targetF = URLMapper.findFileObject(url);  
        if ((targetF!=null && !targetF.canWrite())) {
            return new Problem(true, NbBundle.getMessage(SchemaMoveRefactoringPlugin.class,"ERR_PackageIsReadOnly"));                   
        }
        return null;
    }
    
    
    /**
     * Creates a new instance of XMLWhereUsedRefactoringPlugin
     */
    public SchemaMoveRefactoringPlugin(MoveRefactoring refactoring) {
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
        //System.out.println("SchemaMoveRefactoringPluging : prepare");
        if(obj == null)
            return null;
        if( !(obj instanceof Model) )
            return null;
        fireProgressListenerStart(ProgressEvent.START, -1);
        //get the gloabl XML transaction object
        XMLRefactoringTransaction transaction = request.getContext().lookup(XMLRefactoringTransaction.class);
        
        //get the search roots
        Set<Component> searchRoots = new HashSet<Component>();
        //do we have any given search roots??
        Component searchRoot = request.getContext().lookup(Component.class);
        if(searchRoot == null )
            searchRoots = getSearchRoots(obj);
        else
            searchRoots.add(searchRoot);
        
        this.findErrors = new ArrayList<ErrorItem>();
        List<SchemaRefactoringElement> elements = new ArrayList<SchemaRefactoringElement>();
        for (Component root : searchRoots) {
            List<SchemaRefactoringElement> founds = find(obj, root);
            if (founds != null && founds.size() > 0) {
                   elements.addAll(founds);
            }
        }
        
         //register with the gloabl XML transaction object
         transaction.register((XMLRefactoringPlugin)this, elements);
        
        //register with the Refactoring API
        refactoringElements.registerTransaction(transaction);
         
        for (SchemaRefactoringElement elem : elements) {
           // System.out.println("SchemaMoveRefactoring::adding element");
            elem.addTransactionObject(transaction);
            refactoringElements.add(request, elem);
            fireProgressListenerStep();
         }
        
        //add a faux refactoring element to represent the target/object being refactored
        //this element is to be added to the bag only as it will not participate in actual refactoring
        Model mod = SharedUtils.getModel(obj);
        FileObject fo = mod.getModelSource().getLookup().lookup(FileObject.class);
        if ( XSD_MIME_TYPE.equals(FileUtil.getMIMEType(fo))) {
           refactoringElements.add(request, new FauxRefactoringElement(obj, NbBundle.getMessage(SchemaMoveRefactoringPlugin.class, "LBL_Move")));
        }      
        if(findErrors.size() > 0)
            return processErrors(findErrors);
        
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
                 _refactorUsages(model, modelsInRefactoring.get(model), (MoveRefactoring)request);
        }
        
    }   
    
    private void _refactorUsages(Model mod,Set<RefactoringElementImplementation> elements, MoveRefactoring request ) throws IOException{
        if (mod == null) return;             
        if (! (mod instanceof SchemaModel)) return;
        SchemaModel model = (SchemaModel)mod;
       // ModelSource movedSource = request.getContext().lookup(ModelSource.class);
        boolean startTransaction = ! model.isIntransaction();
        try {
            if (startTransaction) {
                model.startTransaction();
            }
            for (RefactoringElementImplementation u : elements) {
                if ( !(u instanceof SchemaRefactoringElement) )
                    continue;
                SchemaModelReference ref = (SchemaModelReference) u.getLookup().lookup(SchemaModelReference.class);
                if (ref!=null) {
                    String newLocation = ref.getSchemaLocation();
                    try {
                        newLocation = SharedUtils.calculateNewLocationString(mod,request);
                    } catch (Exception e)                    {}
                    
                    ref.setSchemaLocation(newLocation);
                }
            }
        } finally {
            if (startTransaction && model.isIntransaction()) {
                model.endTransaction();
            }
        }
    }
    
     
     public void setModelReference(Component component, String location) {
        if(component instanceof SchemaModelReference){
            Model model = component.getModel();
            boolean startTransaction = ! model.isIntransaction();
            if (startTransaction) {
                model.startTransaction();
            }
            ((SchemaModelReference)component).setSchemaLocation(location);
            
            if (startTransaction && model.isIntransaction()) 
               model.endTransaction();
        }
    }

     
}

