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
import java.io.*;
import java.util.Collections;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import static org.netbeans.api.java.lexer.JavaTokenId.*;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.java.source.query.Query;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Pavel Flaska
 */
public class CommentsTest extends GeneratorTest {
    
    /** Creates a new instance of CommentsTest */
    public CommentsTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(CommentsTest.class);
//        suite.addTest(new CommentsTest("testAddStatement"));
//        suite.addTest(new CommentsTest("testAddJavaDocToMethod"));
//        suite.addTest(new CommentsTest("testGetComment1"));
//        suite.addTest(new CommentsTest("testAddJavaDocToExistingMethod"));
//        suite.addTest(new CommentsTest("testAddTwoEndLineCommments"));
//        suite.addTest(new CommentsTest("testCopyMethodWithCommments"));
//        suite.addTest(new CommentsTest("testAddStatementWithEmptyLine"));
        return suite;
    }

    public void testAddStatement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "        // test\n" +
            "        int a;\n" +
            "        /**\n" +
            "         * becko\n" +
            "         */\n" +
            "        int b;\n" +
            "        // cecko\n" +
            "        int c;\n" +
            "    }\n" +
            "\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                String bodyText = 
                        "{\n" +
                        "    // test\n" +
                        "    int a;\n" +
                        "    /**\n" +
                        "     * becko\n" +
                        "     */\n" +
                        "    int b;\n" +
                        "    // cecko\n" +
                        "    int c; // trail\n" +
                        "}";
                BlockTree block = make.createMethodBody(method, bodyText);
                workingCopy.rewrite(method.getBody(), block);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testGetComment1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "        // preceding comment\n" +
            "        int a; // trailing comment\n" +
            "        // what's up?" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
//                CommentHandler comments = workingCopy.getCommentHandler();
//                CommentSet s = comments.getComments(method.getBody().getStatements().get(0));
//                System.err.println(s);
            }

        };
        src.runModificationTask(task).commit();
    }
    
    // #99329
    public void testAddJavaDocToMethod() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    Test() {\n" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    Test() {\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * Comentario\n" +
            "     */\n" +
            "    public void nuevoMetodo() {\n" +
            "    }\n" +
            "\n" +
            "}\n";
        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(final WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);
                
                TreeMaker make = copy.getTreeMaker();
                ClassTree node = (ClassTree) copy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = make.Method(
                        make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                        "nuevoMetodo",
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        "{ }",
                        null
                );
                make.addComment(method, Comment.create(
                        Comment.Style.JAVADOC, 
                        Query.NOPOS, 
                        Query.NOPOS, 
                        1, // to ensure indentation
                        "Comentario"), 
                        true
                );
                ClassTree clazz = make.addClassMember(node, method);
                copy.rewrite(node, clazz);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddJavaDocToExistingMethod() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void test(int a) {\n" +
            "    }\n\n" +
            "}\n"
            );
        
        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree node = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) node.getMembers().get(1);
                MethodTree copy = make.Method(method.getModifiers(),
                        method.getName(),
                        method.getReturnType(),
                        method.getTypeParameters(),
                        method.getParameters(),
                        method.getThrows(),
                        method.getBody(),
                        (ExpressionTree) method.getDefaultValue()
                );
                make.addComment(copy, Comment.create(
                        Comment.Style.JAVADOC, 
                        Query.NOPOS, 
                        Query.NOPOS, 
                        1, // to ensure indentation
                        "/** Comentario \n*/"),
                        true
                );
                workingCopy.rewrite(method, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        System.err.println(TestUtilities.copyFileToString(testFile));
        assertTrue(TestUtilities.copyFileToString(testFile), TestUtilities.copyFileToString(testFile).contains("Comentario"));
    }

    public void testAddTwoEndLineCommments() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "        // TODO: Process the button click action. Return value is a navigation\n" +
            "        // case name where null will return to the same page.\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                String bodyText = "{ \n" +
                    "        // TODO: Process the button click action. Return value is a navigation\n" +
                    "        // case name where null will return to the same page.\n" +
                    "        return null; }";
                BlockTree block = make.createMethodBody(method, bodyText);
                workingCopy.rewrite(method.getBody(), block);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    // issue #100829
    public void testCopyMethodWithCommments() throws Exception {
        testFile = new File(getWorkDir(), "Origin.java");
        TestUtilities.copyStringToFile(testFile, 
            "public class Origin {\n" +
            "    /**\n" +
            "     * comment\n" +
            "     * @return 1\n" +
            "     */\n" +
            "    int method() {\n" +
            "        // TODO: Process the button click action. Return value is a navigation\n" +
            "        // case name where null will return to the same page.\n" +
            "        return 1;\n" +
            "    }\n" +
            "}\n"
            );
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "import java.io.File;\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "import java.io.File;\n" +
            "public class Test {\n" +
            "    /**\n" +
            "     * comment\n" +
            "     * @return 1\n" +
            "     */\n" +
            "    int method() {\n" +
            "        // TODO: Process the button click action. Return value is a navigation\n" +
            "        // case name where null will return to the same page.\n" +
            "        return 1;\n" +
            "    }\n" +
            "\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws IOException {
                wc.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = wc.getCompilationUnit();
                TreeMaker make = wc.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                
                TypeElement originClass = wc.getElements().getTypeElement("Origin");
                assertNotNull(originClass);
                
                ClassTree origClassTree = wc.getTrees().getTree(originClass);
                Tree method = origClassTree.getMembers().get(1);
                assertNotNull(method);
                
                wc.rewrite(clazz, make.addClassMember(clazz, method));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    

    public void testAddStatementWithEmptyLine() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "\n" +
            "        // test\n" +
            "        int a;\n" +
            "\n" +
            "        /*\n" +
            "         * Test\n" +
            "         * Test2\n" +
            "         */\n" +
            "        int b;\n" +
            "    }\n" +
            "\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                String bodyText = 
                        "{\n" +
                        "    \n" +
                        "    // test\n" +
                        "    int a;\n" +
                        "    \n" +
                        "    /*\n" +
                        "     * Test\n" +
                        "     * Test2\n" +
                        "     */\n" +
                        "    int b;\n" +
                        "}";
                BlockTree block = make.createMethodBody(method, bodyText);
                workingCopy.rewrite(method.getBody(), block);
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
        return"";
    }

}
