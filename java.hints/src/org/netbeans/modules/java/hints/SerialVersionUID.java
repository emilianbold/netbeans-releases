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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import static javax.lang.model.element.Modifier.*;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 * @author Michal Hlavac
 * @author Samuel Halliday
 *
 * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=70746">RFE 70746</a>
 * @see <a href="http://kenai.com/projects/nb-svuid-generator/sources/mercurial/show/src/eu/easyedu/netbeans/svuid">Original Implementation Source Code</a>
 */
public class SerialVersionUID extends AbstractHint {

    private static final String SERIAL = "serial"; //NOI18N
    private static final String SVUID = "serialVersionUID"; //NOI18N
    private static final String SERIALIZABLE = "java.io.Serializable"; //NOI18N
    private final AtomicBoolean cancel = new AtomicBoolean();

    public SerialVersionUID() {
        super(true, false, AbstractHint.HintSeverity.WARNING);
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(getClass(), "DSC_SerialVersionUID"); //NOI18N
    }

    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.CLASS);
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        if (treePath == null || treePath.getLeaf().getKind() != Kind.CLASS) {
            return null;
        }
        cancel.set(false);
        TypeElement type = (TypeElement) info.getTrees().getElement(treePath);
        if (type == null || type.getKind() == ElementKind.INTERFACE || !isSerializable(type) || hasSerialVersionUID(type) || hasSuppressWarning(type, SERIAL)) {
            return null;
        }
        // Contrary to popular belief, abstract classes *should* define serialVersionUID,
        // according to the documentation of Serializable. It refers to "all classes".
        List<Fix> fixes = new ArrayList<Fix>();
        fixes.add(new FixImpl(TreePathHandle.create(treePath, info), false));
        // fixes.add(new FixImpl(TreePathHandle.create(treePath, info), true));
        fixes.addAll(FixFactory.createSuppressWarnings(info, treePath, SERIAL));

        int[] span = info.getTreeUtilities().findNameSpan((ClassTree) treePath.getLeaf());
        if (span == null) { //span cannot be found, do not show anything
            return null;
        }
        String desc = NbBundle.getMessage(getClass(), "ERR_SerialVersionUID"); //NOI18N
        ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), desc, fixes, info.getFileObject(), span[0], span[1]);
        if (cancel.get()) {
            return null;
        }
        return Collections.singletonList(ed);
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
            if (treePath == null || treePath.getLeaf().getKind() != Kind.CLASS) {
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

    private boolean isSerializable(TypeElement type) {
        for (TypeElement t : GeneratorUtils.getAllParents(type)) {
            if (t.getKind() == ElementKind.INTERFACE && t.getQualifiedName().contentEquals(SERIALIZABLE)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSerialVersionUID(TypeElement type) {
        for (VariableElement e : ElementFilter.fieldsIn(type.getEnclosedElements())) {
            if (e.getSimpleName().contentEquals(SVUID)) {
                Set<Modifier> modifiers = e.getModifiers();
                // documentation says ANY-ACCESS-MODIFIER static final long serialVersionUID
                if (modifiers.containsAll(EnumSet.of(STATIC, FINAL))) {
                    TypeMirror t = e.asType();
                    if (t.getKind() != null && t.getKind() == TypeKind.LONG) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    private static boolean hasSuppressWarning(TypeElement type, String warning) {
        SuppressWarnings annotation = type.getAnnotation(SuppressWarnings.class);
        if (annotation != null) {
            for (String val : annotation.value()) {
                if (val.equals(warning)) {
                    return true;
                }
            }
        }
        return false;
    }
}
