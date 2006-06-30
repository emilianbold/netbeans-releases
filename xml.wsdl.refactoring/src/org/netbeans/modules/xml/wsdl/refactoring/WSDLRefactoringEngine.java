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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.wsdl.refactoring;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.refactoring.UsageGroup;
import org.netbeans.modules.xml.refactoring.spi.RefactoringEngine;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
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
            searchRoot instanceof Definitions) {
            return new FindWSDLUsageVisitor().findUsages(
                    (ReferenceableWSDLComponent)target, (Definitions)searchRoot, this);
        }
        return Collections.emptyList();
    }
    
}
