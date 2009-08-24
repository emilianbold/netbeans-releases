/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.CommentSetImpl;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenHierarchy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.orgm">RKo</a>)
 * @todo documentation
 */
@SuppressWarnings({"unchecked"})
public class CommentCollectorTest extends NbTestCase {


    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(CommentCollectorTest.class);
        return suite;
    }


    /**
     * Constructs a test case with the given name.
     *
     * @param name name of the testcase
     */
    public CommentCollectorTest(String name) {
        super(name);
    }

    static JavaSource getJavaSource(File aFile) throws IOException {
        FileObject testSourceFO = FileUtil.toFileObject(aFile);
        assertNotNull(testSourceFO);
        return JavaSource.forFileObject(testSourceFO);
    }

    @Test
    public void testCollector() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        final String origin = "/** (COMM1) This comment belongs before class */\n" +
                "public class Clazz {\n" +
                "\n\n\n//TODO: (COMM2) This is just right under class (inside)" +
                "\n\n\n\n\n\n" +
                "/** (COMM3) This belongs to encapsulate field */\n" +
                "public int field = 9;\n\n //TODO: (COMM4) This is inside the class comment\n" +
                "/** (COMM5) method which do something */\n" +
                "public void method() {\n" +
                "\t//TODO: (COMM6) Implement this method to do something \n" +
                "}\n" +
                "private String str = \"string object\" //(COMM7) NOI18N\n" +
                "}\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            protected CommentHandlerService service;

            public void run(WorkingCopy workingCopy) throws Exception {
//                CommentCollector cc = CommentCollector.getInstance();
                workingCopy.toPhase(JavaSource.Phase.PARSED);
//                cc.collect(workingCopy);
                TokenSequence<JavaTokenId> seq = (TokenSequence<JavaTokenId>) TokenHierarchy.create(origin, JavaTokenId.language()).tokenSequence();
                CompilationUnitTree cu = workingCopy.getCompilationUnit();
                TranslateIdentifier ti = new TranslateIdentifier(workingCopy, true, false, seq);
                ti.translate(cu);

                service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());
                CommentPrinter printer = new CommentPrinter(service);
                cu.accept(printer, null);


                TreeVisitor<Void, Void> w = new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void aVoid) {
                        verify(node, CommentSet.RelativePosition.PRECEDING, service, "/** (COMM1) This comment belongs before class */");
                        return super.visitClass(node, aVoid);
                    }

                    @Override
                    public Void visitVariable(VariableTree node, Void aVoid) {
                        if (node.toString().contains("field")) {
                            verify(node, CommentSet.RelativePosition.PRECEDING, service, 
                                    "//TODO: (COMM2) This is just right under class (inside)", 
                                    "/** (COMM3) This belongs to encapsulate field */");
                        }
                        return super.visitVariable(node, aVoid);
                    }

                    @Override
                    public Void visitLiteral(LiteralTree node, Void aVoid) {
                        if (node.toString().contains("string")) {
                            verify(node, CommentSet.RelativePosition.INLINE, service, "//(COMM7) NOI18N");
                        }
                        return super.visitLiteral(node, aVoid);
                    }

                    @Override
                    public Void visitMethod(MethodTree node, Void aVoid) {
                        if (node.toString().contains("method")) {
                            verify(node, CommentSet.RelativePosition.PRECEDING
                                    , service, "//TODO: (COMM4) This is inside the class comment"
                                    , "/** (COMM5) method which do something */");
                            verify(node.getBody(), CommentSet.RelativePosition.INNER, service, "//TODO: (COMM6) Implement this method to do something");
                        }
                        return super.visitMethod(node, aVoid);
                    }
                };
                cu.accept(w, null);

            }


        };
        src.runModificationTask(task);

    }


    public void DtestMethod() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String origin = "\n" +
                "import java.io.File;\n" +
                "\n" +
                "public class Test {\n" +
                "\n" +
                "    void method() {\n" +
                "        // Test\n" +
                "        System.out.println(\"Slepitchka\");\n" +
                "    }\n" +
                "\n" +
                "}\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            protected CommentHandlerService service;

            public void run(WorkingCopy workingCopy) throws Exception {
                CommentCollector cc = CommentCollector.getInstance();
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                cc.collect(workingCopy);

                service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());
                CommentPrinter printer = new CommentPrinter(service);
                CompilationUnitTree cu = workingCopy.getCompilationUnit();
                cu.accept(printer, null);

                JCTree.JCClassDecl clazz = (JCTree.JCClassDecl) cu.getTypeDecls().get(0);
                final boolean[] processed = new boolean[1];
                TreeVisitor<Void, Void> w = new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitIdentifier(IdentifierTree node, Void aVoid) {
                        if (node.getName().contentEquals("System")) {
                            verify(node, CommentSet.RelativePosition.PRECEDING, service, "// Test");
                            processed[0] = true;
                        }
                        return super.visitIdentifier(node, aVoid);
                    }
                };
                clazz.accept(w, null);
                if (!processed[0]) {
                    fail("Tree has not been processed!");
                }
            }


        };
        src.runModificationTask(task);

    }

    public void DtestMethod2() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        final String origin =
                "public class Origin {\n" +
                        "    /** * comment * @return 1 */\n" +
                        "    int method() {\n" +
                        "        // TODO: Process the button click action. Return value is a navigation\n" +
                        "        // case name where null will return to the same page.\n" +
                        "        return 1;\n" +
                        "    }\n" +
                        "}\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            protected CommentHandlerService service;

            public void run(WorkingCopy workingCopy) throws Exception {
//                CommentCollector cc = CommentCollector.getInstance();
                workingCopy.toPhase(JavaSource.Phase.PARSED);
//                cc.collect(workingCopy);
                TokenSequence<JavaTokenId> seq = (TokenSequence<JavaTokenId>) TokenHierarchy.create(origin, JavaTokenId.language()).tokenSequence();
                TranslateIdentifier ti = new TranslateIdentifier(workingCopy, true, false, seq);
                CompilationUnitTree cu = workingCopy.getCompilationUnit();
                ti.translate(cu);

                service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());
                CommentPrinter printer = new CommentPrinter(service);
                cu.accept(printer, null);

                JCTree.JCClassDecl clazz = (JCTree.JCClassDecl) cu.getTypeDecls().get(0);
                TreeVisitor<Void, Void> w = new TreeScanner<Void, Void>() {

                    @Override
                    public Void visitReturn(ReturnTree node, Void aVoid) {
                        verify(node, CommentSet.RelativePosition.PRECEDING, service,
                                "// TODO: Process the button click action. Return value is a navigation",
                                "// case name where null will return to the same page."
                        );
                        return super.visitReturn(node, aVoid);
                    }

                    @Override
                    public Void visitMethod(MethodTree node, Void aVoid) {
                        if (node.getName().contentEquals("method")) {
                            verify(node, CommentSet.RelativePosition.PRECEDING, service,
                                    "/** * comment * @return 1 */"
                            );
                        }
                        return super.visitMethod(node, aVoid);
                    }


                };
                clazz.accept(w, null);
            }


        };
        src.runModificationTask(task);

    }


    void verify(Tree tree, CommentSet.RelativePosition position, CommentHandler service, String... comments) {
        assertNotNull("Comments handler service not null", service);
        CommentSet set = service.getComments(tree);
        java.util.List<Comment> cl = set.getComments(position);
        assertEquals("Unexpected size of " + tree.getKind() + " "
                + position.toString().toLowerCase() +
                " comments", cl.size(), comments.length);
        Arrays.sort(comments);
        for (Comment comment : cl) {
            String text = comment.getText().trim();
            if (Arrays.binarySearch(comments, text) < 0) {
                fail("There is no occurence of " + comment + " within list of required comments");
            }
        }
    }

    private static class CommentPrinter extends TreeScanner<Void, Void> {
        private CommentHandlerService service;

        CommentPrinter(CommentHandlerService service) {
            this.service = service;
        }

        @Override
        public Void scan(Tree node, Void aVoid) {
            defaultAction(node, aVoid);
            return super.scan(node, aVoid);
        }

        protected Void defaultAction(Tree node, Void aVoid) {
            CommentSetImpl comments = service.getComments(node);
            if (comments.hasComments()) {
                String s = node.toString();
                System.out.println(node.getKind()
                        + ": '"
                        + s.substring(0, Math.min(20, s.length() - 1)).replace("\n", "\\n").replace("\n\r", "\\n")
                        + "' |> " + comments + "\n\n");
            }
            return aVoid;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree node, Void aVoid) {
            defaultAction(node, aVoid);
            return super.visitCompilationUnit(node, aVoid);
        }
    }

}
