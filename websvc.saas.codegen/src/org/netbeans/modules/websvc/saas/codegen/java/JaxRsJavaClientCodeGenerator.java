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
import javax.xml.namespace.QName;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.WadlSaasBean;
import org.netbeans.modules.websvc.saas.codegen.java.support.Util;
import org.openide.filesystems.FileObject;

/**
 * Code generator for Accessing Saas services.
 *
 * @author nam
 */
public class JaxRsJavaClientCodeGenerator extends JaxRsCodeGenerator {

    
    public JaxRsJavaClientCodeGenerator(JTextComponent targetComponent,
            FileObject targetFile, WadlSaasMethod m) throws IOException {
        this(targetComponent, targetFile, new WadlSaasBean(m));
    }
    
    public JaxRsJavaClientCodeGenerator(JTextComponent targetComponent,
            FileObject targetFile, WadlSaasBean bean) throws IOException {
        super(targetComponent, targetFile, bean);
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
        
        //execute this block before insertSaasServiceAccessCode() 
        setJaxbWrapper();
        //No need to check scanning, since we are not locking document
        //Util.checkScanning();
        insertSaasServiceAccessCode(isInBlock(getTargetComponent()));
        addImportsToTargetFile();
        
        finishProgressReporting();

        return new HashSet<FileObject>(Collections.EMPTY_LIST);
    }
    
    private void setJaxbWrapper() {
        List<QName> repTypesFromWadl = getBean().findRepresentationTypes(getBean().getMethod());
        if(!repTypesFromWadl.isEmpty()) {
            getBean().setOutputWrapperName(repTypesFromWadl.get(0).getLocalPart());
            getBean().setOutputWrapperPackageName(
                    (getBean().getGroupName()+"."+
                        getBean().getDisplayName()).toLowerCase());
        }
    }
    
    @Override 
    public void preGenerate() throws IOException {
        addJaxbLib();
        
        super.preGenerate();
    }
    
    protected void addJaxbLib() throws IOException {
        Util.addJaxbLib(getProject());
    }
    
    /**
     *  Create Authorization Frame
     */
    @Override
    public void createAuthorizationClasses() throws IOException {
        //No need to create auth frame class
    }
    
    @Override
    protected String getCustomMethodBody() throws IOException {
        String paramUse = "";
        String paramDecl = "";
        
        //Evaluate parameters (query(not fixed or apikey), header, template,...)
        String indent = "        ";
        String indent2 = "             ";
        List<ParameterInfo> filterParams = getServiceMethodParameters();
        paramUse += Util.getHeaderOrParameterUsage(filterParams);
        paramDecl += getHeaderOrParameterDeclaration(filterParams, indent2);
        
        String methodBody = indent+"try {\n";
        methodBody += paramDecl + "\n";
        methodBody += indent2+REST_RESPONSE+" result = " + getBean().getSaasServiceName() + 
                "." + getBean().getSaasServiceMethodName() + "(" + paramUse + ");\n";
        methodBody += Util.createPrintStatement(
                getBean().getOutputWrapperPackageName(), 
                getBean().getOutputWrapperName(),
                getDropFileType(), 
                getBean().getHttpMethod(), 
                getBean().canGenerateJAXBUnmarshaller(), indent2);
        methodBody += indent+"} catch (Exception ex) {\n";
        methodBody += indent2+"ex.printStackTrace();\n";
        methodBody += indent+"}\n";
       
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
