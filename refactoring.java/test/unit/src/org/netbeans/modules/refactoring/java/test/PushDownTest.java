/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PushDownRefactoring;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class PushDownTest extends RefactoringTestBase {

    public PushDownTest(String name) {
        super(name);
   }
    
    public void testPushDownMethodException() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pushdown/A.java", "package pushdown; public class A extends B {}"),
                new File("pushdown/C.java", "package pushdown; public class C extends B {}"),
                new File("pushdown/B.java", "package pushdown; import java.io.IOException; public class B { public int a() throws IOException { return 1; } }"));
        performPushDown(src.getFileObject("pushdown/B.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pushdown/A.java", "package pushdown; import java.io.IOException; public class A extends B { public int a() throws IOException { return 1; } }"),
                new File("pushdown/C.java", "package pushdown; import java.io.IOException; public class C extends B { public int a() throws IOException { return 1; } }"),
                new File("pushdown/B.java", "package pushdown; import java.io.IOException; public class B {}"));
    }

    public void testPushDownComments() throws Exception { // #208705 - Duplicate comments after Push Down
        writeFilesAndWaitForScan(src,
                new File("pushdown/A.java", "package pushdown; public class A extends B {}"),
                new File("pushdown/C.java", "package pushdown; public class C extends B {}"),
                new File("pushdown/B.java", "package pushdown; public class B { /** * This is a method */ public int a() { return 1; } }"));
        performPushDown(src.getFileObject("pushdown/B.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pushdown/A.java", "package pushdown; public class A extends B { /** * This is a method */ public int a() { return 1; } }"),
                new File("pushdown/C.java", "package pushdown; public class C extends B { /** * This is a method */ public int a() { return 1; } }"),
                new File("pushdown/B.java", "package pushdown; public class B {}"));
    }

    public void testPushDownAbstractComments() throws Exception { // #208705 - Duplicate comments after Push Down
        writeFilesAndWaitForScan(src,
                new File("pushdown/A.java", "package pushdown; public class A extends B {}"),
                new File("pushdown/C.java", "package pushdown; public class C extends B {}"),
                new File("pushdown/B.java", "package pushdown; public class B { /** * This is a method */ public int a() { return 1; } }"));
        performPushDown(src.getFileObject("pushdown/B.java"), 1, Boolean.TRUE);
        verifyContent(src,
                new File("pushdown/A.java", "package pushdown; public class A extends B { /** * This is a method */ public int a() { return 1; } }"),
                new File("pushdown/C.java", "package pushdown; public class C extends B { /** * This is a method */ public int a() { return 1; } }"),
                new File("pushdown/B.java", "package pushdown; public abstract class B { /** * This is a method */ public abstract int a(); }"));
    }

    public void testPushDownField() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pushdown/A.java", "package pushdown; public class A extends B {}"),
                new File("pushdown/C.java", "package pushdown; public class C extends B {}"),
                new File("pushdown/B.java", "package pushdown; public class B { public int a; }"));
        performPushDown(src.getFileObject("pushdown/B.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pushdown/A.java", "package pushdown; public class A extends B { public int a; }"),
                new File("pushdown/C.java", "package pushdown; public class C extends B { public int a; }"),
                new File("pushdown/B.java", "package pushdown; public class B {}"));
    }

    public void testPushDownMethodMakeAbstract() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pushdown/A.java", "package pushdown; public class A extends B {}"),
                new File("pushdown/C.java", "package pushdown; public class C extends B {}"),
                new File("pushdown/B.java", "package pushdown; public class B { public int a() { return 1; } }"));
        performPushDown(src.getFileObject("pushdown/B.java"), 1, Boolean.TRUE);
        verifyContent(src,
                new File("pushdown/A.java", "package pushdown; public class A extends B { public int a() { return 1; } }"),
                new File("pushdown/C.java", "package pushdown; public class C extends B { public int a() { return 1; } }"),
                new File("pushdown/B.java", "package pushdown; public abstract class B { public abstract int a(); }"));
    }

    public void testPushDownMethodNested() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pushdown/A.java", "package pushdown; public class A extends B {} class Nested { }"),
                new File("pushdown/C.java", "package pushdown; public class C extends B {}"),
                new File("pushdown/B.java", "package pushdown; public class B { public int a() { return 1; } }"));
        performPushDown(src.getFileObject("pushdown/B.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pushdown/A.java", "package pushdown; public class A extends B { public int a() { return 1; } } class Nested { }"),
                new File("pushdown/C.java", "package pushdown; public class C extends B { public int a() { return 1; } }"),
                new File("pushdown/B.java", "package pushdown; public class B {}"));
    }
    
    public void testPushDownMethod() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pushdown/A.java", "package pushdown; public class A extends B {}"),
                new File("pushdown/C.java", "package pushdown; public class C extends B {}"),
                new File("pushdown/B.java", "package pushdown; public class B { public int a() { return 1; } }"));
        performPushDown(src.getFileObject("pushdown/B.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pushdown/A.java", "package pushdown; public class A extends B { public int a() { return 1; } }"),
                new File("pushdown/C.java", "package pushdown; public class C extends B { public int a() { return 1; } }"),
                new File("pushdown/B.java", "package pushdown; public class B {}"));
    }

    public void testPushDownInterface() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pushdown/A.java", "package pushdown; public class A extends B { }"),
                new File("pushdown/C.java", "package pushdown; public class C extends B { }"),
                new File("pushdown/B.java", "package pushdown; public class B implements I { public void i() { } }"),
                new File("pushdown/I.java", "package pushdown; public interface I { public void i(); }"));
        performPushDown(src.getFileObject("pushdown/B.java"), -1, Boolean.FALSE);
        verifyContent(src,
                new File("pushdown/A.java", "package pushdown; import pushdown.I; public class A extends B implements I { }"),
                new File("pushdown/C.java", "package pushdown; import pushdown.I; public class C extends B implements I { }"),
                new File("pushdown/B.java", "package pushdown; public class B { public void i() { } }"),
                new File("pushdown/I.java", "package pushdown; public interface I { public void i(); }"));
    }
    
    private void performPushDown(FileObject source, final int position, final Boolean makeAbstract, Problem... expectedProblems) throws IOException, IllegalArgumentException, InterruptedException {
        final PushDownRefactoring[] r = new PushDownRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();
                
                final ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                final TreePath classPath = info.getTrees().getPath(cut, classTree);
                TypeElement classEl = (TypeElement) info.getTrees().getElement(classPath);
                
                TypeMirror superclass = classEl.getSuperclass();
                TypeElement superEl = (TypeElement) info.getTypes().asElement(superclass);
                
                MemberInfo[] members = new MemberInfo[1];
                Tree member;
                if (position >= 0) {
                    member = classTree.getMembers().get(position);
                } else {
                    member = classTree.getImplementsClause().get(Math.abs(position)-1);
                }
                Element el = info.getTrees().getElement(new TreePath(classPath, member));
                if (position <0) {
                    members[0] = MemberInfo.create(el, info, MemberInfo.Group.IMPLEMENTS);
                } else {
                    members[0] = MemberInfo.create(el, info);
                }
                members[0].setMakeAbstract(makeAbstract);

                r[0] = new PushDownRefactoring(TreePathHandle.create(classEl, info));
                r[0].setMembers(members);
            }
        }, true);
        
        RefactoringSession rs = RefactoringSession.create("Push down");
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
    
}
