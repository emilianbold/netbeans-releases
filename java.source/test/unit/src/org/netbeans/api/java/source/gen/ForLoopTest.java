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

import com.sun.source.tree.*;
import java.io.*;
import java.util.Collections;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestSuite;

/**
 * For Loop generator tests.
 * 
 * @author Pavel Flaska
 */
public class ForLoopTest extends GeneratorTestMDRCompat {

    public ForLoopTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ForLoopTest.class);
        return suite;
    }

    public void testReplaceStmtWithBlock1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "            System.err.println(\"taragui() method\");\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "        for (int i = 0; i < 10; i++) {\n" +
            "            System.err.println(\"taragui() method\");\n" + 
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ForLoopTree flt = (ForLoopTree) method.getBody().getStatements().get(0);
                StatementTree mst = flt.getStatement();
                BlockTree block = make.Block(Collections.<StatementTree>singletonList(mst), false);
                workingCopy.rewrite(mst, block);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testReplaceStmtWithBlock2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "        while (true)\n" +
            "            System.err.println(\"taragui() method\");\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "        while (true) {\n" +
            "            System.err.println(\"taragui() method\");\n" + 
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                WhileLoopTree flt = (WhileLoopTree) method.getBody().getStatements().get(0);
                StatementTree mst = flt.getStatement();
                BlockTree block = make.Block(Collections.<StatementTree>singletonList(mst), false);
                workingCopy.rewrite(mst, block);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRenameInInfiniteFor() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public int a = 10;\n" +
            "    \n" +
            "    public void main(String[] args) {\n" +
            "        for (;;) {\n" +
            "            a = 12;\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "\n");
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public int asdf = 10;\n" +
            "    \n" +
            "    public void main(String[] args) {\n" +
            "        for (;;) {\n" +
            "            asdf = 12;\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);

                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(var, make.setLabel(var, "asdf"));
                
                MethodTree method = (MethodTree) clazz.getMembers().get(2);
                ForLoopTree flt = (ForLoopTree) method.getBody().getStatements().get(0);
                BlockTree block = (BlockTree) flt.getStatement();
                AssignmentTree assign = (AssignmentTree) ((ExpressionStatementTree) block.getStatements().get(0)).getExpression();
                ExpressionTree et = assign.getVariable();
                workingCopy.rewrite(et, make.setLabel(et, "asdf"));
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testDoWhileBlockReplacement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public int a = 10;\n" +
            "    \n" +
            "    public void main(String[] args) {\n" +
            "        do\n" +
            "            a = 12;\n" +
            "        while (a == 10);\n" +
            "    }\n" +
            "}\n" +
            "\n");
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public int asdf = 10;\n" +
            "    \n" +
            "    public void main(String[] args) {\n" +
            "        do {\n" +
            "            a = 12;\n" +
            "        } while (a == 10);\n" +
            "    }\n" +
            "}\n" +
            "\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);

                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(var, make.setLabel(var, "asdf"));
                
                MethodTree method = (MethodTree) clazz.getMembers().get(2);
                DoWhileLoopTree flt = (DoWhileLoopTree) method.getBody().getStatements().get(0);
                StatementTree statement = flt.getStatement();
                BlockTree block = make.Block(Collections.<StatementTree>singletonList(statement), false);
                workingCopy.rewrite(statement, block);
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
