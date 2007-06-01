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

 
    
    public static String getLocationReferenceAttributeName(Component usageComponent) {
        if (usageComponent instanceof org.netbeans.modules.xml.wsdl.model.Import) {
            return "location"; //NOI18N
        } else if (usageComponent instanceof SchemaModelReference) {
            return "schemaLocation"; //NOI18N
        } else {
            return "ref"; //NO18N
        }
    }
    
 }
