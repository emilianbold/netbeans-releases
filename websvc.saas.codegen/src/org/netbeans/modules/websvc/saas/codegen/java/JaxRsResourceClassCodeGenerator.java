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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.WadlSaasBean;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.Util;
import org.openide.filesystems.FileObject;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class JaxRsResourceClassCodeGenerator extends JaxRsCodeGenerator {
    
    private JavaSource loginJS;
    private FileObject loginFile;
    private JavaSource callbackJS;
    private FileObject callbackFile;
    
    public JaxRsResourceClassCodeGenerator(JTextComponent targetComponent, 
            FileObject targetFile, WadlSaasMethod m) throws IOException {
        super(targetComponent, targetFile, m);
        getBean().setOutputWrapperName(
                getBean().getSaasName()+
                AbstractGenerator.CONVERTER_SUFFIX);
        getBean().setName(getBean().getSaasName()+getBean().getName());
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
    
        FileObject outputWrapperFO = null;
        List<QName> repTypesFromWadl = getBean().findRepresentationTypes(getBean().getMethod());
        if(!repTypesFromWadl.isEmpty()) {
            /*outputWrapperFO = SourceGroupSupport.getFileObjectFromClassName(
                    getBean().getGroupName()+"."+getBean().getDisplayName()+
                    "."+repTypesFromWadl.get(0).getLocalPart(), getProject());*/
            getBean().setOutputWrapperName(repTypesFromWadl.get(0).getLocalPart());
            getBean().setOutputWrapperPackageName(getBean().getGroupName()+"."+getBean().getDisplayName());
            String uriTemplate = Util.lowerFirstChar(getBean().getName());
            if(uriTemplate.endsWith(AbstractGenerator.RESOURCE_SUFFIX))
                uriTemplate = uriTemplate.substring(0, uriTemplate.length()-8);
            getBean().setUriTemplate(uriTemplate);
        } else {
            outputWrapperFO = generateJaxbOutputWrapper();
            setJaxbOutputWrapperSource(JavaSource.forFileObject(outputWrapperFO));
        }
        generateSaasServiceResourceClass();
        addSubresourceLocator();
        FileObject refConverterFO = getOrCreateGenericRefConverter().
                getFileObjects().iterator().next();
        modifyTargetConverter();
        FileObject[] result = new FileObject[]{getTargetFile(), 
            getWrapperResourceFile(), refConverterFO, outputWrapperFO};
        if (outputWrapperFO == null) {
            result = new FileObject[]{getTargetFile(), getWrapperResourceFile(), refConverterFO};
        }
        JavaSourceHelper.saveSource(result);

        finishProgressReporting();

        return new HashSet<FileObject>(Arrays.asList(result));
    }
    
    /**
     *  Create Authorization Classes
     */
    @Override
    public void createAuthorizationClasses() throws IOException {
        List<ParameterInfo> filterParams = getAuthenticatorMethodParameters();
        final String[] parameters = getGetParamNames(filterParams);
        final Object[] paramTypes = getGetParamTypes(filterParams);
        Util.createSessionKeyAuthorizationClassesForWeb(
            getBean(), getProject(),
            getBean().getSaasName(), getBean().getSaasServicePackageName(), 
            getSaasServiceFolder(), 
            loginJS, loginFile, 
            callbackJS, callbackFile,
            parameters, paramTypes
        );
    }
    
    
    @Override
    protected List<ParameterInfo> getAuthenticatorMethodParameters() {
        if(bean.getAuthenticationType() == SaasAuthenticationType.SESSION_KEY)
            return Util.getAuthenticatorMethodParametersForWeb();
        else
            return super.getAuthenticatorMethodParameters();
    }
    
    @Override
    protected List<ParameterInfo> getServiceMethodParameters() {
        if(bean.getAuthenticationType() == SaasAuthenticationType.SESSION_KEY)
            return Util.getServiceMethodParametersForWeb(getBean());
        else
            return super.getServiceMethodParameters();
    }
    
    @Override
    protected String getLoginBody(WadlSaasBean bean, 
            String groupName, String paramVariableName) throws IOException {
        if(getBean().getAuthenticationType() != SaasAuthenticationType.SESSION_KEY)
            return null;
        return Util.createSessionKeyLoginBodyForWeb(bean, groupName, paramVariableName);
    }
    
    @Override
    protected String getTokenBody(WadlSaasBean bean, 
            String groupName, String paramVariableName, String saasServicePkgName) throws IOException {
        if(getBean().getAuthenticationType() != SaasAuthenticationType.SESSION_KEY)
            return null;
        return Util.createSessionKeyTokenBodyForWeb(bean, groupName, paramVariableName,
                saasServicePkgName);
    }
    
    @Override
    protected String getCustomMethodBody() throws IOException {
        String paramUse = "";

        //Evaluate parameters (query(not fixed or apikey), header, template,...)
        List<ParameterInfo> filterParams = getServiceMethodParameters();//includes request, response also
        paramUse += Util.getHeaderOrParameterUsage(filterParams);
        filterParams = super.getServiceMethodParameters();
        
        String resultClass = getBean().getOutputWrapperName();
            String methodBody = "";
            methodBody += "        "+resultClass+" resultObj = null;\n";
            methodBody += "        try {\n";
            methodBody += "             "+REST_CONNECTION_PACKAGE+"."+REST_RESPONSE+" result = " + 
                    getBean().getSaasServiceName() + "." + 
                    getBean().getSaasServiceMethodName() + "(" + paramUse + ");\n";
        if(getBean().getHttpMethod() == HttpMethodType.GET) {
            if(getBean().canGenerateJAXBUnmarshaller()) {
                methodBody += "             "+resultClass+" resultObj = result.getDataAsJaxbObject("+resultClass+".class);\n";
            } else {
                methodBody += "             resultObj = new "+resultClass+"();\n";
                methodBody += "             resultObj.setString(result.getDataAsString());\n";
            }
        } else {
            methodBody += "                 System.out.println(\"The SaasService returned: \"+result);\n";
        }
        methodBody += "        } catch (Exception ex) {\n";
        methodBody += "             throw new WebApplicationException(ex);\n";
        methodBody += "        }\n";
        if(getBean().getHttpMethod() == HttpMethodType.GET)
            methodBody += "        return resultObj;\n";
            
        return methodBody;
    }
    
    @Override
    protected void addImportsToTargetFile() throws IOException {
    }
    
    @Override
    protected void addImportsToWrapperResource() throws IOException {
        List<String> imports = new ArrayList<String>();
        imports.add(getBean().getSaasServicePackageName()+"."+getBean().getSaasServiceName());
        imports.add(REST_CONNECTION_PACKAGE+"."+REST_RESPONSE);
        imports.add(InputStream.class.getName());
        Util.addImportsToSource(getWrapperResourceSource(), imports);
    }

    @Override
    protected String getSessionKeyLoginArguments() {
        return Util.getSessionKeyLoginArgumentsForWeb();
    }
}
