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

package org.netbeans.modules.php.symfony.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.TextAction;
import org.netbeans.modules.csl.core.UiUtils;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.symfony.SymfonyPhpFrameworkProvider;
import org.netbeans.modules.php.symfony.util.SymfonyUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @see BaseAction
 * @author Tomas Mysik
 */
public final class GoToActionOrViewAction extends TextAction implements ContextAwareAction {
    private static final long serialVersionUID = -1231423139431663L;
    private static final GoToActionOrViewAction INSTANCE = new GoToActionOrViewAction();

    private GoToActionOrViewAction() {
        super(getFullName());
        // copied from BaseAction
        putValue("noIconInMenu", true); // NOI18N
        putValue(NAME, getFullName());
        putValue("menuText", getPureName()); // NOI18N
    }

    public static GoToActionOrViewAction getInstance() {
        return INSTANCE;
    }

    private static String getFullName() {
        return NbBundle.getMessage(GoToActionOrViewAction.class, "LBL_SymfonyAction", getPureName());
    }

    private static String getPureName() {
        return NbBundle.getMessage(GoToActionOrViewAction.class, "LBL_GoToActionOrView");
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        FileObject fo = FileUtils.getFileObject(actionContext);
        return getGoToAction(fo);
    }

    public void actionPerformed(ActionEvent e) {
        FileObject fo = NbEditorUtilities.getFileObject(getTextComponent(e).getDocument());
        Action action = getGoToAction(fo);
        if (action != null) {
            action.actionPerformed(e);
        }
    }

    private Action getGoToAction(FileObject fo) {
        if (!isValid(fo)) {
            return null;
        }

        if (SymfonyUtils.isViewWithAction(fo)) {
            return new GoToActionAction(fo);
        } else if (SymfonyUtils.isAction(fo)) {
            return new GoToViewAction(fo);
        }
        return null;
    }

    private boolean isValid(FileObject fo) {
        if (fo == null) {
            return false;
        }
        PhpModule phpModule = PhpModule.forFileObject(fo);
        if (phpModule == null) {
            return false;
        }
        return SymfonyPhpFrameworkProvider.getInstance().isInPhpModule(phpModule);
    }

    static final class GoToActionAction extends AbstractAction {
        private static final long serialVersionUID = -95284445913404L;
        private static final Pattern ACTION_METHOD_NAME = Pattern.compile("^([a-z0-9_]+)[A-Z]"); // NOI18N

        private final FileObject fo;

        public GoToActionAction(FileObject fo) {
            super(NbBundle.getMessage(GoToActionOrViewAction.class, "LBL_GoToAction"));
            assert SymfonyUtils.isViewWithAction(fo);
            this.fo = fo;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileObject action = SymfonyUtils.getAction(fo);
            if (action != null) {
                UiUtils.open(action, getActionMethodOffset(action));
            }
        }

        private int getActionMethodOffset(FileObject action) {
            String actionMethodName = getActionMethodName(fo.getName());
            EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
            for (PhpClass phpClass : editorSupport.getClasses(action)) {
                if (actionMethodName != null) {
                    for (PhpClass.Method method : phpClass.getMethods()) {
                        if (actionMethodName.equals(method.getName())) {
                            return method.getOffset();
                        }
                    }
                }
                return phpClass.getOffset();
            }
            return 0;
        }

        static String getActionMethodName(String filename) {
            Matcher matcher = ACTION_METHOD_NAME.matcher(filename);
            if (matcher.find()) {
                String group = matcher.group(1);
                return "execute" + group.substring(0, 1).toUpperCase() + group.substring(1); // NOI18N
            }
            return null;
        }
    }

    private static final class GoToViewAction extends AbstractAction {
        private static final long serialVersionUID = -95232154930113404L;

        private final FileObject fo;

        public GoToViewAction(FileObject fo) {
            super(NbBundle.getMessage(GoToActionOrViewAction.class, "LBL_GoToView"));
            assert SymfonyUtils.isAction(fo);
            this.fo = fo;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // XXX
            File parent = FileUtil.toFile(fo).getParentFile();
            File view = PropertyUtils.resolveFile(parent, "../templates/indexSuccess.php"); // NOI18N
            if (view != null) {
                UiUtils.open(FileUtil.toFileObject(view), 0);
            }
        }
    }
}
