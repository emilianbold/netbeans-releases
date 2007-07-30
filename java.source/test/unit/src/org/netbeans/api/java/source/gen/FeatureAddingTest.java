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
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.JavaSource;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Pavel Flaska
 */
public class FeatureAddingTest extends GeneratorTestMDRCompat {

    /** Creates a new instance of FeatureAddingTest */
    public FeatureAddingTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(FeatureAddingTest.class);
//        suite.addTest(new FeatureAddingTest("testAddFieldToBeginning"));
//        suite.addTest(new FeatureAddingTest("testAddFieldToEnd"));
//        suite.addTest(new FeatureAddingTest("testAddFieldToEmpty"));
//        suite.addTest(new FeatureAddingTest("testAddNonAbstractMethod"));
        return suite;
    }

    public void testAddFieldToBeginning() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    \n" + 
            "    /* comment */\n" +
            "    Test(int a, long c, String s) {\n" +
            "    }\n\n" +
            "    void method() {\n" +
            "    }\n\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    int a;\n" +
            "    \n" + 
            "    /* comment */\n" +
            "    Test(int a, long c, String s) {\n" +
            "    }\n\n" +
            "    void method() {\n" +
            "    }\n\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree member = make.Variable(
                    make.Modifiers(
                        Collections.<Modifier>emptySet(),
                        Collections.<AnnotationTree>emptyList()
                    ),
                    "a",
                    make.PrimitiveType(TypeKind.INT), null
                );
                ClassTree copy = make.insertClassMember(clazz, 0, member);
                workingCopy.rewrite(clazz, copy);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddFieldToEnd() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    \n" + 
            "    /* comment */\n" +
            "    Test(int a, long c, String s) {\n" +
            "    }\n\n" +
            "    void method() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    \n" + 
            "    /* comment */\n" +
            "    Test(int a, long c, String s) {\n" +
            "    }\n\n" +
            "    void method() {\n" +
            "    }\n" +
            "    int a;\n" +
            "    \n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree member = make.Variable(
                    make.Modifiers(
                        Collections.<Modifier>emptySet(),
                        Collections.<AnnotationTree>emptyList()
                    ),
                    "a",
                    make.PrimitiveType(TypeKind.INT), null
                );
                ClassTree copy = make.addClassMember(clazz, member);
                workingCopy.rewrite(clazz, copy);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testAddFieldToEmpty() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "    String s;\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                BlockTree block = (BlockTree) workingCopy.getTreeUtilities().parseStatement("{ String s; }", null);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree member = (VariableTree) block.getStatements().get(0);
                ClassTree copy = make.insertClassMember(clazz, 0, member);
                workingCopy.rewrite(clazz, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddNonAbstractMethod() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public interface Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public interface Test {\n\n" +
            "    public void newlyCreatedMethod() throws java.io.IOException;\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree newMethod = make.Method(
                    make.Modifiers( 
                        Collections.singleton(Modifier.PUBLIC), // modifiers
                        Collections.<AnnotationTree>emptyList() // annotations
                    ), // modifiers and annotations
                    "newlyCreatedMethod", // name
                    make.PrimitiveType(TypeKind.VOID), // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    Collections.<VariableTree>emptyList(), // parameters
                    Collections.singletonList(make.Identifier("java.io.IOException")), // throws 
                    (BlockTree) null, // empty statement block
                    null // default value - not applicable here, used by annotations
                );
                workingCopy.rewrite(clazz, make.addClassMember(clazz, newMethod));
            }
            
        };
        src.runModificationTask(task).commit();
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
