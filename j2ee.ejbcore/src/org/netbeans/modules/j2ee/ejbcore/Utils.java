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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore;

import java.util.ArrayList;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.action.CallEjbGenerator;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.filesystems.FileObject;

public class Utils {
    
    private static final String WIZARD_PANEL_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N
    private static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"; //NOI18N;
    
    public static String toClasspathString(File[] classpathEntries) {
        if (classpathEntries == null) {
            return "";
        }
        StringBuffer classpath = new StringBuffer();
        for (int i = 0; i < classpathEntries.length; i++) {
            classpath.append(classpathEntries[i].getAbsolutePath());
            if (i + 1 < classpathEntries.length) {
                classpath.append(':');
            }
        }
        return classpath.toString();
    }
    
    public static void notifyError(Exception exception) {
        NotifyDescriptor ndd = new NotifyDescriptor.Message(exception.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(ndd);
    }
    
    public static void mergeSteps(WizardDescriptor wizard, WizardDescriptor.Panel[] panels, String[] steps) {
        Object prop = wizard.getProperty(WIZARD_PANEL_CONTENT_DATA);
        String[] beforeSteps;
        int offset;
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
            offset = beforeSteps.length;
            if (offset > 0 && ("...".equals(beforeSteps[offset - 1]))) {// NOI18N
                offset--;
            }
        } else {
            beforeSteps = null;
            offset = 0;
        }
        String[] resultSteps = new String[ (offset) + panels.length];
        System.arraycopy(beforeSteps, 0, resultSteps, 0, offset);
        setSteps(panels, steps, resultSteps, offset);
    }
    
    private static void setSteps(WizardDescriptor.Panel[] panels, String[] steps, String[] resultSteps, int offset) {
        int numberOfSteps = steps == null ? 0 : steps.length;
        for (int i = 0; i < panels.length; i++) {
            final JComponent component = (JComponent) panels[i].getComponent();
            String step = i < numberOfSteps ? steps[i] : null;
            if (step == null) {
                step = component.getName();
            }
            component.putClientProperty(WIZARD_PANEL_CONTENT_DATA, resultSteps);
            component.putClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
            component.getAccessibleContext().setAccessibleDescription(step);
            resultSteps[i + offset] = step;
        }
    }
    
    public static void setSteps(WizardDescriptor.Panel[] panels, String[] steps) {
        setSteps(panels, steps, steps, 0);
    }
    
