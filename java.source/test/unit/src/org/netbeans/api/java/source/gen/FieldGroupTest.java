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

import java.io.File;
import com.sun.source.tree.*;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
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
//        suite.addTest(new FieldGroupTest("testFieldGroup1"));
//        suite.addTest(new FieldGroupTest("testFieldGroup2"));
//        suite.addTest(new FieldGroupTest("testFieldGroup3"));
//        suite.addTest(new FieldGroupTest("testFieldGroup4"));
//        suite.addTest(new FieldGroupTest("testFieldGroup5"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody1"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody2"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody3"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody4"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody5"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody6"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody7"));
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
       
    public void testFieldGroup3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, becko = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, what = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(2);
                workingCopy.rewrite(vt, make.setLabel(vt, "what"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroup4() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, becko = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int acko, becko = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "acko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroup5() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, becko = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, becko = 10, cecko = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(3);
                workingCopy.rewrite(vt, make.setLabel(vt, "cecko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, becko = 10, c = 25;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, becko = 10, cecko = 25;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(3);
                workingCopy.rewrite(vt, make.setLabel(vt, "cecko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, becko = 10, c = 25;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, b = 10, c = 25;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(2);
                workingCopy.rewrite(vt, make.setLabel(vt, "b"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, becko = 10, c = 25;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int acko, becko = 10, c = 25;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "acko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    
    public void testFieldGroupInBody4() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, becko = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, becko = 10, cecko = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(2);
                workingCopy.rewrite(vt, make.setLabel(vt, "cecko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody5() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, becko = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, b = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "b"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody6() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, becko = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int acko, becko = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(0);
                workingCopy.rewrite(vt, make.setLabel(vt, "acko"));
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
