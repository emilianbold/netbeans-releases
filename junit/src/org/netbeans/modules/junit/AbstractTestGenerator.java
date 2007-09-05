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
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.util.NbBundle;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.STATIC;
import static org.netbeans.modules.junit.TestCreator.ACCESS_MODIFIERS;

/**
 *
 * @author  Marian Petras
 */
abstract class AbstractTestGenerator implements CancellableTask<WorkingCopy>{
    
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
    /** */
    private static final EnumSet<Modifier> NO_MODIFIERS
            = EnumSet.noneOf(Modifier.class);
    
    /**
     * Returns {@code EnumSet} of all access modifiers.
     * 
     * @return  {@code EnumSet} of all access modifiers;
     *          it is guaranteed that the returned set always contains
     *          the same set of {@code Modifier}s, but the returned
     *          instance may not always be the same
     */
    protected static EnumSet<Modifier> accessModifiers() {
        /*
         * An alternative would be to create an instance of
         * unmodifiable Set<Modifier> (e.g. Collections.unmodifiableSet(...))
         * and always return this instance. But the instance would not be an
         * instance of (subclass of) EnumSet which would significantly slow down
         * many operations performed on it.
         */
        return EnumSet.copyOf(ACCESS_MODIFIERS);
    }
    
    /**
     * Returns an empty {@code EnumSet} of {@code Modifier}s.
     * 
     * @return  empty {@code EnumSet} of all {@code Modifier}s;
     *          it is guaranteed that the returned set is always empty
     *          but the returned instance may not always be the same
     */
    protected static EnumSet<Modifier> noModifiers() {
        /*
         * An alternative would be to create an instance of
         * unmodifiable Set<Modifier> (e.g. Collections.<Modifier>emptySet())
         * and always return that instance. But the instance would not be an
         * instance of (subclass of) EnumSet which would significantly slow down
         * many operations performed on it.
         */
        return EnumSet.copyOf(NO_MODIFIERS);
    }
    
    /** */
    protected final TestGeneratorSetup setup;

    private final List<ElementHandle<TypeElement>> srcTopClassElemHandles;

    private final List<String> suiteMembers;

    private final boolean isNewTestClass;

    private List<String>processedClassNames;

    /**
     * cached value of <code>JUnitSettings.getGenerateMainMethodBody()</code>
     */
    private String initialMainMethodBody;
    
    private volatile boolean cancelled = false;


    /**
     * Used when creating a new empty test class.
     */
    protected AbstractTestGenerator(TestGeneratorSetup setup) {
        this.setup = setup;
        this.srcTopClassElemHandles = null;
        this.suiteMembers = null;
        this.isNewTestClass = true;   //value not used
    }

    /**
     * Used when creating a test class for a given source class
     * or when creating a test suite.
     */
    protected AbstractTestGenerator(
                    TestGeneratorSetup setup,
                    List<ElementHandle<TypeElement>> srcTopClassHandles,
                    List<String>suiteMembers,
                    boolean isNewTestClass) {
        this.setup = setup;
        this.srcTopClassElemHandles = srcTopClassHandles;
        this.suiteMembers = suiteMembers;
        this.isNewTestClass = isNewTestClass;
    }

    /**
     */
    public void run(WorkingCopy workingCopy) throws IOException {
        
        workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);

        CompilationUnitTree compUnit = workingCopy.getCompilationUnit();
        List<ClassTree> tstTopClasses = TopClassFinder.findTopClasses(
                                            compUnit,
                                            workingCopy.getTreeUtilities());
        TreePath compUnitPath = new TreePath(compUnit);

        List<TypeElement> srcTopClassElems
                = resolveHandles(workingCopy, srcTopClassElemHandles);

        if ((srcTopClassElems != null) && !srcTopClassElems.isEmpty()) {

            final String className
                    = workingCopy.getClasspathInfo()
                      .getClassPath(ClasspathInfo.PathKind.SOURCE)
                      .getResourceName(workingCopy.getFileObject(), '.', false);

            /* Create/update a test class for each testable source class: */
            for (TypeElement srcTopClass : srcTopClassElems) {
                createOrUpdateTestClass(srcTopClass,
                                        tstTopClasses,
                                        className,
                                        compUnitPath,
                                        workingCopy);
            }
        } else if (suiteMembers != null) {          //test suite
            for (ClassTree tstClass : tstTopClasses) {
                TreePath tstClassTreePath = new TreePath(compUnitPath,
                                                         tstClass);
                ClassTree origTstTopClass = tstClass;
                ClassTree tstTopClass = generateMissingSuiteClassMembers(
                                                tstClass,
                                                tstClassTreePath,
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
    private void createOrUpdateTestClass(TypeElement srcTopClass,
                                         List<ClassTree> tstTopClasses,
                                         String testClassName,
                                         TreePath compUnitPath,
                                         WorkingCopy workingCopy) {
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
                                       srcTopClass,
                                       srcMethods,
                                       tstTopClass,
                                       tstTopClassTreePath,
                                       isNewTestClass,
                                       workingCopy);
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
                    || tstClassName.equals(TestUtil.getSimpleName(testClassName))) {
                tstTopClass = generateNewTestClass(workingCopy,
                                                   tstClassName,
                                                   srcTopClass,
                                                   srcMethods);
                //PENDING - add the top class to the CompilationUnit

                //PENDING - generate suite method
            }
        }
    }

