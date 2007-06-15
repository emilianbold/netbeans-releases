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
import java.util.Collections;

import org.openide.filesystems.FileUtil;

import com.sun.source.tree.*;

import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;

import org.netbeans.junit.NbTestSuite;

/**
 * Regression tests.
 * 
 * @author Pavel Flaska
 */
public class DuplicatedCommentsTest extends GeneratorTestMDRCompat {

    public DuplicatedCommentsTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(DuplicatedCommentsTest.class);
//        suite.addTest(new DuplicatedCommentsTest("testLineAtTopLevel"));
        return suite;
    }

    public void testLineAtTopLevel() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package tohle;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "// TODO:\n" +
            "\n" +
            "/**\n" +
            " * Alois\n" +
            " */\n" +
            "public class NewClass {\n" +
            "    \n" +
            "    public NewClass() {\n" +
            "        List l = new ArrayList();\n" +
            "    }\n" +
            "}\n");
        String golden = 
            "package tohle;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.List;\n" +
            "\n" +
            "// TODO:\n" +
            "\n" +
            "/**\n" +
            " * Alois\n" +
            " */\n" +
            "public class NewClass {\n" +
            "    \n" +
            "    public NewClass() {\n" +
            "        List l = new ArrayList();\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                // hucky staff, correct memberSelectTree should be provided.
                // for testing reason this hacky stuff is enough.
                ImportTree importt = make.Import(make.Identifier("java.util.ArrayList"), false);
                CompilationUnitTree copy = make.insertCompUnitImport(cut, 0, importt);
                workingCopy.rewrite(cut, copy);
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
