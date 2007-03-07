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
import com.sun.source.tree.ExpressionTree;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.ErrorManager;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 *
 * @author  Marian Petras
 * @author  vstejskal
 */
final class JUnit3TestGenerator extends AbstractTestGenerator {
    
    /**
     */
    JUnit3TestGenerator(TestGeneratorSetup setup) {
        super(setup);
    }
    
    /**
     */
    JUnit3TestGenerator(TestGeneratorSetup setup,
                        List<ElementHandle<TypeElement>> srcTopClassHandles,
                        List<String>suiteMembers,
                        boolean isNewTestClass) {
        super(setup, srcTopClassHandles, suiteMembers, isNewTestClass);
    }
    
    
    /** element representing type {@code junit.framework.Test} */
    private TypeElement testTypeElem;
    /** */
    private TypeElement testCaseTypeElem;
    /** element representing type {@code junit.framework.TestSuite} */
    private TypeElement testSuiteTypeElem;
        
    
    /**
     */
    protected ClassTree composeNewTestClass(WorkingCopy workingCopy,
                                            String name,
                                            List<? extends Tree> members) {
        final TreeMaker maker = workingCopy.getTreeMaker();
        ModifiersTree modifiers = maker.Modifiers(
                                      Collections.<Modifier>singleton(PUBLIC));
        TypeElement testCaseType = getTestCaseTypeElem(workingCopy.getElements());
        Tree extendsClause = (testCaseType != null)
                             ? maker.QualIdent(testCaseType)
                             : maker.Identifier("junit.framework.TestCase");//NOI18N
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
    protected List<? extends Tree> generateInitMembers(WorkingCopy workingCopy) {
        if (!setup.isGenerateSetUp() && !setup.isGenerateTearDown()) {
            return Collections.<Tree>emptyList();
        }

        final TreeMaker maker = workingCopy.getTreeMaker();
        List<MethodTree> result = new ArrayList<MethodTree>(2);
        if (setup.isGenerateSetUp()) {
            result.add(generateInitMethod("setUp", maker));             //NOI18N
        }
        if (setup.isGenerateTearDown()) {
            result.add(generateInitMethod("tearDown", maker));          //NOI18N
        }
        return result;
    }

    /**
     */
    protected ClassTree generateMissingInitMembers(ClassTree tstClass,
                                                   TreePath tstClassTreePath,
                                                   WorkingCopy workingCopy) {
        if (!setup.isGenerateSetUp() && !setup.isGenerateTearDown()
                && !setup.isGenerateSuiteClasses()) {
            return tstClass;
        }

        ClassMap classMap = ClassMap.forClass(tstClass, tstClassTreePath, workingCopy.getTrees());

        if ((!setup.isGenerateSetUp() || classMap.containsSetUp())
                && (!setup.isGenerateTearDown() || classMap.containsTearDown())
                && (!setup.isGenerateSuiteClasses() || classMap.containsNoArgMethod("suite"))) {//NOI18N
            return tstClass;
        }

        List<? extends Tree> tstMembersOrig = tstClass.getMembers();
        List<Tree> tstMembers = new ArrayList<Tree>(tstMembersOrig.size() + 2);
        tstMembers.addAll(tstMembersOrig);

        generateMissingInitMembers(tstMembers, classMap, workingCopy);
        generateTestClassSuiteMethod(tstClassTreePath, tstMembers, classMap,
                                     workingCopy);

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
    protected boolean generateMissingInitMembers(List<Tree> tstMembers,
                                                 ClassMap clsMap,
                                                 WorkingCopy workingCopy) {
        TreeMaker treeMaker = workingCopy.getTreeMaker();
        
        boolean modified = false;
        if (setup.isGenerateSetUp() && !clsMap.containsSetUp()) {
            addInitMethod("setUp",                                      //NOI18N
                          clsMap.getTearDownIndex(),
                          tstMembers,
                          clsMap,
                          treeMaker);
            modified = true;
        }
        if (setup.isGenerateTearDown() && !clsMap.containsTearDown()) {
            int setUpIndex = clsMap.getSetUpIndex();
            addInitMethod("tearDown",                                   //NOI18N
                          (setUpIndex != -1) ? setUpIndex + 1 : -1,
                          tstMembers,
                          clsMap,
                          treeMaker);
            modified = true;
        }
        return modified;
    }
    
    /**
     * Creates a new init method ({@code setUp()}, {@code tearDown()}.
     * When the method is created, it is added to the passed
     * {@code List<Tree>} of class members and the passed {@code ClassMap}
     * is updated appropriately.
     * 
     * @param  methodName  name of the init method to be added
     * @param  targetIndex  position in the list of members where the new
     *                      init method should be put; or {@code -1} if this
     *                      is the first init method to be added and
     *                      the position should be determined automatically
     * @param  clsMembers  list of class members to which the created init
     *                     method should be added
     * @param  clsMap  map of the current class members (will be updated)
     * @param  treeMaker  maker to be used for creation of the init method
     */
    private void addInitMethod(String methodName,
                               int targetIndex,
                               List<Tree> clsMembers,
                               ClassMap clsMap,
                               TreeMaker treeMaker) {
        MethodTree initMethod = generateInitMethod(methodName, treeMaker);
        
        if (targetIndex == -1) {
            targetIndex = getPlaceForFirstInitMethod(clsMap);
        }
        
        if (targetIndex != -1) {
            clsMembers.add(targetIndex, initMethod);
        } else {
            clsMembers.add(initMethod);
        }
        clsMap.addNoArgMethod(methodName, targetIndex);
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
    protected MethodTree generateInitMethod(String methodName,
                                            TreeMaker maker) {
        ModifiersTree modifiers = maker.Modifiers(
                Collections.<Modifier>singleton(PROTECTED));
        ExpressionTree superMethodCall = maker.MethodInvocation(
                Collections.<ExpressionTree>emptyList(),    // type params.
                maker.MemberSelect(
                        maker.Identifier("super"), methodName),         //NOI18N
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
                        maker.Identifier("Exception")),     // throws...//NOI18N
                methodBody,
                null);                                      // default value
        return method;
    }

    /**
     */
    protected void generateMissingPostInitMethods(TreePath tstClassTreePath,
                                                  List<Tree> tstMembers,
                                                  ClassMap clsMap,
                                                  WorkingCopy workingCopy) {
        if (setup.isGenerateSuiteClasses()) {
            generateTestClassSuiteMethod(tstClassTreePath,
                                         tstMembers,
                                         clsMap,
                                         workingCopy);
        }
    }
    
    /**
     */
    protected MethodTree composeNewTestMethod(String testMethodName,
                                              BlockTree testMethodBody,
                                              List<ExpressionTree> throwsList,
                                              WorkingCopy workingCopy) {
        TreeMaker maker = workingCopy.getTreeMaker();
        return maker.Method(
                maker.Modifiers(createModifierSet(PUBLIC)),
                testMethodName,
                maker.PrimitiveType(TypeKind.VOID),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                throwsList,
                testMethodBody,
                null);          //default value - used by annotations
    }

    /**
     */
    protected ClassTree finishSuiteClass(ClassTree tstClass,
                                         TreePath tstClassTreePath,
                                         List<Tree> tstMembers,
                                         List<String> suiteMembers,
                                         boolean membersChanged,
                                         ClassMap classMap,
                                         WorkingCopy workingCopy) {
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
                classMap.addNoArgMethod("suite", targetIndex);          //NOI18N
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

        return workingCopy.getTreeMaker().Class(
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
                maker.Modifiers(noModifiers()),
                "suite",                                                //NOI18N
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
                                           "suite"),                    //NOI18N
                        Collections.<ExpressionTree>emptyList());
                methodCall = maker.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        maker.MemberSelect(maker.Identifier("suite"),   //NOI18N
                                           "addTest"),                  //NOI18N
                        Collections.singletonList(suiteMethodCall));

                bodyContent.add(maker.ExpressionStatement(methodCall));
            }
        }

        /* return suite; */

        bodyContent.add(maker.Return(maker.Identifier("suite")));       //NOI18N


        return maker.Method(
                    maker.Modifiers(createModifierSet(PUBLIC, STATIC)),
                    "suite",                                            //NOI18N
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
            if (method.getSimpleName().contentEquals("suite")           //NOI18N
                    && method.getParameters().isEmpty()) {
                return method.getModifiers().contains(Modifier.STATIC)
                       && types.isSameType(method.getReturnType(),
                                           testType);
            }
        }
        return false;
    }

    /**
     */
    private boolean generateTestClassSuiteMethod(TreePath tstClassTreePath,
                                                   List<Tree> tstMembers,
                                                   ClassMap clsMap,
                                                   WorkingCopy workingCopy) {
        if (!setup.isGenerateSuiteClasses()
                        || clsMap.containsNoArgMethod("suite")) {       //NOI18N
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
                maker.Modifiers(noModifiers()),
                "suite",                                                //NOI18N
                maker.QualIdent(getTestSuiteTypeElem(elements)),
                maker.NewClass(
                        null,           //enclosing instance
                        Collections.<ExpressionTree>emptyList(),
                        maker.QualIdent(getTestSuiteTypeElem(elements)),
                        Collections.singletonList(
                                maker.MemberSelect(maker.QualIdent(tstClassElem),
                                                   "class")),           //NOI18N
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
                                    "suite"),                           //NOI18N
                            Collections.<ExpressionTree>emptyList());

                    /* suite.addTest(...) */
                    MethodInvocationTree methodCall = maker.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            maker.MemberSelect(
                                    maker.Identifier("suite"),          //NOI18N
                                    "addTest"),                         //NOI18N
                            Collections.singletonList(arg));

                    bodyContent.add(maker.ExpressionStatement(methodCall));
                }
            }
        }

        /* return suite; */

        ReturnTree returnStmt
                = maker.Return(maker.Identifier("suite"));              //NOI18N
        bodyContent.add(returnStmt);

        MethodTree suiteMethod = maker.Method(
                maker.Modifiers(createModifierSet(PUBLIC, STATIC)),
                "suite",                                                //NOI18N
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
        clsMap.addNoArgMethod("suite", targetIndex);                    //NOI18N

        return true;
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
                                        "junit.framework.TestCase",     //NOI18N
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
    private static TypeElement getElemForClassName(String className,
                                            Elements elements) {
        TypeElement elem = elements.getTypeElement(className);
        if (elem == null) {
            ErrorManager.getDefault().log(
                    ErrorManager.ERROR,
                    "Could not find TypeElement for " + className);     //NOI18N
        }
        return elem;
    }

}
