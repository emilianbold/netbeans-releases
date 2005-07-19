/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;
import org.netbeans.modules.javacore.jmiimpl.javamodel.DiffElement;
import org.netbeans.modules.javacore.jmiimpl.javamodel.ResourceImpl;
import org.openide.ErrorManager;

import org.openide.util.NbBundle;
import org.netbeans.jmi.javamodel.*;
import org.netbeans.modules.javacore.api.JavaModel;


/**
 *
 * @author  vstejskal
 * @author  Marian Petras
 * @version 1.0
 */
public final class TestCreator {
    /* the class is final only for performance reasons */
    
    /* attributes - private */
    static private final String JUNIT_SUPER_CLASS_NAME                = "TestCase";
    static private final String JUNIT_FRAMEWORK_PACKAGE_NAME    = "junit.framework";
    
//    static private final String GENERATED_SUITE_BLOCK_START                = "--JUNIT:";
//    static private final String GENERATED_SUITE_BLOCK_END                  = ":JUNIT--";
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
    private static final int ACCESS_MODIFIERS = Modifier.PUBLIC
                                                | Modifier.PROTECTED
                                                | Modifier.PRIVATE;
    
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
    private int methodAccessModifiers = Modifier.PUBLIC | Modifier.PROTECTED;
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
    
    /** pattern of a multipart Java ID */
    private final Pattern javaIdFullPattern;
    
    
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
        javaIdFullPattern = Pattern.compile(RegexpPatterns.JAVA_ID_REGEX_FULL);
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
        
