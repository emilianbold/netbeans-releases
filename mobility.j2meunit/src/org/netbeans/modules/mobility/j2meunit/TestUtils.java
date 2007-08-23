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

package org.netbeans.modules.mobility.j2meunit;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileUtil;

/**
 * This is a utilities class used by the J2MEUnit test generator
 *
 * @author bohemius
 */
public class TestUtils {

    static final String TEST_CLASSNAME_PREFIX = NbBundle.getMessage(
            TestUtils.class, "PROP_test_classname_prefix");                //NOI18N
    static final String TEST_CLASSNAME_SUFFIX = NbBundle.getMessage(
            TestUtils.class, "PROP_test_classname_suffix");                //NOI18N
    static final String SUITE_CLASSNAME_PREFIX = NbBundle.getMessage(
            TestUtils.class, "PROP_suite_classname_prefix");               //NOI18N
    static final String SUITE_CLASSNAME_SUFFIX = NbBundle.getMessage(
            TestUtils.class, "PROP_suite_classname_suffix");               //NOI18N
    static final boolean GENERATE_TESTS_FROM_TEST_CLASSES = NbBundle.getMessage(
            TestUtils.class, "PROP_generate_tests_from_test_classes").equals("true");    //NOI18N
    static final String TEST_METHODNAME_PREFIX = NbBundle.getMessage(
            TestUtils.class, "PROP_test_method_prefix");                   //NOI18N
    static final String TEST_METHODNAME_SUFFIX = NbBundle.getMessage(
            TestUtils.class, "PROP_test_method_suffix");
    static final String TEST_RUNNER_NAME = "TestRunnerMIDlet";//NOI18N TODO add to bundle

    public static Set<Modifier> createModifierSet(Modifier... modifiers) {
        EnumSet<Modifier> modifierSet = EnumSet.noneOf(Modifier.class);
        for (Modifier m : modifiers) {
            modifierSet.add(m);
        }
        return modifierSet;
    }

    public static String getTestClassName(String sourceClassName) {
        return TEST_CLASSNAME_PREFIX + sourceClassName + TEST_CLASSNAME_SUFFIX;
    }

    public static String getSimpleName(String fullName) {
        int lastDotIndex = fullName.lastIndexOf('.');
        return (lastDotIndex == -1) ? fullName
                : fullName.substring(lastDotIndex + 1);
    }

    public static String getPackageName(String fullName) {
        if (fullName != null) {
            int i = fullName.lastIndexOf('.');
            return (i != -1) ? fullName.substring(0, i) : ""; //NOI18N
        } else
            return "";
    }

    public static String getTestClassFullName(String sourceClassName, String packageName) {
        String shortTestClassName = getTestClassName(sourceClassName);
        return ((packageName == null) || (packageName.length() == 0))
                ? shortTestClassName
                : packageName.replace('.', '/') + '/' + shortTestClassName;
    }

    public static String createTestMethodName(String smName) {
        return "test"                                                   //NOI18N
                + smName.substring(0, 1).toUpperCase() + smName.substring(1);
    }

    static void addTestClassProperty(Project p, AntProjectHelper aph, String clazz) throws IOException {
        ProjectConfigurationsHelper pch=(ProjectConfigurationsHelper) p.getLookup().lookup(ProjectConfigurationsHelper.class);
        Collection<ProjectConfiguration> confs=pch.getConfigurations();
        EditableProperties ep=aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ReferenceHelper refHelper=p.getLookup().lookup(ReferenceHelper.class);
        String defaultValue=ep.getProperty(DefaultPropertiesDescriptor.MANIFEST_JAD);
        HashMap map = defaultValue != null ? (HashMap) DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.decode(defaultValue,aph,refHelper)  : new HashMap();
        addTestClassProperty(map,clazz);
        ep.put(DefaultPropertiesDescriptor.MANIFEST_JAD, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.encode(map,aph,refHelper));

        for (ProjectConfiguration conf : confs) {
            String confName = conf.getDisplayName();
            String propertyName = VisualPropertySupport.translatePropertyName(confName, DefaultPropertiesDescriptor.MANIFEST_JAD, false);
            if (propertyName == null)
                continue;
            String propertyValue = ep.getProperty(propertyName);
            if (propertyValue == null)
                continue;
            map = (HashMap) DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.decode(defaultValue,aph,refHelper);
            addTestClassProperty(map, clazz);
            ep.put(propertyName, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.encode(map,aph,refHelper));
        }

        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);

