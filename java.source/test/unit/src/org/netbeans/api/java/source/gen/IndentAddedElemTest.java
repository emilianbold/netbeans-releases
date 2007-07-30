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

import com.sun.source.tree.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import junit.textui.TestRunner;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests indentation of newly added elements
 * @author Max Sauer
 */
public class IndentAddedElemTest extends GeneratorTest {
    
    public IndentAddedElemTest(String name) {
        super(name);
    }
    
    /**
     * Adds tests to suite
     * @return created suite
     */
    public static NbTestSuite suite() {
        //NbTestSuite suite = new NbTestSuite();
        NbTestSuite suite = new NbTestSuite(IndentAddedElemTest.class);
        //suite.addTest(new IndentMethodTest("testAddMethodToEmpty"));
        return suite;
    }
    
    /**
     * Adding of methods
     */
    public void testAddMethodToEmpty() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package com.max.test.alfa;\n\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package com.max.test.alfa;\n\n" +
                "public class Test {\n" +
                "\n" +
                "    public double Eval(int param) {\n" +
                "    }\n" +
                "}\n";
        
        process(
                new Transformer<Void, Object>() {
            @Override
            public Void visitClass(ClassTree node, Object p) {
                super.visitClass(node, p);
                if ("Test".contentEquals(node.getSimpleName())) {
                    ModifiersTree parMods = make.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
                    VariableTree param = make.Variable(parMods, "param", make.PrimitiveType(TypeKind.INT), null);
                    List<VariableTree> parList = new ArrayList<VariableTree>(1);
                    parList.add(param);
                    MethodTree member = make.Method(
                            make.Modifiers(
                            Collections.singleton(Modifier.PUBLIC), // modifiers
                            Collections.<AnnotationTree>emptyList() // annotations
                            ), // modifiers and annotations
                            "Eval", // name
                            make.PrimitiveType(TypeKind.DOUBLE), // return type
                            Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                            parList, // parameters
                            Collections.<ExpressionTree>emptyList(), // throws
                            make.Block(Collections.<StatementTree>emptyList(), false), // empty statement block
                            null // default value - not applicable here, used by annotations
                            );
                    
                    ClassTree copy = make.addClassMember(node, member);
                    changes.rewrite(node, copy);
                }
                return null;
            }
        }
        
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    /**
     * Adding of fields
     */
    public void testAddFieldEmpty() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package com.max.test.alfa;\n\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package com.max.test.alfa;\n\n" +
                "public class Test {\n" +
                "    private double value;\n" +
                "}\n";
        
        process(
                new Transformer<Void, Object>() {
            @Override
            public Void visitClass(ClassTree node, Object p) {
                super.visitClass(node, p);
                if ("Test".contentEquals(node.getSimpleName())) {
                    ModifiersTree modTree = make.Modifiers(EnumSet.of(Modifier.PRIVATE));
                    VariableTree member = make.Variable(
                            make.Modifiers(
                                modTree,
                                Collections.<AnnotationTree>emptyList()
                            ),
                            "value", //name
                            make.PrimitiveType(TypeKind.DOUBLE), null
                            );
                    
                    ClassTree copy = make.addClassMember(node, member);
                    changes.rewrite(node, copy);
                }
                return null;
            }
        }
        
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }
    
    String getSourcePckg() {
        return "";
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
}
