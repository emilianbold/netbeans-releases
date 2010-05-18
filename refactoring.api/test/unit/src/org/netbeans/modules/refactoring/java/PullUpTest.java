/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.refactoring.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PullUpRefactoring;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jiri Prox
 */
public class PullUpTest extends RefactoringTestCase {

    public PullUpTest(String name) {
        super(name);
    }

    public void testPullUpField() throws Exception {
        FileObject srcFO = getFileInProject("default", "src/pulluppkg/PullUpField.java");
        JavaSource jsSrc = JavaSource.forFileObject(srcFO);
        FileObject dest = getFileInProject("default", "src/pulluppkg/PullUpFieldSuper.java");
        JavaSource jsDest = JavaSource.forFileObject(dest);
        final TreePathResolver fieldSelector = new TreePathResolver(new MemberSelector(ElementKind.FIELD, 1));
        final TreePathResolver srcClassSelector = new TreePathResolver(new TopClassSelector(0));
        final TreePathResolver destClassSelector = new TreePathResolver(new TopClassSelector(0));
        jsSrc.runUserActionTask(fieldSelector, true);
        jsSrc.runUserActionTask(srcClassSelector, true);
        jsDest.runUserActionTask(destClassSelector, true);
        final PullUpRefactoring pullUp = new PullUpRefactoring(srcClassSelector.tph);
        perform(pullUp, new ParameterSetter() {

                    public void setParameters() {
                        MemberInfo[] mi = new MemberInfo[]{MemberInfo.create(fieldSelector.tph.resolveElement(fieldSelector.info), fieldSelector.info)};
                        pullUp.setMembers(mi);
                        ElementHandle el = ElementHandle.create(destClassSelector.tph.resolveElement(destClassSelector.info));
                        pullUp.setTargetType(el);
                    }
                });
    }

    public void testPullUpMethod() throws Exception {
        FileObject test = getFileInProject("default", "src/pulluppkg/PullUpBaseClass.java");
        JavaSource js = JavaSource.forFileObject(test);
        FileObject dest = getFileInProject("default", "src/pulluppkg/PullUpSuperClass.java");
        JavaSource jsDest = JavaSource.forFileObject(dest);
        final TreePathResolver pulledElementSelector = new TreePathResolver(new MemberSelector(ElementKind.METHOD, 1));
        final TreePathResolver srcClassSelector = new TreePathResolver(new TopClassSelector(0));
        final TreePathResolver destClassSelector = new TreePathResolver(new TopClassSelector(0));
        performPullUp(test, srcClassSelector, jsDest, destClassSelector, new boolean[]{false}, pulledElementSelector);
    }

    public void testPullUpClass() throws Exception {
        FileObject test = getFileInProject("default", "src/pulluppkg/PullUpInnerClass.java");
        JavaSource js = JavaSource.forFileObject(test);
        FileObject dest = getFileInProject("default", "src/pulluppkg/PullUpInnerClassSuper.java");
        JavaSource jsDest = JavaSource.forFileObject(dest);
        final TreePathResolver pulledElementSelector = new TreePathResolver(new MemberSelector(ElementKind.CLASS, 1));
        final TreePathResolver srcClassSelector = new TreePathResolver(new TopClassSelector(0));
        final TreePathResolver destClassSelector = new TreePathResolver(new TopClassSelector(0));
        performPullUp(test, srcClassSelector, jsDest, destClassSelector, new boolean[]{false}, pulledElementSelector);
    }

    public void testPullUp2Iface() throws Exception {
        FileObject test = getFileInProject("default", "src/pulluppkg/PullUp2Iface.java");
        JavaSource js = JavaSource.forFileObject(test);
        FileObject dest = getFileInProject("default", "src/pulluppkg/PullUp2IfaceSuper.java");
        JavaSource jsDest = JavaSource.forFileObject(dest);
        final TreePathResolver pulledElementSelector = new TreePathResolver(new MemberSelector(ElementKind.METHOD, 1));
        final TreePathResolver srcClassSelector = new TreePathResolver(new TopClassSelector(0));
        final TreePathResolver destClassSelector = new TreePathResolver(new TopClassSelector(0));
        performPullUp(test, srcClassSelector, jsDest, destClassSelector, new boolean[]{true}, pulledElementSelector);
    }

