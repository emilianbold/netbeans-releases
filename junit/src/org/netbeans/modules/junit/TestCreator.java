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

import org.openide.util.NbBundle;
import org.netbeans.jmi.javamodel.*;
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


    static public void createTestClass(Resource srcRc, JavaClass srcClass, 
                                       Resource tgtRc, JavaClass tgtClass) 
    {
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
            org.netbeans.jmi.javamodel.Import frameworkImp = 
                createFrameworkImport(tgtPackage);

            if (!tImpSet.contains(new ImpEq(frameworkImp))) 
                tgtImports.add(frameworkImp);

            // all other imports if not present, yet
            Iterator simpit = srcImports.iterator();
            while (simpit.hasNext()) {
                org.netbeans.jmi.javamodel.Import imp = 
                    (org.netbeans.jmi.javamodel.Import)simpit.next();
                if (!tImpSet.contains(new ImpEq(imp))) {
                    tgtImports.add(tgtPackage.getImport().
                                   createImport(imp.getName(), 
                                                imp.getIdentifier(), 
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

    static public void createTestSuite(List listMembers, 
                                       String packageName, 
                                       JavaClass tgtClass)  
    {
        JavaModel.getJavaRepository().beginTrans(true);
        try {

            Resource   tgtRes = tgtClass.getResource();
            tgtRes.setPackageName(packageName);

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

            // construct/update test class from the source class
            fillSuiteClass(listMembers, packageName, tgtClass);
        
            // if aplicable, add main method (method checks options itself)
            addMainMethod(tgtClass);        

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

    static public boolean isClassTestable(JavaClass jc) {

        JavaModel.getJavaRepository().beginTrans(true);
        try {

            if (jc == null) return false;
        
            JUnitSettings settings = JUnitSettings.getDefault();
        
            // check whether class implements test interfaces
            if (TestUtil.isClassImplementingTestInterface(jc)) {
                if (!JUnitSettings.GENERATE_TESTS_FROM_TEST_CLASSES) {
                    // we don't want to generate tests from test classes                
                    return false;
                }
            }

            int classModifiers = jc.getModifiers();
            if ( ((0 != (classModifiers & Modifier.PUBLIC)) || 
                  ( settings.isIncludePackagePrivateClasses() && (0 == ( classModifiers & Modifier.PRIVATE )))
                  ) &&
                 (settings.isGenerateExceptionClasses() || ! TestUtil.isClassException(jc)) &&
                 (!jc.isInner() || 0 != (classModifiers & Modifier.STATIC)) &&
                 (0 == (classModifiers & Modifier.ABSTRACT) || settings.isGenerateAbstractImpl()) &&
                 hasTestableMethods(jc)) {
                return true;
            }

            // nothing from the non-static inner class is accessible (and testable),
            // except there is a class specific way how to get an instance of inner class
            if (jc.isInner() && 0 == (classModifiers & Modifier.STATIC)) {
                return false;
            }
            
            // check for testable inner classes
            Iterator it  = TestUtil.collectFeatures(jc, JavaClass.class, 0, true).iterator();
            while (it.hasNext()) {
                if (isClassTestable((JavaClass)it.next())) return true;
            }

            return false;


        } finally {
            JavaModel.getJavaRepository().endTrans();
        }

    }
    
    



    
    /* private methods */
    static private boolean         cfg_MethodsFilterPackage = true;
    static private int             cfg_MethodsFilter = 0;


    

    /**
     * Returns true if tgtClass contains suite() method 
     */
    static private boolean hasSuiteMethod(JavaClass tgtClass) {
        return tgtClass.getMethod("suite", Collections.EMPTY_LIST, false)!= null;
    }

    static private Method createSuiteMethod(JavaModelPackage pkg) {
        Method ret = pkg.getMethod().createMethod();
        ret.setName("suite");
        ret.setModifiers(Modifier.STATIC | Modifier.PUBLIC);
        ret.setTypeName(TestUtil.getTypeReference(pkg,"junit.framework.Test"));
        return ret;
    }

    /**
     * Creates function <b>static public Test suite()</b> and fills its body,
     * appends all test functions in the class and creates sub-suites for
     * all test inner classes.
     */
    static private Method createTestClassSuiteMethod(JavaClass tgtClass) {

        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        // create header of function
        Method method = createSuiteMethod(pkg);
        
        StringBuffer body = new StringBuffer(1024);
        body.append("\njunit.framework.TestSuite suite = new junit.framework.TestSuite(");
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
        method.setBodyText(body.toString());
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

    static private Method createTestMethod(JavaClass sclass, Method sm, JavaModelPackage pkg) {

        String smName = sm.getName();

        // method name
        String newName = createTestMethodName(smName);

        List annotations = Collections.EMPTY_LIST;
        int modifiers = Modifier.PUBLIC;

        // javadoc
        String javadocText = 
            JUnitSettings.getDefault().isJavaDoc() ? 
            MessageFormat.format(NbBundle.getMessage(TestCreator.class,
                                                     "TestCreator.variantMethods.JavaDoc.comment"), 
                                 new Object[] {smName, sclass.getName()})
            : null;
            
        // create body of the method
        StringBuffer newBody = new StringBuffer(512);
        newBody.append("\n");
        if (JUnitSettings.getDefault().isBodyContent()) {
            // generate default bodies, printing the name of method
            newBody.append("System.out.println(\"" + newName + "\");\n");
        }
        if (JUnitSettings.getDefault().isBodyComments()) {
            // generate comments to bodies
            newBody.append("\n"+NbBundle.getMessage(TestCreator.class,"TestCreator.variantMethods.defaultComment")+"\n");
        }
        if (JUnitSettings.getDefault().isBodyContent()) {
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


     

    static private boolean hasTestableMethods(JavaClass cls) {

        Iterator methods = TestUtil.collectFeatures(cls, Method.class, 0, true).iterator();
        while (methods.hasNext()) {
            if (isMethodAcceptable((Method)methods.next()))
                return true;
        }
        
        return false;
    }



    static public void fillGeneral(JavaClass testClass) {
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
            if (JUnitSettings.getDefault().isGenerateSetUp()
                && !hasInitMethod(testClass, METHOD_NAME_SETUP)) {

                testClass.getFeatures().add(generateInitMethod(pkg, METHOD_NAME_SETUP));
            }
        
            //add method tearDown() (optionally):
            if (JUnitSettings.getDefault().isGenerateTearDown()
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
    private static Method generateInitMethod(JavaModelPackage pkg, String methodName)
    {
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
                         Collections.singletonList(pkg.getMultipartId().createMultipartId("java.lang.Exception", null, Collections.EMPTY_LIST)), // exception names
                         pkg.getMultipartId().createMultipartId("void", null, Collections.EMPTY_LIST), // typeName
                         0 // dimCount
                         );
        return method;
    }




    static private void fillTestClass(Resource srcRc, JavaClass srcClass, Resource tgtRc, JavaClass tgtClass) 
    {
        fillGeneral(tgtClass);

        List    innerClasses = TestUtil.filterFeatures(srcClass, JavaClass.class);
        
        // create test classes for inner classes
        Iterator itInner = innerClasses.iterator();
        while (itInner.hasNext()) {
            JavaClass theClass = (JavaClass)itInner.next();
            JavaModelPackage pkg = ((JavaModelPackage)tgtClass.refImmediatePackage());

            if (isClassTestable(theClass)) {
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

        if (JUnitSettings.getDefault().isGenerateSuiteClasses() &&
            (!hasSuiteMethod(tgtClass))) {
            tgtClass.getFeatures().add(createTestClassSuiteMethod(tgtClass));            
        } 

        

        // fill methods according to the iface of tested class
        Iterator methit = TestUtil.filterFeatures(srcClass, Method.class).iterator();
        while (methit.hasNext()) {
            Method sm = (Method)methit.next();
            if (isMethodAcceptable(sm) &&
                tgtClass.getMethod(createTestMethodName(sm.getName()),
                                   createTestMethodParams(sm, (JavaModelPackage)tgtClass.refImmediatePackage()), 
                                   false) == null) 
                {
                    Method tm = createTestMethod(srcClass, sm, (JavaModelPackage)tgtClass.refImmediatePackage());
                    tgtClass.getFeatures().add(tm);
                } 

        }


        // create abstract class implementation
         if (
             (JUnitSettings.getDefault().isGenerateAbstractImpl()) &&
             (
              (Modifier.ABSTRACT == (srcClass.getModifiers() & Modifier.ABSTRACT)) |
              (srcClass.isInterface())
              )
             ) {
             createAbstractImpl(srcClass, tgtClass);
         }
         
    }        
  

    static private void fillSuiteClass(List listMembers, 
                                       String packageName, 
                                       JavaClass tgtClass)  {
        
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();                 
        fillGeneral(tgtClass);

        // find "suite()" method 
        Method suiteMethod = tgtClass.getMethod("suite", Collections.EMPTY_LIST, false);
        tgtClass.getFeatures().remove(suiteMethod);
        
        suiteMethod = createSuiteMethod(pkg);
        String javadocText = NbBundle.getMessage(TestCreator.class,"TestCreator.suiteMethod.JavaDoc.comment");
        JavaDoc jd = pkg.getJavaDoc().createJavaDoc(javadocText, Collections.EMPTY_LIST);
        suiteMethod.setJavadoc(jd);
        
        StringBuffer newBody = new StringBuffer();
        generateSuiteBody(tgtClass.getSimpleName(), newBody, listMembers, true);
        suiteMethod.setBodyText(newBody.toString());
        tgtClass.getFeatures().add(suiteMethod);

    }


    

    static private void generateSuiteBody(String testName, StringBuffer body, List members, boolean alreadyExists) {
        Iterator    li;
        String      name;
        
           
        body.append('\n');
        //body.append("//" + GENERATED_SUITE_BLOCK_START + "\n");
        //body.append(NbBundle.getMessage(TestCreator.class,"TestCreator.suiteMethod.suiteBlock.comment")+"\n");
        body.append("junit.framework.TestSuite suite = new junit.framework.TestSuite(\"" + testName + "\");\n");
        
        li = members.listIterator();
        
        while (li.hasNext()) {
            name = (String) li.next();
            body.append("suite.addTest(" + name + ".suite());\n");
        }

        body.append("return suite;\n");
        //body.append("//" + GENERATED_SUITE_BLOCK_END + "\n");
                
    }



    static private boolean isMethodAcceptable(Method m) {
        return (
                (m.getModifiers() & Modifier.PRIVATE) == 0 &&
                (
                 (m.getModifiers() & cfg_MethodsFilter) != 0 ||
                 (
                  (m.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0 
                  && cfg_MethodsFilterPackage
                  )
                 )
                );
    }


    static private void createAbstractImpl(JavaClass srcClass, JavaClass tgtClass) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        String implClassName = srcClass.getSimpleName() + "Impl";
        JavaClass innerClass = tgtClass.getInnerClass(implClassName, false);

        if (innerClass == null) {
            String name = implClassName;
            List annotations = Collections.EMPTY_LIST;
            int modifiers = Modifier.PRIVATE;

            // generate JavaDoc for the generated implementation of tested abstract class
            String javadocText = null;
            if (JUnitSettings.getDefault().isJavaDoc()) {
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
    

    static private Method createMethodImpl(JavaModelPackage pkg, Method origMethod)  {
        Method   newMethod = pkg.getMethod().createMethod();

        newMethod.setName(origMethod.getName());

        // compute modifiers of the method
        int mod = origMethod.getModifiers() & ~Modifier.ABSTRACT;
        if (((JavaClass)origMethod.getDeclaringClass()).isInterface())
            mod |= Modifier.PUBLIC;
        newMethod.setModifiers(mod);

        // prepare the body of method implementation
        StringBuffer    body = new StringBuffer(200);
        body.append('\n');
        if (JUnitSettings.getDefault().isBodyComments()) {
            body.append(NbBundle.getMessage(TestCreator.class,"TestCreator.methodImpl.bodyComment"));
            body.append('\n');
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
                body.append("\nreturn "+value+";\n");
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
                 nctr.setBodyText("\nsuper(" + getParameterString(ctr.getParameters()) + ");\n");
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


    private static void addMainMethod(JavaClass tgtClass) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();  
        if ((JUnitSettings.getDefault().isGenerateMainMethod()) && (!TestUtil.hasMainMethod(tgtClass))) {
            // add main method
            String mainMethodBodySetting = 
                JUnitSettings.getDefault().getGenerateMainMethodBody();

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


    private static Type createStringType(JavaModelPackage pkg) {
        return pkg.getType().resolve("java.lang.String");
    }



}
