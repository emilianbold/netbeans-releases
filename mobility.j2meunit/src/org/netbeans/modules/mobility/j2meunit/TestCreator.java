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



import com.sun.source.util.TreePath;

import com.sun.source.util.SourcePositions;

import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;



import javax.lang.model.element.Element;

import javax.lang.model.element.TypeElement;



import org.netbeans.api.java.source.JavaSource;



import java.io.IOException;

import java.util.*;

import javax.lang.model.element.ElementKind;

import javax.lang.model.element.ExecutableElement;

import javax.lang.model.element.Modifier;

import javax.lang.model.element.VariableElement;

import javax.lang.model.type.TypeKind;

import javax.lang.model.type.TypeMirror;

import javax.lang.model.util.ElementFilter;

import javax.lang.model.util.Elements;

import javax.lang.model.util.Types;



import org.netbeans.api.java.classpath.ClassPath;

import org.netbeans.api.java.source.CancellableTask;

import org.netbeans.api.java.source.ClasspathInfo;

import org.netbeans.api.java.source.Comment;

import org.netbeans.api.java.source.CompilationInfo;

import org.netbeans.api.java.source.ElementHandle;

import org.netbeans.api.java.source.JavaSource.Phase;

import org.netbeans.api.java.source.ModificationResult;

import org.netbeans.api.java.source.TreeMaker;

import org.netbeans.api.java.source.TreeUtilities;

import org.netbeans.api.java.source.WorkingCopy;

import org.netbeans.api.project.Project;

import org.netbeans.spi.project.support.ant.AntProjectHelper;

import org.openide.filesystems.FileObject;

import org.netbeans.modules.junit.plugin.JUnitPlugin.CreateTestParam;

import org.netbeans.spi.java.classpath.support.ClassPathSupport;

import org.openide.ErrorManager;

import org.openide.cookies.SaveCookie;

import org.openide.filesystems.Repository;

import org.openide.loaders.DataFolder;

import org.openide.loaders.DataObject;

import org.openide.loaders.DataObjectNotFoundException;

import org.openide.util.NbBundle;



import static javax.lang.model.element.Modifier.ABSTRACT;

import static javax.lang.model.element.Modifier.PRIVATE;

import static javax.lang.model.element.Modifier.PROTECTED;

import static javax.lang.model.element.Modifier.PUBLIC;

import static javax.lang.model.element.Modifier.STATIC;



/**

 * This is a J2MEUnit test generator used by the JUnit plugin API

 *

 * @author bohemius

 */

public class TestCreator {



    private Map<CreateTestParam, Object> parameters;

    private FileObject testSourceRoot;

    private Project mProject;

    private AntProjectHelper aph;



    static private final String J2MEUNIT_SUPER_CLASS_NAME = "TestCase";//NOI18N

    static private final String J2MEUNIT_FRAMEWORK_PACKAGE_NAME = "jmunit.framework.cldc10";//NOI18N



    static private final String METHOD_NAME_SETUP = "setUp";            //NOI18N

    static private final String METHOD_NAME_TEARDOWN = "tearDown";      //NOI18N

    static private final String CLASS_COMMENT_LINE1 = "TestCreator.javaClass.addTestsHereComment.l1";

    static private final String CLASS_COMMENT_LINE2 = "TestCreator.javaClass.addTestsHereComment.l2";



    private static final String INSTANCE_VAR_NAME = "instance";         //NOI18N

    private static final String RESULT_VAR_NAME = "result";             //NOI18N

    private static final String EXP_RESULT_VAR_NAME = "expResult";      //NOI18N

    private static final String ARTIFICAL_VAR_NAME_BASE = "arg";        //NOI18N



    private static final EnumSet<Modifier> ACCESS_MODIFIERS

            = EnumSet.of(Modifier.PUBLIC,

            Modifier.PROTECTED,

            Modifier.PRIVATE);



    private static final EnumSet<Modifier> NO_MODIFIERS

            = EnumSet.noneOf(Modifier.class);



    private boolean skipPkgPrivateClasses = false;

