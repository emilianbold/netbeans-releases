/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

import org.openide.src.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.api.mdr.MDRepository;


/**
 *
 * @author  vstejskal
 * @author  Marian Petras
 * @version 1.0
 */
public class TestCreator extends java.lang.Object {

    /* attributes - private */
    static private final String JUNIT_SUPER_CLASS_NAME                = "TestCase";
    static private final String JUNIT_FRAMEWORK_PACKAGE_NAME    = "junit.framework";    
    
    static private final String forbiddenMethods[]               = {"main", "suite", "run", "runBare", "setUp", "tearDown"};
    
    static private final String GENERATED_SUITE_BLOCK_START                = "--JUNIT:";
    static private final String GENERATED_SUITE_BLOCK_END                  = ":JUNIT--";    
    private static final String METHOD_NAME_SETUP = "setUp";            //NOI18N
    private static final String METHOD_NAME_TEARDOWN = "tearDown";      //NOI18N

    /* public methods */

    /** Creates new TestCreator */
    public TestCreator() {
    }
    
    private static String arrayToString(Object[] array) {
        String result=array.getClass().getName()+":";
        for (int i=0; i<array.length; i++) {
            result+=array[i]+" ";
        }
        return result;   
    }

    static public void createTestClass(FileObject sourceCtx, ClassElement classSource, FileObject classCtx, ClassElement classTarget) throws SourceException {
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            SourceElement   srcelSource;
            SourceElement   srcelTarget;
        
            // update the source file of the test class
            srcelSource = classSource.getSource();
            srcelTarget = classTarget.getSource();

            srcelTarget.setPackage(srcelSource.getPackage());
 
            // add imports from the source but only those that are not
            // already present
            Import [] timports = srcelTarget.getImports();
            Import [] simports = srcelSource.getImports();
            HashSet tImpSet = new HashSet(timports.length);
            for (int i = 0 ; i < timports.length; i++) tImpSet.add(timports[i]);

            // import for junit.framework.*
            Import frameworkImp = new Import(Identifier.create(JUNIT_FRAMEWORK_PACKAGE_NAME), Import.PACKAGE);
            if (!tImpSet.contains(frameworkImp)) srcelTarget.addImport(frameworkImp);

            // all other imports if not present, yet
            for (int j = 0; j < simports.length; j++) {
                if (!tImpSet.contains(simports[j])) {
                    srcelTarget.addImport(simports[j]);
                }
            }


            // construct/update test class from the source class
            fillTestClass(sourceCtx, classSource, classCtx, classTarget);
        
            // if aplicable, add main method (method checks options itself)
            addMainMethod(classTarget);

        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }
    

