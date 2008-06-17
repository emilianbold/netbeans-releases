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

import org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator;
import org.netbeans.modules.websvc.saas.model.CustomSaasMethod;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.Constants.DropFileType;
import org.netbeans.modules.websvc.saas.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.model.CustomClientSaasBean;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamFilter;
import org.netbeans.modules.websvc.saas.codegen.java.support.SourceGroupSupport;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author Ayub Khan
 */
public class CustomClientRestResourceCodeGenerator extends SaasClientCodeGenerator {
    
    private DropFileType dropFileType;
    private FileObject serviceFolder;
    private SaasClientJavaAuthenticationGenerator authGen;

    public CustomClientRestResourceCodeGenerator() {
        setDropFileType(Constants.DropFileType.RESOURCE);
    }

    @Override
    public void init(SaasMethod m, Document doc) throws IOException {
        super.init(m, doc);
        setBean(new CustomClientSaasBean((CustomSaasMethod) m));
        this.authGen = new SaasClientJavaAuthenticationGenerator(getBean(), getProject());
        this.authGen.setLoginArguments(getLoginArguments());
        this.authGen.setAuthenticatorMethodParameters(getAuthenticatorMethodParameters());
        this.authGen.setSaasServiceFolder(getSaasServiceFolder());

    }

    @Override
    public CustomClientSaasBean getBean() {
        return (CustomClientSaasBean) super.getBean();
    }

    public SaasClientJavaAuthenticationGenerator getAuthenticationGenerator() {
        return authGen;
    }
    
    public DropFileType getDropFileType() {
        return dropFileType;
    }

    void setDropFileType(DropFileType dropFileType) {
        this.dropFileType = dropFileType;
    }

    public FileObject getSaasServiceFolder() throws IOException {
        if (serviceFolder == null) {
            SourceGroup[] srcGrps = SourceGroupSupport.getJavaSourceGroups(getProject());
            serviceFolder = SourceGroupSupport.getFolderForPackage(srcGrps[0],
                    getBean().getSaasServicePackageName(), true);
        }
        return serviceFolder;
    }
    
    public boolean canAccept(SaasMethod method, Document doc) {
        if (method instanceof CustomSaasMethod && 
                Util.isRestJavaFile(NbEditorUtilities.getDataObject(doc))) {
            return true;
        }
        return false;
    }
    
    @Override
    protected void preGenerate() throws IOException {
        createRestConnectionFile(getProject());
        
        //add JAXB Classes, etc, if available
        if(getBean().getMethod().getSaas().getLibraryJars().size() > 0)
            Util.addClientJars(getBean(), getProject(), null);
        
        getTargetFolder().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                try {
                    /*
                      Already taken care by Util.addClientJars()
                     List<String> libs = getBean().getArtifactLibs(); 
                    for(String lib: libs) {
                        //TODO - Fix the copyFile method
                        copyFile(lib, FileUtil.toFile(getTargetFolder()));
                    }*/
                    Map<String, String> templates = getBean().getArtifactTemplates();
                    for(Map.Entry e: templates.entrySet()) {
                        String id = (String) e.getKey();
                        String template = (String) e.getValue();
                        Util.createDataObjectFromTemplate(template, getSaasServiceFolder(), id);
                    }
                } finally {
                }
            }
        });
    }

    protected String getCustomMethodBody() throws IOException {
        String paramStr = "";       //NOI18N
    
        int count = 0;
        for (ParameterInfo param : getBean().getInputParameters()) {
            if (count++ > 0) {
                paramStr += ", ";       //NOI18N
            }
            
            paramStr += getParameterName(param, true, true);
        }
        
        return "return execute(" + paramStr + ")";
    }
    
    protected List<ParameterInfo> getServiceMethodParameters() {
        List<ParameterInfo> params = getBean().filterParametersByAuth(getBean().filterParameters(
                new ParamFilter[]{ParamFilter.FIXED}));
        HttpMethodType httpMethod = getBean().getHttpMethod();
        
        if (httpMethod == HttpMethodType.PUT || httpMethod == HttpMethodType.POST) {
            
            ParameterInfo contentTypeParam = Util.findParameter(getBean().getInputParameters(), Constants.CONTENT_TYPE);
            Class contentType = InputStream.class;
            
            if (contentTypeParam == null) {
                params.add(new ParameterInfo(Constants.CONTENT_TYPE, String.class));
            } else {
                if (!contentTypeParam.isFixed() && !params.contains(contentTypeParam)) {
                    params.add(contentTypeParam);
                } else {
                    String value = Util.findParamValue(contentTypeParam);
                    if (value.equals("text/plain") || value.equals("application/xml") ||
                            value.equals("text/xml")) {     //NOI18N
                        contentType = String.class;
                    }
                }
            }
            params.add(new ParameterInfo(Constants.PUT_POST_CONTENT, contentType));
        }
        return params;
    }
    

    protected String getHeaderOrParameterDeclaration(List<ParameterInfo> params,
            String indent) {
        if (indent == null) {
            indent = " ";
        }
        String paramDecl = "";
        for (ParameterInfo param : params) {
            String name = getVariableName(param.getName());
            String paramVal = Util.findParamValue(param);
            if (param.getType() != String.class) {
                paramDecl += indent + param.getType().getName() + " " + name + " = " + paramVal + ";\n";
            } else {
                if (paramVal != null) {
                    paramDecl += indent + "String " + name + " = \"" + paramVal + "\";\n";
                } else {
                    paramDecl += indent + "String " + name + " = null;\n";
                }
            }
        }
        return paramDecl;
    }

    protected String getHeaderOrParameterDeclaration(List<ParameterInfo> params) {
        String indent = "                 ";
        return getHeaderOrParameterDeclaration(params, indent);
    }
    
    public static List<ParameterInfo> getAuthenticatorMethodParametersForWeb() {
        List<ParameterInfo> params = new ArrayList<ParameterInfo>();
        params.add(new ParameterInfo(Constants.HTTP_SERVLET_REQUEST_VARIABLE, Object.class,
                Constants.HTTP_SERVLET_REQUEST_CLASS));
        params.add(new ParameterInfo(Constants.HTTP_SERVLET_RESPONSE_VARIABLE, Object.class,
                Constants.HTTP_SERVLET_RESPONSE_CLASS));
        return params;
    }

    public static List<ParameterInfo> getServiceMethodParametersForWeb(CustomClientSaasBean bean) {
        List<ParameterInfo> params = new ArrayList<ParameterInfo>();
        params.addAll(getAuthenticatorMethodParametersForWeb());
        params.addAll(bean.filterParametersByAuth(bean.filterParameters(
                new ParamFilter[]{ParamFilter.FIXED})));
        return params;
    }
    
    protected List<ParameterInfo> getAuthenticatorMethodParameters() {
        return Collections.emptyList();
    }

    protected String getLoginArguments() {
        return "";
    }

}