    private boolean skipAbstractClasses = false;

    private boolean skipExceptionClasses = false;

    private Set<Modifier> methodAccessModifiers

            = TestUtils.createModifierSet(Modifier.PUBLIC,

            Modifier.PROTECTED);



    private boolean testPkgPrivateMethods = true;

    private boolean generateDefMethodBody = true;

    private boolean generateMethodJavadoc = true;

    private boolean generateSourceCodeHints = true;

    private boolean generateSetUp = true;

    private boolean generateTearDown = true;

    private String initialMainMethodBody;



    /**

     * Creates a new instance of TestCreator

     */

    public TestCreator(Map<CreateTestParam, Object> params, FileObject testTargetRoot, Project p, AntProjectHelper aph) {

        this.parameters = params;

        this.testSourceRoot = testTargetRoot;

        this.aph = aph;

        this.mProject = p;



        skipPkgPrivateClasses = !Boolean.TRUE.equals(params.get(

                CreateTestParam.INC_PKG_PRIVATE_CLASS));

        skipAbstractClasses = !Boolean.TRUE.equals(params.get(

                CreateTestParam.INC_ABSTRACT_CLASS));

        skipExceptionClasses = !Boolean.TRUE.equals(params.get(

                CreateTestParam.INC_EXCEPTION_CLASS));

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

    }



    public void setSkipPackagePrivateClasses(boolean skip) {

        this.skipPkgPrivateClasses = skip;

    }



    public void setSkipAbstractClasses(boolean skip) {

        this.skipAbstractClasses = skip;

    }



    public void setSkipExceptionClasses(boolean skip) {

        this.skipExceptionClasses = skip;

    }



    public void setTestPublicMethods(boolean test) {

        if (test) {

            methodAccessModifiers.add(Modifier.PUBLIC);

        } else {

            methodAccessModifiers.remove(Modifier.PUBLIC);

        }

    }



    public void setTestProtectedMethods(boolean test) {

        if (test) {

            methodAccessModifiers.add(Modifier.PROTECTED);

        } else {

            methodAccessModifiers.remove(Modifier.PROTECTED);

        }

    }



    public void setTestPackagePrivateMethods(boolean test) {

        this.testPkgPrivateMethods = test;

    }



    public void setGenerateDefMethodBody(boolean generate) {

        this.generateDefMethodBody = generate;

    }



    public void setGenerateMethodJavadoc(boolean generate) {

        this.generateMethodJavadoc = generate;

    }



    public void setGenerateMethodBodyComment(boolean generate) {

        this.generateSourceCodeHints = generate;

    }



    public void setGenerateSetUp(boolean generate) {

        this.generateSetUp = generate;

    }



    public void setGenerateTearDown(boolean generate) {

        this.generateTearDown = generate;

    }



    public FileObject[] generateTests(final FileObject[] files2test) {

        LinkedList<FileObject> result = new LinkedList();

        Enumeration<? extends FileObject> dataFiles=Collections.enumeration(Arrays.asList(files2test));;





        while (dataFiles.hasMoreElements()) {

            FileObject dataFile=dataFiles.nextElement();

            if (dataFile.isFolder()) {

                Enumeration<? extends FileObject> testableFiles=dataFile.getData(true);

                while (testableFiles.hasMoreElements()) {

                    FileObject testableFile=testableFiles.nextElement();

                    if (TestUtils.isTestable(testableFile))

                        result.addAll(generateFromSingleSource(testableFile));

                }

            } else if (TestUtils.isTestable(dataFile))

                result.addAll(generateFromSingleSource(dataFile));

        }





        return result.toArray(new FileObject[result.size()]);

    }



    private List<FileObject> generateFromFolder(FileObject folder2test) {

        assert folder2test.isFolder();



        LinkedList<FileObject> result=new LinkedList();

        Enumeration<? extends FileObject> dataFiles=folder2test.getData(true);



        return null;

    }



