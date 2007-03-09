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
package org.netbeans.modules.xml.wsdl.refactoring;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.refactoring.DeleteRequest;
import org.netbeans.modules.xml.refactoring.FileRenameRequest;
import org.netbeans.modules.xml.refactoring.RefactorRequest;
import org.netbeans.modules.xml.refactoring.RenameRequest;
import org.netbeans.modules.xml.refactoring.Usage;
import org.netbeans.modules.xml.refactoring.UsageGroup;
import org.netbeans.modules.xml.refactoring.spi.RefactoringEngine;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Nam Nguyen
 */
public class WSDLRefactoringEngine extends RefactoringEngine {
    public static final String WSDL_MIME_TYPE = "text/xml-wsdl";  // NOI18N
    
    /** Creates a new instance of WSDLRefactoringEngine */
    public WSDLRefactoringEngine() {
    }

    public Component getSearchRoot(FileObject fo) throws IOException {
        return getWSDLDefinitions(fo);
    }
    
    public static Definitions getWSDLDefinitions(FileObject fo) throws IOException {
        if (! WSDL_MIME_TYPE.equals(FileUtil.getMIMEType(fo))) {
            return null;
        }
        ModelSource modelSource = Utilities.getModelSource(fo, true);
        WSDLModel model = WSDLModelFactory.getDefault().getModel(modelSource);
        if (model != null) {
            if (model.getState().equals(Model.State.VALID)) {
                return model.getDefinitions();
            } else {
                String msg = NbBundle.getMessage(WSDLRefactoringEngine.class, 
                        "MSG_ModelSourceMalformed", fo.getPath());
                throw new IOException(msg);
            }
        }
        return null;
    }

    public List<UsageGroup> findUsages(Component target, Component searchRoot) {
        if (target instanceof ReferenceableWSDLComponent &&
            searchRoot instanceof Definitions) 
        {
            return new FindWSDLUsageVisitor().findUsages(
                    (ReferenceableWSDLComponent)target, (Definitions)searchRoot, this);
        }
        return Collections.emptyList();
    }

    public void refactorUsages(RefactorRequest request) throws IOException {
        for (UsageGroup g : request.getUsages().getUsages()) {
            if (g.getModel() instanceof WSDLModel) {
                if (request.getTarget() instanceof WSDLComponent) {
                    if (request instanceof RenameRequest) {
                        new WSDLRenameReferenceVisitor().refactor((RenameRequest) request, g);
                    } else if (request instanceof DeleteRequest) {
                        // cascade delete or reset reference is not supported
                    } 
                } else if (request.getTarget() instanceof WSDLModel &&
                           request instanceof FileRenameRequest) {
                    _refactorUsages((FileRenameRequest) request, g);
                    
                }   
            }
        }
    }
    
    void _refactorUsages(FileRenameRequest request, UsageGroup usage) {
        if (request == null || usage == null || usage.getModel() == null) return;
        if (! (usage.getModel() instanceof WSDLModel)) return;

        Model model = usage.getModel();
        boolean startTransaction = ! model.isIntransaction();
        try {
            if (startTransaction) {
                model.startTransaction();
            }
        
            for (Usage u : usage.getItems()) {
                if (u.getComponent() instanceof Import) {
                    Import ref = (Import) u.getComponent();
                    String newLocation = request.calculateNewLocationString(ref.getLocation());
                    ref.setLocation(newLocation);
                }
            }
        } finally {
            if (startTransaction && model.isIntransaction()) {
                model.endTransaction();
            }
        }
    }

    public void precheck(RefactorRequest request) {
        if (request.getTarget() instanceof WSDLComponent) {
            if (request instanceof DeleteRequest) {
                SharedUtils.addCascadeDeleteErrors((DeleteRequest)request, WSDLModel.class);
            } else if (request instanceof RenameRequest) {
                prepareDescription((RenameRequest) request);
            } else if (request instanceof FileRenameRequest) {
                prepareDescription((FileRenameRequest) request);
            }
        }
    }

    /**
     * Returns UI helper in displaying the usages.  Implementation could override
     * the default UI to help display usages in a more intuitive way than the 
     * generic helper.
     */
    public UIHelper getUIHelper() {
        return new WSDLUIHelper();
    }

    public List<UsageGroup> findUsages(Model target, Component searchRoot) {
        if (target instanceof WSDLModel &&
            searchRoot instanceof Definitions) {
            Definitions definitions = (Definitions) searchRoot;
            String namespace = ((WSDLModel)target).getDefinitions().getTargetNamespace();
            UsageGroup ug = new UsageGroup(this, searchRoot.getModel(), (WSDLModel) target);
            for (Import i : definitions.getImports()) {
                Model imported = null;
                if (namespace.equals(i.getNamespace())) {
                    try {
                        imported = i.getImportedWSDLModel();
                    } catch(CatalogModelException ex) {
                        ug.addError(searchRoot, ex.getMessage());
                    }
                }

                if (imported == target) {
                    ug.addItem(i);
                }
            }
            return Collections.singletonList(ug);
        }
        return Collections.emptyList();
    }
    
    private void prepareDescription(RenameRequest request) {
        WSDLComponent target =  (WSDLComponent) request.getTarget();
        for (UsageGroup usage : request.getUsages().getUsages()) {
            if (! (usage.getModel() instanceof WSDLModel)) {
                continue;
            }
            String ns = ((WSDLModel)target.getModel()).getDefinitions().getTargetNamespace();
            for (Usage i : usage.getItems()) {
                String prefix = ((AbstractDocumentComponent)i.getComponent()).lookupPrefix(ns);
                String refString = prefix + ":" + request.getNewName(); //NOI18N
                //TODO a visitor to get the right attribute name from i.getComponent().
                String refAttribute = "ref"; //NOI18N
                String msg = NbBundle.getMessage(WSDLRefactoringEngine.class, 
                        "MSG_SetReferenceStringTo", refAttribute, refString);
                i.setRefactoringDescription(msg);
            }
        }
    }

    private void prepareDescription(FileRenameRequest request) {
        WSDLModel target =  (WSDLModel) request.getTargetModel();
        for (UsageGroup usage : request.getUsages().getUsages()) {
            if (! (usage.getModel() instanceof WSDLModel) && 
                ! (usage.getEngine() instanceof WSDLRefactoringEngine)) {
                continue;
            }
            for (Usage i : usage.getItems()) {
                if (i.getComponent() instanceof Import) {
                    String refAttribute = "location"; //NOI18N
                    String msg = NbBundle.getMessage(WSDLRefactoringEngine.class, 
                            "MSG_SetLocationStringTo", refAttribute, getNewLocationValue(request, i.getComponent()));
                    i.setRefactoringDescription(msg);
                }
            }
        }
    }
    
    private static String getNewLocationValue(FileRenameRequest request, Component usageComponent) {
        String current = ""; //NOI18N
        if (usageComponent instanceof Import) {
            current =((Import)usageComponent).getLocation();
        }        

        return request.calculateNewLocationString(current);
    }
    
    @Override
    public String getModelReference(Component component) {
        if (component instanceof Import) {
            return ((Import)component).getLocation();
        }
        return null;
    }
}
