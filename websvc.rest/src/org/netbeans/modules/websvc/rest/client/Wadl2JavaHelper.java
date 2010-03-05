/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.rest.client;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.rest.client.ClientJavaSourceHelper.HttpMimeType;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WadlSaasResource;
import org.netbeans.modules.websvc.saas.model.jaxb.TemplateType;
import org.netbeans.modules.websvc.saas.model.wadl.Method;
import org.netbeans.modules.websvc.saas.model.wadl.RepresentationType;
import org.netbeans.modules.websvc.saas.model.wadl.Request;
import org.netbeans.modules.websvc.saas.model.wadl.Response;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

/**
 *
 * @author mkuchtiak
 */
class Wadl2JavaHelper {

    private static final String PROP_XML_SCHEMA="xml_schema"; //NOI18N
    private static final String PROP_PACKAGE_NAME="package_name"; //NOI18N
    private static final String PROP_SOURCE_ROOT="source_root"; //NOI18N
    private static final String SIGN_PARAMS_METHOD="signParams";

    static ClassTree addHttpMethods(WorkingCopy copy, ClassTree innerClass, WadlSaasResource saasResource, Security security) {
        List<WadlSaasMethod> saasMethods = saasResource.getMethods();
        ClassTree modifiedInnerClass = innerClass;
        TreeMaker maker = copy.getTreeMaker();
        boolean hasMultipleParamsInList = false;
        boolean hasOptionalQueryParams = false;
        boolean hasFormParams = false;
        HttpParams globalParams = new HttpParams(saasResource);
        for (WadlSaasMethod saasMethod : saasMethods) {
            HttpParams httpParams = new HttpParams(saasMethod);
            httpParams.mergeQueryandHeaderParams(globalParams);
            if (httpParams.hasMultipleParamsInList()) {
                hasMultipleParamsInList = true;
            }
            if ((httpParams.hasOptionalQueryParams() && httpParams.hasRequiredQueryParams()) ||
                    httpParams.hasDefaultQueryParams()) {
                hasOptionalQueryParams = true;
            }
            if (httpParams.hasFormParams()) {
                hasFormParams = true;
            }
            List<MethodTree> httpMethods = Wadl2JavaHelper.createHttpMethods(copy, saasMethod, httpParams, security);
            for (MethodTree httpMethod : httpMethods) {
                modifiedInnerClass = maker.addClassMember(modifiedInnerClass, httpMethod);
            }
        }
        if (hasMultipleParamsInList || hasFormParams) {
            // add new private method to compute MultivaluedMap
            String mvMapClass = "javax.ws.rs.core.MultivaluedMap"; //NOI18N
            TypeElement mvMapEl = copy.getElements().getTypeElement(mvMapClass);
            String mvType = mvMapEl == null ? mvMapClass : "MultivaluedMap"; //NOI18N

            String body =
            "{"+ //NOI18N
                mvType+"<String,String> qParams = new com.sun.jersey.api.representation.Form();"+ //NOI18N
                "for (int i=0;i< paramNames.length;i++) {" + //NOI18N
                "    if (paramValues[i] != null) {"+ //NOI18N
                "        qParams.add(paramNames[i], paramValues[i]);"+ //NOI18N
                "    }"+ //NOI18N
                "}"+ //NOI18N
                "return qParams;"+ //NOI18N
            "}"; //NOI18N
            ModifiersTree methodModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE));
            ExpressionTree returnTree =
                    mvMapEl == null ?
                        copy.getTreeMaker().Identifier(mvMapClass) :
                        copy.getTreeMaker().QualIdent(mvMapEl);
            List<VariableTree> paramList = new ArrayList<VariableTree>();
            ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());
            paramList.add(maker.Variable(paramModifier, "paramNames", maker.Identifier("String[]"), null)); //NOI18N
            paramList.add(maker.Variable(paramModifier, "paramValues", maker.Identifier("String[]"), null)); //NOI18N
            MethodTree methodTree = maker.Method (
                    methodModifier,
                    "getQueryOrFormParams", //NOI18N
                    returnTree,
                    Collections.<TypeParameterTree>emptyList(),
                    paramList,
                    Collections.<ExpressionTree>emptyList(),
                    body,
                    null); //NOI18N
            modifiedInnerClass = maker.addClassMember(modifiedInnerClass, methodTree);
        }
        if (hasOptionalQueryParams) {
            // add new private method
            String mvMapClass = "javax.ws.rs.core.MultivaluedMap"; //NOI18N
            TypeElement mvMapEl = copy.getElements().getTypeElement(mvMapClass);
            String mvType = mvMapEl == null ? mvMapClass : "MultivaluedMap"; //NOI18N

            String body =
            "{"+ //NOI18N
                mvType+"<String,String> qParams = new com.sun.jersey.api.representation.Form();"+ //NOI18N
               "for (String qParam : optionalParams) {" + //NOI18N
                "    String[] qPar = qParam.split(\"=\");"+ //NOI18N
                "    if (qPar.length > 1) qParams.add(qPar[0], qPar[1])"+ //NOI18N
                "}"+ //NOI18N
                "return qParams;"+ //NOI18N
            "}"; //NOI18N
            ModifiersTree methodModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE));
            ExpressionTree returnTree =
                    mvMapEl == null ?
                        copy.getTreeMaker().Identifier(mvMapClass) :
                        copy.getTreeMaker().QualIdent(mvMapEl);
            ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());
            VariableTree param = maker.Variable(paramModifier, "optionalParams", maker.Identifier("String..."), null); //NOI18N
            MethodTree methodTree = maker.Method (
                    methodModifier,
                    "getQParams", //NOI18N
                    returnTree,
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>singletonList(param),
                    Collections.<ExpressionTree>emptyList(),
                    body,
                    null); //NOI18N
            modifiedInnerClass = maker.addClassMember(modifiedInnerClass, methodTree);
        }
        return modifiedInnerClass;
    }

    static List<MethodTree> createHttpMethods(WorkingCopy copy, WadlSaasMethod saasMethod, HttpParams httpParams, Security security) {
        List<MethodTree> httpMethods = new ArrayList<MethodTree>();
        String methodName = saasMethod.getName();
        Method wadlMethod = saasMethod.getWadlMethod();
        String methodType = wadlMethod.getName();
        //HeaderParamsInfo headerParamsInfo = new HeaderParamsInfo(saasMethod);
        if (RestConstants.GET_ANNOTATION.equals(methodType)) { //GET
            List<RepresentationType> produces = new ArrayList<RepresentationType>();
            Response wadlResponse = wadlMethod.getResponse();
            if (wadlResponse != null) {
                List<JAXBElement<RepresentationType>> reprOrFaults = wadlResponse.getRepresentationOrFault();
                for (JAXBElement<RepresentationType> reprOrFault : reprOrFaults) {
                    if ("representation".equals(reprOrFault.getName().getLocalPart())) { //NOI18N
                        produces.add(reprOrFault.getValue());
                    }
                }
            }
            boolean found = false;
            boolean multipleMimeTypes = produces.size() > 1;
            for (RepresentationType prod : produces) {
                String mediaType = prod.getMediaType();
                if (mediaType != null) {
                    for (HttpMimeType mimeType : HttpMimeType.values()) {
                        if (mediaType.equals(mimeType.getMimeType())) {
                            MethodTree method = createHttpGETMethod(copy, saasMethod, mimeType, multipleMimeTypes, httpParams, security);
                            if (method != null) {
                                httpMethods.add(method);
                            }
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (!found) {
                httpMethods.add(createHttpGETMethod(copy, saasMethod, null, false, httpParams, security));
            }

        } else if ( RestConstants.PUT_ANNOTATION.equals(methodType) ||
                    RestConstants.POST_ANNOTATION.equals(methodType) ||
                    RestConstants.DELETE_ANNOTATION.equals(methodType)
                  ) {
            List<RepresentationType> consumes = new ArrayList<RepresentationType>();
            Request wadlRequest = wadlMethod.getRequest();
            if (wadlRequest != null) {
                List<RepresentationType> representationTypes = wadlRequest.getRepresentation();
                for (RepresentationType reprType : representationTypes) {
                    consumes.add(reprType);
                }
            }

            boolean found = false;
            boolean multipleMimeTypes = consumes.size() > 1;
            for (RepresentationType cons : consumes) {
                String mediaType = cons.getMediaType();
                if (mediaType != null) {
                    for (HttpMimeType mimeType : HttpMimeType.values()) {
                        if (mediaType.equals(mimeType.getMimeType())) {
                            MethodTree method = createHttpPOSTMethod(copy, saasMethod, mimeType, multipleMimeTypes, httpParams, security);
                            if (method != null) {
                                httpMethods.add(method);
                            }
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (!found) {
                httpMethods.add(createHttpPOSTMethod(copy, saasMethod, null, false, httpParams, security));
            }
        }
        return httpMethods;
    }

    static MethodTree createHttpGETMethod(WorkingCopy copy, WadlSaasMethod saasMethod, HttpMimeType mimeType, boolean multipleMimeTypes, HttpParams httpParams, Security security) {
        String methodName = saasMethod.getName() + (multipleMimeTypes ? "_"+mimeType.name() : ""); //NOI18N

        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree methodModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC));
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());

        VariableTree classParam = maker.Variable(paramModifier, "responseType", maker.Identifier("Class<T>"), null); //NOI18N
        ExpressionTree responseTree = maker.Identifier("T");
        String bodyParam = "responseType"; //NOI18N
        List<TypeParameterTree> typeParams =   Collections.<TypeParameterTree>singletonList(maker.TypeParameter("T", Collections.<ExpressionTree>emptyList()));

        List<VariableTree> paramList = new ArrayList<VariableTree>();
        if (classParam != null) {
            paramList.add(classParam);
        }

        StringBuffer queryP = new StringBuffer();
        StringBuffer queryParamPart = new StringBuffer();
        StringBuffer commentBuffer = new StringBuffer("@param responseType Class representing the response\n"); //NOI18N

        if (httpParams.hasQueryParams() || httpParams.hasHeaderParams()) {
            addQueryAndHeaderParams(maker, httpParams, security, paramList, queryP, queryParamPart, commentBuffer);
        }

        commentBuffer.append("@return response object (instance of responseType class)"); //NOI18N
        String body =
            "{"+queryParamPart+ //NOI18N

                (mimeType == null ?
                    "   return webResource"+queryP+".get("+bodyParam+");" :  //NOI18N
                    "   return webResource"+queryP+".accept("+mimeType.getMediaType()+").get("+bodyParam+");") +  //NOI18N
            "}"; //NOI18N
        
        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.api.client.UniformInterfaceException"); //NOI18N
        throwsList.add(throwsTree);
        if (Security.Authentication.SESSION_KEY == security.getAuthentication()) {
            ExpressionTree ioExceptionTree = JavaSourceHelper.createTypeTree(copy, "java.io.IOException"); //NOI18N
            throwsList.add(ioExceptionTree);
        }

        MethodTree method = maker.Method (
                            methodModifier,
                            methodName,
                            responseTree,
                            typeParams,
                            paramList,
                            throwsList,
                            body,
                            null);
        if (method != null) {
            Comment comment = Comment.create(Style.JAVADOC, commentBuffer.toString());
            maker.addComment(method, comment, true);
        }
        return method;
    }

    static MethodTree createHttpPOSTMethod(WorkingCopy copy, WadlSaasMethod saasMethod, HttpMimeType requestMimeType, boolean multipleMimeTypes, HttpParams httpParams, Security security) {
        String methodName = saasMethod.getName() + (multipleMimeTypes ? "_"+requestMimeType.name() : ""); //NOI18N
        String methodPrefix = saasMethod.getWadlMethod().getName().toLowerCase();

        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree methodModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC));
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());

        Response resp = saasMethod.getWadlMethod().getResponse();

        List<VariableTree> paramList = new ArrayList<VariableTree>();
        ExpressionTree responseTree = null;
        List<TypeParameterTree> typeParams = null;
        String bodyParam1 = "";
        String bodyParam2 = "";
        String ret = ""; //NOI18N

        if (resp != null) {
            VariableTree classParam = maker.Variable(paramModifier, "responseType", maker.Identifier("Class<T>"), null); //NOI18N
            responseTree = maker.Identifier("T");
            bodyParam1 = "responseType"; //NOI18N
            typeParams =   Collections.<TypeParameterTree>singletonList(maker.TypeParameter("T", Collections.<ExpressionTree>emptyList()));
            if (classParam != null) {
                paramList.add(classParam);
            }
            ret = "return "; //NOI18N
        } else {
            responseTree = maker.Identifier("void");
            typeParams = Collections.<TypeParameterTree>emptyList();
        }

        StringBuffer queryP = new StringBuffer();
        StringBuffer queryParamPart = new StringBuffer();
        StringBuffer commentBuffer = new StringBuffer("@param responseType Class representing the response\n"); //NOI18N

        if (httpParams.hasFormParams() || httpParams.hasQueryParams() || httpParams.hasHeaderParams()) {
            addQueryAndHeaderParams(maker, httpParams, security, paramList, queryP, queryParamPart, commentBuffer);
        }

        if (requestMimeType != null) {
            if (requestMimeType == HttpMimeType.FORM && httpParams.hasFormParams()) {
                bodyParam2=(bodyParam1.length() > 0 ? ", " : "") + "getQueryOrFormParams(formParamNames, formParamValues)"; //NOI18N
            } else {
                VariableTree objectParam = maker.Variable(paramModifier, "requestEntity", maker.Identifier("Object"), null); //NOI18N
                paramList.add(0, objectParam);
                bodyParam2=(bodyParam1.length() > 0 ? ", " : "") + "requestEntity"; //NOI18N
                commentBuffer.append("@param requestEntity request data");
            }
        }

        commentBuffer.append("@return response object (instance of responseType class)"); //NOI18N

        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.api.client.UniformInterfaceException"); //NOI18N
        throwsList.add(throwsTree);

        if (Security.Authentication.SESSION_KEY == security.getAuthentication()) {
            ExpressionTree ioExceptionTree = JavaSourceHelper.createTypeTree(copy, "java.io.IOException"); //NOI18N
            throwsList.add(ioExceptionTree);
        }

        String body =
            "{"+queryParamPart + //NOI18N
                (requestMimeType == null ?
                    "   "+ret+"webResource"+queryP+"."+methodPrefix+"("+bodyParam1+bodyParam2+");" :  //NOI18N
                    "   "+ret+"webResource"+queryP+".type("+requestMimeType.getMediaType()+")."+methodPrefix+"("+bodyParam1+bodyParam2+");") +  //NOI18N
            "}"; //NOI18N

        MethodTree method = maker.Method (
                methodModifier,
                methodName,
                responseTree,
                typeParams,
                paramList,
                throwsList,
                body,
                null); //NOI18N
        if (method != null) {
            Comment comment = Comment.create(Style.JAVADOC, commentBuffer.toString());
            maker.addComment(method, comment, true);
        }
        return method;
    }

    private static void addQueryAndHeaderParams(TreeMaker maker, HttpParams httpParams, Security security,  List<VariableTree> paramList, StringBuffer queryP, StringBuffer queryParamPart, StringBuffer commentBuffer) {
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());
        SecurityParams securityParams = security.getSecurityParams();
        // adding form params
        if (httpParams.hasFormParams()) {
            for (String formParam : httpParams.getFormParams()) {
                if (securityParams == null ||
                       (!isSecurityParam(formParam, securityParams) && !isSignatureParam(formParam, securityParams))) {
                    String javaIdentifier = makeJavaIdentifier(formParam);
                    VariableTree paramTree = maker.Variable(paramModifier, javaIdentifier, maker.Identifier("String"), null); //NOI18N
                    paramList.add(paramTree);
                    commentBuffer.append("@param "+javaIdentifier+" form parameter\n"); //NOI18N
                }
            }
            Pair<String> paramPair = null;
            if (securityParams != null) {
                paramPair = getParamList(httpParams.getFormParams(), httpParams.getFixedFormParams(), securityParams);
            } else {
                paramPair = getParamList(httpParams.getFormParams(), httpParams.getFixedFormParams());
            }
            queryParamPart.append("String[] formParamNames = new String[] {"+paramPair.getKey()+"}"); //NOI18N
            queryParamPart.append("String[] formParamValues = new String[] {"+paramPair.getValue()+"}"); //NOI18N
        }
        // add query params
        if (httpParams.hasQueryParams()) {
            if (httpParams.hasRequiredQueryParams()) {
                // adding method parameters
                for (String requiredParam : httpParams.getRequiredQueryParams()) {
                   if (securityParams == null ||
                           (!isSecurityParam(requiredParam, securityParams) && !isSignatureParam(requiredParam, securityParams))) {
                        String javaIdentifier = makeJavaIdentifier(requiredParam);
                        VariableTree paramTree = maker.Variable(paramModifier, javaIdentifier, maker.Identifier("String"), null); //NOI18N
                        paramList.add(paramTree);
                        commentBuffer.append("@param "+javaIdentifier+" query parameter[REQUIRED]\n"); //NOI18N
                    }
                }
                // adding query params calculation to metthod body
                if (httpParams.hasMultipleParamsInList()) {
                    Pair<String> paramPair = null;
                    if (securityParams != null) {
                        paramPair = getParamList(httpParams.getRequiredQueryParams(), httpParams.getFixedQueryParams(), securityParams);
                    } else {
                        paramPair = getParamList(httpParams.getRequiredQueryParams(), httpParams.getFixedQueryParams());
                    }
                    queryParamPart.append("String[] queryParamNames = new String[] {"+paramPair.getKey()+"}"); //NOI18N
                    queryParamPart.append("String[] queryParamValues = new String[] {"+paramPair.getValue()+"}"); //NOI18N
                    if (Security.Authentication.SESSION_KEY == security.getAuthentication() && securityParams != null) {
                        String optParams = ""; //NOI18N
                        if (httpParams.hasOptionalQueryParams()) {
                            optParams = ", optionalQueryParams";
                        }
                        queryParamPart.append("String signature = "+SIGN_PARAMS_METHOD+"(queryParamNames, queryParamValues"+optParams+");"); //NOI18N
                        String sigParam = securityParams.getSignature();
                        queryP.append(".queryParams(getQueryOrFormParams(queryParamNames, queryParamValues)).queryParam(\""+sigParam+"\", signature)"); //NOI18N
                    } else {
                        queryP.append(".queryParams(getQueryOrFormParams(queryParamNames, queryParamValues))"); //NOI18N
                    }
                } else {
                    List<String> requiredParams = httpParams.getRequiredQueryParams();
                    if (requiredParams.size() > 0) {
                        String paramName = requiredParams.get(0);
                        String paramValue = makeJavaIdentifier(requiredParams.get(0));
                        if (Security.Authentication.SESSION_KEY == security.getAuthentication() && securityParams != null && httpParams.hasFormParams()) {
                            String optParams = ""; //NOI18N
                            if (httpParams.hasOptionalQueryParams()) {
                                optParams = ", optionalQueryParams";
                            }
                            queryParamPart.append("String signature = "+SIGN_PARAMS_METHOD+"(formParamNames, formParamValues"+optParams+");"); //NOI18N
                            String sigParam = securityParams.getSignature();
                            queryP.append(".queryParam(\""+sigParam+"\", signature)"); //NOI18N
                        } else {
                            queryP.append(".queryParam(\""+paramName+"\","+paramValue+")"); //NOI18N
                        }
                    } else {
                        Map<String, String> fixedParams = httpParams.getFixedQueryParams();
                        for (String paramName : fixedParams.keySet()) {
                            String paramValue = fixedParams.get(paramName);
                            queryP.append(".queryParam(\""+paramName+"\",\""+paramValue+"\")"); //NOI18N"
                        }
                    }
                }
            } else if (httpParams.hasOptionalQueryParams()) {
                // optional params should be listed also when there are no required params
                for (String optionalParam : httpParams.getOptionalQueryParams()) {
                    String javaIdentifier = makeJavaIdentifier(optionalParam);
                    VariableTree paramTree = maker.Variable(paramModifier, javaIdentifier, maker.Identifier("String"), null); //NOI18N
                    paramList.add(paramTree);
                    commentBuffer.append("@param "+javaIdentifier+" query parameter\n"); //NOI18N
                }
                // there are no fixed params in this case
                Pair<String> paramPair = getParamList(httpParams.getOptionalQueryParams(), Collections.<String, String>emptyMap());
                queryParamPart.append("String[] queryParamNames = new String[] {"+paramPair.getKey()+"}"); //NOI18N
                queryParamPart.append("String[] queryParamValues = new String[] {"+paramPair.getValue()+"}"); //NOI18N
                queryP.append(".queryParams(getQueryOrFormParams(queryParamNames, queryParamValues))"); //NOI18N
            }

            // add optional params (only when there are also some required params)
            if ((httpParams.hasOptionalQueryParams() && httpParams.hasRequiredQueryParams()) || httpParams.hasDefaultQueryParams()) {
                VariableTree paramTree = maker.Variable(paramModifier, "optionalQueryParams", maker.Identifier("String..."), null); //NOI18N
                paramList.add(paramTree);

                commentBuffer.append("@param optionalQueryParams List of optional query parameters in the form of \"param_name=param_value\",...<br>\nList of optional query parameters:\n"); //NOI18N
                for (String otherParam : httpParams.getOptionalQueryParams()) {
                    commentBuffer.append("<LI>"+otherParam+" [OPTIONAL]\n"); //NOI18N
                }
                // add default params
                Map<String,String> defaultParams = httpParams.getDefaultQueryParams();
                for (String key : defaultParams.keySet()) {
                    commentBuffer.append("<LI>"+key+" [OPTIONAL, DEFAULT VALUE: \""+defaultParams.get(key)+"\"]\n"); //NOI18N
                }
                queryP.append(".queryParams(getQParams(optionalQueryParams))"); //NOI18N
            }


        }
        // add header params
        if (httpParams.hasHeaderParams()) {
            for (String headerParam : httpParams.getHeaderParams()) {
                String javaIdentifier = makeJavaIdentifier(headerParam);
                VariableTree paramTree = maker.Variable(paramModifier, javaIdentifier, maker.Identifier("String"), null); //NOI18N
                paramList.add(paramTree);
                commentBuffer.append("@param "+javaIdentifier+" header parameter[REQUIRED]\n"); //NOI18N
                queryP.append(".header(\""+headerParam+"\","+javaIdentifier+")"); //NOI18N
            }
            Map<String, String> fixedHeaderParams = httpParams.getFixedHeaderParams();
            for (String paramName : fixedHeaderParams.keySet()) {
                String paramValue = fixedHeaderParams.get(paramName);
                queryP.append(".header(\""+paramName+"\",\""+paramValue+"\")"); //NOI18N
            }
        }
    }

    private static Pair<String> getParamList(List<String> requiredParams, Map<String,String> fixedParams) {
        StringBuffer paramNames = new StringBuffer();
        StringBuffer paramValues = new StringBuffer();
        boolean first = true;
        for (String p : requiredParams) {
            if (first) {
                first = false;
            } else {
                paramNames.append(",");
                paramValues.append(",");
            }
            paramNames.append("\""+p+"\"");
            paramValues.append(makeJavaIdentifier(p));
        }
        for (String p : fixedParams.keySet()) {
            if (first) {
                first = false;
            } else {
                paramNames.append(",");
                paramValues.append(",");
                
            }
            paramNames.append("\""+p+"\"");
            paramValues.append("\""+fixedParams.get(p)+"\"");
        }

        return new Pair<String>(paramNames.toString(), paramValues.toString());
    }

    private static Pair<String> getParamList(List<String> requiredParams, Map<String,String> fixedParams, SecurityParams securityParams) {
        StringBuffer paramNames = new StringBuffer();
        StringBuffer paramValues = new StringBuffer();
        boolean first = true;
        for (String p : requiredParams) {
            if (!isSignatureParam(p, securityParams)) {
                if (first) {
                    first = false;
                } else {
                    paramNames.append(",");
                    paramValues.append(",");
                }
                paramNames.append("\""+p+"\"");
                if (isSecurityParam(p, securityParams)) {
                    paramValues.append(findGetterForParam(p, securityParams.getMethodDescriptors()));
                } else {
                    paramValues.append(makeJavaIdentifier(p));
                }
            }
        }
        for (String p : fixedParams.keySet()) {
            if (!isSignatureParam(p, securityParams)) {
                if (first) {
                    first = false;
                } else {
                    paramNames.append(",");
                    paramValues.append(",");

                }
                paramNames.append("\""+p+"\"");
                if (isSecurityParam(p, securityParams)) {
                    paramValues.append(findGetterForParam(p, securityParams.getMethodDescriptors()));
                } else {
                    paramValues.append("\""+fixedParams.get(p)+"\"");
                }

            }
        }
        return new Pair<String>(paramNames.toString(), paramValues.toString());
    }

    private static String makeJavaIdentifier(String s) {
        int len = s.length();
        String result = s;
        for (int i=0; i<len; i++) {
            char ch = result.charAt(i);
            if (!Character.isJavaIdentifierPart(ch)) {
                result = result.replace(ch, '_');
            }
        }
        if (len>0) {
            if (!Character.isJavaIdentifierStart(result.charAt(0))) {
                result = "_"+result;
            }
            result = result.substring(0,1).toLowerCase()+result.substring(1);
        }
        return result;
    }

    static String getClientClassName(WadlSaasResource saasResource) {
        String path = saasResource.getResource().getPath();
        int len = path.length();
        for (int i=0; i<len; i++) {
            char ch = path.charAt(i);
            if (!Character.isJavaIdentifierPart(ch)) {
                path = path.replace(ch, '_'); //NOI18N
            }
        }
        while (path.startsWith("_")) { //NOI18N
            path = path.substring(1);
        }
        while (path.endsWith("_")) { //NOI18N
            path = path.substring(0, path.length()-1);
        }
        String saasName = saasResource.getSaas().getDisplayName();
        saasName = saasName.replace(" ", "_"); //NOI18N

        if (saasName.length() == 0) {
            saasName = "Resource"; //NOI18N
        } else if (!Character.isJavaIdentifierStart(saasName.charAt(0))) {
            saasName= "Resource_"+saasName; //NOI18N
        } else if (Character.isLowerCase(saasName.charAt(0))) {
            saasName = saasName.substring(0,1).toUpperCase()+saasName.substring(1);
        }

        return saasName+(path.length() == 0 ? "" : "_"+path)+"_JerseyClient"; //NOI18N
    }

    static class Pair<T> {
        private T key;
        private T value;

        public Pair(T key, T value) {
            this.key = key;
            this.value = value;
        }

        public T getKey() {
            return key;
        }

        public T getValue() {
            return value;
        }
    }

    static void generateJaxb(FileObject targetFo, WadlSaas wadlSaas) throws java.io.IOException {
        Project project = FileOwnerQuery.getOwner(targetFo);
        if (project != null) {
            FileObject buildXml = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
            if (buildXml != null) {
                List<FileObject> schemaFiles = wadlSaas.getLocalSchemaFiles();
                if (schemaFiles.size() > 0) {
                    FileObject srcRoot = findSourceRootForFile(project, targetFo);
                    if (srcRoot != null) {
                        XmlStaxUtils staxUtils = new XmlStaxUtils();
                        String saasDir =  getSourceRootPath(project, srcRoot);
                        String packagePrefix = wadlSaas.getPackageName();
                        String targetName = "saas.xjc."+packagePrefix; //NOI18N
                        try {
                            boolean isXjcTarget = staxUtils.isTarget(buildXml, targetName); //NOI18N
                            if (!isXjcTarget) {
                                NotifyDescriptor dd = new NotifyDescriptor.Confirmation(
                                    NbBundle.getMessage(Wadl2JavaHelper.class, "MSG_CreateJaxbArtifacts", new Object[]{targetName, saasDir}),
                                    NotifyDescriptor.YES_NO_OPTION);
                                DialogDisplayer.getDefault().notify(dd);
                                if (NotifyDescriptor.OK_OPTION.equals(dd.getValue())) {
                                    // create META-INF if missing
                                    FileObject metaInf = srcRoot.getFileObject("META-INF"); //NOI18N
                                    if (metaInf == null) {
                                        metaInf = srcRoot.createFolder("META-INF"); //NOI18N
                                    }
                                    //try {
                                    String[] xmlSchemas = new String[schemaFiles.size()];
                                    String[] packageNames = new String[schemaFiles.size()];
                                    boolean isInitTarget = staxUtils.isTarget(buildXml, "saas-init-xjc"); //NOI18N
                                    int i=0;
                                    for (FileObject schemaFile : schemaFiles) {
                                        if (metaInf != null && metaInf.isFolder() && metaInf.getFileObject(schemaFile.getNameExt()) == null) {
                                            // copy schema to META-INF
                                            FileUtil.copyFile(schemaFile, metaInf, schemaFile.getName());
                                            xmlSchemas[i] = saasDir+"/META-INF/"+schemaFile.getNameExt(); //NOI18N
                                        } else {
                                            xmlSchemas[i] = schemaFile.getPath();
                                        }
                                        packageNames[i++] = packagePrefix+"."+SaasUtil.toValidJavaName(schemaFile.getName()).toLowerCase();
                                    }
                                    XmlDomUtils.addJaxbXjcTargets(buildXml, targetName, saasDir, xmlSchemas, packageNames, isInitTarget, isNbProject(project));
                                    for (FileObject schemaFile : schemaFiles) {
                                        ExecutorTask executorTask = ActionUtils.runTarget(buildXml, new String[] {targetName}, null);
                                    }
                                }
                            }
                        } catch (XMLStreamException ex) {
                            Logger.getLogger(Wadl2JavaHelper.class.getName()).log(Level.WARNING, "Can not parse wadl file", ex);
                        } catch (ParserConfigurationException ex) {
                            Logger.getLogger(Wadl2JavaHelper.class.getName()).log(Level.WARNING, "Can not configure parser for wadl file", ex);
                        } catch (SAXException ex) {
                            Logger.getLogger(Wadl2JavaHelper.class.getName()).log(Level.WARNING, "Can not parse wadl file", ex);
                        }
                    }
                }
            }
        }
    }

    static boolean isNbProject(Project project) {
        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
        for (int i=1;i<10;i++) {
            // be prepared for namespace upgrade
            if (aux.getConfigurationFragment("data", //NOI18N
                    "http://www.netbeans.org/ns/nb-module-project/"+String.valueOf(i), //NOI18N
                    true) != null) { //NOI18N
                return true;
            }
        }
        return false;
    }

    private static FileObject findSourceRootForFile(Project project, FileObject fo) {
        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject srcRoot = sourceGroup.getRootFolder();
            if (FileUtil.isParentOf(srcRoot, fo)) {
                return srcRoot;
            }
        }
        return null;
    }

    private static String getSourceRootPath(Project project, FileObject srcRoot) {
        return FileUtil.getRelativePath(project.getProjectDirectory(), srcRoot);
    }

    private static boolean isSecurityParam(String param, SecurityParams securityParams) {
        if (securityParams.getParams().contains(param)) {
            return true;
        }
        return false;
    }

    private static boolean isSignatureParam(String param, SecurityParams securityParams) {
        if (param.equals(securityParams.getSignature())) {
            return true;
        }
        return false;
    }

    private static String findGetterForParam(String param, List<TemplateType.MethodDescriptor> methodDescriptors) {
        for (TemplateType.MethodDescriptor method : methodDescriptors) {
            if (param.equals(method.getId())) {
                return method.getName()+"()"; //NOI18N
            }
        }
        return makeJavaIdentifier(param);
    }

    static ClassTree addSessionAuthMethods(WorkingCopy copy, ClassTree originalClass, Security security) {
        ClassTree modifiedClass = originalClass;
        TreeMaker maker = copy.getTreeMaker();
        SecurityParams securityParams = security.getSecurityParams();
        if (securityParams != null) {

            for (TemplateType.FieldDescriptor field : securityParams.getFieldDescriptors()) {
                ModifiersTree fieldModifier = null;
                if ("public".equals(field.getModifier())) { //NOI18N
                    fieldModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC));
                } else {
                    fieldModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE));
                }
                ExpressionTree fieldType = JavaSourceHelper.createTypeTree(copy, field.getType());
                VariableTree fieldTree = maker.Variable(fieldModifier, field.getName(), fieldType, null); //NOI18N
                modifiedClass = maker.addClassMember(modifiedClass, fieldTree);
            }

            for (TemplateType.MethodDescriptor m : securityParams.getMethodDescriptors()) {
                ModifiersTree methodModifier = null;
                if ("public".equals(m.getModifier())) { //NOI18N
                    methodModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC));
                } else {
                    methodModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE));
                }
                // add params
                List<VariableTree> paramList = new ArrayList<VariableTree>();
                String pList = m.getParamNames();
                if (pList != null) {
                    List<String> paramN = getList(pList);
                    List<String> paramT = getList(m.getParamTypes());
                    ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());
                    for (int i=0; i<paramN.size(); i++) {
                        Tree paramTypeTree = JavaSourceHelper.createTypeTree(copy, paramT.get(i)); //NOI18N
                        VariableTree paramTree = maker.Variable(paramModifier, paramN.get(i), paramTypeTree, null); //NOI18N
                        paramList.add(paramTree);
                    }
                }
                
                // add throws
                List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
                String tList = m.getThrows();
                if (tList != null) {
                    for (String thr : getList(tList)) {
                        throwsList.add(JavaSourceHelper.createTypeTree(copy, thr));
                    }
                }

                String body = m.getBody();
                if (body == null) {
                    body = getMethodBody(m.getBodyRef());
                    if (body == null) {
                        body = ("void".equals(m.getReturnType())? "{}" : "{return null;}"); //NOI18N
                    }
                }
                MethodTree methodTree = maker.Method (
                        methodModifier,
                        m.getName(), //NOI18N
                        JavaSourceHelper.createTypeTree(copy, m.getReturnType()), //NOI18N
                        Collections.<TypeParameterTree>emptyList(),
                        paramList,
                        throwsList,
                        body,
                        null); //NOI18N
                modifiedClass = maker.addClassMember(modifiedClass, methodTree);

            }
        }
        return modifiedClass;
    }

    private static List<String> getList(String s) {
        List<String> list = new ArrayList<String>();
        StringTokenizer tokens = new StringTokenizer(s, ",");
        while (tokens.hasMoreTokens()) {
            list.add(tokens.nextToken().trim());
        }
        return list;
    }

    private static String getMethodBody(String templatePath) {
        FileObject templateFo = FileUtil.getConfigFile(templatePath);
        if (templateFo != null) {
            try {
                InputStreamReader is = null;
                StringWriter writer = null;
                try {
                    is = new InputStreamReader(templateFo.getInputStream());
                    writer = new StringWriter();
                    char[] buffer = new char[1024];
                    int b;
                    while((b=is.read(buffer)) != -1) {
                        writer.write(buffer,0,b);
                    }
                    return writer.toString();
                } finally {
                    if (is != null) is.close();
                    if (writer != null) writer.close();
                }
            } catch(java.io.IOException ex) {
                return null;
            }
        }
        return null;
    }
}
