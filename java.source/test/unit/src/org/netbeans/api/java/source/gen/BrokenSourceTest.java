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

import java.io.File;
import java.io.IOException;

import com.sun.source.tree.CompilationUnitTree;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

import org.netbeans.junit.NbTestSuite;

/**
 * Makes source changes in broken sources.
 * 
 * @author Pavel Flaska
 */
public class BrokenSourceTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of BrokenSourceTest 
     
     * @param name  test name
     */
    public BrokenSourceTest(String name) {
        super(name);
    }
    
    /**
     * Return suite.
     * 
     * @return  suite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(BrokenSourceTest.class);
//        suite.addTest(new BrokenSourceTest("testAddImportWhenClosingCurlyMissing"));
        return suite;
    }

    /**
     * Regression test for #97901.
     * 
     * @throws java.lang.Exception 
     */
    public void testAddImportWhenClosingCurlyMissing() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class User {\n" +
            "\n" +
            "    public User(Object node) {\n" +
            "        if (node instanceof Object) {\n" +
            "        } else if (node instanceof ArrayList)\n" +
            "        } else if (node instanceof LinkedList) {\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    void method() {\n" +
            "        System.err.println(\"nafink\");\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "public class User {\n" +
            "\n" +
            "    public User(Object node) {\n" +
            "        if (node instanceof Object) {\n" +
            "        } else if (node instanceof ArrayList)\n" +
            "        } else if (node instanceof LinkedList) {\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    void method() {\n" +
            "        System.err.println(\"nafink\");\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "}\n";
        
        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree cutCopy = make.addCompUnitImport(
                       cut, 
                       make.Import(make.Identifier("java.util.ArrayList"), false)
                );
                workingCopy.rewrite(cut, cutCopy);
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
