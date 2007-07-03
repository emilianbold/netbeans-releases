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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.VariableTree;
import java.io.File;

import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Pavel Flaska
 */
public class FieldGroupTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of FieldGroupTest */
    public FieldGroupTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(FieldGroupTest.class);
        return suite;
    }
    
    /**
     */
    public void testFieldGroup1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int a, b, c, d;\n" +
            "    int e;\n" +
            "    \n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int a, b, c, d;\n" +
            "    int ecko;\n" +
            "    \n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(5);
                workingCopy.rewrite(vt, make.setLabel(vt, "ecko"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     */
    public void testFieldGroup2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int e;\n" +
            "    int a, b, c, d;\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int ecko;\n" +
            "    int a, b, c, d;\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "ecko"));
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