    public static boolean areInSameJ2EEApp(Project project1, Project project2) {
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < openProjects.length; i++) {
            Project project = openProjects[i];
            Object j2eeAppProvider = project.getLookup().lookup(J2eeApplicationProvider.class);
            if (j2eeAppProvider != null) { // == it is j2ee app
                J2eeApplicationProvider j2eeApp = (J2eeApplicationProvider)j2eeAppProvider;
                J2eeModuleProvider[] j2eeModules = j2eeApp.getChildModuleProviders();
                if ((j2eeModules != null) && (j2eeModules.length > 0)) { // == there are some modules in the j2ee app
                    J2eeModuleProvider affectedPrjProvider1 = project1.getLookup().lookup(J2eeModuleProvider.class);
                    J2eeModuleProvider affectedPrjProvider2 = project2.getLookup().lookup(J2eeModuleProvider.class);
                    if (affectedPrjProvider1 != null && affectedPrjProvider2 != null) {
                        List childModules = Arrays.asList(j2eeModules);
                        if (childModules.contains(affectedPrjProvider1) &&
                                childModules.contains(affectedPrjProvider2)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if j2eeModuleProvider is part of some open J2EE application
     */
    public static boolean isPartOfJ2eeApp(J2eeModuleProvider j2eeModuleProvider) {
        for (Project openProject : OpenProjects.getDefault().getOpenProjects()) {
            J2eeApplicationProvider j2eeAppProvider = openProject.getLookup().lookup(J2eeApplicationProvider.class);
            if (j2eeAppProvider != null) {
                if (Arrays.asList(j2eeAppProvider.getChildModuleProviders()).contains(j2eeModuleProvider)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // =========================================================================
    
    // utils for ejb code synchronization
    
    public static boolean canExposeInLocal(FileObject ejbClassFO, final ElementHandle<ExecutableElement> methodHandle) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        final String[] ejbClassName = new String[1];
        final MethodModel[] methodModel = new MethodModel[1];
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ExecutableElement executableElement = methodHandle.resolve(workingCopy);
                Set<Modifier> modifiers = executableElement.getModifiers();
                boolean signatureOk = modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.STATIC);
                if (signatureOk) {
                    Element enclosingElement = executableElement.getEnclosingElement();
                    ejbClassName[0] = ((TypeElement) enclosingElement).getQualifiedName().toString();
                    methodModel[0] = MethodModelSupport.createMethodModel(workingCopy, executableElement);
                }
            }
        });
        if (methodModel[0] != null) {
            EjbMethodController ejbMethodController = EjbMethodController.createFromClass(ejbClassFO, ejbClassName[0]);
            return ejbMethodController != null && ejbMethodController.hasLocal() && !ejbMethodController.hasMethodInInterface(methodModel[0], ejbMethodController.getMethodTypeFromImpl(methodModel[0]), true);
        }
        return false;
    }
    
    public static void exposeInLocal(FileObject ejbClassFO, final ElementHandle<ExecutableElement> methodHandle) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        final String[] ejbClassName = new String[1];
        final MethodModel[] methodModel = new MethodModel[1];
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ExecutableElement method = methodHandle.resolve(workingCopy);
                Element enclosingElement = method.getEnclosingElement();
                ejbClassName[0] = ((TypeElement) enclosingElement).getQualifiedName().toString();
                methodModel[0] = MethodModelSupport.createMethodModel(workingCopy, method);
            }
        });
        EjbMethodController ejbMethodController = EjbMethodController.createFromClass(ejbClassFO, ejbClassName[0]);
        ejbMethodController.createAndAddInterface(methodModel[0], true);
    }
    
    public static boolean canExposeInRemote(FileObject ejbClassFO, final ElementHandle<ExecutableElement> methodHandle) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        final String[] ejbClassName = new String[1];
        final MethodModel[] methodModel = new MethodModel[1];
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ExecutableElement executableElement = methodHandle.resolve(workingCopy);
                Set<Modifier> modifiers = executableElement.getModifiers();
                boolean signatureOk = modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.STATIC);
                if (signatureOk) {
                    Element enclosingElement = executableElement.getEnclosingElement();
                    ejbClassName[0] = ((TypeElement) enclosingElement).getQualifiedName().toString();
                    methodModel[0] = MethodModelSupport.createMethodModel(workingCopy, executableElement);
                }
            }
        });
        if (methodModel[0] != null) {
            EjbMethodController ejbMethodController = EjbMethodController.createFromClass(ejbClassFO, ejbClassName[0]);
            return ejbMethodController != null && ejbMethodController.hasRemote() && !ejbMethodController.hasMethodInInterface(methodModel[0], ejbMethodController.getMethodTypeFromImpl(methodModel[0]), true);
        }
        return false;
    }
    
    public static void exposeInRemote(FileObject ejbClassFO, final ElementHandle<ExecutableElement> methodHandle) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        final String[] ejbClassName = new String[1];
        final MethodModel[] methodModel = new MethodModel[1];
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ExecutableElement method = methodHandle.resolve(workingCopy);
                Element enclosingElement = method.getEnclosingElement();
                ejbClassName[0] = ((TypeElement) enclosingElement).getQualifiedName().toString();
                methodModel[0] = MethodModelSupport.createMethodModel(workingCopy, method);
            }
        });
        EjbMethodController ejbMethodController = EjbMethodController.createFromClass(ejbClassFO, ejbClassName[0]);
        ejbMethodController.createAndAddInterface(methodModel[0], false);
    }

    /** Returns list of all EJB projects that can be called from the caller project.
     *
     * @param enterpriseProject the caller enterprise project
     */
    public static Project [] getCallableEjbProjects(Project enterpriseProject) {
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        
        // TODO: HACK - this must be solved by freeform's own implementation of EnterpriseReferenceContainer, see issue 57003
        // call ejb should not make this check, all should be handled in EnterpriseReferenceContainer
        boolean isCallerFreeform = enterpriseProject.getClass().getName().equals("org.netbeans.modules.ant.freeform.FreeformProject");
        
        List<Project> filteredResults = new ArrayList<Project>(allProjects.length);
        for (int i = 0; i < allProjects.length; i++) {
            boolean isEJBModule = false;
            J2eeModuleProvider j2eeModuleProvider = allProjects[i].getLookup().lookup(J2eeModuleProvider.class);
            if (j2eeModuleProvider != null && j2eeModuleProvider.getJ2eeModule().getModuleType().equals(J2eeModule.EJB)) {
                isEJBModule = true;
            }
            if ((isEJBModule && !isCallerFreeform) ||
                    (isCallerFreeform && enterpriseProject.equals(allProjects[i]))) {
                filteredResults.add(allProjects[i]);
            }
        }
        return filteredResults.toArray(new Project[filteredResults.size()]);
    }
    
