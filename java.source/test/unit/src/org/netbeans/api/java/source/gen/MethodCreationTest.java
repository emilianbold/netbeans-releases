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
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.TypeParameterTree;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Pavel Flaska
 */
public class MethodCreationTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of MethodCreationTest */
    public MethodCreationTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
//        suite.addTestSuite(MethodCreationTest.class);
        suite.addTest(new MethodCreationTest("testAddFirst"));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
        return suite;
    }

    /*
     * create the method:
     *
     * public <T> void taragui(List menta, Object carqueja, int dulce, boolean compuesta) throws IOException {
     * }
     */
    public void testAddFirst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test {\n\n" +
            "public <T> void taragui(List menta, T carqueja, int dulce, boolean compuesta,\n" +
            "                        boolean logrando) throws IOException {\n" +
            "}\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        List parametersList = new ArrayList(5);
                        ModifiersTree mods = make.Modifiers(EnumSet.noneOf(Modifier.class));
                        parametersList.add(make.Variable(mods, "menta", make.Identifier("List"), null));
                        parametersList.add(make.Variable(mods, "carqueja", make.Identifier("T"), null));
                        parametersList.add(make.Variable(mods, "dulce", make.PrimitiveType(TypeKind.INT), null));
                        parametersList.add(make.Variable(mods, "compuesta", make.PrimitiveType(TypeKind.BOOLEAN), null));
                        parametersList.add(make.Variable(mods, "logrando", make.PrimitiveType(TypeKind.BOOLEAN), null));
                        MethodTree newMethod = make.Method(
                                make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)), // modifiers - public
                                "taragui",  // name - targui
                                make.PrimitiveType(TypeKind.VOID), // return type - void
                                Collections.<TypeParameterTree>singletonList(make.TypeParameter("T", Collections.<ExpressionTree>emptyList())), // type parameter - <T>
                                parametersList, // parameters
                                Collections.<ExpressionTree>singletonList(make.Identifier("IOException")), // throws
                                make.Block(Collections.<StatementTree>emptyList(), false),
                                null // default value - not applicable
                        ); 
                        ClassTree copy = make.addClassMember(
                            node, newMethod
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
    
    String getGoldenPckg() {
        return "";
    }
    
    String getSourcePckg() {
        return "";
    }
   
}
