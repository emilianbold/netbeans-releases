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
import org.netbeans.modules.xml.refactoring.FileRenameRequest;
import org.netbeans.modules.xml.refactoring.RenameRequest;
import org.netbeans.modules.xml.refactoring.Usage;
import org.netbeans.modules.xml.refactoring.UsageGroup;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Nam Nguyen
 */
public class RefactoringUtil {
    public static final String XSD_MIME_TYPE = "application/x-schema+xml";  // NOI18N
    
    public static Schema getSchema(FileObject fo) throws IOException {
        if (! XSD_MIME_TYPE.equals(FileUtil.getMIMEType(fo))) {
            return null;
        }
        Schema ret = null;
        ModelSource modelSource = Utilities.getModelSource(fo, true);
        SchemaModel sm = SchemaModelFactory.getDefault().getModel(modelSource);
        assert sm != null : "Unexpected null model returned from factory";
        if (sm.getState().equals(Model.State.VALID)) {
            return sm.getSchema();
        } else {
            String msg = NbBundle.getMessage(RefactoringUtil.class, "MSG_SchemaModelNotWellFormed", fo.getPath());
            throw new IOException(msg);
        }
    }

    public static void prepareDescription(RenameRequest request, Class<? extends Model> referencingModelType) {
        SchemaComponent target =  (SchemaComponent) request.getTarget();
        for (UsageGroup usage : request.getUsages().getUsages()) {
            if (! (referencingModelType.isAssignableFrom(usage.getModel().getClass()))) {
                continue;
            }
            String ns = ((SchemaModel)target.getModel()).getEffectiveNamespace(target);
            for (Object o : usage.getItems()) {
                Usage i = (Usage) o; //strange i have to do this
                String prefix = ((AbstractDocumentComponent)i.getComponent()).lookupPrefix(ns);
                String refString = prefix + ":" + request.getNewName(); //NOI18N
                //TODO a visitor to get the right attribute name from i.getComponent().
                String refAttribute = "ref"; //NOI18N
                String msg = NbBundle.getMessage(RefactoringUtil.class, 
                        "MSG_SetReferenceStringTo", refAttribute, refString);
                i.setRefactoringDescription(msg);
            }
        }
    }

    public static void prepareDescription(FileRenameRequest request, Class<? extends Model> referencingModelType) {
        for (UsageGroup usage : request.getUsages().getUsages()) {
            if (! (referencingModelType.isAssignableFrom(usage.getModel().getClass()))) {
                continue;
            }
            for (Usage i : usage.getItems()) {
                String refAttribute = getLocationReferenceAttributeName(i.getComponent());
                String msg = NbBundle.getMessage(RefactoringUtil.class, 
                        "MSG_SetLocationStringTo", refAttribute, getNewLocationValue(request, i.getComponent()));
                i.setRefactoringDescription(msg);
            }
        }
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
    
    private static String getNewLocationValue(FileRenameRequest request, Component usageComponent) {
        String current = ""; //NOI18N
        if (usageComponent instanceof Import) {
            current =((Import)usageComponent).getLocation();
        } else if (usageComponent instanceof SchemaModelReference) {
            current = ((SchemaModelReference)usageComponent).getSchemaLocation();
        }        

        return request.calculateNewLocationString(current);
    }
}
