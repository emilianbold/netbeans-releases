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

    static ClassTree addHttpMethods(WorkingCopy copy, ClassTree innerClass, List<WadlSaasMethod> saasMethods) {
        ClassTree modifiedInnerClass = innerClass;
        TreeMaker maker = copy.getTreeMaker();
        boolean hasRequiredQParams = false;
        boolean hasOptionalQParams = false;
        for (WadlSaasMethod saasMethod : saasMethods) {
            SaasParamsInfo paramsInfo = new SaasParamsInfo(saasMethod);
            if (paramsInfo.hasRequiredParams()) {
                hasRequiredQParams = true;
            }
            if (paramsInfo.hasOptionalParams()) {
                hasOptionalQParams = true;
            }
            List<MethodTree> httpMethods = Wadl2JavaHelper.createHttpMethods(copy, saasMethod, paramsInfo);
            for (MethodTree httpMethod : httpMethods) {
                modifiedInnerClass = maker.addClassMember(modifiedInnerClass, httpMethod);
            }
        }
        if (hasRequiredQParams) {
            // add new private method
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

    static List<MethodTree> createHttpMethods(WorkingCopy copy, WadlSaasMethod saasMethod, SaasParamsInfo paramsInfo) {
        List<MethodTree> httpMethods = new ArrayList<MethodTree>();
        String methodName = saasMethod.getName();
        Method wadlMethod = saasMethod.getWadlMethod();
        String methodType = wadlMethod.getName();
        if (RestConstants.GET_ANNOTATION.equals(methodType)) { //GET
            List<RepresentationType> produces = new ArrayList<RepresentationType>();
            Response wadlResponse = wadlMethod.getResponse();
            if (wadlResponse != null) {
                List<JAXBElement<RepresentationType>> reprOrFaults = wadlMethod.getResponse().getRepresentationOrFault();
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
                            MethodTree method = createHttpGETMethod(copy, saasMethod, mimeType, multipleMimeTypes, paramsInfo);
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
                httpMethods.add(createHttpGETMethod(copy, saasMethod, null, false, paramsInfo));
            }

        } else if ( RestConstants.PUT_ANNOTATION.equals(methodType) ||
                    RestConstants.POST_ANNOTATION.equals(methodType) ||
                    RestConstants.DELETE_ANNOTATION.equals(methodType)
                  ) {
        }
        return httpMethods;
    }

    static MethodTree createHttpGETMethod(WorkingCopy copy, WadlSaasMethod saasMethod, HttpMimeType mimeType, boolean multipleMimeTypes, SaasParamsInfo paramsInfo) {
        String responseType = null;
        String path = "";
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

        boolean hasQueryParams = paramsInfo.hasQueryParams();

        String queryP="";
        StringBuffer queryParamPart=new StringBuffer();
        StringBuffer commentBuffer = new StringBuffer("@param responseType Class representing the response\n"); //NOI18N

        if (hasQueryParams) {
            // add required params
            if (paramsInfo.hasRequiredParams()) {
                for (String requiredParam : paramsInfo.getRequiredParams()) {
                    String javaIdentifier = makeJavaIdentifier(requiredParam);
                    VariableTree paramTree = maker.Variable(paramModifier, javaIdentifier, maker.Identifier("String"), null); //NOI18N
                    paramList.add(paramTree);
                    commentBuffer.append("@param "+javaIdentifier+" query parameter[REQUIRED]\n"); //NOI18N
                }
                ClientJavaSourceHelper.Pair paramPair = getParamList(paramsInfo.getRequiredParams(), paramsInfo.getFixedParams());
                queryParamPart.append("String[] paramNames = new String[] {"+paramPair.getKey()+"}"); //NOI18N
                queryParamPart.append("String[] paramValues = new String[] {"+paramPair.getValue()+"}"); //NOI18N
                queryP += ".queryParams(getQParams(paramNames, paramValues))"; //NOI18N
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
                queryP += ".queryParams(getQParams(optionalQueryParams))"; //NOI18N
            }
        }
        commentBuffer.append("@return response object (an instance of responseType class)"); //NOI18N
        String body =
            "{"+queryParamPart.toString()+ //NOI18N

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

    static class SaasParamsInfo {
        private boolean hasQueryParams = false;
        private boolean hasRequiredParams = false;
        private boolean hasOptionalParams = false;
        private List<String> requiredParams = new ArrayList<String>();
        private List<String> otherParams = new ArrayList<String>();
        private Map<String, String> fixedParams = new HashMap<String,String>();
        private Map<String, String> defaultParams = new HashMap<String,String>();

        SaasParamsInfo(WadlSaasMethod saasMethod) {
            Request request = saasMethod.getWadlMethod().getRequest();
            if (request != null) {
                List<Param> params = request.getParam();
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

    }
}
