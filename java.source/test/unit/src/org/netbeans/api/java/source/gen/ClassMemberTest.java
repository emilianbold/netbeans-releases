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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import com.sun.source.tree.TypeParameterTree;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.builder.TreeFactory;

/**
 *
 * @author Pavel Flaska
 */
public class ClassMemberTest extends GeneratorTest {
    
    /** Creates a new instance of ClassMemberTest */
    public ClassMemberTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
//        suite.addTestSuite(ClassMemberTest.class);
//        suite.addTest(new ClassMemberTest("testAddAtIndex0"));
        suite.addTest(new ClassMemberTest("testAddAtIndex2"));
        suite.addTest(new ClassMemberTest("testAddToEmpty"));
//        suite.addTest(new ClassMemberTest("testAddConstructor"));
//        suite.addTest(new ClassMemberTest("testInsertFieldToIndex0"));
        suite.addTest(new ClassMemberTest("testModifyFieldName"));
        suite.addTest(new ClassMemberTest("testModifyModifiers"));
        suite.addTest(new ClassMemberTest("testAddToEmptyInterface"));
        suite.addTest(new ClassMemberTest("testAddNewClassWithNewMembers"));
        suite.addTest(new ClassMemberTest("testAddInnerInterface"));
        suite.addTest(new ClassMemberTest("testAddInnerAnnotationType"));
        suite.addTest(new ClassMemberTest("testAddInnerEnum"));
        return suite;
    }

    public void testAddAtIndex0() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" + 
            "    }\n" +
            "    \n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.insertClassMember(classTree, 0, m(make));
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddAtIndex2() throws Exception {
        //member position 2 is actually after the taragui method, as position 0 is the syntetic constructor:
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
            "    }\n" +
            "\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.insertClassMember(classTree, 2, m(make));
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddToEmpty() throws Exception {
        //member position 2 is actually after the taragui method, as position 0 is the syntetic constructor:
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.addClassMember(classTree, m(make));
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddConstructor() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    String prefix;\n" +
            "    \n" +
            "    public void method() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    String prefix;\n" +
            "    \n" +
            "    public Test(boolean prefix) {\n" +
            "    }\n" +
            "    \n" +
            "    public void method() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree classTree = (ClassTree) typeDecl;
                        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                        List<VariableTree> arguments = new ArrayList<VariableTree>();
                        arguments.add(make.Variable(
                            make.Modifiers(EnumSet.noneOf(Modifier.class)),
                            "prefix",
                            make.PrimitiveType(TypeKind.BOOLEAN), null)
                        );
                        MethodTree constructor = make.Method(
                            mods,
                            "<init>",
                            null,
                            Collections.<TypeParameterTree> emptyList(),
                            arguments,
                            Collections.<ExpressionTree>emptyList(),
                            make.Block(Collections.<StatementTree>emptyList(), false),
                            null
                        );
                        ClassTree copy = make.insertClassMember(classTree, 2, constructor);
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testInsertFieldToIndex0() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    String prefix;\n" +
            "    \n" +
            "    int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        VariableTree member = make.Variable(
                                make.Modifiers(Collections.<Modifier>emptySet()),
                                "prefix",
                                make.Identifier("String"),
                                null
                            );
                        ClassTree modifiedClazz = make.insertClassMember(clazz, 0, member);
                        workingCopy.rewrite(clazz,modifiedClazz);
                    }
                }
            }
            public void cancel() {}
        };
        src.runModificationTask(task).commit();    
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testModifyFieldName() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    int newFieldName = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        VariableTree variable = (VariableTree) ((ClassTree) typeDecl).getMembers().get(0);
                        VariableTree copy = make.setLabel(variable, "newFieldName");
                        workingCopy.rewrite(variable, copy);
                    }
                }
            }
            public void cancel() {}
        };
        src.runModificationTask(task).commit();    
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testModifyModifiers() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    private int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    public int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        VariableTree variable = (VariableTree) ((ClassTree) typeDecl).getMembers().get(0);
                        ModifiersTree mods = variable.getModifiers();
                        workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)));
                    }
                }
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();    
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddToEmptyInterface() throws Exception {
        //member position 2 is actually after the taragui method, as position 0 is the syntetic constructor:
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public interface Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public interface Test {\n\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException ;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree classTree = (ClassTree) typeDecl;
                        MethodTree method = m(make);
                        MethodTree methodC = make.Method(method.getModifiers(),
                                method.getName(),
                                method.getReturnType(),
                                (List<TypeParameterTree>) method.getTypeParameters(),
                                method.getParameters(),
                                method.getThrows(),
                                (BlockTree) null,
                                null
                        );
                        ClassTree copy = make.addClassMember(classTree, methodC);
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddNewClassWithNewMembers() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public class X {\n" +
                "        private int i;\n\n" +
                "        public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
        
        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = m(make);
                MethodTree methodC = make.Method(method.getModifiers(),
                        method.getName(),
                        method.getReturnType(),
                        (List<TypeParameterTree>) method.getTypeParameters(),
                        method.getParameters(),
                        method.getThrows(),
                        "{}",
                        null
                        );
                VariableTree var = make.Variable(make.Modifiers(EnumSet.of(Modifier.PRIVATE)), "i", make.Type(workingCopy.getTypes().getPrimitiveType(TypeKind.INT)), null);
                ClassTree nueClass = make.Class(make.Modifiers(EnumSet.of(Modifier.PUBLIC)), "X", Collections.<TypeParameterTree>emptyList(), null, Collections.<ExpressionTree>emptyList(), Arrays.asList(var, methodC));
                ClassTree copy = make.addClassMember(ct, nueClass);
                workingCopy.rewrite(ct, copy);
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    private MethodTree m(TreeMaker make) {
        // create method modifiers
        ModifiersTree parMods = make.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
        // create parameters
        VariableTree par1 = make.Variable(parMods, "a", make.PrimitiveType(TypeKind.INT), null);
        VariableTree par2 = make.Variable(parMods, "b", make.PrimitiveType(TypeKind.FLOAT), null);
        List<VariableTree> parList = new ArrayList<VariableTree>(2);
        parList.add(par1);
        parList.add(par2);
        // create method
        MethodTree newMethod = make.Method(
            make.Modifiers( 
                Collections.singleton(Modifier.PUBLIC), // modifiers
                Collections.EMPTY_LIST // annotations
            ), // modifiers and annotations
            "newlyCreatedMethod", // name
            make.PrimitiveType(TypeKind.VOID), // return type
            Collections.EMPTY_LIST, // type parameters for parameters
            parList, // parameters
            Collections.singletonList(make.Identifier("java.io.IOException")), // throws 
            make.Block(Collections.EMPTY_LIST, false), // empty statement block
            null // default value - not applicable here, used by annotations
        );
        return newMethod;
    }
    
    /**
     * #92726, #92127: When semicolon is in class declaration, it is represented
     * as an empty initilizer in the tree with position -1. This causes many
     * problems during generating. See issues for details.
     */
    public void testAddAfterEmptyInit1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    static enum Enumerace {\n" +
            "        A, B\n" +
            "    };\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    static enum Enumerace {\n" +
            "        A, B\n" +
            "    };\n\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.addClassMember(classTree, m(make));
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92726, #92127: When semicolon is in class declaration, it is represented
     * as an empty initilizer in the tree with position -1. This causes many
     * problems during generating. See issues for details.
     */
    public void testAddAfterEmptyInit2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    ;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    ;\n\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.addClassMember(classTree, m(make));
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #96070
     */
    public void testAddInnerInterface() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    interface Honza {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree innerIntfc = make.Interface(make.Modifiers(
                        Collections.<Modifier>emptySet()),
                        "Honza",
                        Collections.<TypeParameterTree>emptyList(),
                        null,
                        Collections.<ExpressionTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                workingCopy.rewrite(topLevel, make.addClassMember(topLevel, innerIntfc));
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #96070
     */
    public void testAddInnerAnnotationType() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public @interface Honza {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree innerIntfc = make.AnnotationType(make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "Honza",
                        Collections.<TypeParameterTree>emptyList(),
                        null,
                        Collections.<ExpressionTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                workingCopy.rewrite(topLevel, make.addClassMember(topLevel, innerIntfc));
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #96070
     */
    public void testAddInnerEnum() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    protected enum Honza {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree innerIntfc = make.Enum(make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PROTECTED)),
                        "Honza",
                        Collections.<TypeParameterTree>emptyList(),
                        null,
                        Collections.<ExpressionTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                workingCopy.rewrite(topLevel, make.addClassMember(topLevel, innerIntfc));
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
    
}