    /**
     */
    private ClassTree generateNewTestClass(WorkingCopy workingCopy,
                                           String name,
                                           TypeElement srcClass,
                                           List<ExecutableElement> srcMethods) {
        List<MethodTree> testMethods = generateTestMethods(srcClass,
                                                           srcMethods,
                                                           workingCopy);
        return composeNewTestClass(workingCopy, name, testMethods);
    }

    /**
     */
    protected abstract ClassTree composeNewTestClass(
                                        WorkingCopy workingCopy,
                                        String name,
                                        List<? extends Tree> members);

    /**
     */
    protected abstract List<? extends Tree> generateInitMembers(WorkingCopy workingCopy);

    /**
     */
    protected abstract ClassTree generateMissingInitMembers(
                                                ClassTree tstClass,
                                                TreePath tstClassTreePath,
                                                WorkingCopy workingCopy);
    
    /**
     */
    protected abstract boolean generateMissingInitMembers(
                                                 List<Tree> tstMembers,
                                                 ClassMap clsMap,
                                                 WorkingCopy workingCopy);
    
    /**
     * Finds position for the first init method.
     * 
     * @return  index where the first init method should be put,
     *          or {@code -1} if the method should be put to the end
     *          of the class
     */
    protected int getPlaceForFirstInitMethod(ClassMap clsMap) {
        int targetIndex;
        if (clsMap.containsMethods()) {
            targetIndex = clsMap.getFirstMethodIndex();
        } else if (clsMap.containsInitializers()) {
            targetIndex = clsMap.getLastInitializerIndex() + 1;
        } else if (clsMap.containsNestedClasses()) {
            targetIndex = clsMap.getFirstNestedClassIndex();
        } else {
            targetIndex = -1;        //end of the class
        }
        return targetIndex;
    }

    /**
     * 
     * @param  srcMethods  methods to create/update tests for
     * 
     */
    protected ClassTree generateMissingTestMethods(
                                    TypeElement srcClass,
                                    List<ExecutableElement> srcMethods,
                                    ClassTree tstClass,
                                    TreePath tstClassTreePath,
                                    boolean generateMissingInitMembers,
                                    WorkingCopy workingCopy) {
        if (srcMethods.isEmpty()) {
            return tstClass;
        }

        ClassMap clsMap = ClassMap.forClass(tstClass,
                                            tstClassTreePath,
                                            workingCopy.getTrees());

        List<? extends Tree> tstMembersOrig = tstClass.getMembers();
        List<Tree> tstMembers = new ArrayList<Tree>(tstMembersOrig.size() + 4);
        tstMembers.addAll(tstMembersOrig);

        if (generateMissingInitMembers) {
            generateMissingInitMembers(tstMembers, clsMap,
                                       workingCopy);
        }
        generateMissingPostInitMethods(tstClassTreePath,
                                         tstMembers,
                                         clsMap,
                                         workingCopy);

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
                            srcClass,
                            srcMethod,
                            useNoArgConstrutor.booleanValue(),
                            workingCopy);

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
    protected abstract void generateMissingPostInitMethods(
                                                TreePath tstClassTreePath,
                                                List<Tree> tstMembers,
                                                ClassMap clsMap,
                                                WorkingCopy workingCopy);
    
    /**
     */
    private List<MethodTree> generateTestMethods(
                                       TypeElement srcClass,
                                       List<ExecutableElement> srcMethods,
                                       WorkingCopy workingCopy) {
        if (srcMethods.isEmpty()) {
            return Collections.<MethodTree>emptyList();
        }

        boolean useNoArgConstrutor = hasAccessibleNoArgConstructor(srcClass);
        List<MethodTree> testMethods = new ArrayList<MethodTree>(srcMethods.size());
        for (ExecutableElement srcMethod : srcMethods) {
            testMethods.add(
                    generateTestMethod(srcClass,
                                       srcMethod,
                                       useNoArgConstrutor,
                                       workingCopy));
        }
        return testMethods;
    }

    /**
     */
    protected MethodTree generateTestMethod(TypeElement srcClass,
                                          ExecutableElement srcMethod,
                                          boolean useNoArgConstructor,
                                          WorkingCopy workingCopy) {
        final TreeMaker maker = workingCopy.getTreeMaker();

        String testMethodName = createTestMethodName(
                                    srcMethod.getSimpleName().toString());
        ModifiersTree modifiers = maker.Modifiers(createModifierSet(PUBLIC));
        List<ExpressionTree> throwsList;
        if (throwsNonRuntimeExceptions(workingCopy, srcMethod)) {
            throwsList = Collections.<ExpressionTree>singletonList(
                                    maker.Identifier("Exception"));     //NOI18N
        } else {
            throwsList = Collections.<ExpressionTree>emptyList();
        }

        MethodTree method = composeNewTestMethod(
                testMethodName,
                generateTestMethodBody(srcClass, srcMethod, useNoArgConstructor,
                                       workingCopy),
                throwsList,
                workingCopy);

        if (setup.isGenerateMethodJavadoc()) {
            Comment javadoc = Comment.create(
                NbBundle.getMessage(
                    TestCreator.class,
                    "TestCreator.variantMethods.JavaDoc.comment",       //NOI18N
                    srcMethod.getSimpleName().toString(),
                    srcClass.getSimpleName().toString()));
            maker.addComment(method, javadoc, false);
        }

        return method;
    }
    
