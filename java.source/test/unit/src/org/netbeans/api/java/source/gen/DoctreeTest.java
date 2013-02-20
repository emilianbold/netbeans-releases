/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.DocTreeScanner;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePathScanner;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import static org.netbeans.api.java.source.gen.GeneratorTestBase.getJavaSource;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Ralph Benjamin Ruijs
 */
public class DoctreeTest extends GeneratorTestBase {

    /**
     * Creates a new instance of DoctreeTest
     */
    public DoctreeTest(String name) {
        super(name);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return"";
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(DoctreeTest.class);
        return suite;
    }
    
    public void testAddDocCommentTagA() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * Test method\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * Test method\n" +
            "     * @param test\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = (DocTrees) wc.getTrees();
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitDocComment(DocCommentTree docComment, Void p) {
                                ParamTree param = make.Param(false, make.DocIdentifier("test"), new LinkedList<DocTree>());
                                DocCommentTree newDoc = make.DocComment(docComment,
                                        docComment.getFirstSentence(),
                                        docComment.getBody(),
                                        Collections.singletonList(param));
                                wc.rewrite(mt, docComment, newDoc);
                                return super.visitDocComment(docComment, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddDocCommentTagB() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * @param test\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = (DocTrees) wc.getTrees();
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitDocComment(DocCommentTree docComment, Void p) {
                                ParamTree param = make.Param(false, make.DocIdentifier("test"), new LinkedList<DocTree>());
                                DocCommentTree newDoc = make.DocComment(docComment,
                                        docComment.getFirstSentence(),
                                        docComment.getBody(),
                                        Collections.singletonList(param));
                                wc.rewrite(mt, docComment, newDoc);
                                return super.visitDocComment(docComment, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddDocCommentTagC() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * \n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * \n" +
            "     * @param test\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = (DocTrees) wc.getTrees();
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitDocComment(DocCommentTree docComment, Void p) {
                                ParamTree param = make.Param(false, make.DocIdentifier("test"), new LinkedList<DocTree>());
                                DocCommentTree newDoc = make.DocComment(docComment,
                                        docComment.getFirstSentence(),
                                        docComment.getBody(),
                                        Collections.singletonList(param));
                                wc.rewrite(mt, docComment, newDoc);
                                return super.visitDocComment(docComment, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveDocCommentTag() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * Test method\n" +
            "     * \n" +
            "     * @param test\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * Test method\n" +
            "     * \n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = (DocTrees) wc.getTrees();
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitDocComment(DocCommentTree docComment, Void p) {
                                DocCommentTree newDoc = make.DocComment(docComment,
                                        docComment.getFirstSentence(),
                                        docComment.getBody(),
                                        new LinkedList<DocTree>());
                                wc.rewrite(mt, docComment, newDoc);
                                return super.visitDocComment(docComment, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveInlineDocTag() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method\n"
                + "     * \n"
                + "     * @param test {@link #test}\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method\n"
                + "     * \n"
                + "     * @param test\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = (DocTrees) wc.getTrees();
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitParam(ParamTree node, Void p) {
                                List<DocTree> description = new LinkedList<DocTree>();
                                for (DocTree t : node.getDescription()) {
                                    if (t.getKind() != DocTree.Kind.LINK) {
                                        description.add(t);
                                    }
                                }
                                ParamTree param = make.Param(node.isTypeParameter(), node.getName(), description);
                                wc.rewrite(mt, node, param);
                                return super.visitParam(node, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeInlineDocTag() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method\n"
                + "     * \n"
                + "     * @param test {@link #test foo}\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method\n"
                + "     * \n"
                + "     * @param test {@link #test test method}\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = (DocTrees) wc.getTrees();
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitLink(LinkTree node, Void p) {
                                List<DocTree> text = Collections.singletonList((DocTree) make.Text("test method"));
                                LinkTree newLink = make.Link(node.getReference(), text);
                                wc.rewrite(mt, node, newLink);
                                return super.visitLink(node, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
}
