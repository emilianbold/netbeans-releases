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
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.transform.Transformer;

/**
 * Test name change.
 * 
 * @author Pavel Flaska
 */
public class Field5Test extends GeneratorTest {
    
    /**
     * Creates a new instance of Field5Test
     */
    public Field5Test(String testName) {
        super(testName);
    }
    
    public void testChangeParName() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package yerba.mate;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void hierbasDelLitoral(Test[] arrFile) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package yerba.mate;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test2 {\n" +
            "    public void hierbasDelLitoral(Test2[] arrFile) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        System.err.println("visitClass");
                        changes.rewrite(node, make.setLabel(node, "Test2"));
                    }
                    return null;
                }
                
                public Void visitIdentifier(IdentifierTree node, Object p) {
                    super.visitIdentifier(node, p);
                    if ("Test".contentEquals(node.getName())) {
                        System.err.println("visitIdentifier");
                        changes.rewrite(node, make.setLabel(node, "Test2"));
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
