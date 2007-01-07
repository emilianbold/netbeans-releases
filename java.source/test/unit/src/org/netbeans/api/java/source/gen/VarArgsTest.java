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
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.util.Collections;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 * Variable arguments:
 * abstract void method(int a, Object...);
 *
 * @author Pavel Flaska
 */
public class VarArgsTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of VarArgsTest */
    public VarArgsTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(VarArgsTest.class);
        return suite;
    }

    public void testMethodWithVarargs() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n\n" +
            "    void method(Object[] a) {\n" +
            "    }\n\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n\n" +
            "    void method(Object... a) {\n" +
            "    }\n\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
              
                public Void visitVariable(VariableTree node, Object p) {
                    super.visitVariable(node, p);
                    long VARARGS = 1L<<34;
                    VariableTree tCopy = make.Variable(
                            make.Modifiers(VARARGS, Collections.<AnnotationTree>emptyList()),
                            node.getName(),
                            node.getType(),
                            null
                    );
                    changes.rewrite(node, tCopy);
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
}
