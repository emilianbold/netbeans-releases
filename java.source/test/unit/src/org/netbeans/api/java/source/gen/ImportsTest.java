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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;

import java.io.File;
import java.io.IOException;

import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests imports matching and its correct adding/removing. Just generator
 * test, does not do anything with import analysis.
 * 
 * @author Pavel Flaska
 */
public class ImportsTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of MethodParametersTest */
    public ImportsTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ImportsTest.class);
//        suite.addTest(new ImportsTest("testAddFirst"));
//        suite.addTest(new ImportsTest("testAddFirstAgain"));
//        suite.addTest(new ImportsTest("testAddSecondImport"));
//        suite.addTest(new ImportsTest("testAddSecondImportWithEndLineCmt"));
//        suite.addTest(new ImportsTest("testAddTwoImportsOrigWithComment"));
//        suite.addTest(new ImportsTest("testAddBetweenImports"));
//        suite.addTest(new ImportsTest("testRemoveBetweenImportsWithLineEndComment"));
//        suite.addTest(new ImportsTest("testRemoveAllImports"));
//        suite.addTest(new ImportsTest("testRemoveAllImports2"));
//        suite.addTest(new ImportsTest("testAddFirstTwoAgain"));
//        suite.addTest(new ImportsTest("testAddFirstTwo"));
//        suite.addTest(new ImportsTest("testAddFirstToExisting"));
//        suite.addTest(new ImportsTest("testRemoveInnerImport"));
//        suite.addTest(new ImportsTest("testEmptyLines"));
//        suite.addTest(new ImportsTest("testIndentedImport"));
//        suite.addTest(new ImportsTest("testIndentedImport2"));
//        suite.addTest(new ImportsTest("testUnformatted"));
//        suite.addTest(new ImportsTest("testMissingNewLine"));
//        suite.addTest(new ImportsTest("testRemoveAllInDefault"));
//        suite.addTest(new ImportsTest("testRemoveAllInDefault2"));
//        suite.addTest(new ImportsTest("testRemoveAfterEmpty"));
//        suite.addTest(new ImportsTest("testRemoveBeforeEmpty"));
//        suite.addTest(new ImportsTest("testRenameIdentifier"));
//        suite.addTest(new ImportsTest("testRenameIdentifier2"));
//        suite.addTest(new ImportsTest("testAtVeryBeginning"));
//        suite.addTest(new ImportsTest("testPackageInfo"));
        return suite;
    }

    public void testAddFirst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                       node, 
                       make.Import(make.Identifier("java.io.IOException"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstAgain() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                        node, 
                        make.Import(make.Identifier("java.io.IOException"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstToExisting() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.lang.NullPointerException;\n" +
            "\n" + 
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "import java.lang.NullPointerException;\n" +
            "\n" + 
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.insertCompUnitImport(
                        node, 
                        0,
                        make.Import(make.Identifier("java.io.IOException"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstTwo() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n" +
            "import java.util.List;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                        node,
                        make.Import(make.Identifier("java.io.IOException"), false)
                );
                copy = make.addCompUnitImport(
                        copy, 
                        make.Import(make.Identifier("java.util.List"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstTwoAgain() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "/** javadoc comment */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n" +
            "import java.util.List;\n\n" +
            "/** javadoc comment */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                        node,
                        make.Import(make.Identifier("java.io.IOException"), false)
                );
                copy = make.addCompUnitImport(
                        copy, 
                        make.Import(make.Identifier("java.util.List"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddSecondImport() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n" +
            "import java.util.List;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                        node, 
                        make.Import(make.Identifier("java.util.List"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testAddSecondImportWithEndLineCmt() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException; // aa\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException; // aa\n" +
            "import java.util.List;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                        node, 
                        make.Import(make.Identifier("java.util.List"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddTwoImportsOrigWithComment() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException; // yerba mate\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException; // yerba mate\n" +
            "import java.util.List;\n" +
            "import java.util.Collections;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                        node, 
                        make.Import(make.Identifier("java.util.List"), false)
                );
                copy = make.addCompUnitImport(
                        copy, 
                        make.Import(make.Identifier("java.util.Collections"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddBetweenImports() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.IOException; // yerba mate\n" +
            "import java.util.List;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.IOException; // yerba mate\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.List;\n" +
            "import java.util.LinkedList;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.insertCompUnitImport(
                        node, 1,
                        make.Import(make.Identifier("java.util.ArrayList"), false)
                );
                copy = make.insertCompUnitImport(
                        copy, 3,
                        make.Import(make.Identifier("java.util.LinkedList"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveBetweenImportsWithLineEndComment() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n" +
            "import java.util.ArrayList; // polovy seznam\n" +
            "import java.util.List; // yerba mate\n" +
            "import java.util.LinkedList;\n" +
            "import java.util.Collections;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n" +
            "import java.util.ArrayList; // polovy seznam\n" +
            "import java.util.LinkedList;\n" +
            "import java.util.Collections;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(node, 2);
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveInnerImport() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.List;\n" +
            "import java.util.LinkedList;\n" +
            "import java.util.Collections;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.LinkedList;\n" +
            "import java.util.Collections;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(node, 2);
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveAllImports() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.ArrayList; // polovy seznam\n" +
            "import java.util.List; // yerba mate\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(node, 0);
                copy = make.removeCompUnitImport(copy, 0);
                copy = make.removeCompUnitImport(copy, 0);
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveAllImports2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.ArrayList; // polovy seznam\n" +
            "import java.util.List; // yerba mate\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(node, 0);
                copy = make.removeCompUnitImport(copy, 0);
                copy = make.removeCompUnitImport(copy, 0);
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testUnformatted() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;" +
            "import java.util.ArrayList; // polovy seznam\n" +
            "import java.util.List; // yerba mate\n" +
            "import java.util.Collections;" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;" +
            "import java.util.List; // yerba mate\n" +
            "import java.util.Collections;" +
            "import static java.util.Arrays;\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(node, 0);
                copy = make.addCompUnitImport(copy, make.Import(make.Identifier("java.util.Arrays"), true));
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testEmptyLines() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "import java.util.List;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.LinkedList;\n" +
            "\n" +
            "import java.util.List;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ImportTree novyDovoz = make.Import(make.Identifier("java.util.LinkedList"), false);
                CompilationUnitTree copy = make.insertCompUnitImport(cut, 1, novyDovoz);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testIndentedImport() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "    import java.util.ArrayList;\n" +
            "\n" +
            "import java.util.List;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "\n" +
            "import java.util.List;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(cut, 0);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testIndentedImport2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "import java.util.List;\n" +
            "    import java.util.ArrayList;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "import java.util.List;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(cut, 1);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testMissingNewLine() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "import java.util.List;\n" +
            "import java.util.ArrayList;import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "import java.util.List;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(cut, 1);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveAllInDefault() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(cut, 0);
                copy = make.removeCompUnitImport(copy, 0);
                copy = make.removeCompUnitImport(copy, 0);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveAllInDefault2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(cut, 0);
                copy = make.removeCompUnitImport(copy, 0);
                copy = make.removeCompUnitImport(copy, 0);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveAfterEmpty() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "import java.util.List;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "import java.util.List;\n" +
            "\n" +
            "import java.util.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(cut, 1);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveBeforeEmpty() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "import java.util.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "import java.util.List;\n" +
            "\n" +
            "import java.util.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(cut, 1);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRenameIdentifier() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "import java.util.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "import java.util.Jitko; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ImportTree dovoz = cut.getImports().get(2);
                MemberSelectTree mst = (MemberSelectTree) dovoz.getQualifiedIdentifier();
                workingCopy.rewrite(mst, make.setLabel(mst, "Jitko"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRenameIdentifier2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "import java.util.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "import java.jitko.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ImportTree dovoz = cut.getImports().get(2);
                MemberSelectTree mst = (MemberSelectTree) dovoz.getQualifiedIdentifier();
                mst = (MemberSelectTree) mst.getExpression();
                workingCopy.rewrite(mst, make.setLabel(mst, "jitko"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * http://www.netbeans.org/issues/show_bug.cgi?id=100162
     */
    public void testAtVeryBeginning() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "import java.io.IOException;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                       node, 
                       make.Import(make.Identifier("java.io.IOException"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * http://www.netbeans.org/issues/show_bug.cgi?id=103429
     */
    public void testPackageInfo() throws Exception {
        testFile = new File(getWorkDir(), "package-info.java");
        TestUtilities.copyStringToFile(testFile, 
            "@XmlSchema(namespace = \"urn:aaa\")\n" +
            "package javaapplication2;\n"
            );
        String golden =
            "@XmlSchema(namespace = \"urn:aaa\")\n" +
            "package javaapplication2;\n" +
            "\n" +
            "import javax.xml.bind.annotation.XmlSchema;\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                       node, 
                       make.Import(make.Identifier("javax.xml.bind.annotation.XmlSchema"), false)
                );
                workingCopy.rewrite(node, copy);
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
