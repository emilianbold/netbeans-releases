/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.websvc.rest.codegen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.websvc.rest.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.rest.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.rest.codegen.model.GenericResourceBean;
import org.netbeans.modules.websvc.rest.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.support.AbstractTask;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Code generator for plain REST resource class.
 * The generator takes as paramenters:
 *  - target directory
 *  - REST resource bean meta model.
 *
 * @author nam
 */
public class GenericResourceGenerator extends AbstractGenerator {
    public static final String RESOURCE_TEMPLATE = "Templates/WebServices/GenericResource.java"; //NOI18N
    public static final String RESOURCE_ITEM_TEMPLATE = "Templates/WebServices/GenericItemResource.java"; //NOI18N
    public static final String COMMENT_END_OF_GET = "TODO return proper representation object";
    private static final String FIELD_LIST="field_list"; //NOI18N
    private static final String PARAM_LIST="param_list"; //NOI18N
    private static final String ASSIGNMENT_LIST="assignment_list"; //NOI18N
    private static final String ARGUMENT_LIST="argument_list"; //NOI18N
    
    private final FileObject destDir;
    private final GenericResourceBean bean;
    private String template;
    
    public GenericResourceGenerator(FileObject destDir, GenericResourceBean bean) {
        this.destDir = destDir;
        this.bean = bean;
        if (bean.isRootResource()) {
            this.template = RESOURCE_TEMPLATE;
        } else {
            this.template = RESOURCE_ITEM_TEMPLATE;
        }
    }
    
    public FileObject getDestDir() {
        return destDir;
    }
    
    public GenericResourceBean getBean() {
        return bean;
    }
    
    public String getTemplate() {
        return template;
    }
    
    public void setTemplate(String templatePath) {
        template = templatePath;
    }
    
    @Override
    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        initProgressReporting(pHandle, false);
        
        reportProgress(NbBundle.getMessage(GenericResourceGenerator.class,
                "MSG_GeneratingClass", bean.getPackageName() + "." + bean.getName()));  //NOI18N
        JavaSource source;
        if (bean.isRootResource()) {
            source = JavaSourceHelper.createJavaSource(
                    getTemplate(), getDestDir(), bean.getPackageName(), bean.getName());
        } else {
            Map<String,String> params = new HashMap<String,String>();
            String[] uriParams = bean.getUriParams();
            StringBuilder fieldList = new StringBuilder();
            StringBuilder paramList = new StringBuilder();
            StringBuilder assignmentList = new StringBuilder();
            StringBuilder argumentList = new StringBuilder();
            for (int i=0; i<uriParams.length; i++) {
                String param = uriParams[i];
                if (i == 0) {
                    fieldList.append("private String "); //NOI18N
                } else {
                    fieldList.append(", "); //NOI18N
                    paramList.append(", "); //NOI18N
                    argumentList.append(", "); //NOI18N
                    assignmentList.append(" "); //NOI18N
                }
                fieldList.append(param);
                argumentList.append(param);
                paramList.append("String ").append(param); //NOI18N
                assignmentList.append("this.").append(param).append("=").append(param).append(";"); //NOI18N
            }
            if (fieldList.length() > 0) {
                fieldList.append(";");  //NOI18N
            }
            params.put(FIELD_LIST, fieldList.toString());
            params.put(PARAM_LIST, paramList.toString());
            params.put(ASSIGNMENT_LIST, assignmentList.toString());
            params.put(ARGUMENT_LIST, argumentList.toString());
            source = JavaSourceHelper.createJavaSource(
                    getTemplate(), params, getDestDir(), bean.getPackageName(), bean.getName());
        }
        if (bean.getInputParameters().size() > 0) {
            addInputParamFields(source);
            addConstructorWithInputParams(source);
        }
        
