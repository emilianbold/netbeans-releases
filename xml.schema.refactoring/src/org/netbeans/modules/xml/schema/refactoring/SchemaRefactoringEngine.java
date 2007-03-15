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
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.visitor.FindUsageVisitor;
import org.netbeans.modules.xml.schema.model.visitor.Preview;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Nam Nguyen
 */
public class SchemaRefactoringEngine { 
    
    /** Creates a new instance of SchemaRefactoringEngine */
    public SchemaRefactoringEngine() {
    }

    public List<String> getSearchMimeTypes() {
        return Collections.singletonList(RefactoringUtil.XSD_MIME_TYPE);
    }

   /* public Component getSearchRoot(FileObject file) throws IOException {
        return RefactoringUtil.getSchema(file);
    }*/
      
                
 /*   public <T extends RefactorRequest> boolean supportsRefactorType(Class<T> type) {
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
    }*/
    
  
    public String getModelReference(Component component) {
        if (component instanceof SchemaModelReference) {
            return ((SchemaModelReference)component).getSchemaLocation();
        }
        return null;
    }
    
  }
