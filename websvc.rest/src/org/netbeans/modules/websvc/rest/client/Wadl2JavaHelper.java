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
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.xml.bind.JAXBElement;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.websvc.rest.client.ClientJavaSourceHelper.HttpMimeType;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WadlSaasResource;
import org.netbeans.modules.websvc.saas.model.wadl.Method;
import org.netbeans.modules.websvc.saas.model.wadl.Param;
import org.netbeans.modules.websvc.saas.model.wadl.ParamStyle;
import org.netbeans.modules.websvc.saas.model.wadl.RepresentationType;
import org.netbeans.modules.websvc.saas.model.wadl.Request;
import org.netbeans.modules.websvc.saas.model.wadl.Response;

/**
 *
 * @author mkuchtiak
 */
class Wadl2JavaHelper {

    static ClassTree addHttpMethods(WorkingCopy copy, ClassTree innerClass, WadlSaasResource saasResource) {
        List<WadlSaasMethod> saasMethods = saasResource.getMethods();
        ClassTree modifiedInnerClass = innerClass;
        TreeMaker maker = copy.getTreeMaker();
        boolean hasMultipleRequiredQParams = false;
        boolean hasOptionalQParams = false;
        QueryParamsInfo globalParams = new QueryParamsInfo(saasResource);
        for (WadlSaasMethod saasMethod : saasMethods) {
            QueryParamsInfo paramsInfo = new QueryParamsInfo(saasMethod);
            paramsInfo.merge(globalParams);
            if (paramsInfo.hasMultipleRequiredParams()) {
                hasMultipleRequiredQParams = true;
            }
            if (paramsInfo.hasOptionalParams()) {
                hasOptionalQParams = true;
            }
            List<MethodTree> httpMethods = Wadl2JavaHelper.createHttpMethods(copy, saasMethod, paramsInfo);
            for (MethodTree httpMethod : httpMethods) {
                modifiedInnerClass = maker.addClassMember(modifiedInnerClass, httpMethod);
            }
        }
        if (hasMultipleRequiredQParams) {
            // add new private method to compute MultivaluedMap
            String mvMapClass = "javax.ws.rs.core.MultivaluedMap"; //NOI18N
            TypeElement mvMapEl = copy.getElements().getTypeElement(mvMapClass);
            String mvType = mvMapEl == null ? mvMapClass : "MultivaluedMap"; //NOI18N

            String body =
            "{"+ //NOI18N
                mvType+"<String,String> qParams = new com.sun.jersey.core.util.MultivaluedMapImpl();"+ //NOI18N
                "for (int i=0;i< paramNames.length;i++) {" + //NOI18N
                "    qParams.add(paramNames[i], paramValues[i]);"+ //NOI18N
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
                    "getQParams", //NOI18N
                    returnTree,
                    Collections.<TypeParameterTree>emptyList(),
                    paramList,
                    Collections.<ExpressionTree>emptyList(),
                    body,
                    null); //NOI18N
            modifiedInnerClass = maker.addClassMember(modifiedInnerClass, methodTree);
        }
        if (hasOptionalQParams) {
            // add new private method
            String mvMapClass = "javax.ws.rs.core.MultivaluedMap"; //NOI18N
            TypeElement mvMapEl = copy.getElements().getTypeElement(mvMapClass);
            String mvType = mvMapEl == null ? mvMapClass : "MultivaluedMap"; //NOI18N

            String body =
            "{"+ //NOI18N
                mvType+"<String,String> qParams = new com.sun.jersey.core.util.MultivaluedMapImpl();"+ //NOI18N
               "for (String qParam : optionalParams) {" + //NOI18N
                "    String[] qPar = qParam.split(\"[=:]\");"+ //NOI18N
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

    static List<MethodTree> createHttpMethods(WorkingCopy copy, WadlSaasMethod saasMethod, QueryParamsInfo paramsInfo) {
        List<MethodTree> httpMethods = new ArrayList<MethodTree>();
        String methodName = saasMethod.getName();
        Method wadlMethod = saasMethod.getWadlMethod();
        String methodType = wadlMethod.getName();
        HeaderParamsInfo headerParamsInfo = new HeaderParamsInfo(saasMethod);
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
                            MethodTree method = createHttpGETMethod(copy, saasMethod, mimeType, multipleMimeTypes, paramsInfo, headerParamsInfo);
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
                httpMethods.add(createHttpGETMethod(copy, saasMethod, null, false, paramsInfo, headerParamsInfo));
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
            for (RepresentationType prod : consumes) {
                String mediaType = prod.getMediaType();
                if (mediaType != null) {
                    for (HttpMimeType mimeType : HttpMimeType.values()) {
                        if (mediaType.equals(mimeType.getMimeType())) {
                            MethodTree method = createHttpPOSTMethod(copy, saasMethod, mimeType, multipleMimeTypes, paramsInfo, headerParamsInfo);
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
                httpMethods.add(createHttpPOSTMethod(copy, saasMethod, null, false, paramsInfo, headerParamsInfo));
            }
        }
        return httpMethods;
    }

    static MethodTree createHttpGETMethod(WorkingCopy copy, WadlSaasMethod saasMethod, HttpMimeType mimeType, boolean multipleMimeTypes, QueryParamsInfo paramsInfo, HeaderParamsInfo headerParamsInfo) {
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

        if (paramsInfo.hasQueryParams() || headerParamsInfo.hasHeaderParams()) {
            addQueryAndHeaderParams(maker, paramsInfo, headerParamsInfo, paramList, queryP, queryParamPart, commentBuffer);
        }

        commentBuffer.append("@return response object (instance of responseType class)"); //NOI18N
        String body =
            "{"+queryParamPart+ //NOI18N

                (mimeType == null ?
                    "   return webResource"+queryP+".get("+bodyParam+");" :  //NOI18N
                    "   return webResource"+queryP+".accept("+mimeType.getMediaType()+").get("+bodyParam+");") +  //NOI18N
            "}"; //NOI18N
        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.api.client.UniformInterfaceException"); //NOI18N

        MethodTree method = maker.Method (
                            methodModifier,
                            methodName,
                            responseTree,
                            typeParams,
                            paramList,
                            Collections.singletonList(throwsTree), //throws
                            body,
                            null);
        if (method != null) {
            Comment comment = Comment.create(Style.JAVADOC, commentBuffer.toString());
            maker.addComment(method, comment, true);
        }
        return method;
    }

    static MethodTree createHttpPOSTMethod(WorkingCopy copy, WadlSaasMethod saasMethod, HttpMimeType requestMimeType, boolean multipleMimeTypes, QueryParamsInfo paramsInfo, HeaderParamsInfo headerParamsInfo) {
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

        if (paramsInfo.hasQueryParams() || headerParamsInfo.hasHeaderParams()) {
            addQueryAndHeaderParams(maker, paramsInfo, headerParamsInfo, paramList, queryP, queryParamPart, commentBuffer);
        }

        if (requestMimeType != null) {
            if (requestMimeType == HttpMimeType.FORM) {
                // PENDING
            } else {
                VariableTree objectParam = maker.Variable(paramModifier, "requestEntity", maker.Identifier("Object"), null); //NOI18N
                paramList.add(objectParam);
                bodyParam2=(bodyParam1.length() > 0 ? ", " : "") + "requestEntity"; //NOI18N
                commentBuffer.append("@param requestEntity request data");
            }
        }

        commentBuffer.append("@return response object (instance of responseType class)"); //NOI18N

        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.api.client.UniformInterfaceException"); //NOI18N

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
                Collections.singletonList(throwsTree),
                body,
                null); //NOI18N
        if (method != null) {
            Comment comment = Comment.create(Style.JAVADOC, commentBuffer.toString());
            maker.addComment(method, comment, true);
        }
        return method;
    }


    private static void addQueryAndHeaderParams(TreeMaker maker, QueryParamsInfo paramsInfo, HeaderParamsInfo headerParamsInfo,  List<VariableTree> paramList, StringBuffer queryP, StringBuffer queryParamPart, StringBuffer commentBuffer) {
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());
        if (paramsInfo.hasQueryParams()) {
            // add required params
            if (paramsInfo.hasRequiredParams()) {
                for (String requiredParam : paramsInfo.getRequiredParams()) {
                    String javaIdentifier = makeJavaIdentifier(requiredParam);
                    VariableTree paramTree = maker.Variable(paramModifier, javaIdentifier, maker.Identifier("String"), null); //NOI18N
                    paramList.add(paramTree);
                    commentBuffer.append("@param "+javaIdentifier+" query parameter[REQUIRED]\n"); //NOI18N
                }
                if (paramsInfo.hasMultipleRequiredParams()) {
                    ClientJavaSourceHelper.Pair paramPair = getParamList(paramsInfo.getRequiredParams(), paramsInfo.getFixedParams());
                    queryParamPart.append("String[] paramNames = new String[] {"+paramPair.getKey()+"}"); //NOI18N
                    queryParamPart.append("String[] paramValues = new String[] {"+paramPair.getValue()+"}"); //NOI18N
                    queryP.append(".queryParams(getQParams(paramNames, paramValues))"); //NOI18N
                } else {
                    List<String> requiredParams = paramsInfo.getRequiredParams();
                    if (requiredParams.size() > 0) {
                        String paramName = requiredParams.get(0);
                        String paramValue = makeJavaIdentifier(requiredParams.get(0));
                        queryP.append(".queryParam(\""+paramName+"\","+paramValue+")"); //NOI18N"
                    } else {
                        Map<String, String> fixedParams = paramsInfo.getFixedParams();
                        for (String paramName : fixedParams.keySet()) {
                            String paramValue = fixedParams.get(paramName);
                            queryP.append(".queryParam(\""+paramName+"\",\""+paramValue+"\")"); //NOI18N"
                        }
                    }
                }
            }
            // add optional params
            if (paramsInfo.hasOptionalParams()) {
                VariableTree paramTree = maker.Variable(paramModifier, "optionalQueryParams", maker.Identifier("String..."), null); //NOI18N
                paramList.add(paramTree);

                commentBuffer.append("@param optionalQueryParams List of optional query parameters in the form of \"param_name=param_value\",...<br>\nList of optional query parameters:\n"); //NOI18N
                for (String otherParam : paramsInfo.getOtherParams()) {
                    commentBuffer.append("<LI>"+otherParam+" [OPTIONAL]\n"); //NOI18N
                }
                Map<String,String> defaultParams = paramsInfo.getDefaultParams();
                for (String key : defaultParams.keySet()) {
                    commentBuffer.append("<LI>"+key+" [OPTIONAL, DEFAULT VALUE: \""+defaultParams.get(key)+"\"]\n"); //NOI18N
                }
                queryP.append(".queryParams(getQParams(optionalQueryParams))"); //NOI18N
            }
        }
        // add header params
        for (String headerParam : headerParamsInfo.getHeaderParams()) {

            String javaIdentifier = makeJavaIdentifier(headerParam);
            VariableTree paramTree = maker.Variable(paramModifier, javaIdentifier, maker.Identifier("String"), null); //NOI18N
            paramList.add(paramTree);
            commentBuffer.append("@param "+javaIdentifier+" header parameter[REQUIRED]\n"); //NOI18N
            queryP.append(".header(\""+headerParam+"\","+javaIdentifier+")"); //NOI18N
        }
    }

    private static ClientJavaSourceHelper.Pair getParamList(List<String> requiredParams, Map<String,String> fixedParams) {
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

        return new ClientJavaSourceHelper.Pair(paramNames.toString(),paramValues.toString());
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
        }
        return result;
    }