        methodAccessModifiers = 0;
        if (settings.isMembersPublic()) {
            methodAccessModifiers |= Modifier.PUBLIC;
        }
        if (settings.isMembersProtected()) {
            methodAccessModifiers |= Modifier.PROTECTED;
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
            methodAccessModifiers |= Modifier.PUBLIC;
        } else {
            methodAccessModifiers &= ~Modifier.PUBLIC;
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
            methodAccessModifiers |= Modifier.PROTECTED;
        } else {
            methodAccessModifiers &= ~Modifier.PROTECTED;
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
     * Implements equality by attributes of a wrapped Import.
     */
    static private class ImpEq {
        org.netbeans.jmi.javamodel.Import imp;
        
        ImpEq(org.netbeans.jmi.javamodel.Import imp) {
            this.imp = imp;
        }
        
        public boolean equals(Object rhs) {
            if (rhs instanceof ImpEq) {
                org.netbeans.jmi.javamodel.Import i = ((ImpEq)rhs).imp;
                return
                    (i.isStatic() == imp.isStatic()) &&
                    (i.isOnDemand() == imp.isOnDemand()) &&
                    (i.getName().equals(imp.getName()));
            }  else return false;
        }
        
        public int hashCode() {
            return imp.getName().hashCode();
        }
        
        
    }
    
    
    /**
     *
     */
    final class SingleResourceTestCreator {
     
        private final Resource srcRc, tstRc;
        private final JavaModelPackage tgtPkg;
        private final String tstPkgNameDot;
        private final List/*<Import>*/ tstImports;
        /** */
        private Map/*<String, String>*/ clsNames;

        
        /**
         */
        SingleResourceTestCreator(Resource srcRc, Resource tstRc) {
            this.srcRc = srcRc;
            this.tstRc = tstRc;
            
            tgtPkg = (JavaModelPackage) tstRc.refImmediatePackage();
            tstPkgNameDot = tstRc.getPackageName() + '.';
            tstImports = tstRc.getImports();
            clsNames = new HashMap/*<String>*/(20);
        }
        
        /**
         */
        void createTest(JavaClass srcClass, JavaClass tstClass) {
            tstRc.setPackageName(srcRc.getPackageName());
            addFrameworkImport();
            copySourceImports();
            fillTestClass(srcClass, tstClass);

            if (generateMainMethod && !TestUtil.hasMainMethod(tstClass)) {
                addMainMethod(tstClass);
            }
        }
        
        /**
         */
        void createEmptyTest(JavaClass tstClass) {
            addFrameworkImport();
            fillGeneral(tstClass);
        }

        /**
         */
        void createTestSuite(List listMembers,
                             String packageName,
                             JavaClass tstClass) {
            tstRc.setPackageName(packageName);
            addFrameworkImport();      
            fillSuiteClass(listMembers, packageName, tstClass);
            
            if (generateMainMethod && !TestUtil.hasMainMethod(tstClass)) {
                addMainMethod(tstClass);
            }
        }
        
        /**
         * Adds the JUnit framework import
         * (<code>import junit.framework.*</code>) to the test file,
         * if not already there.
         */
        private void addFrameworkImport() {

            /* Try to find the import: */
            for (Iterator/*<Import>*/ i = tstRc.getImports().iterator();
                    i.hasNext(); ) {
                Import imp = (Import) i.next();
                if (imp.getName().equals(JUNIT_FRAMEWORK_PACKAGE_NAME)
                        && imp.isOnDemand()
                        && !imp.isStatic()) {
                    return;     //import already there - quit!
                }
            }
            
            /* If the import is not there (not found), add it: */
            tstRc.getImports().add(
                    tgtPkg.getImport().createImport(
                            JUNIT_FRAMEWORK_PACKAGE_NAME,
                            null, 
                            false,      //not static
                            true));     //on demand (wildcard)
        }

        /**
         * Adds imports from the source but only those that are not
         * already present.
         */
        private void copySourceImports() {
            List/*<Import>*/ srcImportsList = srcRc.getImports();
            List/*<Import>*/ tstImportsList = tstRc.getImports();
            
            /* Create a Set of existing imports in the test class: */
            Set tImpSet = new HashSet((int) (tstImportsList.size() * 1.4f));
            for (Iterator/*<Import>*/ i = tstImportsList.iterator();
                    i.hasNext(); ) {
                tImpSet.add(new ImpEq((Import) i.next()));
            }
            
            final ImportClass tstImportFactory = tgtPkg.getImport();
            
            /*
             * Iterate through the imports in the source class and check whether
             * they are present in the test class - add those that aren't:
             */
            for (Iterator/*<Import>*/ i = srcImportsList.iterator();
                    i.hasNext(); ) {
                Import imp = (Import) i.next();
                if (!tImpSet.contains(new ImpEq(imp))) {
                    tstImportsList.add(tstImportFactory.createImport(
                                                imp.getName(),
                                                null,
                                                imp.isStatic(),
                                                imp.isOnDemand()));
                }
            }
        }
        
        /**
         */
        private void addMainMethod(JavaClass tstClass) {
            Method mainMethod = createMainMethod();
            if (mainMethod != null) {
                tstClass.getFeatures().add(mainMethod);
            }
        }

        /**
         */
        private void fillTestClass(JavaClass srcClass, JavaClass tstClass) {
            
            fillGeneral(tstClass);

            List innerClasses = TestUtil.filterFeatures(srcClass,
                                                        JavaClass.class);

            /* Create test classes for inner classes: */
            for (Iterator i = innerClasses.iterator(); i.hasNext(); ) {
                JavaClass innerCls = (JavaClass) i.next();

                if (!isClassTestable(innerCls).isTesteable()) {
                    continue;
                }
                    
                /*
                 * Check whether the test class for the inner class exists
                 * and create one if it does not exist:
                 */
                String innerTestClsName
                        = TestUtil.getTestClassName(innerCls.getSimpleName());
                JavaClass innerTestCls
                        = TestUtil.getClassBySimpleName(tstClass,
                                                        innerTestClsName);
                if (innerTestCls == null) {
                    innerTestCls = tgtPkg.getJavaClass().createJavaClass();
                    innerTestCls.setSimpleName(
                            tstClass.getName() + '.' + innerTestClsName);
                    tstClass.getFeatures().add(innerTestCls);
                }

                /* Process the tested inner class: */
                fillTestClass(innerCls, innerTestCls);

                /* Make the inner test class testable with JUnit: */
                innerTestCls.setModifiers(innerTestCls.getModifiers() | Modifier.STATIC);
            }

            /* Add the suite() method (only if we are supposed to do so): */
            if (generateSuiteClasses && !hasSuiteMethod(tstClass)) {
                tstClass.getFeatures().add(createTestClassSuiteMethod(tstClass));
            }

            /* Create missing test methods: */
            List srcMethods = TestUtil.filterFeatures(srcClass, Method.class);
            for (Iterator i = srcMethods.iterator(); i.hasNext(); ) {
                Method sm = (Method) i.next();
                if (isMethodAcceptable(sm) &&
                        tstClass.getMethod(createTestMethodName(sm.getName()),
                                          Collections.EMPTY_LIST,
                                          false)
                        == null) {
                    Method tm = createTestMethod(srcClass, sm);
                    tstClass.getFeatures().add(tm);
                }
            }

            /* Create abstract class implementation: */
            if (!skipAbstractClasses
                    && (Modifier.isAbstract(srcClass.getModifiers())
                        || srcClass.isInterface())) {
                createAbstractImpl(srcClass, tstClass);
            }
        }
        
        /**
         */
        private void fillSuiteClass(List listMembers,
                                    String packageName,
                                    JavaClass tstClass) {      
            fillGeneral(tstClass);

            /* Find and remove the current suite() method (if any): */
            Method suiteMethod = tstClass.getMethod("suite",            //NOI18N
                                                    Collections.EMPTY_LIST,
                                                    false);
            tstClass.getFeatures().remove(suiteMethod);

            /* Create a new suite() method: */
            suiteMethod = createSuiteMethod(tstClass, listMembers);
            tstClass.getFeatures().add(suiteMethod);
        }
        
        /**
         */
        private void fillGeneral(JavaClass tstClass) {
            tstClass.setSuperClassName(
                    tgtPkg.getMultipartId().createMultipartId(
                            JUNIT_SUPER_CLASS_NAME,     //name
                            null,                       //parent
                            Collections.EMPTY_LIST));   //type arguments
            tstClass.setModifiers(Modifier.PUBLIC);
            
            // remove default ctor, if exists (shouldn't throw exception)
            List/*<Type>*/ stringTypeList
                = Collections.singletonList(
                        tgtPkg.getType().resolve("java.lang.String"));  //NOI18N
            if (tstClass.getConstructor(stringTypeList, false) == null) {
                tstClass.getFeatures().add(
                       createTestConstructor(tstClass.getSimpleName()));
            }
            
            /* Add method setUp() (optionally): */
            if (generateSetUp
                    && !hasInitMethod(tstClass, METHOD_NAME_SETUP)) {
                tstClass.getFeatures().add(
                        createInitMethod(METHOD_NAME_SETUP));
            }
            
            /* Add method tearDown() (optionally): */
            if (generateTearDown
                    && !hasInitMethod(tstClass, METHOD_NAME_TEARDOWN)) {
                tstClass.getFeatures().add(
                        createInitMethod(METHOD_NAME_TEARDOWN));
            }
        }

        /**
         */
        private Constructor createTestConstructor(String className) {
            Constructor constr = tgtPkg.getConstructor().createConstructor(
                               className,               // name
                               Collections.EMPTY_LIST,  // annotations
                               Modifier.PUBLIC,         // modifiers
                               null,                    // Javadoc text
                               null,                    // Javadoc - object
                               null,                    // body - object
                               "super(testName);\n",    // body - text  //NOI18N
                               Collections.EMPTY_LIST,  // type parameters
                               createTestConstructorParams(),  // parameters
                               null);                   // exception names
            return constr;
        }

        /**
         */
        private List/*<Parameter>*/ createTestConstructorParams() {
            Parameter param = tgtPkg.getParameter().createParameter(
                                "testName",             // parameter name
                                Collections.EMPTY_LIST, // annotations
                                false,                  // not final
                                TestUtil.getTypeReference(   // type
                                        tgtPkg, "String"),              //NOI18N
                                0,                      // dimCount
                                false);                 // is not var.arg.
            return Collections.singletonList(param);
        }

        /**
         * Creates function <b>static public Test suite()</b> and fills its body,
         * appends all test functions in the class and creates sub-suites for
         * all test inner classes.
         */
        private Method createTestClassSuiteMethod(JavaClass tstClass) {
            StringBuffer body = new StringBuffer(1024);
            body.append("TestSuite suite = new TestSuite(");            //NOI18N
            body.append(tstClass.getSimpleName());
            body.append(".class);\n");                                  //NOI18N

            Collection innerClasses = TestUtil.filterFeatures(tstClass,
                                                              JavaClass.class);
            for (Iterator i = innerClasses.iterator(); i.hasNext(); ) {
                JavaClass jc = (JavaClass) i.next();
                if (TestUtil.isClassTest(jc)) {           //PENDING - look at it
                    body.append("suite.addTest(");                      //NOI18N
                    body.append(jc.getSimpleName());                    //NOI18N
                    body.append(".suite());\n");                        //NOI18N
                }
            }
            body.append("\nreturn suite;\n");                           //NOI18N

            // create header of function
            Method method = createPublicNoargMethod(
                                    null,               // no javadoc
                                    true,               // static
                                    "Test",             // ret. type
                                    "suite",            // method name  //NOI18N
                                    body.toString());   // method body
            return method;
        }

        /**
         */
        private Method createTestMethod(JavaClass srcClass, Method srcMethod) {
            String methodName = createTestMethodName(srcMethod.getName());
            String javadocText = generateMethodJavadoc
                                 ? generateJavadoc(srcClass, srcMethod)
                                 : null;
            String methodBody = generateMethodBody(srcClass, srcMethod);
            
            boolean throwsExceptions = false;
            List/*<JavaClass>*/ exceptions = srcMethod.getExceptions();
            if (!exceptions.isEmpty()) {
                Iterator/*<JavaClass>*/ i = exceptions.iterator();
                while (i.hasNext()) {
                    JavaClass exception = (JavaClass) i.next();
                    if (!exception.isSubTypeOf(getRuntimeException())) {
                        throwsExceptions = true;
                        break;
                    }
                }
            }

            List/*<MultipartId>*/ exceptNames = throwsExceptions
                               ? Collections.singletonList(
                                      tgtPkg.getMultipartId().createMultipartId(
                                              "Exception",              //NOI18N
                                              null,
                                              Collections.EMPTY_LIST))
                               : Collections.EMPTY_LIST;
            Method method = tgtPkg.getMethod().createMethod(
                            methodName,                 // name         //NOI18N
                            Collections.EMPTY_LIST,     // annotations
                            Modifier.PUBLIC,            // modifiers
                            javadocText,                // javadoc - text
                            null,                       // javadoc - object
                            null,                       // body - object
                            methodBody,                 // body - text
                            Collections.EMPTY_LIST,     // TypeParameters
                            Collections.EMPTY_LIST,     // Parameters
                            exceptNames,                // exception names
                            TestUtil.getTypeReference(  // return type
                                    tgtPkg, "void"),                    //NOI18N
                            0);                         // dimensions count
            return method;
        }
        
        /**
         */
        private Method createSuiteMethod(JavaClass tstClass,
                                         List/*<String>*/ members) {
            String methodName = "suite";                                //NOI18N
            String javadocText = generateSourceCodeHints
                                 ? NbBundle.getMessage(
                            TestCreator.class,
                            "TestCreator.suiteMethod.JavaDoc.comment")  //NOI18N
                                 : null;
            String methodBody = generateSuiteBody(tstClass, members);

            Method method = createPublicNoargMethod(javadocText,
                                                    true,           // static
                                                    "Test",         // ret. type
                                                    methodName,
                                                    methodBody);
            return method;
        }
        
        /**
         * Generates a set-up or a tear-down method.
         * The generated method will have no arguments, void return type
         * and a declaration that it may throw <code>java.lang.Exception</code>.
         * The method will have a declared protected member access.
         *
         * @param  methodName  name of the method to be created
         * @return  created method
         * @see  http://junit.sourceforge.net/javadoc/junit/framework/TestCase.html
         *       methods <code>setUp()</code> and <code>tearDown()</code>
         */
        private Method createInitMethod(String methodName) {
            Method method = tgtPkg.getMethod().createMethod(
                                methodName,             // name
                                Collections.EMPTY_LIST, // annotations
                                Modifier.PROTECTED,     // modifiers
                                null,                   // Javadoc text
                                null,                   // Javadoc object
                                null,                   // body object
                                "\n",                   // body text    //NOI18N
                                Collections.EMPTY_LIST, // type parameters
                                Collections.EMPTY_LIST, // parameters
                                Collections.singletonList( // exception names
                                        TestUtil.getTypeReference(
                                                tgtPkg, "Exception")),  //NOI18N
                                TestUtil.getTypeReference( // ret. type
                                                tgtPkg, "void"),        //NOI18N
                                0);                     // dimCount
            return method;
        }

        /**
         * Creates a public static <code>main(String[])</code> method
         * with the body taken from settings.
         *
         * @return  created <code>main(...)</code> method,
         *          or <code>null</code> if the method body would be empty
         */
        private Method createMainMethod() {
            String initialMainMethodBody = getInitialMainMethodBody();
            
            if (initialMainMethodBody.length() == 0) {
                return null;
            }
            
            String methodBody = '\n' + initialMainMethodBody + '\n';

            Type paramType = tgtPkg.getArray().resolveArray(
                                    TestUtil.getStringType(tgtPkg));
            Parameter param = tgtPkg.getParameter().createParameter(
                                    "argList",          // param. name  //NOI18N
                                    Collections.EMPTY_LIST, // annotations
                                    false,              // not final
                                    null,               // type name
                                    0,                  // dimCount
                                    false);             // not var. arg.
            param.setType(paramType);

            Method method = tgtPkg.getMethod().createMethod(
                                    "main",             // method name  //NOI18N
                                    Collections.EMPTY_LIST, // annotations
                                    Modifier.STATIC | Modifier.PUBLIC,
                                    null,               // javadoc text
                                    null,               // javadoc - object
                                    null,               // body - object
                                    methodBody,         // body - text
                                    Collections.EMPTY_LIST, // type params
                                    Collections.singletonList(param), // params
                                    Collections.EMPTY_LIST, // exceptions
                                    TestUtil.getTypeReference(tgtPkg,
                                                              "void"),  //NOI18N
                                    0);                 // dimension count
            return method;
        }
        
        /**
         */
        private Method createPublicNoargMethod(String javadocText,
                                               boolean isStatic,
                                               String retType,
                                               String methodName,
                                               String methodBody) {
            return tgtPkg.getMethod().createMethod(
                            methodName,                 // name         //NOI18N
                            Collections.EMPTY_LIST,     // annotations
                            isStatic ? Modifier.PUBLIC | Modifier.STATIC
                                     : Modifier.PUBLIC,
                            javadocText,                // javadoc - text
                            null,                       // javadoc - object
                            null,                       // body - object
                            methodBody,                 // body - text
                            Collections.EMPTY_LIST,     // TypeParameters
                            Collections.EMPTY_LIST,     // Parameters
                            Collections.EMPTY_LIST,     // exception names
                            TestUtil.getTypeReference(tgtPkg, retType),
                            0);                         // dimensions count
        }

        /**
         */
        private void createAbstractImpl(JavaClass srcClass,
                                        JavaClass tstClass) {
            String implClassName = srcClass.getSimpleName() + "Impl";   //NOI18N
            JavaClass innerClass = tstClass.getInnerClass(implClassName, false);

            if (innerClass == null) {
                String javadocText = 
                        generateMethodJavadoc
                        ? javadocText = NbBundle.getMessage(
                              TestCreator.class,
                              "TestCreator.abstracImpl.JavaDoc.comment",//NOI18N
                              srcClass.getName())
                        : null;

                // superclass
                MultipartId supClass
                        = tgtPkg.getMultipartId().createMultipartId(
                                srcClass.isInner() ? srcClass.getName()
                                                   : srcClass.getSimpleName(),
                                null,
                                Collections.EMPTY_LIST);

                innerClass = tgtPkg.getJavaClass().createJavaClass(
                                implClassName,          // class name
                                Collections.EMPTY_LIST, // annotations
                                Modifier.PRIVATE,       // modifiers
                                javadocText,            // Javadoc text
                                null,                   // Javadoc - object
                                Collections.EMPTY_LIST, // contents
                                null,                   // super class name
                                Collections.EMPTY_LIST, // interface names
                                Collections.EMPTY_LIST);// type parameters
                
                if (srcClass.isInterface()) {
                    innerClass.getInterfaceNames().add(supClass);
                } else {
                    innerClass.setSuperClassName(supClass);
                }

                createImpleConstructors(srcClass, innerClass);
                tstClass.getFeatures().add(innerClass);
            }

            // created dummy implementation for all abstract methods
            List abstractMethods = TestUtil.collectFeatures(
                                            srcClass,
                                            Method.class,
                                            Modifier.ABSTRACT,
                                            true);
            for (Iterator i = abstractMethods.iterator(); i.hasNext(); ) {
                Method oldMethod = (Method) i.next();
                if (innerClass.getMethod(
                        oldMethod.getName(),
                        TestUtil.getParameterTypes(oldMethod.getParameters()),
                        false) == null) {
                    Method newMethod = createMethodImpl(oldMethod);
                    innerClass.getFeatures().add(newMethod);
                }

            }
        }

        /**
         */
        private void createImpleConstructors(JavaClass srcClass,
                                             JavaClass tgtClass) {
            List constructors = TestUtil.filterFeatures(srcClass,
                                                        Constructor.class);
            for (Iterator i = constructors.iterator(); i.hasNext(); ) {
                Constructor ctr = (Constructor) i.next();
                
                if (Modifier.isPrivate(ctr.getModifiers())) {
                    continue;
                }
                
                Constructor nctr = tgtPkg.getConstructor().createConstructor();
                nctr.setBodyText("super("                               //NOI18N
                                 + getParameterString(ctr.getParameters())
                                 + ");\n");                             //NOI18N
                nctr.getParameters().addAll(
                        TestUtil.cloneParams(ctr.getParameters(), tgtPkg));
                tgtClass.getFeatures().add(nctr);
            }
        }

        /**
         */
        private Method createMethodImpl(Method origMethod)  {
            Method  newMethod = tgtPkg.getMethod().createMethod();

            newMethod.setName(origMethod.getName());

            /* Set modifiers of the method: */
            int mod = origMethod.getModifiers() & ~Modifier.ABSTRACT;
            if (((JavaClass) origMethod.getDeclaringClass()).isInterface()) {
                mod |= Modifier.PUBLIC;
            }
            newMethod.setModifiers(mod);

            // prepare the body of method implementation
            StringBuffer    body = new StringBuffer(200);
            if (generateSourceCodeHints) {
                body.append(NbBundle.getMessage(
                        TestCreator.class,
                        "TestCreator.methodImpl.bodyComment"));         //NOI18N
                body.append("\n\n");                                    //NOI18N
            }

            newMethod.setType(origMethod.getType());
            Type type = origMethod.getType();
            if (type != null) {
                String value = null;
                if ((type instanceof JavaClass) || (type instanceof Array)) {
                    value = "null";                                     //NOI18N
                } else if (type instanceof PrimitiveType) {
                    PrimitiveTypeKindEnum tke = (PrimitiveTypeKindEnum)
                                               ((PrimitiveType) type).getKind();
                    if (tke.equals(PrimitiveTypeKindEnum.BOOLEAN)) {
                        value = "false";                                //NOI18N
                    } else if (!tke.equals(PrimitiveTypeKindEnum.VOID)) {
                        value = "0";                                    //NOI18N
                    }
                }

                if (value != null) {
                    body.append("return ").append(value).append(";\n"); //NOI18N
                }
            }

            newMethod.setBodyText(body.toString());

            // parameters
            newMethod.getParameters().addAll(
                    TestUtil.cloneParams(origMethod.getParameters(), tgtPkg));

            return newMethod;
        }

        /**
         */
        private String generateJavadoc(JavaClass srcClass, Method srcMethod) {
            return NbBundle.getMessage(
                        TestCreator.class,
                        "TestCreator.variantMethods.JavaDoc.comment",   //NOI18N
                        srcMethod.getName(),
                        srcClass.getName());
        }
        
        /**
         */
        private String generateMethodBody(JavaClass srcClass, Method srcMethod){
            final boolean isStatic
                    = (srcMethod.getModifiers() & Modifier.STATIC) != 0;
            final String shortClsName = getTypeNameString(srcClass);
            
            StringBuffer newBody = new StringBuffer(512);
            
            boolean needsEmptyLine = false;

            if (generateDefMethodBody) {
                // generate default bodies, printing the name of method
                newBody.append("System.out.println(\"")                 //NOI18N
                       .append(srcMethod.getName())
                       .append("\");\n");                               //NOI18N
                needsEmptyLine = true;
            }

            if (needsEmptyLine) {
                newBody.append('\n');
                needsEmptyLine = false;
            }

            final List/*<Parameter>*/ params = srcMethod.getParameters();
            final String[] varNames = getTestSkeletonVarNames(params);

            Iterator i = params.iterator();
            for (int j = 0; j < varNames.length; j++) {
                Parameter param = ((Parameter) i.next());
                Type paramType = param.getType();
                String paramTypeName = getTypeNameString(paramType);
                newBody.append(paramTypeName).append(' ')
                       .append(varNames[j]).append(" = ")               //NOI18N
                       .append(getDefaultValue(paramType))
                       .append(";\n");                                  //NOI18N
            }
            assert !i.hasNext();
            needsEmptyLine |= (varNames.length != 0);

            if (!isStatic) {
                boolean hasDefConstr = false;

                Constructor constructor = srcClass.getConstructor(
                        Collections.EMPTY_LIST, false);
                if (constructor == null) {
                    /*
                     * No no-arguments constructor found. But if there is no
                     * constructor defined in the class, we can count with the
                     * automatically generated public no-argument constructor.
                     */
                    boolean constrFound = false;
                    Iterator/*<ClassMember>*/ j = srcClass.getContents()
                                                  .iterator();
                    while (j.hasNext()) {
                        if (j.next() instanceof Constructor) {
                            constrFound = true;
                            break;
                        }
                    }
                    hasDefConstr = !constrFound;
                } else {
                    hasDefConstr
                        = (constructor.getModifiers() & Modifier.PRIVATE) == 0;
                }

                newBody.append(shortClsName).append(' ')
                       .append(INSTANCE_VAR_NAME).append(" = ");        //NOI18N
                if (hasDefConstr) {
                   newBody.append("new ")                               //NOI18N
                          .append(shortClsName).append("()");           //NOI18N
                } else {
                   newBody.append("null");                              //NOI18N
                }
                newBody.append(";\n");                                  //NOI18N
                needsEmptyLine |= true;
            }   //if (isStatic)

            if (needsEmptyLine) {
                newBody.append('\n');
                needsEmptyLine = false;
            }

            final Type returnType = srcMethod.getType();
            String returnTypeName = getTypeNameString(returnType);
            final String defaultRetValue = getDefaultValue(returnType);
            final boolean isVoid = (defaultRetValue == null);

            if (!isVoid) {
                newBody.append(returnTypeName).append(' ')
                       .append(EXP_RESULT_VAR_NAME).append(" = ")       //NOI18N
                       .append(defaultRetValue)
                       .append(";\n");                                  //NOI18N
                newBody.append(returnTypeName).append(' ')
                       .append(RESULT_VAR_NAME).append(" = ");          //NOI18N
            }
            newBody.append(isStatic ? shortClsName : INSTANCE_VAR_NAME)
                   .append('.').append(srcMethod.getName()).append('(');
            if (varNames.length != 0) {
                newBody.append(varNames[0]);
                for (int j = 1; j < varNames.length; j++) {
                    newBody.append(", ").append(varNames[j]);           //NOI18N
                }
            }
            newBody.append(");\n");                                     //NOI18N
            if (!isVoid) {
                newBody.append("assertEquals(")                         //NOI18N
                       .append(EXP_RESULT_VAR_NAME)
                       .append(", ")                                    //NOI18N
                       .append(RESULT_VAR_NAME)
                       .append(");\n");                                 //NOI18N
            }
            needsEmptyLine = true;

            if (generateSourceCodeHints) {
                // generate comments to bodies
                if (needsEmptyLine) {
                    newBody.append('\n');
                    needsEmptyLine = false;
                }
                newBody.append(NbBundle.getMessage(
                           TestCreator.class,
                           "TestCreator.variantMethods.defaultComment"))//NOI18N
                       .append('\n');
            }
            
            if (generateDefMethodBody) {
                
                /* Generate a test failuare (in response to request 022): */
                if (needsEmptyLine) {
                    newBody.append('\n');
                    needsEmptyLine = false;
                }
                newBody.append(NbBundle.getMessage(
                           TestCreator.class,
                           "TestCreator.variantMethods.defaultBody"))   //NOI18N
                       .append('\n');
            }
            
            return newBody.toString();
        }
        
        /**
         */
        private String generateSuiteBody(JavaClass tstClass,
                                         List/*<String>*/ members) {
            StringBuffer body = new StringBuffer(512);
            
            //body.append("//" + GENERATED_SUITE_BLOCK_START + "\n");
            //body.append(NbBundle.getMessage(TestCreator.class,"TestCreator.suiteMethod.suiteBlock.comment")+"\n");
            body.append("TestSuite suite = new TestSuite(\"")           //NOI18N
                .append(tstClass.getSimpleName()).append("\");\n");     //NOI18N

            final TypeClass typeClass = tgtPkg.getType();
            for (Iterator/*<String>*/ i = members.iterator(); i.hasNext(); ) {
                String name = (String) i.next();

                Type type = (Type) typeClass.resolve(name);
                if (type instanceof ClassDefinition) {
                    Method suiteMethod = ((ClassDefinition) type).getMethod(
                                                    "suite",            //NOI18N
                                                    Collections.EMPTY_LIST,
                                                    true);
                    if ((suiteMethod != null)
                            && Modifier.isStatic(suiteMethod.getModifiers())) {
                        body.append("suite.addTest(")                   //NOI18N
                            .append(name).append(".suite());\n");       //NOI18N
                    }
                }
            }

            body.append("return suite;\n");                             //NOI18N
            //body.append("//" + GENERATED_SUITE_BLOCK_END + "\n");
            
            return body.toString();
        }

        /**
         * Returns the shortest usable name of the given type.
         * Name of the type is shortened if at least one of the following
         * conditions is met:
         * <ul>
         *     <li>the type's full name starts with <code>java.lang.</code>
         *     <li>the type is from the same package as the generated
         *         test class</li>
         *     <li>the type is imported in the source code of the test
         *         class</li>
         *     <li>the type represents an inner class and name of one of its
         *         containing classes may be shortened</li>
         * </ul>
         * <p>
         * <em>Examples:</em>
         * </p><p>
         * If the type is <code>foo.bar.Baz</code> and the source code contains
         * import statement <code>import foo.bar.*</code>, the returned name
         * will be <code>Baz</code>.
         * </p><p>
         * If the type is <code>foo.bar.Baz.Boo</code> and the source code
         * contains import statement <code>import foo.bar.Baz</code>, the
         * returned name will be <code>Baz.Boo</code>.
         *
         * @param  type  type whose name needs to be returned
         * @return  shortened name of the given type, or a full name
         *          if the full name is necessary
         */
        private String getTypeNameString(Type type) {
            if (!(type instanceof ClassDefinition)) {     //e.g. primitive types
                return type.getName();
            }
            if (type instanceof Array) {
                return getTypeNameString(((Array) type).getType())
                       + "[]";                                          //NOI18N
            }
            if (type instanceof ParameterizedType) {
                ParameterizedType paramzedType = (ParameterizedType) type;
                String defTypeName
                        = getTypeNameString(paramzedType.getDefinition());
                
                List/*<Type>*/ typeParams = paramzedType.getParameters();
                if (typeParams.isEmpty()) {
                    return defTypeName;
                }

                StringBuffer buf = new StringBuffer(60);
                buf.append(defTypeName).append('<');

                buf.append(getTypeNameString((Type) typeParams.get(0)));
                if (typeParams.size() > 1) {
                    Iterator/*<Type>*/ i = typeParams.iterator();
                    i.next();                           //skip the first element
                    do {
                        buf.append(", ");                               //NOI18N
                        buf.append(getTypeNameString((Type) i.next()));
                    } while (i.hasNext());
                }

                buf.append('>');
                return buf.toString();
            }
            if (type instanceof AnnotationType) {
                //PENDING:
                return type.getName();
            }
            if (!(type instanceof JavaClass)) {
                return type.getName();       //handle unknown Type subinterfaces
            }

            String fullClsName = type.getName();
            
            if (!javaIdFullPattern.matcher(fullClsName).matches()) {
                return type.getName();       //handle unknown Type subinterfaces
            }

            return getJavaClassTypeNameString(
                        (JavaClass) type,
                        fullClsName,
                        fullClsName.startsWith("java.lang."),           //NOI18N
                        fullClsName.startsWith(tstPkgNameDot));
        }

        /**
         */
        private String getJavaClassTypeNameString(JavaClass clsType,
                                                  String fullClsName,
                                                  boolean maybeFromJavaLang,
                                                  boolean maybeFromThisPkg) {
            if (fullClsName == null) {
                fullClsName = clsType.getName();
            }

            String result;

            result = (String) clsNames.get(fullClsName);
            if (result != null) {
                return result;
            }

            String simpleName = clsType.getSimpleName();
            if ((maybeFromThisPkg
                            && checkIsFromThisPkg(simpleName, fullClsName))
                    || (maybeFromJavaLang
                            && checkIsFromJavaLang(simpleName, fullClsName))
                    || checkIsImported(clsType)) {
                result = simpleName;
            } else if (clsType.isInner()) {
                ClassDefinition declaringCls = clsType.getDeclaringClass();
                if (declaringCls instanceof JavaClass) {
                    result = getJavaClassTypeNameString(
                                         (JavaClass) declaringCls,
                                         (String) null,         //full cls. name
                                         maybeFromJavaLang,
                                         maybeFromThisPkg)
                             + '.' + simpleName;
                } else {
                    result = fullClsName;
                }
            } else {
                result = fullClsName;
            }

            clsNames.put(fullClsName, result);
            return result;
        }
        
        /**
         */
        private boolean checkIsFromThisPkg(String shortName, String fullName) {
            return fullName.length()
                                == tstPkgNameDot.length() + shortName.length();
        }
        
        /**
         */
        private boolean checkIsFromJavaLang(String shortName, String fullName) {
            return fullName.length() == 10 + shortName.length();
        }
        
        /**
         */
        private boolean checkIsImported(JavaClass clsType) {
            if (simpleImports == null) {
                prepareImports();
            }
            return simpleImports.contains(clsType)
                   || importedClsTypes.contains(clsType);
        }
        
        private Collection/*<NamedElement>*/ simpleImports;
        private Collection/*<JavaClass>*/ importedClsTypes;
        
        /**
         */
        private void prepareImports() {
            simpleImports = new ArrayList/*<Import>*/(tstImports.size());
            importedClsTypes = new ArrayList/*<JavaClass>*/(20);
            for (Iterator/*<Import>*/ i = tstImports.iterator(); i.hasNext();) {
                Import imp = (Import) i.next();
                if (imp.isStatic()) {
                    continue;
                }
                if (!imp.isOnDemand()) {
                    simpleImports.add(imp.getImportedNamespace());
                } else {
                    Collection/*<NamedElement>*/ importedElems
                            = imp.getImportedElements();
                    Iterator/*<NamedElement>*/ j = importedElems.iterator();
                    while (j.hasNext()) {
                        Object o = j.next();
                        if (o instanceof JavaClass) {
                            importedClsTypes.add(/*(JavaClass)*/o);
                        }
                    }
                }
            }
            
            assert simpleImports instanceof ArrayList;
            if (simpleImports.isEmpty()) {
                simpleImports = Collections.EMPTY_LIST;
            } else {
                ((ArrayList) simpleImports).trimToSize();
            }
            
            assert importedClsTypes instanceof ArrayList;
            if (importedClsTypes.isEmpty()) {
                importedClsTypes = Collections.EMPTY_LIST;
            } else {
                ((ArrayList) importedClsTypes).trimToSize();
            }
        }
        
        
        /** */
        private ClassDefinition runtimeException;
        
        /**
         */
        private ClassDefinition getRuntimeException() {
            if (runtimeException == null) {
                Type runtimeExcType = tgtPkg.getType().resolve(
                                          "java.lang.RuntimeException");//NOI18N
                runtimeException = (ClassDefinition) runtimeExcType;
            }
            assert runtimeException != null;
            return runtimeException;
        }

    }
        
    /**
     */
    public void createTestClass(Resource srcRc, JavaClass srcClass,
                                Resource tgtRc, JavaClass tgtClass) {
        // public entry points are wrapped in MDR transactions
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            new SingleResourceTestCreator(srcRc, tgtRc)
                    .createTest(srcClass, tgtClass);
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }
    
    /**
     */
    public void createTestSuite(List listMembers,
                                String packageName,
                                JavaClass tgtClass) {
        // public entry points are wrapped in MDR transactions
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            new SingleResourceTestCreator(null, tgtClass.getResource())
                    .createTestSuite(listMembers, packageName, tgtClass);
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }
    
    /**
     */
    public void createEmptyTest(Resource tstRc, JavaClass tstClass) {
        // public entry points are wrapped in MDR transactions
        JavaModel.getJavaRepository().beginTrans(true);
        try {   
            new SingleResourceTestCreator(null, tstRc)
                    .createEmptyTest(tstClass);
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
        
        if (generateSourceCodeHints) {
            /*
             * The comment must be added after the end of the previous
             * JMI transaction in order to be able to correctly acquire
             * source code offsets. The comment is generated
             * at the end of the class passed in in the parameter.
             */
            JavaModel.getJavaRepository().beginTrans(true);
            try {   
                addClassBodyComment(tstClass);
            } finally {
                JavaModel.getJavaRepository().endTrans();
            }
        }
    }
    
    /**
     * Checks whether the given class or at least one of its nested classes
     * is testable.
     *
     * @param  jc  class to be checked
     * @return  TesteableResult that isOk, if the class is testeable or carries
     *          the information why the class is not testeable
     */
    TesteableResult isClassTestable(JavaClass jc) {
        assert jc != null;
        
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            
            TesteableResult result = TesteableResult.OK;
            
            /*
             * If the class is a test class and test classes should be skipped,
             * do not check nested classes (skip all):
             */
            /* Check if the class itself (w/o nested classes) is testable: */
            final int modifiers = jc.getModifiers();

            if (skipTestClasses && TestUtil.isClassImplementingTestInterface(jc)) 
                result = TesteableResult.combine(result, TesteableResult.TEST_CLASS);
            if (skipPkgPrivateClasses && !Modifier.isPublic(modifiers) && !Modifier.isPrivate(modifiers))
                result = TesteableResult.combine(result, TesteableResult.PACKAGE_PRIVATE_CLASS);
            if (skipAbstractClasses && Modifier.isAbstract(modifiers))
                result = TesteableResult.combine(result, TesteableResult.ABSTRACT_CLASS);
            if (!Modifier.isStatic(modifiers) && jc.isInner())
                result = TesteableResult.combine(result, TesteableResult.NONSTATIC_INNER_CLASS);
            if (!hasTestableMethods(jc))
                result = TesteableResult.combine(result, TesteableResult.NO_TESTEABLE_METHODS);
            if (skipExceptionClasses && TestUtil.isClassException(jc)) 
                result = TesteableResult.combine(result, TesteableResult.EXCEPTION_CLASS);
            
            
            /* Not testeable. But maybe one of its nested classes is testable: */
            if (result.isFailed()) {
                Iterator it  = TestUtil.collectFeatures(jc, JavaClass.class, 0, true).iterator();
                while (it.hasNext()) {
                    if (isClassTestable((JavaClass)it.next()).isTesteable()) 
                        return TesteableResult.OK;
                }
            }
            return result;
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }
    
    
    /* private methods */
    
    /**
     * Returns true if tgtClass contains suite() method
     */
    private static boolean hasSuiteMethod(JavaClass tgtClass) {
        return tgtClass.getMethod("suite", Collections.EMPTY_LIST, false)!= null;
    }
    
    /**
     */
    private static String createTestMethodName(String smName) {
        return "test" + smName.substring(0,1).toUpperCase() + smName.substring(1);
    }
    
    /**
     */
    private static String getDefaultValue(final Type varType) {
        final String varTypeName = varType.getName();
        
        if (varTypeName.equals("void")) {                               //NOI18N
            return null;
        } else if (varTypeName.equals("int")) {                         //NOI18N
            return "0";                                                 //NOI18N
        } else if (varTypeName.equals("float")) {                       //NOI18N
            return "0.0F";                                              //NOI18N
        } else if (varTypeName.equals("long")) {                        //NOI18N
            return "0L";                                                //NOI18N
        } else if (varTypeName.equals("double")) {                      //NOI18N
            return "0.0";                                               //NOI18N
        } else if (varTypeName.equals("boolean")) {                     //NOI18N
            return "true";                                              //NOI18N
        } else if (varTypeName.equals("java.lang.String")) {            //NOI18N
            return "\"\"";                                              //NOI18N
        } else if (varTypeName.equals("short")) {                       //NOI18N
            return "0";                                                 //NOI18N
        } else if (varTypeName.equals("byte")) {                        //NOI18N
            return "0";                                                 //NOI18N
        } else if (varTypeName.equals("char")) {                        //NOI18N
            return "' '";                                               //NOI18N
        } else {
            assert !(varType instanceof PrimitiveType);
            return "null";                                              //NOI18N
        }
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
    private static String[] getTestSkeletonVarNames(
            final List/*<TypeParameter>*/ sourceMethodParams) {
        
        /* Handle the trivial case: */
        if (sourceMethodParams.isEmpty()) {
            return new String[0];
        }
        
        final int count = sourceMethodParams.size();
        String[] varNames = new String[count];
        boolean[] conflicts = new boolean[count];
        boolean issueFound = false;
        
        HashSet varNamesSet = new HashSet((int) ((count + 2) * 1.4));
        varNamesSet.add(INSTANCE_VAR_NAME);
        varNamesSet.add(RESULT_VAR_NAME);
        varNamesSet.add(EXP_RESULT_VAR_NAME);
        
        Iterator it = sourceMethodParams.iterator();
        for (int i = 0; i < count; i++) {
            String paramName = ((Parameter) it.next()).getName();
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
     * Detects whether a given class contains a no-argument method of a given
     * name, having protected or public member access.
     *
     * @param  cls  class the method is to be found in
     * @param  methodName  name of the method to be found
     * @return  <code>true</code> if the class contains such a method,
     *          <code>false</code> otherwise
     */
    private static boolean hasInitMethod(JavaClass cls,
                                         String methodName) {
        return cls.getMethod(methodName,
                             Collections.EMPTY_LIST,
                             false) != null;
    }
    
    /**
     */
    private static String getParameterString(List params) {
        StringBuffer paramString = new StringBuffer();
        
        Iterator it = params.iterator();
        while (it.hasNext()) {
            Parameter param= (Parameter)it.next();
            if (paramString.length() > 0) {
                paramString.append(", ");
            }
            paramString.append(param.getName());
        }
        
        return paramString.toString();
    }
    
    /**
     *
     * @param cls JavaClass to generate the comment to.
     */
    private static void addClassBodyComment(JavaClass cls) {
        int off = cls.getEndOffset() - 1;        
        String theComment1 = NbBundle.getMessage(TestCreator.class,
                                                 CLASS_COMMENT_LINE1);
        String theComment2 = NbBundle.getMessage(TestCreator.class,
                                                 CLASS_COMMENT_LINE2);
        String indent = getIndentString();
        DiffElement diff = new DiffElement(
                off,
                off,
                indent + theComment1 + '\n'
                + indent + theComment2 + '\n' + '\n');
        ((ResourceImpl) cls.getResource()).addExtDiff(diff);
    }

    /**
     */
    private static String getIndentString() {
        int spt = org.netbeans.modules.javacore.jmiimpl.javamodel.MetadataElement.getIndentSpace(); // spaces per tab
        String tabString;
        if (org.netbeans.modules.javacore.jmiimpl.javamodel.MetadataElement.isExpandTab()) {
            char [] arr = new char[spt];
            Arrays.fill(arr, ' ');
            tabString = new String(arr);
        } else
            tabString = "\t";
        
        return tabString;
    }

    
    /**
     */
    private boolean hasTestableMethods(JavaClass cls) {
        
        Iterator methods = TestUtil.collectFeatures(cls, Method.class, 0, true).iterator();
        while (methods.hasNext()) {
            if (isMethodAcceptable((Method)methods.next()))
                return true;
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
    private boolean isMethodAcceptable(Method m) {
        final int modifiers = m.getModifiers();
        return ((modifiers & methodAccessModifiers) != 0)
            || (testPkgPrivateMethods && ((modifiers & ACCESS_MODIFIERS) == 0));
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
     * Helper class representing reasons for skipping a class in the test 
     * generation process. The class enumerates known reasons, why a class may 
     * not be considered testeable, allows to combine the reasons and provide 
     * human-readable representation  of them.
     **/
    public static final class TesteableResult {
        // bitfield of reasons for skipping a class
        private long reason;
        
        // reason constants
        public static final TesteableResult OK = new TesteableResult(0);
        public static final TesteableResult PACKAGE_PRIVATE_CLASS = new TesteableResult(1);
        public static final TesteableResult NO_TESTEABLE_METHODS = new TesteableResult(2);
        public static final TesteableResult TEST_CLASS = new TesteableResult(4);
        public static final TesteableResult ABSTRACT_CLASS = new TesteableResult(8);
        public static final TesteableResult NONSTATIC_INNER_CLASS = new TesteableResult(16);
        public static final TesteableResult EXCEPTION_CLASS = new TesteableResult(32);


        // bundle keys for reason descriptions
        private static final String [] reasonBundleKeys = {
            "TesteableResult_PkgPrivate", 
            "TesteableResult_NoTesteableMethods",
            "TesteableResult_TestClass",
            "TesteableResult_AbstractClass",
            "TesteableResult_NonstaticInnerClass",
            "TesteableResult_ExceptionClass"};
        
        private TesteableResult(long reason) {
            this.reason = reason;
        }
        
        /**
         * Combine two result reasons into a new one.
         *
         * The combination is the union
         * of the failure reasons represented by the two results. Thus,
         * if both are success (no failure), the combination is a success. If 
         * some of them is failed, the result is failed.
         *
         * @param lhs the first TesteableResult
         * @param rhs the second TesteableResult
         * @return a new TesteableResult representing the combination of the two 
         *         results
         **/
        public static TesteableResult combine(TesteableResult lhs, TesteableResult rhs) {
            return new TesteableResult(lhs.reason | rhs.reason);
        }

        /**
         * Returns true if the result is for a testable class.
         * @return true or false
         */
        public boolean isTesteable() { return reason == 0;}
        
        /**
         * Returns true if the result is for a non-testeable class.
         * @return true if the result is for a non-testeable class.
         */
        public boolean isFailed() { return reason != 0;}
        
        /**
         * Returns a human-readable representation of the reason. If the reason 
         * is a combination of multiple reasons, they are separated with ",".
         * @return String
         */
        public String getReason() { return getReason(", ", ", ");}

        /**
         * Returns {@link #getReason()}.
         * @return String
         */
        public String toString() { 
            return getReason(", ", ", ");
        }
        
        /** 
         * Returns a human-readable representation of the reason. If the reason 
         * is a combination of multiple reasons, they are separated with 
         * <code>separ</code> except for the last reason, which is separated 
         * with <code>terminalSepar</code>
         * <p>
         * For example: getReason(", ", " or ") might return 
         * "abstract, package private or without testeable methods".
         *
         * @return String
         */
        
        public String getReason(String separ, String terminalSepar) {
            try {
                ResourceBundle bundle = NbBundle.getBundle(TestCreator.class);
                if (reason == 0) { return bundle.getString("TesteableResult_OK"); }
                
                else {
                    String str = "";
                    boolean lastPrep = true;
                    for (long i = 0, r = reason; r>0;r >>= 1, i++) {
                        if ((r & 1) != 0) {
                            if (str.length()>0) 
                                if (lastPrep) {
                                    str = terminalSepar + str;
                                    lastPrep = false;
                                } else 
                                    str = separ + str;
                            str = bundle.getString(reasonBundleKeys[(int)i]) + str;
                        }
                    } 
                    return str;
                }
            } catch (MissingResourceException ex) {
                ErrorManager.getDefault().notify(ex);
                return "";
            }
        }
    }
    
}
