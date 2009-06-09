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
import static com.sun.source.tree.Tree.*;
import java.io.File;
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 * Modifying operator through the API methods.
 * 
 * @author Pavel Flaska
 */
public class OperatorsTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of OperatorsTest 
     * 
     * @param name 
     */
    public OperatorsTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(OperatorsTest.class);
//        suite.addTest(new OperatorsTest("testAndToOrOperAssign"));
//        suite.addTest(new OperatorsTest("testChangeBinaryOperator"));
//        suite.addTest(new OperatorsTest("testChangeUnaryOperator"));
        return suite;
    }

    /**
     *
     */
    public void testAndToOrOperAssign() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int a = 10;\n" +
            "        int b = 20;\n" +
            "        a &= b;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int a = 10;\n" +
            "        int b = 20;\n" +
            "        a |= b;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(2);
                CompoundAssignmentTree t = (CompoundAssignmentTree) est.getExpression();
                workingCopy.rewrite(t, make.CompoundAssignment(Kind.OR_ASSIGNMENT, t.getVariable(), t.getExpression()));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     *
     */
    public void testChangeBinaryOperator() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int c = (0x0f | 7);\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int c = (0x0f & 7);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree lvd = (VariableTree) method.getBody().getStatements().get(0);
                ParenthesizedTree pt = (ParenthesizedTree) lvd.getInitializer();
                BinaryTree bt = (BinaryTree) pt.getExpression();
                workingCopy.rewrite(bt, make.Binary(Kind.AND, bt.getLeftOperand(), bt.getRightOperand()));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     *
     */
    public void testChangeUnaryOperator() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int c;\n" +
            "        c++;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int c;\n" +
            "        c--;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(1);
                UnaryTree ut = (UnaryTree) est.getExpression();
                workingCopy.rewrite(ut, make.Unary(Kind.POSTFIX_DECREMENT, ut.getExpression()));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testChangeBinaryOperator2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        Object o = null;\n" +
            "        boolean c = o == null && o instanceof String;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        Object o = null;\n" +
            "        boolean c = o != null || !(o instanceof String);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree lvd = (VariableTree) method.getBody().getStatements().get(1);
                ExpressionTree orig = lvd.getInitializer();
                ExpressionTree nue = negate(workingCopy, orig);
                workingCopy.rewrite(orig, nue);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    private static ExpressionTree negate(WorkingCopy wc, ExpressionTree input) {
        TreeMaker make = wc.getTreeMaker();

        switch (input.getKind()) {
            case CONDITIONAL_AND:
                BinaryTree andT = (BinaryTree) input;

                return make.Binary(Kind.CONDITIONAL_OR, negate(wc, andT.getLeftOperand()), negate(wc, andT.getRightOperand()));
            case CONDITIONAL_OR:
                BinaryTree orT = (BinaryTree) input;

                return make.Binary(Kind.CONDITIONAL_AND, negate(wc, orT.getLeftOperand()), negate(wc, orT.getRightOperand()));
                
            case EQUAL_TO:
                BinaryTree eqT = (BinaryTree) input;

                return make.Binary(Kind.NOT_EQUAL_TO, eqT.getLeftOperand(), eqT.getRightOperand());

            case PARENTHESIZED:
                return make.Parenthesized(negate(wc, ((ParenthesizedTree) input).getExpression()));

            case LOGICAL_COMPLEMENT:
                ExpressionTree withoutComplement = ((UnaryTree) input).getExpression();

                if (withoutComplement.getKind() == Kind.PARENTHESIZED) {
                    withoutComplement = ((ParenthesizedTree) withoutComplement).getExpression();
                }

                return withoutComplement;

            default:
                return make.Unary(Kind.LOGICAL_COMPLEMENT, make.Parenthesized(input));
        }
    }

    public void testChangeUnary2() throws Exception {
        String test = "public class Test { void m(int x) { int y = x+|+; } }";
        String golden = "public class Test { void m(int x) { int y = --x; } }";
        testFile = new File(getWorkDir(), "Test.java");
        final int index = test.indexOf("|");
        assertTrue(index != -1);
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                Tree node = copy.getTreeUtilities().pathFor(index).getLeaf();
                assertEquals(Kind.POSTFIX_INCREMENT, node.getKind());
                UnaryTree node2 = (UnaryTree) node;
                IdentifierTree original = (IdentifierTree) node2.getExpression();
                System.out.println("node: " + node);
                TreeMaker make = copy.getTreeMaker();
                UnaryTree modified = make.Unary(Kind.PREFIX_DECREMENT, original);
                System.out.println("modified: " + modified);
                copy.rewrite(node, modified);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }

    public void testUnary158150() throws Exception {
        String test = "public class Test { void m(int x) { int y = -| - x; } }";
        String golden = "public class Test { void m(int x) { int y = +x; } }";
        testFile = new File(getWorkDir(), "Test.java");
        final int index = test.indexOf("|");
        assertTrue(index != -1);
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                Tree node = copy.getTreeUtilities().pathFor(index).getLeaf();
                assertEquals(Kind.UNARY_MINUS, node.getKind());
                UnaryTree node2 = (UnaryTree) node;
                UnaryTree original = (UnaryTree) node2.getExpression();
                System.out.println("node: " + node);
                TreeMaker make = copy.getTreeMaker();
                UnaryTree modified = make.Unary(Kind.UNARY_PLUS, original.getExpression());
                System.out.println("modified: " + modified);
                copy.rewrite(node, modified);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
            
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

    
    
}
