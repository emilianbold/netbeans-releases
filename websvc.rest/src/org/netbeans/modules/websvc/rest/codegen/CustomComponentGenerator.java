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
package org.netbeans.modules.websvc.rest.codegen;

import java.io.IOException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.websvc.rest.codegen.model.CustomComponentBean;
import org.netbeans.modules.websvc.rest.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.rest.component.palette.RestComponentData;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.openide.filesystems.FileObject;
import static com.sun.source.tree.Tree.Kind.*;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class CustomComponentGenerator extends RestComponentGenerator {
    
    public static final String REST_CONNECTION = "RestConnection"; //NOI18N
    public static final String REST_CONNECTION_TEMPLATE = "Templates/WebServices/RestConnection.java"; //NOI18N

    public CustomComponentGenerator(FileObject targetFile, RestComponentData data) throws IOException {
        this(targetFile, new CustomComponentBean(data));
    }

    private CustomComponentGenerator(FileObject targetFile, CustomComponentBean bean) {
        super(targetFile, bean);
    }

    @Override
    protected void preGenerate() {
        JavaSource source = JavaSourceHelper.createJavaSource(REST_CONNECTION_TEMPLATE, destDir, bean.getPackageName(), REST_CONNECTION);
    }

    protected String getCustomMethodBody() throws IOException {
        String paramStr = "";       //NOI18N
    
        int count = 0;
        for (ParameterInfo param : bean.getInputParameters()) {
            if (count++ > 0) {
                paramStr += ", ";       //NOI18N
            }
            
            paramStr += param.getName();
        }
        
        return "return execute(" + paramStr + ")";
    }
}