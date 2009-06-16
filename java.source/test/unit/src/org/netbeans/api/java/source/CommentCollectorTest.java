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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.CommentSetImpl;
import org.netbeans.modules.java.source.query.CommentSet;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.orgm">RKo</a>)
 * @todo documentation
 */
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
        String origin = "/** (COMM1) This comment belongs before class */\n" +
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
                CommentCollector cc = CommentCollector.getInstance();
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                cc.collect(workingCopy);

                service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());
                CommentPrinter printer = new CommentPrinter(service);
                CompilationUnitTree cu = workingCopy.getCompilationUnit();
                cu.accept(printer, null);

                JCTree.JCClassDecl clazz = (JCTree.JCClassDecl) cu.getTypeDecls().get(0);
                verify(clazz, CommentSet.RelativePosition.PRECEDING,  "/** (COMM1) This comment belongs before class */");
                verify(clazz, CommentSet.RelativePosition.INNER, "//TODO: (COMM2) This is just right under class (inside)");

                List<JCTree> trees = clazz.getMembers();
                for (JCTree tree : trees) {
                    if (tree.toString().contains("String str")) {
                        verify(tree, CommentSet.RelativePosition.INLINE, "//(COMM7) NOI18N");
                    } else if (tree.toString().contains("field")) {
                        verify(tree, CommentSet.RelativePosition.PRECEDING, "/** (COMM3) This belongs to encapsulate field */");
                    } else if (tree.toString().contains("method")) {
                        verify(tree, CommentSet.RelativePosition.PRECEDING
                                ,"//TODO: (COMM4) This is inside the class comment"
                                ,"/** (COMM5) method which do something */");
                        verify(tree, CommentSet.RelativePosition.INNER, "//TODO: (COMM6) Implement this method to do something");
                    }
                }
            }

            void verify(Tree tree, CommentSet.RelativePosition position, String... comments) {
                assertNotNull("Comments handler service not null", service);
                CommentSetImpl set = service.getComments(tree);
                java.util.List<Comment> cl = set.getComments(position);
                assertTrue("Unexpected size of " + tree.getKind() + " " 
                        + position.toString().toLowerCase() + 
                        " comments.", cl.size() == comments.length);
                Arrays.sort(comments);
                for (Comment comment : cl) {
                    String text = comment.getText().trim();
                    if (Arrays.binarySearch(comments, text) < 0) {
                        fail("There is no occurence of " + comment + " within list of required comments");
                    }
                }
            }
        };
        src.runModificationTask(task);

    }

    private static class CommentPrinter extends TreeScanner<Void, Void> {
        private CommentHandlerService service;

        CommentPrinter(CommentHandlerService service) {
            this.service = service;
        }

        @Override
        public Void scan(Tree node, Void aVoid) {
            return super.scan(node, defaultAction(node, aVoid));
        }

        protected Void defaultAction(Tree node, Void aVoid) {
            CommentSetImpl comments = service.getComments(node);
            if (comments.hasComments()) {
                String s = node.toString();
                System.out.println(s.substring(0, Math.min(20, s.length() - 1)).replace("[\n\r]", "") + " |> " + comments + "\n\n");
            }
            return aVoid;
        }
    }

}
