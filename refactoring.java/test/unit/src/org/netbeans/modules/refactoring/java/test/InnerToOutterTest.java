/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.InnerToOuterRefactoring;

public class InnerToOutterTest extends RefactoringTestBase {

    public InnerToOutterTest(String name) {
        super(name);
    }

    public void test178451() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "@A(foo=A.FOO) package t; public @interface A { public String foo(); public static final String FOO = \"foo\"; public static class F { } }"));
        performEncapsulateFieldsTest();
        verifyContent(src,
                      new File("t/F.java", "package t;\n\npublic class F {\n\n    A outer;\n\n    public F(A outer) {\n        this.outer = outer;\n    }\n}\n"),//TODO: why outer reference?
                      new File("t/A.java", "@A(foo=A.FOO) package t; public @interface A { public String foo(); public static final String FOO = \"foo\"; }"));
    }

    private void performEncapsulateFieldsTest() throws Exception {
        final InnerToOuterRefactoring[] r = new InnerToOuterRefactoring[1];
        
        JavaSource.forFileObject(src.getFileObject("t/A.java")).runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();
                
                ClassTree outter = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree inner = (ClassTree) outter.getMembers().get(outter.getMembers().size() - 1);

                TreePath tp = TreePath.getPath(cut, inner);
                r[0] = new InnerToOuterRefactoring(TreePathHandle.create(tp, parameter));
            }
        }, true);

        r[0].setClassName("F");
        r[0].setReferenceName(null);

        RefactoringSession rs = RefactoringSession.create("Session");
        assertNull(r[0].prepare(rs));
        assertNull(rs.doRefactoring(true));
    }

}