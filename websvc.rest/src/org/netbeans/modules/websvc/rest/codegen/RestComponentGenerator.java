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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.codegen.model.GenericResourceBean;
import org.netbeans.modules.websvc.rest.codegen.model.JaxwsOperationInfo;
import org.netbeans.modules.websvc.rest.support.AbstractTask;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import static com.sun.source.tree.Tree.Kind.*;

/**
 * Code generator for REST services.
 *
 * @author nam
 */
public abstract class RestComponentGenerator extends AbstractGenerator {
    public static final String RESOURCE_TEMPLATE = "Templates/WebServices/JaxwsWrapperResource.java"; //NOI18N
    public static final String COMMENT_END_OF_HTTP_MEHTOD_GET = "TODO return proper representation object";
    
    protected FileObject targetFile;  // resource file target of the drop
    protected FileObject destDir;
    protected FileObject wrapperResourceFile;
    protected Project project;
    protected GenericResourceBean bean;
    protected JavaSource wrapperResourceJS;
    protected JavaSource targetResourceJS;
    protected JavaSource jaxbOutputWrapperJS;
    protected String getSubResourceMethodName;

    public RestComponentGenerator(FileObject targetFile, GenericResourceBean bean) {
        this.targetFile = targetFile;
        this.destDir = targetFile.getParent();
        project = FileOwnerQuery.getOwner(targetFile);
        if (project == null) {
            throw new IllegalArgumentException(targetFile.getPath() + " is not part of a project.");
        }
        targetResourceJS = JavaSource.forFileObject(targetFile);
        String packageName = JavaSourceHelper.getPackageName(targetResourceJS);
        bean.setPackageName(packageName);
        bean.setPrivateFieldForQueryParam(true);
        this.bean = bean;
        wrapperResourceFile = SourceGroupSupport.findJavaSourceFile(project, bean.getName());

        getSubResourceMethodName = "get" + Util.upperFirstChar(bean.getShortName());
    }
    
    /**
     *  Return Input parameter types
     */    
    public abstract Map<String,String> getInputParameterTypes();
    
    /**
     *  Set Input Values
     */    
    public abstract void setConstantInputValues(Map<String,Object> constantParamValues);
    
    /**
     *  Check to see if Inputs need to be displayed
     */    
    public abstract boolean needsInputs();
    
    /**
     *  Return target and generated file objects
     */    
    public abstract FileObject generateJaxbOutputWrapper() throws IOException;
    
    /**
     *  Return target and generated file objects
     */    
    public abstract void generateComponentResourceClass() throws IOException;
    
    /**
     *  Return target and generated file objects
     */    
    public abstract void modifyGETMethod() throws IOException;

    protected String getOverridingStatements() {
        String[] params = bean.getQueryParams();
        String[] paramTypes = bean.getQueryParamTypes();
        return JavaSourceHelper.getParamEqualThisFieldStatements(params, paramTypes);
    }
    
