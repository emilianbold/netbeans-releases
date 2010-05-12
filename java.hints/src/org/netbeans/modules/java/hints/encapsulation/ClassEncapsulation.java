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
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
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
public class ClassEncapsulation {

    private static final String ACTION_PATH = "Actions/Refactoring/org-netbeans-modules-refactoring-java-api-ui-InnerToOuterAction.instance";   //NOI18N
    private static final Logger LOG = Logger.getLogger(ClassEncapsulation.class.getName());

    static final String ALLOW_ENUMS_KEY = "allow.enums";
    static final boolean ALLOW_ENUMS_DEFAULT = false;

    @Hint(category="encapsulation",suppressWarnings={"PublicInnerClass"}, enabled=false, customizerProvider=CustomizerImpl.class)   //NOI18N
    @TriggerTreeKind(Kind.CLASS)
    public static ErrorDescription publicCls(final HintContext ctx) {
        assert ctx != null;
        return create(ctx, Modifier.PUBLIC,
            NbBundle.getMessage(ClassEncapsulation.class, "TXT_PublicInnerClass"), "PublicInnerClass");  //NOI18N
    }

    @Hint(category="encapsulation",suppressWarnings={"ProtectedInnerClass"}, enabled=false, customizerProvider=CustomizerImpl.class)    //NOI18N
    @TriggerTreeKind(Kind.CLASS)
    public static ErrorDescription protectedCls(final HintContext ctx) {
        assert ctx != null;
        return create(ctx, Modifier.PROTECTED,
            NbBundle.getMessage(ClassEncapsulation.class, "TXT_ProtectedInnerClass"), "ProtectedInnerClass"); //NOI18N
    }

    @Hint(category="encapsulation", suppressWarnings={"PackageVisibleInnerClass"}, enabled=false, customizerProvider=CustomizerImpl.class)
    @TriggerTreeKind(Kind.CLASS)
    public static ErrorDescription packageCls(final HintContext ctx) {
        assert ctx != null;
        return create(ctx, null,
            NbBundle.getMessage(ClassEncapsulation.class, "TXT_PackageInnerClass"), "PackageVisibleInnerClass");    //NOI18N
    }

    private static ErrorDescription create(final HintContext ctx, final Modifier visibility,
        final String description, final String suppressWarnings) {
        assert ctx != null;
        assert description != null;
        assert suppressWarnings != null;
        final TreePath tp = ctx.getPath();
        final Tree owner = tp.getParentPath().getLeaf();
        if (owner.getKind() != Kind.CLASS) {
            return null;
        }
        if (!hasRequiredVisibility(((ClassTree)tp.getLeaf()).getModifiers().getFlags(),visibility)) {
            return null;
        }
        if (ctx.getPreferences().getBoolean(ALLOW_ENUMS_KEY, ALLOW_ENUMS_DEFAULT)) {
            if (ctx.getInfo().getTreeUtilities().isEnum((ClassTree) tp.getLeaf())) {
                return null;
            }
        }
        return ErrorDescriptionFactory.forName(ctx, tp, description,
            new FixImpl(TreePathHandle.create(tp, ctx.getInfo())),
            FixFactory.createSuppressWarningsFix(ctx.getInfo(), tp, suppressWarnings));
    }

    private static boolean hasRequiredVisibility(final Set<Modifier> modifiers, final Modifier reqModifier) {
        return reqModifier != null ?
            modifiers.contains(reqModifier):
            modifiers.isEmpty() ? true:
                !EnumSet.copyOf(modifiers).removeAll(EnumSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC));
    }

    private static class FixImpl implements Fix {

        private final TreePathHandle handle;

        private FixImpl(final TreePathHandle handle) {
            assert handle != null;
            this.handle = handle;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(ClassEncapsulation.class,"FIX_MoveInnerToOuter");
        }

        @Override
        public ChangeInfo implement() throws Exception {
            final FileObject file = handle.getFileObject();
            final JTextComponent component = EditorRegistry.lastFocusedComponent();
            if (file != null && file == getFileObject(component)) {
                final int[] position = new int[] {-1};
                JavaSource.forFileObject(file).runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController controller) throws Exception {
                        controller.toPhase(JavaSource.Phase.PARSED);
                        final TreePath tp = handle.resolve(controller);
                        if (tp != null && tp.getLeaf().getKind() == Tree.Kind.CLASS) {
                            position[0] = (int) controller.getTrees().getSourcePositions().getStartPosition(
                                    tp.getCompilationUnit(),
                                    (ClassTree)tp.getLeaf())+1;
                        }
                    }
                }, true);
                invokeRefactoring(component, position[0]);
            }
            return null;
        }

        private static FileObject getFileObject (final JTextComponent comp) {
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

        private void invokeRefactoring(final JTextComponent component, final int position) {
            assert component != null;
            final FileObject cfgRoot = FileUtil.getConfigRoot();
            final FileObject actionFo = cfgRoot.getFileObject(ACTION_PATH);
            if (actionFo == null) {
                LOG.warning("Move Inner to Outer action not found at: " + ACTION_PATH); //NOI18N
                return;
            }
            try {
                final DataObject actionDobj = DataObject.find(actionFo);
                final InstanceCookie ic = actionDobj.getLookup().lookup(InstanceCookie.class);
                if (ic == null) {
                    throw new IOException();
                }
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
                LOG.warning("Move Inner to Outer action is broken: " + ACTION_PATH); //NOI18N
                return;
            } catch (ClassNotFoundException cnf) {
                LOG.warning("Move Inner to Outer action is broken: " + ACTION_PATH); //NOI18N
                return;
            }
        }

    }

    public static final class CustomizerImpl extends OneCheckboxCustomizerProvider {
        public CustomizerImpl() {
            super(NbBundle.getMessage(ClassEncapsulation.class, "DN_IgnoreEnumForInnerClass"),
                  NbBundle.getMessage(ClassEncapsulation.class, "TP_IgnoreEnumForInnerClass"),
                  ALLOW_ENUMS_KEY,
                  ALLOW_ENUMS_DEFAULT);
        }
    }
}
