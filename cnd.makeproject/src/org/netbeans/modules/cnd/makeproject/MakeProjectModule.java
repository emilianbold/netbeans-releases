/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.netbeans.modules.cnd.makeproject.api.compilers.CCCCompiler;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.modules.ModuleInstall;

public class MakeProjectModule extends ModuleInstall {
    @Override
    public void restored() {
        // Moved to services...
//	RunProfileProvider profileProvider = new RunProfileProvider();
//	ConfigurationDescriptorProvider.addAuxObjectProvider(profileProvider);
//	profileCustomizerNode = new RunProfileNodeProvider().createProfileNode();
//	CustomizerRootNodeProvider.getInstance().addCustomizerNode(profileCustomizerNode);
    }

    public void uninstall() {
    }

    @Override
    public void close() {
        CompilerSetManager csm = CompilerSetManager.getDefault(false);
        if (csm != null) {
            for (CompilerSet cs : csm.getCompilerSets()) {
                for (Tool tool : cs.getTools()) {
                    if (tool instanceof CCCCompiler) { // FIXUP: should implement/use 'capability' of tool
                        ((CCCCompiler) tool).saveSystemIncludesAndDefines();
                    }
                }
            }
        }
    }

    public static class ActionWrapper extends CallableSystemAction implements ContextAwareAction, PropertyChangeListener {

        private Action action;

        public ActionWrapper(Action action) {
            this.action = action;
        }

        public String getName() {
            return (String) action.getValue(Action.NAME);
        }

        @Override
        public String iconResource() {
            return null;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            action.actionPerformed(ev);
        }

        @Override
        public boolean isEnabled() {
            return action.isEnabled();
        }

        @Override
        protected void addNotify() {
            this.action.addPropertyChangeListener(this);
            super.addNotify();
        }

        @Override
        protected void removeNotify() {
            this.action.removePropertyChangeListener(this);
            super.removeNotify();
        }

        public void performAction() {
            actionPerformed(new ActionEvent(this, 0, "")); // NOI18N
        }

        public Action createContextAwareInstance(Lookup actionContext) {
            return ((ContextAwareAction) action).createContextAwareInstance(actionContext);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }

    public static class CompileWrapper extends ActionWrapper {

        CompileWrapper() {
            super(FileSensitiveActions.fileCommandAction(
                    ActionProvider.COMMAND_COMPILE_SINGLE,
                    NbBundle.getMessage(MakeProjectModule.class, "LBL_CompileFile_Action"), // NOI18N
                    null));
        }
    }

    public static class RunWrapper extends ActionWrapper {

        RunWrapper() {
            super(FileSensitiveActions.fileCommandAction(
                    ActionProvider.COMMAND_RUN_SINGLE,
                    NbBundle.getMessage(MakeProjectModule.class, "LBL_RunFile_Action"), // NOI18N
                    null));

        }
    }

    public static class DebugWrapper extends ActionWrapper {

        DebugWrapper() {
            super(FileSensitiveActions.fileCommandAction(
                    ActionProvider.COMMAND_DEBUG_SINGLE,
                    NbBundle.getMessage(MakeProjectModule.class, "LBL_DebugFile_Action"), // NOI18N
                    null));
        }
    }
}
