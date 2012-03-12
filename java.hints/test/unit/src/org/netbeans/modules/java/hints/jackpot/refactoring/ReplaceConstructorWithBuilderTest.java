/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.jackpot.refactoring;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import java.util.Collections;
import org.netbeans.modules.java.hints.jackpot.refactoring.ReplaceConstructorWithBuilderRefactoring.Setter;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class ReplaceConstructorWithBuilderTest extends RefTestBase {

    public ReplaceConstructorWithBuilderTest(String name) {
        super(name);
    }

    public void testReplaceWithBuilder() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("test/Test.java", "package test;\n public class Test {\n public Test(int i) {}\n private void t() {\n Test t = new Test(1);\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new Test(-1); } }"));

        performTest("create");

        assertContent(src,
                new File("test/Test.java", "package test;\n public class Test {\n public Test(int i) {}\n private void t() {\n Test t = new test.TestBuilder().setI(1).createTest();\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new test.TestBuilder().setI(-1).createTest(); } }"),
                new File("test/TestBuilder.java", "package test; public class TestBuilder { private int i; public TestBuilder() { } public TestBuilder setI(int i) { this.i = i; return this; } public Test createTest() { return new Test(i); } } "));
    }

    private void performTest(final String factoryName) throws Exception {
        final ReplaceConstructorWithBuilderRefactoring[] r = new ReplaceConstructorWithBuilderRefactoring[1];
        FileObject testFile = src.getFileObject("test/Test.java");

        JavaSource.forFileObject(testFile).runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                MethodTree var = (MethodTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(0);

                TreePath tp = TreePath.getPath(cut, var);
                r[0] = new ReplaceConstructorWithBuilderRefactoring(TreePathHandle.create(tp, parameter));
                r[0].setBuilderName("test.TestBuilder");
                Setter setter = new ReplaceConstructorWithBuilderRefactoring.Setter(
                        "setI",
                        "int",
                        null,
                        "i",
                        false);
                r[0].setSetters(Collections.singletonList(setter));
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Session");
        Thread.sleep(1000);
        r[0].prepare(rs);
        rs.doRefactoring(true);

        IndexingManager.getDefault().refreshIndex(src.getURL(), null);
        SourceUtils.waitScanFinished();
        //assertEquals(false, TaskCache.getDefault().isInError(src, true));
    }
}