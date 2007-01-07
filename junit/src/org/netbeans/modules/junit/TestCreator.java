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

package org.netbeans.modules.junit;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.junit.plugin.JUnitPlugin.CreateTestParam;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static org.openide.NotifyDescriptor.WARNING_MESSAGE;

/**
 *
 * @author  vstejskal
 * @author  Marian Petras
 * @version 1.0
 */
public final class TestCreator implements TestabilityJudge {
    /* the class is final only for performance reasons */
    
    /* attributes - private */
    static private final String JUNIT_SUPER_CLASS_NAME                = "TestCase";
    static private final String JUNIT_FRAMEWORK_PACKAGE_NAME    = "junit.framework";
    
    static private final String METHOD_NAME_SETUP = "setUp";            //NOI18N
    static private final String METHOD_NAME_TEARDOWN = "tearDown";      //NOI18N
    static private final String CLASS_COMMENT_LINE1 = "TestCreator.javaClass.addTestsHereComment.l1";
    static private final String CLASS_COMMENT_LINE2 = "TestCreator.javaClass.addTestsHereComment.l2";
    
    /**
     * name of the 'instance' variable in the generated test method skeleton
     *
     * @see  #RESULT_VAR_NAME
     * @see  #EXP_RESULT_VAR_NAME
     */
    private static final String INSTANCE_VAR_NAME = "instance";         //NOI18N
    /**
     * name of the 'result' variable in the generated test method skeleton
     *
     * @see  #EXP_RESULT_VAR_NAME
     */
    private static final String RESULT_VAR_NAME = "result";             //NOI18N
    /**
     * name of the 'expected result' variable in the generated test method
     * skeleton
     *
     * @see  #RESULT_VAR_NAME
     */
    private static final String EXP_RESULT_VAR_NAME = "expResult";      //NOI18N
    /**
     * base for artificial names of variables
     * (if there is no name to derive from)
     */
    private static final String ARTIFICAL_VAR_NAME_BASE = "arg";        //NOI18N
    
    /**
     * bitmap combining modifiers PUBLIC, PROTECTED and PRIVATE
     *
     * @see  java.lang.reflect.Modifier
     */
    private static final EnumSet<Modifier> ACCESS_MODIFIERS
            = EnumSet.of(Modifier.PUBLIC,
                         Modifier.PROTECTED,
                         Modifier.PRIVATE);
    
    /** */
    private static final EnumSet<Modifier> NO_MODIFIERS
            = EnumSet.noneOf(Modifier.class);
    
    /** should test classes be skipped during generation of tests? */
    private boolean skipTestClasses = true;
    /** should package-private classes be skipped during generation of tests? */
    private boolean skipPkgPrivateClasses = false;
    /** should abstract classes be skipped during generation of tests? */
    private boolean skipAbstractClasses = false;
    /** should exception classes be skipped during generation of tests? */
    private boolean skipExceptionClasses = false;
    /**
     * should test suite classes be generated when creating tests for folders
     * and/or packages?
     */
    private boolean generateSuiteClasses = true;
    /**
     * bitmap defining whether public/protected methods should be tested
     *
     * @see  #testPackagePrivateMethods
     */
    private Set<Modifier> methodAccessModifiers
            = createModifierSet(Modifier.PUBLIC,
                                Modifier.PROTECTED);
    /**
     * should package-private methods be tested? 
     *
     * @see  #methodAccessModifiers
     */
    private boolean testPkgPrivateMethods = true;
    /**
     * should default method bodies be generated for newly created test methods?
     *
     * @see  #generateMethodJavadoc
     * @see  #generateMethodBodyComments
     */
    private boolean generateDefMethodBody = true;
    /**
     * should Javadoc comment be generated for newly created test methods?
     *
     * @see  #generateDefMethodBody
     * @see  #generateMethodBodyComments
     */
    private boolean generateMethodJavadoc = true;
    /**
     * should method body comment be generated for newly created test methods?
     *
     * @see  #generateDefMethodBody
     * @see  #generateMethodJavadoc
     */
    private boolean generateSourceCodeHints = true;
    /**
     * should <code>setUp()</code> method be generated in test classes?
     *
     * @see  #generateTearDown
     * @see  #generateMainMethod
     */
    private boolean generateSetUp = true;
    /**
     * should <code>tearDown()</code> method be generated in test classes?
     *
     * @see  #generateSetUp
     * @see  #generateMainMethod
     */
    private boolean generateTearDown = true;
    /**
     * should static method <code>main(String args[])</code>
     * be generated in test classes?
     *
     * @see  #generateSetUp
     * @see  #generateTearDown
     */
    private boolean generateMainMethod = true;
    /**
     * cached value of <code>JUnitSettings.getGenerateMainMethodBody()</code>
     */
    private String initialMainMethodBody;
    
    /* public methods */
    
    /**
     * Creates a new <code>TestCreator</code>.
     *
     * @param  loadDefaults  <code>true</code> if defaults should be loaded
     *                       from <code>JUnitSettings</code>;
     *                       <code>false</code> otherwise
     */
    public TestCreator(boolean loadDefaults) {
        if (loadDefaults) {
            loadDefaults();
        }
    }
    