    private List<FileObject> generateFromSingleSource(FileObject file2test) {

        ClassPath testClassPath = ClassPathSupport.createClassPath(new FileObject[]{this.testSourceRoot});

        LinkedList<FileObject> result = new LinkedList();

        List<ElementHandle<TypeElement>> testable = null;



        try {

            JavaSource javaSource = JavaSource.forFileObject(file2test);

            testable = TestUtils.findTopClasses(javaSource);

        } catch (IOException ex) {

            System.out.println(ex.getMessage());

            ex.printStackTrace();

        }



        if (testable != null && !testable.isEmpty()) {

            String packageName = TestUtils.getPackageName(ClassPath.getClassPath(file2test, ClassPath.SOURCE).getResourceName(file2test, '.', false));



            try {

                for (ElementHandle<TypeElement> clsToTest : testable) {

                    String srcClassNameShort = TestUtils.getSimpleName(clsToTest.getQualifiedName());

                    String testClassResourceName = TestUtils.getTestClassFullName(srcClassNameShort, packageName);



                    /* find or create the test class DataObject: */

                    DataObject testDataObj = null;

                    FileObject testFileObj = testClassPath.findResource(testClassResourceName + ".java");//NOI18N

                    boolean isNew = (testFileObj == null);

                    if (testFileObj == null) {

                        testDataObj = createTestClassDataObj(testClassResourceName, this.loadTestTemplate("PROP_emptyTestClassTemplate"));//NOI18N

                        testFileObj = testDataObj.getPrimaryFile();

                    }



                    JavaSource testSource = JavaSource.forFileObject(testFileObj);

                    SingleTestCreator testCreator = new SingleTestCreator(Collections.singletonList(clsToTest), isNew);

                    ModificationResult mResult = testSource.runModificationTask(testCreator);

                    mResult.commit();

                    if (testDataObj == null) {

                        testDataObj = DataObject.find(testFileObj);

                    }

                    SaveCookie sc = testDataObj.getCookie(SaveCookie.class);

                    if (sc != null) {

                        sc.save();

                    }

                    result.add(testFileObj);

                    // add the test class to the JMUnitTestClasses property

                    TestUtils.addTestClassProperty(this.mProject, this.aph, packageName + "." + TestUtils.getTestClassName(srcClassNameShort));



                }

            } catch (IOException ex) {

                System.out.println(ex.getMessage());

                ex.printStackTrace();

            }

        }

        return result;

    }



    private DataObject createTestClassDataObj(String testClassName,

                                              DataObject templateDataObj) throws DataObjectNotFoundException, IOException {



        int index = testClassName.lastIndexOf('/');

        String className = index > -1 ? testClassName.substring(index + 1) : testClassName;

        FileObject packageFO = index > -1 ? this.testSourceRoot.getFileObject(testClassName.substring(0,index)) : this.testSourceRoot;



        // instantiate template into the package

        return templateDataObj.createFromTemplate(DataFolder.findFolder(packageFO), className);

    }



    private DataObject loadTestTemplate(String templateID) {

        String path = NbBundle.getMessage(TestCreator.class, templateID);

        try {

            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(path);

            if (fo == null) {

                System.out.println("Cannot find template.");

                return null;

            }

            return DataObject.find(fo);

        } catch (DataObjectNotFoundException e) {

            System.out.println(e.getMessage());

            e.printStackTrace();

            return null;

        }

    }



    private final class SingleTestCreator implements CancellableTask<WorkingCopy> {



        private final List<ElementHandle<TypeElement>> srcTopClassElemHandles;

        private final boolean isNewTestClass;

        private List<String> processedClassNames;

        private volatile boolean cancelled = false;



        private TypeElement testCaseTypeElem;



        private SingleTestCreator() {

            this.srcTopClassElemHandles = null;

            this.isNewTestClass = true;   //value not used

        }



        private SingleTestCreator(

                List<ElementHandle<TypeElement>> srcTopClassHandles,

                boolean isNewTestClass) {

            this.srcTopClassElemHandles = srcTopClassHandles;

            this.isNewTestClass = isNewTestClass;

        }



