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
import java.io.File;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.jackpot.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests modifying implements clause in the source.
 *
 * @author Pavel Flaska
 */
public class ClassImplementsTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of ClassImplementsTest */
    public ClassImplementsTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ClassImplementsTest.class);
//        suite.addTest(new ClassImplementsTest("testAddFirst"));
//        suite.addTest(new ClassImplementsTest("testAddFirstToExisting"));
//        suite.addTest(new ClassImplementsTest("testAddFirstTwo"));
//        suite.addTest(new ClassImplementsTest("testAddThirdToExisting"));
//        suite.addTest(new ClassImplementsTest("testRemoveAll"));
//        suite.addTest(new ClassImplementsTest("testRemoveMid"));
//        suite.addTest(new ClassImplementsTest("testRemoveFirst"));
//        suite.addTest(new ClassImplementsTest("testRemoveLast"));
//        suite.addTest(new ClassImplementsTest("testRemoveJust"));
//        suite.addTest(new ClassImplementsTest("testAddFirstTypeParam"));
//        suite.addTest(new ClassImplementsTest("testRemoveAllImplTypeParam"));
//        suite.addTest(new ClassImplementsTest("testAddFirstImplExtends"));
//        suite.addTest(new ClassImplementsTest("testRemoveAllImplExtends"));
        return suite;
    }
    
    public void testAddFirst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        ClassTree copy = make.addClassImplementsClause(
                            node, make.Identifier("List")
                        );
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddFirstTwo() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test extends Object {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test extends Object implements Collection, List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        ClassTree copy = make.addClassImplementsClause(
                            node, make.Identifier("Collection")
                        );
                        copy = make.addClassImplementsClause(
                            copy, make.Identifier("List")
                        );
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveAll() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test implements Serializable, List, Collection {\n" +
            "    public void taragui() {\n" +
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
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        ClassTree copy = make.removeClassImplementsClause(node, 0);
                        copy = make.removeClassImplementsClause(copy, 0);
                        copy = make.removeClassImplementsClause(copy, 0);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddThirdToExisting() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Serializable {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Serializable, Collection {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        ClassTree copy = make.addClassImplementsClause(
                            node, make.Identifier("Collection")
                        );
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstToExisting() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements Serializable, List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        ClassTree copy = make.insertClassImplementsClause(
                            node, 0, make.Identifier("Serializable")
                        );
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveMid() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Serializable, Collection {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Collection {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        ClassTree copy = make.removeClassImplementsClause(node, 1);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveFirst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        ClassTree copy = make.removeClassImplementsClause(node, 0);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveLast() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Collection, Serializable {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Collection {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        ClassTree copy = make.removeClassImplementsClause(node, 2);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveJust() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Collection {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        ClassTree copy = make.removeClassImplementsClause(node, 1);
                        copy = make.removeClassImplementsClause(copy, 0);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstTypeParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";
        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        ClassTree copy = make.addClassImplementsClause(
                            node, make.Identifier("List")
                        );
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveAllImplTypeParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";
        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        ClassTree copy = make.removeClassImplementsClause(
                            node, 0
                        );
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstImplExtends() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> extends Object {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> extends Object implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";
        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        ClassTree copy = make.addClassImplementsClause(
                            node, make.Identifier("List")
                        );
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveAllImplExtends() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> extends Object implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> extends Object {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";
        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        ClassTree copy = make.removeClassImplementsClause(
                            node, 0
                        );
                        changes.rewrite(node, copy);
                    }
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
