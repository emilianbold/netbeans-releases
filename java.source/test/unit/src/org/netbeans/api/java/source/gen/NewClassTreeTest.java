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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 * Test modifications of NewClassTree.
 * 
 * @author Jan Lahoda and Pavel Flaska
 */
public class NewClassTreeTest extends GeneratorTest {
    
    /** Creates a new instance of TryTest */
    public NewClassTreeTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(NewClassTreeTest.class);
        return suite;
    }

    /**
     * Renames variable in try body.
     */ 
    public void testRemoveClassBody() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        new Runnable() {\n" +
            "            public void run() {}\n" +
            "        };\n" +
            "    }\n" +
            "    private static class X {}\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        new X() ;\n" + //XXX: there should be no space between '()' and ';' 
            "    }\n" +
            "    private static class X {}\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree st = (ExpressionStatementTree) method.getBody().getStatements().get(0); 
                NewClassTree nct = (NewClassTree) st.getExpression();
                workingCopy.rewrite(nct, make.NewClass(null, Collections.<ExpressionTree>emptyList(), make.Identifier("X"), nct.getArguments(), null));
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
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