        public void run(WorkingCopy workingCopy) throws IOException {

            final String className = workingCopy.getClasspathInfo()

                    .getClassPath(ClasspathInfo.PathKind.SOURCE)

                    .getResourceName(workingCopy.getFileObject(), '.', false);



            workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);



            CompilationUnitTree compUnit = workingCopy.getCompilationUnit();

            List<ClassTree> tstTopClasses = TestUtils.findTopClasses(compUnit, workingCopy.getTreeUtilities());



            List<TypeElement> srcTopClassElems

                    = resolveHandles(workingCopy, srcTopClassElemHandles);



            TreePath compUnitPath = new TreePath(compUnit);



            if ((srcTopClassElems != null) && !srcTopClassElems.isEmpty()) {



                /* Create/update a test class for each testable source class: */

                for (TypeElement srcTopClass : srcTopClassElems) {

                    String srcClassName = srcTopClass.getSimpleName().toString();

                    String tstClassName = TestUtils.getTestClassName(srcClassName);



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

                                || tstClassName.equals(TestUtils.getSimpleName(className))) {

                            tstTopClass = generateNewTestClass(workingCopy,

                                    tstClassName,

                                    srcTopClass,

                                    srcMethods);

                            //PENDING - add the top class to the CompilationUnit

                        }

                    }

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

                List<Tree> mMembers = new ArrayList<Tree>(testMethods.size() + 1);

                mMembers.addAll(testMethods);

                mMembers.add(generateOverrideTestMethod(workingCopy, testMethods));