//TODO: this method should be removed and org.netbeans.modules.j2ee.common.Util.isJavaEE5orHigher(Project project)
//should be called instead of it
    public static boolean isJavaEE5orHigher(Project project) {
        if (project == null) {
            return false;
        }
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            J2eeModule j2eeModule = j2eeModuleProvider.getJ2eeModule();
            if (j2eeModule != null) {
                Object type = j2eeModule.getModuleType();
                double version = Double.parseDouble(j2eeModule.getModuleVersion());
                if (J2eeModule.EJB.equals(type) && (version > 2.1)) {
                    return true;
                }
                if (J2eeModule.WAR.equals(type) && (version > 2.4)) {
                    return true;
                }
                if (J2eeModule.CLIENT.equals(type) && (version > 1.4)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean isAppClient(Project project) {
        J2eeModuleProvider module = project.getLookup().lookup(J2eeModuleProvider.class);
        return  (module != null) ? module.getJ2eeModule().getModuleType().equals(J2eeModule.CLIENT) : false;
    }
    
    /**
     * @return true if given <code>target</code> is defined in a Java SE environment.
     */
    public static boolean isTargetJavaSE(FileObject fileObject, final String className) throws IOException{
        Project owner = FileOwnerQuery.getOwner(fileObject);
        if (owner.getLookup().lookup(J2eeModuleProvider.class) == null){
            return true;
        }
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final boolean[] result = new boolean[] { false };
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(className);
                TypeElement junitTestCase = controller.getElements().getTypeElement("junit.framework.TestCase");
                if (junitTestCase != null) {
                    result[0] = controller.getTypes().isSubtype(typeElement.asType(), junitTestCase.asType());
                }
            }
        }, true);
        return result[0];
    }
    
