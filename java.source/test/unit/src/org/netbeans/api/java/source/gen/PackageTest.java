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
import java.io.IOException;
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;

/**
 * Test packages.
 * 
 * @author Pavel Flaska
 */
public class PackageTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of PackageTest */
    public PackageTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(PackageTest.class);
//        suite.addTest(new PackageTest("testChangePackage"));
//        suite.addTest(new PackageTest("testChangeDefToNamedPackage"));
//        suite.addTest(new PackageTest("testChangeDefToNamedPackageWithImport"));
//        suite.addTest(new PackageTest("testChangeToDefPackage"));
//        suite.addTest(new PackageTest("testPackageRenameWithAnnotation"));
        return suite;
    }
    
    /**
     * Change package declaration 'package org.nothing;' to
     * 'package com.unit;'.
     */
    public void testChangePackage() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "package org.nothing;\n\n" +
            "class Test {\n" +
            "}\n"
            );
        String golden = 
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "package com.unit;\n\n" +
            "class Test {\n" +
            "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.CompilationUnit(
                    make.Identifier("com.unit"),
                    cut.getImports(),
                    cut.getTypeDecls(),
                    cut.getSourceFile()
                );
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Remove the package declartion, i.e. make the class part of
     * default package.
     */
    public void testChangeToDefPackage() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "package org.nothing;\n\n" +
            "class Test {\n" +
            "}\n"
            );
        String golden = 
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "\n\n" +
            "class Test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.CompilationUnit(
                    null,
                    cut.getImports(),
                    cut.getTypeDecls(),
                    cut.getSourceFile()
                );
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Remove the package declartion, i.e. make the class part of
     * default package.
     */
    public void testChangeDefToNamedPackage() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "class Test {\n" +
            "}\n"
            );
        String golden = 
            "package gro.snaebten.seludom.avaj;\n" +
            "\n" +
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "class Test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                    CompilationUnitTree copy = make.CompilationUnit(
                        make.Identifier("gro.snaebten.seludom.avaj"),
                        cut.getImports(),
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                    );
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Remove the package declartion, i.e. make the class part of
     * default package.
     */
    public void testChangeDefToNamedPackageWithImport() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "import gro;\n\n" +
            "class Test {\n" +
            "}\n"
            );
        String golden = 
            "package gro.snaebten.seludom.avaj;\n" +
            "\n" +
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "import gro;\n\n" +
            "class Test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                    CompilationUnitTree copy = make.CompilationUnit(
                        make.Identifier("gro.snaebten.seludom.avaj"),
                        cut.getImports(),
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                    );
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #93045: Package annotation removed when renaming package
     */
    public void testPackageRenameWithAnnotation() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "@SuppressWarnings()\n" +
            "package javaapplication1;\n");
        String golden = 
            "@SuppressWarnings()\n" +
            "package app;\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ExpressionTree pckgName = workingCopy.getCompilationUnit().getPackageName();
                workingCopy.rewrite(pckgName, make.setLabel(pckgName, "app"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    // not important for this test
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

    
}
