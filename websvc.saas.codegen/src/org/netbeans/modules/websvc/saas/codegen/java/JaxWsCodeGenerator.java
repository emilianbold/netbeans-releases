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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.MimeType;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.saas.codegen.java.model.JaxwsOperationInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.WsdlSaasBean;
import org.netbeans.modules.websvc.saas.codegen.java.support.AbstractTask;
import org.netbeans.modules.websvc.saas.codegen.java.support.Inflector;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.SourceGroupSupport;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import static com.sun.source.tree.Tree.Kind.*;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class JaxWsCodeGenerator extends AbstractGenerator {

    public static final String QNAME = "javax.xml.namespace.QName";
    public static final String WS_BINDING_PROVIDER = "com.sun.xml.ws.developer.WSBindingProvider";
    public static final String HEADERS = "com.sun.xml.ws.api.message.Headers";
    
    protected FileObject targetFile; // resource file target of the drop
    protected FileObject destDir;
    protected FileObject wrapperResourceFile;
    protected Project project;
    protected WsdlSaasBean bean;
    protected JavaSource wrapperResourceJS;
    protected JavaSource targetResourceJS;
    protected JavaSource jaxbOutputWrapperJS;
    protected String subresourceLocatorName;
    protected String subresourceLocatorUriTemplate;
    private Collection<String> existingUriTemplates;
    public static final String SET_HEADER_PARAMS = "setHeaderParameters";

    public JaxWsCodeGenerator(JTextComponent targetComponent, 
            FileObject targetFile, WsdlSaasMethod m) throws IOException {
        this(targetComponent, targetFile, new WsdlSaasBean(m, FileOwnerQuery.getOwner(targetFile)));
    }

    private JaxWsCodeGenerator(JTextComponent targetComponent, 
            FileObject targetFile, WsdlSaasBean bean) {
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

    public boolean wrapperResourceExists() {
        return wrapperResourceFile != null;
    }

    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        initProgressReporting(pHandle);

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

    public boolean showParams() {
        return wrapperResourceFile == null;
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
    
    public WsdlSaasBean getBean() {
        return (WsdlSaasBean) bean;
    }

    public void addSupportingMethods() throws IOException {
        if (getBean().getHeaderParameters().isEmpty()) {
            return;
        }
        ModificationResult result = wrapperResourceJS.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                JavaSourceHelper.addImports(copy, new String[] { QNAME, WS_BINDING_PROVIDER, HEADERS} );
                ClassTree initial = JavaSourceHelper.getTopLevelClassTree(copy);
                ClassTree tree = addSetHeaderParamsMethod(copy, initial, getBean().lastOperationInfo().getPort().getJavaName());
                copy.rewrite(initial, tree);
            }}
        );
        result.commit();
    }

    protected String getCustomMethodBody() throws IOException {
        String methodBody = "try { ";
        String converterName = getConverterName();
        if (converterName != null) {
            methodBody += "$CONVERTER$ representation = new $CONVERTER$(); ".
                replace("$CONVERTER$", getConverterName());
        }
        JaxwsOperationInfo[] operations = ((WsdlSaasBean) bean).getOperationInfos();
        for (JaxwsOperationInfo info : operations) {
            methodBody += getWSInvocationCode(info);
        }

        if (getBean().needsHtmlRepresentation()) {
            methodBody += "return result;"; //NOI18N
        } else {
            methodBody += "return representation;"; //NOI18N
        }
        methodBody += "} catch(Exception ex) { //TODO handle \n throw new WebApplicationException(ex); }";
        return methodBody;
    }
    
    private static final String HINT_INIT_ARGUMENTS = " // TODO initialize WS operation arguments here\n"; //NOI18N
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    // {7} = service field name
    private static final String JAVA_SERVICE_DEF = "   {0} {7} = new {0}();\n"; //NOI18N
    private static final String JAVA_PORT_DEF = "   {1} port = {7}.{2}();\n"; //NOI18N
    private static final String JAVA_RESULT = "   {3}" + "   // TODO process result here\n" + "   {4} result = port.{5}({6});\n"; //NOI18N
    private static final String JAVA_VOID = "   {3}" + "   port.{5}({6});\n"; //NOI18N
    private static final String JAVA_OUT = "   {8}.println(\"Result = \"+result);\n"; //NOI18N
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    private static final String JAVA_STATIC_STUB_ASYNC_POLLING = "\ntry '{' // Call Web Service Operation(async. polling)\n" + "   {0} service = new {0}();\n" + "   {1} port = service.{2}();\n" + "   {3}" + "   // TODO process asynchronous response here\n" + "   {4} resp = port.{5}({6});\n" + "   while(!resp.isDone()) '{'\n" + "       // do something\n" + "       Thread.sleep(100);\n" + "   '}'\n" + "   System.out.println(\"Result = \"+resp.get());\n" + "'}' catch (Exception ex) '{'\n" + "   // TODO handle custom exceptions here\n" + "'}'\n"; //NOI18N
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    // {7} = response type (e.g. FooResponse)
    private static final String JAVA_STATIC_STUB_ASYNC_CALLBACK = "\ntry '{' // Call Web Service Operation(async. callback)\n" + "   {0} service = new {0}();\n" + "   {1} port = service.{2}();\n" + "   {3}" + "       public void handleResponse(javax.xml.ws.Response<{7}> response) '{'\n" + "           try '{'\n" + "               // TODO process asynchronous response here\n" + "               System.out.println(\"Result = \"+ response.get());\n" + "           '}' catch(Exception ex) '{'\n" + "               // TODO handle exception\n" + "           '}'\n" + "       '}'\n" + "   '}';\n" + "   {4} result = port.{5}({6});\n" + "   while(!result.isDone()) '{'\n" + "       // do something\n" + "       Thread.sleep(100);\n" + "   '}'\n" + "'}' catch (Exception ex) '{'\n" + "   // TODO handle custom exceptions here\n" + "'}'\n"; //NOI18N

    /**
     * Add JAXWS client code for invoking the given operation at current position.
     */
    private String getWSInvocationCode(JaxwsOperationInfo info) throws IOException {
        //Collect java names for invocation code
        String wsdlUrl = info.getWsdlURL();
        final String serviceJavaName = info.getService().getJavaName();
        String portJavaName = info.getPort().getJavaName();
        String operationJavaName = info.getOperation().getJavaName();
        String portGetterMethod = info.getPort().getPortGetter();
        String serviceFieldName = "service"; //NOI18N
        String returnTypeName = info.getOperation().getReturnTypeName();
        List<WsdlParameter> outArguments = info.getOutputParameters();
        String responseType = "Object"; //NOI18N
        String callbackHandlerName = "javax.xml.ws.AsyncHandler"; //NOI18N
        String argumentInitializationPart = "";
        String argumentDeclarationPart = "";
        try {
            StringBuffer argumentBuffer1 = new StringBuffer();
            StringBuffer argumentBuffer2 = new StringBuffer();
            for (int i = 0; i < outArguments.size(); i++) {
                String argumentTypeName = outArguments.get(i).getTypeName();
                if (argumentTypeName.startsWith("javax.xml.ws.AsyncHandler")) {
                    //NOI18N
                    responseType = resolveResponseType(argumentTypeName);
                    callbackHandlerName = argumentTypeName;
                }
                String argumentName = outArguments.get(i).getName();
                argumentBuffer1.append("\t" + argumentTypeName + " " + argumentName + " = " + resolveInitValue(argumentTypeName) + "\n"); //NOI18N
            }

            List<WsdlParameter> parameters = info.getOperation().getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                String argument = parameters.get(i).getName();
                argumentBuffer2.append(i > 0 ? ", " + argument : argument); //NOI18N
            }
            argumentInitializationPart = (argumentBuffer1.length() > 0 ? "\t" + HINT_INIT_ARGUMENTS + argumentBuffer1.toString() : "");
            argumentDeclarationPart = argumentBuffer2.toString();
        } catch (NullPointerException npe) {
            // !PW notify failure to extract service information.
            npe.printStackTrace();
            String message = NbBundle.getMessage(JaxWsCodeGenerator.class, "ERR_FailedUnexpectedWebServiceDescriptionPattern"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(message, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
        }

        boolean success = false;

        final boolean[] insertServiceDef = {true};
        final String[] printerName = {"System.out"}; // NOI18N
        final String[] argumentInitPart = {argumentInitializationPart};
        final String[] argumentDeclPart = {argumentDeclarationPart};
        final String[] serviceFName = {serviceFieldName};
        final boolean[] generateWsRefInjection = {false};
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {

            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                CompilationUnitTree cut = controller.getCompilationUnit();
                ClassTree classTree = JavaSourceHelper.findPublicTopLevelClass(controller);
                generateWsRefInjection[0] = JavaSourceHelper.isInjectionTarget(controller);
                insertServiceDef[0] = !generateWsRefInjection[0];

                // compute the service field name
                if (generateWsRefInjection[0]) {
                    Set<String> serviceFieldNames = new HashSet<String>();
                    boolean injectionExists = false;
                    int memberOrder = 0;
                    for (Tree member : classTree.getMembers()) {
                        // for the first inner class in top level
                        ++memberOrder;
                        if (VARIABLE == member.getKind()) {
                            // get variable type
                            VariableTree var = (VariableTree) member;
                            Tree typeTree = var.getType();
                            TreePath typeTreePath = controller.getTrees().getPath(cut, typeTree);
                            TypeElement typeEl = JavaSourceHelper.getTypeElement(controller, typeTreePath);
                            if (typeEl != null) {
                                String variableType = typeEl.getQualifiedName().toString();
                                if (serviceJavaName.equals(variableType)) {
                                    serviceFName[0] = var.getName().toString();
                                    generateWsRefInjection[0] = false;
                                    injectionExists = true;
                                    break;
                                }
                            }
                            serviceFieldNames.add(var.getName().toString());
                        }
                    }
                    if (!injectionExists) {
                        serviceFName[0] = findProperServiceFieldName(serviceFieldNames);
                    }
                }
            }

            public void cancel() {
            }
        };

        // create the inserted text
        wrapperResourceJS.runUserActionTask(task, true);

        String invocationBody = getJavaInvocationBody(info.getOperation(), insertServiceDef[0], serviceJavaName, portJavaName, portGetterMethod, argumentInitPart[0], returnTypeName, operationJavaName, argumentDeclPart[0], serviceFName[0], printerName[0], responseType);

        if (!getBean().needsHtmlRepresentation()) {
            List<WsdlParameter> outParams = info.getOutputParameters();
            String outputClassName = SourceGroupSupport.getClassName(info.getOutputType());
            invocationBody += "representation.set" + outputClassName + "(";
            if (Constants.VOID.equals(returnTypeName) && outParams.size() > 0) {
                invocationBody += outParams.get(0).getName() + ".value);";
            } else if (!Constants.VOID.equals(returnTypeName)) {
                invocationBody += "result);";
            } else {
                throw new IllegalArgumentException("Unsupported return type " + returnTypeName);
            }
        }

        if (generateWsRefInjection[0]) {
            insertServiceRef(wrapperResourceJS, info, serviceFieldName);
        }
        return invocationBody;
    }

    private void insertServiceRef(JavaSource targetJS, JaxwsOperationInfo info, final String serviceFieldName) throws IOException {
        final String serviceJavaName = info.getService().getJavaName();
        String wsdlUrl = info.getWsdlURL();

        if (wsdlUrl.startsWith("file:")) {
            //NOI18N
            wsdlUrl = info.getWsdlLocation();
        }
        final String localWsdlUrl = wsdlUrl;
        CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                TypeElement typeElement = JavaSourceHelper.getTopLevelClassElement(workingCopy);
                ClassTree javaClass = JavaSourceHelper.getClassTree(workingCopy, typeElement);
                VariableTree serviceRefInjection = generateServiceRefInjection(workingCopy, make, serviceFieldName, serviceJavaName, localWsdlUrl);
                ClassTree modifiedClass = make.insertClassMember(javaClass, 0, serviceRefInjection);
                workingCopy.rewrite(javaClass, modifiedClass);
            }

            public void cancel() {
            }
        };
        targetJS.runModificationTask(modificationTask).commit();
    }

    /**
     * Determines the initialization value of a variable of type "type"
     * @param type Type of the variable
     * @param targetFile FileObject containing the class that declares the type
     */
    private static String resolveInitValue(String type) {
        if (type.startsWith("javax.xml.ws.Holder")) {
            //NOI18N
            return "new " + type + "();";
        }
        if ("int".equals(type) || "long".equals(type) || "short".equals(type) || "byte".equals(type)) {
            //NOI18N
            return "0;"; //NOI18N
        }
        if ("boolean".equals(type)) {
            //NOI18N
            return "false;"; //NOI18N
        }
        if ("float".equals(type) || "double".equals(type)) {
            //NOI18N
            return "0.0;"; //NOI18N
        }
        if ("java.lang.String".equals(type)) {
            //NOI18N
            return "\"\";"; //NOI18N
        }
        if (type.endsWith("CallbackHandler")) {
            //NOI18N
            return "new " + type + "();"; //NOI18N
        }
        if (type.startsWith("javax.xml.ws.AsyncHandler")) {
            //NOI18N
            return "new " + type + "() {"; //NOI18N
        }

        return "null;"; //NOI18N
    }

    private static String resolveResponseType(String argumentType) {
        int start = argumentType.indexOf("<");
        int end = argumentType.indexOf(">");
        if (start > 0 && end > 0 && start < end) {
            return argumentType.substring(start + 1, end);
        } else {
            return "javax.xml.ws.Response"; //NOI18N
        }
    }
    
    public static final String SET_HEADER_PARAMS_CALL = SET_HEADER_PARAMS + "(port); \n";

    private String getJavaInvocationBody(WsdlOperation operation, 
            boolean insertServiceDef, String serviceJavaName, String portJavaName, 
            String portGetterMethod, String argumentInitializationPart, 
            String returnTypeName, String operationJavaName, String argumentDeclarationPart, 
            String serviceFieldName, String printerName, String responseType) {

        String invocationBody = "";
        String setHeaderParams = getBean().getHeaderParameters().size() > 0 ? SET_HEADER_PARAMS_CALL : "" ;
        Object[] args = new Object[]{serviceJavaName, portJavaName, portGetterMethod, argumentInitializationPart, returnTypeName, operationJavaName, argumentDeclarationPart, serviceFieldName, printerName};
        switch (operation.getOperationType()) {
            case WsdlOperation.TYPE_NORMAL:
                {
                    if ("void".equals(returnTypeName)) {
                        //NOI18N
                        String body = (insertServiceDef ? JAVA_SERVICE_DEF : "") + setHeaderParams + JAVA_PORT_DEF + JAVA_VOID;
                        invocationBody += MessageFormat.format(body, args);
                    } else {
                        String body = (insertServiceDef ? JAVA_SERVICE_DEF : "") + JAVA_PORT_DEF + setHeaderParams + JAVA_RESULT + JAVA_OUT;
                        invocationBody += MessageFormat.format(body, args);
                    }
                    break;
                }
            case WsdlOperation.TYPE_ASYNC_POLLING:
                {
                    invocationBody += MessageFormat.format(JAVA_STATIC_STUB_ASYNC_POLLING, args);
                    break;
                }
            case WsdlOperation.TYPE_ASYNC_CALLBACK:
                {
                    args[7] = responseType;
                    invocationBody += MessageFormat.format(JAVA_STATIC_STUB_ASYNC_CALLBACK, args);
                    break;
                }
        }
        return invocationBody;
    }

    private static VariableTree generateServiceRefInjection(WorkingCopy workingCopy, TreeMaker make, String fieldName, String fieldType, String wsdlUrl) {
        TypeElement wsRefElement = workingCopy.getElements().getTypeElement("javax.xml.ws.WebServiceRef"); //NOI18N
        AnnotationTree wsRefAnnotation = make.Annotation(make.QualIdent(wsRefElement), Collections.<ExpressionTree>singletonList(make.Assignment(make.Identifier("wsdlLocation"), make.Literal(wsdlUrl))));
        // create method modifier: public and no annotation
        ModifiersTree methodModifiers = make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC), Collections.<AnnotationTree>singletonList(wsRefAnnotation));
        TypeElement typeElement = workingCopy.getElements().getTypeElement(fieldType);

        VariableTree var = make.Variable(methodModifiers, fieldName, make.Type(typeElement.asType()), null);
        return make.Variable(make.Modifiers(var.getModifiers().getFlags(), var.getModifiers().getAnnotations()), var.getName(), var.getType(), var.getInitializer());
    }

    private static String findProperServiceFieldName(Set serviceFieldNames) {
        String name = "service";
        int i = 0;
        while (serviceFieldNames.contains(name)) {
            name = "service_" + String.valueOf(++i);
        }
        return name; //NOI18N
    }

    private ClassTree addSetHeaderParamsMethod(WorkingCopy copy, ClassTree tree, String portJavaType) {
        Modifier[] modifiers = Constants.PRIVATE;
        String[] annotations = new String[0];
        Object[] annotationAttrs = new Object[0];
        Object returnType = Constants.VOID;
        String bodyText = "{ WSBindingProvider bp = (WSBindingProvider)port;";
        bodyText += "bp.setOutboundHeaders(";
        boolean first = true;
        for (ParameterInfo pinfo : getBean().getHeaderParameters()) {
            if (pinfo.getDefaultValue() == null) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                bodyText += ", \n ";
            }
            String namespaceUri = pinfo.getQName().getNamespaceURI();
            bodyText += "Headers.create(new QName(";
            if (namespaceUri != null) {
                bodyText += "\""+ namespaceUri +"\",";
            }
            bodyText += "\"" + pinfo.getName()+"\"), \""+pinfo.getDefaultValue()+"\")";
        }
        bodyText += ");";
        String[] parameters = new String[] { "port" };
        Object[] paramTypes = new Object[] { portJavaType };
        String[] paramAnnotations = new String[0];
        Object[] paramAnnotationAttrs = new String[0];
        String comment = null;

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                SET_HEADER_PARAMS, returnType, parameters, paramTypes, //NOI18N
                paramAnnotations, paramAnnotationAttrs,
                bodyText, comment);      //NOI18N
    }

    protected FileObject generateJaxbOutputWrapper() throws IOException {
        if (getBean().needsHtmlRepresentation()) {
            return null;
        }
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
}