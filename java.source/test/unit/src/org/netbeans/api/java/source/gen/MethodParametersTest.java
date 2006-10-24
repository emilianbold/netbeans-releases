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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.jackpot.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests method parameters.
 * 
 * @author Pavel Flaska
 */
public class MethodParametersTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of MethodParametersTest */
    public MethodParametersTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(MethodParametersTest.class);
        //suite.addTest(new MethodParametersTest("testAddToIndex0"));
        return suite;
    }
    
    public void testAddInsertReplaceParameters() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int a, long c, String s) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
//            "package hierbas.del.litoral;\n\n" +
//            "import java.io.File;\n\n" +
//            "public class Test {\n" +
//            "    public void taragui(final File elaborada, File marcela, long c, String s, File cedron) {\n" +
//            "    }\n" +
//            "}\n";
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(final File elaborada,File marcela, long c, String s,File cedron) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.insertMethodParameter(
                            node, 0,
                            make.Variable(
                                make.Modifiers(
                                    Collections.singleton(Modifier.FINAL),
                                    Collections.<AnnotationTree>emptyList()
                                ),
                                "elaborada",
                                make.Identifier("File"),
                                null
                            )
                        );
                        copy = make.removeMethodParameter(copy, 1);
                        copy = make.addMethodParameter(
                            copy,
                            make.Variable(
                                make.Modifiers(
                                    Collections.<Modifier>emptySet(),
                                    Collections.<AnnotationTree>emptyList()
                                ),
                                "cedron",
                                make.Identifier("File"),
                                null
                            )
                        );
                        copy = make.insertMethodParameter(
                            copy,
                            1,
                            make.Variable(
                                make.Modifiers(
                                    Collections.<Modifier>emptySet(),
                                    Collections.<AnnotationTree>emptyList()
                                ),
                                "marcela",
                                make.Identifier("File"),
                                null
                            )
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
    
    public void testAddFirst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(final File elaborada) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.insertMethodParameter(
                            node, 0,
                            make.Variable(
                                make.Modifiers(
                                    Collections.singleton(Modifier.FINAL),
                                    Collections.<AnnotationTree>emptyList()
                                ),
                                "elaborada",
                                make.Identifier("File"),
                                null
                            )
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
    
    public void testAddToIndex0() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(final File carqueja) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(final File elaborada,final File carqueja) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.insertMethodParameter(
                            node, 0,
                            make.Variable(
                                make.Modifiers(
                                    Collections.singleton(Modifier.FINAL),
                                    Collections.<AnnotationTree>emptyList()
                                ),
                                "elaborada",
                                make.Identifier("File"),
                                null
                            )
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
    
    public void testRemoveLast() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodParameter(
                            node, 0
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
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int para, int empezar, int sugerimos) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int para,  int sugerimos) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodParameter(
                            node, 1
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
    
    public void testRemoveLastTwo() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int para, int empezar, int sugerimos) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
//            "package hierbas.del.litoral;\n\n" +
//            "import java.io.File;\n\n" +
//            "public class Test {\n" +
//            "    public void taragui(int para) {\n" +
//            "    }\n" +
//            "}\n";
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int para ) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodParameter(node, 1);
                        copy = make.removeMethodParameter(copy, 1);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testRemoveFirstTwo() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int para, int empezar, int sugerimos) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
//            "package hierbas.del.litoral;\n\n" +
//            "import java.io.File;\n\n" +
//            "public class Test {\n" +
//            "    public void taragui(int sugerimos) {\n" +
//            "    }\n" +
//            "}\n";
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(  int sugerimos) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodParameter(node, 0);
                        copy = make.removeMethodParameter(copy, 0);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testSwap() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int empezar, int sugerimos) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui( int sugerimos,int empezar) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        VariableTree vt = node.getParameters().get(0);
                        MethodTree copy = make.removeMethodParameter(node, 0);
                        copy = make.addMethodParameter(copy, vt);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
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
