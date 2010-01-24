/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Detects usage of this in annonymous class
 * @author Max Sauer
 */
public class ThisInAnonymous extends AbstractHint {
    private static final String THIS_KEYWORD = "this"; // NOI18N
    private static final String DOT_THIS = ".this"; // NOI18N

    public ThisInAnonymous() {
        super(true, true, HintSeverity.WARNING);
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(ThisInAnonymous.class, "DESC_TestInAnonymous"); // NOI18N
    }

    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.IDENTIFIER);
    }

    public List<ErrorDescription> run(CompilationInfo compilationInfo,
            TreePath treePath) {
        Element e = compilationInfo.getTrees().getElement(treePath);
        if (e != null && e.getSimpleName().contentEquals(THIS_KEYWORD)) {
            TreePath anonClassTP = getParentClass(treePath);
            Element annonClass = compilationInfo.getTrees().getElement(anonClassTP);
            if (isAnonymousClass(annonClass)) {
                CompilationUnitTree compilationUnit = compilationInfo.getCompilationUnit();
                Tree leaf = treePath.getLeaf();

                List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(
                        TreePathHandle.create(treePath, compilationInfo),
                        ElementHandle.create(compilationInfo.getTrees().getElement(getParentClass(anonClassTP.getParentPath()))),
                        compilationInfo.getFileObject()));
                
                return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(
                        getSeverity().toEditorSeverity(),
                        getDisplayName(),
                        fixes,
                        compilationInfo.getFileObject(),
                        (int) compilationInfo.getTrees().getSourcePositions().getStartPosition(compilationUnit, leaf),
                        (int) compilationInfo.getTrees().getSourcePositions().getStartPosition(compilationUnit, leaf)));
            }
        }

        return null;
    }

    public String getId() {
        return getClass().getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ThisInAnonymous.class, "DN_TestInAnonymous"); // NOI18N
    }

    public void cancel() {
        //TODO: add canacel boolean and use in run
    }

    private static TreePath getParentClass(TreePath tp) {
        while (tp.getLeaf().getKind() != Kind.CLASS) {
            tp = tp.getParentPath();
        }
        return tp;
    }

    private static boolean isAnonymousClass(Element e) {
        ElementKind enclosingKind = e.getKind();
        Set<ElementKind> fm = EnumSet.of(ElementKind.METHOD, ElementKind.FIELD);
        if (enclosingKind == ElementKind.CLASS && (e.getSimpleName().length() == 0 || fm.contains(e.getEnclosingElement().getKind()))) {
            return true;
        }
        return false;
    }

    private static final class FixImpl implements Fix, Task<WorkingCopy> {

        private final TreePathHandle thisHandle;
        private final ElementHandle parentClassElementHandle;
        private final FileObject file;

        public FixImpl(TreePathHandle thisHandle, ElementHandle parentClassElementHandle, FileObject file) {
            this.thisHandle = thisHandle;
            this.parentClassElementHandle = parentClassElementHandle;
            this.file = file;
        }

        public String getText() {
            return NbBundle.getMessage(ThisInAnonymous.class, "FIX_TestInAnonymous"); // NOI18N
        }

        public ChangeInfo implement() throws Exception {
            ModificationResult result = JavaSource.forFileObject(file).runModificationTask(this);
            result.commit();
            return null;
        }

        public void run(WorkingCopy wc) throws Exception {
            wc.toPhase(Phase.RESOLVED);
            TreePath tp = thisHandle.resolve(wc);
            Element parentClass = parentClassElementHandle.resolve(wc);

            assert tp != null;
            assert parentClass != null;

            IdentifierTree newT = wc.getTreeMaker().Identifier(parentClass.getSimpleName().toString() + DOT_THIS);
            wc.rewrite(tp.getLeaf(), newT);
        }

    }

}