                members = mMembers;

            } else if (testMethods.isEmpty()) {

                members = initMembers;

            } else {

                List<Tree> allMembers = new ArrayList<Tree>(

                        initMembers.size() + testMethods.size());

                allMembers.addAll(initMembers);

                allMembers.addAll(testMethods);

                allMembers.add(generateOverrideTestMethod(workingCopy, testMethods));



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



        private ClassTree generateMissingInitMembers(ClassTree tstClass, TreePath tstClassTreePath, WorkingCopy workingCopy) {

            if (!generateSetUp && !generateTearDown) {

                return tstClass;

            }



            if ((!generateSetUp || TestUtils.hasSetUp(tstClass))

                    && (!generateTearDown || TestUtils.hasTearDown(tstClass))) {

                return tstClass;

            }



            final TreeMaker maker = workingCopy.getTreeMaker();



            List<? extends Tree> tstMembersOrig = tstClass.getMembers();

            List<Tree> tstMembers = new ArrayList<Tree>(tstMembersOrig.size() + 2);

            tstMembers.addAll(tstMembersOrig);



            if (generateSetUp && !TestUtils.hasSetUp(tstClass)) {

                tstMembers.add(generateInitMethod(maker, "setUp"));//NOI18N

            }



            if (generateTearDown && !TestUtils.hasTearDown(tstClass)) {

                tstMembers.add(generateInitMethod(maker, "tearDown"));//NOI18N

            }



            ClassTree newClass = maker.Class(

                    tstClass.getModifiers(),

                    tstClass.getSimpleName(),

                    tstClass.getTypeParameters(),

                    tstClass.getExtendsClause(),

                    (List<? extends ExpressionTree>) tstClass.getImplementsClause(),

                    tstMembers);

            return newClass;

        }



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



            List<? extends Tree> tstMembersOrig = tstClass.getMembers();

            List<Tree> tstMembers = new ArrayList<Tree>(tstMembersOrig.size() + 4);

            tstMembers.addAll(tstMembersOrig);



            if (generateMissingInitMembers) {

                generateMissingInitMembers(tstClass, tstClassTreePath, workingCopy);

            }



            Boolean useNoArgConstrutor = null;

            for (ExecutableElement srcMethod : srcMethods) {

                String testMethodName = TestUtils.createTestMethodName(

                        srcMethod.getSimpleName().toString());

                if (TestUtils.testMethodExists(tstClass, testMethodName)) {

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

            }



            if (tstMembers.size() == tstMembersOrig.size()) {  //no test method added

                return tstClass;

            } else {

                List<MethodTree> testMethods = new LinkedList<MethodTree>();

                for (Tree member : tstMembers) {

                    if (member.getKind() == Tree.Kind.METHOD) {

                        MethodTree testMethod = (MethodTree) member;

                        if (TestUtils.isTestMethod(testMethod))

                            testMethods.add(testMethod);

                    }

                }

                tstMembers.add(generateOverrideTestMethod(workingCopy, testMethods));

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



        private List<MethodTree> generateTestMethods(WorkingCopy workingCopy, TypeElement srcClass, List<ExecutableElement> srcMethods) {

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



        private MethodTree generateOverrideTestMethod(WorkingCopy workingCopy, List<MethodTree> testMethods) {

            final TreeMaker maker = workingCopy.getTreeMaker();

            final ClassTree clsTree = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);



            //prepare method body

            StringBuffer methodBody = new StringBuffer("{\nswitch(" + NbBundle.getMessage(TestCreator.class, "PROP_generator_override_test_method_param") + ") {\n");//NOI18N

            int i = 0;

            for (MethodTree testMethod : testMethods) {

                methodBody.append("case " + i + ":" + testMethod.getName().toString() + "();break;\n");//NOI18N

                i++;

            }

            methodBody.append("default: break;\n}\n}\n");//NOI18N



            MethodTree mMethod = null;

            for (Tree member : clsTree.getMembers()) {

                if (member.getKind() == Tree.Kind.METHOD) {

                    mMethod = (MethodTree) member;

                    if (mMethod.getName().toString().equals(NbBundle.getMessage(TestCreator.class, "PROP_generator_override_test_method")))//NOI18N

                        break;

                    else

                        mMethod = null;

                }

            }



            List<ExpressionTree> throwsList = Collections.<ExpressionTree>singletonList(

                    maker.Identifier(NbBundle.getMessage(TestCreator.class, "PROP_generator_throwable")));//NOI18N

            ModifiersTree parameterModifiers = maker.Modifiers(Collections.<Modifier>emptySet(),

                    Collections.<AnnotationTree>emptyList());

            VariableTree parameter = maker.Variable(parameterModifiers,

                    NbBundle.getMessage(TestCreator.class, "PROP_generator_override_test_method_param"),//NOI18N

                    maker.PrimitiveType(TypeKind.INT),

                    null);



            if (mMethod == null) {

                mMethod = maker.Method(

                        maker.Modifiers(TestUtils.createModifierSet(PUBLIC)),

                        NbBundle.getMessage(TestCreator.class, "PROP_generator_override_test_method"),

                        maker.PrimitiveType(TypeKind.VOID),

                        Collections.<TypeParameterTree>emptyList(),

                        Collections.<VariableTree>singletonList(parameter),

                        throwsList,

                        methodBody.toString(),

                        null);

                workingCopy.rewrite(clsTree, maker.addClassMember(clsTree, mMethod));

            } else {

                BlockTree mBlockTree = maker.createMethodBody(mMethod, methodBody.toString());

                workingCopy.rewrite(mMethod.getBody(), mBlockTree);

            }

            updateTestClassConctructor(workingCopy,i);



            return mMethod;

        }



        private void updateTestClassConctructor(WorkingCopy workingCopy, int nTests) {

            final ClassTree clsTree = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);

            final TreeMaker maker=workingCopy.getTreeMaker();



            MethodTree consMethod = null;

            for (Tree member : clsTree.getMembers()) {

                if (member.getKind() == Tree.Kind.METHOD) {

                    consMethod = (MethodTree) member;

                    if (consMethod.getName().toString().equals("<init>"))//NOI18N

                        break;

                }

            }



            String consBody="{\nsuper("+nTests+",\""+clsTree.getSimpleName()+"\");\n}\n";

            BlockTree consBlock=consMethod.getBody();

            TreeUtilities treeUtils=workingCopy.getTreeUtilities();

            Tree newBlock=treeUtils.parseStatement(consBody,new SourcePositions[1]);

            assert Tree.Kind.BLOCK == newBlock.getKind();

            workingCopy.rewrite(consBlock,newBlock);

        }



        private MethodTree generateTestMethod(WorkingCopy workingCopy, TypeElement srcClass, ExecutableElement srcMethod, boolean useNoArgConstructor) {

            final TreeMaker maker = workingCopy.getTreeMaker();



            String testMethodName = TestUtils.createTestMethodName(srcMethod.getSimpleName().toString());

            ModifiersTree modifiers = maker.Modifiers(TestUtils.createModifierSet(PUBLIC));

            List<ExpressionTree> throwsList = new LinkedList();



            throwsList.add(maker.Identifier(NbBundle.getMessage(TestCreator.class, "PROP_generator_test_method_exception")));//NOI18N

            if (throwsNonRuntimeExceptions(workingCopy, srcMethod)) {

                throwsList.add(maker.Identifier(NbBundle.getMessage(TestCreator.class, "PROP_generator_nonrte"))); //NOI18N

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

                                "PROP_src_code_javadoc",   //NOI18N

                                srcMethod.getSimpleName().toString(),

                                srcClass.getSimpleName().toString()));

                maker.addComment(method, javadoc, false);

            }



            return method;

        }



