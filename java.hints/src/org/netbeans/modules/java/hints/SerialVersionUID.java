/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import static javax.lang.model.element.Modifier.*;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 * @author Michal Hlavac
 * @author Samuel Halliday
 *
 * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=70746">RFE 70746</a>
 * @see <a href="http://kenai.com/projects/nb-svuid-generator/sources/mercurial/show/src/eu/easyedu/netbeans/svuid">Original Implementation Source Code</a>
 */
public class SerialVersionUID implements ErrorRule<Void> {
    public static final Set<String> CODES = Collections.singleton("compiler.warn.missing.SVUID");

    private static final String SERIAL = "serial"; //NOI18N
    private static final String SVUID = "serialVersionUID"; //NOI18N
    private final AtomicBoolean cancel = new AtomicBoolean();

    public SerialVersionUID() {
    }

    public Set<Kind> getTreeKinds() {
        return TreeUtilities.CLASS_TREE_KINDS;
    }

    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        if (treePath == null || !TreeUtilities.CLASS_TREE_KINDS.contains(treePath.getLeaf().getKind())) {
            return null;
        }

        TypeElement type = (TypeElement) info.getTrees().getElement(treePath);
        List<Fix> fixes = new ArrayList<Fix>();

        fixes.add(new FixImpl(TreePathHandle.create(treePath, info), false));
        // fixes.add(new FixImpl(TreePathHandle.create(treePath, info), true));

        if (!type.getNestingKind().equals(NestingKind.ANONYMOUS)) {
            // add SuppressWarning only to non-anonymous class
            fixes.addAll(FixFactory.createSuppressWarnings(info, treePath, SERIAL));
        }

        return fixes;
    }

    public String getId() {
        return getClass().getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "DN_SerialVersionUID");//NOI18N
    }

    public void cancel() {
        cancel.set(true);
    }

    private static class FixImpl implements Fix, Task<WorkingCopy> {
        private static final int ENUM_FLAG = 1 << 14;

        private final TreePathHandle handle;
        private final boolean generated;

        /**
         * @param handle to the CLASS
         * @param generated true will insert a generated value, false will use a default
         */
        public FixImpl(TreePathHandle handle, boolean generated) {
            this.handle = handle;
            this.generated = generated;
            if (generated) {
                throw new UnsupportedOperationException("TODO: implement");
            }
        }

        public String getText() {
            if (generated) {
                return NbBundle.getMessage(getClass(), "HINT_SerialVersionUID_Generated");//NOI18N
            }
            return NbBundle.getMessage(getClass(), "HINT_SerialVersionUID");//NOI18N
        }

        public ChangeInfo implement() throws Exception {
            JavaSource js = JavaSource.forFileObject(handle.getFileObject());
            js.runModificationTask(this).commit();
            return null;
        }

        public void run(WorkingCopy copy) throws Exception {
            if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                return;
            }
            TreePath treePath = handle.resolve(copy);
            if (treePath == null || !TreeUtilities.CLASS_TREE_KINDS.contains(treePath.getLeaf().getKind())) {
                return;
            }
            ClassTree classTree = (ClassTree) treePath.getLeaf();
            TreeMaker make = copy.getTreeMaker();

            // documentation recommends private
            Set<Modifier> modifiers = EnumSet.of(PRIVATE, STATIC, FINAL);
            VariableTree svuid = make.Variable(make.Modifiers(modifiers), SVUID, make.Identifier("long"), make.Literal(1L)); //NO18N

            ClassTree decl = GeneratorUtilities.get(copy).insertClassMember(classTree, svuid);
            copy.rewrite(classTree, decl);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FixImpl other = (FixImpl) obj;
            if (this.handle != other.handle && (this.handle == null || !this.handle.equals(other.handle))) {
                return false;
            }
            if (this.generated != other.generated) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + (this.handle != null ? this.handle.hashCode() : 0);
            hash = 41 * hash + (this.generated ? 1 : 0);
            return hash;
        }
    }

}