    /**
     *
     */
    public TestCreator(Map<CreateTestParam, Object> params) {
        final JUnitSettings settings = JUnitSettings.getDefault();
        
        skipTestClasses = !JUnitSettings.GENERATE_TESTS_FROM_TEST_CLASSES;
        
        skipPkgPrivateClasses = !Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_PKG_PRIVATE_CLASS));
        skipAbstractClasses = !Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_ABSTRACT_CLASS));
        skipExceptionClasses = !Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_EXCEPTION_CLASS));
        generateSuiteClasses = Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_GENERATE_SUITE));
        
        methodAccessModifiers.clear();
        if (Boolean.TRUE.equals(params.get(CreateTestParam.INC_PUBLIC))) {
            methodAccessModifiers.add(Modifier.PUBLIC);
        }
        if (Boolean.TRUE.equals(params.get(CreateTestParam.INC_PROTECTED))) {
            methodAccessModifiers.add(Modifier.PROTECTED);
        }
        testPkgPrivateMethods = Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_PKG_PRIVATE));
        generateDefMethodBody = Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_METHOD_BODIES));
        generateMethodJavadoc = Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_JAVADOC));
        generateSourceCodeHints = Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_CODE_HINT));
        generateSetUp = Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_SETUP));
        generateTearDown = Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_TEAR_DOWN));
        
        generateMainMethod = settings.isGenerateMainMethod();
    }

    
    /**
     * Loads default settings from <code>JUnitSettings</code>.
     */
    private void loadDefaults() {
        final JUnitSettings settings = JUnitSettings.getDefault();
        
        skipTestClasses = JUnitSettings.GENERATE_TESTS_FROM_TEST_CLASSES;
        skipPkgPrivateClasses = !settings.isIncludePackagePrivateClasses();
        skipAbstractClasses = !settings.isGenerateAbstractImpl();
        skipExceptionClasses = !settings.isGenerateExceptionClasses();
        generateSuiteClasses = settings.isGenerateSuiteClasses();
        
        methodAccessModifiers.clear();
        if (settings.isMembersPublic()) {
            methodAccessModifiers.add(Modifier.PUBLIC);
        }
        if (settings.isMembersProtected()) {
            methodAccessModifiers.add(Modifier.PROTECTED);
        }
        testPkgPrivateMethods = settings.isMembersPackage();
        
        generateDefMethodBody = settings.isBodyContent();
        generateMethodJavadoc = settings.isJavaDoc();
        generateSourceCodeHints = settings.isBodyComments();
        generateSetUp = settings.isGenerateSetUp();
        generateTearDown = settings.isGenerateTearDown();
        generateMainMethod = settings.isGenerateMainMethod();
    }
    
    /**
     * Sets whether tests for test classes should be generated
     * The default is <code>true</code>.
     *
     * @param  test  <code>false</code> if test classes should be skipped
     *               during test creation;
     *               <code>true</code> otherwise
     */
    public void setSkipTestClasses(boolean skip) {
        this.skipTestClasses = skip;
    }
    
    /**
     * Sets whether tests for package-private classes should be generated
     * The default is <code>false</code>.
     *
     * @param  test  <code>false</code> if package-private classes should
     *               be skipped during test creation;
     *               <code>true</code> otherwise
     */
    public void setSkipPackagePrivateClasses(boolean skip) {
        this.skipPkgPrivateClasses = skip;
    }
    
    /**
     * Sets whether tests for abstract classes should be generated
     * The default is <code>false</code>.
     *
     * @param  test  <code>false</code> if abstract classes should be skipped
     *               during test creation;
     *               <code>true</code> otherwise
     */
    public void setSkipAbstractClasses(boolean skip) {
        this.skipAbstractClasses = skip;
    }
    
    /**
     * Sets whether tests for exception classes should be generated
     * The default is <code>false</code>.
     *
     * @param  test  <code>false</code> if exception classes should be skipped
     *               during test creation;
     *               <code>true</code> otherwise
     */
    public void setSkipExceptionClasses(boolean skip) {
        this.skipExceptionClasses = skip;
    }
    
    /**
     * Sets whether test suite classes should be generated when creating tests
     * for folders and/or packages.
     *
     * @param  generate  <code>true</code> if test suite classes should
     *                   be generated; <code>false</code> otherwise
     */
    public void setGenerateSuiteClasses(boolean generate) {
        this.generateSuiteClasses = generate;
    }
    
    /**
     * Sets whether public methods should be tested or not.
     * The default is <code>true</code>.
     *
     * @param  test  <code>true</code> if public methods should be tested;
     *               <code>false</code> if public methods should be skipped
     */
    public void setTestPublicMethods(boolean test) {
        if (test) {
            methodAccessModifiers.add(Modifier.PUBLIC);
        } else {
            methodAccessModifiers.remove(Modifier.PUBLIC);
        }
    }
    
    /**
     * Sets whether protected methods should be tested or not.
     * The default is <code>true</code>.
     *
     * @param  test  <code>true</code> if protected methods should be tested;
     *               <code>false</code> if protected methods should be skipped
     */
    public void setTestProtectedMethods(boolean test) {
        if (test) {
            methodAccessModifiers.add(Modifier.PROTECTED);
        } else {
            methodAccessModifiers.remove(Modifier.PROTECTED);
        }
    }
    
    /**
     * Sets whether package-private methods should be tested or not.
     * The default is <code>true</code>.
     *
     * @param  test  <code>true</code> if package-private methods should be
     *               tested;
     *               <code>false</code> if package-private methods should be
     *              skipped
     */
    public void setTestPackagePrivateMethods(boolean test) {
        this.testPkgPrivateMethods = test;
    }
    
    /**
     * Sets whether default method bodies should be generated for newly created
     * test methods.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if default method bodies should
     *                   be generated; <code>false</code> otherwise
     */
    public void setGenerateDefMethodBody(boolean generate) {
        this.generateDefMethodBody = generate;
    }
    
    /**
     * Sets whether Javadoc comment should be generated for newly created
     * test methods.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if Javadoc comment should
     *                   be generated; <code>false</code> otherwise
     */
    public void setGenerateMethodJavadoc(boolean generate) {
        this.generateMethodJavadoc = generate;
    }
    
    /**
     * Sets whether method body comment should be generated for newly created
     * test methods.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if method body comment should
     *                   be generated; <code>false</code> otherwise
     */
    public void setGenerateMethodBodyComment(boolean generate) {
        this.generateSourceCodeHints = generate;
    }
    
    /**
     * Sets whether <code>setUp()</code> method should be generated
     * in test classes being created/updated.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if <code>setUp()</code> method
     *                   should be generated; <code>false</code> otherwise
     * @see  #setGenerateTearDown
     * @see  #setGenerateMainMethod
     */
    public void setGenerateSetUp(boolean generate) {
        this.generateSetUp = generate;
    }
    
    /**
     * Sets whether <code>tearDown()</code> method should be generated
     * in test classes being created/updated.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if <code>tearDown()</code> method
     *                   should be generated; <code>false</code> otherwise
     * @see  #setGenerateSetUp
     * @see  #setGenerateMainMethod
     */
    public void setGenerateTearDown(boolean generate) {
        this.generateTearDown = generate;
    }
    
    /**
     * Sets whether static method <code>main(String args[])</code> should
     * be generated in test classes.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if the method should be generated;
     *                   <code>false</code> otherwise
     * @see  #setGenerateSetUp
     * @see  #setGenerateTearDown
     */
    public void setGenerateMainMethod(boolean generate) {
        this.generateMainMethod = generate;
    }

    
    /**
     */
    public void createEmptyTest(final JavaSource tstSource) throws IOException {
        SingleResourceTestCreator testCreator
                = new SingleResourceTestCreator();
        ModificationResult result = tstSource.runModificationTask(testCreator);
        result.commit();
    }
    
    /**
     * 
     * @return  list of names of created classes
     */
    public void createSimpleTest(ElementHandle<TypeElement> topClassToTest,
                                 JavaSource tstSource,
                                 boolean isNewTestClass) throws IOException {
        SingleResourceTestCreator testCreator
                = new SingleResourceTestCreator(Collections.singletonList(topClassToTest),
                                                null,
                                                isNewTestClass);
        ModificationResult result = tstSource.runModificationTask(testCreator);
        result.commit();
    }
    
    /**
     */
    public List<String> createTestSuite(List<String> suiteMembers,
                                        JavaSource tstSource,
                                        boolean isNewTestClass) throws IOException {
        SingleResourceTestCreator testCreator
                = new SingleResourceTestCreator(null, suiteMembers, isNewTestClass);
        ModificationResult result = tstSource.runModificationTask(testCreator);
        result.commit();
        
        return testCreator.getProcessedClassNames();
    }
    
    /**
     *
     */
    private final class SingleResourceTestCreator implements CancellableTask<WorkingCopy> {

        private final List<ElementHandle<TypeElement>> srcTopClassElemHandles;
        
        private final List<String> suiteMembers;
        
        private final boolean isNewTestClass;
        
        private List<String>processedClassNames;
        
        private volatile boolean cancelled = false;
        
        
        /** element representing type {@code junit.framework.Test} */
        private TypeElement testTypeElem;
        /** */
        private TypeElement testCaseTypeElem;
        /** element representing type {@code junit.framework.TestSuite} */
        private TypeElement testSuiteTypeElem;
        
        
        /**
         * Used when creating a new empty test class.
         */
        private SingleResourceTestCreator() {
            this.srcTopClassElemHandles = null;
            this.suiteMembers = null;
            this.isNewTestClass = true;   //value not used
        }
        
        /**
         * Used when creating a test class for a given source class
         * or when creating a test suite.
         */
        private SingleResourceTestCreator(
                        List<ElementHandle<TypeElement>> srcTopClassHandles,
                        List<String>suiteMembers,
                        boolean isNewTestClass) {
            this.srcTopClassElemHandles = srcTopClassHandles;
            this.suiteMembers = suiteMembers;
            this.isNewTestClass = isNewTestClass;
        }
        
        /*i*
         */
        public void run(WorkingCopy workingCopy) throws IOException {
            final String className = workingCopy.getClasspathInfo()
                                     .getClassPath(ClasspathInfo.PathKind.SOURCE)
                                     .getResourceName(workingCopy.getFileObject(), '.', false);
            
            workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
            
            CompilationUnitTree compUnit = workingCopy.getCompilationUnit();
            List<ClassTree> tstTopClasses = TopClassFinder.findTopClasses(
                                                compUnit,
                                                workingCopy.getTreeUtilities());

            List<TypeElement> srcTopClassElems
                    = resolveHandles(workingCopy, srcTopClassElemHandles);
            
            TreePath compUnitPath = new TreePath(compUnit);
            
            if ((srcTopClassElems != null) && !srcTopClassElems.isEmpty()) {
                
                /* Create/update a test class for each testable source class: */
                for (TypeElement srcTopClass : srcTopClassElems) {
                    String srcClassName = srcTopClass.getSimpleName().toString();
                    String tstClassName = TestUtil.getTestClassName(srcClassName);
                    
                    List<ExecutableElement> srcMethods
                                            = findTestableMethods(srcTopClass);
                    boolean srcHasTestableMethods = !srcMethods.isEmpty();
                        
                    ClassTree tstTopClass = null;
                    for (ClassTree tstClass : tstTopClasses) {
                        if (tstClass.getSimpleName().contentEquals(tstClassName)) {
                            tstTopClass = tstClass;
                            break;
                        }
                    }
                    if (tstTopClass != null) {
                        TreePath tstTopClassTreePath = new TreePath(compUnitPath,
                                                                    tstTopClass);
                        
                        ClassTree origTstTopClass = tstTopClass;
                        if (srcHasTestableMethods) {
                            tstTopClass = generateMissingTestMethods(
                                                   workingCopy,
                                                   tstTopClass,
                                                   tstTopClassTreePath,
                                                   srcTopClass,
                                                   srcMethods,
                                                   isNewTestClass);
                        } else if (isNewTestClass) {
                            tstTopClass = generateMissingInitMembers(
                                                    tstTopClass,
                                                    tstTopClassTreePath,
                                                    workingCopy);
                        }
                        if (tstTopClass != origTstTopClass) {
                            workingCopy.rewrite(origTstTopClass,
                                                tstTopClass);
                        }
                    } else {
                        if (srcHasTestableMethods
                                || tstClassName.equals(TestUtil.getSimpleName(className))) {
                            tstTopClass = generateNewTestClass(workingCopy,
                                                               tstClassName,
                                                               srcTopClass,
                                                               srcMethods);
                            //PENDING - add the top class to the CompilationUnit
                            
                            //PENDING - generate suite method
                        }
                    }
                }
            } else if (suiteMembers != null) {          //test suite
                for (ClassTree tstClass : tstTopClasses) {
                    ClassTree origTstTopClass = tstClass;
                    ClassTree tstTopClass = generateMissingSuiteClassMembers(
                                                    tstClass,
                                                    suiteMembers,
                                                    isNewTestClass,
                                                    workingCopy);
                    if (tstTopClass != origTstTopClass) {
                        workingCopy.rewrite(origTstTopClass,
                                            tstTopClass);
                    }
                    classProcessed(tstClass);
                }
            } else if (srcTopClassElems == null) {      //new empty test class
                for (ClassTree tstClass : tstTopClasses) {
                    ClassTree origTstTopClass = tstClass;
                    ClassTree tstTopClass = generateMissingInitMembers(
                                                    tstClass,
                                                    new TreePath(compUnitPath,
                                                                 tstClass),
                                                    workingCopy);
                    if (tstTopClass != origTstTopClass) {
                        workingCopy.rewrite(origTstTopClass,
                                            tstTopClass);
                    }
                }
            }
        }
        
        /**
         */
        private ClassTree generateNewTestClass(WorkingCopy workingCopy,
                                               String name,
                                               TypeElement srcClass,
                                               List<ExecutableElement> srcMethods) {
            final TreeMaker maker = workingCopy.getTreeMaker();
            final Elements elements = workingCopy.getElements();
            
            ModifiersTree modifiers = maker.Modifiers(
                                    Collections.<Modifier>singleton(PUBLIC));
            
            TypeElement testCaseType = getTestCaseTypeElem(elements);
            Tree extendsClause = (testCaseType != null)
                                 ? maker.QualIdent(testCaseType)
                                 : maker.Identifier("junit.framework.TestCase");//NOI18N
            
            List<? extends Tree> initMembers = generateInitMembers(maker);
            List<MethodTree> testMethods = generateTestMethods(workingCopy,
                                                               srcClass,
                                                               srcMethods);
            List<? extends Tree> members;
            if (initMembers.isEmpty() && testMethods.isEmpty()) {
                members = Collections.<Tree>emptyList();
            } else if (initMembers.isEmpty()) {
                members = testMethods;
            } else if (testMethods.isEmpty()) {
                members = initMembers;
            } else {
                List<Tree> allMembers = new ArrayList<Tree>(
                                    initMembers.size() + testMethods.size());
                allMembers.addAll(initMembers);
                allMembers.addAll(testMethods);
                
                members = allMembers;
            }
            
            return maker.Class(
                        modifiers,                                 //modifiers
                        name,                                      //name
                        Collections.<TypeParameterTree>emptyList(),//type params
                        extendsClause,                             //extends
                        Collections.<ExpressionTree>emptyList(),   //implements
                        members);                                  //members
        }
        
        /**
         */
        private ClassTree generateMissingInitMembers(ClassTree tstClass,
                                                     TreePath tstClassTreePath,
                                                     WorkingCopy workingCopy) {
            if (!generateSetUp && !generateTearDown && !generateSuiteClasses) {
                return tstClass;
            }
            
            ClassMap classMap = ClassMap.forClass(tstClass);
            
            if ((!generateSetUp || classMap.containsSetUp())
                    && (!generateTearDown || classMap.containsTearDown())
                    && (!generateSuiteClasses || classMap.containsNoArgMethod(
                                                            "suite"))) {//NOI18N
                return tstClass;
            }
            
            final TreeMaker maker = workingCopy.getTreeMaker();
            
            List<? extends Tree> tstMembersOrig = tstClass.getMembers();
            List<Tree> tstMembers = new ArrayList<Tree>(tstMembersOrig.size() + 2);
            tstMembers.addAll(tstMembersOrig);
            
            generateMissingInitMembers(tstMembers, ClassMap.forClass(tstClass),
                                       maker);
            generateTestClassSuiteMethod(tstClassTreePath, tstMembers, classMap,
                                         workingCopy);
            
            ClassTree newClass = maker.Class(
                    tstClass.getModifiers(),
                    tstClass.getSimpleName(),
                    tstClass.getTypeParameters(),
                    tstClass.getExtendsClause(),
                    (List<? extends ExpressionTree>) tstClass.getImplementsClause(),
                    tstMembers);
            return newClass;
        }
        
        /**
         */
        private List<? extends Tree> generateInitMembers(TreeMaker maker) {
            if (!generateSetUp && !generateTearDown) {
                return Collections.<Tree>emptyList();
            }
            
            List<MethodTree> result = new ArrayList<MethodTree>(2);
            if (generateSetUp) {
                result.add(generateInitMethod(maker, "setUp"));         //NOI18N
            }
            if (generateTearDown) {
                result.add(generateInitMethod(maker, "tearDown"));      //NOI18N
            }
            return result;
        }
        
        /**
         * Generates a set-up or a tear-down method.
         * The generated method will have no arguments, void return type
         * and a declaration that it may throw {@code java.lang.Exception}.
         * The method will have a declared protected member access.
         * The method contains call of the corresponding super method, i.e.
         * {@code super.setUp()} or {@code super.tearDown()}.
         *
         * @param  methodName  name of the method to be created
         * @return  created method
         * @see  http://junit.sourceforge.net/javadoc/junit/framework/TestCase.html
         *       methods {@code setUp()} and {@code tearDown()}
         */
        private MethodTree generateInitMethod(TreeMaker maker,
                                              String methodName) {
            ModifiersTree modifiers = maker.Modifiers(
                    Collections.<Modifier>singleton(PROTECTED));
            ExpressionTree superMethodCall = maker.MethodInvocation(
                    Collections.<ExpressionTree>emptyList(),    // type params.
                    maker.MemberSelect(
                            maker.Identifier("super"), methodName),     //NOI18N
                    Collections.<ExpressionTree>emptyList());
            BlockTree methodBody = maker.Block(
                    Collections.<StatementTree>singletonList(
                            maker.ExpressionStatement(superMethodCall)),
                    false);
            MethodTree method = maker.Method(
                    modifiers,              // modifiers
                    methodName,             // name
                    maker.PrimitiveType(TypeKind.VOID),         // return type
                    Collections.<TypeParameterTree>emptyList(), // type params
                    Collections.<VariableTree>emptyList(),      // parameters
                    Collections.<ExpressionTree>singletonList(
                            maker.Identifier("Exception")),// throws... //NOI18N
                    methodBody,
                    null);                                      // default value
            return method;
        }

        /**
         * 
         * @param  srcMethods  methods to create/update tests for
         * 
         */
        private ClassTree generateMissingTestMethods(
                                        WorkingCopy workingCopy,
                                        ClassTree tstClass,
                                        TreePath tstClassTreePath,
                                        TypeElement srcClass,
                                        List<ExecutableElement> srcMethods,
                                        boolean generateMissingInitMembers) {
            if (srcMethods.isEmpty()) {
                return tstClass;
            }
            
            ClassMap clsMap = ClassMap.forClass(tstClass);
            
            List<? extends Tree> tstMembersOrig = tstClass.getMembers();
            List<Tree> tstMembers = new ArrayList<Tree>(tstMembersOrig.size() + 4);
            tstMembers.addAll(tstMembersOrig);
            
            if (generateMissingInitMembers) {
                generateMissingInitMembers(tstMembers, clsMap,
                                           workingCopy.getTreeMaker());
            }
            if (generateSuiteClasses) {
                generateTestClassSuiteMethod(tstClassTreePath,
                                             tstMembers, clsMap,
                                             workingCopy);
            }
            
            Boolean useNoArgConstrutor = null;
            for (ExecutableElement srcMethod : srcMethods) {
                String testMethodName = createTestMethodName(
                                        srcMethod.getSimpleName().toString());
                int testMethodIndex = clsMap.findNoArgMethod(testMethodName);
                if (testMethodIndex != -1) {
                    continue;       //corresponding test method already exists
                }
                
                if (useNoArgConstrutor == null) {
                    useNoArgConstrutor = Boolean.valueOf(
                                  hasAccessibleNoArgConstructor(srcClass));
                }
                MethodTree newTestMethod = generateTestMethod(
                                workingCopy,
                                srcClass,
                                srcMethod,
                                useNoArgConstrutor.booleanValue());
                
                tstMembers.add(newTestMethod);
                clsMap.addNoArgMethod(newTestMethod.getName().toString());
            }
            
            if (tstMembers.size() == tstMembersOrig.size()) {  //no test method added
                return tstClass;
            }
            
            ClassTree newClass = workingCopy.getTreeMaker().Class(
                    tstClass.getModifiers(),
                    tstClass.getSimpleName(),
                    tstClass.getTypeParameters(),
                    tstClass.getExtendsClause(),
                    (List<? extends ExpressionTree>) tstClass.getImplementsClause(),
                    tstMembers);
            return newClass;
        }
        
        /**
         */
        private boolean generateTestClassSuiteMethod(TreePath tstClassTreePath,
                                                     List<Tree> tstMembers,
                                                     ClassMap clsMap,
                                                     WorkingCopy workingCopy) {
            if (!generateSuiteClasses
                            || clsMap.containsNoArgMethod("suite")) {   //NOI18N
                return false;
            }
            
            final TreeMaker maker = workingCopy.getTreeMaker();
            final Elements elements = workingCopy.getElements();
            final Trees trees = workingCopy.getTrees();
            
            Element tstClassElem = trees.getElement(tstClassTreePath);
            assert tstClassElem != null;
            
            List<StatementTree> bodyContent = new ArrayList<StatementTree>(4);
            
            
            /* TestSuite suite = new TestSuite(MyTestClass.class); */
            
            VariableTree suiteVar = maker.Variable(
                    maker.Modifiers(NO_MODIFIERS),
                    "suite",                                            //NOI18N
                    maker.QualIdent(getTestSuiteTypeElem(elements)),
                    maker.NewClass(
                            null,           //enclosing instance
                            Collections.<ExpressionTree>emptyList(),
                            maker.QualIdent(getTestSuiteTypeElem(elements)),
                            Collections.singletonList(
                                    maker.MemberSelect(maker.QualIdent(tstClassElem),
                                                       "class")),       //NOI18N
                            null));         //class definition
                    
            bodyContent.add(suiteVar);
            
            /* suite.addTest(NestedClass.suite());        */
            /* suite.addTest(AnotherNestedClass.suite()); */
            /*    ...                                     */
            
            List<TypeElement> nestedClassElems
                    = ElementFilter.typesIn(tstClassElem.getEnclosedElements());
            if (!nestedClassElems.isEmpty()) {
                for (TypeElement nestedClassElem : nestedClassElems) {
                    if (TestUtil.isClassTest(workingCopy, nestedClassElem)) {
                        
                        /* suite.addTest(NestedClass.suite()); */
                        
                        /* NestedClass.suite() */
                        MethodInvocationTree arg = maker.MethodInvocation(
                                Collections.<ExpressionTree>emptyList(),
                                maker.MemberSelect(
                                        maker.QualIdent(nestedClassElem),
                                        "suite"),                       //NOI18N
                                Collections.<ExpressionTree>emptyList());
                        
                        /* suite.addTest(...) */
                        MethodInvocationTree methodCall = maker.MethodInvocation(
                                Collections.<ExpressionTree>emptyList(),
                                maker.MemberSelect(
                                        maker.Identifier("suite"),
                                        "addTest"),
                                Collections.singletonList(arg));
                        
                        bodyContent.add(maker.ExpressionStatement(methodCall));
                    }
                }
            }
            
            /* return suite; */
            
            ReturnTree returnStmt
                    = maker.Return(maker.Identifier("suite"));          //NOI18N
            bodyContent.add(returnStmt);
            
            MethodTree suiteMethod = maker.Method(
                    maker.Modifiers(createModifierSet(PUBLIC, STATIC)),
                    "suite",                                            //NOI18N
                    maker.QualIdent(getTestTypeElem(elements)), //ret. type
                    Collections.<TypeParameterTree>emptyList(), //type params
                    Collections.<VariableTree>emptyList(),      //parameters
                    Collections.<ExpressionTree>emptyList(),    //throws ...
                    maker.Block(bodyContent, false),            //body
                    null,                                       //default value
                    (TypeElement) tstClassElem);
            
            int targetIndex;
            if (clsMap.containsMethods()) {
                targetIndex = clsMap.getFirstMethodIndex();     //before methods
            } else if (clsMap.containsNestedClasses()) {
                targetIndex = clsMap.getFirstNestedClassIndex(); //before nested
            } else {
                targetIndex = clsMap.size();                  //end of the class
            }
            
            if (targetIndex == clsMap.size()) {
                tstMembers.add(suiteMethod);
            } else {
                tstMembers.add(targetIndex, suiteMethod);
            }
            clsMap.addNoArgMethod(targetIndex, "suite");                //NOI18N
            
            return true;
        }
        
        /**
         */
        private boolean generateMissingInitMembers(List<Tree> tstMembers,
                                                   ClassMap clsMap,
                                                   TreeMaker treeMaker) {
            boolean modified = false;
            if (generateSetUp && !clsMap.containsSetUp()) {
                MethodTree setUpMethod = generateInitMethod(
                                                treeMaker, "setUp");    //NOI18N

                int targetIndex;
                if (clsMap.containsTearDown()) {
                    targetIndex = clsMap.getTearDownIndex();
                } else if (clsMap.containsMethods()) {
                    targetIndex = clsMap.getFirstMethodIndex();
                } else if (clsMap.containsInitializers()) {
                    targetIndex = clsMap.getLastInitializerIndex() + 1;
                } else if (clsMap.containsNestedClasses()) {
                    targetIndex = clsMap.getFirstNestedClassIndex();
                } else {
                    targetIndex = clsMap.size();        //end of the class
                }

                if (targetIndex == clsMap.size()) {
                    tstMembers.add(setUpMethod);
                } else {
                    tstMembers.add(targetIndex, setUpMethod);
                }
                clsMap.addNoArgMethod(targetIndex, "setUp");        //NOI18N
                
                modified = true;
            }
            if (generateTearDown && !clsMap.containsTearDown()) {
                MethodTree tearDownMethod = generateInitMethod(
                                                treeMaker, "tearDown"); //NOI18N

                int targetIndex;
                if (clsMap.containsSetUp()) {
                    targetIndex = clsMap.getSetUpIndex() + 1;
                } else if (clsMap.containsMethods()) {
                    targetIndex = clsMap.getFirstMethodIndex();
                } else if (clsMap.containsInitializers()) {
                    targetIndex = clsMap.getLastInitializerIndex();
                } else if (clsMap.containsNestedClasses()) {
                    targetIndex = clsMap.getFirstNestedClassIndex();
                } else {
                    targetIndex = clsMap.size();        //end of the class
                }

                if (targetIndex == clsMap.size()) {
                    tstMembers.add(tearDownMethod);
                } else {
                    tstMembers.add(targetIndex, tearDownMethod);
                }
                clsMap.addNoArgMethod(targetIndex, "tearDown");     //NOI18N
                
                modified = true;
            }
            return modified;
        }
        
        /**
         */
        private List<MethodTree> generateTestMethods(
                                           WorkingCopy workingCopy,
                                           TypeElement srcClass,
                                           List<ExecutableElement> srcMethods) {
            if (srcMethods.isEmpty()) {
                return Collections.<MethodTree>emptyList();
            }
            
            boolean useNoArgConstrutor = hasAccessibleNoArgConstructor(srcClass);
            List<MethodTree> testMethods = new ArrayList<MethodTree>(srcMethods.size());
            for (ExecutableElement srcMethod : srcMethods) {
                testMethods.add(
                        generateTestMethod(workingCopy,
                                           srcClass,
                                           srcMethod,
                                           useNoArgConstrutor));
            }
            return testMethods;
        }
        
        /**
         */
        private MethodTree generateTestMethod(WorkingCopy workingCopy,
                                              TypeElement srcClass,
                                              ExecutableElement srcMethod,
                                              boolean useNoArgConstructor) {
            final TreeMaker maker = workingCopy.getTreeMaker();
            
            String testMethodName = createTestMethodName(
                                        srcMethod.getSimpleName().toString());
            ModifiersTree modifiers = maker.Modifiers(createModifierSet(PUBLIC));
            List<ExpressionTree> throwsList;
            if (throwsNonRuntimeExceptions(workingCopy, srcMethod)) {
                throwsList = Collections.<ExpressionTree>singletonList(
                                        maker.Identifier("Exception")); //NOI18N
            } else {
                throwsList = Collections.<ExpressionTree>emptyList();
            }
            
            MethodTree method = maker.Method(
                    modifiers,
                    testMethodName,
                    maker.PrimitiveType(TypeKind.VOID),
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    throwsList,
                    generateTestMethodBody(workingCopy, srcClass, srcMethod,
                                           useNoArgConstructor),
                    null);          //default value - used by annotations
            
            if (generateMethodJavadoc) {
                Comment javadoc = Comment.create(
                    NbBundle.getMessage(
                        TestCreator.class,
                        "TestCreator.variantMethods.JavaDoc.comment",   //NOI18N
                        srcMethod.getSimpleName().toString(),
                        srcClass.getSimpleName().toString()));
                maker.addComment(method, javadoc, false);
            }
            
            return method;
        }
        
        /**
         */
        private ClassTree generateMissingSuiteClassMembers(
                                                ClassTree tstClass,
                                                List<String> suiteMembers,
                                                boolean isNewTestClass,
                                                WorkingCopy workingCopy) {
            final TreeMaker maker = workingCopy.getTreeMaker();
            
            List<? extends Tree> tstMembersOrig = tstClass.getMembers();
            List<Tree> tstMembers = new ArrayList<Tree>(tstMembersOrig.size() + 2);
            tstMembers.addAll(tstMembersOrig);
            boolean membersChanged = false;
            
            ClassMap classMap = ClassMap.forClass(tstClass);
            
            if (isNewTestClass) {
                membersChanged |= generateMissingInitMembers(tstMembers,
                                                             classMap,
                                                             maker);
            }
            
            MethodTree suiteMethod = generateSuiteMethod(
                                            tstClass.getSimpleName().toString(),
                                            suiteMembers,
                                            workingCopy);
            if (suiteMethod != null) {
                int suiteMethodIndex = classMap.findNoArgMethod("suite");   //NOI18N
                if (suiteMethodIndex != -1) {
                    tstMembers.set(suiteMethodIndex, suiteMethod);  //replace method
                } else {
                    int targetIndex;
                    if (classMap.containsInitializers()) {
                        targetIndex = classMap.getLastInitializerIndex() + 1;
                    } else if (classMap.containsMethods()) {
                        targetIndex = classMap.getFirstMethodIndex();
                    } else if (classMap.containsNestedClasses()) {
                        targetIndex = classMap.getFirstNestedClassIndex();
                    } else {
                        targetIndex = classMap.size();
                    }
                    if (targetIndex == classMap.size()) {
                        tstMembers.add(suiteMethod);
                    } else {
                        tstMembers.add(targetIndex, suiteMethod);
                    }
                    classMap.addNoArgMethod(targetIndex, "suite");      //NOI18N
                }
                membersChanged = true;
            }
            
            //PENDING - generating main(String[]) method:
            //if (generateMainMethod && !TestUtil.hasMainMethod(tstClass)) {
            //    addMainMethod(tstClass);
            //}
            
            if (!membersChanged) {
                return tstClass;
            }
            
            return maker.Class(
                    tstClass.getModifiers(),
                    tstClass.getSimpleName(),
                    tstClass.getTypeParameters(),
                    tstClass.getExtendsClause(),
                    (List<? extends ExpressionTree>) tstClass.getImplementsClause(),
                    tstMembers);
        }
        
        /**
         * 
         * @return  object representing body of the suite() method,
         *          or {@code null} if an error occured while creating the body
         */
        private MethodTree generateSuiteMethod(String suiteName,
                                              List<String> members,
                                              WorkingCopy workingCopy) {
            final Types types = workingCopy.getTypes();
            final Elements elements = workingCopy.getElements();
            final TreeMaker maker = workingCopy.getTreeMaker();
            
            TypeElement testSuiteElem = getTestSuiteTypeElem(elements);
            if (testSuiteElem == null) {
                return null;
            }
            
            TypeElement testTypeElem = getTestTypeElem(elements);
            if (testTypeElem == null) {
                return null;
            }
            TypeMirror testType = testTypeElem.asType();
            
            List<StatementTree> bodyContent
                    = new ArrayList<StatementTree>(members.size() + 2);
            
            /* TestSuite suite = new TestSuite("ClassName") */
            
            VariableTree suiteObjInit = maker.Variable(
                    maker.Modifiers(NO_MODIFIERS),
                    "suite",
                    maker.QualIdent(testSuiteElem),
                    maker.NewClass(
                            null,                           //enclosing instance
                            Collections.<ExpressionTree>emptyList(), //type args
                            maker.QualIdent(testSuiteElem), //class name
                            Collections.singletonList(      //params
                                    maker.Literal(TestUtil.getSimpleName(suiteName))),
                            null));                         //class body
            
            bodyContent.add(suiteObjInit);
            
            for (String className : members) {
                TypeElement classElem = elements.getTypeElement(className);
                if ((classElem != null) && containsSuiteMethod(
                                                    classElem,
                                                    elements, types,
                                                    testType)) {
                    
                    /* suite.addTest(ClassName.suite()) */
                    
                    MethodInvocationTree suiteMethodCall, methodCall;
                    suiteMethodCall = maker.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            maker.MemberSelect(maker.QualIdent(classElem),
                                               "suite"),                //NOI18N
                            Collections.<ExpressionTree>emptyList());
                    methodCall = maker.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            maker.MemberSelect(
                                    maker.Identifier("suite"),          //NOI18N
                                    "addTest"),                         //NOI18N
                            Collections.singletonList(suiteMethodCall));
                    
                    bodyContent.add(maker.ExpressionStatement(methodCall));
                }
            }
            
            /* return suite; */
            
            bodyContent.add(maker.Return(maker.Identifier("suite")));   //NOI18N
            
            
            return maker.Method(
                        maker.Modifiers(createModifierSet(PUBLIC, STATIC)),
                        "suite",
                        maker.QualIdent(testTypeElem),             //return type
                        Collections.<TypeParameterTree>emptyList(),//type params
                        Collections.<VariableTree>emptyList(),     //params
                        Collections.<ExpressionTree>emptyList(),   //throws-list
                        maker.Block(bodyContent, false),           //body
                        null);  //def. value - only for annotations
        }
        
        /**
         * Finds whether the given {@code TypeElement} or any of its type
         * ancestor contains an accessible static no-arg method
         * of the given name.
         * 
         * @param  typeElement  {@code TypeElement} to search
         * @param  methodName  name of the method to be found
         * @param  elements  support instance to be used for the search
         * @return  {@code true} if the given {@code TypeElement} contains,
         *          whether inherited or declared directly,
         *          a static no-argument method of the given name,
         *          {@code false} otherwise
         */
        private boolean containsSuiteMethod(TypeElement typeElement,
                                            Elements elements,
                                            Types types,
                                            TypeMirror testType) {
            List<ExecutableElement> allMethods
                    = ElementFilter.methodsIn(elements.getAllMembers(typeElement));
            for (ExecutableElement method : allMethods) {
                if (method.getSimpleName().contentEquals("suite")       //NOI18N
                        && method.getParameters().isEmpty()) {
                    return method.getModifiers().contains(Modifier.STATIC)
                           && types.isSameType(method.getReturnType(),
                                               testType);
                }
            }
            return false;
        }

        // <editor-fold defaultstate="collapsed" desc=" disabled code ">
