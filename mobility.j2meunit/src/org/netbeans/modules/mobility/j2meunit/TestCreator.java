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

/*
 * TestCreator.java
 *
 * Created on March 23, 2006, 4:22 PM
 */
package org.netbeans.modules.mobility.j2meunit;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.NullCipher;
import org.netbeans.jmi.javamodel.CallableFeature;
import org.netbeans.jmi.javamodel.ClassDefinition;
import org.netbeans.jmi.javamodel.Constructor;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.Feature;
import org.netbeans.jmi.javamodel.Import;
import org.netbeans.jmi.javamodel.JavaDoc;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.JavaPackage;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.PrimitiveType;
import org.netbeans.jmi.javamodel.PrimitiveTypeKindEnum;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.jmi.javamodel.ThisExpression;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.TypeReference;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.javacore.internalapi.JavaModelUtil;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.junit.plugin.JUnitPlugin.CreateTestParam;
import org.openide.util.NbBundle;
/**
 *
 * @author bohemius
 */
public class TestCreator {
    
    private static final int CLASS_NAME             =   99310;
    private static final int INC_PROTECTED          =   99312;
    private static final int INC_PKG_PRIVATE        =   99313;
    private static final int INC_SETUP              =   99314;
    private static final int INC_TEAR_DOWN          =   99315;
    private static final int INC_METHOD_BODIES      =   99316;
    private static final int INC_JAVADOC            =   99317;
    private static final int INC_CODE_HINT          =   99318;
    private static final int INC_PKG_PRIVATE_CLASS  =   99319;
    private static final int INC_ABSTRACT_CLASS     =   99320;
    private static final int INC_EXCEPTION_CLASS    =   99321;
    private static final int GENERATE_SUITE         =   99322;
    
    private Map<CreateTestParam, Object> parameters;
    private List<JavaClass> existingTestClasses;
    private JavaModelPackage testTargetPkg;
    private FileObject testSourceRoot;
    