        ProjectManager.getDefault().saveProject(p);        
    } 
    
    private static void addTestClassProperty(Map map, String clazz) {
        String key = NbBundle.getMessage(TestUtils.class, "PROP_config_TestClasses_key");//NOI18N
        if (map.containsKey(key)) {
            String prop = (String) map.get(key);
            if (prop.indexOf(clazz) < 0) {
                if (prop == null || prop.equals(""))
                    prop = clazz;
                else
                    prop = prop + " " + clazz;
                map.put(key, prop);
            }
        } else
            map.put(key, clazz);
    }
    

    static void addTestRunnerMIDletProperty(Project project, AntProjectHelper h) throws IOException {
        String name=NbBundle.getMessage(TestUtils.class,"PROP_config_TestRunner_name");//NOI18N
        String clazz=NbBundle.getMessage(TestUtils.class,"PROP_config_TestRunner_clazz");//NOI18N
        String icon=NbBundle.getMessage(TestUtils.class,"PROP_config_TestRunner_icon");//NOI18N

        ProjectConfigurationsHelper confHelper = project.getLookup().lookup(ProjectConfigurationsHelper.class);
        Collection<ProjectConfiguration> confs=confHelper.getConfigurations();
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String defaultValue = ep.getProperty(DefaultPropertiesDescriptor.MANIFEST_MIDLETS);        

        HashMap map = defaultValue != null ? (HashMap)DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.decode(defaultValue,null,null) : new HashMap();
        addTestRunnerMIDletProperty(map, name, clazz, icon);
        ep.put(DefaultPropertiesDescriptor.MANIFEST_MIDLETS, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.encode(map,null,null));

        for (ProjectConfiguration conf : confs) {
            String confName = conf.getDisplayName();
            String propertyName = VisualPropertySupport.translatePropertyName(confName, DefaultPropertiesDescriptor.MANIFEST_MIDLETS, false);
            if (propertyName == null)
                continue;
            String propertyValue = ep.getProperty(propertyName);
            if (propertyValue == null)
                continue;
            map = (HashMap)DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.decode(defaultValue,null,null);
            addTestRunnerMIDletProperty(map, name, clazz, icon);
            ep.put(propertyName, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.encode(map,null,null));
        }

        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(project);
    }
    

    private static void addTestRunnerMIDletProperty(Map map, String name, String clazz, String icon) {
        int a = 1;
        boolean flag = false;
        while (map.containsKey("MIDlet-" + a)) { //NOI18N
            String midletDef = (String) map.get("MIDlet-" + a); //NOI18N
            if (midletDef.contains(NbBundle.getMessage(TestUtils.class, "PROP_config_TestRunner_clazz")))
                flag = true;
            a++;
        }
        if (!flag)
            map.put("MIDlet-" + a, name + ", " + icon + ", " + clazz);  //NOI18N
    }

    public static FileObject getTestFileObject(FileObject classFile) {
        String testClassName = TEST_CLASSNAME_PREFIX + classFile.getName() + TEST_CLASSNAME_SUFFIX;
        String absolutePath = FileUtil.toFile(classFile).getAbsolutePath();

        String directoryPath = absolutePath.substring(absolutePath.indexOf(classFile.getName()));
        File testFile = new File(directoryPath + testClassName + ".java");
        return FileUtil.toFileObject(testFile);
    }

    /*
     *
     * The retouche stuff for finding classes and element handles
     * for JavaSource
     *
     */
    public static boolean testMethodExists(ClassTree tstClass, String testMethodName) {
        assert tstClass.getKind() == Tree.Kind.CLASS;
        List<? extends Tree> members = tstClass.getMembers();
        for (Tree member : members) {
            if (member instanceof MethodTree) {
                MethodTree methodTree = (MethodTree) member;
                if (methodTree.getName().toString().equals(testMethodName))
                    return true;
            }
        }
        return false;
    }

    private static boolean isTestable(ClassTree typeDecl,
                                      TreeUtilities treeUtils) {
        return !treeUtils.isAnnotation(typeDecl);
    }


    static boolean isTestable(FileObject fo) {
        if (!(fo.getName().endsWith(TEST_RUNNER_NAME) || fo.getName().endsWith(TEST_CLASSNAME_SUFFIX)) && fo.getExt().equals("java")) //NOI18N
            return true;
        return false;
    }

    static boolean isTestable(Element typeDeclElement) {
        ElementKind elemKind = typeDeclElement.getKind();
        return (elemKind != ElementKind.ANNOTATION_TYPE)
                && (elemKind.isClass() || elemKind.isInterface());
    }

    static boolean isTestMethod(MethodTree testMethod) {
        String testMethodName = testMethod.getName().toString();
        if (testMethodName.startsWith(NbBundle.getMessage(TestUtils.class, "PROP_test_method_prefix")) && //NOI18N
                !testMethodName.equals(NbBundle.getMessage(TestUtils.class, "PROP_test_method_prefix")) &&
                testMethodName.endsWith(NbBundle.getMessage(TestUtils.class, "PROP_test_method_suffix"))) //NOI18N
            return true;
        else return false;
    }

    private static List<TypeElement> findTopClassElems(CompilationInfo compInfo, CompilationUnitTree compilationUnit) {
        List<? extends Tree> typeDecls = compilationUnit.getTypeDecls();
        if ((typeDecls == null) || typeDecls.isEmpty()) {
            return Collections.<TypeElement>emptyList();
        }

        List<TypeElement> result = new ArrayList<TypeElement>(typeDecls.size());

        Trees trees = compInfo.getTrees();
        for (Tree typeDecl : typeDecls) {
            if (typeDecl.getKind() == Tree.Kind.CLASS) {
                Element element = trees.getElement(new TreePath(new TreePath(compilationUnit), typeDecl));
                TypeElement typeElement = (TypeElement) element;
                if (isTestable(element)) {
                    result.add(typeElement);
                }
            }
        }
        return result;
    }

    private static List<ElementHandle<TypeElement>> findTopClassElemHandles(
            CompilationInfo compInfo,
            CompilationUnitTree compilationUnit) {
        return getElemHandles(findTopClassElems(compInfo, compilationUnit));
    }

    private static <T extends Element> List<ElementHandle<T>> getElemHandles(List<T> elements) {
        if (elements == null) {
            return null;
        }
        if (elements.isEmpty()) {
            return Collections.<ElementHandle<T>>emptyList();
        }

        List<ElementHandle<T>> handles = new ArrayList<ElementHandle<T>>(elements.size());
        for (T element : elements) {
            handles.add(ElementHandle.<T>create(element));
        }
        return handles;
    }


    public static List<ClassTree> findTopClasses(CompilationUnitTree compilationUnit, TreeUtilities treeUtils) {
        List<? extends Tree> typeDecls = compilationUnit.getTypeDecls();
        if ((typeDecls == null) || typeDecls.isEmpty()) {
            return Collections.<ClassTree>emptyList();
        }

        List<ClassTree> result = new ArrayList<ClassTree>(typeDecls.size());
        for (Tree typeDecl : typeDecls) {
            if (typeDecl.getKind() == Tree.Kind.CLASS) {
                ClassTree clsTree = (ClassTree) typeDecl;
                if (isTestable(clsTree, treeUtils)) {
                    result.add(clsTree);
                }
            }
        }

        return result;
    }

    public static HashMap<ElementHandle<TypeElement>,List<ExecutableElement>> findTopClasses(JavaSource javaSource, boolean tpm) throws IOException {
        TopClassFinderTask finder = new TopClassFinderTask(tpm);
        javaSource.runUserActionTask(finder, true);
        return finder.getTopClassElems();
    }

    public static boolean hasSetUp(ClassTree tstClass) {
        assert tstClass.getKind() == Tree.Kind.CLASS;
        List<? extends Tree> members = tstClass.getMembers();
        for (Tree member : members) {
            if (member instanceof MethodTree) {
                MethodTree methodTree = (MethodTree) member;
                if (methodTree.getName().toString().equals(NbBundle.getMessage(TestUtils.class, "PROP_generator_method_setup"))) //NOI18N
                    return true;
            }
        }
        return false;
    }

    public static boolean hasTearDown(ClassTree tstClass) {
        assert tstClass.getKind() == Tree.Kind.CLASS;
        List<? extends Tree> members = tstClass.getMembers();
        for (Tree member : members) {
            if (member instanceof MethodTree) {
                MethodTree methodTree = (MethodTree) member;
                if (methodTree.getName().toString().equals(NbBundle.getMessage(TestUtils.class, "PROP_generator_method_teardown"))) //NOI18N
                    return true;
            }
        }
        return false;
    }

    private static class TopClassFinderTask implements CancellableTask<CompilationController> {
        private volatile boolean cancelled;
        final private boolean testPkgPrivateMethods;

        private HashMap<ElementHandle<TypeElement>,List<ExecutableElement>> topClassMap = new HashMap<ElementHandle<TypeElement>,List<ExecutableElement>>();

        private TopClassFinderTask(boolean tpm) {
            testPkgPrivateMethods = tpm;
        }

        public void cancel() {
            this.cancelled = true;
        }

        public void run(CompilationController parameter) throws Exception {
            parameter.toPhase(Phase.ELEMENTS_RESOLVED);
            if (cancelled) {
                return;
            }

            
            List<ElementHandle<TypeElement>> ltce = findTopClassElemHandles(parameter, parameter.getCompilationUnit());
            
            for (ElementHandle<TypeElement> tce : ltce)
            {
                topClassMap.put(tce,findTestableMethods(tce.resolve(parameter)));
            }
        }
        
        private boolean isTestableMethod(ExecutableElement method) {
            if (method.getKind() != ElementKind.METHOD) {
                throw new IllegalArgumentException();
            }

            return isMethodAcceptable(method);
        }
        
         private boolean isMethodAcceptable(ExecutableElement method) {
            Set<Modifier> methodAccessModifiers
                = TestUtils.createModifierSet(Modifier.PUBLIC,
                Modifier.PROTECTED);
            Set<Modifier> modifiers = method.getModifiers();

            if (modifiers.contains(Modifier.PUBLIC) && methodAccessModifiers.contains(Modifier.PUBLIC))
                return true;
            else if (modifiers.contains(Modifier.PROTECTED) && methodAccessModifiers.contains(Modifier.PROTECTED))
                return true;
            else if (!(modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.PROTECTED))
                    &&  testPkgPrivateMethods && !modifiers.contains(Modifier.PRIVATE))
                return true;
            else
                return false;
        }

        
        private List<ExecutableElement> findTestableMethods(TypeElement classElem) {
            List<ExecutableElement> methods
                    = ElementFilter.methodsIn(classElem.getEnclosedElements());

            if (methods.isEmpty()) {
                return Collections.<ExecutableElement>emptyList();
            }

            List<ExecutableElement> testableMethods = null;

            int skippedCount = 0;
            for (ExecutableElement method : methods) {
                if (isTestableMethod(method)) {
                    if (testableMethods == null) {
                        testableMethods = new ArrayList<ExecutableElement>(
                                methods.size() - skippedCount);
                    }
                    testableMethods.add(method);
                } else {
                    skippedCount++;
                }
            }


            return (testableMethods != null)
                    ? testableMethods
                    : Collections.<ExecutableElement>emptyList();
        }

        public HashMap<ElementHandle<TypeElement>,List<ExecutableElement>> getTopClassElems() {
            return this.topClassMap;
        }
    }
}
