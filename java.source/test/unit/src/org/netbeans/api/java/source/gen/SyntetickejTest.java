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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;

/**
 * Check synthetic non-static initializer represented by semicolon inside
 * type declaration, i.e. semicolon is represeted by empty non-static block
 * in tree with position -1.
 * 
 * @author Pavel Flaska
 */
public class SyntetickejTest extends GeneratorTestMDRCompat {

    public SyntetickejTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(SyntetickejTest.class);
        return suite;
    }
    
    public void testEmptyStaticBlockSemicolon() throws Exception{
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package tohle;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    static enum Whorehouse {\n" +
            "        /** first prostitute */\n" +
            "        PrvniDevka,\n" +
            "        /** second prostitue */\n" +
            "        DruhaDevka,\n" +
            "        /** third prostitute */\n" +
            "        TretiDevka;\n" +
            "    };\n" + // the semicolon is strange here -- shouldn't be there, 
            "    \n" + // but it is correct and we have to handle such a situation
            "    void method() {\n" +
            "        Object o = null;\n" +
            "        String s = o;\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package tohle;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    static enum Whorehouse {\n" +
            "        /** first prostitute */\n" +
            "        PrvniDevka,\n" +
            "        /** second prostitue */\n" +
            "        DruhaDevka,\n" +
            "        /** third prostitute */\n" +
            "        TretiDevka;\n" +
            "    };\n" +
            "    \n" +
            "    void method() {\n" +
            "        Object o = null;\n" +
            "        String s = (String) o;\n" +
            "    }\n" +
            "}\n";
        
        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException{
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(3);
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
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