//    /**
//     * @return true if given <code>javaClass</code> is a subtype (direct or
//     * indirect) of <code>junit.framework.TestCase</code>.
//     */
//    private static boolean extendsTestCase(CompilationController controller, TypeElement typeElement){
//        if (typeElement == null){
//            return false;
//        }
//        if (typeElement.getQualifiedName().contentEquals("junit.framework.TestCase")){
//            return true;
//        }
//        DeclaredType superClassType = (DeclaredType) typeElement.getSuperclass();
//        return extendsTestCase(controller, (TypeElement) superClassType.asElement());
//    }
    
    /**
     * Converts the given <code>jndiName</code> to camel case, i.e. removes
     * all <code>/</code> characters and converts characters to upper case appropriately.
     * For example, returns <code>SomeJndiName</code> for <code>some/jndi/name</code> or 
     * <code>someJndiName</code> if <code>lowerCaseFirstChar</code> is true.
     * @param jndiName the JNDI name to convert; must not be null.
     * @param lowerCaseFirstChar defines whether the first char of the resulting name
     * should be lower case (note that if all chars of the given <code>jndiName</code> are
     * uppercase characters, its first char will not be converted to lower case even 
     * if this param is true).
     * @param prefixToStrip the prefix that will be stripped from the resulting name. If null, 
     * nothing will be stripped.
     * @return String representing the converted name.
     */
    public static String jndiNameToCamelCase(String jndiName, boolean lowerCaseFirstChar, String prefixToStrip){
        
        String strippedJndiName = jndiName;
        if (prefixToStrip != null && jndiName.startsWith(prefixToStrip)){
            strippedJndiName = jndiName.substring(jndiName.indexOf(prefixToStrip) + prefixToStrip.length());
        }
        
        StringBuilder result = new StringBuilder();
        
        for (String token : strippedJndiName.split("/")){
            if (token.length() == 0){
                continue;
            }
            char firstChar = token.charAt(0);
            if (lowerCaseFirstChar && result.length() == 0 && !isAllUpperCase(token)){
                firstChar = Character.toLowerCase(firstChar);
            } else {
                firstChar = Character.toUpperCase(firstChar);
            }
            result.append(firstChar);
            result.append(token.substring(1));
        }
        
        return result.toString();
    }

    public static AntArtifact getAntArtifact(final EjbReference ejbReference) throws IOException {
        
        MetadataModel<EjbJarMetadata> ejbReferenceMetadataModel = ejbReference.getEjbModule().getMetadataModel();
        FileObject ejbReferenceEjbClassFO = ejbReferenceMetadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, FileObject>() {
            public FileObject run(EjbJarMetadata metadata) throws Exception {
                return metadata.findResource(toResourceName(ejbReference.getEjbClass()));
            }
        });

        Project project = FileOwnerQuery.getOwner(ejbReferenceEjbClassFO);
        AntArtifact[] antArtifacts = AntArtifactQuery.findArtifactsByType(project, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        boolean hasArtifact = (antArtifacts != null && antArtifacts.length > 0);
        
        return hasArtifact ? antArtifacts[0] : null;
        
    }
 
    /**
     * Creates resource name from fully-qualified class name by
     * replacing '.' with '/' and appending ".java"
     */
    public static String toResourceName(String className) {
        assert className != null: "cannot find null className";
        return className.replace('.', '/') + ".java";
    }
    
    /**
     * @return true if the given <code>str</code> has more than one char 
     *  and all its chars are uppercase, false otherwise.
     */
    private static boolean isAllUpperCase(String str){
        if (str.length() <= 1){
            return false;
        }
        for (char c : str.toCharArray()) {
            if (Character.isLowerCase(c)){
                return false;
            }
        }
        return true;
    }

//    public static ExecutableElement[] getMethods(EjbMethodController c, boolean checkLocal, boolean checkRemote) {
//        List<ExecutableElement> methods = new ArrayList<ExecutableElement>();
//        List features;
//        for (ExecutableElement method : ElementFilter.methodsIn(c.getBeanClass().getEnclosedElements())) {
//            methods.add(method);
//        }
//        if (checkLocal) {
//            for (TypeElement interfaceCE : c.getLocalInterfaces()) {
//                for (ExecutableElement method : ElementFilter.methodsIn(interfaceCE.getEnclosedElements())) {
//                    methods.add(method);
//                }
//            }
//        }
//        if (checkRemote) {
//            for (TypeElement interfaceCE : c.getRemoteInterfaces()) {
//                for (ExecutableElement method : ElementFilter.methodsIn(interfaceCE.getEnclosedElements())) {
//                    methods.add(method);
//                }
//            }
//        }
//        ExecutableElement[] methodsArray = methods.toArray(new ExecutableElement[methods.size()]);
//        return methodsArray;
//    }
    
}