//        /**
//         */
//        private void addMainMethod(final ClassTree classTree) {
//            MethodTree mainMethod = createMainMethod(maker);
//            if (mainMethod != null) {
//                maker.addClassMember(classTree, mainMethod);
//            }
//        }
//
//        /**
//         */
//        private void fillTestClass(JavaClass srcClass, JavaClass tstClass) {
//            
//            fillGeneral(tstClass);
//
//            List innerClasses = TestUtil.filterFeatures(srcClass,
//                                                        JavaClass.class);
//
//            /* Create test classes for inner classes: */
//            for (Iterator i = innerClasses.iterator(); i.hasNext(); ) {
//                JavaClass innerCls = (JavaClass) i.next();
//
//                if (!isClassTestable(innerCls).isTestable()) {
//                    continue;
//                }
//                    
//                /*
//                 * Check whether the test class for the inner class exists
//                 * and create one if it does not exist:
//                 */
//                String innerTestClsName
//                        = TestUtil.getTestClassName(innerCls.getSimpleName());
//                JavaClass innerTestCls
//                        = TestUtil.getClassBySimpleName(tstClass,
//                                                        innerTestClsName);
//                if (innerTestCls == null) {
//                    innerTestCls = tgtPkg.getJavaClass().createJavaClass();
//                    innerTestCls.setSimpleName(
//                            tstClass.getName() + '.' + innerTestClsName);
//                    tstClass.getFeatures().add(innerTestCls);
//                }
//
//                /* Process the tested inner class: */
//                fillTestClass(innerCls, innerTestCls);
//
//                /* Make the inner test class testable with JUnit: */
//                innerTestCls.setModifiers(innerTestCls.getModifiers() | Modifier.STATIC);
//            }
//
//            /* Add the suite() method (only if we are supposed to do so): */
//            if (generateSuiteClasses && !hasSuiteMethod(tstClass)) {
//                tstClass.getFeatures().add(createTestClassSuiteMethod(tstClass));
//            }
//
//            /* Create missing test methods: */
//            List srcMethods = TestUtil.filterFeatures(srcClass, Method.class);
//            for (Iterator i = srcMethods.iterator(); i.hasNext(); ) {
//                Method sm = (Method) i.next();
//                if (isMethodAcceptable(sm) &&
//                        tstClass.getMethod(createTestMethodName(sm.getName()),
//                                          Collections.EMPTY_LIST,
//                                          false)
//                        == null) {
//                    Method tm = createTestMethod(srcClass, sm);
//                    tstClass.getFeatures().add(tm);
//                }
//            }
//
//            /* Create abstract class implementation: */
//            if (!skipAbstractClasses
//                    && (Modifier.isAbstract(srcClass.getModifiers())
//                        || srcClass.isInterface())) {
//                createAbstractImpl(srcClass, tstClass);
//            }
//        }
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc=" disabled code ">
//        /**
//         */
//        private Constructor createTestConstructor(String className) {
//            Constructor constr = tgtPkg.getConstructor().createConstructor(
//                               className,               // name
//                               Collections.EMPTY_LIST,  // annotations
//                               Modifier.PUBLIC,         // modifiers
//                               null,                    // Javadoc text
//                               null,                    // Javadoc - object
//                               null,                    // body - object
//                               "super(testName);\n",    // body - text  //NOI18N
//                               Collections.EMPTY_LIST,  // type parameters
//                               createTestConstructorParams(),  // parameters
//                               null);                   // exception names
//            return constr;
//        }
//
//        /**
//         */
//        private List/*<Parameter>*/ createTestConstructorParams() {
//            Parameter param = tgtPkg.getParameter().createParameter(
//                                "testName",             // parameter name
//                                Collections.EMPTY_LIST, // annotations
//                                false,                  // not final
//                                TestUtil.getTypeReference(   // type
//                                        tgtPkg, "String"),              //NOI18N
//                                0,                      // dimCount
//                                false);                 // is not var.arg.
//            return Collections.singletonList(param);
//        }
        // </editor-fold>
        
        /**
         * Creates a public static {@code main(String[])} method
         * with the body taken from settings.
         *
         * @param  maker  {@code TreeMaker} to use for creating the method
         * @return  created {@code main(...)} method,
         *          or {@code null} if the method body would be empty
         */
        private MethodTree createMainMethod(TreeMaker maker) {
            String initialMainMethodBody = getInitialMainMethodBody();
            if (initialMainMethodBody.length() == 0) {
                return null;
            }
            
            ModifiersTree modifiers = maker.Modifiers(
                    createModifierSet(Modifier.PUBLIC, Modifier.STATIC));
            VariableTree param = maker.Variable(
                            maker.Modifiers(Collections.<Modifier>emptySet()),
                            "argList",                                  //NOI18N
                            maker.Identifier("String[]"),               //NOI18N
                            null);            //initializer - not used in params
            MethodTree mainMethod = maker.Method(
                  modifiers,                            //public static
                  "main",                               //method name "main"
                  maker.PrimitiveType(TypeKind.VOID),   //return type "void"
                  Collections.<TypeParameterTree>emptyList(),     //type params
                  Collections.<VariableTree>singletonList(param), //method param
                  Collections.<ExpressionTree>emptyList(),        //throws-list
                  '{' + initialMainMethodBody + '}',    //body text
                  null);                                //only for annotations
            
            return mainMethod;
        }
        
        // <editor-fold defaultstate="collapsed" desc=" disabled code ">
