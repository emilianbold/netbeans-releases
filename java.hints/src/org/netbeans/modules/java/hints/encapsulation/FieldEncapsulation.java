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

package org.netbeans.modules.java.hints.encapsulation;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerTreeKind;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.java.hints.jackpot.spi.support.OneCheckboxCustomizerProvider;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
public class FieldEncapsulation {

    private static final Logger LOG = Logger.getLogger(FieldEncapsulation.class.getName());
    private static final String ACTION_PATH = "Actions/Refactoring/org-netbeans-modules-refactoring-java-api-ui-EncapsulateFieldAction.instance";   //NOI18N
    private static final String KW_THIS = "this";

    static final String ALLOW_ENUMS_KEY = "allow.enums";
    static final boolean ALLOW_ENUMS_DEFAULT = false;

    @Hint(category="encapsulation", suppressWarnings={"ProtectedField"}, enabled=false, customizerProvider=CustomizerImpl.class) //NOI18N
    @TriggerTreeKind(Kind.VARIABLE)
    public static ErrorDescription protectedField(final HintContext ctx) {
        return create(ctx,
            Modifier.PROTECTED,
            NbBundle.getMessage(FieldEncapsulation.class, "TXT_ProtectedField"),
            "ProtectedField");  //NOI18N
    }

    @Hint(category="encapsulation", suppressWarnings={"PublicField"}, enabled=false, customizerProvider=CustomizerImpl.class) //NOI18N
    @TriggerTreeKind(Kind.VARIABLE)
    public static ErrorDescription publicField(final HintContext ctx) {
        return create(ctx,
            Modifier.PUBLIC,
            NbBundle.getMessage(FieldEncapsulation.class, "TXT_PublicField"),
            "PublicField"); //NOI18N
    }

    @Hint(category="encapsulation", suppressWarnings={"PackageVisibleField"}, enabled=false, customizerProvider=CustomizerImpl.class) //NOI18N
    @TriggerTreeKind(Kind.VARIABLE)
    public static ErrorDescription packageField(final HintContext ctx) {
        return create(ctx,
            null,
            NbBundle.getMessage(FieldEncapsulation.class, "TXT_PackageField"),
            "PackageVisibleField"); //NOI18N
    }

    @Hint(category="encapsulation", suppressWarnings={"AccessingNonPublicFieldOfAnotherObject"}, enabled=false) //NOI18N
    @TriggerTreeKind(Kind.MEMBER_SELECT)
    public static ErrorDescription privateField(final HintContext ctx) {
        assert ctx != null;
        final TreePath tp = ctx.getPath();
        final Element selectElement = ctx.getInfo().getTrees().getElement(tp);
        if (selectElement == null ||
            selectElement.getKind()!= ElementKind.FIELD ||
            !((VariableElement)selectElement).getModifiers().contains(Modifier.PRIVATE)||
            ((VariableElement)selectElement).getModifiers().contains(Modifier.STATIC)) {
            return null;
        }
        final ExpressionTree subSelect = ((MemberSelectTree)tp.getLeaf()).getExpression();
        if ((subSelect.getKind() == Tree.Kind.IDENTIFIER && KW_THIS.contentEquals(((IdentifierTree)subSelect).getName())) ||
            (subSelect.getKind() == Tree.Kind.MEMBER_SELECT && KW_THIS.contentEquals(((MemberSelectTree)subSelect).getIdentifier()))){
            return null;
        }
        final TypeElement selectOwner = getEnclosingClass(tp, ctx.getInfo().getTrees());
        if (selectOwner == null ||
            SourceUtils.getOutermostEnclosingTypeElement(selectElement) != SourceUtils.getOutermostEnclosingTypeElement(selectOwner)) {
            return null;
        }
        SourceUtils.getOutermostEnclosingTypeElement(selectElement);
        return ErrorDescriptionFactory.forName(ctx, tp,
                NbBundle.getMessage(FieldEncapsulation.class, "TXT_OtherPrivateField"),
                FixFactory.createSuppressWarningsFix(ctx.getInfo(), tp, "AccessingNonPublicFieldOfAnotherObject")); //NOI18N
    }

    private static TypeElement getEnclosingClass (TreePath path, final Trees trees) {
        while (path != null && path.getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT) {
            if (path.getLeaf().getKind() == Tree.Kind.CLASS) {
                return (TypeElement) trees.getElement(path);
            }
            path = path.getParentPath();
        }
        return null;
    }

