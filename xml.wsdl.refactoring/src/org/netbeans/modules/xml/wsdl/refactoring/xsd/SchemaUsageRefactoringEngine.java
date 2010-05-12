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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.wsdl.refactoring.xsd;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.RefactoringEngine;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.refactoring.WSDLRefactoringElement;
import org.netbeans.modules.xml.wsdl.refactoring.WSDLRefactoringEngine;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * Provides capability to search for usages of schema components in WSDL models.
 * Provides capability to refactor schema component references in WSDL models.
 *
 * @author Nam Nguyen
 */
public class SchemaUsageRefactoringEngine {
    
    /** Creates a new instance of WSDLRefactoringEngine */
    public SchemaUsageRefactoringEngine() {
    }

      
    public Component getSearchRoot(FileObject file) throws IOException {
        return WSDLRefactoringEngine.getWSDLDefinitions(file);
    }
    
    public List<WSDLRefactoringElement> findUsages(Component target, Component searchRoot, RefactoringSession session, XMLRefactoringTransaction transaction) {
        if (target instanceof ReferenceableSchemaComponent &&
            searchRoot instanceof Definitions) {
            return new FindSchemaUsageVisitor().findUsages(
                    (ReferenceableSchemaComponent)target, (Definitions)searchRoot, session, transaction);
        }
        return null;
    }

    public List<WSDLRefactoringElement> findUsages(Model target, Component searchRoot) {
        List<WSDLRefactoringElement> elements = new ArrayList<WSDLRefactoringElement>();
        if (target instanceof SchemaModel && searchRoot instanceof Definitions) {
            Definitions definitions = (Definitions) searchRoot;
            String namespace = ((SchemaModel)target).getSchema().getTargetNamespace();
            FileObject fo = searchRoot.getModel().getModelSource().getLookup().lookup(FileObject.class);
            if (namespace == null) return null;
            for (Import i : definitions.getImports()) {
                if (! namespace.equals(i.getNamespace())) {
                    continue;
                }
                ModelSource ms = resolve(definitions.getModel(), i.getLocation(), namespace);
                if (areSameSource(ms, target.getModelSource())) {
                    //UsageGroup ug = new UsageGroup(this, searchRoot.getModel(), (SchemaModel) target);
                   // ug.addItem(i);
                    elements.add(new WSDLRefactoringElement(searchRoot.getModel(), target, i));
                    return elements;
                }
            }
            Types types = definitions.getTypes();
            Collection<Schema> schemas = Collections.emptyList();
            if (types != null && types.getSchemas() != null) {
                schemas = types.getSchemas();
            }
            for (Schema schema : schemas) {
                for (SchemaModelReference ref : schema.getSchemaReferences()) {
                    if (isReferenceTo(ref, (SchemaModel) target)) {
                       // UsageGroup ug = new UsageGroup(this, searchRoot.getModel(), (SchemaModel) target);
                        //ug.addItem(ref);
                        FileObject fo1 = ref.getModel().getModelSource().getLookup().lookup(FileObject.class);
                        elements.add(new WSDLRefactoringElement(searchRoot.getModel(), target, ref));
                        return elements;
                    }
                }
            }
        }
        return null;
    }
 
