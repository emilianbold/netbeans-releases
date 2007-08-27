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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
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
public class MethodBodyTest extends GeneratorTest {
    
    /** Creates a new instance of MethodBodyTest */
    public MethodBodyTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(MethodBodyTest.class);
//        suite.addTest(new MethodBodyTest("testAddFirstStatement"));
//        suite.addTest(new MethodBodyTest("testAddBodyText"));
//        suite.addTest(new MethodBodyTest("testAddVarDecl"));
        return suite;
    }
    
    /**
     * Add first method body statement
     */
    public void testAddFirstStatement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        System.out.println(\"test\");\n" +
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
                ExpressionStatementTree est = make.ExpressionStatement(
                    make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.MemberSelect(
                            make.MemberSelect(
                                make.Identifier("System"),
                                "out"
                            ),
                            "println"
                        ),
                        Collections.<ExpressionTree>singletonList(
                            make.Literal("test")
                        )
                    )
                );
                workingCopy.rewrite(block, make.addBlockStatement(block, est));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Add method body as a text
     */
    public void testAddBodyText() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        System.out.println(\"test\");\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree newBody = make.createMethodBody(method, "{ System.out.println(\"test\"); }");
                workingCopy.rewrite(method.getBody(), newBody);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * "Map env = new HashMap();"
     */
    public void testAddVarDecl() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "import java.util.HashMap;\n" +
            "import java.util.Map;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        Map env = new HashMap();\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                TypeElement hashMapClass = workingCopy.getElements().getTypeElement("java.util.HashMap"); // NOI18N
                ExpressionTree hashMapEx = treeMaker.QualIdent(hashMapClass);
                TypeElement mapClass = workingCopy.getElements().getTypeElement("java.util.Map");// NOI18N
                ExpressionTree mapEx = treeMaker.QualIdent(mapClass);
                NewClassTree mapConstructor = treeMaker.NewClass(
                        null,
                        Collections.<ExpressionTree>emptyList(),
                        hashMapEx,
                        Collections.<ExpressionTree>emptyList(), null
                );
                VariableTree vt = treeMaker.Variable( treeMaker.Modifiers(
                        Collections.<Modifier>emptySet(),
                        Collections.<AnnotationTree>emptyList()
                        ), "env", mapEx, mapConstructor
                );
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                workingCopy.rewrite(method.getBody(), treeMaker.addBlockStatement(method.getBody(), vt));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * diff switch statement
     */
    public void XtestSwitchStatement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        int i = 3;\n" +
            "        switch (i) {\n" +
            "            case 1: System.err.println(); break;\n" +
            "            default: break;\n" +
            "        }\n" + 
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        int i = 3;\n" +
            "        switch (i) {\n" +
            "            case 1: System.err.println(); break;\n" +
            "            case 2: System.err.println(); break;\n" +
            "            default:  break;\n" +
            "        }\n" + 
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                SwitchTree switchStatement = (SwitchTree) method.getBody().getStatements().get(1);
                
                List<CaseTree> cases = new LinkedList<CaseTree>();
                
                cases.add(treeMaker.Case(treeMaker.Literal(1), switchStatement.getCases().get(0).getStatements()));
                cases.add(treeMaker.Case(treeMaker.Literal(2), switchStatement.getCases().get(0).getStatements()));
                cases.add(treeMaker.Case(null, Collections.singletonList(treeMaker.Break(null))));
                
                workingCopy.rewrite(switchStatement, treeMaker.Switch(switchStatement.getExpression(), cases));
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