//        /**
//         */
//        private void createAbstractImpl(JavaClass srcClass,
//                                        JavaClass tstClass) {
//            String implClassName = srcClass.getSimpleName() + "Impl";   //NOI18N
//            JavaClass innerClass = tstClass.getInnerClass(implClassName, false);
//
//            if (innerClass == null) {
//                String javadocText = 
//                        generateMethodJavadoc
//                        ? javadocText = NbBundle.getMessage(
//                              TestCreator.class,
//                              "TestCreator.abstracImpl.JavaDoc.comment",//NOI18N
//                              srcClass.getName())
//                        : null;
//
//                // superclass
//                MultipartId supClass
//                        = tgtPkg.getMultipartId().createMultipartId(
//                                srcClass.isInner() ? srcClass.getName()
//                                                   : srcClass.getSimpleName(),
//                                null,
//                                Collections.EMPTY_LIST);
//
//                innerClass = tgtPkg.getJavaClass().createJavaClass(
//                                implClassName,          // class name
//                                Collections.EMPTY_LIST, // annotations
//                                Modifier.PRIVATE,       // modifiers
//                                javadocText,            // Javadoc text
//                                null,                   // Javadoc - object
//                                Collections.EMPTY_LIST, // contents
//                                null,                   // super class name
//                                Collections.EMPTY_LIST, // interface names
//                                Collections.EMPTY_LIST);// type parameters
//                
//                if (srcClass.isInterface()) {
//                    innerClass.getInterfaceNames().add(supClass);
//                } else {
//                    innerClass.setSuperClassName(supClass);
//                }
//
//                createImpleConstructors(srcClass, innerClass);
//                tstClass.getFeatures().add(innerClass);
//            }
//
//            // created dummy implementation for all abstract methods
//            List abstractMethods = TestUtil.collectFeatures(
//                                            srcClass,
//                                            Method.class,
//                                            Modifier.ABSTRACT,
//                                            true);
//            for (Iterator i = abstractMethods.iterator(); i.hasNext(); ) {
//                Method oldMethod = (Method) i.next();
//                if (innerClass.getMethod(
//                        oldMethod.getName(),
//                        TestUtil.getParameterTypes(oldMethod.getParameters()),
//                        false) == null) {
//                    Method newMethod = createMethodImpl(oldMethod);
//                    innerClass.getFeatures().add(newMethod);
//                }
//
//            }
//        }
//
//        /**
//         */
//        private void createImpleConstructors(JavaClass srcClass,
//                                             JavaClass tgtClass) {
//            List constructors = TestUtil.filterFeatures(srcClass,
//                                                        Constructor.class);
//            for (Iterator i = constructors.iterator(); i.hasNext(); ) {
//                Constructor ctr = (Constructor) i.next();
//                
//                if (Modifier.isPrivate(ctr.getModifiers())) {
//                    continue;
//                }
//                
//                Constructor nctr = tgtPkg.getConstructor().createConstructor();
//                nctr.setBodyText("super("                               //NOI18N
//                                 + getParameterString(ctr.getParameters())
//                                 + ");\n");                             //NOI18N
//                nctr.getParameters().addAll(
//                        TestUtil.cloneParams(ctr.getParameters(), tgtPkg));
//                tgtClass.getFeatures().add(nctr);
//            }
//        }
//
//        /**
//         */
//        private Method createMethodImpl(Method origMethod)  {
//            Method  newMethod = tgtPkg.getMethod().createMethod();
//
//            newMethod.setName(origMethod.getName());
//
//            /* Set modifiers of the method: */
//            int mod = origMethod.getModifiers() & ~Modifier.ABSTRACT;
//            if (((JavaClass) origMethod.getDeclaringClass()).isInterface()) {
//                mod |= Modifier.PUBLIC;
//            }
//            newMethod.setModifiers(mod);
//
//            // prepare the body of method implementation
//            StringBuffer    body = new StringBuffer(200);
//            if (generateSourceCodeHints) {
//                body.append(NbBundle.getMessage(
//                        TestCreator.class,
//                        "TestCreator.methodImpl.bodyComment"));         //NOI18N
//                body.append("\n\n");                                    //NOI18N
//            }
//
//            newMethod.setType(origMethod.getType());
//            Type type = origMethod.getType();
//            if (type != null) {
//                String value = null;
//                if ((type instanceof JavaClass) || (type instanceof Array)) {
//                    value = "null";                                     //NOI18N
//                } else if (type instanceof PrimitiveType) {
//                    PrimitiveTypeKindEnum tke = (PrimitiveTypeKindEnum)
//                                               ((PrimitiveType) type).getKind();
//                    if (tke.equals(PrimitiveTypeKindEnum.BOOLEAN)) {
//                        value = "false";                                //NOI18N
//                    } else if (!tke.equals(PrimitiveTypeKindEnum.VOID)) {
//                        value = "0";                                    //NOI18N
//                    }
//                }
//
//                if (value != null) {
//                    body.append("return ").append(value).append(";\n"); //NOI18N
//                }
//            }
//
//            newMethod.setBodyText(body.toString());
//
//            // parameters
//            newMethod.getParameters().addAll(
//                    TestUtil.cloneParams(origMethod.getParameters(), tgtPkg));
//
//            return newMethod;
//        }
//
//        /**
//         */
//        private String generateJavadoc(JavaClass srcClass, Method srcMethod) {
//            return NbBundle.getMessage(
//                        TestCreator.class,
//                        "TestCreator.variantMethods.JavaDoc.comment",   //NOI18N
//                        srcMethod.getName(),
//                        srcClass.getName());
//        }
        // </editor-fold>
        
        /**
         */
        private BlockTree generateTestMethodBody(WorkingCopy workingCopy,
                                                 TypeElement srcClass,
                                                 ExecutableElement srcMethod,
                                                 boolean useNoArgConstructor) {
            TreeMaker maker = workingCopy.getTreeMaker();
            
            boolean isStatic = srcMethod.getModifiers().contains(Modifier.STATIC);
            List<StatementTree> statements = new ArrayList<StatementTree>(8);
            
            if (generateDefMethodBody) {
                StatementTree sout = generateSystemOutPrintln(
                                        maker,
                                        srcMethod.getSimpleName().toString());
                List<VariableTree> paramVariables = generateParamVariables(
                                        maker,
                                        srcMethod);
                statements.add(sout);
                statements.addAll(paramVariables);
                
                if (!isStatic) {
                    VariableTree instanceVarInit = maker.Variable(
                            maker.Modifiers(Collections.<Modifier>emptySet()),
                            INSTANCE_VAR_NAME,
                            maker.QualIdent(srcClass),
                            useNoArgConstructor
                                 ? generateNoArgConstructorCall(maker, srcClass)
                                 : maker.Literal(null));
                    statements.add(instanceVarInit);
                }
                
                MethodInvocationTree methodCall = maker.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),    //type args.
                        maker.MemberSelect(
                                isStatic ? maker.QualIdent(srcClass)
                                         : maker.Identifier(INSTANCE_VAR_NAME),
                                srcMethod.getSimpleName()),
                        createIdentifiers(maker, paramVariables));
                
                TypeMirror retType = srcMethod.getReturnType();
                TypeKind retTypeKind = retType.getKind();
                
                if (retTypeKind == TypeKind.VOID) {
                    StatementTree methodCallStmt = maker.ExpressionStatement(methodCall);
                    
                    statements.add(methodCallStmt);
                } else {
                    ExpressionTree retTypeTree = retTypeKind.isPrimitive()
                            ? maker.Identifier(retType.toString())
                            : maker.QualIdent(
                                    workingCopy.getTypes().asElement(retType));
                    
                    VariableTree expectedValue = maker.Variable(
                            maker.Modifiers(NO_MODIFIERS),
                            EXP_RESULT_VAR_NAME,
                            retTypeTree,
                            getDefaultValue(maker, retType));
                    VariableTree actualValue = maker.Variable(
                            maker.Modifiers(NO_MODIFIERS),
                            RESULT_VAR_NAME,
                            retTypeTree,
                            methodCall);
                    
                    List<ExpressionTree> comparisonArgs = new ArrayList<ExpressionTree>(2);
                    comparisonArgs.add(maker.Identifier(expectedValue.getName().toString()));
                    comparisonArgs.add(maker.Identifier(actualValue.getName().toString()));
                    
                    MethodInvocationTree comparison = maker.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),//type args.
                            maker.Identifier("assertEquals"),           //NOI18N
                            comparisonArgs);
                    StatementTree comparisonStmt = maker.ExpressionStatement(
                            comparison);
                    
                    statements.add(expectedValue);
                    statements.add(actualValue);
                    statements.add(comparisonStmt);
                }
            }
            
            //PENDING - source code hints