    static private final String J2MEUNIT_SUPER_CLASS_NAME="TestCase";
    static private final String J2MEUNIT_FRAMEWORK_PACKAGE_NAME="j2meunit.framework";
    
    
    /** Creates a new instance of TestCreator */
    public TestCreator(Map<CreateTestParam, Object> params, FileObject testTargetRoot) {
        this.parameters=params;
        this.testSourceRoot=testTargetRoot;
        this.existingTestClasses=new LinkedList<JavaClass>();
        //System.out.println("testTargetRoot: "+testTargetRoot.getPath());
        //System.out.println("parameters: "+parameters);
        try {
            this.testTargetPkg=JavaModel.getJavaExtent(testTargetRoot);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Resource testClassExists(JavaClass clazz, String pkgName) {
        Resource res=null;
        String testClassFileName;
        
        try {
            if (pkgName.equals(""))
                testClassFileName=TestUtils.getTestClassName(clazz.getSimpleName())+".java";
            else
                testClassFileName=pkgName.replace('.','/')+"/"+TestUtils.getTestClassName(clazz.getSimpleName())+".java";
            res=JavaModel.getResource(this.testSourceRoot, testClassFileName);
        } catch (Exception e) {
            System.out.println("We have problem here...");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            return res;
        }
    }
    
    private Method createSetUp() {
        return this.testTargetPkg.getMethod().createMethod(
                "setUp",                        //name of the test method
                Collections.EMPTY_LIST,         //list of annotations
                Modifier.PUBLIC,                //modifiers
                null,                           //javadoc text
                null,                           //javadoc
                null,                           //method body (in statement object)
                null,                           //method body (in string)
                Collections.EMPTY_LIST,         //parameter types, none
                Collections.EMPTY_LIST,         //parameters, none
                Collections.EMPTY_LIST,         //exceptions, none
                TestUtils.getTypeReference(testTargetPkg, "void"), //type reference
                0                               //dim count
                );
    }
    
    private Method createTearDown() {
        return this.testTargetPkg.getMethod().createMethod(
                "tearDown",                     //name of the test method
                Collections.EMPTY_LIST,         //list of annotations
                Modifier.PUBLIC,                //modifiers
                null,                           //javadoc text
                null,                           //javadoc
                null,                           //method body (in statement object)
                null,                           //method body (in string)
                Collections.EMPTY_LIST,         //parameter types, none
                Collections.EMPTY_LIST,         //parameters, none
                Collections.EMPTY_LIST,         //exceptions, none
                TestUtils.getTypeReference(testTargetPkg, "void"), //type reference
                0                               //dim count
                );
    }
    
    public FileObject[] generateTests(FileObject[] files2test) {
        Iterator it=null;
        LinkedList<FileObject> result=new LinkedList<FileObject>();
        LinkedList<JavaClass> testClasses=new LinkedList<JavaClass>();
        
        try {
            LinkedList classes2Test=new LinkedList();
            
            for (int i=0;i<files2test.length;i++) {
                if (files2test[i].isFolder()) {
                    Enumeration en=files2test[i].getData(false);
                    while (en.hasMoreElements()) {
                        FileObject fo=(FileObject) en.nextElement();
                        if (TestUtils.isTestable(fo)) {
                            classes2Test.addAll(TestUtils.getAllClassesFromFile(fo));
                            System.out.println("Adding classes from "+fo.getName()+" to test generation que.");
                        }
                    }
                } else {
                    if (TestUtils.isTestable(files2test[i])) {
                        classes2Test.addAll(TestUtils.getAllClassesFromFile(files2test[i]));
                        System.out.println("Adding classes from "+files2test[i].getName()+" to test generation que.");
                    }
                }
            }
            
            //System.out.println("classes to test: "+classes2Test.size()+" ref: "+classes2Test);
            it=classes2Test.iterator();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        
        while (it.hasNext()) {
            Resource r=null;
            boolean flag=false;
            
            JavaModel.getJavaRepository().beginTrans(true);
            try {
                JavaClass testedClass=(JavaClass) it.next();
                String pkgName=testedClass.getResource().getPackageName();
                r=testClassExists(testedClass, pkgName);
                if (r==null) {
                    System.out.println("Generating test for: "+testedClass.getName());
                    JavaClass testClass=generateTestClass(testedClass);
                    System.out.println("Done generating test class: "+testClass.getName());
                    //create a file from the JavaClass
                    r=testTargetPkg.getResource().createResource(
                            TestUtils.getFullTestClassFileName(testedClass),//test class file name//NOI18N
                            System.currentTimeMillis(),                     //timestamp
                            Collections.singletonList(testClass),           //list of clasifiers
                            generateImports(testedClass),                   //list of imports
                            pkgName,                                        //package name
                            null,                                           //Package identifier
                            Collections.EMPTY_LIST                          //annotations
                            );
                } else {
                    updateTestClass(r, testedClass);
                }
            } catch (Exception e) {
                flag=true;
                System.out.println(e.getMessage());
                e.printStackTrace();
            } finally {
                if (r!=null && !flag) {
                    result.add(JavaModel.getFileObject(r));
                    testClasses.add(getTestClassFromResource(r));
                    JavaModel.getJavaRepository().endTrans();
                } else
                    JavaModel.getJavaRepository().endTrans(true);
            }
        }
        result.addAll(generateTestRunnerMIDlets(testClasses));
        return (FileObject[]) result.toArray(new FileObject[result.size()]);
    }
    
    private JavaClass generateTestClass(JavaClass clazz) {
        int constructorIndex = 1;
        JavaClass result=null;
        
        List features = clazz.getFeatures();
        String testClassName = TestUtils.getTestClassName(clazz.getSimpleName());
        //start MDR transaction here
        JavaModel.getJavaRepository().beginTrans(true);
        boolean flag=false;
        try {
            if (features != null && features.size() > 0) {
                System.out.println("Parsing features");
                ArrayList testClassFeatures=new ArrayList(features.size());
                testClassFeatures.add(generateDefaultTestConstructor(testClassName));
                testClassFeatures.add(generateTestConstructor(testClassName));
                for (int i = 0; i < features.size(); i++) {
                    Object feature = features.get(i);
                    if (feature instanceof CallableFeature) {
                        CallableFeature callable = (CallableFeature) feature;
                        
                        if (!(callable instanceof Constructor)) {
                            Method methd=generateTestMethod(callable);
                            testClassFeatures.add(generateTestMethod(callable));
                        }
                    }
                }
                if (((Boolean) this.parameters.get(CreateTestParam.INC_SETUP)).booleanValue())
                    testClassFeatures.add(createSetUp());
                if (((Boolean) this.parameters.get(CreateTestParam.INC_TEAR_DOWN)).booleanValue())
                    testClassFeatures.add(createTearDown());
                //create the public suite method
                testClassFeatures.add(generateTestSuite(testClassFeatures,testClassName));
                //System.out.println("test class features: "+testClassFeatures);
                result=this.testTargetPkg.getJavaClass().createJavaClass(
                        testClassName,                      // test class name
                        Collections.EMPTY_LIST,             // annotations
                        Modifier.PUBLIC,                    // modifier
                        null,                               // javadoc text
                        null,                               // javadoc
                        testClassFeatures,                  // contents
                        testTargetPkg.getMultipartId().createMultipartId(
                        J2MEUNIT_SUPER_CLASS_NAME,
                        null,
                        Collections.EMPTY_LIST),            // super class name
                        Collections.EMPTY_LIST,             // implementing interfaces
                        Collections.EMPTY_LIST);            // type parameters
            }
        } catch (Exception e) {
            flag=true;
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            //end MDR transaction here
            if (result==null || flag)
                JavaModel.getJavaRepository().endTrans(true);
            else
                JavaModel.getJavaRepository().endTrans();
        }
        return result;
    }
    
    private Method generateTestMethod(CallableFeature callable) {
        String testMethodName=TestUtils.getTestMethodName(callable.getName());
        String testedClassName=callable.getDeclaringClass().getName();
        
        List testParameters=callable.getParameters();
        
        StringBuffer body=new StringBuffer();
        JavaDoc testMethodJavaDoc=null;
        
        //create JavaDoc if requested
        if (((Boolean) this.parameters.get(CreateTestParam.INC_JAVADOC)).booleanValue()) {
            testMethodJavaDoc=this.testTargetPkg.getJavaDoc().createJavaDoc(
                    NbBundle.getMessage(TestCreator.class,"PROP_src_code_javadoc", callable.getName(),testedClassName),//NOI18N
                    Collections.EMPTY_LIST
                    );
        }
        
        //create method body
        if (((Boolean) this.parameters.get(CreateTestParam.INC_METHOD_BODIES)).booleanValue()) {
            //create output line
            body.append("System.out.println(\""+callable.getName()+"\");");//NOI18N
            body.append("\n");//NOI18N
            //create variables corresponding to parameters
            Iterator it=testParameters.iterator();
            while (it.hasNext()) {
                Parameter p=(Parameter) it.next();
                body.append(TestUtils.getTypeNameString(p.getType())+" "+p.getName()+" = "  //NOI18N
                        +TestUtils.getDefaultValue(p.getType())+";\n"); //NOI18N
            }
            //create instance of the tested class variable
            if (callable.getDeclaringClass().getConstructor(Collections.EMPTY_LIST,false)!=null)
                //found parameter-less constructor create class instance
                body.append(testedClassName+" instance = new "+testedClassName+"();\n");//NOI18N
            else
                //only constructor with parameters found, set the instance to null like the JUnit generator
                body.append(testedClassName+" instance = null;\n");//NOI18N
            
            //create the prototype testing code TODO parametrize the expectedResult and other such variables
            String callableTypeName=TestUtils.getTypeNameString(callable.getType());
            if (!isVoidType(callable.getType())) {
                //generate test body appropriate for function
                body.append(callableTypeName+" expectedResult = "+TestUtils.getDefaultValue(callable.getType())+";\n");//NOI18N
                body.append(callableTypeName+" result = instance."+callable.getName()+"("+TestUtils.getParamString(testParameters)+");\n");//NOI18N
                body.append("assertEquals(expectedResult, result);\n");//NOI18N
                body.append("\n");//NOI18N
            } else {
                //generate test body appropriate for void
                body.append("instance."+callable.getName()+"("+TestUtils.getParamString(testParameters)+");\n");//NOI18N
                body.append("\n");//NOI18N
            }
            //create Code hint if requested
            if (((Boolean) this.parameters.get(CreateTestParam.INC_CODE_HINT)).booleanValue())
                body.append(NbBundle.getMessage(TestCreator.class, "PROP_src_code_sample_hint")+"\n");//NOI18N
            body.append("fail(\""+NbBundle.getMessage(TestCreator.class,"PROP_src_code_sample_msg")+"\");\n");//NOI18N
        }
        //create Code hint if requested, it is different when no method bodies are generated
        else if (((Boolean) this.parameters.get(CreateTestParam.INC_CODE_HINT)).booleanValue())
            body.append(NbBundle.getMessage(TestCreator.class,"PROP_src_code_hint"));//NOI18N
        
        Method result=testTargetPkg.getMethod().createMethod(
                testMethodName,                 //name of the test method
                Collections.EMPTY_LIST,         //list of annotations
                Modifier.PUBLIC,                //modifiers
                null,                           //javadoc text
                testMethodJavaDoc,              //javadoc
                null,                           //method body (in statement object)
                body.toString(),                //method body (in string)
                Collections.EMPTY_LIST,         //parameter types, none
                Collections.EMPTY_LIST,         //parameters, none
                Collections.EMPTY_LIST,         //exceptions, none
                TestUtils.getTypeReference(testTargetPkg, "void"), //type reference
                0                               //dim count
                );
        return result;
    }
    
    private Constructor generateDefaultTestConstructor(String testClassName) {
        return this.testTargetPkg.getConstructor().createConstructor(
                testClassName,                  // name
                Collections.EMPTY_LIST,         // annotations
                Modifier.PUBLIC,                // modifiers
                null,                           // Javadoc text
                null,                           // Javadoc - object
                null,                           // body - object
                "",                             // body - text  //NOI18N
                Collections.EMPTY_LIST,         // type parameters
                Collections.EMPTY_LIST,         // parameters
                null);                          // exception names
    }
    
    private Constructor generateTestConstructor(String testClassName) {
        Parameter p1 = this.testTargetPkg.getParameter().createParameter(
                "testName",
                Collections.EMPTY_LIST, // annotations
                false,                  // is final?
                TestUtils.getTypeReference(testTargetPkg, "String"), //NOI18N
                0,                      // dimCount
                false);                 // is not var.arg.
        
        Parameter p2 = this.testTargetPkg.getParameter().createParameter(
                "testMethod",
                Collections.EMPTY_LIST,
                false,
                TestUtils.getTypeReference(testTargetPkg, "TestMethod"),
                0,
                false);
        
        LinkedList parameterList=new LinkedList();
        parameterList.add(p1);parameterList.add(p2);
        
        return this.testTargetPkg.getConstructor().createConstructor(
                testClassName,                  // name
                Collections.EMPTY_LIST,         // annotations
                Modifier.PUBLIC,                // modifiers
                null,                           // Javadoc text
                null,                           // Javadoc - object
                null,                           // body - object
                "super(testName, testMethod);\n", // body - text  //NOI18N
                Collections.EMPTY_LIST,         // type parameters
                parameterList,                  // parameters
                null);                          // exception names
    }
    
    
    private LinkedList<Import> generateImports(JavaClass clazz) {
        LinkedList<Import> result=new LinkedList<Import>();
        
        try {
            //add framework import
            result.add(testTargetPkg.getImport().createImport(J2MEUNIT_FRAMEWORK_PACKAGE_NAME,null,false,true));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
    
    // private helper methods
    private boolean isVoidType(Type type) {
        return type instanceof PrimitiveType  &&  PrimitiveTypeKindEnum.VOID.equals(((PrimitiveType) type).getKind());
    }
    
    private void updateTestClass(Resource r, JavaClass testedClass) throws UnsupportedOperationException {
        Iterator it=r.getClassifiers().iterator();
        LinkedList<JavaClass> l=new LinkedList<JavaClass>();
        
        //parse the existing resource which corresponds to a test class which exists
        while (it.hasNext()) {
            Element e = (Element)it.next();
            if (e instanceof JavaClass) {
                l.add((JavaClass) e);
            }
        }
        if (l.size()>1)
            throw new UnsupportedOperationException("Test class files can contain only one class.");
        
        JavaClass testClass=l.getFirst();
        System.out.println("Updating test class: "+testClass.getName());
        
        List<Feature> features=testedClass.getFeatures();
        Iterator tIt=features.iterator();
        
        //parse the class for which we are updating the test class
        JavaModel.getJavaRepository().beginTrans(true);
        boolean flag=false;
        try {
            //remove the original suite() method
            Method suiteMethod=testClass.getMethod("suite",Collections.EMPTY_LIST,false);
            if (suiteMethod!=null)
                testClass.getFeatures().remove(suiteMethod);
            //add any new test methods
            while (tIt.hasNext()) {
                Feature f=(Feature) tIt.next();
                if (f instanceof CallableFeature && !(f instanceof Constructor)) {
                    Method m=generateTestMethod((CallableFeature) f);
                    if (testClass.getMethod(m.getName(),m.getParameters(), false)==null) {
                        System.out.println("Adding method: "+m.getName());
                        testClass.getContents().add(m);
                    } else
                        System.out.println("Skipping method: "+m.getName());
                }
            }
            //remove any test methods that do not have original methods
            List<Feature> testClassFeatures=testClass.getFeatures();
            List<Feature> removeList=new LinkedList<Feature>();
            int fl=testClassFeatures.size();
            
            for (int i=0;i<fl;i++) {
                Feature f=testClassFeatures.get(i);
                if (f instanceof Method) {
                    Method tm=(Method) f;
                    String testMethodName=tm.getName();
                    int j=0;
                    for (;j<testedClass.getFeatures().size();j++) {
                        if (testedClass.getFeatures().get(j) instanceof Method) {
                            Method om=(Method) testedClass.getFeatures().get(j);
                            String origMethodName=om.getName();
                            if (origMethodName.equals(TestUtils.getOriginalMethodName(testMethodName)))
                                break;
                        }
                    }
                    if (j==testedClass.getFeatures().size()) {
                        System.out.println("Queing test method for removal: "+testMethodName);
                        removeList.add(f);
                    }
                }
            }
            //now safely remove unavailable methods
            if (removeList.size()>0)
                testClassFeatures.removeAll(removeList);
            //now all test methods have been updated
            testClass.getContents().add(generateTestSuite(testClass.getFeatures(),testClass.getSimpleName()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            flag=true;
        } finally {
            if (flag)
                //there was a problem, rollback
                JavaModel.getJavaRepository().endTrans(true);
            else
                //all good
                JavaModel.getJavaRepository().endTrans();
        }
    }
    
    private Method generateTestSuite(List testClassFeatures, String testClassName) {
        StringBuffer body=new StringBuffer();
        Method result=null;
        String warning="Do not modify this code, it is automatically generated and rewritten\n" +
                " when Create JUnit Test action is invoked.  Any changes you make will be lost.";
        Iterator it=testClassFeatures.iterator();
        
        JavaModel.getJavaRepository().beginTrans(false);
        try {
            body.append("TestSuite suite = new TestSuite();\n\n");
            while (it.hasNext()) {
                Feature f=(Feature) it.next();
                if (f instanceof Method) {
                    Method m=(Method) f;
                    body.append("suite.addTest(new "+testClassName+"(\""+m.getName()+"\", new TestMethod()");
                    body.append("{ public void run(TestCase tc) {(("+testClassName+") tc)."+m.getName()+"();}}));\n");
                }
            }
            body.append("return suite;\n");
            
            result=this.testTargetPkg.getMethod().createMethod(
                    "suite",                                            //name
                    Collections.EMPTY_LIST,                             //annotations
                    Modifier.PUBLIC,                                    //modifiers
                    null,                                               //javadoc text
                    null,                                               //javadoc
                    null,                                               //statement body
                    body.toString(),                                    //body text
                    Collections.EMPTY_LIST,                             //type parameters
                    Collections.EMPTY_LIST,                             //parameters
                    Collections.EMPTY_LIST,                             //exceptions
                    TestUtils.getTypeReference(testTargetPkg, "Test"),  //type reference
                    0                               //dim count
                    );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            if (result==null)
                JavaModel.getJavaRepository().endTrans(true);
            else
                JavaModel.getJavaRepository().endTrans();
        }
        return result;
    }
    
    private List createTestRunnerImports() {
        LinkedList<Import> result=new LinkedList<Import>();
        
        try {
            //add framework import
            result.add(testTargetPkg.getImport().createImport("j2meunit.midletui",null,false,true));//TODO add to bundle
            //add LCDUI stuff
            result.add(testTargetPkg.getImport().createImport("javax.microedition.lcdui",null,false,true));//TODO add to bundle
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
    
    private List createTestRunnerMethods(String testClassList) {
        LinkedList<Method> result=new LinkedList<Method>();
        
        Method startAppMethod=this.testTargetPkg.getMethod().createMethod(
                "startApp",                                         //name
                Collections.EMPTY_LIST,                             //annotations
                Modifier.PUBLIC,                                    //modifiers
                null,                                               //javadoc text
                null,                                               //javadoc
                null,                                               //statement body
                "this.start(new String[] {"+testClassList+"});\n",  //method body
                Collections.EMPTY_LIST,                             //type parameters
                Collections.EMPTY_LIST,                             //parameters
                Collections.EMPTY_LIST,                             //exceptions
                TestUtils.getTypeReference(testTargetPkg, "void"),  //type reference
                0                                                   //dim count
                );
        
        Method pauseAppMethod=this.testTargetPkg.getMethod().createMethod(
                "pauseApp",                                         //name
                Collections.EMPTY_LIST,                             //annotations
                Modifier.PUBLIC,                                    //modifiers
                null,                                               //javadoc text
                null,                                               //javadoc
                null,                                               //statement body
                "",                                                 //empty method body
                Collections.EMPTY_LIST,                             //type parameters
                Collections.EMPTY_LIST,                             //parameters
                Collections.EMPTY_LIST,                             //exceptions
                TestUtils.getTypeReference(testTargetPkg, "void"),  //type reference
                0                               //dim count
                );
        
        Parameter p=this.testTargetPkg.getParameter().createParameter(
                "unconditional",
                Collections.EMPTY_LIST,
                false,
                TestUtils.getTypeReference(testTargetPkg, "boolean"),
                0,
                false);
        
        Method destroyAppMethod=this.testTargetPkg.getMethod().createMethod(
                "destroyApp",                                       //name
                Collections.EMPTY_LIST,                             //annotations
                Modifier.PUBLIC,                                    //modifiers
                null,                                               //javadoc text
                null,                                               //javadoc
                null,                                               //statement body
                "System.gc();\n",                                   //empty method body
                Collections.EMPTY_LIST,                             //type parameters
                Collections.singletonList(p),                       //parameters
                Collections.EMPTY_LIST,                             //exceptions
                TestUtils.getTypeReference(testTargetPkg, "void"),  //type reference
                0                               //dim count
                );
        
        result.add(startAppMethod);result.add(pauseAppMethod);result.add(destroyAppMethod);
        return result;
    }
    
    private JavaClass getTestClassFromResource(Resource r) {
        Iterator it=r.getClassifiers().iterator();
        
        while (it.hasNext()) {
            Object o=it.next();
            if (o instanceof JavaClass) {
                JavaClass c=(JavaClass) o;
                if (c.getSimpleName().endsWith(TestUtils.TEST_CLASSNAME_SUFFIX))
                    return c;
            }
        }
        return null;
    }
    
    private List<FileObject> generateTestRunnerMIDlets(LinkedList<JavaClass> testClasses) {
        LinkedList<FileObject> result=new LinkedList<FileObject>();
        /*sort packages and test classes into a map, key is pkgname and contents is string
        listing test classes present in that package */
        Iterator clzIt=testClasses.iterator();
        Map<String,String> packages=new HashMap<String,String>();
        
        while (clzIt.hasNext()) {
            JavaClass testClass=(JavaClass) clzIt.next();
            String clsName=testClass.getSimpleName();
            String pkgName=testClass.getResource().getPackageName();
            if (pkgName!=null && !pkgName.equals(""))
                clsName=pkgName+"."+clsName;
            String clsList=packages.get(pkgName);
            if (clsList!=null && !clsList.equals(""))
                clsList=clsList+",\""+clsName+"\"";
            else
                clsList="\""+clsName+"\"";
            packages.put(pkgName,clsList);
        }
        //check if test runner MIDlet exists and regenerate its contents if it does, otherwise create a new one
        
        Iterator pkgIt=packages.keySet().iterator();
        
        while (pkgIt.hasNext()) {
            String pkgName=(String) pkgIt.next();
            String testRunnerClassName="";
            if (pkgName!=null && !pkgName.equals(""))
                testRunnerClassName=pkgName.replace('.','/')+"/TestRunnerMIDlet.java";
            else
                testRunnerClassName="TestRunnerMIDlet.java";
            Resource res=JavaModel.getResource(this.testSourceRoot,testRunnerClassName);
            if (res!=null) {
                //test runner for this package exists, update it
                boolean flag=false;
                JavaModel.getJavaRepository().beginTrans(true);
                try {
                    List classifiers=res.getClassifiers();
                    Iterator clsIt=classifiers.iterator();
                    while (clsIt.hasNext()) {
                        JavaClass c=null;
                        Object o=clsIt.next();
                        if (o instanceof JavaClass)
                            c=(JavaClass) o;
                        if (c!=null) {
                            c.getFeatures().clear();
                            c.getFeatures().addAll(createTestRunnerMethods(packages.get(pkgName)));
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    flag=true;
                } finally {
                    if (flag)
                        JavaModel.getJavaRepository().endTrans(true);
                    else
                        JavaModel.getJavaRepository().endTrans();
                }
            } else {
                //test runner MIDlet for this package does not exist, create it
                try {
                    JavaModel.getJavaRepository().beginTrans(true);
                    JavaClass runnerClass=this.testTargetPkg.getJavaClass().createJavaClass(
                            "TestRunnerMIDlet",                                     // test class name
                            Collections.EMPTY_LIST,                                 // annotations
                            Modifier.PUBLIC,                                        // modifier
                            null,                                                   // javadoc text
                            null,                                                   // javadoc
                            createTestRunnerMethods(packages.get(pkgName)),         // contents
                            testTargetPkg.getMultipartId().createMultipartId(
                            "TestRunner",                                           //TODO put it into bundle
                            null,
                            Collections.EMPTY_LIST),                                // super class name
                            Collections.EMPTY_LIST,                                 // implementing interfaces
                            Collections.EMPTY_LIST                                  // type parameters
                            );
                    
                    res=this.testTargetPkg.getResource().createResource(
                            testRunnerClassName,                                //test class file name
                            System.currentTimeMillis(),                         //timestamp
                            Collections.singletonList(runnerClass),             //list of clasifiers
                            createTestRunnerImports(),                          //list of imports
                            pkgName,                                            //package name
                            null,                                               //Package identifier
                            Collections.EMPTY_LIST                              //annotations
                            );
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                } finally {
                    if (res==null)
                        JavaModel.getJavaRepository().endTrans(true);
                    else {
                        JavaModel.getJavaRepository().endTrans();
                        result.add(JavaModel.getFileObject(res));
                    }
                }
            }
        }
        if (result.size()!=0)
            return result;
        else
            return Collections.EMPTY_LIST;
    }
    
}
