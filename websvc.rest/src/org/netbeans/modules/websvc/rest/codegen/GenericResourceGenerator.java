/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.rest.codegen;

import com.sun.source.tree.ClassTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.websvc.rest.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.rest.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.rest.codegen.model.GenericResourceBean;
import org.netbeans.modules.websvc.rest.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.rest.support.AbstractTask;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
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
    public static final String COMMENT_END_OF_GET = "TODO return proper representatin object";
    
    private FileObject destDir;
    private GenericResourceBean bean;
    private String template;
    
    public GenericResourceGenerator(FileObject destDir, GenericResourceBean bean) {
        this.destDir = destDir;
        this.bean = bean;
        this.template = RESOURCE_TEMPLATE;
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
    
    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        initProgressReporting(pHandle, false);
        
        reportProgress(NbBundle.getMessage(GenericResourceGenerator.class,
                "MSG_GeneratingClass", bean.getPackageName() + "." + bean.getName()));  //NOI18N
        
        JavaSource source = JavaSourceHelper.createJavaSource(
                getTemplate(), getDestDir(), bean.getPackageName(), bean.getName());
      
        if (bean.getInputParameters().size() > 0) {
            addInputParamFields(source);
            addConstructorWithInputParams(source);
        }
        
        modifyResourceClass(source);
        return new HashSet<FileObject>(source.getFileObjects());
    }
  
    private void addInputParamFields(JavaSource source) throws IOException {
        ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {
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
            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                List<ParameterInfo> params = bean.getInputParameters();
                String body = "{";      //NOI18N
                
                for (ParameterInfo param : params) {
                    String name = param.getName();
                    body += "if (" + name + " != null) {" +
                            "this." + name + " = " + name + ";" +
                            "}\n";    //NOI18N
                }
                
                ClassTree modifiedTree = JavaSourceHelper.addConstructor(copy, tree,
                        Constants.PUBLIC,
                        getParamNames(params), getParamTypeNames(params),
                        body, null);
                
                copy.rewrite(tree, modifiedTree);
            }
        });
        result.commit();
    }
      
    private void modifyResourceClass(JavaSource source) {
        try {
            ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {
                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);
                    //TODO import
                    //JavaSourceHelper.addImports(copy, new String[0]);
                    if (bean.isGenerateUriTemplate()) {
                        JavaSourceHelper.addClassAnnotation(copy,
                                new String[] { Constants.URI_TEMPLATE_ANNOTATION },
                                new Object[] { bean.getUriTemplate() });
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
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "", ex);
        }
    }
    
    protected ClassTree addMethods(WorkingCopy copy, ClassTree tree) {
        MimeType[] mimes = bean.getMimeTypes();
        String[] types = bean.getRepresentationTypes();
        for (int i=0; i<mimes.length; i++) {
            MimeType mime = mimes[i];
            String type = types[i];
            tree = addGetMethod(mime, type, copy, tree);
            
            if (bean.getMethodTypes().contains(HttpMethodType.POST)) {
                tree = addPostMethod(mime, type, copy, tree);
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
            Constants.HTTP_METHOD_ANNOTATION,
            Constants.PRODUCE_MIME_ANNOTATION};
        
        Object[] annotationAttrs = new Object[] {
            HttpMethodType.GET.name(),
            mime.value()};
        
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
        
        String comment = "Retrieves representation of an instance of " + bean.getQualifiedClassName() + "\n";
        for (String param : parameters) {
            comment += "@param $PARAM$ resource URI parameter\n".replace("$PARAM$", param);
        }
        comment += "@return an instance of "+type;
        
        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                getMethodName(HttpMethodType.GET, mime), type, parameters, paramTypes,
                paramAnnotations, paramAnnotationAttrs,
                bodyText, comment);      //NOI18N
    }
    
    private ClassTree addPostMethod(MimeType mime, String type, WorkingCopy copy, ClassTree tree) {
        Modifier[] modifiers = Constants.PUBLIC;
        
        String[] annotations = new String[] {
            Constants.HTTP_METHOD_ANNOTATION,
            Constants.CONSUME_MIME_ANNOTATION,
            Constants.PRODUCE_MIME_ANNOTATION
        };
        
        Object[] annotationAttrs = new Object[] {
            HttpMethodType.POST.name(),
            mime.value(), mime.value() };
        
        String bodyText = "{ //TODO\n return Response.Builder.created(content, context.getURI()).build(); }"; //NOI18N
        String[] parameters = getPostPutParams();
        Object[] paramTypes = getPostPutParamTypes(type);
        if (type != null) {
            paramTypes[paramTypes.length-1] = type;
        }
        String[] paramAnnotations = getUriParamAnnotations();
        Object[] paramAnnotationAttrs = bean.getUriParams();
        
        String comment = "POST method for creating an instance of " + bean.getName() + "\n";
        for (int i=0; i<parameters.length-1; i++) {
            comment += "@param $PARAM$ resource URI parameter\n".replace("$PARAM$", parameters[i]);
        }
        comment += "@param $PARAM$ representation for the new resource\n".replace("$PARAM$", parameters[parameters.length-1]);
        comment += "@return an HTTP response with content of the created resource";
        
        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                getMethodName(HttpMethodType.POST, mime), Constants.HTTP_RESPONSE,
                parameters, paramTypes, paramAnnotations, paramAnnotationAttrs,
                bodyText, comment);
    }
    
    private ClassTree addPutMethod(MimeType mime, String type, WorkingCopy copy, ClassTree tree) {
        Modifier[] modifiers = Constants.PUBLIC;
        
        String[] annotations = new String[] {
            Constants.HTTP_METHOD_ANNOTATION,
            Constants.CONSUME_MIME_ANNOTATION
        };
        
        Object[] annotationAttrs = new Object[] {
            HttpMethodType.PUT.name(),
            mime.value(), mime.value() };
        Object returnType = Constants.VOID;
        String bodyText = "{ //TODO }";    //NOI18N
        
        String[] parameters = getPostPutParams();
        Object[] paramTypes = getPostPutParamTypes(type);
        if (type != null) {
            paramTypes[paramTypes.length-1] = type;
        }
        String[] paramAnnotations = getUriParamAnnotations();
        Object[] paramAnnotationAttrs = bean.getUriParams();
        
        String comment = "PUT method for updating or creating an instance of " + bean.getName() + "\n";
        for (int i=0; i<parameters.length-1; i++) {
            comment += "@param $PARAM$ resource URI parameter\n".replace("$PARAM$", parameters[i]);
        }
        comment += "@param $PARAM$ representation for the resource\n".replace("$PARAM$", parameters[parameters.length-1]);
        comment += "@return an HTTP response with content of the updated or created resource.";
        
        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                getMethodName(HttpMethodType.PUT, mime), returnType,
                parameters, paramTypes, paramAnnotations, paramAnnotationAttrs,
                bodyText, comment);
    }
    
    private ClassTree addDeleteMethod(WorkingCopy copy, ClassTree tree) {
        Modifier[] modifiers = Constants.PUBLIC;
        
        String[] annotations = new String[] {
            Constants.HTTP_METHOD_ANNOTATION,
        };
        
        Object[] annotationAttrs = new Object[] {
            HttpMethodType.DELETE.name()};
        
        Object returnType = Constants.VOID;
        String bodyText = "{ //TODO implement }";
        
        String[] parameters = bean.getUriParams();
        Object[] paramTypes = getUriParamTypes();
        String[] paramAnnotations = getUriParamAnnotations();
        Object[] paramAnnotationAttrs = bean.getUriParams();
        
        String comment = "DELETE method for resource " + bean.getName() + "\n";
        for (String param : parameters) {
            comment += "@param $PARAM$ resource URI parameter\n".replace("$PARAM$", param);
        }
        
        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                "delete", returnType, parameters, paramTypes, //NOI18N
                paramAnnotations, paramAnnotationAttrs,
                bodyText, comment);      //NOI18N
    }
    
    private ClassTree addSubResourceLocatorMethod(WorkingCopy copy, ClassTree tree, GenericResourceBean subBean) {
        Modifier[] modifiers = Constants.PUBLIC;
        String methodName = "get" + subBean.getName();  //NOI18N
        
        String[] annotations = new String[] {
            Constants.URI_TEMPLATE_ANNOTATION
        };
        
        Object[] annotationAttrs = new Object[] {subBean.getUriTemplate()};
        Object returnType = subBean.getName();
        
        String bodyText = "{ return new " + returnType + "(); }";
        
        String comment = "Sub-resource locator method for  " + subBean.getUriTemplate() + "\n";
        
        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                methodName, returnType, null, null, null, null,
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
        params.addAll(Arrays.asList(bean.getUriParams()));
        params.addAll(Arrays.asList(getParamNames(queryParams)));
        return params.toArray(new String[params.size()]);
    }
    
    private String[] getGetParamTypes(List<ParameterInfo> queryParams) {
        ArrayList<String> types = new ArrayList<String>();
        types.addAll(Arrays.asList(getUriParamTypes()));
        types.addAll(Arrays.asList(getParamTypeNames(queryParams)));
        return types.toArray(new String[types.size()]);
    }
    
    private String[] getUriParamAnnotations() {
        return getUriParamAnnotations(bean);
    }
    
    public static String[] getUriParamAnnotations(GenericResourceBean bean) {
        String[] annos = new String [bean.getUriParams().length];
        for (int i=0; i<annos.length; i++) {
            annos[i] = Constants.URI_PARAM_ANNOTATION;
        }
        return annos;
    }
   
    private String[][] getGetParamAnnotations(List<ParameterInfo> queryParams) {
        ArrayList<String[]> annos = new ArrayList<String[]>();
     
        for (String uriParam : bean.getUriParams()) {
            annos.add(new String[] {Constants.URI_PARAM_ANNOTATION});
        }
        
        String[] annotations = null;
        for (ParameterInfo param : queryParams) {
            if (param.getDefaultValue() != null) {
                annotations = new String[] {
                    Constants.QUERY_PARAM_ANNOTATION,
                       Constants.DEFAULT_VALUE_ANNOTATION
                };
            } else {
                annotations = new String[] {Constants.QUERY_PARAM_ANNOTATION};
            }
            annos.add(annotations);
        }
    
        return annos.toArray(new String[annos.size()][]);
    }
    
    private Object[][] getGetParamAnnotationAttrs(List<ParameterInfo> queryParams) {
        ArrayList<Object[]> attrs = new ArrayList<Object[]>();
        
        for (String uriParam : bean.getUriParams()) {
            attrs.add(new Object[] {uriParam});
        }
      
        Object[] annotationAttrs = null;
        for (ParameterInfo param : queryParams) {
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
        List<String> params = new ArrayList<String>(Arrays.asList(bean.getUriParams()));
        params.add("content");  //NO18N
        return params.toArray(new String[params.size()]);
    }
    
    private String[] getPostPutParamTypes(String representatinType) {
        String defaultType = String.class.getName();
        String[] types = new String[bean.getUriParams().length + 1];
        for (int i=0; i < types.length; i++) {
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
    
}
