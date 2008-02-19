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

import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
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
import org.netbeans.modules.websvc.saas.codegen.java.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.WadlSaasBean;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.saas.codegen.java.support.AbstractTask;
import org.netbeans.modules.websvc.saas.codegen.java.support.Inflector;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.SourceGroupSupport;
import org.openide.filesystems.FileObject;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class JaxRsCodeGenerator extends AbstractGenerator {

    public static final String REST_CONNECTION = "RestConnection"; //NOI18N
    public static final String REST_CONNECTION_TEMPLATE = "Templates/SaaSServices/RestConnection.java"; //NOI18N
    private static final String COMMENT_END_OF_HTTP_MEHTOD_GET = "TODO return proper representation object";      //NOI18N
    private static final String GENERIC_REF_CONVERTER_TEMPLATE = "Templates/SaaSServices/RefConverter.java"; //NOI18N
    private static final String GENERIC_REF_CONVERTER = "GenericRefConverter"; //NOI18N
    public static final String CONVERTER_SUFFIX = "Converter";      //NOI18N
    public static final String CONVERTER_FOLDER = "converter";      //NOI18N
    public static final String RESOURCE_SUFFIX = "Resource";      //NOI18N
    protected FileObject targetFile; // resource file target of the drop
    protected FileObject destDir;
    protected FileObject wrapperResourceFile;
    protected Project project;
    protected WadlSaasBean bean;
    protected JavaSource wrapperResourceJS;
    protected JavaSource targetResourceJS;
    protected JavaSource jaxbOutputWrapperJS;
    protected String subresourceLocatorName;
    protected String subresourceLocatorUriTemplate;
    private Collection<String> existingUriTemplates;

    public JaxRsCodeGenerator(FileObject targetFile, WadlSaasMethod m) throws IOException {
        this(targetFile, new WadlSaasBean(m));
    }

    private JaxRsCodeGenerator(FileObject targetFile, WadlSaasBean bean) {
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

    protected void preGenerate() {
        JavaSource source = JavaSourceHelper.createJavaSource(REST_CONNECTION_TEMPLATE, destDir, bean.getPackageName(), REST_CONNECTION);
    }

    protected String getCustomMethodBody() throws IOException {
        String converterName = getConverterName();
        String paramStr = null;
        StringBuffer sb1 = new StringBuffer();
        List<ParameterInfo> params = bean.getInputParameters();

        for (ParameterInfo param : params) {
            String paramName = param.getName();
            if (param.getType() != String.class) {
                sb1.append("{\"" + paramName + "\", " + paramName + ".toString()},");
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
        methodBody += "             RestConnection cl = new RestConnection();\n";
        methodBody += "             String[][] params = new String[][]{\n";
        methodBody += "                 " + paramStr + "\n";
        methodBody += "             };\n";
        methodBody += "             String result = cl.connect(url, params);\n";
        methodBody += "             converter.setString(result);\n";
        methodBody += "             return converter;\n";
        methodBody += "        } catch (java.io.IOException ex) {\n";
        methodBody += "             throw new WebApplicationException(ex);\n";
        methodBody += "        }\n }";
       
        return methodBody;
    }
    
    public WadlSaasBean getBean() {
        return bean;
    }

    public boolean wrapperResourceExists() {
        return wrapperResourceFile != null;
    }

    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        initProgressReporting(pHandle);

        preGenerate();

        FileObject outputWrapperFO = generateJaxbOutputWrapper();
        if (outputWrapperFO != null) {
            jaxbOutputWrapperJS = JavaSource.forFileObject(outputWrapperFO);
        }
        generateComponentResourceClass();
        addSubresourceLocator();
        FileObject refConverterFO = getOrCreateGenericRefConverter().getFileObjects().iterator().next();
        modifyTargetConverter();
        FileObject[] result = new FileObject[]{targetFile, wrapperResourceFile, refConverterFO, outputWrapperFO};
        if (outputWrapperFO == null) {
            result = new FileObject[]{targetFile, wrapperResourceFile, refConverterFO};
        }
        JavaSourceHelper.saveSource(result);

        finishProgressReporting();

        return new HashSet<FileObject>(Arrays.asList(result));
    }

    protected FileObject generateJaxbOutputWrapper() throws IOException {
        MimeType mimeType = bean.getMimeTypes()[0];

        if (mimeType == MimeType.JSON || mimeType == MimeType.XML) {
            FileObject converterFolder = getConverterFolder();
            String packageName = SourceGroupSupport.packageForFolder(converterFolder);
            bean.setOutputWrapperPackageName(packageName);
            String[] returnTypeNames = bean.getOutputTypes();
            XmlOutputWrapperGenerator gen = new XmlOutputWrapperGenerator(converterFolder, bean.getOutputWrapperName(), packageName, returnTypeNames);

            return gen.generate();
        }
        
        return null;
    }

    protected void generateComponentResourceClass() throws IOException {
        if (wrapperResourceFile == null) {
            GenericResourceGenerator delegate = new GenericResourceGenerator(destDir, bean);
            delegate.setTemplate(bean.getResourceClassTemplate());
            Set<FileObject> files = delegate.generate(getProgressHandle());

            if (files == null || files.size() == 0) {
                return;
            }
            wrapperResourceFile = files.iterator().next();
            wrapperResourceJS = JavaSource.forFileObject(wrapperResourceFile);
            addSupportingMethods();
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
                String[] annotations = new String[]{RestConstants.PATH_ANNOTATION};
                Object[] annotationAttrs = new Object[]{getSubresourceLocatorUriTemplate()};
                boolean addTryFinallyBlock = false;

                if (hasGetEntityMethod(JavaSourceHelper.getTopLevelClassElement(copy))) {
                    addTryFinallyBlock = true;
                }

                String body = "{";

                if (addTryFinallyBlock) {
                    body += "try {";
                }

                body += getParamInitStatements(copy);
                body += "return new $CLASS$($ARGS$);}";
                body = body.replace("$CLASS$", JavaSourceHelper.getClassName(wrapperResourceJS));
                body = body.replace("$ARGS$", getParamList());

                String comment = "Returns " + bean.getName() + " sub-resource.\n";

                if (addTryFinallyBlock) {
                    body += "finally { PersistenceService.getInstance().close()";
                }

                ClassTree modifiedTree = JavaSourceHelper.addMethod(copy, tree, 
                        Constants.PUBLIC, annotations, annotationAttrs, 
                        getSubresourceLocatorName(), bean.getQualifiedClassName(), 
                        null, null, null, null, 
                        body, comment);
                copy.rewrite(tree, modifiedTree);
            }
        });
        result.commit();
    }

    /**
     *  Add supporting methods, if any, for GET
     */
    protected void addSupportingMethods() throws IOException {
    }

    /**
     *  Return target and generated file objects
     */
    protected void modifyGetMethod() throws IOException {
        ModificationResult result = wrapperResourceJS.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                String converterType = getConverterType();
                if (converterType != null) {
                    JavaSourceHelper.addImports(copy, new String[]{getConverterType()});
                }

                String methodBody = "{" + getOverridingStatements(); //NOI18N
                methodBody += getCustomMethodBody();

                methodBody += "}"; //NOI18N
                for(MimeType mime:bean.getMimeTypes()) {
                    MethodTree methodTree = JavaSourceHelper.getMethodByName(copy, bean.getGetMethodName(mime));
                    JavaSourceHelper.replaceMethodBody(copy, methodTree, methodBody);
                }
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
        if (jaxbOutputWrapperJS != null) {
            return JavaSourceHelper.getClassType(jaxbOutputWrapperJS);
        }
        return null;
    }

    protected String getConverterName() throws IOException {
        String converterType = getConverterType();
        if (converterType == null) {
            return null;
        }
        return converterType.substring(converterType.lastIndexOf('.') + 1);
    }

    private String getParamInitStatements(WorkingCopy copy) {
        String comment = "// TODO: Assign a value to one of the following variables if you want to \n" + "// override the corresponding default value or value from the query \n" + "// parameter in the subresource class.\n"; //NOI18N
        String statements = ""; //NOI18N
        boolean addGetEntityStatement = false;

        for (ParameterInfo param : bean.getInputParameters()) {
            String initValue = "null"; //NOI18N
            String access = match(JavaSourceHelper.getTopLevelClassElement(copy), param.getName());

            if (access != null) {
                initValue = access;
                addGetEntityStatement = true;
            }

            statements += param.getSimpleTypeName() + " " + param.getName() + " = " + initValue + ";"; //NOI18N
        }

        String getEntityStatement = "";

        if (addGetEntityStatement) {
            getEntityStatement = getEntityType(JavaSourceHelper.getTopLevelClassElement(copy)) + " entity = getEntity()";
        }

        return comment + getEntityStatement + statements;
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

    private boolean hasGetEntityMethod(TypeElement typeElement) {
        List<ExecutableElement> methods = ElementFilter.methodsIn(typeElement.getEnclosedElements());
   
        for (ExecutableElement method : methods) {
            if (method.getSimpleName().contentEquals("getEntity")) {
                return true;
            }
        }

        return false;
    }

    private String getUriParam(TypeElement typeElement) {
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
                    if (JavaSourceHelper.isOfAnnotationType(m, RestConstants.URI_PARAM_ANNOTATION)) {
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

    private String getEntityType(TypeElement typeElement) {
        List<ExecutableElement> methods = ElementFilter.methodsIn(typeElement.getEnclosedElements());

        for (ExecutableElement method : methods) {
            if (method.getSimpleName().contentEquals("getEntity")) {
                return method.getReturnType().toString();
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
        //List<VariableElement> fields = ElementFilter.fieldsIn(targetClass.getEnclosedElements());
        String argName = arg.toLowerCase();
//        for (VariableElement field : fields) {
//            if (match(field.getSimpleName().toString(), argName)) {
//                return field.getSimpleName().toString();
//            }
//        }

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
                            return "entity." + m.getSimpleName().toString() + "()"; //NOI18N
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
        //System.out.println("targetResourceJS = " + targetResourceJS);
        //System.out.println("targetResourceType = " + targetResourceType);
        TypeElement representationType = JavaSourceHelper.getXmlRepresentationClass(targetResourceType, CONVERTER_SUFFIX);
        //System.out.println("representationType = " + representationType);
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
            xmlElementName = uriTemplate.substring(0, uriTemplate.length() - 1) + "Ref";
        } else {
            xmlElementName = uriTemplate + "Ref";
        }

        Object[] annotationAttrs = new Object[]{JavaSourceHelper.createAssignmentTree(copy, "name", xmlElementName)};

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
            FileObject dir = destDir.getParent().getFileObject(CONVERTER_FOLDER);
            if (dir != null) {
                converterDir = dir;
            }
        }
        return converterDir;
    }

    public Collection<String> getExistingUriTemplates() {
        if (existingUriTemplates == null) {
            existingUriTemplates = JavaSourceHelper.getAnnotationValuesForAllMethods(targetResourceJS, RestConstants.PATH);
        }

        return existingUriTemplates;
    }

    private String getAvailableUriTemplate() {
        //TODO: Need to create an unique UriTemplate value.
        Collection<String> existingUriTemplates = getExistingUriTemplates();
        int counter = 1;
        String uriTemplate = Inflector.getInstance().camelize(bean.getShortName(), true);
        String temp = uriTemplate;

        while (existingUriTemplates.contains(temp) || existingUriTemplates.contains(temp + "/")) {
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
