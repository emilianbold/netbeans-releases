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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo.ParamStyle;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.ApiKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SignedUrlAuthentication;
import org.netbeans.modules.websvc.saas.codegen.java.model.WadlSaasBean;
import org.netbeans.modules.websvc.saas.codegen.java.support.AbstractTask;
import org.netbeans.modules.websvc.saas.codegen.java.support.Inflector;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.SourceGroupSupport;
import org.netbeans.modules.websvc.saas.codegen.java.support.Util;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

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
    private HttpMethodType httpMethod;
    private HashMap<String, ParameterInfo> filterParamMap;
    
    public JaxRsCodeGenerator(JTextComponent targetComponent, 
            FileObject targetFile, WadlSaasMethod m) throws IOException {
        super(targetComponent, targetFile, new WadlSaasBean(m));
        saasServiceFile = SourceGroupSupport.findJavaSourceFile(getProject(), getSaasServiceName());
        if(saasServiceFile != null)
            saasServiceJS = JavaSource.forFileObject(saasServiceFile);
        SaasGroup g = getBean().getMethod().getSaas().getParentGroup();
        if(g.getParent() == null) //g is root group, so use topLevel group usually the vendor group
            g = getBean().getMethod().getSaas().getTopLevelGroup();
        this.groupName = Util.normailizeName(g.getName());
        this.httpMethod = HttpMethodType.valueOf(getBean().getMethod().getWadlMethod().getName());
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
    
    public HttpMethodType getHttpMethodName() {
        return this.httpMethod;
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
            String paramName = getParameterName(param);
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
            if(param.isFixed())
                fixedCode +=  "String " + getParameterName(param) + " = \"" + findParamValue(param) + "\";\n";
        }
        String pathParamsCode = "";
        if(getBean().getTemplateParameters() != null && getBean().getTemplateParameters().size() > 0)
            pathParamsCode = getTemplateParameterDefinition(getBean().getTemplateParameters(), PATH_PARAMS, false);
        
        String queryParamsCode = "";
        if(getBean().getQueryParameters() != null && 
                getBean().getQueryParameters().size() > 0) {
            queryParamsCode = getHeaderOrParameterDefinition(getBean().getQueryParameters(), QUERY_PARAMS, false);
        }

        String methodBody = "";
        methodBody += "        String result = null;\n";
        
        //Insert authentication code before new RestConnection() call
        methodBody += "             " + getPreAuthenticationCode()+"\n";
        
        //Insert parameter declaration
        methodBody += "        "+fixedCode;
        methodBody += "        "+pathParamsCode;
        methodBody += "        "+queryParamsCode;
        
        methodBody += "             RestConnection conn = new RestConnection(\""+getBean().getUrl()+"\"";
        if(!pathParamsCode.trim().equals(""))
            methodBody += ", "+PATH_PARAMS+", "+(queryParamsCode.trim().equals("")?"null":QUERY_PARAMS);
        else if(!queryParamsCode.trim().equals(""))
            methodBody += ", "+QUERY_PARAMS;
        methodBody += ");\n";
        
        //Insert authentication code after new RestConnection() call
        methodBody += "             " + getPostAuthenticationCode()+"\n";
      
        String headerUsage = "null";
        if(getBean().getHeaderParameters() != null && getBean().getHeaderParameters().size() > 0) {
            headerUsage = HEADER_PARAMS;
            methodBody += "        "+getHeaderOrParameterDefinition(getBean().getHeaderParameters(), HEADER_PARAMS, false);;
        }
        
        //Insert the method call
        HttpMethodType httpMethod = getHttpMethodName();
        if(httpMethod == HttpMethodType.GET) {
            methodBody += "             result = conn.get("+headerUsage+");\n";
        } else if(httpMethod == HttpMethodType.PUT) {
            methodBody += "             String content = \"Some content.\"";
            methodBody += "             result = conn.put("+headerUsage+", content.getBytes());\n";
        } else if(httpMethod == HttpMethodType.POST) {
            methodBody += "             String content = \"Some content.\"";
            methodBody += "             result = conn.post("+headerUsage+", content.getBytes());\n";
        } else if(httpMethod == HttpMethodType.DELETE) {
            methodBody += "             result = conn.delete("+headerUsage+");\n";
        }
        
        methodBody += "        return result;\n";
       
        return methodBody;
    }
    
    /* 
     * Insert this code before new RestConnection()
     */
    private String getPreAuthenticationCode() {
        String methodBody = "";
        SaasAuthenticationType authType = getBean().getAuthenticationType();
        if(authType == SaasAuthenticationType.API_KEY) {
            methodBody += "        String apiKey = "+getGroupName()+"Authenticator.getApiKey();";
        } else if(authType == SaasAuthenticationType.SESSION_KEY) {
            methodBody += "        "+getGroupName()+"Authenticator.login();\n";
            methodBody += "        String apiKey = "+getGroupName()+"Authenticator.getApiKey();\n";
            methodBody += "        String sessionKey = "+getGroupName()+"Authenticator.getSessionKey();\n";
            methodBody += "        String method = \"facebook.friends.get\";\n";
            methodBody += "        String v = \"1.0\";\n";
            methodBody += "        String callId = String.valueOf(System.currentTimeMillis());\n";
            methodBody += "        String sig = "+getGroupName()+"Authenticator.sign(\n";
            methodBody += "                new String[][]{\n";
            methodBody += "                    {\"method\", method},\n";
            methodBody += "                    {\"v\", v},\n";
            methodBody += "                    {\"api_key\", apiKey},\n";
            methodBody += "                    {\"session_key\", sessionKey},\n";
            methodBody += "                    {\"call_id\", callId}\n";
            methodBody += "                });\n\n";

            methodBody += "        String[][] params = new String[][]{\n";
            methodBody += "                {\"method\", method},\n";
            methodBody += "                {\"v\", v},\n";
            methodBody += "                {\"api_key\", apiKey},\n";
            methodBody += "                {\"session_key\", sessionKey},\n";
            methodBody += "                {\"sig\", sig},\n";
            methodBody += "                {\"call_id\", callId}\n";
            methodBody += "        };\n";
        }
        return methodBody;
    }
    
    /* 
     * Insert this code after new RestConnection()
     */
    private String getPostAuthenticationCode() {
        String methodBody = "";
        SaasAuthenticationType authType = getBean().getAuthenticationType();
        if(authType == SaasAuthenticationType.HTTP_BASIC) {
            methodBody += "        conn.setAuthenticator(new "+getGroupName()+"Authenticator());\n";
        } else if(authType == SaasAuthenticationType.SIGNED_URL) {
            SignedUrlAuthentication signedUrl = (SignedUrlAuthentication)getBean().getAuthentication();
            String paramStr = "";
            List<ParameterInfo> signParams = signedUrl.getParameters();
            if(signParams != null && signParams.size() > 0) {
                for(ParameterInfo p:signParams) {
                    if(isContains(p, filterParameters()))
                        continue;
                    paramStr += "        String "+
                            Inflector.getInstance().camelize(Util.normailizeName(p.getName()), true)+" = ";
                    if(p.getFixed() != null) {
                        paramStr += "\""+p.getFixed()+"\";\n";
                    } else if(p.getType() == Date.class) {
                        paramStr += "conn.getDate();\n";
                    } else if(p.isRequired()) {
                        if(p.getDefaultValue() != null)
                            paramStr += "\""+p.getDefaultValue()+"\";\n";
                        else
                            paramStr += "\"\";\n";
                    } else {
                        if(p.getDefaultValue() != null)
                            paramStr += "\""+p.getDefaultValue()+"\";\n";
                        else
                            paramStr += "null;\n";
                    }
                }
                paramStr += "\n";
                
                paramStr += "        authorization = "+getGroupName()+"Authenticator.sign(\n";
                paramStr += "                new String[][] {\n";
                for(ParameterInfo p:signParams) {
                    paramStr += "                    {\""+p.getName()+"\", "+
                            Inflector.getInstance().camelize(Util.normailizeName(p.getName()), true)+"},\n";
                }
                paramStr += "        });\n";
            }
            methodBody += paramStr;
        }
        return methodBody;
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
            else if(getBean().getAuthenticationType() == SaasAuthenticationType.HTTP_BASIC)
                authTemplate = TEMPLATES_SAAS+"HttpBasicAuthenticator.java";
            else if(getBean().getAuthenticationType() == SaasAuthenticationType.SIGNED_URL)
                authTemplate = TEMPLATES_SAAS+"SignedUrlAuthenticator.java";
            else if(getBean().getAuthenticationType() == SaasAuthenticationType.SESSION_KEY)
                authTemplate = TEMPLATES_SAAS+"SessionKeyAuthenticator.java";
            if(authTemplate != null) {
                saasAuthJS = JavaSourceHelper.createJavaSource(authTemplate,targetFolder, pkg, authFileName);
                Set<FileObject> files = new HashSet<FileObject>(saasAuthJS.getFileObjects());
                if (files != null && files.size() > 0) {
                    saasAuthFile = files.iterator().next();
                }
            }
            //Also copy profile.properties
            DataObject prof = null;
            String authProfile = getBean().getAuthenticationProfile();
            if (authProfile != null && !authProfile.trim().equals("")) {
                try {
                    prof = Util.createDataObjectFromTemplate(authProfile, targetFolder, null);
                } catch (Exception ex) {
                    throw new IOException("Profile file specified in saas-services/service-metadata/authentication/@profile, not found: "+authProfile);
                } 
            } else {
                try {
                    prof = Util.createDataObjectFromTemplate("SaaSServices/" + getGroupName() + "/profile.properties", targetFolder, null);
                } catch (Exception ex1) {
                    try {
                        prof = Util.createDataObjectFromTemplate(TEMPLATES_SAAS+getBean().getAuthenticationType().value()+".properties", targetFolder, null);
                    } catch (Exception ex2) {//ignore
                    }
                } 
            }
            if(prof != null) {
                EditorCookie ec = (EditorCookie) prof.getCookie(EditorCookie.class);
                StyledDocument doc = ec.openDocument();
                String profileText = null;
                if(getBean().getAuthenticationType() == SaasAuthenticationType.API_KEY) {
                    ParameterInfo p = findParameter(((ApiKeyAuthentication)getBean().getAuthentication()).getApiKeyName());
                    if(p != null && p.getDefaultValue() != null)
                        profileText = "api_key="+p.getDefaultValue()+"\n";
                }
                if(profileText != null) {
                    try {
                        doc.insertString(doc.getLength(), profileText, null);
                    } catch (BadLocationException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Replacing property values failed. Try to change it manually.", ex);
                    }
                }
            }
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
                        null, null, new String[]{"java.io.IOException"},
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
                if(param.isApiKey() || param.isFixed()) {
                        continue;
                }
                filterParams.add(param);
            }
        }
        return filterParams;
    }
    
    public ParameterInfo findParameter(String name) {
        List<ParameterInfo> params = getBean().getInputParameters();
        if(params != null) {
            for (ParameterInfo param : params) {
                if(param.getName().equals(name)) {
                    return param;
                }
            }
        }
        return null;
    }
    
    public String[] getUriParamTypes() {
        String defaultType = String.class.getName();
        String[] types = new String[getBean().getUriParams().length];
        for (int i=0; i < types.length; i++) {
            types[i] = defaultType;
        }
        return types;
    }
    
    protected String getHeaderOrParameterDeclaration(List<ParameterInfo> params) {
        String paramDecl = "";
        for (ParameterInfo param : params) {
            String name = getParameterName(param, true, true, true);
            String paramVal = findParamValue(param);
            if (param.getType() != String.class) {
                paramDecl +=  "        "+param.getType().getName()+" " + name + " = " + paramVal + ";\n";
            } else {
                if(paramVal != null)
                    paramDecl +=  "             String " + name + " = \"" + paramVal + "\";\n";
                else
                    paramDecl +=  "             String " + name + " = null;\n";
            }
        }
        return paramDecl;
    }
    
    protected String getHeaderOrParameterUsage(List<ParameterInfo> params) {
        String paramUsage = "";
        for (ParameterInfo param : params) {
            String name = getParameterName(param, true, true, true);
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
            if(evaluate || param.isApiKey()) {
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
                sb.append("{\"" + paramName + "\", " + getParameterName(param, true, true, true) + "},\n");
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
            String paramName = getParameterName(param);
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
        if(param.isApiKey()) {
            paramVal = "\"+apiKey+\"";
        } else if(param.getStyle() == ParamStyle.TEMPLATE) {
            if(param.getDefaultValue() != null)
                paramVal = param.getDefaultValue().toString();
            else
                paramVal = "";
        } else if(param.getStyle() == ParamStyle.HEADER) {
            if(param.getDefaultValue() != null)
                paramVal = param.getDefaultValue().toString();
            else
                paramVal = getParameterName(param).toLowerCase();
        } else {
            if(param.isFixed())
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

    private boolean isContains(ParameterInfo pInfo, List<ParameterInfo> params) {
        if(filterParamMap == null) {
            filterParamMap = new HashMap<String, ParameterInfo>();
            for(ParameterInfo p:params) {
                filterParamMap.put(getParameterName(p, true, true, true), p);
            }
        }
        return filterParamMap.containsKey(getParameterName(pInfo, true, true, true));
            
    }
    
    public static final String HEADER_PARAMS = "headerParams"; // NOI18n
    public static final String QUERY_PARAMS = "queryParams"; // NOI18n
    public static final String PATH_PARAMS = "pathParams"; // NOI18n

}
