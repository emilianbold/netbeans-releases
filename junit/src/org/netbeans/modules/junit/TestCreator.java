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
/*
 * TestCreator.java
 *
 * Created on January 19, 2001, 1:02 PM
 */

package org.netbeans.modules.junit;

import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;
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
    
    static private final String GENERATED_SUITE_BLOCK_START                = "--JUNIT:";
    static private final String GENERATED_SUITE_BLOCK_END                  = ":JUNIT--";
    static private final String METHOD_NAME_SETUP = "setUp";            //NOI18N
    static private final String METHOD_NAME_TEARDOWN = "tearDown";      //NOI18N
    static private final String CLASS_COMMENT_LINE1 = "TestCreator.javaClass.addTestsHereComment.l1";
    static private final String CLASS_COMMENT_LINE2 = "TestCreator.javaClass.addTestsHereComment.l2";
    
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
    
    
    private static String arrayToString(Object[] array) {
        String result=array.getClass().getName()+":";
        for (int i=0; i<array.length; i++) {
            result+=array[i]+" ";
        }
        return result;
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
    
    
    public void createTestClass(Resource srcRc, JavaClass srcClass,
                                Resource tgtRc, JavaClass tgtClass) {
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            JavaModelPackage tgtPackage = (JavaModelPackage)tgtRc.refImmediatePackage();
            
            tgtRc.setPackageName(srcRc.getPackageName());
            
            // add imports from the source but only those that are not
            // already present
            List srcImports = srcRc.getImports();
            List tgtImports = tgtRc.getImports();
            
            // use hashtable for faster access
            HashSet tImpSet = new HashSet(tgtImports.size());
            Iterator it = tgtImports.iterator();
            while (it.hasNext()) {
                tImpSet.add(new ImpEq((org.netbeans.jmi.javamodel.Import)it.next()));
            }
            
            
            // import for junit.framework.*
            addFrameworkImport(tgtRc);
            
            // all other imports if not present, yet
            Iterator simpit = srcImports.iterator();
            while (simpit.hasNext()) {
                org.netbeans.jmi.javamodel.Import imp =
                    (org.netbeans.jmi.javamodel.Import)simpit.next();
                if (!tImpSet.contains(new ImpEq(imp))) {
                    tgtImports.add(tgtPackage.getImport().
                                   createImport(imp.getName(),
                                                null,
                                                imp.isStatic(),
                                                imp.isOnDemand()));
                }
            }
            
            
            // construct/update test class from the source class
            fillTestClass(srcRc, srcClass, tgtRc, tgtClass);
            
            // if aplicable, add main method (method checks options itself)
            addMainMethod(tgtClass);
            
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }
    
    private static Import createFrameworkImport(JavaModelPackage pkg) {
        return pkg.getImport().createImport(JUNIT_FRAMEWORK_PACKAGE_NAME,null, false, true);
    }
    
    public void createTestSuite(List listMembers,
                                String packageName,
                                JavaClass tgtClass) {
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            
            Resource   tgtRes = tgtClass.getResource();
            tgtRes.setPackageName(packageName);
            
            addFrameworkImport(tgtRes);      
            // construct/update test class from the source class
            fillSuiteClass(listMembers, packageName, tgtClass);
            
            // if aplicable, add main method (method checks options itself)
            addMainMethod(tgtClass);
            
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }
    
    public static void addFrameworkImport(Resource tgtRes){
        JavaModelPackage pkg = (JavaModelPackage)tgtRes.refImmediatePackage();
        
        // look for the import among all imports in the target file
        Iterator ti_it = tgtRes.getImports().iterator();
        boolean found = false;
        while (ti_it.hasNext()) {
            Import i = (Import)ti_it.next();
            if (i.getName().equals(JUNIT_FRAMEWORK_PACKAGE_NAME) &&
                i.isStatic() == false &&
                i.isOnDemand() == true) { found = true; break;}
        }

        if (!found) // not found
            tgtRes.getImports().add(createFrameworkImport(pkg));
        
    }
    
    /**
     * Checks whether the given class or at least one of its nested classes
     * is testable.
     *
     * @param  jc  class to be checked
     * @return  TesteableResult that isOk, if the class is testeable or carries
     *          the information why the class is not testeable
     */
    public TesteableResult isClassTestable(JavaClass jc) {
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
    static private boolean hasSuiteMethod(JavaClass tgtClass) {
        return tgtClass.getMethod("suite", Collections.EMPTY_LIST, false)!= null;
    }
    
    private static Method createSuiteMethod(JavaModelPackage pkg, String javadocText, String bodyText) {
        Method ret = pkg.getMethod().createMethod("suite", 
                Collections.EMPTY_LIST,
                Modifier.STATIC | Modifier.PUBLIC,
                javadocText, null, null, bodyText, 
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                TestUtil.getTypeReference(pkg, "Test"),
                0);
        return ret;
    }
    
    /**
     * Creates function <b>static public Test suite()</b> and fills its body,
     * appends all test functions in the class and creates sub-suites for
     * all test inner classes.
     */
    static private Method createTestClassSuiteMethod(JavaClass tgtClass) {
        
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        StringBuffer body = new StringBuffer(1024);
        body.append("TestSuite suite = new TestSuite(");                //NOI18N
        body.append(tgtClass.getSimpleName());
        body.append(".class);\n");
      
        Collection innerClasses = TestUtil.filterFeatures(tgtClass, JavaClass.class);
        Iterator itic = innerClasses.iterator();
        while (itic.hasNext()) {
            JavaClass jc = (JavaClass)itic.next();
            if (TestUtil.isClassTest(jc)) {
                body.append("suite.addTest(");
                body.append(jc.getSimpleName());
                body.append(".suite());\n");
            }
        }
        
        body.append("\nreturn suite;\n");

        // create header of function
        Method method = createSuiteMethod(pkg, null, body.toString());

        return method;
    }
    
    static private List createTestConstructorParams(JavaModelPackage pkg) {
        Parameter param = pkg.getParameter().
            createParameter("testName",
                            Collections.EMPTY_LIST, // annotations
                            false, // isFinal
                            pkg.getMultipartId().createMultipartId("java.lang.String", null, Collections.EMPTY_LIST),// typeName
                            0, // dimCount
                            false); // isvararg
        return Collections.singletonList(param);
    }
    
    
    static private Constructor createTestConstructor(JavaModelPackage pkg, String className) {
        Constructor constr = pkg.getConstructor()
            .createConstructor(
                               className, // name
                               Collections.EMPTY_LIST, // annotations
                               Modifier.PUBLIC, // modifiers
                               null, // javadoc text
                               null, // javadoc - object repre
                               null, // body - object repre
                               "\nsuper(testName);\n", // body -
                               // string repre
                               Collections.EMPTY_LIST,// type parameters
                               createTestConstructorParams(pkg),// parameters
                               null); // exception names
        return constr;
    }
    
    static private List createTestMethodParams(Method sm, JavaModelPackage pkg) {
        return Collections.EMPTY_LIST;
    }
    
    static private String createTestMethodName(String smName) {
        return "test" + smName.substring(0,1).toUpperCase() + smName.substring(1);
    }
    
    private Method createTestMethod(JavaClass sclass, Method sm, JavaModelPackage pkg) {
        
        String smName = sm.getName();
        
        // method name
        String newName = createTestMethodName(smName);
        
        List annotations = Collections.EMPTY_LIST;
        int modifiers = Modifier.PUBLIC;
        
        // javadoc
        String javadocText =
            generateMethodJavadoc ?
            MessageFormat.format(NbBundle.getMessage(TestCreator.class,
                                                     "TestCreator.variantMethods.JavaDoc.comment"),
                                 new Object[] {smName, sclass.getName()})
            : null;
            
        // create body of the method
        StringBuffer newBody = new StringBuffer(512);
        if (generateDefMethodBody) {
            // generate default bodies, printing the name of method
            newBody.append("System.out.println(\"" + newName + "\");\n");
        }
        if (generateSourceCodeHints) {
            // generate comments to bodies
            newBody.append("\n"+NbBundle.getMessage(TestCreator.class,"TestCreator.variantMethods.defaultComment")+"\n");
        }
        if (generateDefMethodBody) {
            // generate a test failuare by default (in response to request 022).
            newBody.append(NbBundle.getMessage(TestCreator.class,"TestCreator.variantMethods.defaultBody")+"\n");
        }
            
        // return type
        TypeReference typeName = pkg.getMultipartId().createMultipartId("void", null, Collections.EMPTY_LIST);
            
        // method parameters
        List params = createTestMethodParams(sm, pkg);
            
        Method ret = pkg.getMethod().createMethod(newName,
                                                  annotations,
                                                  modifiers,
                                                  javadocText,
                                                  null, // javadoc
                                                  null, // body
                                                  newBody.toString(),
                                                  Collections.EMPTY_LIST, // type parameters
                                                  params,
                                                  Collections.EMPTY_LIST, // exceptions
                                                  typeName,
                                                  0);
        return ret;
    }
    
    
    
    
    private boolean hasTestableMethods(JavaClass cls) {
        
        Iterator methods = TestUtil.collectFeatures(cls, Method.class, 0, true).iterator();
        while (methods.hasNext()) {
            if (isMethodAcceptable((Method)methods.next()))
                return true;
        }
        
        return false;
    }
    
    
    
    public void fillGeneral(JavaClass testClass) {
        // public entry points are wrapped in MDR transactions
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            
            JavaModelPackage pkg = (JavaModelPackage)testClass.refImmediatePackage();
            
            testClass.setSuperClassName(pkg.getMultipartId().createMultipartId(JUNIT_SUPER_CLASS_NAME, null, Collections.EMPTY_LIST));
            testClass.setModifiers(Modifier.PUBLIC);
            
            // remove default ctor, if exists (shouldn't throw exception)
            if (null == testClass.getConstructor(Collections.singletonList(createStringType(pkg)), false)) {
                //fill classe's constructor
                Constructor newConstr = createTestConstructor(pkg, testClass.getSimpleName());
                testClass.getFeatures().add(newConstr);
            }
            
            
            //add method setUp() (optionally):
            if (generateSetUp
                && !hasInitMethod(testClass, METHOD_NAME_SETUP)) {
                
                testClass.getFeatures().add(generateInitMethod(pkg, METHOD_NAME_SETUP));
            }
            
            //add method tearDown() (optionally):
            if (generateTearDown
                && !hasInitMethod(testClass, METHOD_NAME_TEARDOWN)) {
                testClass.getFeatures().add(generateInitMethod(pkg, METHOD_NAME_TEARDOWN));
            }
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
        
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
    private static Method generateInitMethod(JavaModelPackage pkg, String methodName) {
        Method method = pkg.getMethod().
            createMethod(methodName, // name
                         Collections.EMPTY_LIST, // annotations
                         Modifier.PROTECTED, // modifiers
                         null, // javadoc text
                         null, // javadoc object
                         null, // body object
                         "\n", // body text
                         Collections.EMPTY_LIST, // type parameters
                         Collections.EMPTY_LIST, // parameters
                         Collections.singletonList(pkg.getMultipartId().createMultipartId("Exception", null, Collections.EMPTY_LIST)), // exception names
                         pkg.getMultipartId().createMultipartId("void", null, Collections.EMPTY_LIST), // typeName
                         0 // dimCount
                         );
        return method;
    }
    
    
    
    
    private void fillTestClass(Resource srcRc, 
                               JavaClass srcClass,
                               Resource tgtRc,
                               JavaClass tgtClass) {
        fillGeneral(tgtClass);
        
        List    innerClasses = TestUtil.filterFeatures(srcClass, JavaClass.class);
        
        // create test classes for inner classes
        Iterator itInner = innerClasses.iterator();
        while (itInner.hasNext()) {
            JavaClass theClass = (JavaClass)itInner.next();
            JavaModelPackage pkg = ((JavaModelPackage)tgtClass.refImmediatePackage());
            
            if (isClassTestable(theClass).isTesteable()) {
                // create new test class
                JavaClass innerTester;
                String    name = TestUtil.getTestClassName(theClass.getSimpleName());
                
                if (null == (innerTester = TestUtil.getClassBySimpleName(tgtClass, name))) {
                    
                    innerTester = pkg.getJavaClass().createJavaClass();
                    innerTester.setSimpleName(tgtClass.getName()+"."+name);
                    tgtClass.getFeatures().add(innerTester);
                }
                
                // process tested inner class the same way like top-level class
                fillTestClass(srcRc, theClass, tgtRc, innerTester);
                
                // do additional things for test class to became inner class usable for testing in JUnit
                innerTester.setModifiers(innerTester.getModifiers() | Modifier.STATIC);
                
            }
        }
        
        // add suite method ... only if we are supposed to do so
        
        if (generateSuiteClasses && !hasSuiteMethod(tgtClass)) {
            tgtClass.getFeatures().add(createTestClassSuiteMethod(tgtClass));
        }
        
        
        
        // fill methods according to the iface of tested class
        Iterator methit = TestUtil.filterFeatures(srcClass, Method.class).iterator();
        while (methit.hasNext()) {
            Method sm = (Method)methit.next();
            if (isMethodAcceptable(sm) &&
                tgtClass.getMethod(createTestMethodName(sm.getName()),
                                   createTestMethodParams(sm, (JavaModelPackage)tgtClass.refImmediatePackage()),
                                   false) == null) {
                Method tm = createTestMethod(srcClass, sm, (JavaModelPackage)tgtClass.refImmediatePackage());
                tgtClass.getFeatures().add(tm);
            }
            
        }
        
        
        // create abstract class implementation
        if (!skipAbstractClasses
                && (Modifier.isAbstract(srcClass.getModifiers())
                    || srcClass.isInterface())) {
            createAbstractImpl(srcClass, tgtClass);
        }
        
    }
    
    
    private void fillSuiteClass(List listMembers,
                                       String packageName,
                                       JavaClass tgtClass)  
    {      
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        fillGeneral(tgtClass);
        
        // find "suite()" method
        Method suiteMethod = tgtClass.getMethod("suite", Collections.EMPTY_LIST, false);
        tgtClass.getFeatures().remove(suiteMethod);
        
        String javadocText = generateSourceCodeHints ? NbBundle.getMessage(TestCreator.class,"TestCreator.suiteMethod.JavaDoc.comment") : null;

        StringBuffer newBody = new StringBuffer();
        generateSuiteBody(pkg, tgtClass.getSimpleName(), newBody, listMembers, true);
        String bodyText = newBody.toString();

        suiteMethod = createSuiteMethod(pkg, javadocText, bodyText);
        
        tgtClass.getFeatures().add(suiteMethod);
        
    }
    
    
    
    
    static private void generateSuiteBody(JavaModelPackage pkg, String testName, StringBuffer body, List members, boolean alreadyExists) {
        Iterator    li;
        String      name;
        
        
        //body.append("//" + GENERATED_SUITE_BLOCK_START + "\n");
        //body.append(NbBundle.getMessage(TestCreator.class,"TestCreator.suiteMethod.suiteBlock.comment")+"\n");
        body.append("TestSuite suite = new TestSuite(\"" + testName + "\");\n");
        
        li = members.listIterator();
        TypeClass typeClass = pkg.getType();
        
        while (li.hasNext()) {
            name = (String) li.next();
            
            Type ty = (Type)typeClass.resolve(name);
            if (ty instanceof ClassDefinition) {
                Method suiteMethod = ((ClassDefinition)ty).getMethod("suite", Collections.EMPTY_LIST, true);
                if (suiteMethod != null &&
                    ((suiteMethod.getModifiers() & Modifier.STATIC) == Modifier.STATIC))
                    body.append("suite.addTest(" + name + ".suite());\n");
            }
        }
        
        body.append("return suite;\n");
        //body.append("//" + GENERATED_SUITE_BLOCK_END + "\n");
        
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
    
    
    private void createAbstractImpl(JavaClass srcClass, JavaClass tgtClass) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        String implClassName = srcClass.getSimpleName() + "Impl";
        JavaClass innerClass = tgtClass.getInnerClass(implClassName, false);
        
        if (innerClass == null) {
            String name = implClassName;
            List annotations = Collections.EMPTY_LIST;
            int modifiers = Modifier.PRIVATE;
            
            // generate JavaDoc for the generated implementation of tested abstract class
            String javadocText = null;
            
            if (generateMethodJavadoc) {
                javadocText = MessageFormat.format(NbBundle.getMessage(TestCreator.class,"TestCreator.abstracImpl.JavaDoc.comment"),
                                                   new Object[] {srcClass.getName()});
            }
            
            // superclass
            MultipartId supClass = null;
            if (srcClass.isInner())
                supClass = pkg.getMultipartId().createMultipartId(srcClass.getName(),
                                                                  null,
                                                                  Collections.EMPTY_LIST);
            else
                supClass = pkg.getMultipartId().createMultipartId(srcClass.getSimpleName(),
                                                                  null,
                                                                  Collections.EMPTY_LIST);
            
            innerClass = pkg.getJavaClass().createJavaClass(name,
                                                            annotations,
                                                            modifiers,
                                                            javadocText,
                                                            null,
                                                            Collections.EMPTY_LIST,
                                                            null,
                                                            Collections.EMPTY_LIST,
                                                            Collections.EMPTY_LIST);
            if (srcClass.isInterface())
                innerClass.getInterfaceNames().add(supClass);
            else
                innerClass.setSuperClassName(supClass);
            
            
            createImpleConstructors(srcClass, innerClass);
            tgtClass.getFeatures().add(innerClass);
        }
        
        // created dummy implementation for all abstract methods
        Iterator it = TestUtil.collectFeatures(srcClass, Method.class,
                                               Modifier.ABSTRACT, true).iterator();
        
        while (it.hasNext()) {
            Method oldMethod = (Method)it.next();
            if (innerClass.getMethod(oldMethod.getName(),
                                     TestUtil.getParameterTypes(oldMethod.getParameters()),
                                     false) == null) {
                Method newMethod = createMethodImpl(pkg, oldMethod);
                innerClass.getFeatures().add(newMethod);
            }
            
        }
        
        
    }
    
    
    private Method createMethodImpl(JavaModelPackage pkg, Method origMethod)  {
        Method   newMethod = pkg.getMethod().createMethod();
        
        newMethod.setName(origMethod.getName());
        
        // compute modifiers of the method
        int mod = origMethod.getModifiers() & ~Modifier.ABSTRACT;
        if (((JavaClass)origMethod.getDeclaringClass()).isInterface())
            mod |= Modifier.PUBLIC;
        newMethod.setModifiers(mod);
        
        // prepare the body of method implementation
        StringBuffer    body = new StringBuffer(200);
        if (generateSourceCodeHints) {
            body.append(NbBundle.getMessage(TestCreator.class,"TestCreator.methodImpl.bodyComment"));
            body.append("\n\n");
        }
        
        newMethod.setType(origMethod.getType());
        Type type= origMethod.getType();
        if (type != null) {
            String value = null;
            if ((type instanceof JavaClass) || (type instanceof Array)) {
                value = "null";
            } else if (type instanceof PrimitiveType) {
                PrimitiveTypeKindEnum tke = (PrimitiveTypeKindEnum)((PrimitiveType)type).getKind();
                if (tke.equals(PrimitiveTypeKindEnum.BOOLEAN)) value = "false";
                else if (!tke.equals(PrimitiveTypeKindEnum.VOID)) value = "0";
            }
            
            if (value != null)
                body.append("return "+value+";\n");
        }
        
        newMethod.setBodyText(body.toString());
        
        // parameters
        newMethod.getParameters().addAll(TestUtil.cloneParams(origMethod.getParameters(), pkg));
        
        return newMethod;
    }
    
    
    
    static private void createImpleConstructors(JavaClass srcClass, JavaClass tgtClass) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        Iterator it = TestUtil.filterFeatures(srcClass, Constructor.class).iterator();
        while (it.hasNext()) {
            Constructor ctr = (Constructor)it.next();
            
            if (0 == (ctr.getModifiers() & Modifier.PRIVATE)) {
                Constructor nctr = pkg.getConstructor().createConstructor();
                nctr.setBodyText("super(" + getParameterString(ctr.getParameters()) + ");\n");
                nctr.getParameters().addAll(TestUtil.cloneParams(ctr.getParameters(),pkg));
                tgtClass.getFeatures().add(nctr);
            }
        }
    }
    
    
    
    static private String getParameterString(List params) {
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
    
    
    
    private static void removeSuiteMethod(JavaClass tgtClass) {
        Method sm = tgtClass.getMethod("suite", Collections.EMPTY_LIST, false);
        if (sm != null) tgtClass.getFeatures().remove(sm);
    }
    
    
    private void addMainMethod(JavaClass tgtClass) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        if (generateMainMethod && !TestUtil.hasMainMethod(tgtClass)) {
            // add main method
            String mainMethodBodySetting = getInitialMainMethodBody();
            
            if ((mainMethodBodySetting != null) && (mainMethodBodySetting.length() > 0) ) {
                // create body
                StringBuffer mainMethodBody = new StringBuffer(mainMethodBodySetting.length() + 2);
                mainMethodBody.append('\n');
                mainMethodBody.append(mainMethodBodySetting);
                mainMethodBody.append('\n');
                
                Type paramType = pkg.getArray().resolveArray(TestUtil.getStringType(pkg));
                Parameter param = pkg.getParameter().createParameter("argList",
                                                                     Collections.EMPTY_LIST, // annotations
                                                                     false, // is final
                                                                     null, // typename
                                                                     0, // dimCount
                                                                     false);
                param.setType(paramType);
                
                
                Method mainMethod = pkg.getMethod().createMethod("main",
                                                                 Collections.EMPTY_LIST,
                                                                 Modifier.STATIC | Modifier.PUBLIC,
                                                                 null, // javadoc text
                                                                 null, // jvadoc
                                                                 null, // object body
                                                                 mainMethodBody.toString(), // string body
                                                                 Collections.EMPTY_LIST, // type params
                                                                 Collections.singletonList(param), // parameters
                                                                 Collections.EMPTY_LIST, // exceptions
                                                                 TestUtil.getTypeReference(pkg, "void"), // type
                                                                 0);
                tgtClass.getFeatures().add(mainMethod);
            }
        }
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
    
    private static Type createStringType(JavaModelPackage pkg) {
        return pkg.getType().resolve("java.lang.String");
    }
    
    public void createEmptyTest(Resource srcRc, JavaClass cls) {
        // public entry points are wrapped in MDR transactions
        JavaModel.getJavaRepository().beginTrans(true);
        try {   
            addFrameworkImport(srcRc);
            fillGeneral(cls);
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }

        if (generateSourceCodeHints) addClassBodyComment(cls);
    }
    
    /**
     * This method must be run outside of JMI transaction in order to be
     * able to correctly acquire source code offsets. The comment is generated
     * at the end of the class passed in in the parameter.
     *
     * @param cls JavaClass to generate the comment to.
     */
    private void addClassBodyComment(JavaClass cls) {
        JavaModel.getJavaRepository().beginTrans(true);
        int off = cls.getEndOffset() - 1;        
        try {   
            String thecomment1 = NbBundle.getMessage(TestCreator.class, CLASS_COMMENT_LINE1);
            String thecomment2 = NbBundle.getMessage(TestCreator.class, CLASS_COMMENT_LINE2);
            String indent = getIndentString();
            DiffElement diff = new DiffElement(off, off, indent + thecomment1 + "\n" + indent + thecomment2 + "\n\n");
            ((ResourceImpl)cls.getResource()).addExtDiff(diff);
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }
    
    private static String getIndentString() {
        int spt = org.netbeans.modules.javacore.jmiimpl.javamodel.MetadataElement.getIndentSpace(); // spaces per tab
        String tabString;
        if (org.netbeans.modules.javacore.jmiimpl.javamodel.MetadataElement.isExpandTab()) {
            tabString = "";
            for (int i = 0; i<spt; i++) tabString += " ";
        } else
            tabString = "\t";
        
        return tabString;
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
