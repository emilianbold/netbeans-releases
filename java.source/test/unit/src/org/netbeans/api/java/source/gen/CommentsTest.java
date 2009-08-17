/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import java.io.*;
import java.util.Collections;
import java.util.EnumSet;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Comment.Style;
import static org.netbeans.modules.java.source.save.PositionEstimator.*;
import static org.netbeans.api.java.lexer.JavaTokenId.*;
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.save.PositionEstimator;
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
    
    public void DISABLEtestGetComment1() throws Exception {
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
    public void DISABLEtestAddJavaDocToMethod() throws Exception {
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
                        NOPOS, 
                        NOPOS, 
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
    
    public void DISABLEtestAddJavaDocToExistingMethod() throws Exception {
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
                        NOPOS, 
                        NOPOS, 
                        NOPOS, // to ensure indentation
                        "Comentario"),
                        true
                );
                workingCopy.rewrite(method, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        System.err.println(TestUtilities.copyFileToString(testFile));
        assertTrue(TestUtilities.copyFileToString(testFile), TestUtilities.copyFileToString(testFile).contains("Comentario"));
    }

    public void DISABLEtestAddTwoEndLineCommments() throws Exception {
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
            "\n" +
            "    /**\n" +
            "     * comment\n" +
            "     * @return 1\n" +
            "     */\n" +
            "    int method() {\n" +
            "        // TODO: Process the button click action. Return value is a navigation\n" +
            "        // case name where null will return to the same page.\n" +
            "        return 1;\n" +
            "    }\n" +
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
                method = GeneratorUtilities.get(wc).importComments(method, wc.getTrees().getPath(originClass).getCompilationUnit());
                wc.rewrite(clazz, make.addClassMember(clazz, method));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    

    public void DISABLEDtestAddStatementWithEmptyLine() throws Exception {
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

    /*
     * http://www.netbeans.org/issues/show_bug.cgi?id=113315
     */
    public void DISABLEtestAddJavaDoc113315() throws Exception {
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
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * What's up?\n" +
            "     */\n" +
            "    void methoda() {\n" +
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
                MethodTree method = make.Method(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "methoda",
                        make.Identifier("void"),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        "{}",
                        null
                );
                int no = PositionEstimator.NOPOS;
                make.addComment(method, Comment.create(Style.JAVADOC, no, no, no, "What's up?\n"), true);
                ClassTree copy = make.addClassMember(clazz, method);
                workingCopy.rewrite(clazz, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /*
     * http://www.netbeans.org/issues/show_bug.cgi?id=100829
     */
    public void DISABLEtestCopyDoc100829_1() throws Exception {
        File secondFile = new File(getWorkDir(), "Test2.java");
        TestUtilities.copyStringToFile(secondFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test2 {\n" +
            "}\n"
            );
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "        // Test\n" +
            "        System.out.println(\"Slepitchka\");\n" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test2 {\n" +
            "\n" +
            "    void method() {\n" +
            "        // Test\n" +
            "        System.out.println(\"Slepitchka\");\n" +
            "    }\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(secondFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                Element e = workingCopy.getElements().getTypeElement("Test");
                ClassTree newClazz = (ClassTree) workingCopy.getTrees().getTree(e);
                CompilationUnitTree secondCut = workingCopy.getTrees().getPath(e).getCompilationUnit();
                newClazz = make.addClassMember(clazz, GeneratorUtilities.get(workingCopy).importComments(newClazz.getMembers().get(1), secondCut));
                workingCopy.rewrite(clazz, newClazz);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(secondFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /*
     * http://www.netbeans.org/issues/show_bug.cgi?id=100829
     */
    public void DISABLEtestCopyDoc100829_2() throws Exception {
        File secondFile = new File(getWorkDir(), "Test2.java");
        TestUtilities.copyStringToFile(secondFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test2 {\n" +
            "}\n"
            );
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "        // Test\n" +
            "        int a = 0;\n" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test2 {\n" +
            "\n" +
            "    void method() {\n" +
            "        // Test\n" +
            "        int a = 0;\n" +
            "    }\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(secondFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                Element e = workingCopy.getElements().getTypeElement("Test");
                ClassTree newClazz = (ClassTree) workingCopy.getTrees().getTree(e);
                CompilationUnitTree secondCut = workingCopy.getTrees().getPath(e).getCompilationUnit();
                newClazz = make.addClassMember(
                        clazz, 
                        GeneratorUtilities.get(workingCopy).importComments(newClazz.getMembers().get(1), secondCut)
                );
                workingCopy.rewrite(clazz, newClazz);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(secondFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * http://www.netbeans.org/issues/show_bug.cgi?id=121898
     */
    public void DISABLEtestRemoveMethodWithComment() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "\n" +
            "\n" +
            "/*\n" +
            " * To change this template, choose Tools | Templates\n" +
            " * and open the template in the editor.\n" +
            " */\n" +
            "\n" +
            "package javaapplication11;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "\n" +
            "/**\n" +
            " *\n" +
            " * @author jp159440\n" +
            " */\n" +
            "public class Class1 extends ClassA{\n" +
            "                \n" +
            "    /**\n" +
            "     * a\n" +
            "     * @param x b\n" +
            "     * @return c\n" +
            "     * @throws java.io.IOException d\n" +
            "     */\n" +
            "    public int method(int x) throws IOException {\n" +
            "        \n" +
            "        return 1;        \n" +
            "    }\n" +
            "\n" +
            "    \n" +
            "    \n" +
            "}\n" +
            "\n"
            );
        String golden =
            "\n" +
            "\n" +
            "/*\n" +
            " * To change this template, choose Tools | Templates\n" +
            " * and open the template in the editor.\n" +
            " */\n" +
            "\n" +
            "package javaapplication11;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "\n" +
            "/**\n" +
            " *\n" +
            " * @author jp159440\n" +
            " */\n" +
            "public class Class1 extends ClassA{\n" +
            "}\n" +
            "\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                workingCopy.rewrite(clazz, make.removeClassMember(clazz, 1));
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
