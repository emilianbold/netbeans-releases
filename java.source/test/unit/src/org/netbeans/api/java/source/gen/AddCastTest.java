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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.jackpot.test.TestUtilities;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests correct adding cast to statement.
 *
 * @author Pavel Flaska
 */
public class AddCastTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of AddCastTest */
    public AddCastTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(AddCastTest.class);
        return suite;
    }

    public void testAddCastToDeclStmt() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "        System.err.println(\"taragui() method\");\n" + 
            "        String s = \"Oven.\";\n" +
            "//         line comment\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "        System.err.println(\"taragui() method\");\n" + 
            "        String s = (String) \"Oven.\";\n" +
            "//         line comment\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                // hovoovno
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                // coze?
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(1);
                ExpressionTree init = var.getInitializer();
                ExpressionTree cast = make.TypeCast(make.Identifier("String"), init);
                workingCopy.rewrite(init, cast);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddCastInForWithoutSteps() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void cast() {\n" +
            "        Object o = null;\n" +
            "        for (int i = 0; i < 5; ) {\n" +
            "            String s = o;\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void cast() {\n" +
            "        Object o = null;\n" +
            "        for (int i = 0; i < 5; ) {\n" +
            "            String s = (String) o;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ForLoopTree forLoop = (ForLoopTree) method.getBody().getStatements().get(1);
                BlockTree block = (BlockTree) forLoop.getStatement();
                VariableTree vt = (VariableTree) block.getStatements().get(0);
                ExpressionTree init = vt.getInitializer();
                ExpressionTree cast = make.TypeCast(make.Identifier("String"), init);
                workingCopy.rewrite(init, cast);
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
