/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.websvc.saas.codegen.java;

import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.WadlSaasBean;
import org.netbeans.modules.websvc.saas.codegen.java.support.AbstractTask;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.openide.filesystems.FileObject;

/**
 * Code generator for Accessing Saas services.
 *
 * @author nam
 */
public class JaxRsServletCodeGenerator extends JaxRsCodeGenerator {

    public JaxRsServletCodeGenerator(JTextComponent targetComponent,
            FileObject targetFile, WadlSaasMethod m) throws IOException {
        super(targetComponent, targetFile, m);
    }
    
    @Override
    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        initProgressReporting(pHandle);

        preGenerate();
        insertSaasServiceAccessCode();
        
        finishProgressReporting();

        return new HashSet<FileObject>(Collections.EMPTY_LIST);
    }

    /**
     *  Return target and generated file objects
     */
    protected void insertSaasServiceAccessCode() throws IOException {
        JavaSource targetFileJS = JavaSource.forFileObject(getTargetFile());
        ModificationResult result = targetFileJS.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                
                String methodBody = "{" + getOverridingStatements(); //NOI18N
                methodBody += getCustomMethodBody();

                methodBody += "}"; //NOI18N
                for(MimeType mime:bean.getMimeTypes()) {
                    MethodTree methodTree = JavaSourceHelper.getMethodByName(copy, bean.getGetMethodName(mime));
                    JavaSourceHelper.replaceMethodBody(copy, methodTree, methodBody);
                }
            }
        });
        result.commit();
    }

    @Override
    protected String getCustomMethodBody() throws IOException {
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
        
        String methodBody = "String url = \"" + ((WadlSaasBean) bean).getUrl() + "\";\n";
        methodBody += "        try {\n";
        methodBody += "             String[][] params = new String[][]{\n";
        methodBody += "                 " + paramStr + "\n";
        methodBody += "             };\n";
        methodBody += "             RestConnection cl = new RestConnection(url, params);\n";
        methodBody += "             String result = cl.get();\n";
        methodBody += "        } catch (java.io.IOException ex) {\n";
        methodBody += "             ex.printStackTrace();\n";
        methodBody += "        }\n }";
       
        return methodBody;
    }
    
    @Override
    public boolean showParams() {
        return getWrapperResourceFile() != null;
    }
}