    static class HeaderParamsInfo {
        private boolean hasHeaderParams = false;
        private List<String> headerParams = new ArrayList<String>();
         HeaderParamsInfo(WadlSaasResource saasResource) {
            initParams(saasResource.getResource().getParam());
        }

        HeaderParamsInfo(WadlSaasMethod saasMethod) {
            Request request = saasMethod.getWadlMethod().getRequest();
            if (request != null) {
                initParams(request.getParam());
            }
        }

        private void initParams (List<Param> params) {
             for (Param param : params) {
                if (ParamStyle.HEADER == param.getStyle()) {
                    hasHeaderParams = true;
                    headerParams.add(param.getName());
                }
            }
        }
        boolean hasHeaderParams() {
            return hasHeaderParams;
        }

        List<String> getHeaderParams() {
            return headerParams;
        }
    }


    static class QueryParamsInfo {
        private boolean hasQueryParams = false;
        private boolean hasRequiredParams = false;
        private boolean hasOptionalParams = false;
        private List<String> requiredParams = new ArrayList<String>();
        private List<String> otherParams = new ArrayList<String>();
        private Map<String, String> fixedParams = new HashMap<String,String>();
        private Map<String, String> defaultParams = new HashMap<String,String>();

        QueryParamsInfo(WadlSaasResource saasResource) {
            initParams(saasResource.getResource().getParam());
        }