    public void testPullUpMakeAbs() throws Exception {
        FileObject test = getFileInProject("default", "src/pulluppkg/PullUpAbs.java");
        JavaSource js = JavaSource.forFileObject(test);
        FileObject dest = getFileInProject("default", "src/pulluppkg/PullUpAbsSuper.java");
        JavaSource jsDest = JavaSource.forFileObject(dest);
        final TreePathResolver pulledElementSelector = new TreePathResolver(new MemberSelector(ElementKind.METHOD, 1));
        final TreePathResolver srcClassSelector = new TreePathResolver(new TopClassSelector(0));
        final TreePathResolver destClassSelector = new TreePathResolver(new TopClassSelector(0));
        performPullUp(test, srcClassSelector, jsDest, destClassSelector, new boolean[]{true}, pulledElementSelector);
    }

    public void testPullUpAbsMethod() throws Exception {
        FileObject test = getFileInProject("default", "src/pulluppkg/PullUpAbsMethod.java");
        JavaSource js = JavaSource.forFileObject(test);
        FileObject dest = getFileInProject("default", "src/pulluppkg/PullUpAbsMethodSuper.java");
        JavaSource jsDest = JavaSource.forFileObject(dest);
        final TreePathResolver pulledElementSelector = new TreePathResolver(new MemberSelector(ElementKind.METHOD, 1));
        final TreePathResolver srcClassSelector = new TreePathResolver(new TopClassSelector(0));
        final TreePathResolver destClassSelector = new TreePathResolver(new TopClassSelector(0));
        performPullUp(test, srcClassSelector, jsDest, destClassSelector, new boolean[]{false}, pulledElementSelector);
    }

    public void testPullUpAbsMethod2Iface() throws Exception {
        FileObject test = getFileInProject("default", "src/pulluppkg/PullUpAbsMethod2Iface.java");
        JavaSource js = JavaSource.forFileObject(test);
        FileObject dest = getFileInProject("default", "src/pulluppkg/PullUpAbsMethod2IfaceSuper.java");
        JavaSource jsDest = JavaSource.forFileObject(dest);
        final TreePathResolver pulledElementSelector = new TreePathResolver(new MemberSelector(ElementKind.METHOD, 1));
        final TreePathResolver srcClassSelector = new TreePathResolver(new TopClassSelector(0));
        final TreePathResolver destClassSelector = new TreePathResolver(new TopClassSelector(0));
        performPullUp(test, srcClassSelector, jsDest, destClassSelector, new boolean[]{false}, pulledElementSelector);
    }

    //    public void testPullUpInterface() throws Exception {
//        FileObject test = getFileInProject("default", "src/pulluppkg/PullUpIface.java");
//        JavaSource js = JavaSource.forFileObject(test);
//        FileObject dest = getFileInProject("default", "src/pulluppkg/PullUpIfaceSuper.java");
//        JavaSource jsDest = JavaSource.forFileObject(dest);
//        final TreePathResolver pulledElementSelector = new TreePathResolver(new ImplementsSelector(0, 0));
//        final TreePathResolver srcClassSelector = new TreePathResolver(new TopClassSelector(0));
//        final TreePathResolver destClassSelector = new TreePathResolver(new TopClassSelector(0));
//        performPullUp(test, srcClassSelector, jsDest, destClassSelector, new boolean[]{false}, MemberInfo.Group.IMPLEMENTS,pulledElementSelector);
//    }

    public void testPullUpTwoClassesUp() throws Exception {
        FileObject test = getFileInProject("default", "src/pulluppkg/PullUpTwoClasses.java");
        JavaSource js = JavaSource.forFileObject(test);
        FileObject dest = getFileInProject("default", "src/pulluppkg/PullUpTwoClassesSuperSuper.java");
        JavaSource jsDest = JavaSource.forFileObject(dest);
        final TreePathResolver pulledElementSelector = new TreePathResolver(new MemberSelector(ElementKind.METHOD, 1));
        final TreePathResolver pulledElementSelector2 = new TreePathResolver(new MemberSelector(ElementKind.FIELD, 1));
        final TreePathResolver srcClassSelector = new TreePathResolver(new TopClassSelector(0));
        final TreePathResolver destClassSelector = new TreePathResolver(new TopClassSelector(0));
        performPullUp(test, srcClassSelector, jsDest, destClassSelector, new boolean[]{false, false}, pulledElementSelector, pulledElementSelector2);
    }

