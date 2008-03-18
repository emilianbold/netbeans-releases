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
package org.netbeans.modules.sun.manager.jbi.actions;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.sun.manager.jbi.nodes.Undeployable;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Action to undeploy one or more JBI Service Assemblies.
 * 
 * @author jqian
 */
public abstract class UndeployAction extends NodeAction {

    protected void performAction(final Node[] activatedNodes) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    for (Node node : activatedNodes) {
                        Lookup lookup = node.getLookup();
                        Undeployable undeployable = lookup.lookup(Undeployable.class);

                        if (undeployable != null) {
                            undeployable.undeploy(isForceAction());
                        }
                    }
                } catch (RuntimeException rex) {
                    //gobble up exception
                }
            }
        });
    }

    protected boolean enable(Node[] activatedNodes) {
        boolean ret = false;

        if (activatedNodes != null && activatedNodes.length > 0) {
            ret = true;
            for (Node node : activatedNodes) {
                Undeployable undeployable =
                        node.getLookup().lookup(Undeployable.class);
                try {
                    if (undeployable != null && !undeployable.canUndeploy(isForceAction())) {
                        ret = false;
                        break;
                    }
                } catch (RuntimeException rex) {
                    //gobble up exception
                }
            }
        }

        return ret;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected abstract boolean isForceAction();

    /**
     * Normal undeploy action.
     */
    public static class Normal extends UndeployAction {

        protected boolean isForceAction() {
            return false;
        }

        public String getName() {
            return NbBundle.getMessage(ShutdownAction.class, "LBL_UndeployAction");  // NOI18N
        }
    }

    /**
     * Force undeploy action.
     */
    public static class Force extends UndeployAction implements Presenter.Popup {

        public String getName() {
            return NbBundle.getMessage(ShutdownAction.class, "LBL_ForceUndeployAction");  // NOI18N
        }

        public JMenuItem getPopupPresenter() {
            JMenu result = new JMenu(
                    NbBundle.getMessage(ShutdownAction.class, "LBL_Advanced"));  // NOI18N

            //result.add(new JMenuItem(SystemAction.get(ShutdownAction.Force.class)));
            Action forceShutdownAction = SystemAction.get(ShutdownAction.Force.class);
            JMenuItem forceShutdownMenuItem = new JMenuItem();
            Actions.connect(forceShutdownMenuItem, forceShutdownAction, false);
            result.add(forceShutdownMenuItem);

            //result.add(new JMenuItem(this));
            Action forceUndeployAction = SystemAction.get(UndeployAction.Force.class);
            JMenuItem forceUndeployMenuItem = new JMenuItem();
            Actions.connect(forceUndeployMenuItem, forceUndeployAction, false);
            result.add(forceUndeployMenuItem);

            return result;
        }

        protected boolean isForceAction() {
            return true;
        }
    }
}
