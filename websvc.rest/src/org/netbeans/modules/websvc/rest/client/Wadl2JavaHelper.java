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


    static List<MethodTree> createHttpMethods(WorkingCopy copy, WadlSaasMethod saasMethod) {
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
                    if ("representation".equals(reprOrFault.getName().getLocalPart())) {
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
                            MethodTree method = createHttpGETMethod(copy, saasMethod, mimeType, multipleMimeTypes);
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
                httpMethods.add(createHttpGETMethod(copy, saasMethod, null, false));
            }

        } else if ( RestConstants.PUT_ANNOTATION.equals(methodType) ||
                    RestConstants.POST_ANNOTATION.equals(methodType) ||
                    RestConstants.DELETE_ANNOTATION.equals(methodType)
                  ) {
        }
        return httpMethods;
    }

    static MethodTree createHttpGETMethod(WorkingCopy copy, WadlSaasMethod saasMethod, HttpMimeType mimeType, boolean multipleMimeTypes) {
        String responseType = null;
        String path = "";
        String methodName = saasMethod.getName() + (multipleMimeTypes ? "_"+mimeType.name() : ""); //NOI18N
        System.out.println("methodName = "+methodName+":"+mimeType);

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
        Request request = saasMethod.getWadlMethod().getRequest();
        List<String> requiredParams = new ArrayList<String>();
        List<String> otherParams = new ArrayList<String>();
        Map<String, String> fixedParams = new HashMap<String,String>();
        Map<String, String> defaultParams = new HashMap<String,String>();

        String queryP="";
        StringBuffer queryParamPart=new StringBuffer();
        StringBuffer commentBuffer = new StringBuffer("@param responseType Class representing the response\n");
        if (request != null) {
            boolean hasQueryParams = false;
            boolean hasOptionalParams = false;
            List<Param> params = request.getParam();
            for (Param param : params) {
                if (ParamStyle.QUERY == param.getStyle()) {
                    hasQueryParams = true;
                    if (param.isRequired() && param.getFixed() == null && param.getDefault() == null) {
                        requiredParams.add(param.getName());
                    } else if (param.getFixed() != null) {
                        fixedParams.put(param.getName(), param.getFixed());
                    } else if (param.getDefault() != null) {
                        defaultParams.put(param.getName(), param.getDefault());
                    } else {
                        otherParams.add(param.getName());
                    }
                }
            }

            // add required params
            for (String requiredParam : requiredParams) {
                VariableTree paramTree = maker.Variable(paramModifier, requiredParam, maker.Identifier("String"), null); //NOI18N
                paramList.add(paramTree);
                commentBuffer.append("@param "+requiredParam+" Query parameter(required)\n");
            }
            if (defaultParams.size()+otherParams.size() > 0) {
                hasOptionalParams = true;
                VariableTree paramTree = maker.Variable(paramModifier, "optionalQueryParams", maker.Identifier("String..."), null); //NOI18N
                paramList.add(paramTree);
                commentBuffer.append("@param optionalQueryParams List of optional query parameters in the form of \"param_name=param_value\",...\n");
            }
            if (hasQueryParams) {
                queryParamPart.append("javax.ws.rs.core.MultivaluedMap<String, String> qParams = new com.sun.jersey.core.util.MultivaluedMapImpl();");
                for (String requiredParam : requiredParams) {
                    queryParamPart.append("qParams.add(\""+requiredParam+"\","+requiredParam+");");
                }
                for (String fixedParam : fixedParams.keySet()) {
                    queryParamPart.append("qParams.add(\""+fixedParam+"\",\""+fixedParams.get(fixedParam)+"\");");
                }
                if (hasOptionalParams) {
                    queryParamPart.append("for (String qParam : optionalQueryParams) {");
                    queryParamPart.append("String[] qPar = qParam.split(\"[=:]\");");
                    queryParamPart.append("qParams.add(qPar[0], qPar[1]);");
                    queryParamPart.append("}");
                    for (String otherParam : otherParams) {
                        commentBuffer.append("<LI>"+otherParam+" Query parameter(optional)\n");
                    }
                    for (String key : defaultParams.keySet()) {
                        commentBuffer.append("<LI>"+key+" Query parameter(optional, default value["+defaultParams.get(key)+"])\n");
                    }
                }
                queryP = ".queryParams(qParams)"; //NOI18N
            }
        }
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
}
/*
        String responseType = httpMethod.getReturnType();
        String path = httpMethod.getPath();
        String methodName = httpMethod.getName() + (multipleMimeTypes ? "_"+mimeType.name() : ""); //NOI18N

        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree methodModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC));
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());

        VariableTree classParam = null;
        ExpressionTree responseTree = null;
        String bodyParam = ""; //NOI18N
        List<TypeParameterTree> typeParams =  null;

        if ("java.lang.String".equals(responseType)) { //NOI18N
            responseTree = maker.Identifier("String"); //NOI18N
            bodyParam="String.class"; //NOI18N
            typeParams =  Collections.<TypeParameterTree>emptyList();
        } else {
            responseTree = maker.Identifier("T"); //NOI18N
            bodyParam="responseType"; //NOI18N
            classParam = maker.Variable(paramModifier, "responseType", maker.Identifier("Class<T>"), null); //NOI18N
            typeParams = Collections.<TypeParameterTree>singletonList(maker.TypeParameter("T", Collections.<ExpressionTree>emptyList())); //NOI18N
        }

        List<VariableTree> paramList = new ArrayList<VariableTree>();
        if (classParam != null) {
            paramList.add(classParam);
        }

        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.api.client.UniformInterfaceException"); //NOI18N

        if (path.length() == 0) {
            String body =
                "{"+ //NOI18N
                    (mimeType == null ?
                        "   return webResource.get("+bodyParam+");" :  //NOI18N
                        "   return webResource.accept("+mimeType.getMediaType()+").get("+bodyParam+");") +  //NOI18N
                "}"; //NOI18N
            return maker.Method (
                    methodModifier,
                    methodName,
                    responseTree,
                    typeParams,
                    paramList,
                    Collections.singletonList(throwsTree), //throws
                    body,
                    null);
        } else {
            PathFormat pf = getPathFormat(path);
            for (String arg : pf.getArguments()) {
                Tree typeTree = maker.Identifier("String"); //NOI18N
                ModifiersTree fieldModifier = maker.Modifiers(Collections.<Modifier>emptySet());
                VariableTree fieldTree = maker.Variable(fieldModifier, arg, typeTree, null); //NOI18N
                paramList.add(fieldTree);
            }
            String body =
                    "{"+ //NOI18N
                        (mimeType == null ?
                            "   return webResource.path("+getPathExpression(pf)+").get("+bodyParam+");" :  //NOI18N
                            "   return webResource.path("+getPathExpression(pf)+").accept("+mimeType.getMediaType()+").get("+bodyParam+");") +  //NOI18N
                    "}"; //NOI18N
            return maker.Method (
                    methodModifier,
                    methodName,
                    responseTree,
                    typeParams,
                    paramList,
                    Collections.<ExpressionTree>singletonList(throwsTree), //throws
                    body,
                    null); //NOI18N
        }
 */