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

import com.sun.source.tree.CompilationUnitTree;
import java.io.File;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.jackpot.transform.Transformer;
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
        //suite.addTestSuite(ImportsTest.class);
        suite.addTest(new ImportsTest("testAddFirst"));
        suite.addTest(new ImportsTest("testAddSecondImport"));
        suite.addTest(new ImportsTest("testAddTwoImportsOrigWithComment"));
        suite.addTest(new ImportsTest("testAddBetweenImports"));
        suite.addTest(new ImportsTest("testRemoveBetweenImportsWithLineEndComment"));
        suite.addTest(new ImportsTest("testRemoveAllImports"));
        suite.addTest(new ImportsTest("testAddFirstTwo"));
        suite.addTest(new ImportsTest("testUnformatted"));
        return suite;
    }
    
    public void testAddFirst() throws Exception {
        // XXX: for this test case, introduce two new lines?
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
            "import java.io.IOException;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                    super.visitCompilationUnit(node, p);
                    CompilationUnitTree copy = make.addCompUnitImport(
                            node, 
                            make.Import(make.Identifier("java.io.IOException"), false)
                    );
                    changes.rewrite(node, copy);
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

        process(
            new Transformer<Void, Object>() {
            
                public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                    super.visitCompilationUnit(node, p);
                    CompilationUnitTree copy = make.addCompUnitImport(
                            node,
                            make.Import(make.Identifier("java.io.IOException"), false)
                    );
                    copy = make.addCompUnitImport(
                            copy, 
                            make.Import(make.Identifier("java.util.List"), false)
                    );
                    changes.rewrite(node, copy);
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testAddSecondImport() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n\n" +
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

        process(
            new Transformer<Void, Object>() {
            
                public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                    super.visitCompilationUnit(node, p);
                    CompilationUnitTree copy = make.addCompUnitImport(
                            node, 
                            make.Import(make.Identifier("java.util.List"), false)
                    );
                    changes.rewrite(node, copy);
                    return null;
                }
            }
        );
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

        process(
            new Transformer<Void, Object>() {
            
                public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                    super.visitCompilationUnit(node, p);
                    CompilationUnitTree copy = make.addCompUnitImport(
                            node, 
                            make.Import(make.Identifier("java.util.List"), false)
                    );
                    copy = make.addCompUnitImport(
                            copy, 
                            make.Import(make.Identifier("java.util.Collections"), false)
                    );
                    changes.rewrite(node, copy);
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddBetweenImports() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException; // yerba mate\n" +
            "import java.util.List;\n" +
            "import java.util.Collections;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException; // yerba mate\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.List;\n" +
            "import java.util.LinkedList;\n" +
            "import java.util.Collections;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                    super.visitCompilationUnit(node, p);
                    CompilationUnitTree copy = make.insertCompUnitImport(
                            node, 1,
                            make.Import(make.Identifier("java.util.ArrayList"), false)
                    );
                    copy = make.insertCompUnitImport(
                            copy, 3,
                            make.Import(make.Identifier("java.util.LinkedList"), false)
                    );
                    changes.rewrite(node, copy);
                    return null;
                }
            }
        );
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

        process(
            new Transformer<Void, Object>() {
            
                public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                    super.visitCompilationUnit(node, p);
                    CompilationUnitTree copy = make.removeCompUnitImport(node, 2);
                    changes.rewrite(node, copy);
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveAllImports() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.ArrayList; // polovy seznam\n" +
            "import java.util.List; // yerba mate\n" +
            "import java.util.Collections;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                    super.visitCompilationUnit(node, p);
                    CompilationUnitTree copy = make.removeCompUnitImport(node, 0);
                    copy = make.removeCompUnitImport(copy, 0);
                    copy = make.removeCompUnitImport(copy, 0);
                    changes.rewrite(node, copy);
                    return null;
                }
            }
        );
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
            "package hierbas.del.litoral;\n" +
            "import java.util.List; // yerba mate\n" +
            "import java.util.Collections;\n" +
            "import static java.util.Arrays;" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                    super.visitCompilationUnit(node, p);
                    CompilationUnitTree copy = make.removeCompUnitImport(node, 0);
                    copy = make.addCompUnitImport(copy, make.Import(make.Identifier("java.util.Arrays"), true));
                    changes.rewrite(node, copy);
                    return null;
                }
            }
        );
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