    private static ErrorDescription create (final HintContext ctx,
                                            final Modifier visibility,
                                            final String message,
                                            final String suppressWarnings) {
        assert ctx != null;
        assert message != null;
        assert suppressWarnings != null;
        final TreePath tp = ctx.getPath();
        final Tree parent = tp.getParentPath().getLeaf();
        if (parent.getKind() != Tree.Kind.CLASS ||
            ctx.getInfo().getTreeUtilities().isInterface((ClassTree)parent)) {
            return null;
        }
        final VariableTree vt = (VariableTree) tp.getLeaf();
        final ModifiersTree mt = vt.getModifiers();
        if (mt.getFlags().contains(Modifier.FINAL) || !hasRequiredVisibility(mt.getFlags(),visibility)) {
            return null;
        }
        if (ctx.getPreferences().getBoolean(ALLOW_ENUMS_KEY, ALLOW_ENUMS_DEFAULT)) {
            Element type = ctx.getInfo().getTrees().getElement(new TreePath(tp, vt.getType()));
            if (type != null && type.getKind() == ElementKind.ENUM) {
                return null;
            }
        }
        final Collection<? extends TreePath> fieldGroup = Utilities.resolveFieldGroup(ctx.getInfo(), tp);
        if (fieldGroup.size() != 1 && fieldGroup.iterator().next().getLeaf() != tp.getLeaf()) {
            return null;
        }
        return ErrorDescriptionFactory.forName(ctx, tp, message,
                new FixImpl(TreePathHandle.create(tp, ctx.getInfo())),
                FixFactory.createSuppressWarningsFix(ctx.getInfo(), tp, suppressWarnings)); //NOI18N
    }

    private static boolean hasRequiredVisibility(final Set<Modifier> mods, final Modifier reqMod) {
        return reqMod != null ?
            mods.contains(reqMod):
            mods.isEmpty() ?
                true:
                !EnumSet.copyOf(mods).removeAll(EnumSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC));
    }

    private static class FixImpl implements Fix {

        private final TreePathHandle handle;

        public FixImpl(final TreePathHandle handle) {
            this.handle = handle;
        }


        @Override
        public String getText() {
            return NbBundle.getMessage(FieldEncapsulation.class, "FIX_EncapsulateField");
        }

        @Override
        public ChangeInfo implement() throws Exception {
            final FileObject file = handle.getFileObject();
            final JTextComponent comp = EditorRegistry.lastFocusedComponent();
            if (file != null && file == getFileObject(comp)) {
                final int[] pos = new int[]{-1};
                JavaSource.forFileObject(file).runUserActionTask(new Task<CompilationController>(){
                    @Override
                    public void run(CompilationController info) throws Exception {
                        info.toPhase(JavaSource.Phase.PARSED);
                        final TreePath tp = handle.resolve(info);
                        if (tp != null && tp.getLeaf().getKind() == Tree.Kind.VARIABLE) {
                            pos[0] = (int) info.getTrees().getSourcePositions().getEndPosition(
                                    tp.getCompilationUnit(),
                                    ((VariableTree)tp.getLeaf()).getType()) + 1;
                        }
                    }
                }, true);
                invokeRefactoring (comp, pos[0]);
            }
            return null;
        }

        public static FileObject getFileObject(JTextComponent comp) {
            if (comp == null) {
                return null;
            }
            final Document doc = comp.getDocument();
            if (doc == null) {
                return null;
            }
            final Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
            if (sdp instanceof FileObject) {
                return (FileObject)sdp;
            }
            if (sdp instanceof DataObject) {
                return ((DataObject)sdp).getPrimaryFile();
            }
            return null;
        }

        /**
         * todo:
         * Currently there is no API to invoke encapsulate field action.
         */
        private void invokeRefactoring(final JTextComponent component, final int position) {
            final FileObject cfgRoot = FileUtil.getConfigRoot();
            final FileObject actionFile = cfgRoot.getFileObject(ACTION_PATH);
            if (actionFile == null) {
                LOG.warning("Encapsulate Field action not found at: " + ACTION_PATH); //NOI18N
                return;
            }
            try {
                final DataObject dobj  = DataObject.find(actionFile);
                final InstanceCookie ic = dobj.getLookup().lookup(InstanceCookie.class);
                final Object instance = ic.instanceCreate();
                if (!(instance instanceof Action)) {
                    throw new IOException();
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (position != -1) {
                            component.setCaretPosition(position);
                        }
                        ((Action)instance).actionPerformed(new ActionEvent(component, 0, null));
                    }
                });
            } catch (IOException ioe) {
                LOG.warning("Encapsulate Field action is broken: " + ACTION_PATH); //NOI18N
                return;
            } catch (ClassNotFoundException cnf) {
                LOG.warning("Encapsulate Field action is broken: " + ACTION_PATH); //NOI18N
                return;
            }
        }
    }
    
    public static final class CustomizerImpl extends OneCheckboxCustomizerProvider {
        public CustomizerImpl() {
            super(NbBundle.getMessage(FieldEncapsulation.class, "DN_IgnoreEnumForField"),
                  NbBundle.getMessage(FieldEncapsulation.class, "TP_IgnoreEnumForField"),
                  ALLOW_ENUMS_KEY,
                  ALLOW_ENUMS_DEFAULT);
        }
    }
}