//            if (generateSourceCodeHints) {
//                // generate comments to bodies
//                if (needsEmptyLine) {
//                    newBody.append('\n');
//                    needsEmptyLine = false;
//                }
//                newBody.append(NbBundle.getMessage(
//                    TestCreator.class,
//                    generateDefMethodBody
//                           ? "TestCreator.variantMethods.defaultComment"//NOI18N
//                           : "TestCreator.variantMethods.onlyComment")) //NOI18N
//                       .append('\n');
//            }
            
            if (generateDefMethodBody) {
                String failMsg = NbBundle.getMessage(
                        TestCreator.class,
                        "TestCreator.variantMethods.defaultFailMsg");   //NOI18N
                MethodInvocationTree failMethodCall = maker.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),    //type args.
                        maker.Identifier("fail"),                       //NOI18N
                        Collections.<ExpressionTree>singletonList(
                                maker.Literal(failMsg)));
                statements.add(maker.ExpressionStatement(failMethodCall));
            }
            
            return maker.Block(statements, false);
        }

        /**
         */
        private StatementTree generateSystemOutPrintln(TreeMaker maker,
                                                       String arg) {
            MethodInvocationTree methodInvocation = maker.MethodInvocation(
                    Collections.<ExpressionTree>emptyList(),        //type args
                    maker.MemberSelect(
                            maker.MemberSelect(
                                    maker.Identifier("System"), "out"), "println"),//NOI18N
                    Collections.<LiteralTree>singletonList(
                            maker.Literal(arg)));                   //args.
            return maker.ExpressionStatement(methodInvocation);
        }
        
        /**
         */
        private List<VariableTree> generateParamVariables(
                                                TreeMaker maker,
                                                ExecutableElement srcMethod) {
            List<? extends VariableElement> params = srcMethod.getParameters();
            if ((params == null) || params.isEmpty()) {
                return Collections.<VariableTree>emptyList();
            }
            
            Set<Modifier> noModifiers = Collections.<Modifier>emptySet();
            List<VariableTree> paramVariables = new ArrayList<VariableTree>(params.size());
            String[] varNames = getTestSkeletonVarNames(params);
            int index = 0;
            for (VariableElement param : params) {
                TypeMirror paramType = param.asType();
                paramVariables.add(
                        maker.Variable(maker.Modifiers(noModifiers),
                                       varNames[index++],
                                       maker.Type(paramType),
                                       getDefaultValue(maker, paramType)));
            }
            return paramVariables;
        }
        
        /**
         */
        private List<IdentifierTree> createIdentifiers(
                                                TreeMaker maker,
                                                List<VariableTree> variables) {
            List<IdentifierTree> identifiers;
            if (variables.isEmpty()) {
                identifiers = Collections.<IdentifierTree>emptyList();
            } else {
                identifiers = new ArrayList<IdentifierTree>(variables.size());
                for (VariableTree var : variables) {
                    identifiers.add(maker.Identifier(var.getName().toString()));
                }
            }
            return identifiers;
        }
        
        /**
         * Builds list of variable names for use in a test method skeleton.
         * By default, names of variables are same as names of tested method's
         * declared parameters. There are three variable names reserved
         * for variables holding the instance the tested method will be called on,
         * the expected result and the actual result returned
         * by the tested method. This method resolves a potential conflict
         * if some of the tested method's parameter's name is one of these
         * reserved names - in this case, the variable name used is a slight
         * modification of the declared parameter's name. The method also resolves
         * cases that some or all parameters are without name - in this case,
         * an arbitrary name is assigned to each of these unnamed parameters.
         * The goal is to ensure that all of the used variable names are unique.
         *
         * @param  sourceMethodParams 
         *                  list of tested method's parameters (items are of type
         *                  <code>org.netbeans.jmi.javamodel.TypeParameter</code>)
         * @return  variable names used for default values of the tested method's
         *          parameters (the reserved variable names are not included)
         */
        private String[] getTestSkeletonVarNames(
                final List<? extends VariableElement> sourceMethodParams) {

            /* Handle the trivial case: */
            if (sourceMethodParams.isEmpty()) {
                return new String[0];
            }

            final int count = sourceMethodParams.size();
            String[] varNames = new String[count];
            boolean[] conflicts = new boolean[count];
            boolean issueFound = false;

            Set<String> varNamesSet = new HashSet<String>((int) ((count + 2) * 1.4));
            varNamesSet.add(INSTANCE_VAR_NAME);
            varNamesSet.add(RESULT_VAR_NAME);
            varNamesSet.add(EXP_RESULT_VAR_NAME);

            Iterator<? extends VariableElement> it = sourceMethodParams.iterator();
            for (int i = 0; i < count; i++) {
                String paramName = it.next().getSimpleName().toString();
                varNames[i] = paramName;

                if (paramName == null) {
                    issueFound = true;
                } else if (!varNamesSet.add(paramName)) {
                    conflicts[i] = true;
                    issueFound = true;
                } else {
                    conflicts[i] = false;
                }
            }

            if (issueFound) {
                for (int i = 0; i < count; i++) {
                    String paramName;
                    if (varNames[i] == null) {
                        paramName = ARTIFICAL_VAR_NAME_BASE + i;
                        if (varNamesSet.add(paramName)) {
                            varNames[i] = paramName;
                            continue;
                        } else {
                            conflicts[i] = true;
                        }
                    }
                    if (conflicts[i]) {
                        String paramNamePrefix = varNames[i] + '_';

                        int index = 2;
                        while (!varNamesSet.add(
                                        paramName = (paramNamePrefix + (index++))));
                        varNames[i] = paramName;
                    }
                }
            }

            return varNames;
        }
        
        /**
         */
        private ExpressionTree getDefaultValue(TreeMaker maker,
                                               TypeMirror type) {
            ExpressionTree defValue;
            TypeKind typeKind = type.getKind();
            if (typeKind.isPrimitive()) {
                switch (typeKind) {
                    case BOOLEAN:
                        defValue = maker.Literal(Boolean.FALSE);
                        break;
                    case CHAR:
                        defValue = maker.Literal(new Character(' '));
                        break;
                    case BYTE:
                        defValue = maker.Literal(new Byte((byte) 0));
                        break;
                    case SHORT:
                        defValue = maker.Literal(new Short((short) 0));
                        break;
                    case INT:
                        defValue = maker.Literal(new Integer(0));
                        break;
                    case FLOAT:
                        defValue = maker.Literal(new Float(0.0F));
                        break;
                    case LONG:
                        defValue = maker.Literal(new Long(0L));
                        break;
                    case DOUBLE:
                        defValue = maker.Literal(new Double(0.0));
                        break;
                    default:
                        assert false : "unknown primitive type";        //NOI18N
                        defValue = maker.Literal(new Integer(0));
                        break;
                }
            } else if ((typeKind == TypeKind.DECLARED)
                       && type.toString().equals("java.lang.String")) { //NOI18N
                defValue = maker.Literal("");                           //NOI18N
            } else {
                defValue = maker.Literal(null);
            }
            return defValue;
        }
        
        /**
         */
        private ExpressionTree generateNoArgConstructorCall(TreeMaker maker,
                                                            TypeElement cls) {
            return maker.NewClass(
                    null,                                   //enclosing instance
                    Collections.<ExpressionTree>emptyList(),//type arguments
                    maker.QualIdent(cls),                   //class identifier
                    Collections.<ExpressionTree>emptyList(),//arguments list
                    null);                                  //class body
        }

        /**
         */
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
        
        /**
         */
        private boolean isTestableMethod(ExecutableElement method) {
            if (method.getKind() != ElementKind.METHOD) {
                throw new IllegalArgumentException();
            }
            
            return isMethodAcceptable(method);
        }
        
        /**
         */
        private boolean hasAccessibleNoArgConstructor(TypeElement srcClass) {
            boolean answer;
            
            List<ExecutableElement> constructors
                 = ElementFilter.constructorsIn(srcClass.getEnclosedElements());
            
            if (constructors.isEmpty()) {
                answer = true;  //no explicit constructor -> synthetic no-arg. constructor
            } else {
                answer = false;
                for (ExecutableElement constructor : constructors) {
                    if (constructor.getParameters().isEmpty()) {
                        answer = !constructor.getModifiers().contains(Modifier.PRIVATE);
                        break;
                    }
                }
            }
            return answer;
        }
        
        /**
         */
        private boolean throwsNonRuntimeExceptions(CompilationInfo compInfo,
                                                   ExecutableElement method) {
            List<? extends TypeMirror> thrownTypes = method.getThrownTypes();
            if (thrownTypes.isEmpty()) {
                return false;
            }
            
            String runtimeExcName = "java.lang.RuntimeException";       //NOI18N
            TypeElement runtimeExcElement = compInfo.getElements()
                                            .getTypeElement(runtimeExcName);
            if (runtimeExcElement == null) {
                Logger.getLogger("junit").log(                          //NOI18N
                        Level.WARNING,
                        "Could not find TypeElement for "               //NOI18N
                                + runtimeExcName);
                return true;
            }

            Types types = compInfo.getTypes();
            TypeMirror runtimeExcType = runtimeExcElement.asType();
            for (TypeMirror exceptionType : thrownTypes) {
                if (!types.isSubtype(exceptionType, runtimeExcType)) {
                    return true;
                }
            }
            
            return false;
        }
        
        /**
         */
        private <T extends Element> List<T> resolveHandles(
                                                CompilationInfo compInfo,
                                                List<ElementHandle<T>> handles) {
            if (handles == null) {
                return null;
            }
            if (handles.isEmpty()) {
                return Collections.<T>emptyList();
            }
            
            List<T> elements = new ArrayList<T>(handles.size());
            for (ElementHandle<T> handle : handles) {
                elements.add(handle.resolve(compInfo));
            }
            return elements;
        }
        
        /**
         */
        private TypeElement getTestTypeElem(Elements elements) {
            if (testTypeElem == null) {
                testTypeElem = getElemForClassName(
                                            "junit.framework.Test",     //NOI18N
                                            elements);
            }
            return testTypeElem;
        }
        
        /**
         */
        private TypeElement getTestCaseTypeElem(Elements elements) {
            if (testCaseTypeElem == null) {
                testCaseTypeElem = getElemForClassName(
                                            "junit.framework.TestCase", //NOI18N
                                            elements);
            }
            return testCaseTypeElem;
        }
        
        /**
         */
        private TypeElement getTestSuiteTypeElem(Elements elements) {
            if (testSuiteTypeElem == null) {
                testSuiteTypeElem = getElemForClassName(
                                            "junit.framework.TestSuite",//NOI18N
                                            elements);
            }
            return testSuiteTypeElem;
        }
        
        /**
         */
        private TypeElement getElemForClassName(String className,
                                                Elements elements) {
            TypeElement elem = elements.getTypeElement(className);
            if (elem == null) {
                ErrorManager.getDefault().log(
                        ErrorManager.ERROR,
                        "Could not find TypeElement for " + className); //NOI18N
            }
            return elem;
        }
        
        /**
         * Stops this creator - cancels creation of a test class.
         */
        public void cancel() {
            cancelled = true;
        }
        
        /**
         */
        private void classProcessed(ClassTree cls) {
            if (processedClassNames == null) {
                processedClassNames = new ArrayList<String>(4);
            }
            processedClassNames.add(cls.getSimpleName().toString());
        }
        
        /**
         */
        List<String> getProcessedClassNames() {
            return processedClassNames != null
                   ? processedClassNames
                   : Collections.<String>emptyList();
        }
        
    }
    
    /**
     * Checks whether the given class or at least one of its nested classes
     * is testable.
     *
     * @param  compInfo  used for {@link CompilationInfo#getElements()}
     *                   and {@link CompilationInfo#getTypes()}
     * @param  classElem  class to be checked
     * @return  TestabilityResult that isOk, if the class is testable or carries
     *          the information why the class is not testable
     */
    public TestabilityResult isClassTestable(CompilationInfo compInfo,
                                             TypeElement classElem) {
        assert classElem != null;
        
        TestabilityResult result = isClassTestableSingle(compInfo, classElem);

        if (result.isTestable()) {
            return TestabilityResult.OK;
        }

        List<? extends Element> enclosedElems = classElem.getEnclosedElements();
        if (enclosedElems.isEmpty()) {
            /* Not testable, no contained types - no more chance: */
            return result;
        }
        
        List<TypeElement> enclosedTypes = ElementFilter.typesIn(enclosedElems);
        if (enclosedTypes.isEmpty()) {
            /* Not testable, no contained types - no more chance: */
            return result;
        }
        
        /* Not testable but maybe one of its nested classes is testable: */
        List<TypeElement> stack
               = new ArrayList<TypeElement>(Math.max(10, enclosedTypes.size()));
        stack.addAll(enclosedTypes);
        int stackSize = stack.size();

        Set<TypeElement> nonTestable = new HashSet<TypeElement>(64);
        nonTestable.add(classElem);

        do {
            TypeElement classToCheck = stack.remove(--stackSize);
            
            if (!TopClassFinder.isTestable(classElem)) {   //it is an annotation
                continue;
            }

            if (!nonTestable.add(classToCheck)) {
                continue; //we already know this single class is nontestable
            }

            TestabilityResult resultSingle
                                = isClassTestableSingle(compInfo, classToCheck);
            if (resultSingle.isTestable()) {
                return TestabilityResult.OK;
            } else {
                result = TestabilityResult.combine(result, resultSingle);
            }

            enclosedTypes = ElementFilter.typesIn(classToCheck.getEnclosedElements());
            if (!enclosedTypes.isEmpty()) {
                stack.addAll(enclosedTypes);
                stackSize = stack.size();
            }
        } while (stackSize != 0);

        /* So not a single contained class is testable - no more chance: */
        return result;
    }
    
    
    /* private methods */
    
    /**
     * Checks whether the given class is testable.
     *
     * @param  jc  class to be checked
     * @return  TestabilityResult that isOk, if the class is testable or carries
     *          the information why the class is not testable
     */
    private TestabilityResult isClassTestableSingle(CompilationInfo compInfo,
                                                    TypeElement classElem) {
        assert classElem != null;
        
        TestabilityResult result = TestabilityResult.OK;

        /*
         * If the class is a test class and test classes should be skipped,
         * do not check nested classes (skip all):
         */
        /* Check if the class itself (w/o nested classes) is testable: */
        Set<Modifier> modifiers = classElem.getModifiers();

        if (modifiers.contains(PRIVATE))
            result = TestabilityResult.combine(result, TestabilityResult.PRIVATE_CLASS);
        if (skipTestClasses && TestUtil.isClassImplementingTestInterface(compInfo, classElem)) 
            result = TestabilityResult.combine(result, TestabilityResult.TEST_CLASS);
        if (skipPkgPrivateClasses && !EnumSet.copyOf(modifiers).removeAll(ACCESS_MODIFIERS))
            result = TestabilityResult.combine(result, TestabilityResult.PACKAGE_PRIVATE_CLASS);
        if (skipAbstractClasses && modifiers.contains(ABSTRACT))
            result = TestabilityResult.combine(result, TestabilityResult.ABSTRACT_CLASS);
        if (!modifiers.contains(STATIC) && (classElem.getNestingKind() != NestingKind.TOP_LEVEL))
            result = TestabilityResult.combine(result, TestabilityResult.NONSTATIC_INNER_CLASS);
        if (!hasTestableMethods(classElem))
            result = TestabilityResult.combine(result, TestabilityResult.NO_TESTEABLE_METHODS);
        if (skipExceptionClasses && TestUtil.isClassException(compInfo, classElem)) 
            result = TestabilityResult.combine(result, TestabilityResult.EXCEPTION_CLASS);

        return result;
    }
    
    /**
     */
    private static String createTestMethodName(String smName) {
        return "test"                                                   //NOI18N
               + smName.substring(0,1).toUpperCase() + smName.substring(1);
    }
    
