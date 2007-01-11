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

import java.io.File;
import java.util.Collections;
import com.sun.source.tree.*;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 * Test class add couple of body statements. It test statements creation and
 * addition to body.
 * 
 * @author Pavel Flaska
 */
public class BodyStatementTest extends GeneratorTest {
    
    /** Creates a new instance of BodyStatementTest */
    public BodyStatementTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(BodyStatementTest.class);
//        suite.addTest(new BodyStatementTest("testNullLiteral"));
//        suite.addTest(new BodyStatementTest("testBooleanLiteral"));
//        suite.addTest(new BodyStatementTest("testRenameInIfStatement"));
//        suite.addTest(new BodyStatementTest("testRenameInLocalDecl"));
//        suite.addTest(new BodyStatementTest("testRenameInInvocationPars"));
//        suite.addTest(new BodyStatementTest("testAddMethodToAnnInTry"));
//        suite.addTest(new BodyStatementTest("testReturnNotDoubled"));
//        suite.addTest(new BodyStatementTest("testForNotRegen"));
//        suite.addTest(new BodyStatementTest("testAssignLeft"));
//        suite.addTest(new BodyStatementTest("testAssignRight"));
//        suite.addTest(new BodyStatementTest("testAssignBoth"));
//        suite.addTest(new BodyStatementTest("testReturn"));
//        suite.addTest(new BodyStatementTest("testPlusBinary"));
//        suite.addTest(new BodyStatementTest("testRenameInWhile"));
//        suite.addTest(new BodyStatementTest("testRenameInDoWhile"));
//        suite.addTest(new BodyStatementTest("testRenameInForEach"));
//        suite.addTest(new BodyStatementTest("testRenameInSyncro"));
//        suite.addTest(new BodyStatementTest("testRenameInCatch"));
        return suite;
    }
    
    /**
     * Adds 'System.err.println(null);' statement to the method body. 
     */
    public void testNullLiteral() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        ;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        ;\n" +
            "    System.err.println(null);\n" + 
            "}\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree statement = make.ExpressionStatement(
                    make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.MemberSelect(
                            make.MemberSelect(
                                make.Identifier("System"),
                                "err"
                            ),
                            "println"
                        ),
                        Collections.singletonList(
                            make.Literal(null)
                        )
                    )
                );
                BlockTree copy = make.addBlockStatement(method.getBody(), statement);
                workingCopy.rewrite(method.getBody(), copy);
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Adds 'System.err.println(true);' statement to the method body. 
     */
    public void testBooleanLiteral() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        ;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        ;\n" +
            "    System.err.println(true);\n" + 
            "}\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree statement = make.ExpressionStatement(
                    make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.MemberSelect(
                            make.MemberSelect(
                                make.Identifier("System"),
                                "err"
                            ),
                            "println"
                        ),
                        Collections.singletonList(
                            make.Literal(Boolean.TRUE)
                        )
                    )
                );
                BlockTree copy = make.addBlockStatement(method.getBody(), statement);
                workingCopy.rewrite(method.getBody(), copy);
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Renames el to element in method parameter and if statement
     */
    public void testRenameInIfStatement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void action666(Element el) {\n" +
            "        if (el.getName().equalsIgnoreCase(\"flaskuvElement\")) {\n" +
            "            System.err.println(\"Win!\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n");
        String golden = 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void action666(Element element) {\n" +
            "        if (element.getName().equalsIgnoreCase(\"flaskuvElement\")) {\n" +
            "            System.err.println(\"Win!\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                // rename in parameter
                VariableTree vt = method.getParameters().get(0);
                VariableTree parCopy = make.setLabel(vt, "element");
                workingCopy.rewrite(vt, parCopy);
                // no need to check kind
                // rename in if
                IfTree statementTree = (IfTree) method.getBody().getStatements().get(0);
                ParenthesizedTree condition = (ParenthesizedTree) statementTree.getCondition();
                MethodInvocationTree invocation = (MethodInvocationTree) condition.getExpression();
                MemberSelectTree select = (MemberSelectTree) invocation.getMethodSelect();
                invocation = (MethodInvocationTree) select.getExpression();
                select = (MemberSelectTree) invocation.getMethodSelect();
                IdentifierTree identToRename = (IdentifierTree) select.getExpression();
                IdentifierTree copy = make.setLabel(identToRename, "element");
                workingCopy.rewrite(identToRename, copy);
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Renames el to element in method parameter and if statement
     */
    public void testRenameInLocalDecl() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void action666(Element el) {\n" +
            "        String name = el.getName();\n" +
            "    }\n" +
            "}\n");
        String golden = 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void action666(Element element) {\n" +
            "        String name = element.getName();\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                // rename in parameter
                VariableTree vt = method.getParameters().get(0);
                VariableTree parCopy = make.setLabel(vt, "element");
                workingCopy.rewrite(vt, parCopy);
                // no need to check kind
                VariableTree statementTree = (VariableTree) method.getBody().getStatements().get(0);
                MethodInvocationTree invocation = (MethodInvocationTree) statementTree.getInitializer();
                MemberSelectTree select = (MemberSelectTree) invocation.getMethodSelect();
                IdentifierTree identToRename = (IdentifierTree) select.getExpression();
                IdentifierTree copy = make.setLabel(identToRename, "element");
                workingCopy.rewrite(identToRename, copy);
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Renames el to element in method parameter and if statement
     */
    public void testRenameInInvocationPars() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void action666(Element el) {\n" +
            "        Collections.singleton(el);\n" +
            "    }\n" +
            "}\n");
        String golden = 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void action666(Element element) {\n" +
            "        Collections.singleton(element);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                // rename in parameter
                VariableTree vt = method.getParameters().get(0);
                VariableTree parCopy = make.setLabel(vt, "element");
                workingCopy.rewrite(vt, parCopy);
                // no need to check kind
                // rename in if
                ExpressionStatementTree expressionStmt = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                MethodInvocationTree invocation = (MethodInvocationTree) expressionStmt.getExpression();
                IdentifierTree identToRename = (IdentifierTree) invocation.getArguments().get(0);
                IdentifierTree copy = make.setLabel(identToRename, "element");
                workingCopy.rewrite(identToRename, copy);
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * Adds method to annonymous class declared in try section.
     */
    public void testAddMethodToAnnInTry() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "   public void method() {\n" +
            "        try {\n" +
            "            new Runnable() {\n" +
            "            };\n" +
            "        } finally {\n" +
            "            System.err.println(\"Got a problem.\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "   public void method() {\n" +
            "        try {\n" +
            "            new Runnable() {\n" +
            "                public void run() {\n" +
            "                }\n\n" +
            "            };\n" +
            "        } finally {\n" +
            "            System.err.println(\"Got a problem.\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                // rename in parameter
                TryTree tryStmt = (TryTree) method.getBody().getStatements().get(0);
                ExpressionStatementTree exprStmt = (ExpressionStatementTree) tryStmt.getBlock().getStatements().get(0);
                NewClassTree newClassTree = (NewClassTree) exprStmt.getExpression();
                ClassTree anonClassTree = newClassTree.getClassBody();
                MethodTree methodToAdd = make.Method(
                    make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                    "run",
                    make.PrimitiveType(TypeKind.VOID),
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    make.Block(Collections.<StatementTree>emptyList(), false),
                    null
                );
                ClassTree copy = make.addClassMember(anonClassTree, methodToAdd);
                workingCopy.rewrite(anonClassTree, copy);
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Check return statement is not doubled. (#90806)
     */
    public void testReturnNotDoubled() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "   public Object method() {\n" +
            "        try {\n" +
            "            new Runnable() {\n" +
            "            }\n" +
            "            return null;\n" +
            "        } finally {\n" +
            "            System.err.println(\"Got a problem.\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "   public Object method() {\n" +
            "        try {\n" +
            "            new Runnable() {\n" +
            "                public void run() {\n" +
            "                }\n\n" +
            "            }\n" +
            "            return null;\n" +
            "        } finally {\n" +
            "            System.err.println(\"Got a problem.\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                // rename in parameter
                TryTree tryStmt = (TryTree) method.getBody().getStatements().get(0);
                BlockTree tryBlock = (BlockTree) tryStmt.getBlock();
                ExpressionStatementTree exprStmt = (ExpressionStatementTree) tryStmt.getBlock().getStatements().get(0);
                NewClassTree newClassTree = (NewClassTree) exprStmt.getExpression();
                ClassTree anonClassTree = newClassTree.getClassBody();
                MethodTree methodToAdd = make.Method(
                    make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                    "run",
                    make.PrimitiveType(TypeKind.VOID),
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    make.Block(Collections.<StatementTree>emptyList(), false),
                    null
                );
                ClassTree copy = make.addClassMember(anonClassTree, methodToAdd);
                workingCopy.rewrite(anonClassTree, copy);
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Check 'for' body is not regenerated. (#91061)
     */
    public void testForNotRegen() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "   public Object method() {\n" +
            "        for (int var2 = 0; var2 < 10; var2++) {\n" +
            "           // comment\n" +
            "           System.out.println(var2); // What a ... comment\n" +
            "           // comment\n" +
            "           List l;\n" +
            "        }\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "   public Object method() {\n" +
            "        for (int newVar = 0; newVar < 10; newVar++) {\n" +
            "           // comment\n" +
            "           System.out.println(newVar); // What a ... comment\n" +
            "           // comment\n" +
            "           List l;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ForLoopTree forLoop = (ForLoopTree) method.getBody().getStatements().get(0);
                // rewrite in initializer
                VariableTree initalizer = (VariableTree) forLoop.getInitializer().get(0);
                workingCopy.rewrite(initalizer, make.setLabel(initalizer, "newVar"));
                
                // rewrite in condition
                BinaryTree condition = (BinaryTree) forLoop.getCondition();
                IdentifierTree ident = (IdentifierTree) condition.getLeftOperand();
                workingCopy.rewrite(ident, make.setLabel(ident, "newVar"));
                
                ExpressionStatementTree update = (ExpressionStatementTree) forLoop.getUpdate().get(0);
                UnaryTree unary = (UnaryTree) update.getExpression();
                ident = (IdentifierTree) unary.getExpression();
                workingCopy.rewrite(ident, make.setLabel(ident, "newVar"));
                
                // and finally in the body
                BlockTree block = (BlockTree) forLoop.getStatement();
                ExpressionStatementTree systemOut = (ExpressionStatementTree) block.getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) systemOut.getExpression();
                ident = (IdentifierTree) mit.getArguments().get(0);
                workingCopy.rewrite(ident, make.setLabel(ident, "newVar"));
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test for left right side of assignment
     */
    public void testAssignLeft() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        this.key = key;\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        this.key2 = key;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                AssignmentTree assignment = (AssignmentTree) est.getExpression();
                MemberSelectTree mstCopy = make.setLabel((MemberSelectTree) assignment.getVariable(), "key2");
                workingCopy.rewrite(assignment.getVariable(), mstCopy);
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test for right side of assignment
     */
    public void testAssignRight() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        this.key = key;\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        this.key = key2;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                AssignmentTree assignment = (AssignmentTree) est.getExpression();
                IdentifierTree copy = make.setLabel((IdentifierTree) assignment.getExpression(), "key2");
                workingCopy.rewrite(assignment.getExpression(), copy);
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test for right side of assignment
     */
    public void testAssignBoth() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        this.key = key;\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        this.key2 = key2;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                AssignmentTree assignment = (AssignmentTree) est.getExpression();
                MemberSelectTree mstCopy = make.setLabel((MemberSelectTree) assignment.getVariable(), "key2");
                workingCopy.rewrite(assignment.getVariable(), mstCopy);
                IdentifierTree copy = make.setLabel((IdentifierTree) assignment.getExpression(), "key2");
                workingCopy.rewrite(assignment.getExpression(), copy);
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test for return rename
     */
    public void testReturn() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        return nullanen;\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        return nullanen2;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ReturnTree rejturn = (ReturnTree) method.getBody().getStatements().get(0);
                workingCopy.rewrite(rejturn.getExpression(), make.Identifier("nullanen2"));
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test in PLUS rename
     */
    public void testPlusBinary() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        return \"[\" + key + \"; \" + value + \"]\"\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        return \"[\" + key2 + \"; \" + value + \"]\"\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ReturnTree rejturn = (ReturnTree) method.getBody().getStatements().get(0);
                BinaryTree in = (BinaryTree) rejturn.getExpression();
                for (int i = 0; i < 3; i++) {
                    in = (BinaryTree) in.getLeftOperand();
                }
                IdentifierTree ident = (IdentifierTree) in.getRightOperand();
                workingCopy.rewrite(ident, make.Identifier("key2"));
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Rename in while
     */
    public void testRenameInWhile() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        int i = 0;\n" +
            "        while (i < 10) {\n" +
            "            i = i + 1;\n" +
            "        }\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        int counter = 0;\n" +
            "        while (counter < 10) {\n" +
            "            counter = counter + 1;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                workingCopy.rewrite(var, make.setLabel(var, "counter"));
                
                WhileLoopTree whileLoop = (WhileLoopTree) method.getBody().getStatements().get(1);
                ParenthesizedTree paren = (ParenthesizedTree) whileLoop.getCondition();
                BinaryTree lessThan = (BinaryTree) paren.getExpression();
                IdentifierTree left = (IdentifierTree) lessThan.getLeftOperand();
                workingCopy.rewrite(left, make.setLabel(left, "counter"));
                
                ExpressionStatementTree expr = (ExpressionStatementTree) ((BlockTree) whileLoop.getStatement()).getStatements().get(0);
                AssignmentTree assign = (AssignmentTree) expr.getExpression();
                left = (IdentifierTree) assign.getVariable();
                workingCopy.rewrite(left, make.setLabel(left, "counter"));
                BinaryTree right = (BinaryTree) assign.getExpression();
                left = (IdentifierTree) right.getLeftOperand();
                workingCopy.rewrite(left, make.setLabel(left, "counter"));
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Rename in do while
     */
    public void testRenameInDoWhile() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        int i = 0;\n" +
            "        do {\n" +
            "            i++;\n" +
            "        } while (i > 10);\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        int counter = 0;\n" +
            "        do {\n" +
            "            counter++;\n" +
            "        } while (counter > 10);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                workingCopy.rewrite(var, make.setLabel(var, "counter"));
                
                DoWhileLoopTree doWhileLoop = (DoWhileLoopTree) method.getBody().getStatements().get(1);
                ParenthesizedTree paren = (ParenthesizedTree) doWhileLoop.getCondition();
                BinaryTree lessThan = (BinaryTree) paren.getExpression();
                IdentifierTree left = (IdentifierTree) lessThan.getLeftOperand();
                workingCopy.rewrite(left, make.setLabel(left, "counter"));
                
                ExpressionStatementTree expr = (ExpressionStatementTree) ((BlockTree) doWhileLoop.getStatement()).getStatements().get(0);
                UnaryTree unary = (UnaryTree) expr.getExpression();
                workingCopy.rewrite(unary.getExpression(), make.setLabel(unary.getExpression(), "counter"));
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Rename in for each
     */
    public void testRenameInForEach() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        List l = new ArrayList();\n" +
            "        for (Object o : l) {\n" +
            "            o.toString();\n" +
            "        }\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        List list = new ArrayList();\n" +
            "        for (Object object : list) {\n" +
            "            object.toString();\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                workingCopy.rewrite(var, make.setLabel(var, "list"));
                
                EnhancedForLoopTree forEach = (EnhancedForLoopTree) method.getBody().getStatements().get(1);
                var = forEach.getVariable();
                workingCopy.rewrite(var, make.setLabel(var, "object"));
                IdentifierTree ident = (IdentifierTree) forEach.getExpression();
                workingCopy.rewrite(ident, make.setLabel(ident, "list"));
                BlockTree body = (BlockTree) forEach.getStatement();
                ExpressionStatementTree est = (ExpressionStatementTree) body.getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MemberSelectTree mst = (MemberSelectTree) mit.getMethodSelect();
                
                workingCopy.rewrite(mst.getExpression(), make.setLabel(mst.getExpression(), "object"));
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * #92187: Test rename in synchronized
     */
    public void testRenameInSyncro() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        Object lock = new Object();\n" +
            "        \n" +
            "        synchronized(lock) {\n" +
            "            int a = 20;\n" +
            "            lock.wait();\n" +
            "        }\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        Object zamek = new Object();\n" +
            "        \n" +
            "        synchronized(zamek) {\n" +
            "            int a = 20;\n" +
            "            zamek.wait();\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                workingCopy.rewrite(var, make.setLabel(var, "zamek"));
                
                SynchronizedTree syncro = (SynchronizedTree) method.getBody().getStatements().get(1);
                ParenthesizedTree petecko = (ParenthesizedTree) syncro.getExpression();
                IdentifierTree ident = (IdentifierTree) petecko.getExpression();
                workingCopy.rewrite(ident, make.setLabel(ident, "zamek"));
                BlockTree body = (BlockTree) syncro.getBlock();
                ExpressionStatementTree est = (ExpressionStatementTree) body.getStatements().get(1);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MemberSelectTree mst = (MemberSelectTree) mit.getMethodSelect();
                
                workingCopy.rewrite(mst.getExpression(), make.setLabel(mst.getExpression(), "zamek"));
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test rename in catch
     */
    public void testRenameInCatch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        Object zamek = new Object();\n" +
            "        try {\n" +
            "            zamek.wait();\n" +
            "        } catch (InterruptedException ex) {\n" +
            "            ex.printStackTrace();\n" +
            "        }\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        Object zamek = new Object();\n" +
            "        try {\n" +
            "            zamek.wait();\n" +
            "        } catch (InterruptedException vyjimka) {\n" +
            "            vyjimka.printStackTrace();\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                workingCopy.rewrite(var, make.setLabel(var, "zamek"));
                TryTree tryTree = (TryTree) method.getBody().getStatements().get(1);
                CatchTree ct = tryTree.getCatches().get(0);
                workingCopy.rewrite(ct.getParameter(), make.setLabel(ct.getParameter(), "vyjimka"));
                BlockTree body = (BlockTree) ct.getBlock();
                ExpressionStatementTree est = (ExpressionStatementTree) body.getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MemberSelectTree mst = (MemberSelectTree) mit.getMethodSelect();
                workingCopy.rewrite(mst.getExpression(), make.setLabel(mst.getExpression(), "vyjimka"));
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    // methods not used in this test.
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
        // TODO code application logic here
    }

}
