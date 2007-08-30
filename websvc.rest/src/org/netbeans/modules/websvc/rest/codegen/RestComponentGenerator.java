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
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.rest.codegen.model.RestComponentBean;
import org.netbeans.modules.websvc.rest.support.AbstractTask;
import org.netbeans.modules.websvc.rest.support.Inflector;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.openide.filesystems.FileObject;
import static com.sun.source.tree.Tree.Kind.*;

/**
 * Code generator for REST services.
 *
 * @author nam
 */
public abstract class RestComponentGenerator extends AbstractGenerator {

    private static final String RESOURCE_TEMPLATE = "Templates/WebServices/JaxwsWrapperResource.java"; //NOI18N
    private static final String COMMENT_END_OF_HTTP_MEHTOD_GET = "TODO return proper representation object";
    private static final String GENERIC_REF_CONVERTER_TEMPLATE = "Templates/WebServices/RefConverter.java"; //NOI18N
    private static final String GENERIC_REF_CONVERTER = "GenericRefConverter"; //NOI18N
    protected FileObject targetFile; // resource file target of the drop
    protected FileObject destDir;
    protected FileObject wrapperResourceFile;
    protected Project project;
    protected RestComponentBean bean;
    protected JavaSource wrapperResourceJS;
    protected JavaSource targetResourceJS;
    protected JavaSource jaxbOutputWrapperJS;
    protected String subresourceLocatorName;
    protected String subresourceLocatorUriTemplate;
    private Collection<String> existingUriTemplates;

    public RestComponentGenerator(FileObject targetFile, RestComponentBean bean) {
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
    }

    public RestComponentBean getBean() {
        return bean;
    }

    public boolean wrapperResourceExists() {
        return wrapperResourceFile != null;
    }

    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        initProgressReporting(pHandle);

        preGenerate();

        FileObject outputWrapperFO = generateJaxbOutputWrapper();
        jaxbOutputWrapperJS = JavaSource.forFileObject(outputWrapperFO);
        generateComponentResourceClass();
        addSubresourceLocator();
        FileObject refConverterFO = getOrCreateGenericRefConverter().getFileObjects().iterator().next();
        modifyTargetConverter();
        FileObject[] result = new FileObject[]{targetFile, wrapperResourceFile, refConverterFO, outputWrapperFO};
        JavaSourceHelper.saveSource(result);

        postGenerate();

        finishProgressReporting();

