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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests correct fields addition and matching mechanism.
 * 
 * @author Pavel Flaska
 */
public class Field6Test extends GeneratorTest {
    
    /** Creates a new instance of MethodParametersTest */
    public Field6Test(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
//         suite.addTestSuite(Field6Test.class);
        suite.addTest(new Field6Test("testAddFieldToIndex0"));
        suite.addTest(new Field6Test("testRemoveInitialValue"));
        suite.addTest(new Field6Test("testAddFirstParameter"));
        return suite;
    }
    
    public void testAddFieldToIndex0() throws Exception {
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
            "    int field1;\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    ModifiersTree mods = make.Modifiers(Collections.<Modifier>emptySet());
                    PrimitiveTypeTree type = make.PrimitiveType(TypeKind.INT);
                    VariableTree var = make.Variable(mods, "field1", type, null);
                    ClassTree copy = make.addClassMember(node, var);
                    changes.rewrite(node, copy);
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testRemoveInitialValue() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    int removeInitialValueField = null;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    int removeInitialValueField;\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    super.visitVariable(node, p);
                    if ("removeInitialValueField".contentEquals(node.getName())) {
                        VariableTree vt = make.Variable(
                                node.getModifiers(), 
                                node.getName(),
                                node.getType(),
                                null);
                        changes.rewrite(node, vt);
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstParameter() throws Exception {
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
            "    public void taragui(int equilibrio) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        VariableTree vt = make.Variable(
                                make.Modifiers(Collections.<Modifier>emptySet()),
                                "equilibrio",
                                make.PrimitiveType(TypeKind.INT),
                                null);
                        MethodTree copy = make.addMethodParameter(node, vt);
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
