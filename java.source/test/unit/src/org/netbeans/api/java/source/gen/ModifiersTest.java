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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 * Tests modifiers changes.
 * 
 * @author Pavel Flaska
 */
public class ModifiersTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of ModifiersTEst */
    public ModifiersTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ModifiersTest.class);
//        suite.addTest(new ModifiersTest("testChangeToFinalLocVar"));
//        suite.addTest(new ModifiersTest("testAddClassAbstract"));
//        suite.addTest(new ModifiersTest("testMethodMods1"));
//        suite.addTest(new ModifiersTest("testMethodMods2"));
//        suite.addTest(new ModifiersTest("testMethodMods3"));
//        suite.addTest(new ModifiersTest("testMethodMods4"));
//        suite.addTest(new ModifiersTest("testMethodMods5"));
//        suite.addTest(new ModifiersTest("testMethodMods6"));
//        suite.addTest(new ModifiersTest("testMethodMods7"));
//        suite.addTest(new ModifiersTest("testAnnRename"));
//        suite.addTest(new ModifiersTest("testAddArrayValue"));
//        suite.addTest(new ModifiersTest("testRenameAnnotationAttribute"));
//        suite.addTest(new ModifiersTest("testMakeClassAbstract"));
//        suite.addTest(new ModifiersTest("test106543"));
//        suite.addTest(new ModifiersTest("test106403"));
//        suite.addTest(new ModifiersTest("test106403_2"));
//        suite.addTest(new ModifiersTest("testAddMethodAnnotation"));
//        suite.addTest(new ModifiersTest("testAddMethodAnnotation2"));
//        suite.addTest(new ModifiersTest("testChangeInterfaceModifier"));
//        suite.addTest(new ModifiersTest("testRemoveClassAnnotation"));
        return suite;
    }

    /**
     * Tests the change of modifier in local variable
     */
    public void testChangeToFinalLocVar() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        int i = 10;\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        final int i = 10;\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(0);
                ModifiersTree mods = vt.getModifiers();
                workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>singleton(Modifier.FINAL)));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Update top-level class modifiers.
     */
    public void testAddClassAbstract() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public abstract void taragui();\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public abstract class Test {\n" +
                "    public abstract void taragui();\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                Set<Modifier> s = new HashSet<Modifier>(mods.getFlags());
                s.add(Modifier.ABSTRACT);
                workingCopy.rewrite(mods, make.Modifiers(s));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * void method() {
     * }
     * 
     * Result:
     * 
     * public static void method() {
     * }
     */
    public void testMethodMods1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    void method() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public static void method() {\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ModifiersTree mods = method.getModifiers();
                workingCopy.rewrite(mods, make.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC)));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * public static void method() {
     * }
     * 
     * Result:
     * 
     * void method() {
     * }
     */
    public void testMethodMods2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public static void method() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    void method() {\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ModifiersTree mods = method.getModifiers();
                workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>emptySet()));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * Test() {
     * }
     * 
     * Result:
     * 
     * public Test() {
     * }
     */
    public void testMethodMods3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    Test() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public Test() {\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(0);
                ModifiersTree mods = method.getModifiers();
                workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * public Test() {
     * }
     * 
     * Result:
     * 
     * Test() {
     * }
     */
    public void testMethodMods4() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public Test() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    Test() {\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(0);
                ModifiersTree mods = method.getModifiers();
                workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>emptySet()));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * public static void method() {
     * }
     * 
     * Result:
     * 
     * static void method() {
     * }
     */
    public void testMethodMods5() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public static void method() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    static void method() {\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ModifiersTree mods = method.getModifiers();
                workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>singleton(Modifier.STATIC)));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * public Test() {
     * }
     * 
     * Result:
     * 
     * protected Test() {
     * }
     */
    public void testMethodMods6() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public Test() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    protected Test() {\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(0);
                ModifiersTree mods = method.getModifiers();
                workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>singleton(Modifier.PROTECTED)));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * @Anotace()
     * public Test() {
     * }
     * 
     * Result:
     * 
     * @Annotaition()
     * protected Test() {
     * }
     */
    public void testAnnRename() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotace()\n" +
                "public class Test {\n" +
                "    public Test() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation()\n" +
                "public class Test {\n" +
                "    public Test() {\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree ann = mods.getAnnotations().get(0);
                IdentifierTree ident = (IdentifierTree) ann.getAnnotationType();
                workingCopy.rewrite(ident, make.Identifier("Annotation"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * public class Test {
     * ...
     * 
     * Result:
     * 
     * @Annotation(value = { "Lojza", "Karel" })
     * public class Test {
     * ...
     * 
     */
    public void testAddArrayValue() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    public Test() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation(value = {\"Lojza\", \"Karel\"})\n" +
                "public class Test {\n" +
                "    public Test() {\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                List<LiteralTree> l = new ArrayList<LiteralTree>();
                l.add(make.Literal("Lojza"));
                l.add(make.Literal("Karel"));
                NewArrayTree nat = make.NewArray(null, Collections.<ExpressionTree>emptyList(), l);
                AssignmentTree at = make.Assignment(make.Identifier("value"), nat);
                AnnotationTree ann = make.Annotation(make.Identifier("Annotation"), Collections.<ExpressionTree>singletonList(at));
                workingCopy.rewrite(mods, make.Modifiers(mods.getFlags(), Collections.<AnnotationTree>singletonList(ann)));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /*
     * Test rename annotation attribute, regression test for #99162
     */
    public void testRenameAnnotationAttribute() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation(val = 2)\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation(value = 2)\n" +
                "public class Test {\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree annotationTree = mods.getAnnotations().get(0);
                AssignmentTree assignementTree = (AssignmentTree) annotationTree.getArguments().get(0);
                workingCopy.rewrite(assignementTree.getVariable(), make.Identifier("value"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #95354
    public void testMakeClassAbstract() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package org.netbeans.test.java.hints;\n" +
            "\n" +
            "@Test1 @Test2(test=\"uuu\") class MakeClassAbstract3 {\n" +
            "\n" +
            "    public MakeClassAbstract3() {\n" +
            "    }\n" +
        "\n" +
            "    public abstract void test();\n" +
            "\n" +
            "}\n" +
            "\n" +
            "@interface Test1 {}\n" +
            "\n" +
            "@interface Test2 {\n" +
            "    public String test();\n" +
            "}\n"
        );
        String golden =
            "package org.netbeans.test.java.hints;\n" +
            "\n" +
            "@Test1 @Test2(test=\"uuu\") abstract class MakeClassAbstract3 {\n" +
            "\n" +
            "    public MakeClassAbstract3() {\n" +
            "    }\n" +
            "\n" +
            "    public abstract void test();\n" +
            "\n" +
            "}\n" +
            "\n" +
            "@interface Test1 {}\n" +
            "\n" +
            "@interface Test2 {\n" +
            "    public String test();\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                Set<Modifier> flags = new HashSet<Modifier>(mods.getFlags());
                flags.add(Modifier.ABSTRACT);
                workingCopy.rewrite(mods, make.Modifiers(flags, mods.getAnnotations()));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #106543 - Positions broken when removing annotation attribute value.
    public void test106543() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation(val = 2)\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation()\n" +
                "public class Test {\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree annotationTree = mods.getAnnotations().get(0);
                AnnotationTree copy = make.removeAnnotationAttrValue(annotationTree, annotationTree.getArguments().get(0));
                workingCopy.rewrite(annotationTree, copy);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #106403
    public void test106403() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation(val = 2)\n" +
                "public class Test {\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree annotationTree = mods.getAnnotations().get(0);
                AnnotationTree modified = make.addAnnotationAttrValue(
                        annotationTree, 
                        make.Assignment(make.Identifier("val"), make.Literal(2))
                );
                workingCopy.rewrite(annotationTree, modified);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #106403 -2-
    public void test106403_2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation()\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation(val = 2)\n" +
                "public class Test {\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree annotationTree = mods.getAnnotations().get(0);
                AnnotationTree modified = make.addAnnotationAttrValue(
                        annotationTree, 
                        make.Assignment(make.Identifier("val"), make.Literal(2))
                );
                workingCopy.rewrite(annotationTree, modified);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #105018 - bad formatting when adding annotation
    public void testAddMethodAnnotation() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    public void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    @Annotation\n" +
                "    public void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = ((MethodTree) clazz.getMembers().get(1)).getModifiers();
                AnnotationTree annotationTree = make.Annotation(
                        make.Identifier("Annotation"),
                        Collections.<ExpressionTree>emptyList()
                );
                ModifiersTree modified = make.addModifiersAnnotation(
                        mods,
                        annotationTree
                );
                workingCopy.rewrite(mods, modified);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #105018 - bad formatting when adding annotation
    public void testAddMethodAnnotation2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    @Annotation\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = ((MethodTree) clazz.getMembers().get(1)).getModifiers();
                AnnotationTree annotationTree = make.Annotation(
                        make.Identifier("Annotation"),
                        Collections.<ExpressionTree>emptyList()
                );
                ModifiersTree modified = make.addModifiersAnnotation(
                        mods,
                        annotationTree
                );
                workingCopy.rewrite(mods, modified);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #106252 - interface keyword doubled
    public void testChangeInterfaceModifier() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "public interface Test {\n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "interface Test {\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                Set<Modifier> flags = new HashSet<Modifier>(mods.getFlags());
                flags.remove(Modifier.PUBLIC);
                ModifiersTree modified = make.Modifiers(flags);
                
                ClassTree copy = make.Interface(
                        modified,
                        clazz.getSimpleName(),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<Tree>emptyList(),
                        clazz.getMembers()
                );
                workingCopy.rewrite(clazz, copy);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveClassAnnotation() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                ModifiersTree modified = make.removeModifiersAnnotation(mods, 0);
                workingCopy.rewrite(mods, modified);
            }
        };
        testSource.runModificationTask(task).commit();
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