    /**
     */
    protected String createTestMethodName(String smName) {
        return "test"                                                   //NOI18N
               + smName.substring(0,1).toUpperCase() + smName.substring(1);
    }
    
    /**
     */
    protected abstract MethodTree composeNewTestMethod(
                                            String testMethodName,
                                            BlockTree testMethodBody,
                                            List<ExpressionTree> throwsList,
                                            WorkingCopy workingCopy);

    /**
     */
    private ClassTree generateMissingSuiteClassMembers(
                                            ClassTree tstClass,
                                            TreePath tstClassTreePath,
                                            List<String> suiteMembers,
                                            boolean isNewTestClass,
                                            WorkingCopy workingCopy) {
        final TreeMaker maker = workingCopy.getTreeMaker();

        List<? extends Tree> tstMembersOrig = tstClass.getMembers();
        List<Tree> tstMembers = new ArrayList<Tree>(tstMembersOrig.size() + 2);
        tstMembers.addAll(tstMembersOrig);
        boolean membersChanged = false;

        ClassMap classMap = ClassMap.forClass(tstClass,
                                              tstClassTreePath,
                                              workingCopy.getTrees());

        if (isNewTestClass) {
            membersChanged |= generateMissingInitMembers(tstMembers,
                                                         classMap,
                                                         workingCopy);
        }

        return finishSuiteClass(tstClass,
                                tstClassTreePath,
                                tstMembers,
                                suiteMembers,
                                membersChanged,
                                classMap,
                                workingCopy);
    }
    
    /**
     */
    protected abstract ClassTree finishSuiteClass(
                                        ClassTree tstClass,
                                        TreePath tstClassTreePath,
                                        List<Tree> tstMembers,
                                        List<String> suiteMembers,
                                        boolean membersChanged,
                                        ClassMap classMap,
                                        WorkingCopy workingCopy);
    
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
                        "argList",                                      //NOI18N
                        maker.Identifier("String[]"),                   //NOI18N
                        null);            //initializer - not used in params
        MethodTree mainMethod = maker.Method(
              modifiers,                            //public static
              "main",                               //method name "main"//NOI18N
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
    protected BlockTree generateTestMethodBody(TypeElement srcClass,
                                               ExecutableElement srcMethod,
                                               boolean useNoArgConstructor,
                                               WorkingCopy workingCopy) {
        TreeMaker maker = workingCopy.getTreeMaker();

        boolean isStatic = srcMethod.getModifiers().contains(Modifier.STATIC);
        List<StatementTree> statements = new ArrayList<StatementTree>(8);

        if (setup.isGenerateDefMethodBody()) {
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
                        Collections.<ExpressionTree>emptyList(),    //type args.
                        maker.Identifier("assertEquals"),               //NOI18N
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

        if (setup.isGenerateDefMethodBody()) {
            String failMsg = NbBundle.getMessage(
                    TestCreator.class,
                    "TestCreator.variantMethods.defaultFailMsg");       //NOI18N
            MethodInvocationTree failMethodCall = maker.MethodInvocation(
                    Collections.<ExpressionTree>emptyList(),    //type args.
                    maker.Identifier("fail"),                           //NOI18N
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
                    assert false : "unknown primitive type";            //NOI18N
                    defValue = maker.Literal(new Integer(0));
                    break;
            }
        } else if ((typeKind == TypeKind.DECLARED)
                   && type.toString().equals("java.lang.String")) {     //NOI18N
            defValue = maker.Literal("");                               //NOI18N
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

        return setup.isMethodTestable(method);
    }

    /**
     */
    protected boolean hasAccessibleNoArgConstructor(TypeElement srcClass) {
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

        String runtimeExcName = "java.lang.RuntimeException";           //NOI18N
        TypeElement runtimeExcElement = compInfo.getElements()
                                        .getTypeElement(runtimeExcName);
        if (runtimeExcElement == null) {
            Logger.getLogger("junit").log(                              //NOI18N
                    Level.WARNING,
                    "Could not find TypeElement for "                   //NOI18N
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

    /* private methods */
    
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
    static Set<Modifier> createModifierSet(Modifier... modifiers) {
        EnumSet<Modifier> modifierSet = EnumSet.noneOf(Modifier.class);
        for (Modifier m : modifiers) {
            modifierSet.add(m);
        }
        return modifierSet;
    }
    
}