    public void testPullUpExisting() throws Exception {
        FileObject test = getFileInProject("default", "src/pulluppkg/PullUpExisting.java");
        JavaSource js = JavaSource.forFileObject(test);
        FileObject dest = getFileInProject("default", "src/pulluppkg/PullUpExistingSuper.java");
        JavaSource jsDest = JavaSource.forFileObject(dest);
        final TreePathResolver pulledElementSelector = new TreePathResolver(new MemberSelector(ElementKind.METHOD, 1));
        final TreePathResolver srcClassSelector = new TreePathResolver(new TopClassSelector(0));
        final TreePathResolver destClassSelector = new TreePathResolver(new TopClassSelector(0));
        performPullUp(test, srcClassSelector, jsDest, destClassSelector, new boolean[]{false}, pulledElementSelector);
    }

    public void testPullUpLocalyReferenced() throws Exception {
        FileObject test = getFileInProject("default", "src/pulluppkg/PullUpReferenced.java");
        JavaSource js = JavaSource.forFileObject(test);
        FileObject dest = getFileInProject("default", "src/pulluppkg/PullUpReferencedSuper.java");
        JavaSource jsDest = JavaSource.forFileObject(dest);
        final TreePathResolver pulledElementSelector = new TreePathResolver(new MemberSelector(ElementKind.METHOD, 1));
        final TreePathResolver srcClassSelector = new TreePathResolver(new TopClassSelector(0));
        final TreePathResolver destClassSelector = new TreePathResolver(new TopClassSelector(0));
        performPullUp(test, srcClassSelector, jsDest, destClassSelector, new boolean[]{false}, pulledElementSelector);
    }

    private void performPullUp(FileObject sourceFO, final TreePathResolver srcClassSelector, JavaSource dest, final TreePathResolver destClassSelector, final boolean[] makeAbstract, final TreePathResolver... pulledElementSelectors) throws IOException, IllegalArgumentException {
        JavaSource source = JavaSource.forFileObject(sourceFO);
        for (int i = 0; i < pulledElementSelectors.length; i++) {
            source.runUserActionTask(pulledElementSelectors[i], true);
        }
        source.runUserActionTask(srcClassSelector, true);
        dest.runUserActionTask(destClassSelector, true);
        final PullUpRefactoring pullUp = new PullUpRefactoring(srcClassSelector.tph);
        perform(pullUp, new ParameterSetter() {

                    @SuppressWarnings("unchecked")            
            public void setParameters() {
                        MemberInfo[] mi = new MemberInfo[pulledElementSelectors.length];

                        for (int i = 0; i < pulledElementSelectors.length; i++) {
                            mi[i] = MemberInfo.create(pulledElementSelectors[i].tph.resolveElement(pulledElementSelectors[i].info),
                                    pulledElementSelectors[i].info);
                            mi[i].setMakeAbstract(makeAbstract[i]);

                        }
                        pullUp.setMembers(mi);
                        ElementHandle el = ElementHandle.create(destClassSelector.tph.resolveElement(destClassSelector.info));
                        pullUp.setTargetType(el);
                    }
                });
    }

    class MemberSelector implements TreePathResolver.TreePathHandleSelector {

        private ElementKind kind;
        private int number;

        public MemberSelector(ElementKind kind, int number) {
            this.kind = kind;
            this.number = number;
        }

        public MemberSelector(int number) {
            this(null, number);
            this.number = number;
        }

        public TreePathHandle select(CompilationController compilationController) {
            TreePath cuPath = new TreePath(compilationController.getCompilationUnit());
            List<? extends Tree> typeDecls = compilationController.getCompilationUnit().getTypeDecls();
            Tree t = typeDecls.get(0);
            TreePath p = new TreePath(cuPath, t);
            Element e = compilationController.getTrees().getElement(p);
            List<? extends Element> elems = e.getEnclosedElements();
            for (Element element : elems) {
                int i = 0;
                if (element.getKind() == kind || kind == null) {
                    i++;
                    if (i == number) {
                        return TreePathHandle.create(element, compilationController);
                    }
                }
            }
            return null;
        }
    }
    
    class ImplementsSelector implements TreePathResolver.TreePathHandleSelector {

        private int classNum;
        private int ifaceNum;

        public ImplementsSelector(int classNum, int ifaceNum) {
            this.classNum = classNum;
            this.ifaceNum = ifaceNum;
        }

        public TreePathHandle select(CompilationController compilationController) {
            TreePath cuPath = new TreePath(compilationController.getCompilationUnit());
            List<? extends Tree> typeDecls = compilationController.getCompilationUnit().getTypeDecls();
            ClassTree cTree = (ClassTree) typeDecls.get(classNum);
            Tree interfaceTree = cTree.getImplementsClause().get(ifaceNum);
            TreePathHandle pathHandle = TreePathHandle.create(TreePath.getPath(cuPath, interfaceTree), compilationController);
            return pathHandle;
        }
    }
}

