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

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 * Tests if statement creation.
 */
public class IfTest extends GeneratorTest {

    public IfTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(IfTest.class);
//        suite.addTest(new IfTest("testEmptyThenBlock"));
//        suite.addTest(new IfTest("testEmptyElseBlock"));
//        suite.addTest(new IfTest("testReplaceCondition"));
        return suite;
    }

    /**
     * Test replacing then statement with empty block.
     */
    public void testEmptyThenBlock() throws Exception {
        testFile = new File(getWorkDir(), "IfTest.java");        
        TestUtilities.copyStringToFile(testFile, 
            "package foo.bar;\n" +
            "\n" +
            "public class IfTest {\n" +
            "    public void test(boolean b) {\n" +
            "        if( b )\n" +
            "            System.out.println();\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package foo.bar;\n" +
            "\n" +
            "public class IfTest {\n" +
            "    public void test(boolean b) {\n" +
            "        if( b ) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                IfTree oldIf = (IfTree)method.getBody().getStatements().get(0);
                BlockTree blk = make.Block(Collections.<StatementTree>emptyList(), false);
                IfTree newIf = make.If(oldIf.getCondition(), blk, null);
                workingCopy.rewrite(oldIf, newIf);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testEmptyElseBlock() throws Exception {
        testFile = new File(getWorkDir(), "IfTest.java");        
        TestUtilities.copyStringToFile(testFile, 
            "package foo.bar;\n" +
            "\n" +
            "public class IfTest {\n" +
            "    public void test(boolean b) {\n" +
            "        if( b ) {\n" +
            "        } else\n" +
            "            System.err.println(\"Hrebejk je hrebec.\");\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package foo.bar;\n" +
            "\n" +
            "public class IfTest {\n" +
            "    public void test(boolean b) {\n" +
            "        if( b ) {\n" +
            "        } else {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                IfTree oldIf = (IfTree)method.getBody().getStatements().get(0);
                BlockTree block = make.Block(Collections.<StatementTree>emptyList(), false);
                StatementTree oldElse = oldIf.getElseStatement();
                workingCopy.rewrite(oldElse, block);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testReplaceCondition() throws Exception {
        testFile = new File(getWorkDir(), "IfTest.java");        
        TestUtilities.copyStringToFile(testFile, 
            "package foo.bar;\n" +
            "\n" +
            "public class IfTest {\n" +
            "    public void test(boolean b) {\n" +
            "        if (prec == treeinfo.notExpression)\n" +
            "            print(';');\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package foo.bar;\n" +
            "\n" +
            "public class IfTest {\n" +
            "    public void test(boolean b) {\n" +
            "        if (prec == TreeInfo.notExpression)\n" +
            "            print(';');\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                IfTree oldIf = (IfTree) method.getBody().getStatements().get(0);
                BinaryTree zatvorka = (BinaryTree) ((ParenthesizedTree) oldIf.getCondition()).getExpression();
                MemberSelectTree mst = (MemberSelectTree) zatvorka.getRightOperand();
                workingCopy.rewrite(mst.getExpression(), make.Identifier("TreeInfo"));
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testModifyingIf() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public boolean method(int i) {\n" +
            "        int y = 0;\n" +
            "        if (i == 0) {\n" +
            "            y = 2;\n" +
            "        } else {y = 9;}\n" +
            "        return y == 8;\n" +
            "    }\n" +
            "}\n");
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public boolean method(int i) {\n" +
            "        int y = 0;\n" +
            "        if (method(null)) {\n" + 
            "            return true;\n" +
            "        }\n" +
            "        return y == 8;\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                IfTree mit = (IfTree) block.getStatements().get(1);
                IfTree nue = make.If(
                    make.Parenthesized(make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.Identifier("method"),
                        Arrays.asList(make.Literal(null)))
                    ),
                    make.Block(
                        Collections.<StatementTree>singletonList(make.Return(make.Literal(true))),
                        false
                    ),
                    null
                );
                workingCopy.rewrite(mit, nue);
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
