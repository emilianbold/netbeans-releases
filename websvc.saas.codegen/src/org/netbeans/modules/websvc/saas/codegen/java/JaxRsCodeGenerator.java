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

import com.sun.source.tree.ClassTree;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo.ParamStyle;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.ApiKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.java.model.WadlSaasBean;
import org.netbeans.modules.websvc.saas.codegen.java.support.AbstractTask;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.SourceGroupSupport;
import org.netbeans.modules.websvc.saas.codegen.java.support.Util;
import org.openide.filesystems.FileObject;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class JaxRsCodeGenerator extends SaasCodeGenerator {
    
    public static final String SAAS_SERVICE_TEMPLATE = "";
    
    private FileObject saasServiceFile = null;
    private JavaSource saasServiceJS = null;
    private String groupName;
    private Object saasAuthFile;
    private JavaSource saasAuthJS;
    
    public JaxRsCodeGenerator(JTextComponent targetComponent, 
            FileObject targetFile, WadlSaasMethod m) throws IOException {
        super(targetComponent, targetFile, new WadlSaasBean(m));
        saasServiceFile = SourceGroupSupport.findJavaSourceFile(getProject(), getSaasServiceName());
        if(saasServiceFile != null)
            saasServiceJS = JavaSource.forFileObject(saasServiceFile);
        this.groupName = Util.normailizeName(getBean().getMethod().getSaas().getTopLevelGroup().getName());
    }
    
    @Override
    public WadlSaasBean getBean() {
        return (WadlSaasBean )bean;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public String getSaasServiceName() {
        return getGroupName()+"Service";
    }
    
    public String getSaasServicePackageName() {
        return RESTCONNECTION_PACKAGE+"."+getGroupName().toLowerCase();
    }
    
    public String getSaasServiceMethodName() {
        return "get" + getBean().getName();
    }
    
    @Override
    protected void preGenerate() throws IOException {
        createRestConnectionFile(getProject());
    }
    
    protected String getCustomMethodBody() throws IOException {
        String converterName = getConverterName();
        String paramStr = null;
        StringBuffer sb1 = new StringBuffer();
        List<ParameterInfo> params = filterParameters();

        for (ParameterInfo param : params) {
            String paramName = param.getName();
            if (param.getType() != String.class) {
                sb1.append("{\"" + paramName + "\", \"" + paramName + "\"},");
            } else {
                sb1.append("{\"" + paramName + "\", " + paramName + "},");
            }
        }
        paramStr = sb1.toString();
        if (params.size() > 0) {
            paramStr = paramStr.substring(0, paramStr.length() - 1);
        }
        
        String methodBody = "String url = \"" + ((WadlSaasBean) bean).getUrl() + "\";\n";
        methodBody += "        " + converterName + " converter = new " + converterName + "();\n";
        methodBody += "        try {\n";
        methodBody += "             String[][] params = new String[][]{\n";
        methodBody += "                 " + paramStr + "\n";
        methodBody += "             };\n";
        methodBody += "             RestConnection cl = new RestConnection(url, params);\n";
        methodBody += "             String result = cl.get();\n";
        methodBody += "             converter.setString(result);\n";
        methodBody += "             return converter;\n";
        methodBody += "        } catch (java.io.IOException ex) {\n";
        methodBody += "             throw new WebApplicationException(ex);\n";
        methodBody += "        }\n }";
       
        return methodBody;
    }
    
    protected String getServiceMethodBody() throws IOException {
        String fixedCode = "";
        for (ParameterInfo param : getBean().getInputParameters()) {
            if(param.getStyle() == ParamStyle.QUERY_FIXED)
                fixedCode +=  "String " + param.getName() + " = \"" + findParamValue(param) + "\";\n";
        }
        String headerParamsCode = "";
        if(getBean().getHeaderParameters() != null && getBean().getHeaderParameters().size() > 0)
            headerParamsCode = getHeaderOrParameterDefinition(getBean().getHeaderParameters(), HEADER_PARAMS, true);
        
        String pathParamsCode = "";
        if(getBean().getTemplateParameters() != null && getBean().getTemplateParameters().size() > 0)
            pathParamsCode = getTemplateParameterDefinition(getBean().getTemplateParameters(), PATH_PARAMS, false);
        
        String queryParamsCode = "";
        List<ParameterInfo> filterParams = new ArrayList<ParameterInfo>();
        if(getBean().getInputParameters() != null) {
            for(ParameterInfo param: getBean().getInputParameters()) {
                if(param.getStyle() == ParamStyle.TEMPLATE)
                    continue;
                filterParams.add(param);
            }
            if(filterParams.size() > 0)
                queryParamsCode = getHeaderOrParameterDefinition(filterParams, QUERY_PARAMS, false);
        }

        String methodBody = "";
        methodBody += "        String result = null;\n";
        methodBody += "        try {\n";
        methodBody += "             " + getPreAuthenticationCode()+"\n";
        methodBody += "        "+fixedCode;
        methodBody += "        "+headerParamsCode;
        methodBody += "        "+pathParamsCode;
        methodBody += "        "+queryParamsCode;
        methodBody += "             RestConnection conn = new RestConnection(\""+getBean().getUrl();
        if(!pathParamsCode.trim().equals(""))
            methodBody += "\", "+PATH_PARAMS+", "+(queryParamsCode.trim().equals("")?"null":QUERY_PARAMS);
        else if(!queryParamsCode.trim().equals(""))
            methodBody += "\", "+QUERY_PARAMS;
        methodBody += ");\n";
        methodBody += "             " + getPostAuthenticationCode()+"\n";
        HttpMethodType httpMethod = HttpMethodType.valueOf(getBean().getMethod().getWadlMethod().getName());
        if(httpMethod == HttpMethodType.GET) {
            methodBody += "             result = conn.get("+(headerParamsCode.trim().equals("")?"null":HEADER_PARAMS)+");\n";
        } else if(httpMethod == HttpMethodType.PUT) {
            methodBody += "             String content = \"Some content.\"";
            methodBody += "             result = conn.put("+(headerParamsCode.trim().equals("")?"null":HEADER_PARAMS)+", content.getBytes());\n";
        } else if(httpMethod == HttpMethodType.POST) {
            methodBody += "             String content = \"Some content.\"";
            methodBody += "             result = conn.post("+(headerParamsCode.trim().equals("")?"null":HEADER_PARAMS)+", content.getBytes());\n";
        } else if(httpMethod == HttpMethodType.DELETE) {
            methodBody += "             result = conn.delete("+(headerParamsCode.trim().equals("")?"null":HEADER_PARAMS)+");\n";
        }
        methodBody += "        } catch (java.io.IOException ex) {\n";
        methodBody += "             Logger.getLogger(" + getSaasServiceName() + ".class.getName()).log(Level.SEVERE, null, ex);\n";
        methodBody += "        }\n";
        methodBody += "        return result;\n";
       
        return methodBody;
    }
    
    private String getPreAuthenticationCode() {
        boolean isApiKey = getBean().getAuthenticationType() == SaasAuthenticationType.API_KEY;
        if(isApiKey)
            return "String apiKey = "+getGroupName()+"Authenticator.getApiKey();";
        return "";
    }
    
    private String getPostAuthenticationCode() {
        if(getBean().getAuthenticationType() == SaasAuthenticationType.HTTP_BASIC)
            return "conn.setAuthenticator(new "+getGroupName()+"Authenticator());";
        return "";
    }
    
    protected void addImportsToTargetFile() throws IOException {
        ModificationResult result = getTargetSource().runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                JavaSourceHelper.addImports(copy, new String[] {getSaasServicePackageName()+"."+getSaasServiceName()});
            }
        });
        result.commit();
    }
    
    protected void addImportsToSaasService() throws IOException {
        ModificationResult result = saasServiceJS.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                JavaSourceHelper.addImports(copy, new String[] {RESTCONNECTION_PACKAGE+"."+REST_CONNECTION});
            }
        });
        result.commit();
    }

    /**
     *  Insert the Saas client call
     */
    public void insertSaasServiceAccessCode(boolean isInBlock) throws IOException {
        try {
            String code = "";
            if(isInBlock) {
                code = getCustomMethodBody();
            } else {
                code = "\nprivate String call"+getBean().getName()+"Service() {\n";
                code += getCustomMethodBody()+"\n";
                code += "return result;\n";
                code += "}\n";
            }
            insert(code, getTargetComponent(), true);
        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
    }
    
    /**
     *  Create Authenticator
     */
    public void createAuthenticatorClass() throws IOException {
        if(saasAuthFile == null) {
            SourceGroup[] srcGrps = SourceGroupSupport.getJavaSourceGroups(getProject());
            String pkg = getSaasServicePackageName();
            FileObject targetFolder = SourceGroupSupport.getFolderForPackage(srcGrps[0], pkg, true);
            String authFileName = getGroupName()+"Authenticator";
            String authTemplate = null;
            if(getBean().getAuthenticationType() == SaasAuthenticationType.API_KEY)
                authTemplate = TEMPLATES_SAAS+"ApiKeyAuthenticator.java";
            if(getBean().getAuthenticationType() == SaasAuthenticationType.HTTP_BASIC)
                authTemplate = TEMPLATES_SAAS+"HttpBasicAuthenticator.java";
            if(authTemplate != null) {
                saasAuthJS = JavaSourceHelper.createJavaSource(authTemplate,targetFolder, pkg, authFileName);
                Set<FileObject> files = new HashSet<FileObject>(saasAuthJS.getFileObjects());
                if (files != null && files.size() > 0) {
                    saasAuthFile = files.iterator().next();
                }
            }
            //Also copy profile.properties
            try {
                Util.createDataObjectFromTemplate("SaaSServices/"+getGroupName()+"/profile.properties", targetFolder, null);
            } catch(Exception ex) {} //ignore
        }
    }
    
    /**
     *  Create Saas Service
     */
    public void createSaasServiceClass() throws IOException {
        if(saasServiceFile == null) {
            SourceGroup[] srcGrps = SourceGroupSupport.getJavaSourceGroups(getProject());
            String pkg = getSaasServicePackageName();
            FileObject targetFolder = SourceGroupSupport.getFolderForPackage(srcGrps[0], pkg, true);
            saasServiceJS = JavaSourceHelper.createJavaSource(getBean().getSaasServiceTemplate(),targetFolder, pkg, getSaasServiceName());
            Set<FileObject> files = new HashSet<FileObject>(saasServiceJS.getFileObjects());
            if (files != null && files.size() > 0) {
                saasServiceFile = files.iterator().next();
            }
        }
    }
    
    /**
     *  Return target and generated file objects
     */
    protected void addSaasServiceMethod() throws IOException {
        ModificationResult result = saasServiceJS.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);

                Modifier[] modifiers = Constants.PUBLIC_STATIC;

                String type = String.class.getName();
                String bodyText = "{ \n" + getServiceMethodBody() + "\n }";

                List<ParameterInfo> filterParams = filterParameters();
                String[] parameters = getGetParamNames(filterParams);
                Object[] paramTypes = getGetParamTypes(filterParams);

                String comment = "Retrieves representation of an instance of " + getBean().getQualifiedClassName() + "\n";
                for (String param : parameters) {
                    comment += "@param $PARAM$ resource URI parameter\n".replace("$PARAM$", param);
                }
                comment += "@return an instance of "+type;
                ClassTree initial = JavaSourceHelper.getTopLevelClassTree(copy);
                ClassTree tree = JavaSourceHelper.addMethod(copy, initial,
                        modifiers, null, null,
                        getSaasServiceMethodName(), type, parameters, paramTypes,
                        null, null,
                        bodyText, comment);      //NOI18N
                copy.rewrite(initial, tree);
            }
            
        });
        result.commit();
    }
    
    public List<ParameterInfo> filterParameters() {
        List<ParameterInfo> filterParams = new ArrayList<ParameterInfo>();
        if(getBean().getInputParameters() != null) {
            for (ParameterInfo param : getBean().getInputParameters()) {
                ParamStyle style = param.getStyle();
                if(style == ParamStyle.QUERY_APIKEY || style == ParamStyle.QUERY_FIXED) {
                        continue;
                }
                filterParams.add(param);
            }
        }
        return filterParams;
    }
    
    public String[] getUriParamTypes() {
        String defaultType = String.class.getName();
        String[] types = new String[getBean().getUriParams().length];
        for (int i=0; i < types.length; i++) {
            types[i] = defaultType;
        }
        return types;
    }
    
    private String[] getGetParamNames(List<ParameterInfo> queryParams) {
        ArrayList<String> params = new ArrayList<String>();
        params.addAll(Arrays.asList(getBean().getUriParams()));
        params.addAll(Arrays.asList(getParamNames(queryParams)));
        return params.toArray(new String[params.size()]);
    }
    
    private String[] getGetParamTypes(List<ParameterInfo> queryParams) {
        ArrayList<String> types = new ArrayList<String>();
        types.addAll(Arrays.asList(getUriParamTypes()));
        types.addAll(Arrays.asList(getParamTypeNames(queryParams)));
        return types.toArray(new String[types.size()]);
    }
    
    
      private String[] getParamNames(List<ParameterInfo> params) {
        List<String> results = new ArrayList<String>();
        
        for (ParameterInfo param : params) {
            results.add(param.getName());
        }
        
        return results.toArray(new String[results.size()]);
    }
    
    private String[] getParamTypeNames(List<ParameterInfo> params) {
        List<String> results = new ArrayList<String>();
        
        for (ParameterInfo param : params) {
            results.add(param.getTypeName());
        }
        
        return results.toArray(new String[results.size()]);
    }
    
    protected String getParameterName(ParameterInfo param) {
        String name = param.getName();
        if(param.getStyle() == ParamStyle.TEMPLATE 
                && name.startsWith("{") && name.endsWith("}"))
            name = name.substring(0, name.length()-1);
        return name;
    }
    
    protected String getQueryParameterDeclaration(List<ParameterInfo> params) {
        String paramDecl = "";
        for (ParameterInfo param : params) {
            String name = getParameterName(param);
            String paramVal = findParamValue(param);
            if (param.getType() != String.class) {
                paramDecl +=  "        "+param.getType().getName()+" " + name + " = " + paramVal + ";\n";
            } else {
                if(paramVal != null)
                    paramDecl +=  "        String " + name + " = \"" + paramVal + "\";\n";
                else
                    paramDecl +=  "        String " + name + " = null;\n";
            }
        }
        return paramDecl;
    }
    
    protected String getQueryParameterUsage(List<ParameterInfo> params) {
        String paramUsage = "";
        for (ParameterInfo param : params) {
            String name = getParameterName(param);
            paramUsage +=  name + ", ";
        }
        return paramUsage;
    }
    
    private String getHeaderOrParameterDefinition(List<ParameterInfo> params, String varName, boolean evaluate) {
        String paramsStr = null;
        StringBuffer sb = new StringBuffer();
        for (ParameterInfo param : params) {
            String paramName = getParameterName(param);
            String paramVal = null;
            if(evaluate || param.getStyle() == ParamStyle.QUERY_APIKEY) {
                paramVal = findParamValue(param);
                if (param.getType() != String.class) {
                    sb.append("{\"" + paramName + "\", \"" + paramVal + "\".toString()},\n");
                } else {
                    if(paramVal != null)
                        sb.append("{\"" + paramName + "\", \"" + paramVal + "\"},\n");
                    else
                        sb.append("{\"" + paramName + "\", null},\n");
                }
            } else {
                sb.append("{\"" + paramName + "\", " + paramName + "},\n");
            }
        }
        paramsStr = sb.toString();
        if (params.size() > 0) {
            paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
        }
        
        String paramCode = "";
        paramCode += "             String[][] "+varName+" = new String[][]{\n";
        paramCode += "                 " + paramsStr + "\n";
        paramCode += "             };\n";
        return paramCode;
    }
    
    //String pathParams[] = new String[][]  { {"{volumeId}", volumeId},  {"{objectId}", objectId}}; 
    private String getTemplateParameterDefinition(List<ParameterInfo> params, String varName, boolean evaluate) {
        String paramsStr = null;
        StringBuffer sb = new StringBuffer();
        for (ParameterInfo param : params) {
            String paramName = param.getName();
            String paramVal = null;
            if(evaluate) {
                paramVal = findParamValue(param);
                if (param.getType() != String.class) {
                    sb.append("{\"" + paramName + "\", \"" + paramVal + "\".toString()},\n");
                } else {
                    if(paramVal != null)
                        sb.append("{\"{" + paramName + "}\", \"" + paramVal + "\"},\n");
                    else
                        sb.append("{\"{" + paramName + "}\", null},\n");
                }
            } else {
                sb.append("{\"{" + paramName + "}\", " + paramName + "},\n");
            }
        }
        paramsStr = sb.toString();
        if (params.size() > 0) {
            paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
        }
        
        String paramCode = "";
        paramCode += "             String[][] "+varName+" = new String[][]{\n";
        paramCode += "                 " + paramsStr + "\n";
        paramCode += "             };\n";
        return paramCode;
    }
    
    private String findParamValue(ParameterInfo param) {
        String paramVal = null;
        if(param.getStyle() == ParamStyle.QUERY_APIKEY) {
            paramVal = "\"+apiKey+\"";
        } else if(param.getStyle() == ParamStyle.TEMPLATE) {
            if(param.getDefaultValue() != null)
                paramVal = param.getDefaultValue().toString();
            else
                paramVal = "";
        } else {
            if(param.getStyle() == ParamStyle.QUERY_FIXED)
                paramVal = param.getFixed();
            else {
                if(param.isRequired())
                    paramVal = "";
                if(param.getDefaultValue() != null)
                    paramVal = param.getDefaultValue().toString();
            }
        }
        return paramVal;
    }

    public static final String HEADER_PARAMS = "headerParams"; // NOI18n
    public static final String QUERY_PARAMS = "queryParams"; // NOI18n
    public static final String PATH_PARAMS = "pathParams"; // NOI18n

}
