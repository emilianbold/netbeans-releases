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

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.io.File;
import java.util.Collections;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.jackpot.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests method type parameters changes.
 * 
 * @author Pavel Flaska
 */
public class MethodTypeParametersTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of MethodParametersTest */
    public MethodTypeParametersTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(MethodTypeParametersTest.class);
//        suite.addTest(new MethodTypeParametersTest("testAddFirst"));
//        suite.addTest(new MethodTypeParametersTest("testAddFirstToExisting"));
//        suite.addTest(new MethodTypeParametersTest("testAddFirstTwo"));
//        suite.addTest(new MethodTypeParametersTest("testAddThirdToExisting"));
//        suite.addTest(new MethodTypeParametersTest("testRemoveAll"));
//        suite.addTest(new MethodTypeParametersTest("testRemoveMid"));
//        suite.addTest(new MethodTypeParametersTest("testRemoveFirst"));
//        suite.addTest(new MethodTypeParametersTest("testRemoveLast"));
//        suite.addTest(new MethodTypeParametersTest("testRemoveJust"));
        return suite;
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
            "    public <T> void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.addMethodTypeParameter(
                            node, make.TypeParameter("T", Collections.<ExpressionTree>emptyList())
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
            "    public <T,N> void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.addMethodTypeParameter(
                            node, make.TypeParameter("T", Collections.<ExpressionTree>emptyList())
                        );
                        copy = make.addMethodTypeParameter(
                            copy, make.TypeParameter("N", Collections.<ExpressionTree>emptyList())
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
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <E,N,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodTypeParameter(node, 0);
                        copy = make.removeMethodTypeParameter(copy, 0);
                        copy = make.removeMethodTypeParameter(copy, 0);
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
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T,N>void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T,N,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.addMethodTypeParameter(
                            node, make.TypeParameter("M", Collections.<ExpressionTree>emptyList())
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
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T,N,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <E,T,N,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.insertMethodTypeParameter(
                            node, 0, make.TypeParameter("E", Collections.<ExpressionTree>emptyList())
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
            "    public <T,N,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodTypeParameter(node, 1);
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
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T,N,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <N,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodTypeParameter(node, 0);
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
            "    public <T,N,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T,N>void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodTypeParameter(node, 2);
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
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T>void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodTypeParameter(node, 0);
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
