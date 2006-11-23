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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.jackpot.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests method type parameters changes.
 * 
 * @author Pavel Flaska
 */
public class MethodThrowsTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of MethodParametersTest */
    public MethodThrowsTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(MethodThrowsTest.class);
//        suite.addTest(new MethodThrowsTest("testAddFirst"));
//        suite.addTest(new MethodThrowsTest("testAddFirstToExisting"));
//        suite.addTest(new MethodThrowsTest("testAddFirstTwo"));
//        suite.addTest(new MethodThrowsTest("testAddThirdToExisting"));
//        suite.addTest(new MethodThrowsTest("testRemoveAll"));
//        suite.addTest(new MethodThrowsTest("testRemoveMid"));
//        suite.addTest(new MethodThrowsTest("testRemoveFirst"));
//        suite.addTest(new MethodThrowsTest("testRemoveLast"));
//        suite.addTest(new MethodThrowsTest("testRemoveJust"));
//        suite.addTest(new MethodThrowsTest("testStupid"));
//        suite.addTest(new MethodThrowsTest("testTuner"));
        return suite;
    }
    
    public void testAddFirst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws IOException {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.addMethodThrows(
                            node, make.Identifier("IOException")
                        );
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testAddFirstTwo() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws IOException, FileNotFoundException {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.addMethodThrows(
                            node, make.Identifier("IOException")
                        );
                        copy = make.addMethodThrows(
                            copy, make.Identifier("FileNotFoundException")
                        );
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testRemoveAll() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws IOException, Exception, FileNotFoundException {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodThrows(node, 0);
                        copy = make.removeMethodThrows(copy, 0);
                        copy = make.removeMethodThrows(copy, 0);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);

    }
    
    
    public void testAddThirdToExisting() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws IOException, FileNotFoundException {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws IOException, FileNotFoundException, Exception {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.addMethodThrows(
                            node, make.Identifier("Exception")
                        );
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testAddFirstToExisting() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws IOException {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws FileNotFoundException, IOException {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.insertMethodThrows(
                            node, 0, make.Identifier("FileNotFoundException")
                        );
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testRemoveMid() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws IOException, FileNotFoundException, Exception {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws IOException, Exception {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodThrows(node, 1);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testRemoveFirst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws IOException {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodThrows(node, 0);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testRemoveLast() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws IOException, FileNotFoundException, Exception {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws IOException, FileNotFoundException {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodThrows(node, 2);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testRemoveJust() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws Exception,IOException,FileNotFoundException {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws IOException,FileNotFoundException {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodThrows(node, 0);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }

    public void testStupid() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws Exception,IOException // karel\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws Exception,IOException, FileNotFoundException // karel\n" +
            "    {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.addMethodThrows(node, make.Identifier("FileNotFoundException"));
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testTuner() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws Exception, // karel\n" +
            "                                 IOException,\n" +
            "                                 PermissionDeniedException,\n" +
            "                                 FileNotFoundException // charles\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws Exception, // karel\n" +
            "                                 IOException,\n" +
            "                                 PermissionDeniedException,\n" +
            "                                 FileNotFoundException, \n" +
            "                                 NullPointerException // charles\n" +
            "    {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.addMethodThrows(node, make.Identifier("NullPointerException"));
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testAddFirstToBadFormatted() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui(){\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() throws IOException{\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    MethodTree method = (MethodTree) clazz.getMembers().get(1);
                    if ("taragui".contentEquals(method.getName())) {
                        MethodTree copy = make.addMethodThrows(
                            method, make.Identifier("IOException")
                        );
                        workingCopy.rewrite(method, copy);
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
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "Test.java");
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
    
}