        modifyResourceClass(source);
        return new HashSet<FileObject>(source.getFileObjects());
    }
  
    private void addInputParamFields(JavaSource source) throws IOException {
        ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {
            @Override
            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                List<ParameterInfo> params = bean.getInputParameters();
                
                JavaSourceHelper.addFields(copy, getParamNames(params),
                        getParamTypeNames(params), getParamValues(params));
            }
        });
        result.commit();
    }
    
     private void addConstructorWithInputParams(JavaSource source) throws IOException {
        ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {
            @Override
            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                List<ParameterInfo> params = bean.getInputParameters();
                StringBuilder body = new StringBuilder("{");      //NOI18N
                
                for (ParameterInfo param : params) {
                    String name = param.getName();
                    body.append( "if (" );                  //NOI18N
                    body.append( name );
                    body.append( " != null) { this.");      //NOI18N
                    body.append(name );
                    body.append(" = " );		    //NOI18N
                    body.append( name );
                    body.append("; }\n");                   //NOI18N
                }
		body.append("}\n");			    //NOI18N
                
                ClassTree modifiedTree = JavaSourceHelper.addConstructor(copy, tree,
                        Constants.PUBLIC,
                        getParamNames(params), getParamTypeNames(params),
                        body.toString(), null);
                
                copy.rewrite(tree, modifiedTree);
            }
        });
        result.commit();
    }
      
    private void modifyResourceClass(JavaSource source) {
        try {
            ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {
                @Override
                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);
                    String jsr311Imports[] = getJsr311AnnotationImports(bean);
                    String imports[] = jsr311Imports; 
                    boolean cdiEnabled = Util.isCDIEnabled(getDestDir());
                    if ( cdiEnabled ){
                        imports  = new String[jsr311Imports.length+1];
                        System.arraycopy(jsr311Imports, 0, imports, 0, jsr311Imports.length);
                        imports[jsr311Imports.length] = Constants.FQN_REQUESTED_SCOPE;
                    }
                    JavaSourceHelper.addImports(copy, imports);
                    List<String> annotations= new ArrayList<String>(2);
                    List<Object> annotationAttributes = new ArrayList<Object>(2); 
                    if (bean.isGenerateUriTemplate()) {
                        annotations.add(RestConstants.PATH_ANNOTATION);
                        annotationAttributes.add(bean.getUriTemplate());
                    }
                    if ( cdiEnabled){
                        annotations.add( Constants.REQUESTED_SCOPE);
                        annotationAttributes.add(null);
                    }
                    if ( annotations.size() >0 ){
                        JavaSourceHelper.addClassAnnotation(copy,
                                annotations.toArray( new String[annotations.size()]) ,
                                annotationAttributes.toArray( 
                                        new Object[annotationAttributes.size()]));
                    }
                    ClassTree initial = JavaSourceHelper.getTopLevelClassTree(copy);
                    ClassTree tree = addMethods(copy, initial);
                    
                    for (GenericResourceBean subBean : bean.getSubResources()) {
                        tree = addSubResourceLocatorMethod(copy, tree, subBean);
                    }
                    
                    copy.rewrite(initial, tree);
                }}
            );
            result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static String[] getJsr311AnnotationImports(GenericResourceBean rbean) {
        HashSet<String> result = new HashSet<String>();
        if (rbean.isGenerateUriTemplate()) {
            result.add(RestConstants.PATH);
        }
        if (rbean.isRootResource() && !rbean.getSubResources().isEmpty()) {
            result.add(RestConstants.PATH_PARAM);
        }
        for (HttpMethodType m : rbean.getMethodTypes()) {
            result.add(m.getAnnotationType());
            if (m == HttpMethodType.GET) {
                result.add(RestConstants.PRODUCE_MIME);
            }
            if (m == HttpMethodType.POST || m == HttpMethodType.PUT) {
                result.add(RestConstants.CONSUME_MIME);
            }
        }
        if (rbean.getQueryParameters().size() > 0) {
            result.add(RestConstants.QUERY_PARAM);
        }
        return result.toArray(new String[result.size()]);
    }
    
    protected ClassTree addMethods(WorkingCopy copy, ClassTree tree) {
        MimeType[] mimes = bean.getMimeTypes();
        String[] types = bean.getRepresentationTypes();
        for (int i=0; i<mimes.length; i++) {
            MimeType mime = mimes[i];
            String type = types[i];
            tree = addGetMethod(mime, type, copy, tree);
            
            if (bean.getMethodTypes().contains(HttpMethodType.POST)) {
                GenericResourceBean subBean = getSubresourceBean();
                if (subBean == null) {
                    tree = addPostMethod(mime, type, copy, tree);
                } else {
                    String[] subBeanTypes = subBean.getRepresentationTypes();
                        if (subBeanTypes != null && subBeanTypes.length>0) {
                        tree = addPostMethod(mime, subBeanTypes[0], copy, tree);
                    } else {
                        tree = addPostMethod(mime, "String", copy, tree); //NOI18N
                    }
                }
            }
            
            if (bean.getMethodTypes().contains(HttpMethodType.PUT)) {
                tree = addPutMethod(mime, type, copy, tree);
            }
            
        }
        
        if (bean.getMethodTypes().contains(HttpMethodType.DELETE)) {
            tree = addDeleteMethod(copy, tree);
        }
        return tree;
    }
    
    private ClassTree addGetMethod(MimeType mime, String type, WorkingCopy copy, ClassTree tree) {
        Modifier[] modifiers = Constants.PUBLIC;
        
        String[] annotations = new String[] {
            RestConstants.GET_ANNOTATION,
            RestConstants.PRODUCE_MIME_ANNOTATION};
        
        Object[] annotationAttrs = new Object[] {
            null,
            mime.expressionTree(copy.getTreeMaker())
        };
        
        if (type == null) {
            type = String.class.getName();
        }
        String bodyText = "{ //"+COMMENT_END_OF_GET+"\n";
        bodyText += "throw new UnsupportedOperationException(); }";
        
        List<ParameterInfo> queryParams = bean.getQueryParameters();
        String[] parameters = getGetParamNames(queryParams);
        Object[] paramTypes = getGetParamTypes(queryParams);
        String[][] paramAnnotations = getGetParamAnnotations(queryParams);
        Object[][] paramAnnotationAttrs = getGetParamAnnotationAttrs(queryParams);
        
        StringBuilder comment = new StringBuilder("Retrieves representation of an instance of ");
        comment.append( bean.getQualifiedClassName());
        comment.append( "\n");
        for (String param : parameters) {
            comment.append( "@param ");
            comment.append(param);
            comment.append(" resource URI parameter\n");
        }
        comment.append( "@return an instance of ");
        comment.append( type);
        
        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                getMethodName(HttpMethodType.GET, mime), type, parameters, paramTypes,
                paramAnnotations, paramAnnotationAttrs,
                bodyText, comment.toString());      
    }
    
    private ClassTree addPostMethod(MimeType mime, String type, WorkingCopy copy, ClassTree tree) {
        Modifier[] modifiers = Constants.PUBLIC;
        
        String[] annotations = new String[] {
            RestConstants.POST_ANNOTATION,
            RestConstants.CONSUME_MIME_ANNOTATION,
            RestConstants.PRODUCE_MIME_ANNOTATION
        };

        ExpressionTree mimeTree = mime.expressionTree(copy.getTreeMaker());
        Object[] annotationAttrs = new Object[] {
            null,
            mimeTree,
            mimeTree
        };

        String bodyText = "{ //TODO\n return Response.created(context.getAbsolutePath()).build(); }"; //NOI18N
        String[] parameters = getPostPutParams();
        Object[] paramTypes = getPostPutParamTypes(type);
        if (type != null) {
            paramTypes[paramTypes.length-1] = type;
        }
        String[] paramAnnotations = getParamAnnotations(parameters.length);
        Object[] paramAnnotationAttrs = getParamAnnotationAttributes(parameters.length);

        GenericResourceBean subBean = getSubresourceBean();
        StringBuilder comment = new StringBuilder("POST method for creating an instance of ");
        comment.append(subBean == null ? bean.getName() : subBean.getName());
        comment.append( "\n");
        for (int i=0; i<parameters.length-1; i++) {
            comment.append("@param ");
            comment.append(parameters[i]);
            comment.append(" resource URI parameter\n");
        }
        comment.append( "@param ");
        comment.append( parameters[parameters.length-1]);
        comment.append( " representation for the new resource\n");
        comment.append( "@return an HTTP response with content of the created resource");
        
        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                getMethodName(HttpMethodType.POST, mime), RestConstants.HTTP_RESPONSE,
                parameters, paramTypes, paramAnnotations, paramAnnotationAttrs,
                bodyText, comment.toString());
    }
    
    private ClassTree addPutMethod(MimeType mime, String type, WorkingCopy copy, ClassTree tree) {
        Modifier[] modifiers = Constants.PUBLIC;
        
        String[] annotations = new String[] {
            RestConstants.PUT_ANNOTATION,
            RestConstants.CONSUME_MIME_ANNOTATION
        };

        ExpressionTree mimeTree = mime.expressionTree(copy.getTreeMaker());
        Object[] annotationAttrs = new Object[] {
            null,
            mimeTree,
            mimeTree
        };
        Object returnType = Constants.VOID;
        String bodyText = "{ //TODO }";    //NOI18N

        String[] parameters = getPostPutParams();
        Object[] paramTypes = getPostPutParamTypes(type);
        if (type != null) {
            paramTypes[paramTypes.length-1] = type;
        }
        String[] paramAnnotations = getParamAnnotations(parameters.length);
        Object[] paramAnnotationAttrs = getParamAnnotationAttributes(parameters.length);

        StringBuilder comment = new StringBuilder("PUT method for updating or creating an instance of ");
        comment.append( bean.getName());
        comment.append(  "\n");
        for (int i=0; i<parameters.length-1; i++) {
            comment.append("@param ");
            comment.append(parameters[i]);
            comment.append(" resource URI parameter\n");
        }
        comment.append( "@param ");
        comment.append(parameters[parameters.length-1]);
        comment.append(" representation for the resource");
        
        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                getMethodName(HttpMethodType.PUT, mime), returnType,
                parameters, paramTypes, paramAnnotations, paramAnnotationAttrs,
                bodyText, comment.toString());
    }
    
    private ClassTree addDeleteMethod(WorkingCopy copy, ClassTree tree) {
        Modifier[] modifiers = Constants.PUBLIC;
        
        String[] annotations = new String[] {
            RestConstants.DELETE_ANNOTATION,
        };
        
        Object[] annotationAttrs = new Object[] { null };
        
        Object returnType = Constants.VOID;
        String bodyText = "{ //TODO implement }";
        
//        String[] parameters = bean.getUriParams();
//        Object[] paramTypes = getUriParamTypes();
//        String[] paramAnnotations = getParamAnnotations(parameters.length);
//        Object[] paramAnnotationAttrs = getParamAnnotationAttributes(parameters.length);
        
        String comment = "DELETE method for resource " + bean.getName() + "\n";
//        for (String param : parameters) {
//            comment += "@param $PARAM$ resource URI parameter\n".replace("$PARAM$", param);
//        }
        
        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                "delete", returnType, null, null, //NOI18N
                null, null,
                bodyText, comment);      //NOI18N
    }
    
    private ClassTree addSubResourceLocatorMethod(WorkingCopy copy, ClassTree tree, GenericResourceBean subBean) {
        Modifier[] modifiers = Constants.PUBLIC;
        String methodName = "get" + subBean.getName();  //NOI18N
        
        String[] annotations = new String[] {
            RestConstants.PATH_ANNOTATION
        };

        String uriTemplate = subBean.getUriTemplate();

        Object[] annotationAttrs = new Object[] {uriTemplate};
        Object returnType = subBean.getName();

        String[] parameters = subBean.getUriParams();
        Object[] paramTypes = null;
        String[] paramAnnotations = null;
        Object[] paramAnnotationAttrs = null;
        StringBuilder params = new StringBuilder();
        if (parameters != null && parameters.length >= 1) {
            paramTypes = getUriParamTypes(subBean);
            
            paramAnnotations = new String[parameters.length];
            paramAnnotationAttrs = new Object[parameters.length];
            for (int i=0; i<parameters.length; i++) {
                if (i != 0) {
                    params.append(",");
                }
                params.append(parameters[i]);
                paramAnnotations[i] = RestConstants.PATH_PARAM_ANNOTATION;
                paramAnnotationAttrs[i] = parameters[i];
            }
        }

        String bodyText = "{ return " + returnType + ".getInstance("+params.toString()+"); }"; //NOI18N
        
        String comment = "Sub-resource locator method for " + uriTemplate + "\n";
        
        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                methodName, returnType, parameters, paramTypes, paramAnnotations, paramAnnotationAttrs,
                bodyText, comment);
    }
    
    public String getPostPutMethodBodyText(MimeType mimeType) {
        return "{//TODO \n return new HttpResponse(201, new Representation(content, \""+ mimeType.value() +"\"); }"; //NOI18N
    }
    
    private String[] getUriParamTypes() {
        return getUriParamTypes(bean);
    }
    
    public static String[] getUriParamTypes(GenericResourceBean bean) {
        String defaultType = String.class.getName();
        String[] types = new String[bean.getUriParams().length];
        for (int i=0; i < types.length; i++) {
            types[i] = defaultType;
        }
        return types;
    }
    
    private String[] getGetParamNames(List<ParameterInfo> queryParams) {
        ArrayList<String> params = new ArrayList<String>();
        if (bean.isRootResource()) {
            params.addAll(Arrays.asList(bean.getUriParams()));
        }
        params.addAll(Arrays.asList(getParamNames(queryParams)));
        return params.toArray(new String[params.size()]);
    }
    
    private String[] getGetParamTypes(List<ParameterInfo> queryParams) {
        ArrayList<String> types = new ArrayList<String>();
        if (bean.isRootResource()) {
            types.addAll(Arrays.asList(getUriParamTypes()));
        }
        types.addAll(Arrays.asList(getParamTypeNames(queryParams)));
        return types.toArray(new String[types.size()]);
    }
    
    private Object[] getParamAnnotationAttributes(int allParamCount) {
        String[] uriParams = bean.getUriParams();
        int uriParamCount = bean.isRootResource() ? uriParams.length : 0;
        if (allParamCount < uriParamCount) {
            throw new IllegalArgumentException("allParamCount="+allParamCount);
        }

        String[] attrs = new String [allParamCount];
        System.arraycopy(uriParams, 0, attrs, 0, uriParamCount);
        return attrs;
    }
    
    private String[] getParamAnnotations(int allParamCount) {
        int uriParamCount = bean.isRootResource() ? bean.getUriParams().length : 0;
        if (allParamCount < uriParamCount) {
            throw new IllegalArgumentException("allParamCount="+allParamCount);
        }
        String[] annos = new String [allParamCount];
        for (int i=0; i<uriParamCount; i++) {
            annos[i] = RestConstants.PATH_PARAM_ANNOTATION;
        }
        for (int i=uriParamCount; i<allParamCount; i++) {
            annos[i] = null;
        }
        return annos;
    }
   
    private String[][] getGetParamAnnotations(List<ParameterInfo> queryParams) {
        ArrayList<String[]> annos = new ArrayList<String[]>();

        if (bean.isRootResource()) {
            for (String uriParam : bean.getUriParams()) {
                annos.add(new String[] {RestConstants.PATH_PARAM_ANNOTATION});
            }
        }
        
        for (ParameterInfo param : queryParams) {
            String[] annotations;
            if (param.getDefaultValue() != null) {
                annotations = new String[] {
                    RestConstants.QUERY_PARAM_ANNOTATION,
                       RestConstants.DEFAULT_VALUE_ANNOTATION
                };
            } else {
                annotations = new String[] {RestConstants.QUERY_PARAM_ANNOTATION};
            }
            annos.add(annotations);
        }
    
        return annos.toArray(new String[annos.size()][]);
    }
    
    private Object[][] getGetParamAnnotationAttrs(List<ParameterInfo> queryParams) {
        ArrayList<Object[]> attrs = new ArrayList<Object[]>();
        if (bean.isRootResource()) {
            for (String uriParam : bean.getUriParams()) {
                attrs.add(new Object[] {uriParam});
            }
        }
      
        for (ParameterInfo param : queryParams) {
            Object[] annotationAttrs;
            if (param.getDefaultValue() != null) {
                annotationAttrs = new Object[] {
                    param.getName(), param.getDefaultValue().toString()
                };
            } else {
                annotationAttrs = new Object[] {param.getName()};
            }
            attrs.add(annotationAttrs);
        }
    
        return attrs.toArray(new Object[attrs.size()][]);
    }
    
    private String[] getPostPutParams() {
        if (bean.isRootResource()) {
            List<String> params = new ArrayList<String>(Arrays.asList(bean.getUriParams()));
            params.add("content");  // NOI18N
            return params.toArray(new String[params.size()]);
        } else {
            return new String[] {"content"}; //NOI18N
        }
    }
    
    private String[] getPostPutParamTypes(String representatinType) {
        String defaultType = String.class.getName();
        int typesLength = 1;
        if (bean.isRootResource()) {
            typesLength = bean.getUriParams().length + 1;
        }
        String[] types = new String[typesLength];
        for (int i=0; i < types.length -1; i++) {
            types[i] = defaultType;
        }
        types[types.length-1] = representatinType;
        return types;
    }
    
    
    private String getMethodName(HttpMethodType methodType, MimeType mime) {
        return methodType.prefix() + mime.suffix();
    }
    
    public static String getNounForMethodName(HttpMethodType type) {
        String name = type.toString().toLowerCase();
        StringBuilder sb = new StringBuilder(name);
        sb.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        return sb.toString();
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
    
    private Object[] getParamValues(List<ParameterInfo> params) {
        List<Object> results = new ArrayList<Object>();
        
        for (ParameterInfo param : params) {
            Object defaultValue = null;
            
            if (!param.isQueryParam()) {
                defaultValue = param.getDefaultValue();
            }
            
            results.add(defaultValue);
        }
        
        return results.toArray(new Object[results.size()]);
    }

    private GenericResourceBean getSubresourceBean() {
        List<GenericResourceBean> subResources = bean.getSubResources();
        if (subResources.size() > 0) {
            return subResources.get(0);
        }
        return null;
    }
}
