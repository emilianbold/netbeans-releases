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
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PullUpRefactoring;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ralph Ruijs
 */
public class PullUpTest extends RefactoringTestBase {

    public PullUpTest(String name) {
        super(name);
   }

    public void testPullUpField() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A extends B { public int i; }"),
                new File("pullup/B.java", "package pullup; public class B { }"));
        performPullUp(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A extends B {}"),
                new File("pullup/B.java", "package pullup; public class B { public int i; }"));
    }

    public void testPullUpMethod() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "\n"
                + "}"));
        performPullUp(src.getFileObject("pullup/PullUpBaseClass.java"), 2, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "\n"
                + "}"));
    }

    public void testPullUpClass() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "    \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "}"));
        performPullUp(src.getFileObject("pullup/PullUpBaseClass.java"), 3, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "    \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "}"));
    }

    public void testPullUp2Iface() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpBaseClass implements PullUpSuperIface {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperIface.java", "package pullup;\n"
                + "\n"
                + "public interface PullUpSuperIface {\n"
                + "}"));
        performPullUpIface(src.getFileObject("pullup/PullUpBaseClass.java"), 2, 0, Boolean.TRUE);
            verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpBaseClass implements PullUpSuperIface {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperIface.java", "package pullup;\n"
                + "\n"
                + "public interface PullUpSuperIface {\n"
                + " void method();\n"
                + "}"));
    }

    public void testPullUpMakeAbs() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "\n"
                + "}"));
        performPullUp(src.getFileObject("pullup/PullUpBaseClass.java"), 2, Boolean.TRUE);
        verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public abstract class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "\n"
                + "    public abstract void method();\n"
                + "}"));
    }

    public void testPullUpAbsMethod() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public abstract class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public abstract void method();\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "\n"
                + "}"));
        performPullUp(src.getFileObject("pullup/PullUpBaseClass.java"), 2, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public abstract class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public abstract class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "\n"
                + "    public abstract void method();\n"
                + "}"));
    }

    public void testPullUpAbsMethod2Iface() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "public abstract class PullUpBaseClass implements PullUpSuperIface {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public abstract void method();\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperIface.java", "package pullup;\n"
                + "\n"
                + "public interface PullUpSuperIface {\n"
                + "}"));
        performPullUpIface(src.getFileObject("pullup/PullUpBaseClass.java"), 2, 0, Boolean.TRUE);
            verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "public abstract class PullUpBaseClass implements PullUpSuperIface {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperIface.java", "package pullup;\n"
                + "\n"
                + "public interface PullUpSuperIface {\n"
                + " void method();\n"
                + "}"));
    }

        public void testPullUpInterface() throws Exception {
            writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A extends B implements Runnable { public void run() { } }"),
                new File("pullup/B.java", "package pullup; public class B { }"));
        performPullUpImplements(src.getFileObject("pullup/A.java"), 0);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A extends B { public void run() { } }"),
                new File("pullup/B.java", "package pullup; import java.lang.Runnable; public class B implements Runnable { }"));
    }

    public void testPullUpTwoClassesUp() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A extends B { public int i; }"),
                new File("pullup/B.java", "package pullup; public class B extends C { }"),
                new File("pullup/C.java", "package pullup; public class C { }"));
        performPullUpSuper(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A extends B {}"),
                new File("pullup/B.java", "package pullup; public class B extends C { }"),
                new File("pullup/C.java", "package pullup; public class C { public int i; }"));
    }

    public void testPullUpExisting() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A extends B { public void foo() { } }"),
                new File("pullup/B.java", "package pullup; public class B { public void foo() { } }"));
        performPullUp(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE, new Problem(true, "ERR_PullUp_MemberAlreadyExists"));
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A extends B { public void foo() { } }"),
                new File("pullup/B.java", "package pullup; public class B { public void foo() { } }"));
    }

    public void testPullUpLocalyReferenced() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A extends B { private void foo() { } private method() { foo() } }"),
                new File("pullup/B.java", "package pullup; public class B { }"));
        performPullUp(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A extends B { private method() { foo() } }"),
                new File("pullup/B.java", "package pullup; public class B { protected void foo() { } }"));
    }
        
    private void performPullUpImplements(FileObject source, final int position, Problem... expectedProblems) throws IOException, IllegalArgumentException {
        final PullUpRefactoring[] r = new PullUpRefactoring[1];
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
                TypeMirror implementedInterface = classEl.getInterfaces().get(position);
                members[0] = MemberInfo.create(RetoucheUtils.typeToElement(implementedInterface, info), info, MemberInfo.Group.IMPLEMENTS);

                r[0] = new PullUpRefactoring(TreePathHandle.create(classEl, info));
                r[0].setTargetType(ElementHandle.create(superEl));
                r[0].setMembers(members);
            }
        }, true);
        
        RefactoringSession rs = RefactoringSession.create("Session");
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
    
    private void performPullUpIface(FileObject source, final int position, final int iface, final Boolean makeAbstract, Problem... expectedProblems) throws IOException, IllegalArgumentException {
        final PullUpRefactoring[] r = new PullUpRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();
                
                final ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                final TreePath classPath = info.getTrees().getPath(cut, classTree);
                TypeElement classEl = (TypeElement) info.getTrees().getElement(classPath);
                
                TypeMirror superclass = classEl.getInterfaces().get(iface);
                TypeElement superEl = (TypeElement) info.getTypes().asElement(superclass);
                
                MemberInfo[] members = new MemberInfo[1];
                Tree member = classTree.getMembers().get(position);
                Element el = info.getTrees().getElement(new TreePath(classPath, member));
                members[0] = MemberInfo.create(el, info);
                members[0].setMakeAbstract(makeAbstract);

                r[0] = new PullUpRefactoring(TreePathHandle.create(classEl, info));
                r[0].setTargetType(ElementHandle.create(superEl));
                r[0].setMembers(members);
            }
        }, true);
        
        RefactoringSession rs = RefactoringSession.create("Session");
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
    
    private void performPullUp(FileObject source, final int position, final Boolean makeAbstract, Problem... expectedProblems) throws IOException, IllegalArgumentException {
        final PullUpRefactoring[] r = new PullUpRefactoring[1];
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
                Tree member = classTree.getMembers().get(position);
                Element el = info.getTrees().getElement(new TreePath(classPath, member));
                members[0] = MemberInfo.create(el, info);
                members[0].setMakeAbstract(makeAbstract);

                r[0] = new PullUpRefactoring(TreePathHandle.create(classEl, info));
                r[0].setTargetType(ElementHandle.create(superEl));
                r[0].setMembers(members);
            }
        }, true);
        
        RefactoringSession rs = RefactoringSession.create("Session");
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
    
    private void performPullUpSuper(FileObject source, final int position, final Boolean makeAbstract, Problem... expectedProblems) throws IOException, IllegalArgumentException {
        final PullUpRefactoring[] r = new PullUpRefactoring[1];
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
                TypeMirror supersuperclass = superEl.getSuperclass();
                TypeElement supersuperEl = (TypeElement) info.getTypes().asElement(supersuperclass);
                
                MemberInfo[] members = new MemberInfo[1];
                Tree member = classTree.getMembers().get(position);
                Element el = info.getTrees().getElement(new TreePath(classPath, member));
                members[0] = MemberInfo.create(el, info);
                members[0].setMakeAbstract(makeAbstract);

                r[0] = new PullUpRefactoring(TreePathHandle.create(classEl, info));
                r[0].setTargetType(ElementHandle.create(supersuperEl));
                r[0].setMembers(members);
            }
        }, true);
        
        RefactoringSession rs = RefactoringSession.create("Session");
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