        private MethodTree generateSuiteMethod(String suiteName,

                                               List<String> members,

                                               WorkingCopy workingCopy) {

            final Types types = workingCopy.getTypes();

            final Elements elements = workingCopy.getElements();

            final TreeMaker maker = workingCopy.getTreeMaker();



            List<StatementTree> bodyContent

                    = new ArrayList<StatementTree>(members.size() + 2);



            return maker.Method(

                    maker.Modifiers(TestUtils.createModifierSet(PUBLIC)),

                    "test",

                    maker.PrimitiveType(TypeKind.VOID),        //return type

                    Collections.<TypeParameterTree>emptyList(),//type params

                    Collections.<VariableTree>emptyList(),     //params

                    Collections.<ExpressionTree>emptyList(),   //throws-list

                    maker.Block(bodyContent, false),           //body

                    null);  //def. value - only for annotations

        }



        private BlockTree generateTestMethodBody(WorkingCopy workingCopy, TypeElement srcClass, ExecutableElement srcMethod, boolean useNoArgConstructor) {

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

/*                    ExpressionTree retTypeTree = retTypeKind.isPrimitive()

                            ? maker.Identifier(retType.toString())

                            : maker.QualIdent(

                            workingCopy.getTypes().asElement(retType));*/

                    Tree retTypeTree=maker.Type(retType);



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

                    

                    MethodInvocationTree comparison;
                    if (retTypeKind == TypeKind.FLOAT || retTypeKind == TypeKind.DOUBLE)
                    {
                        comparisonArgs.add(maker.Binary(Kind.EQUAL_TO,
                            maker.Identifier(expectedValue.getName().toString()),
                            maker.Identifier(actualValue.getName().toString())));
                        comparison = maker.MethodInvocation(
                                Collections.<ExpressionTree>emptyList(),//type args.
                                maker.Identifier("assertTrue"),           //NOI18N
                                comparisonArgs);
                    }
                    else
                    {
                        comparisonArgs.add(maker.Identifier(expectedValue.getName().toString()));
                        comparisonArgs.add(maker.Identifier(actualValue.getName().toString()));
                        comparison = maker.MethodInvocation(
                                Collections.<ExpressionTree>emptyList(),//type args.
                                maker.Identifier("assertEquals"),           //NOI18N
                                comparisonArgs);
                    }

                    StatementTree comparisonStmt = maker.ExpressionStatement(

                            comparison);



                    statements.add(expectedValue);

                    statements.add(actualValue);

                    statements.add(comparisonStmt);

                }

            }



            if (generateDefMethodBody) {

                String failMsg = NbBundle.getMessage(

                        TestCreator.class,

                        "PROP_src_code_sample_msg");   //NOI18N

                MethodInvocationTree failMethodCall = maker.MethodInvocation(

                        Collections.<ExpressionTree>emptyList(),    //type args.

                        maker.Identifier("fail"),                       //NOI18N

                        Collections.<ExpressionTree>singletonList(

                                maker.Literal(failMsg)));

                statements.add(maker.ExpressionStatement(failMethodCall));

            }



            return maker.Block(statements, false);

        }



        private StatementTree generateSystemOutPrintln(TreeMaker maker, String arg) {

            MethodInvocationTree methodInvocation = maker.MethodInvocation(

                    Collections.<ExpressionTree>emptyList(),        //type args

                    maker.MemberSelect(

                            maker.MemberSelect(

                                    maker.Identifier("System"), "out"), "println"),//NOI18N

                    Collections.<LiteralTree>singletonList(

                            maker.Literal(arg)));                   //args.

            return maker.ExpressionStatement(methodInvocation);

        }



        private List<VariableTree> generateParamVariables(TreeMaker maker, ExecutableElement srcMethod) {

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



        private List<IdentifierTree> createIdentifiers(TreeMaker maker, List<VariableTree> variables) {

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



        private String[] getTestSkeletonVarNames(final List<? extends VariableElement> sourceMethodParams) {



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

                                paramName = (paramNamePrefix + (index++)))) ;

                        varNames[i] = paramName;

                    }

                }

            }



            return varNames;

        }



        private ExpressionTree getDefaultValue(TreeMaker maker, TypeMirror type) {

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



        private ExpressionTree generateNoArgConstructorCall(TreeMaker maker, TypeElement cls) {

            return maker.NewClass(

                    null,                                   //enclosing instance

                    Collections.<ExpressionTree>emptyList(),//type arguments

                    maker.QualIdent(cls),                   //class identifier

                    Collections.<ExpressionTree>emptyList(),//arguments list

                    null);                                  //class body

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



        private boolean isTestableMethod(ExecutableElement method) {

            if (method.getKind() != ElementKind.METHOD) {

                throw new IllegalArgumentException();

            }



            return isMethodAcceptable(method);

        }



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



        private boolean throwsNonRuntimeExceptions(CompilationInfo compInfo, ExecutableElement method) {

            List<? extends TypeMirror> thrownTypes = method.getThrownTypes();

            if (thrownTypes.isEmpty()) {

                return false;

            }



            String runtimeExcName = "java.lang.RuntimeException";       //NOI18N

            TypeElement runtimeExcElement = compInfo.getElements().getTypeElement(runtimeExcName);

            if (runtimeExcElement == null) {

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



        private <T extends Element> List<T> resolveHandles(CompilationInfo compInfo, List<ElementHandle<T>> handles) {

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



        private TypeElement getTestCaseTypeElem(Elements elements) {

            if (testCaseTypeElem == null) {

                testCaseTypeElem = getElemForClassName(J2MEUNIT_FRAMEWORK_PACKAGE_NAME, elements);

            }

            return testCaseTypeElem;

        }



        private TypeElement getElemForClassName(String className, Elements elements) {

            TypeElement elem = elements.getTypeElement(className);

            if (elem == null) {

                ErrorManager.getDefault().log(

                        ErrorManager.ERROR,

                        "Could not find TypeElement for " + className); //NOI18N

            }

            return elem;

        }



        public void cancel() {

            cancelled = true;

        }



        private void classProcessed(ClassTree cls) {

            if (processedClassNames == null) {

                processedClassNames = new ArrayList<String>(4);

            }

            processedClassNames.add(cls.getSimpleName().toString());

        }



        List<String> getProcessedClassNames() {

            return processedClassNames != null

                    ? processedClassNames

                    : Collections.<String>emptyList();

        }

    }



    private boolean isMethodAcceptable(ExecutableElement method) {

        Set<Modifier> modifiers = method.getModifiers();



        if (modifiers.contains(Modifier.PUBLIC) && methodAccessModifiers.contains(Modifier.PUBLIC))

            return true;

        else if (modifiers.contains(Modifier.PROTECTED) && methodAccessModifiers.contains(Modifier.PROTECTED))

            return true;

        else if (!(modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.PROTECTED))

                && testPkgPrivateMethods && !modifiers.contains(Modifier.PRIVATE))

            return true;

        else

            return false;

    }

}

