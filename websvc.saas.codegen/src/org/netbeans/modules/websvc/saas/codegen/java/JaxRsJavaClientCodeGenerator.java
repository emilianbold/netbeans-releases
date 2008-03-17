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
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.Util;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Code generator for Accessing Saas services.
 *
 * @author nam
 */
public class JaxRsJavaClientCodeGenerator extends JaxRsCodeGenerator {

    
    public JaxRsJavaClientCodeGenerator(JTextComponent targetComponent,
            FileObject targetFile, WadlSaasMethod m) throws IOException {
        super(targetComponent, targetFile, m);
    }
    
    @Override
    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        initProgressReporting(pHandle);

        preGenerate();
        
        //Create Authenticator classes
        createAuthenticatorClass();
        
        //Create Authorization classes
        createAuthorizationClasses();
        
        createSaasServiceClass();
        addSaasServiceMethod();
        addImportsToSaasService();
                
        //Modify Authenticator class
        modifyAuthenticationClass(); 
        
        insertSaasServiceAccessCode(isInBlock(getTargetComponent()));
        addImportsToTargetFile();
        
        finishProgressReporting();

        return new HashSet<FileObject>(Collections.EMPTY_LIST);
    }
    
    /**
     *  Create Authorization Frame
     */
    @Override
    public void createAuthorizationClasses() throws IOException {
        if (getBean().getAuthenticationType() == SaasAuthenticationType.SESSION_KEY) {
            try {
                String authFileName = getAuthorizationFrameClassName();
                FileObject authorizationFile;
                JavaSource authorizationJS = JavaSourceHelper.createJavaSource(
                        TEMPLATES_SAAS + Constants.SERVICE_AUTHORIZATION_FRAME+".java",
                        getSaasServiceFolder(), getSaasServicePackageName(), authFileName);// NOI18n
                Set<FileObject> files = new HashSet<FileObject>(authorizationJS.getFileObjects());
                if (files != null && files.size() > 0) {
                    authorizationFile = files.iterator().next();
                }
            } catch (Exception ex) {
                throw new IOException(
                    NbBundle.getMessage(AbstractGenerator.class,
                    "MSG_CreateAuthFrameFailed", ex)); // NOI18N
            }
        }
    }
    
    @Override
    protected String getCustomMethodBody() throws IOException {
        String paramUse = "";
        String paramDecl = "";
        
        //Evaluate parameters (query(not fixed or apikey), header, template,...)
        List<ParameterInfo> filterParams = getServiceMethodParameters();
        paramUse += Util.getHeaderOrParameterUsage(filterParams);
        paramDecl += getHeaderOrParameterDeclaration(filterParams);
        
        String methodBody = "try {\n";
        methodBody += paramDecl + "\n";
        methodBody += "             String result = " + getSaasServiceName() + "." + getSaasServiceMethodName() + "(" + paramUse + ");\n";
        methodBody += "             System.out.println(\"The SaasService returned: \"+result);\n";
        methodBody += "        } catch (java.io.IOException ex) {\n";
        methodBody += "             //java.util.logging.Logger.getLogger(this.getClass().getName()).log(java.util.logging.Level.SEVERE, null, ex);\n";
        methodBody += "             ex.printStackTrace();\n";
        methodBody += "        }\n";
       
        return methodBody;
    }
    
    @Override
    public boolean canShowResourceInfo() {
        return false;
    }
    
    @Override
    public boolean canShowParam() {
        return true;
    }
    
}
