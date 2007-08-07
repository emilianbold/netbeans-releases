/**
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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Pavel Flaska
 */
public class RefactoringRegressionsTest extends GeneratorTestMDRCompat {

    public RefactoringRegressionsTest(String aName) {
        super(aName);
    }
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
//        suite.addTestSuite(RefactoringRegressionsTest.class);
        suite.addTest(new RefactoringRegressionsTest("testRenameTypeParameterInInvocation"));
        return suite;
    }

    /**
     * #111981
     */
    public void testRenameTypeParameterInInvocation() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Mnozina {\n" +
            "    \n" +
            "    static enum Prvek {\n" +
            "        PrvniPrvek,\n" +
            "        DruhyPrvek;\n" +
            "    }\n" +
            "    \n" +
            "    void metoda() {\n" +
            "        List<Prvek> required = new ArrayList<Prvek>();\n" +
            "        required.addAll(Arrays.<Prvek>asList());\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Mnozina {\n" +
            "    \n" +
            "    static enum Unit {\n" +
            "        PrvniPrvek,\n" +
            "        DruhyPrvek;\n" +
            "    }\n" +
            "    \n" +
            "    void metoda() {\n" +
            "        List<Unit> required = new ArrayList<Unit>();\n" +
            "        required.addAll(Arrays.<Unit>asList());\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree innerClazz = (ClassTree) clazz.getMembers().get(1);
                MethodTree method = (MethodTree) clazz.getMembers().get(2);
                workingCopy.rewrite(innerClazz, make.setLabel(innerClazz, "Unit"));
                
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                ParameterizedTypeTree ptt = (ParameterizedTypeTree) var.getType();
                IdentifierTree ident = (IdentifierTree) ptt.getTypeArguments().get(0);
                workingCopy.rewrite(ident, make.Identifier("Unit"));
                
                NewClassTree nct = (NewClassTree) var.getInitializer();
                ptt = (ParameterizedTypeTree) nct.getIdentifier();
                ident = (IdentifierTree) ptt.getTypeArguments().get(0);
                workingCopy.rewrite(ident, make.Identifier("Unit"));
                
                ExpressionStatementTree stat = (ExpressionStatementTree) method.getBody().getStatements().get(1);
                MethodInvocationTree mit = (MethodInvocationTree) stat.getExpression();
                mit = (MethodInvocationTree) mit.getArguments().get(0);
                ident = (IdentifierTree) mit.getTypeArguments().get(0);
                workingCopy.rewrite(ident, make.Identifier("Unit"));
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
