/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.IntroduceParameterRefactoring;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class IntroduceParameterTest extends RefactoringTestBase {

    public IntroduceParameterTest(String name) {
        super(name);
    }

    public void testIntroduceParameter() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t;\n"
                + "public class A {\n"
                + "public int add(int a, int b) {\n"
                + "    return a*(a+b);\n"
                + "}\n"
                + "public void m() {\n"
                + "    System.out.println(add(1,1));\n"
                + "}\n"
                + "}\n"
                + "class F extends A {\n"
                + "    public int add(int a, int b) {\n"
                + "        return super.add(a, b);\n"
                + "    }\n"
                + "    public void bar() {\n"
                + "        System.out.println(add(1,add(23,1)) + 1);\n"
                + "    }\n"
                + "}\n"));

        performIntroduce(src.getFileObject("t/A.java"), source.indexOf("a+b") );
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "public int add(int a, int b, int introduced) {\n"
                + "    return a* introduced;\n"
                + "}\n"
                + "public void m() {\n"
                + "    System.out.println(add(1,1, 1 + 1));\n"
                + "}\n"
                + "}\n"
                + "class F extends A {\n"
                + "    public int add(int a, int b, int introduced) {\n"
                + "        return super.add(a, b, introduced);\n"
                + "    }\n"
                + "    public void bar() {\n"
                + "        System.out.println(add(1,add(23,1, 23 + 1), 1 + add(23,1, 23 + 1)) + 1);\n"
                + "    }\n"
                + "}\n"));

    }

    private void performIntroduce(FileObject source, final int position, Problem... expectedProblems) throws Exception {
        final IntroduceParameterRefactoring[] r = new IntroduceParameterRefactoring[1];

        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                TreePath tp = parameter.getTreeUtilities().pathFor(position);

                r[0] = new IntroduceParameterRefactoring(TreePathHandle.create(tp, parameter));
                r[0].setParameterName("introduced");
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Introduce Parameter");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);
    }
    
    private boolean problemIsFatal(List<Problem> problems) {
        for (Problem problem : problems) {
            if (problem.isFatal()) {
                return true;
            }
        }
        return false;
    }
}
