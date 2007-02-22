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
package org.netbeans.modules.xml.schema.refactoring;

import java.io.IOException;
import java.util.ArrayList;
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
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.visitor.FindUsageVisitor;
import org.netbeans.modules.xml.schema.model.visitor.Preview;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Nam Nguyen
 */
public class SchemaRefactoringEngine extends RefactoringEngine {
    
    /** Creates a new instance of SchemaRefactoringEngine */
    public SchemaRefactoringEngine() {
    }

    public List<String> getSearchMimeTypes() {
        return Collections.singletonList(RefactoringUtil.XSD_MIME_TYPE);
    }

    public Component getSearchRoot(FileObject file) throws IOException {
        return RefactoringUtil.getSchema(file);
    }

    public List<UsageGroup> findUsages(Model target, Component searchRoot) {
        if (! (target instanceof SchemaModel) ||
            ! (searchRoot instanceof Schema)) {
            return null;
        }
        SchemaModel targetModel = (SchemaModel) target;
        Schema schema = (Schema) searchRoot;
        UsageGroup ug = new UsageGroup(this, schema.getModel(), targetModel);
        List<SchemaModelReference> refs = new ArrayList<SchemaModelReference>();
        refs.addAll(schema.getImports());
        refs.addAll(schema.getIncludes());
        refs.addAll(schema.getRedefines());
        for (SchemaModelReference ref : refs) {
            SchemaModel importedModel = null;
            try {
                importedModel = ref.resolveReferencedModel();
            } catch (CatalogModelException e) {
                ug.addError(searchRoot, e.getMessage());
            }
            if (targetModel.equals(importedModel)) {
                ug.addItem(ref);
                return Collections.singletonList(ug);
            }
        }
        return Collections.emptyList();
    }

    public List<UsageGroup> findUsages(Component target, Component searchRoot) {
        if (! (target instanceof ReferenceableSchemaComponent) ||
            ! (searchRoot instanceof Schema)) {
            return Collections.emptyList();
        }
        
        ReferenceableSchemaComponent referenceable = 
                (ReferenceableSchemaComponent) target;
        Schema schema = (Schema) searchRoot;
        UsageGroup u = null;
        Preview p = new FindUsageVisitor().findUsages(Collections.singleton(schema), referenceable);
        if (! p.getUsages().keySet().isEmpty()) {
            u = new UsageGroup(this, schema.getModel(), referenceable);
            for (SchemaComponent c : p.getUsages().keySet()) {
                u.addItem(c);
            }
        }
        if (u == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(u);
        }
    }

    public UIHelper getUIHelper() {
        return new SchemaUIHelper();
    }

    public void refactorUsages(RefactorRequest request) throws IOException {
        for (UsageGroup usage : request.getUsages().getUsages()) {
            if (usage.getEngine() instanceof SchemaRefactoringEngine) {
                if (request instanceof RenameRequest) {
                    new RenameReferenceVisitor().rename((RenameRequest)request, usage);
                } else if (request instanceof DeleteRequest) {
                    // no supports for cascade delete or reset reference at this time.
                } else if (request instanceof FileRenameRequest) {
                    _refactorUsages((FileRenameRequest)request, usage);
                }
            }
        }
    }
    
    void _refactorUsages(FileRenameRequest request, UsageGroup usage) {
        if (request == null || usage == null || usage.getModel() == null) return;
        if (! (usage.getModel() instanceof SchemaModel)) return;
        SchemaModel model = (SchemaModel) usage.getModel();
        boolean startTransaction = ! model.isIntransaction();
        try {
            if (startTransaction) {
                model.startTransaction();
            }
            for (Usage u : usage.getItems()) {
                if (u.getComponent() instanceof SchemaModelReference) {
                    SchemaModelReference ref = (SchemaModelReference) u.getComponent();
                    String newLocation = request.calculateNewLocationString(ref.getSchemaLocation());
                    ref.setSchemaLocation(newLocation);
                }
            }
        } finally {
            if (startTransaction && model.isIntransaction()) {
                model.endTransaction();
            }
        }
    }
    
    public <T extends RefactorRequest> boolean supportsRefactorType(Class<T> type) {
        return (type == RenameRequest.class || 
                type == DeleteRequest.class ||
                type == FileRenameRequest.class);
    }

    public void precheck(RefactorRequest request) {
        if (request.getTarget() instanceof SchemaComponent) {
            if (request instanceof RenameRequest) {
                RefactoringUtil.prepareDescription((RenameRequest)request, SchemaModel.class);
            } else if (request instanceof DeleteRequest) {
                SharedUtils.addCascadeDeleteErrors((DeleteRequest)request, SchemaModel.class);
            } else if (request instanceof FileRenameRequest) {
                RefactoringUtil.prepareDescription((FileRenameRequest)request, SchemaModel.class);
            }
        }
    }
    
    @Override
    public String getModelReference(Component component) {
        if (component instanceof SchemaModelReference) {
            return ((SchemaModelReference)component).getSchemaLocation();
        }
        return null;
    }
}
