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
package org.netbeans.modules.websvc.saas.codegen.php;

import org.netbeans.modules.websvc.saas.codegen.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.HttpBasicAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SessionKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseGenerator;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseGenerator.Login;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseGenerator.Method;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseGenerator.Token;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseGenerator.Token.Prompt;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseTemplates;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseTemplates.Template;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SignedUrlAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.Time;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class SaasClientPhpAuthenticationGenerator extends SaasClientAuthenticationGenerator {
    
    public static final String INDENT = "             ";
    
    private FileObject saasAuthFile;
    private FileObject loginFile;
    private FileObject callbackFile;
    
    public SaasClientPhpAuthenticationGenerator(SaasBean bean,
            Project project) {
        super(bean, project);
    }

    /* 
     * Insert this code before new "+Constants.REST_CONNECTION+"()
     */
    public String getPreAuthenticationCode() {
        String methodBody = "";
        SaasAuthenticationType authType = getBean().getAuthenticationType();
        if (authType == SaasAuthenticationType.API_KEY) {
            methodBody += INDENT+"$apiKey = " + getBean().getAuthenticatorClassName() + "::getApiKey();";
        } else if (authType == SaasAuthenticationType.SESSION_KEY) {
            SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) getBean().getAuthentication();
            methodBody += INDENT + getBean().getAuthenticatorClassName() + "::login(" + getLoginArguments() + ");\n";
            List<ParameterInfo> signParams = sessionKey.getParameters();
            String paramStr = "";

            if (signParams != null && signParams.size() > 0) {
                paramStr = getSignParamDeclaration(getBean(), signParams, Collections.<ParameterInfo>emptyList());
            }

            String sigName = sessionKey.getSigKeyName();
            paramStr += INDENT+"$sign_params = array();\n";
            for (ParameterInfo p : getBean().getInputParameters()) {
                if (p.getName().equals(sigName)) continue;
                
                paramStr += INDENT+"$sign_params[\"" + p.getName() + "\"] = $" +
                        Util.getVariableName(p.getName()) + ";\n";
            }
            paramStr += INDENT+"$" + Util.getVariableName(sigName) + " = " +
                    getBean().getAuthenticatorClassName() + "::sign($sign_params);\n";//sig
            methodBody += paramStr;

        } else if (authType == SaasAuthenticationType.HTTP_BASIC) {
            methodBody += INDENT + getBean().getAuthenticatorClassName() + "::login(" + getLoginArguments() + ");\n";
        }
        return methodBody;
    }

    /* 
     * Insert this code after new "+Constants.REST_CONNECTION+"()
     */
    public String getPostAuthenticationCode() {
        String methodBody = "";
        SaasAuthenticationType authType = getBean().getAuthenticationType();
        if (authType == SaasAuthenticationType.SIGNED_URL) {
            SignedUrlAuthentication signedUrl = (SignedUrlAuthentication) getBean().getAuthentication();
            List<ParameterInfo> signParams = signedUrl.getParameters();
            if (signParams != null && signParams.size() > 0) {
                String paramStr = getSignParamDeclaration(getBean(), signParams, getBean().getInputParameters());
                paramStr += INDENT+"$sign_params = array();\n";
                for (ParameterInfo p : signParams) {
                    paramStr += INDENT+"$sign_params[\"" + p.getName() + "\"] = $" +
                            Util.getVariableName(p.getName()) + ";\n";
                }
                paramStr += INDENT+"$" +Util.getVariableName(signedUrl.getSigKeyName()) + " = " +
                        getBean().getAuthenticatorClassName() + "::sign($sign_params);\n";
                methodBody += paramStr;
            }
        }
        return methodBody;
    }

    /**
     *  Create Authenticator
     */
    public void createAuthenticatorClass() throws IOException {
        FileObject targetFolder = getSaasServiceFolder();
        if(!getBean().isUseTemplates()) {
            if(saasAuthFile == null) {
                String authFileName = getBean().getAuthenticatorClassName();
                String authTemplate = null;
                SaasAuthenticationType authType = getBean().getAuthenticationType();
                if (authType == SaasAuthenticationType.API_KEY) {
                    authTemplate = SaasClientCodeGenerator.TEMPLATES_SAAS + 
                            getAuthenticationType().getClassIdentifier();
                } else if (authType == SaasAuthenticationType.HTTP_BASIC) {
                    authTemplate = SaasClientCodeGenerator.TEMPLATES_SAAS + 
                            getAuthenticationType().getClassIdentifier();
                } else if (authType == SaasAuthenticationType.SIGNED_URL) {
                    authTemplate = SaasClientCodeGenerator.TEMPLATES_SAAS + 
                            getAuthenticationType().getClassIdentifier();
                } else if (authType == SaasAuthenticationType.SESSION_KEY) {
                    authTemplate = SaasClientCodeGenerator.TEMPLATES_SAAS + 
                            getAuthenticationType().getClassIdentifier();
                }
                if (authTemplate != null) {
                    DataObject d = Util.createDataObjectFromTemplate(
                            authTemplate + Constants.SERVICE_AUTHENTICATOR + "."+Constants.PHP_EXT,
                            targetFolder, authFileName);// NOI18n
                    if(d != null)
                        saasAuthFile = d.getPrimaryFile();
                }
            }
        } else {
            UseTemplates useTemplates = null;
            if(getBean().getAuthentication() instanceof SessionKeyAuthentication) {
                SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) getBean().getAuthentication();
                useTemplates = sessionKey.getUseTemplates();
            } else if(getBean().getAuthentication() instanceof HttpBasicAuthentication) {
                HttpBasicAuthentication httpBasic = (HttpBasicAuthentication) getBean().getAuthentication();
                useTemplates = httpBasic.getUseTemplates();
            }
            if(useTemplates != null) {
                for (Template template : useTemplates.getTemplates()) {
                    String id = template.getId();
                    String type = template.getType();
                    String templateUrl = template.getUrl();
                    if (type.equals(Constants.AUTH)) {
                        String fileName = getBean().getAuthenticatorClassName();
                        FileObject fobj = targetFolder.getFileObject(fileName);
                        if (fobj == null) {
                            Util.createDataObjectFromTemplate(templateUrl, targetFolder,
                                    fileName);
                        }
                    }
                }
            }
        }

        //Also copy config
        if(getBean().getAuthenticationType() != SaasAuthenticationType.PLAIN) {
            String profileName = getBean().getAuthenticatorClassName()+"Profile";
            if (getAuthenticationProfile() != null && !getAuthenticationProfile().trim().equals("")) {
                try {
                    Util.createDataObjectFromTemplate(getAuthenticationProfile(),
                            targetFolder, profileName);
                } catch (Exception ex) {
                    throw new IOException("Profile file specified in " +
                            "saas-services/service-metadata/authentication/@profile, " +
                            "not found: " + getAuthenticationProfile());// NOI18n
                }
            }
        }
    }

    
    /**
     *  Create Authorization Frame
     */
    public void createAuthorizationClasses() throws IOException {
    }

    /**
     *  Return target and generated file objects
     */
    public void modifyAuthenticationClass() throws IOException {
    }

    /**
     *  Return target and generated file objects
     */
    public void modifyAuthenticationClass(final String comment, final Object[] modifiers,
            final Object returnType, final String name, final String[] parameters, final Object[] paramTypes,
            final Object[] throwList, final String bodyText)
            throws IOException {
    }

    public String getLoginBody(SaasBean bean,
            String groupName, String paramVariableName) throws IOException {
        if (getBean().isDropTargetWeb()) {
            if (getBean().getAuthenticationType() != SaasAuthenticationType.SESSION_KEY) {
                return null;
            }
            return Util.createSessionKeyLoginBodyForWeb(bean, groupName, paramVariableName);
        }
        String methodBody = "";
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) getBean().getAuthentication();
        UseGenerator useGenerator = sessionKey.getUseGenerator();
        if (useGenerator != null) {
            Login login = useGenerator.getLogin();
            if (login != null) {
                String tokenName = Util.getTokenName(useGenerator);
                String tokenMethodName = Util.getTokenMethodName(useGenerator);
                methodBody += "        if (" + Util.getVariableName(sessionKey.getSessionKeyName()) + " == null) {\n";
                methodBody += "            String " + tokenName + " = " + tokenMethodName + "(" +
                        Util.getHeaderOrParameterUsage(getAuthenticatorMethodParameters()) + ");\n\n";

                methodBody += "            if (" + tokenName + " != null) {\n";
                methodBody += "                try {\n";
                Map<String, String> tokenMap = new HashMap<String, String>();
                methodBody += Util.getLoginBody(login, getBean(), groupName, tokenMap);
                methodBody += "                } catch (IOException ex) {\n";
                methodBody += "                    Logger.getLogger(" + getBean().getAuthenticatorClassName() + ".class.getName()).log(Level.SEVERE, null, ex);\n";
                methodBody += "                }\n\n";

                methodBody += "            }\n";
                methodBody += "        }\n";
            }
        }
        return methodBody;
    }

    public String getLogoutBody() {
        String methodBody = "";
        return methodBody;
    }

    public String getTokenBody(SaasBean bean,
            String groupName, String paramVariableName, String saasServicePkgName) throws IOException {
        if (getBean().isDropTargetWeb()) {
            if (getBean().getAuthenticationType() != SaasAuthenticationType.SESSION_KEY) {
                return null;
            }
            return Util.createSessionKeyTokenBodyForWeb(bean, groupName, paramVariableName,
                    saasServicePkgName);
        }
        String authFileName = getBean().getAuthorizationFrameClassName();
        String methodBody = "";
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) getBean().getAuthentication();
        UseGenerator useGenerator = sessionKey.getUseGenerator();
        if (useGenerator != null) {
            Token token = useGenerator.getToken();
            if (token != null) {
                String tokenName = Util.getTokenName(useGenerator);
                String sigId = "sig";
                if (token.getSignId() != null) {
                    sigId = token.getSignId();
                }
                String methodName = null;
                Method method = token.getMethod();
                if (method != null) {
                    methodName = method.getHref();
                    if (methodName == null) {
                        return methodBody;
                    } else {
                        methodName = methodName.startsWith("#") ? methodName.substring(1) : methodName;
                    }
                }
                methodBody += "       String " + tokenName + " = null;\n";
                methodBody += "       try {\n";
                methodBody += "            String method = \"" + methodName + "\";\n";
                methodBody += "            String v = \"1.0\";\n\n";

                List<ParameterInfo> signParams = token.getParameters();
                if (signParams != null && signParams.size() > 0) {
                    String paramStr = "";
                    paramStr += "        String " + sigId + " = sign(secret, \n";
                    paramStr += getSignParamUsage(signParams, groupName);
                    paramStr += ");\n\n";
                    methodBody += paramStr;
                }

                String queryParamsCode = "";
                Map<String, String> tokenMap = new HashMap<String, String>();
                if (method != null) {
                    String id = method.getId();
                    if (id != null) {
                        String[] tokens = id.split(",");
                        for (String tk : tokens) {
                            String[] tokenElem = tk.split("=");
                            if (tokenElem.length == 2) {
                                tokenMap.put(tokenElem[0], tokenElem[1]);
                            }
                        }
                    }
                    String href = method.getHref();
                    if (href != null && bean instanceof RestClientSaasBean) {
                        org.netbeans.modules.websvc.saas.model.wadl.Method wadlMethod =
                                SaasUtil.wadlMethodFromIdRef(
                                ((RestClientSaasBean)bean).getMethod().getSaas().getWadlModel(), href);
                        if (wadlMethod != null) {
                            ArrayList<ParameterInfo> params = ((RestClientSaasBean)bean).findWadlParams(wadlMethod);
                            if (params != null &&
                                    params.size() > 0) {
                                queryParamsCode = Util.getHeaderOrParameterDefinition(params, paramVariableName, false);
                            }
                        }
                    }
                }

                //Insert parameter declaration
                methodBody += "        " + queryParamsCode;

                String url = "";
                if (bean instanceof RestClientSaasBean) {
                    url = ((RestClientSaasBean) bean).getUrl();
                }
                methodBody += "             " + Constants.REST_CONNECTION + " conn = new " + Constants.REST_CONNECTION + "(\"" + url + "\"";
                if (!queryParamsCode.trim().equals("")) {
                    methodBody += ", " + paramVariableName;
                }
                methodBody += ");\n";

                methodBody += "            String result = conn.get();\n";

                for (Entry e : tokenMap.entrySet()) {
                    String name = Util.getVariableName((String) e.getKey());
                    String val = (String) e.getValue();
                    if (val.startsWith("{")) {
                        val = val.substring(1);
                    }
                    if (val.endsWith("}")) {
                        val = val.substring(0, val.length() - 1);
                    }
                    methodBody += "            " + name + " = result.substring(result.indexOf(\"<" + val + "\"),\n";
                    methodBody += "                            result.indexOf(\"</" + val + ">\"));\n\n";
                    methodBody += "            " + name + " = " + name + ".substring(" + name + ".indexOf(\">\") + 1);\n\n";
                }


                if (token.getPrompt() != null) {
                    Prompt prompt = token.getPrompt();
                    signParams = prompt.getParameters();
                    if (signParams != null && signParams.size() > 0) {
                        methodBody += "            String perms = \"write\";";
                        String paramStr = "";
                        paramStr += "        " + sigId + " = sign(\n";
                        paramStr += "                new String[][] {\n";
                        for (ParameterInfo p : signParams) {
                            paramStr += "                    {\"" + p.getName() + "\", " +
                                    Util.getParameterName(p, true, true) + "},\n";
                        }
                        paramStr += "        });\n\n";
                        methodBody += paramStr;
                    }
                    url = prompt.getDesktopUrl();
                    methodBody += "            String loginUrl = \"" + Util.getTokenPromptUrl(token, url) + "\";\n";
                }
                methodBody += "            " + authFileName + " frame = new " + authFileName + "(loginUrl);\n";
                methodBody += "            synchronized (frame) {\n";
                methodBody += "                try {\n";
                methodBody += "                    frame.wait();\n";
                methodBody += "                } catch (InterruptedException ex) {\n";
                methodBody += "                    Logger.getLogger(" + getBean().getAuthenticatorClassName() + ".class.getName()).log(Level.SEVERE, null, ex);\n";
                methodBody += "                }\n";
                methodBody += "            }\n";
                methodBody += "       } catch (IOException ex) {\n";
                methodBody += "            Logger.getLogger(" + getBean().getAuthenticatorClassName() + ".class.getName()).log(Level.SEVERE, null, ex);\n";
                methodBody += "       }\n\n";
                methodBody += "       return " + tokenName + ";\n";
            }
        }
        return methodBody;
    }

    public String getSignParamUsage(List<ParameterInfo> signParams, String groupName) {
        return Util.getSignParamUsage(signParams, groupName, 
                getBean().isDropTargetWeb());
    }
    
    /*
     * Generates something like 
    $apiKey = FacebookAuthenticator::getApiKey();
    $sessionKey = FacebookAuthenticator::getSessionKey();
    $method = "facebook.friends.get";
    $v = "1.0";
    $callId = RestConnection::currentTimeMillis();
     */
    public static String getSignParamDeclaration(SaasBean bean,
            List<ParameterInfo> signParams, List<ParameterInfo> filterParams) {
        String paramStr = "";
        for (ParameterInfo p : signParams) {
            String[] pIds = getParamIds(p, bean.getSaasName(),
                    bean.isDropTargetWeb());
            if (pIds != null) {//process special case
                paramStr += INDENT+ "$" + Util.getVariableName(pIds[0]) + " = " + pIds[1] + ";\n";
                continue;
            }
            if (Util.isContains(p, filterParams)) {
                continue;
            }

            paramStr += INDENT+ "$" + Util.getVariableName(p.getName()) + " = ";
            if (p.getFixed() != null) {
                paramStr += "\"" + p.getFixed() + "\";\n";
            } else if (p.getType() == Date.class) {
                paramStr += "$conn->getDate();\n";
            } else if (p.getType() == Time.class) {
                paramStr += "RestConnection::currentTimeMillis();\n";
            } else if (p.getType() == HttpMethodType.class) {
                paramStr += "\"" + bean.getHttpMethod().value() + "\";\n";
            } else if (p.isRequired()) {
                if (p.getDefaultValue() != null) {
                    paramStr += getQuotedValue(p.getDefaultValue().toString()) + ";\n";
                } else {
                    paramStr += "\"\";\n";
                }
            } else {
                if (p.getDefaultValue() != null) {
                    paramStr += getQuotedValue(p.getDefaultValue().toString()) + ";\n";
                } else {
                    paramStr += "null;\n";
                }
            }
        }
        paramStr += "\n";
        return paramStr;
    }
    
    public static String getQuotedValue(String value) {
        return Util.getQuotedValue(value).replace("+", ".");
    }
    
    public static String[] getParamIds(ParameterInfo p, String groupName,
            boolean isDropTargetWeb) {
        if (p.getId() != null) {//process special case
            String[] pElems = p.getId().split("=");
            if (pElems.length == 2) {
                String val = pElems[1];
                if (val.startsWith("{")) {
                    val = val.substring(1);
                }
                if (val.endsWith("}")) {
                    val = val.substring(0, val.length() - 1);
                }
                val = Util.getVariableName(val);
                val = Util.getAuthenticatorClassName(groupName) + "::" +
                        "get" + val.substring(0, 1).toUpperCase() + val.substring(1);
                val += "()";
                return new String[]{pElems[0], val};
            }
        }
        return null;
    }
}