        return new HashSet<FileObject>(Arrays.asList(result));
    }

    protected void preGenerate() {
    }

    protected void postGenerate() {
    }

    protected abstract String getCustomMethodBody() throws IOException;

    protected FileObject generateJaxbOutputWrapper() throws IOException {
        FileObject converterFolder = getConverterFolder();
        String packageName = SourceGroupSupport.packageForFolder(converterFolder);
        bean.setOutputWrapperPackageName(packageName);
        String[] returnTypeNames = bean.getOutputTypes();
        XmlOutputWrapperGenerator gen = new XmlOutputWrapperGenerator(converterFolder, bean.getOutputWrapperName(), packageName, returnTypeNames);

        return gen.generate();
    }

    protected void generateComponentResourceClass() throws IOException {
        if (wrapperResourceFile == null) {
            GenericResourceGenerator delegate = new GenericResourceGenerator(destDir, bean);
            delegate.setTemplate(RESOURCE_TEMPLATE);
            Set<FileObject> files = delegate.generate(getProgressHandle());

            if (files == null || files.size() == 0) {
                return;
            }
            wrapperResourceFile = files.iterator().next();
            wrapperResourceJS = JavaSource.forFileObject(wrapperResourceFile);
            modifyGetMethod();
        } else {
            wrapperResourceJS = JavaSource.forFileObject(wrapperResourceFile);
        }
    }

    protected void addSubresourceLocator() throws IOException {
        ModificationResult result = targetResourceJS.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                JavaSourceHelper.addImports(copy, getSubresourceLocatorImports());

                ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                String[] annotations = new String[]{Constants.URI_TEMPLATE_ANNOTATION};
                Object[] annotationAttrs = new Object[]{getSubresourceLocatorUriTemplate()};
                String[] params = null;
                Object[] paramTypes = null;
                String[] paramAnnotations = null;
                Object[] paramAnnotationAttrs = null;

                String uriParamAnnotationAttribute = getUriParam(JavaSourceHelper.getTopLevelClassElement(copy));
                if (uriParamAnnotationAttribute != null) {
                    params = new String[]{"id"}; //NOI18N
                    paramTypes = new Object[]{Integer.class.getName()};
                    paramAnnotations = new String[]{Constants.URI_PARAM_ANNOTATION};
                    paramAnnotationAttrs = new Object[]{uriParamAnnotationAttribute};
                }

                String body = "{";
                body += getParamInitStatements(copy);
                body += "return new $CLASS$($ARGS$);}";
                body = body.replace("$CLASS$", JavaSourceHelper.getClassName(wrapperResourceJS));
                body = body.replace("$ARGS$", getParamList());

                String comment = "Returns " + bean.getName() + " sub-resource.\n";

                ClassTree modifiedTree = JavaSourceHelper.addMethod(copy, tree, Constants.PUBLIC, annotations, annotationAttrs, getSubresourceLocatorName(), bean.getQualifiedClassName(), params, paramTypes, paramAnnotations, paramAnnotationAttrs, body, comment);
                copy.rewrite(tree, modifiedTree);
            }
        });
        result.commit();
    }

    /**
     *  Return target and generated file objects
     */
    protected void modifyGetMethod() throws IOException {
        ModificationResult result = wrapperResourceJS.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                JavaSourceHelper.addImports(copy, new String[]{getConverterType()});

                String methodBody = "{" + getOverridingStatements(); //NOI18N
                methodBody += getCustomMethodBody();

                methodBody += "}"; //NOI18N
                MethodTree methodTree = JavaSourceHelper.getMethodByName(copy, "getXml"); //NOI18N
                JavaSourceHelper.replaceMethodBody(copy, methodTree, methodBody);
            }
        });
        result.commit();
    }

    protected String getOverridingStatements() {
        String text = ""; //NOI18N
        for (ParameterInfo param : bean.getQueryParameters()) {
            String name = param.getName();
            text += "if (this." + name + " != null) {" + name + " = this." + name + ";" + "}\n";
        }

        return text;
    }

    protected JavaSource getOrCreateGenericRefConverter() {
        FileObject converterFolder = getConverterFolder();
        String packageName = SourceGroupSupport.packageForFolder(converterFolder);
        FileObject refConverterFO = converterFolder.getFileObject(GENERIC_REF_CONVERTER, "java"); //NOI18N
        if (refConverterFO == null) {
            JavaSource source = JavaSourceHelper.createJavaSource(GENERIC_REF_CONVERTER_TEMPLATE, converterFolder, packageName, GENERIC_REF_CONVERTER);
            return source;
        } else {
            return JavaSource.forFileObject(refConverterFO);
        }
    }

    protected String getConverterType() throws IOException {
        return JavaSourceHelper.getClassType(jaxbOutputWrapperJS);
    }

    protected String getConverterName() throws IOException {
        String converterType = getConverterType();

        return converterType.substring(converterType.lastIndexOf('.') + 1);
    }

    private String getParamInitStatements(WorkingCopy copy) {
        String text = ""; //NOI18N
        for (ParameterInfo param : bean.getInputParameters()) {
            String initValue = "null"; //NOI18N
            String access = match(JavaSourceHelper.getTopLevelClassElement(copy), param.getName());

            if (access != null) {
                initValue = access;
            }

            text += param.getType().getSimpleName() + " " + param.getName() + " = " + initValue + ";"; //NOI18N
        }

        return text;
    }

    private String getParamList() {
        List<ParameterInfo> inputParams = bean.getInputParameters();
        String text = ""; //NOI18N
        for (int i = 0; i < inputParams.size(); i++) {
            ParameterInfo param = inputParams.get(i);

            if (i == 0) {
                text += param.getName();
            } else {
                text += ", " + param.getName(); //NOI18N
            }
        }

        return text;
    }

    private String getOutputWrapperQualifiedName() throws IOException {
        return JavaSourceHelper.getClassType(jaxbOutputWrapperJS);
    }

    private static String getUriParam(TypeElement typeElement) {
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

    public String getSubresourceLocatorName() {
        if (subresourceLocatorName == null) {
            String uriTemplate = getSubresourceLocatorUriTemplate();

            if (uriTemplate.endsWith("/")) {
                uriTemplate = uriTemplate.substring(0, uriTemplate.length() - 1);
            }
            subresourceLocatorName = "get" + Inflector.getInstance().camelize(uriTemplate);
        }

        return subresourceLocatorName;
    }

    public void setSubresourceLocatorName(String name) {
        this.subresourceLocatorName = name;
    }

    public String getSubresourceLocatorUriTemplate() {
        if (subresourceLocatorUriTemplate == null) {
            subresourceLocatorUriTemplate = getAvailableUriTemplate();
        }

        if (!subresourceLocatorUriTemplate.endsWith("/")) {
            //NOI18N
            subresourceLocatorUriTemplate += "/"; //NOI18N
        }
        return subresourceLocatorUriTemplate;
    }

    public void setSubresourceLocatorUriTemplate(String uriTemplate) {
        this.subresourceLocatorUriTemplate = uriTemplate;
    }

//
//    public List shrink(Object[] array) {
//        ArrayList result = new ArrayList();
//        for (Object o : array) {
//            if (o != null) {
//                result.add(o);
//            }
//        }
//        return result;
//    }
//
//    public List<String> shrink(String[] array) {
//        ArrayList<String> result = new ArrayList<String>();
//        for (String o : array) {
//            if (o != null) {
//                result.add(o);
//            }
//        }
//        return result;
//    }
//
//    public String[] getQueryParamAnnotations(GenericResourceBean bean) {
//        String[] anns = new String[bean.getQueryParams().length];
//        for (int i = 0; i < anns.length; i++) {
//            anns[i] = Constants.QUERY_PARAM_ANNOTATION;
//        }
//        return anns;
//    }
//
    private String match(TypeElement targetClass, String arg) {
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
                    Element element = ((DeclaredType) tm).asElement();
                    List<ExecutableElement> eMethods = ElementFilter.methodsIn(element.getEnclosedElements());
                    for (ExecutableElement m : eMethods) {
                        String mName = m.getSimpleName().toString().toLowerCase();
                        if (mName.startsWith("get") && match(mName.substring(3), argName)) {
                            return "getEntity(id)." + m.getSimpleName().toString() + "()"; //NOI18N
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean match(String s1, String lower) {
        String s10 = s1.toLowerCase();
        return s10.indexOf(lower) > -1 || lower.indexOf(s10) > -1;
    }

    private void modifyTargetConverter() throws IOException {
        TypeElement targetResourceType = JavaSourceHelper.getTypeElement(targetResourceJS);
        TypeElement representationType = JavaSourceHelper.getXmlRepresentationClass(targetResourceType, EntityResourcesGenerator.CONVERTER_SUFFIX);
        if (representationType != null) {
            JavaSource representationJS = JavaSourceHelper.forTypeElement(representationType, project);
            ModificationResult result = representationJS.runModificationTask(new AbstractTask<WorkingCopy>() {

                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);
                    ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                    ClassTree modifiedTree = addGetComponentRefMethod(copy, tree);
                    copy.rewrite(tree, modifiedTree);
                }
            });
            result.commit();
        }
    }

    private ClassTree addGetComponentRefMethod(WorkingCopy copy, ClassTree tree) {
        String[] annotations = new String[]{Constants.XML_ELEMENT_ANNOTATION};
        String uriTemplate = getSubresourceLocatorUriTemplate();
        String xmlElementName = null;
        
        if (uriTemplate.endsWith("/")) {
            xmlElementName = uriTemplate.substring(0, uriTemplate.length()-1) + "Ref";
        } else {
            xmlElementName = uriTemplate + "Ref";
        }
        
        Object[] annotationAttrs = new Object[]{JavaSourceHelper.createAssignmentTree(copy, "name", 
                xmlElementName)};

        String body = "{ return new $CLASS$(uri.resolve(\"$URITEMPLATE$\")); }";
        body = body.replace("$CLASS$", GENERIC_REF_CONVERTER);
        body = body.replace("$URITEMPLATE$", uriTemplate);
        String comment = "Returns reference to " + bean.getName() + " resource.\n";
        String methodName = getSubresourceLocatorName() + "Ref";
        return JavaSourceHelper.addMethod(copy, tree, Constants.PUBLIC, annotations, annotationAttrs, methodName, GENERIC_REF_CONVERTER, null, null, null, null, body, comment);
    }

    private FileObject getConverterFolder() {
        FileObject converterDir = destDir;
        if (destDir.getParent() != null) {
            FileObject dir = destDir.getParent().getFileObject(EntityResourcesGenerator.CONVERTER_FOLDER);
            if (dir != null) {
                converterDir = dir;
            }
        }
        return converterDir;
    }

    public Collection<String> getExistingUriTemplates() {
        if (existingUriTemplates == null) {
            existingUriTemplates = JavaSourceHelper.getAnnotationValuesForAllMethods(targetResourceJS, Constants.URI_TEMPLATE);
        }

        return existingUriTemplates;
    }

    private String getAvailableUriTemplate() {
        //TODO: Need to create an unique UriTemplate value.
        Collection<String> existingUriTemplates = getExistingUriTemplates();
        int counter = 1;
        String uriTemplate = Inflector.getInstance().camelize(bean.getShortName(), true);
        String temp = uriTemplate;
        
        while (existingUriTemplates.contains(temp) || 
                existingUriTemplates.contains(temp + "/")) {
            //NOI18N
            temp = uriTemplate + counter++;
        }

        return temp;
    }

    private String[] getSubresourceLocatorImports() throws IOException {
        List<String> imports = new ArrayList<String>();
        //imports.add(getOutputWrapperQualifiedName());
        List<ParameterInfo> inputParams = bean.getInputParameters();

        for (ParameterInfo param : inputParams) {
            if (!param.getType().getPackage().getName().equals("java.lang")) {
                imports.add(param.getType().getName());
            }
        }

        return imports.toArray(new String[imports.size()]);
    }
}