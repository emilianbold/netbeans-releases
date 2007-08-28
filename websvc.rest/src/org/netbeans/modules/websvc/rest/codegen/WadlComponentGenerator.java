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
import java.util.List;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.websvc.rest.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.rest.codegen.model.WadlResourceBean;
import org.netbeans.modules.websvc.rest.component.palette.RestComponentData;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.openide.filesystems.FileObject;
import static com.sun.source.tree.Tree.Kind.*;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class WadlComponentGenerator extends RestComponentGenerator {

    public static final String REST_CONNECTION = "RestConnection"; //NOI18N
    public static final String REST_CONNECTION_TEMPLATE = "Templates/WebServices/RestConnection.java"; //NOI18N

    public WadlComponentGenerator(FileObject targetFile, RestComponentData data) throws IOException {
        this(targetFile, new WadlResourceBean(data));
    }

    private WadlComponentGenerator(FileObject targetFile, WadlResourceBean bean) {
        super(targetFile, bean);
    }

    @Override
    protected void preGenerate() {
        JavaSource source = JavaSourceHelper.createJavaSource(REST_CONNECTION_TEMPLATE, destDir, bean.getPackageName(), REST_CONNECTION);
    }

    protected String getCustomMethodBody() throws IOException {
        String converterName = getConverterName();
        String paramStr = null;
        StringBuffer sb1 = new StringBuffer();
        List<ParameterInfo> params = bean.getInputParameters();

        for (ParameterInfo param : params) {
            String paramName = param.getName();
            if (param.getType() != String.class) {
                sb1.append("{\"" + paramName + "\", " + paramName + ".toString()},");
            } else {
                sb1.append("{\"" + paramName + "\", " + paramName + "},");
            }
        }
        paramStr = sb1.toString();
        if (params.size() > 0) {
            paramStr = paramStr.substring(0, paramStr.length() - 1);
        }
        
        String methodBody = "String url = \"" + ((WadlResourceBean) bean).getUrl() + "\";\n";
        methodBody += "        " + converterName + " converter = new " + converterName + "();\n";
        methodBody += "        try {\n";
        methodBody += "             RestConnection cl = new RestConnection();\n";
        methodBody += "             String[][] params = new String[][]{\n";
        methodBody += "                 " + paramStr + "\n";
        methodBody += "             };\n";
        methodBody += "             String result = cl.connect(url, params);\n";
        methodBody += "             converter.setString(result);\n";
        methodBody += "             return converter;\n";
        methodBody += "        } catch (java.io.IOException ex) {\n";
        methodBody += "             java.util.logging.Logger.getLogger(" + converterName + ".class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n";
        methodBody += "        }\n";
        methodBody += "        return converter; }";
        
        return methodBody;
    }
}