//XXX: retouche
//    /**
//     *
//     * @param cls JavaClass to generate the comment to.
//     */
//    private static void addClassBodyComment(JavaClass cls) {
//        int off = cls.getEndOffset() - 1;        
//        String theComment1 = NbBundle.getMessage(TestCreator.class,
//                                                 CLASS_COMMENT_LINE1);
//        String theComment2 = NbBundle.getMessage(TestCreator.class,
//                                                 CLASS_COMMENT_LINE2);
//        String indent = getIndentString();
//        DiffElement diff = new DiffElement(
//                off,
//                off,
//                indent + theComment1 + '\n'
//                + indent + theComment2 + '\n' + '\n');
//        ((ResourceImpl) cls.getResource()).addExtDiff(diff);
//    }
    
    /**
     */
    private boolean hasTestableMethods(TypeElement classElem) {
        List<? extends Element> enclosedElems = classElem.getEnclosedElements();
        if (enclosedElems.isEmpty()) {
            return false;
        }
        
        List<ExecutableElement> methods = ElementFilter.methodsIn(enclosedElems);
        if (methods.isEmpty()) {
            return false;
        }
        
        for (ExecutableElement method : methods) {
            if (isMethodAcceptable(method)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks whether a test for the given method should be created.
     * Access modifiers of the given method are compared to this creator's
     * settings.
     *
     * @param  m  method to be checked
     * @return  <code>true</code> if this creator is configured to create tests
     *          for methods having the given method's access modifiers;
     *          <code>false</code> otherwise
     */
    private boolean isMethodAcceptable(ExecutableElement method) {
        Set<Modifier> modifiers = method.getModifiers();
        
        return (testPkgPrivateMethods && !EnumSet.copyOf(modifiers).removeAll(ACCESS_MODIFIERS))
               || EnumSet.copyOf(modifiers).removeAll(methodAccessModifiers);
    }
    
    /**
     */
    private String getInitialMainMethodBody() {
        if (initialMainMethodBody == null) {
            initialMainMethodBody = JUnitSettings.getDefault()
                                    .getGenerateMainMethodBody();
            if (initialMainMethodBody == null) {
                /*
                 * set it to a non-null value so that this method does not try
                 * to load it from the settings next time
                 */
                initialMainMethodBody = "";                             //NOI18N
            }
        }
        return initialMainMethodBody;
    }
    
    /**
     * Creates a {@code Set} of {@code Modifier}s from the given list
     * of modifiers.
     * 
     * @param  modifiers  modifiers that should be contained in the set
     * @return  set containing exactly the given modifiers
     */
    private static Set<Modifier> createModifierSet(Modifier... modifiers) {
        EnumSet<Modifier> modifierSet = EnumSet.noneOf(Modifier.class);
        for (Modifier m : modifiers) {
            modifierSet.add(m);
        }
        return modifierSet;
    }
    
}