    public void _refactorUsages(Model mod, Set<RefactoringElementImplementation> elements, AbstractRefactoring request) throws IOException {
        if (request == null || elements == null || mod == null) return;
        if (! (mod instanceof WSDLModel)) return;
        WSDLModel model = (WSDLModel) mod;
        boolean startTransaction = ! model.isIntransaction();
        
        try {
            if (startTransaction) {
                model.startTransaction();
            }
            for (RefactoringElementImplementation u:elements) {
                if (u.getLookup().lookup(Import.class)!=null) {
                    Import im = u.getLookup().lookup(Import.class);
                    String newLocation = im.getLocation();
                    if(request instanceof RenameRefactoring ) {
                        newLocation = SharedUtils.calculateNewLocationString(im.getLocation(), (RenameRefactoring)request);
                    } else if (request instanceof MoveRefactoring) {
                        try {
                            newLocation = SharedUtils.calculateNewLocationString(mod, (MoveRefactoring)request);
                        } catch (Exception e){}
                    }
                    im.setLocation(newLocation);
                } else if (u.getLookup().lookup(SchemaModelReference.class)!=null) {
                    SchemaModelReference ref = u.getLookup().lookup(SchemaModelReference.class);
                    String newLocation = ref.getSchemaLocation();
                    if(request instanceof RenameRefactoring ) {
                         newLocation = calculateNewLocationString(ref.getSchemaLocation(), (RenameRefactoring)request);
                    } else if (request instanceof MoveRefactoring){
                        try {
                             newLocation = SharedUtils.calculateNewLocationString(mod, (MoveRefactoring)request);
                        }catch (Exception e){}
                    }
                    ref.setSchemaLocation(newLocation);
                }
            }
        } finally {
            if (startTransaction && model.isIntransaction()) {
                model.endTransaction();
            }
        }
    }
    
     public String calculateNewLocationString(String currentLocationString, RenameRefactoring request) {
        StringBuilder sb = new StringBuilder();
        int i = currentLocationString.lastIndexOf('/');
        if (i > -1) {
            sb.append(currentLocationString.substring(0, i+1));
        }
        sb.append(request.getNewName());
        sb.append("."); //NOI18N
        Referenceable ref = request.getRefactoringSource().lookup(Referenceable.class);
        Model model = SharedUtils.getModel(ref);
        FileObject fo = model.getModelSource().getLookup().lookup(FileObject.class);
        sb.append(fo.getExt());
        return sb.toString();
    }
    
    public static String getLocationReferenceAttributeName(Component usageComponent) {
        if (usageComponent instanceof org.netbeans.modules.xml.wsdl.model.Import) {
            return "location"; //NOI18N
        } else if (usageComponent instanceof SchemaModelReference) {
            return "schemaLocation"; //NOI18N
        } else {
            return "ref"; //NO18N
        }
    }
    
    public static ModelSource resolveModelSource(
            String location, Model currentModel, CatalogModel currentCatalog) {
        ModelSource ms = null;
        try {
            if (location != null) {
                URI uri = new URI(location);
                ms = currentCatalog.getModelSource(uri, currentModel.getModelSource());
            }
        } catch (URISyntaxException ex) {
            log(ex.getMessage());
        } catch (CatalogModelException nse) {
            // unable to resolve location
            log(nse.getMessage());
        }
        return ms;
    }
    
    private static void log(String message) {
        Logger.getLogger(SchemaUsageRefactoringEngine.class.getName()).log(Level.FINE, message);
    }
    
    public static ModelSource resolve(Model currentModel, String hint, String backup) {
        CatalogModel nr = (CatalogModel) currentModel.getModelSource().getLookup().lookup(CatalogModel.class);
        
        // try hint
        ModelSource ms = resolveModelSource(hint, currentModel, nr);
        
        // hint didn't work now try backup
        if (ms == null) {
            ms = resolveModelSource(backup, currentModel, nr);
        }
        
        return ms;
    }
    
    public static boolean areSameSource(ModelSource m1, ModelSource m2) {
        if (m1 == null || m2 == null) return false;
        DataObject dobj1 = (DataObject) m1.getLookup().lookup(DataObject.class);
        DataObject dobj2 = (DataObject) m2.getLookup().lookup(DataObject.class);
        return dobj1 != null && dobj1.equals(dobj2);
    }

    public static boolean isReferenceTo(SchemaModelReference ref, SchemaModel target) {
        try {
            return target == ref.resolveReferencedModel();
        } catch(CatalogModelException ex) {
            log(ex.getMessage());
        }
        return false;
    }

    public String getModelReference(Component component) {
        if (component instanceof SchemaModelReference) {
            return ((SchemaModelReference)component).getSchemaLocation();
        }        
        return null;
    }
}