    static public void createTestSuite(LinkedList listMembers, String packageName, ClassElement classTarget) throws SourceException {

        JavaModel.getJavaRepository().beginTrans(true);
        try {
            SourceElement   srcelTarget;

            // update the source file of the suite class
            srcelTarget = classTarget.getSource();
            //System.err.println("createTestSuite(): srcelTarget:"+srcelTarget);
        
            srcelTarget.setPackage(packageName.length() != 0 ? Identifier.create(packageName) : null);
            srcelTarget.addImport(new Import(Identifier.create(JUNIT_FRAMEWORK_PACKAGE_NAME), Import.PACKAGE));

            // construct/update test class from the source class
            fillSuiteClass(listMembers, packageName, classTarget);
        
            // if aplicable, add main method (method checks options itself)
            addMainMethod(classTarget);        

        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }

    
    static public void initialize() {
        // setup the methods filter
        cfg_MethodsFilter = 0;
        cfg_MethodsFilterPackage = JUnitSettings.getDefault().isMembersPackage();
        if (JUnitSettings.getDefault().isMembersProtected()) cfg_MethodsFilter |= Modifier.PROTECTED;
        if (JUnitSettings.getDefault().isMembersPublic()) cfg_MethodsFilter |= Modifier.PUBLIC;
    }

    static public boolean isClassTestable(FileObject ctx, ClassElement ce) {

        JavaModel.getJavaRepository().beginTrans(true);
        try {
            // @@        System.out.println("isClassTestable : " + ce.getName().getFullName());
        
            ClassElement[]  innerClasses;

            // check whether the ClassElement is class
            if (null == ce || (!ce.isClass())) {
                // we will not create tests for it
                return false;
            }
        
            JUnitSettings settings = JUnitSettings.getDefault();
        
            //System.err.println("isClassTestable: class name="+ce.getVMName());
        
            // check whether class implements test interfaces
            if (TestUtil.isClassElementImplementingTestInterface(ctx, ce)) {
                //System.err.println("!!Class implements Test Interface");
                if ( ! JUnitSettings.GENERATE_TESTS_FROM_TEST_CLASSES) {
                    // we don't want to generate tests from test classes                
                    return false;
                }
            }

            // this is WILD :-(((( ....
            int classModifiers = ce.getModifiers();
            if ( ((0 != (classModifiers & Modifier.PUBLIC)) || 
                  ( settings.isIncludePackagePrivateClasses() && (0 == ( classModifiers & Modifier.PRIVATE )))
                  ) &&
                 (settings.isGenerateExceptionClasses() || ! TestUtil.isClassElementException(ctx, ce)) &&
                 (!ce.isInner() || 0 != (classModifiers & Modifier.STATIC)) &&
                 (0 == (classModifiers & Modifier.ABSTRACT) || settings.isGenerateAbstractImpl()) &&
                 hasTestableMethods(ce)) {
                return true;
            }

            //System.err.println("isClassTestable(): does not seem to be testable");
            // nothing from the non-static inner class is accessible (and testable),
            // except there is a class specific way how to get an instance of inner class
            if (ce.isInner() && 0 == (classModifiers & Modifier.STATIC)) {
                //System.err.println("isClassTestable(): is inner, but not static");
                return false;
            }
            
            // check for testable inner classes
            innerClasses = ce.getClasses();
            for(int i = 0; i < innerClasses.length; i++) {
                if (isClassTestable(ctx, innerClasses[i]))
                    return true;
            }

            return false;

        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }
    
    



    
    /* private methods */
    static private boolean         cfg_MethodsFilterPackage = true;
    static private int             cfg_MethodsFilter = Modifier.PUBLIC | Modifier.PROTECTED;
    
    static private void addMethods(ClassElement classTest, LinkedList methods) {
        ListIterator    li;
        MethodElement   m;
        
        if (null == methods)
            return;
        
        li = methods.listIterator();
        while (li.hasNext()) {
            m = (MethodElement)li.next();
            try {
                classTest.addMethod(m);
            } catch (SourceException e) {
                // Nothing is done, because it is expected that the method already exist
            }
        }
    }    
    

    /**
     * Creates function <b>static public Test suite()</b> and fills its body,
     * appends all test functions in the class and creates sub-suites for
     * all test inner classes.
     */
    static private MethodElement createTestClassSuiteMethod(FileObject ctx, ClassElement classTest) throws SourceException {
        StringBuffer    body = new StringBuffer(512);
        ClassElement    innerClasses[];        
        
                
        
        //System.err.println("Generating suite() method for :"+classTest.getName());
        
        //removeSuiteMethod(classTest);
        
        // create header of function
        MethodElement method = new MethodElement();
        method.setName(Identifier.create("suite"));
        method.setModifiers(Modifier.STATIC | Modifier.PUBLIC);
        method.setReturn(org.openide.src.Type.createClass(Identifier.create("Test")));
        
        // !-- GENERATE NbJUnit no longer supported
        // prepare the body - if we generate nbjunit - we have to generate NbTestSuite otherwise TestSuite        
        /*if (JUnitSettings.getDefault().isGenerateNbJUnit()) {
            body.append("\nTestSuite suite = new NbTestSuite(");
        } else {         
           body.append("\nTestSuite suite = new TestSuite(");             
        }*/
        // GENERATE NbJUnit no longer supported --!
        
        body.append("\nTestSuite suite = new TestSuite(");
        body.append(classTest.getName().getName());
        body.append(".class);\n");
        
        innerClasses = classTest.getClasses();        
        for(int i = 0; i < innerClasses.length; i++) {
            ClassElement innerClass = innerClasses[i];
            if (TestUtil.isClassElementTest(ctx, innerClass)) {
                //System.err.println("Adding inner class:"+innerClasses[i].getVMName());
                body.append("suite.addTest(");
                body.append(innerClass.getName().getName());
                body.append(".suite());\n");
            } 
        }
        
        body.append("\nreturn suite;\n");
        method.setBody(body.toString());
        return method;
    }

    static private ConstructorElement createTestConstructor(Identifier className) throws SourceException {
        ConstructorElement constr = new ConstructorElement();
        constr.setName(className);
        constr.setModifiers(Modifier.PUBLIC);
        MethodParameter[] params = {new MethodParameter("testName", org.openide.src.Type.createFromClass(java.lang.String.class), false)};
        constr.setParameters(params);
        constr.setBody("\nsuper(testName);\n");
        return constr;
    }

    static private LinkedList createVariantMethods (ClassElement classSource) throws SourceException {
        LinkedList      methodList = new LinkedList();
        MethodElement[] allMethods = classSource.getMethods();                        
        MethodElement   method;
        String          name;
        Identifier      newName;
        StringBuffer    newBody = new StringBuffer();
        
        for (int i = 0; i < allMethods.length; i++) {
            // check modifiers against the settings of test generation
            if (isMethodAcceptable(allMethods[i])) {
               name = allMethods[i].getName().getName();
                method = new MethodElement();
                newName = Identifier.create("test" + name.substring(0,1).toUpperCase() + name.substring(1));

                // generate only one test method for overloaded source methods
                if (!existsMethod(methodList, newName)) {
                    method.setName(newName);
                    method.setModifiers(Modifier.PUBLIC);

                    // generate JavaDoc for test method
                    if (JUnitSettings.getDefault().isJavaDoc()) {
                        String msg = MessageFormat.format(NbBundle.getMessage(TestCreator.class,"TestCreator.variantMethods.JavaDoc.comment"),
                                    new Object[] {name, classSource.getName().getFullName()});
                        method.getJavaDoc().setText(msg);
                    }

                    // generate the body of method
                    newBody.delete(0, newBody.length());
                    newBody.append("\n");
                    if (JUnitSettings.getDefault().isBodyContent()) {
                        // generate default bodies, printing the name of method
                        newBody.append("System.out.println(\"" + newName.getName() + "\");\n");
                    }
                    if (JUnitSettings.getDefault().isBodyComments()) {
                        // generate comments to bodies
                        newBody.append("\n"+NbBundle.getMessage(TestCreator.class,"TestCreator.variantMethods.defaultComment")+"\n");
                    }
                    if (JUnitSettings.getDefault().isBodyContent()) {
                        // generate a test failuare by default (in response to request 022).
                        newBody.append(NbBundle.getMessage(TestCreator.class,"TestCreator.variantMethods.defaultBody")+"\n");
                    }
                    method.setBody(newBody.toString());
                    method.setReturn(org.openide.src.Type.VOID);
                    methodList.add(method);
                }
            }
        }                
        return methodList;
    }
    
    static private boolean existsMethod(LinkedList methodList, Identifier id) {
        ListIterator    li;
        MethodElement   m;
        
        li = methodList.listIterator();
        while(li.hasNext()) {
            m = (MethodElement) li.next();
            if (m.getName().equals(id))
                return true;
        }
        return false;
    }

    static private boolean isImplemented(ClassElement implementor, MethodElement m) {
        MethodElement[] methods = implementor.getMethods();
        for(int i = 0; i < methods.length; i++) {
            if (0 == (methods[i].getModifiers() & Modifier.ABSTRACT) &&
                m.getName().equals(methods[i].getName()) &&
                m.getReturn().equals(methods[i].getReturn()) &&
                Arrays.equals(getParameterTypes(m.getParameters()), 
                              getParameterTypes(methods[i].getParameters()))) {
                return true;
            }
        }
        return false;
    }
     
    static private org.openide.src.Type[] getParameterTypes(MethodParameter[] params) {
        org.openide.src.Type[] types = new org.openide.src.Type[params.length];
        for (int i = 0; i < params.length; i++) {
            types[i] = params[i].getType();
        }

        return types;
    }
     
    static private boolean hasTestableMethods(ClassElement classSource) {
        MethodElement[] allMethods = classSource.getMethods();                        
        
        for (int i = 0; i < allMethods.length; i++) {
            if (isMethodAcceptable(allMethods[i]))
                return true;
        }
        
        return false;
    }

    static private void fillGeneral(ClassElement classTest) throws SourceException {
        ConstructorElement  constr;
        
        // !-- GENERATE NbJUnit no longer supported
        // set explicitly super class and modifiers
        /*
        if (JUnitSettings.getDefault().isGenerateNbJUnit()) {
            classTest.setSuperclass(Identifier.create(NBJUNIT_SUPER_CLASS_NAME));
        } else {
            classTest.setSuperclass(Identifier.create(JUNIT_SUPER_CLASS_NAME));
        }
        */
        // GENERATE NbJUnit no longer supported --!
        
        classTest.setSuperclass(Identifier.create(JUNIT_SUPER_CLASS_NAME));
        classTest.setModifiers(Modifier.PUBLIC);

        // remove default ctor, if exists (shouldn't throw exception)
        constr = classTest.getConstructor(new org.openide.src.Type[] {});
        if (null != constr)
            classTest.removeConstructor(constr);
        
        //fill classe's constructor
        constr = createTestConstructor(classTest.getName());
        try {
            classTest.addConstructor(constr);
        } catch (SourceException e) {
            // Nothing is done, because it is expected that constructor already exists
        }
        
        //add method setUp() (optionally):
        if (JUnitSettings.getDefault().isGenerateSetUp()
                && !hasInitMethod(classTest, METHOD_NAME_SETUP)) {
            try {
                classTest.addMethod(generateInitMethod(METHOD_NAME_SETUP));
            } catch (SourceException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
        
        //add method tearDown() (optionally):
        if (JUnitSettings.getDefault().isGenerateTearDown()
                && !hasInitMethod(classTest, METHOD_NAME_TEARDOWN)) {
            try {
                classTest.addMethod(generateInitMethod(METHOD_NAME_TEARDOWN));
            } catch (SourceException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
    }
    
    /**
     * Detects whether a given class contains a no-argument method of a given
     * name, having protected or public member access.
     *
     * @param  testClass  class the method is to be found in
     * @param  methodName  name of the method to be found
     * @return  <code>true</code> if the class contains such a method,
     *          <code>false</code> otherwise
     */
    private static boolean hasInitMethod(ClassElement testClass,
                                         String methodName) {
        return testClass.getMethod(Identifier.create(methodName),
                                   new org.openide.src.Type[0])
               != null;
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
    private static MethodElement generateInitMethod(String methodName)
            throws SourceException {
        MethodElement method = new MethodElement();
        method.setModifiers(Modifier.PROTECTED);
        method.setReturn(org.openide.src.Type.VOID);
        method.setName(Identifier.create(methodName));
        method.setParameters(new MethodParameter[0]);
        method.setExceptions(new Identifier[] {
                Identifier.create("java.lang.Exception", "Exception")});//NOI18N
        return method;
    }
    
    static private void fillTestClass(FileObject sourceCtx, ClassElement classSource, FileObject classCtx, ClassElement classTest) throws SourceException {
        LinkedList      methods;
        ClassElement    innerClasses[];        
        
        fillGeneral(classTest);

        // create test classes for inner classes
        innerClasses = classSource.getClasses();
        for(int i = 0; i < innerClasses.length; i++) {
            if (isClassTestable(sourceCtx, innerClasses[i])) {
                // create new test class
                ClassElement    innerTester;
                Identifier      name = Identifier.create(TestUtil.getTestClassName(innerClasses[i].getName().getName()));
                boolean         add = false;
                
                if (null == (innerTester = classTest.getClass(name))) {
                    add = true;
                    innerTester = new ClassElement();
                    innerTester.setName(name);
                }
                
                // process tested inner class the same way like top-level class
                fillTestClass(sourceCtx, innerClasses[i], classCtx, innerTester);
                
                // do additional things for test class to became inner class usable for testing in JUnit
                innerTester.setModifiers(innerTester.getModifiers() | Modifier.STATIC);
                if (add)
                    classTest.addClass(innerTester);
            }
        }
        
        
        // add suite method ... only if we are supposed to do so
        if (JUnitSettings.getDefault().isGenerateSuiteClasses()) {
            methods = new LinkedList();
            methods.add(createTestClassSuiteMethod(classCtx, classTest));
            addMethods(classTest, methods);            
        } else {
            //System.err.println("TestCreator.createTestClassSuiteMethod() - do not regenerate ...");
        }
        
        // fill methods according to the iface of tested class
        methods = createVariantMethods(classSource);
        addMethods(classTest, methods);
        
        // create abstract class implementation
        if ((JUnitSettings.getDefault().isGenerateAbstractImpl()) &&
            (0 != (classSource.getModifiers() & Modifier.ABSTRACT))) {
// @@            System.out.println("fillTestClass : calling createAbstractImpl");
            try {
                createAbstractImpl(classSource, classTest);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }        

    static private void fillSuiteClass(LinkedList listMembers, String packageName, ClassElement classTest) throws SourceException {
        LinkedList      methods;
        MethodElement   method;
        boolean         updateBody = false;
        
        
        fillGeneral(classTest);
        
        methods = new LinkedList();

        removeSuiteMethod(classTest);

        MethodElement oldMethod = classTest.getMethod(Identifier.create("suite"), new org.openide.src.Type[] {});
        if (oldMethod == null) {
            method = new MethodElement();
            method.setName(Identifier.create("suite"));
            
        } else {
            method = oldMethod;
        }
        method.setModifiers(Modifier.STATIC | Modifier.PUBLIC);
        method.setReturn(org.openide.src.Type.createClass(Identifier.create("Test")));
        method.getJavaDoc().setText(NbBundle.getMessage(TestCreator.class,"TestCreator.suiteMethod.JavaDoc.comment"));
        
        // generate the body of suite method
        StringTokenizer oldBody = new StringTokenizer(method.getBody(), "\n");
        StringBuffer newBody = new StringBuffer();
        
        boolean insideBlock = false;
        boolean justBlockExists = false;
        
        
        
        while (oldBody.hasMoreTokens()) {
            String line = oldBody.nextToken();
            
            if (-1 != line.indexOf(GENERATED_SUITE_BLOCK_START)) {
                insideBlock = true;
            }
            else if (-1 != line.indexOf(GENERATED_SUITE_BLOCK_END)) {
                // JUst's owned suite block, regenerate it
                insideBlock = false;
                generateSuiteBody(classTest.getName().getName(), newBody, listMembers, true);
                justBlockExists = true;
            }
            else if (!insideBlock) {
                newBody.append(line);
                newBody.append("\n");
            }
        }
        
        if (!justBlockExists) {
            generateSuiteBody(classTest.getName().getName(), newBody, listMembers, true);
        }
                    
        //if (updateBody) {
            method.setBody(newBody.toString());
            // add/update methods to the class
            methods.add(method);
            addMethods(classTest, methods);
        //}
    }
    
    static private void generateSuiteBody(String testName, StringBuffer body, LinkedList members, boolean alreadyExists) {
        ListIterator    li;
        String          name;
        
           
        body.append('\n');
        //body.append("//" + GENERATED_SUITE_BLOCK_START + "\n");
        //body.append(NbBundle.getMessage(TestCreator.class,"TestCreator.suiteMethod.suiteBlock.comment")+"\n");
        body.append("TestSuite suite = new TestSuite(\"" + testName + "\");\n");
        
        li = members.listIterator();
        
        while (li.hasNext()) {
            name = (String) li.next();
            body.append("suite.addTest(" + name + ".suite());\n");
        }

        body.append("return suite;\n");
        //body.append("//" + GENERATED_SUITE_BLOCK_END + "\n");
                
    }
    
    static private boolean isForbidden(String name) {
        for (int i = 0; i < forbiddenMethods.length; i++) {            
            if (forbiddenMethods[i].equals(name)) 
              return true;            
        }
        return false;
    }
    

    static private boolean isMethodAcceptable(MethodElement m) {
        String name;
        if ((m.getModifiers() & Modifier.PRIVATE) == 0 &&
            ((m.getModifiers() & cfg_MethodsFilter) != 0 ||
            ((m.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0 && cfg_MethodsFilterPackage))) {
            name = m.getName().getName();
            return !isForbidden(name) && (m.getModifiers() & Modifier.ABSTRACT) == 0;
        }
        return false;
    }
    
    static private void createAbstractImpl(ClassElement sourceClass, ClassElement targetClass) throws SourceException {
        Identifier      implClassName = Identifier.create(sourceClass.getName().getName() + "Impl");
        ClassElement    innerClass = targetClass.getClass(implClassName);
        LinkedList      methods;
        Iterator        iterator;
        
        if (innerClass == null) {
            innerClass = new ClassElement();
            innerClass.setName(implClassName);
            innerClass.setModifiers(Modifier.PRIVATE);

            if (sourceClass.isInner())
                innerClass.setSuperclass(Identifier.create(sourceClass.getName().getFullName()));
            else
                innerClass.setSuperclass(sourceClass.getName());

            // generate JavaDoc for the generated implamentation of tested abstract class
            if (JUnitSettings.getDefault().isJavaDoc()) {
                
                String msg= MessageFormat.format(NbBundle.getMessage(TestCreator.class,"TestCreator.abstracImpl.JavaDoc.comment"), 
                                new Object[] {sourceClass.getName().getFullName()});
                   
                innerClass.getJavaDoc().setText(msg);
            }
            createImpleConstructors(sourceClass, innerClass);
        }

        // retrieve all unimplemented abstract methods
        methods = new LinkedList();
        getAbstractClassMethods(sourceClass, methods);
        
        // created dummy implementation for all abstract methods
        iterator = methods.iterator();
        while (iterator.hasNext()) {
            MethodElement origMethod = (MethodElement)iterator.next();
            MethodElement newMethod = createMethodImpl(origMethod);

            try {
                innerClass.addMethod(newMethod);
            } catch (SourceException e) {
                //ignore as the method already exists
            }
        }

        try {
            targetClass.addClass(innerClass);
        } catch (SourceException e) {
            //ignore as the inner class already exists
        }
    }
     
    static private MethodElement createMethodImpl(MethodElement origMethod) throws SourceException {
        MethodElement   newMethod = (MethodElement)origMethod.clone();
        StringBuffer    body = new StringBuffer(200);
        int             mod = origMethod.getModifiers() & ~Modifier.ABSTRACT;

// @@        System.out.println("createMethodImpl : " + origMethod.getName().getFullName());
// @@        System.out.println("createMethodImpl : " + (null == origMethod.getDeclaringClass() ? "null" : "OK"));
        if (origMethod.getDeclaringClass().isInterface())
            mod |= Modifier.PUBLIC;
        
        newMethod.setModifiers(mod);

        // prepare the body of method implementation
        body.append('\n');
        if (JUnitSettings.getDefault().isBodyComments()) {
            body.append(NbBundle.getMessage(TestCreator.class,"TestCreator.methodImpl.bodyComment"));
            body.append('\n');
        }
        if (newMethod.getReturn().isClass() || newMethod.getReturn().isArray()) {
            body.append("\nreturn null;\n");
        } else if (newMethod.getReturn().equals(org.openide.src.Type.BOOLEAN)) {
            body.append("\nreturn false;\n");
        } else if (!newMethod.getReturn().equals(org.openide.src.Type.VOID)) {
            body.append("\nreturn 0;\n");
        }
        newMethod.setBody(body.toString());

        return newMethod;
     }
     
     static private void createImpleConstructors(ClassElement sourceClass, ClassElement implInnerClass) throws SourceException {
         ConstructorElement[] constructors = sourceClass.getConstructors();
         ConstructorElement nextConstructor = null;
         for (int i = 0; i < constructors.length; i++) {
             nextConstructor = (ConstructorElement)constructors[i].clone();
             if (0 == (nextConstructor.getModifiers() & Modifier.PRIVATE)) {
                 nextConstructor.setBody("\nsuper(" + getParameterString(nextConstructor.getParameters()) + ");\n");
                 nextConstructor.getJavaDoc().clearJavaDoc();
                 implInnerClass.addConstructor(nextConstructor);
             }
         }
     }
     
     static private String getParameterString(MethodParameter[] params) {
         StringBuffer paramString = new StringBuffer();
         
         for (int i = 0; i < params.length; i++) {
             if (paramString.length() > 0) {
                 paramString.append(", ");
             }
             paramString.append(params[i].getName());
         }
         
         return paramString.toString();
     }
     
    static private LinkedList getInterfaceMethods(Identifier iface) throws SourceException {
        LinkedList         interfaceMethods = new LinkedList();
        MethodElement[]    methods;
        Identifier[]       interfaces;
        ClassElement       ifaceClass;

        // add all methods of current inetrface
        ifaceClass = ClassElement.forName(iface.getFullName());
        methods = ifaceClass.getMethods();
        for (int i = 0; i < methods.length; i++)
            interfaceMethods.add(methods[i]);

        // add methods of all implemented interfaces
        interfaces = ifaceClass.getInterfaces();
        for(int i = 0; i < interfaces.length; i++) {
            // name duplicities should be rare and will be discovered during impl generation
            interfaceMethods.addAll(getInterfaceMethods(interfaces[i]));
        }

        return interfaceMethods;
    }
     
    static private void getAbstractClassMethods(ClassElement sourceClass, LinkedList abstractMethods) throws SourceException {
        MethodElement[] methods;
        Identifier[]    interfaces;
        Identifier      superClassId;

// @@        System.out.println("getAbstractClassMethods : " + sourceClass.getName().getFullName());
        
        // given bag of abstract methods of classe's descendant should be
        // checked for those, which current class implements itself
        removeImplemented(sourceClass, abstractMethods, false);
        
        // add abstract methods of all implemented ifaces
        interfaces = sourceClass.getInterfaces();
        for(int i = 0; i < interfaces.length; i++) {
            LinkedList  ifaceAbstracts = getInterfaceMethods(interfaces[i]);

            // remove implemented methods
            removeImplemented(sourceClass, ifaceAbstracts, false);
            abstractMethods.addAll(ifaceAbstracts);
        }

        // add all own abstract methods
        methods = sourceClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (0 != (methods[i].getModifiers() & Modifier.ABSTRACT))
                abstractMethods.add((MethodElement)methods[i]);
        }
        
        // recurse for superclass if it is abstract or remove methods implemented by superclass
        if (null != (superClassId = sourceClass.getSuperclass())) {
            ClassElement superClass = ClassElement.forName(superClassId.getFullName());
            if (null != superClass) {
                if (0 != (superClass.getModifiers() & Modifier.ABSTRACT))
                    getAbstractClassMethods(superClass, abstractMethods);
                else
                    removeImplemented(superClass, abstractMethods, true);
            }
        }
    }

    private static void removeImplemented(ClassElement implementor, LinkedList methods, boolean traverseAll) throws SourceException {
        Identifier  superClass;
        Iterator    it = methods.iterator();
        while (it.hasNext()) {
            MethodElement m = (MethodElement)it.next();
            if (isImplemented(implementor, m)) {
                it.remove();
            }
        }
        
        if (traverseAll && null != (superClass = implementor.getSuperclass())) {
            ClassElement superClassElemenet = ClassElement.forName(superClass.getFullName());
            if (null != superClassElemenet)
                removeImplemented(superClassElemenet, methods, true);
        }
    }
    
    private static void removeSuiteMethod(ClassElement classTest) throws SourceException {
        MethodElement suiteMethod = classTest.getMethod(Identifier.create("suite"),null);
        if (suiteMethod != null) {
            //System.err.println("TestCreator.removeSuiteMethod() - suite method found ..");
            // remove the method
            classTest.removeMethod(suiteMethod);
        } 
    }
    
    private static void addMainMethod(ClassElement targetClass) throws SourceException {
        if (JUnitSettings.getDefault().isGenerateMainMethod()) {
            if (!targetClass.hasMainMethod()) {
                // add main method
                String mainMethodBodySetting = JUnitSettings.getDefault().getGenerateMainMethodBody();
                if ((mainMethodBodySetting != null) & (mainMethodBodySetting.length() > 0) ) {
                    MethodElement mainMethod = createMainMethodElement();
                    StringBuffer mainMethodBody = new StringBuffer(mainMethodBodySetting.length() + 2);
                    mainMethodBody.append('\n');
                    mainMethodBody.append(mainMethodBodySetting);
                    mainMethodBody.append('\n');
                    mainMethod.setBody(mainMethodBody.toString());
                    targetClass.addMethod(mainMethod);
                }
            }
        }
    }
    
    private static MethodElement createMainMethodElement() throws SourceException {
        MethodElement mainMethod = new MethodElement();
        mainMethod.setName(Identifier.create("main"));
        mainMethod.setModifiers(Modifier.STATIC | Modifier.PUBLIC);
        mainMethod.setReturn(org.openide.src.Type.VOID);
        MethodParameter parameter = new MethodParameter("args", org.openide.src.Type.createArray(org.openide.src.Type.createFromClass(String.class)), false);
        mainMethod.setParameters(new MethodParameter[] {parameter} );
        return mainMethod;
    }
}