        QueryParamsInfo(WadlSaasMethod saasMethod) {
            Request request = saasMethod.getWadlMethod().getRequest();
            if (request != null) {
                initParams(request.getParam());
            }
        }

        private void initParams (List<Param> params) {
             for (Param param : params) {
                if (ParamStyle.QUERY == param.getStyle()) {
                    hasQueryParams = true;
                    if (param.isRequired() && param.getFixed() == null && param.getDefault() == null) {
                        hasRequiredParams = true;
                        requiredParams.add(param.getName());
                    } else if (param.getFixed() != null) {
                        hasRequiredParams = true;
                        fixedParams.put(param.getName(), param.getFixed());
                    } else if (param.getDefault() != null) {
                        hasOptionalParams = true;
                        defaultParams.put(param.getName(), param.getDefault());
                    } else {
                        hasOptionalParams = true;
                        otherParams.add(param.getName());
                    }
                }
            }
        }

        boolean hasMultipleRequiredParams() {
            return hasRequiredParams && requiredParams.size() + fixedParams.size() > 1;
        }

        boolean hasQueryParams() {
            return hasQueryParams;
        }
        boolean hasRequiredParams() {
            return hasRequiredParams;
        }
        boolean hasOptionalParams() {
            return hasOptionalParams;
        }

        Map<String, String> getDefaultParams() {
            return defaultParams;
        }

        Map<String, String> getFixedParams() {
            return fixedParams;
        }

        List<String> getOtherParams() {
            return otherParams;
        }

        List<String> getRequiredParams() {
            return requiredParams;
        }

        void merge(QueryParamsInfo qp) {
            hasQueryParams = hasQueryParams || qp.hasQueryParams;
            hasRequiredParams = hasRequiredParams || qp.hasRequiredParams;
            hasOptionalParams = hasOptionalParams || qp.hasOptionalParams;
            requiredParams.addAll(qp.requiredParams);
            otherParams.addAll(qp.otherParams);
            fixedParams.putAll(qp.fixedParams);
            defaultParams.putAll(qp.defaultParams);
        }

    }
}
