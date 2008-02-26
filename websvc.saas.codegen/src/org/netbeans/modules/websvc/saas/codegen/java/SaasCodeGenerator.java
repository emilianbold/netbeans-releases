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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean;
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
abstract public class SaasCodeGenerator extends AbstractGenerator {
    
    public static final String RESTCONNECTION_PACKAGE = "org.netbeans.saas";
    
    private FileObject targetFile; // resource file target of the drop
    private FileObject destDir;
    private FileObject wrapperResourceFile;
    private Project project;
    protected SaasBean bean;
    private JavaSource wrapperResourceJS;
    private JavaSource targetResourceJS;
    private JavaSource jaxbOutputWrapperJS;
    private String subresourceLocatorName;
    private String subresourceLocatorUriTemplate;
    private Collection<String> existingUriTemplates;
    private JTextComponent targetComponent;

    public SaasCodeGenerator(JTextComponent targetComponent, 
            FileObject targetFile, SaasBean bean) {
        this.targetComponent = targetComponent;
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

    protected JTextComponent getTargetComponent() {
        return this.targetComponent;
    }

    protected FileObject getTargetFile() {
        return this.targetFile;
    }
    
    protected FileObject getTargetFolder() {
        return this.destDir;
    }
    
    protected FileObject getWrapperResourceFile() {
        return this.wrapperResourceFile;
    }
    
    protected void setWrapperResourceFile(FileObject wrapperResourceFile) {
        this.wrapperResourceFile = wrapperResourceFile;
    }
    
    protected JavaSource getTargetSource() {
        return targetResourceJS;
    }
    
    protected JavaSource getWrapperResourceSource() {
        return wrapperResourceJS;
    }
    
    protected void setWrapperResourceSource(JavaSource wrapperResourceJS) {
        this.wrapperResourceJS = wrapperResourceJS;
    }
    
    protected JavaSource getJaxbOutputWrapperSource() {
        return this.jaxbOutputWrapperJS;
    }
    
    protected void setJaxbOutputWrapperSource(JavaSource jaxbOutputWrapperJS) {
       this.jaxbOutputWrapperJS = jaxbOutputWrapperJS;
    }
    
    protected Project getProject() {
        return this.project;
    }
    
    protected void preGenerate() throws IOException {
    }
    
    /*
     * Copy File only
     */    
    public void copyFile(String resourceName, File destFile) throws IOException {
        String path = resourceName;
        if(!destFile.exists()) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = this.getClass().getResourceAsStream(path);
                os = new FileOutputStream(destFile);
                int c;
                while ((c = is.read()) != -1) {
                    os.write(c);
                }
            } finally {
                if(os != null) {
                    os.flush();
                    os.close();
                }
                if(is != null)
                    is.close();            
            }
        }
    }

    abstract protected String getCustomMethodBody() throws IOException;
    
    public SaasBean getBean() {
        return bean;
    }

    public boolean canShowParam() {
        return wrapperResourceFile == null;
    }

    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        initProgressReporting(pHandle);

        preGenerate();

        FileObject outputWrapperFO = generateJaxbOutputWrapper();
        if (outputWrapperFO != null) {
            jaxbOutputWrapperJS = JavaSource.forFileObject(outputWrapperFO);
        }
        generateSaasServiceResourceClass();
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

    protected void generateSaasServiceResourceClass() throws IOException {
        if (wrapperResourceFile == null) {
            GenericResourceGenerator delegate = new GenericResourceGenerator(destDir, bean);
            delegate.setTemplate(bean.getResourceClassTemplate());
            Set<FileObject> files = delegate.generate(getProgressHandle());

            if (files == null || files.size() == 0) {
                return;
            }
            wrapperResourceFile = files.iterator().next();
            wrapperResourceJS = JavaSource.forFileObject(wrapperResourceFile);
            addImportsToWrapperResource();
            addSupportingMethods();
            modifyGetMethod();
        } else {
            wrapperResourceJS = JavaSource.forFileObject(wrapperResourceFile);
        }
    }
    
    protected void addImportsToWrapperResource() throws IOException {
        ModificationResult result = wrapperResourceJS.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                JavaSourceHelper.addImports(copy, new String[] {RESTCONNECTION_PACKAGE+"."+REST_CONNECTION});
            }
        });
        result.commit();
    }

    protected void addSubresourceLocator() throws IOException {
        ModificationResult result = targetResourceJS.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                JavaSourceHelper.addImports(copy, getSubresourceLocatorImports());

                ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                String[] annotations = new String[]{Constants.PATH_ANNOTATION};
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

    public String match(TypeElement targetClass, String arg) {
        String argName = arg.toLowerCase();
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

    protected void modifyTargetConverter() throws IOException {
        TypeElement targetResourceType = JavaSourceHelper.getTypeElement(targetResourceJS);
        TypeElement representationType = JavaSourceHelper.getXmlRepresentationClass(targetResourceType, CONVERTER_SUFFIX);
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
            existingUriTemplates = JavaSourceHelper.getAnnotationValuesForAllMethods(targetResourceJS, Constants.PATH);
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
    
    protected static void insert(String s, JTextComponent target, boolean reformat)
    throws BadLocationException {
        Document doc = target.getDocument();
        if (doc == null)
            return;
        
        if (s == null)
            s = "";
        
        if (doc instanceof BaseDocument)
            ((BaseDocument)doc).atomicLock();
        
        int start = insert(s, target, doc);
        
//        if (reformat && start >= 0 && doc instanceof BaseDocument) {  // format the inserted text
//            BaseDocument d = (BaseDocument) doc;
//            int end = start + s.length();
//            Formatter f = d.getFormatter();
//            
//            //f.reformat(d, start, end);
//            f.reformat(d, 0,d.getLength());
//        }
        
//        if (select && start >= 0) { // select the inserted text
//            Caret caret = target.getCaret();
//            int current = caret.getDot();
//            caret.setDot(start);
//            caret.moveDot(current);
//            caret.setSelectionVisible(true);
//        }
        
        if (doc instanceof BaseDocument)
            ((BaseDocument)doc).atomicUnlock();
    }
    
    protected static int insert(String s, JTextComponent target, Document doc)
    throws BadLocationException {
        
        int start = -1;
        try {
            //at first, find selected text range
            Caret caret = target.getCaret();
            int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
            doc.remove(p0, p1 - p0);
            
            //replace selected text by the inserted one
            start = caret.getDot();
            doc.insertString(start, s, null);
        } catch (BadLocationException ble) {}
        
        return start;
    }
    
    protected boolean isInBlock(JTextComponent target) {
        //TODO - FIX return true if the caret position where code is
        //going to be inserted is within some block other Class block.
        Caret caret = target.getCaret();
        int p0 = Math.min(caret.getDot(), caret.getMark());
        int p1 = Math.max(caret.getDot(), caret.getMark());
        return true;
    }
    
    public static void createRestConnectionFile(Project project) throws IOException {
        SourceGroup[] srcGrps = SourceGroupSupport.getJavaSourceGroups(project);
        String pkg = RESTCONNECTION_PACKAGE;
        FileObject targetFolder = SourceGroupSupport.getFolderForPackage(srcGrps[0],pkg , true);
        JavaSourceHelper.createJavaSource(REST_CONNECTION_TEMPLATE, targetFolder, pkg, REST_CONNECTION);
    }
}