    private void insertServiceRef(JavaSource targetJS, JaxwsOperationInfo info, final String serviceFieldName) throws IOException {
        final String serviceJavaName = info.getService().getJavaName();
        String wsdlUrl = info.getWsdlURL();
        
        if (wsdlUrl.startsWith("file:")) { //NOI18N
            wsdlUrl = info.getWsdlLocation().toString();
        }
        final String localWsdlUrl = wsdlUrl;
        CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                TypeElement typeElement = JavaSourceHelper.getTopLevelClassElement(workingCopy);
                ClassTree javaClass = JavaSourceHelper.getClassTree(workingCopy, typeElement);
                VariableTree serviceRefInjection = generateServiceRefInjection(
                        workingCopy, make, serviceFieldName, serviceJavaName, localWsdlUrl);
                ClassTree modifiedClass = make.insertClassMember(javaClass, 0, serviceRefInjection);
                workingCopy.rewrite(javaClass, modifiedClass);
            }
            public void cancel() {}
        };
        targetJS.runModificationTask(modificationTask).commit();
    }
    
    /**
     * Determines the initialization value of a variable of type "type"
     * @param type Type of the variable
     * @param targetFile FileObject containing the class that declares the type
     */
    private static String resolveInitValue(String type) {
        if (type.startsWith("javax.xml.ws.Holder")) { //NOI18N
            return "new "+type+"();";
        }
        if ("int".equals(type) || "long".equals(type) || "short".equals(type) || "byte".equals(type)) //NOI18N
            return "0;"; //NOI18N
        if ("boolean".equals(type)) //NOI18N
            return "false;"; //NOI18N
        if ("float".equals(type) || "double".equals(type)) //NOI18N
            return "0.0;"; //NOI18N
        if ("java.lang.String".equals(type)) //NOI18N
            return "\"\";"; //NOI18N
        if (type.endsWith("CallbackHandler")) { //NOI18N
            return "new "+type+"();"; //NOI18N
        }
        if (type.startsWith("javax.xml.ws.AsyncHandler")) { //NOI18N
            return "new "+type+"() {"; //NOI18N
        }
        
        return "null;"; //NOI18N
    }
    
    private static String resolveResponseType(String argumentType) {
        int start = argumentType.indexOf("<");
        int end = argumentType.indexOf(">");
        if (start>0 && end>0 && start<end) {
            return argumentType.substring(start+1,end);
        } else return "javax.xml.ws.Response"; //NOI18N
    }
    
    private static VariableTree generateServiceRefInjection(
            WorkingCopy workingCopy,
            TreeMaker make,
            String fieldName,
            String fieldType,
            String wsdlUrl) {
        TypeElement wsRefElement = workingCopy.getElements().getTypeElement("javax.xml.ws.WebServiceRef"); //NOI18N
        
        AnnotationTree wsRefAnnotation = make.Annotation(
                make.QualIdent(wsRefElement),
                Collections.<ExpressionTree>singletonList(make.Assignment(make.Identifier("wsdlLocation"), make.Literal(wsdlUrl)))
                );
        // create method modifier: public and no annotation
        ModifiersTree methodModifiers = make.Modifiers(
                Collections.<Modifier>singleton(Modifier.PUBLIC),
                Collections.<AnnotationTree>singletonList(wsRefAnnotation)
                );
        TypeElement typeElement = workingCopy.getElements().getTypeElement(fieldType);
        
        VariableTree var =  make.Variable(
                methodModifiers,
                fieldName,
                make.Type(typeElement.asType()),
                null
                );
        return make.Variable(
                make.Modifiers(var.getModifiers().getFlags(), var.getModifiers().getAnnotations()),
                var.getName(),
                var.getType(),
                var.getInitializer()
                );
    }
    
    private static String findProperServiceFieldName(Set serviceFieldNames) {
        String name="service";
        int i=0;
        while (serviceFieldNames.contains(name)) {
            name="service_"+String.valueOf(++i);
        }
        return name; //NOI18N
    }
    
    private String getOutputWrapperQualifiedName() throws IOException {
        return JavaSourceHelper.getClassType(jaxbOutputWrapperJS);
    }
    
    public void addSubResourceMethod() throws IOException {
        ModificationResult result = targetResourceJS.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                JavaSourceHelper.addImports(copy, new String[] {getOutputWrapperQualifiedName()});
                ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                String[] annotations = new String[] { Constants.URI_TEMPLATE_ANNOTATION };
                Object[] annotationAttrs = new Object[] { bean.getUriWhenUsedAsSubResource() };
                String[] params = bean.getQueryParams();
                Object[] paramTypes = bean.getQueryParamTypes();
                String[] paramAnnotations = getQueryParamAnnotations(bean);
                Object[] paramAnnotationAttrs = bean.getQueryParams();
                StringBuilder args = new StringBuilder();
                for (int i=0; i<params.length; i++) {
                    String access = match(JavaSourceHelper.getTopLevelClassElement(copy), params[i]);
                    if (i != 0) args.append(", ");
                    if (access != null) {
                        args.append(access);
                        params[i] = null;
                        paramTypes[i] = null;
                        paramAnnotations[i] = null;
                        paramAnnotationAttrs[i] = null;
                    } else {
                        args.append(params[i]);
                    }
                }
                
                List<String> paramList = shrink(params);
                List paramTypeList = shrink(paramTypes);
                List<String> paramAnnotationList = shrink(paramAnnotations);
                List paramAnnotationAttrList = shrink(paramAnnotationAttrs);

                String uriParamAnnotationAttribute = getUriParam(JavaSourceHelper.getTopLevelClassElement(copy));
                if (uriParamAnnotationAttribute != null) {
                    paramList.add(0, "id");
                    paramTypeList.add(0, Integer.class.getName());
                    paramAnnotationList.add(0, Constants.URI_PARAM_ANNOTATION);
                    paramAnnotationAttrList.add(0, uriParamAnnotationAttribute);
                }
                
                String body = "{return new $CLASS$($ARGS$);}";
                body = body.replace("$CLASS$", JavaSourceHelper.getClassName(wrapperResourceJS));
                body = body.replace("$ARGS$", args);
                String comment = "Returns "+bean.getName()+" sub-resource.\n";

                ClassTree modifiedTree = JavaSourceHelper.addMethod(
                        copy, tree, Constants.PUBLIC, annotations, annotationAttrs,
                        getSubResourceMethodName, bean.getQualifiedClassName(),
                        paramList.toArray(new String[0]), paramTypeList.toArray(), 
                        paramAnnotationList.toArray(new String[0]), paramAnnotationAttrList.toArray(),
                        body, comment);
                copy.rewrite(tree, modifiedTree);
                
                if (paramAnnotationList.contains(Constants.QUERY_PARAM_ANNOTATION)) {
                    JavaSourceHelper.addImports(copy, new String[] { Constants.QUERY_PARAM });
                }
                
            }});
            result.commit();
    }
    
    public static String getUriParam(TypeElement typeElement) {
        List<ExecutableElement> methods = ElementFilter.methodsIn(typeElement.getEnclosedElements());
        boolean hasMethodGetEntity = false;
        String uriParam = null;
        for (ExecutableElement method : methods) {
            if (hasMethodGetEntity && uriParam != null) {
                return uriParam;
            }
            if (method.getSimpleName().contentEquals("getEntity")) {
                hasMethodGetEntity = true;
            }
            for (VariableElement ve : method.getParameters()) {
                List<? extends AnnotationMirror> annotations = ve.getAnnotationMirrors();
                for (AnnotationMirror m : annotations) {
                    if (JavaSourceHelper.isOfAnnotationType(m, Constants.URI_PARAM_ANNOTATION)) {
                        Collection<? extends AnnotationValue> values = m.getElementValues().values();
                        for (AnnotationValue av : values) {
                            if (av.getValue() instanceof String) {
                                String v = (String) av.getValue();
                                uriParam = v;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public String getSubResourceLocator() {
        return getSubResourceMethodName;
    }
    
    public List shrink(Object[] array) {
        ArrayList result = new ArrayList();
        for (Object o : array) {
            if (o != null) result.add(o);
        }
        return result;
    }
    
    public List<String> shrink(String[] array) {
        ArrayList<String> result = new ArrayList<String>();
        for (String o : array) {
            if (o != null) result.add(o);
        }
        return result;
    }
    
    public String[] getQueryParamAnnotations(GenericResourceBean bean) {
        String[] anns = new String[bean.getQueryParams().length];
        for (int i=0; i<anns.length; i++) {
            anns[i] = Constants.QUERY_PARAM_ANNOTATION;
        }
        return anns;
    }
    
    public String match(TypeElement targetClass, String arg) {
        List<VariableElement> fields = ElementFilter.fieldsIn(targetClass.getEnclosedElements());
        String argName = arg.toLowerCase();
        for (VariableElement field : fields) {
            if (match(field.getSimpleName().toString(), argName)) {
                return field.getSimpleName().toString();
            }
        }
        
        List<ExecutableElement> methods = ElementFilter.methodsIn(targetClass.getEnclosedElements());
        for (ExecutableElement method : methods) {
            if ("getEntity".equals(method.getSimpleName().toString())) {
                TypeMirror tm = method.getReturnType();
                if (tm.getKind() == TypeKind.DECLARED) {
                    Element element = ((DeclaredType)tm).asElement();
                    List<ExecutableElement> eMethods = ElementFilter.methodsIn(element.getEnclosedElements());
                    for (ExecutableElement m : eMethods) {
                        String mName = m.getSimpleName().toString().toLowerCase();
                        if (mName.startsWith("get") && match(mName.substring(3), argName)) {
                            return "getEntity(id)."+m.getSimpleName().toString()+"()"; //NOI18N
                        }
                    }
                }
                
            }
        }
        return null;
    }
    
    public boolean match(String s1, String lower) {
        String s10 = s1.toLowerCase();
        return (s10.indexOf(lower) > -1 || lower.indexOf(s10) > -1);
    }

    public JavaSource getOrCreateGenericRefConverter() {
        FileObject converterFolder = getConverterFolder();
        String packageName = SourceGroupSupport.packageForFolder(converterFolder);
        FileObject refConverterFO = converterFolder.getFileObject(GENERIC_REF_CONVERTER, "java"); //NOI18N
        if (refConverterFO == null) {
            JavaSource source = JavaSourceHelper.createJavaSource(
                    GENERIC_REF_CONVERTER_TEMPLATE, converterFolder, packageName, GENERIC_REF_CONVERTER);
            return source;
        } else {
            return JavaSource.forFileObject(refConverterFO);
        }
    }
    
    public void modifyTargetConverter() throws IOException {
        TypeElement targetResourceType = JavaSourceHelper.getTypeElement(targetResourceJS);
        TypeElement representationType = JavaSourceHelper.getXmlRepresentationClass(
                
                targetResourceType,EntityRestServicesGenerator.CONVERTER_SUFFIX);
        if (representationType != null) {
            JavaSource representationJS = JavaSourceHelper.forTypeElement(representationType, project);
            ModificationResult result = representationJS.runModificationTask(
                        new AbstractTask<WorkingCopy>() {
                    public void run(WorkingCopy copy) throws IOException {
                        copy.toPhase(JavaSource.Phase.RESOLVED);
                        ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                        ClassTree modifiedTree = addGetComponentRefMethod(copy, tree);
                        copy.rewrite(tree, modifiedTree);
                    }}
                );
            result.commit();
        }
    }

    private static final String GENERIC_REF_CONVERTER_TEMPLATE = "Templates/WebServices/RefConverter.java";    //NOI18N
    private static final String GENERIC_REF_CONVERTER = "RefConverter";    //NOI18N
    public FileObject getConverterFolder() {
        FileObject converterDir = destDir;
        if (destDir.getParent() != null) {
            FileObject dir = destDir.getParent().getFileObject(EntityRestServicesGenerator.CONVERTER_FOLDER);
            if (dir != null) {
                converterDir = dir;
            }
        }
        return converterDir;
    }

    private ClassTree addGetComponentRefMethod(WorkingCopy copy, ClassTree tree) {
        String[] annotations = new String[] {Constants.XML_ELEMENT_ANNOTATION};
        Object[] annotationAttrs = new Object[] {
            JavaSourceHelper.createAssignmentTree(copy, "name", bean.getShortName()+"Ref") //NOI18N
        };
        
        String body = "{ return new $CLASS$(uri.resolve(\"$URITEMPLATE$\")); }";
        body = body.replace("$CLASS$", GENERIC_REF_CONVERTER);
        body = body.replace("$URITEMPLATE$", bean.getUriWhenUsedAsSubResource());
        String comment = "Returns reference to "+bean.getName()+" resource.\n";
        String methodName = "get"+bean.getShortName()+"Ref";
        return JavaSourceHelper.addMethod(
                copy, tree, Constants.PUBLIC, annotations, annotationAttrs,
                methodName, GENERIC_REF_CONVERTER, //NOI18N
                null, null, null, null, body, comment);
    }

    public void putFocusOnTargetFile() throws IOException {
        try {
            DataObject dobj = DataObject.find(targetFile);
            if (dobj != null) {
                final EditorCookie ec = dobj.getCookie(EditorCookie.class);
                if (ec != null) {
                    StyledDocument doc = ec.openDocument();
                    final int position = doc.getText(0, doc.getLength()).lastIndexOf(getSubResourceMethodName);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            JEditorPane[] panes = ec.getOpenedPanes();
                            if (panes != null && panes.length > 0) {
                                panes[0].getCaret().setDot(position);
                            }
                        }
                    });
                }
            }
        } catch(Exception ex) {
            //Not critical, just log
            Logger.getLogger(getClass().getName()).log(Level.INFO, "putFocusOnTargetFile", ex);
        }
    }  
}
