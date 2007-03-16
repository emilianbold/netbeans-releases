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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 

    public void _refactorUsages(Model mod, Set<RefactoringElementImplementation> elements, RenameRefactoring request) throws IOException {
        if (request == null || elements == null || mod == null) return;
        if (! (mod instanceof WSDLModel)) return;
        WSDLModel model = (WSDLModel) mod;
        boolean startTransaction = ! model.isIntransaction();
        
        try {
            if (startTransaction) {
                model.startTransaction();
            }
            for (RefactoringElementImplementation u:elements) {
                Component comp = u.getLookup().lookup(Component.class);
                if(comp == null)
                    continue;
                if (comp instanceof Import) {
                    Import im = (Import) comp;
                    String newLocation = calculateNewLocationString(im.getLocation(), request);
                    im.setLocation(newLocation);
                } else if (comp instanceof SchemaModelReference) {
                    SchemaModelReference ref = (SchemaModelReference) comp;
                    String newLocation = calculateNewLocationString(ref.getSchemaLocation(), request);
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
    
   /* public void precheck(RefactorRequest request) {
        if (request.getTarget() instanceof SchemaComponent || 
            request.getTarget() instanceof SchemaModel) {
            if (request instanceof RenameRequest) {
                prepareDescription((RenameRequest)request);
            } else if (request instanceof DeleteRequest) {
                SharedUtils.addCascadeDeleteErrors((DeleteRequest)request, WSDLModel.class);
            } else if (request instanceof FileRenameRequest) {
                prepareDescription((FileRenameRequest)request);
            }
        }
    }

    private void prepareDescription(RenameRequest request) {
        SchemaComponent target =  (SchemaComponent) request.getTarget();
        for (UsageGroup usage : request.getUsages().getUsages()) {
            if (! (usage.getModel() instanceof WSDLModel)) {
                continue;
            }
            String ns = ((SchemaModel)target.getModel()).getEffectiveNamespace(target);
            for (Object o : usage.getItems()) {
                Usage i = (Usage) o; //strange i have to do this
                String prefix = ((AbstractDocumentComponent)i.getComponent()).lookupPrefix(ns);
                String refString = prefix + ":" + request.getNewName(); //NOI18N
                //TODO a visitor to get the right attribute name from i.getComponent().
                String refAttribute = "ref"; //NOI18N
                String msg = NbBundle.getMessage(SchemaUsageRefactoringEngine.class, 
                        "MSG_SetReferenceStringTo", refAttribute, refString);
                i.setRefactoringDescription(msg);
            }
        }
    }

    private void prepareDescription(FileRenameRequest request) {
        SchemaModel target =  (SchemaModel) request.getTargetModel();
        for (UsageGroup usage : request.getUsages().getUsages()) {
            if (! (usage.getModel() instanceof WSDLModel)) {
                continue;
            }
            for (Usage i : usage.getItems()) {
                String refAttribute = getLocationReferenceAttributeName(i.getComponent());
                String msg = NbBundle.getMessage(SchemaUsageRefactoringEngine.class, 
                        "MSG_SetLocationStringTo", refAttribute, getNewLocationValue(request, i.getComponent()));
                i.setRefactoringDescription(msg);
            }
        }
    }*/
    
    public static String getLocationReferenceAttributeName(Component usageComponent) {
        if (usageComponent instanceof org.netbeans.modules.xml.wsdl.model.Import) {
            return "location"; //NOI18N
        } else if (usageComponent instanceof SchemaModelReference) {
            return "schemaLocation"; //NOI18N
        } else {
            return "ref"; //NO18N
        }
    }
    
    /*private static String getNewLocationValue(FileRenameRequest request, Component usageComponent) {
        String current = ""; //NOI18N
        if (usageComponent instanceof Import) {
            current =((Import)usageComponent).getLocation();
        } else if (usageComponent instanceof SchemaModelReference) {
            current = ((SchemaModelReference)usageComponent).getSchemaLocation();
        }        

        return request.calculateNewLocationString(current);
    }*/

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

 //   @Override
    public String getModelReference(Component component) {
        if (component instanceof SchemaModelReference) {
            return ((SchemaModelReference)component).getSchemaLocation();
        }        
        return null;
    }
    
   
}
