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
package org.netbeans.modules.team.ui.share;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.team.ui.TeamServerManager;
import org.netbeans.modules.team.ui.Utilities;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamServerProvider;
import org.netbeans.modules.team.ui.spi.TeamUIUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.windows.WindowManager;
import org.openide.util.NbBundle.Messages;

@Messages("CTL_ShareAction=Share on Team Server...")
public final class ShareAction extends AbstractAction implements ContextAwareAction {

    private static ShareAction inst = null;
    private static Map<Project, Boolean> versionedProjects = Collections.synchronizedMap(new WeakHashMap<Project, Boolean>());

    private ShareAction() {
        putValue(NAME, Bundle.CTL_ShareAction());
    }

    @ActionID(id = "org.netbeans.modules.team.ui.ShareAction", category = "Team")
    @ActionRegistration(lazy = false, displayName = "#CTL_ShareAction")
    @ActionReference(path = "Projects/Actions", position = 150)
    public static synchronized ShareAction getDefault() {
        if (inst == null) {
            inst = new ShareAction();
        }
        return inst;
    }

    public static void actionPerformed(Node[] e) {
        ContextShareAction.actionPerformed(e);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextShareAction();
    }

    static class ContextShareAction extends AbstractAction implements Presenter.Popup {

        public ContextShareAction() {
            putValue(NAME, Bundle.CTL_ShareAction());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Node[] n = WindowManager.getDefault().getRegistry().getActivatedNodes();
            if (n.length > 0) {
                ContextShareAction.actionPerformed(n);
            } else {
                ContextShareAction.actionPerformed((Node[]) null);
            }
        }

        @Messages({
            "# {0} - project name",
            "NameAndLicenseWizardPanelGUI.versioningNotSupported=Local project \"{0}\" is already shared via versioning system."
        })
        public static void actionPerformed (final Node[] e) {
            if (e != null) {
                for (Node node : e) {
                    final Project prj = node.getLookup().lookup(Project.class);
                    if (prj != null) {
                        if (Boolean.TRUE.equals(versionedProjects.get(prj))) {
                            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                                    Bundle.NameAndLicenseWizardPanelGUI_versioningNotSupported(ProjectUtils.getInformation(prj).getDisplayName()));
                            return;
                        }
                    }
                }
            }
            Action a = getShareAction();
            if (a != null) {
                a.actionPerformed(new ActionEvent(ShareAction.getDefault(), ActionEvent.ACTION_PERFORMED, null));
            }
        }

        @Override
        public JMenuItem getPopupPresenter() {
            final JMenuItem i = new JMenuItem(this);
            i.setVisible(false);

            Node[] n = WindowManager.getDefault().getRegistry().getActivatedNodes();
            if (n.length == 1) {
                i.setVisible(true);
                final Project prj = n[0].getLookup().lookup(Project.class);
                if (prj == null) {
                    Logger.getLogger(ShareAction.class.getName()).log(Level.FINE, "ShareAction: cannot find project for node {0}", n[0].getDisplayName());
                } else if (Boolean.TRUE.equals(versionedProjects.get(prj))) {
                    i.setVisible(false);
                } else {
                    Utilities.getRequestProcessor().post(new Runnable() {
                        @Override
                        public void run() {
                            if (Boolean.TRUE.equals(prj.getProjectDirectory().getAttribute("ProvidedExtensions.VCSManaged"))) { //NOI18N
                                versionedProjects.put(prj, Boolean.TRUE);
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        i.setVisible(false);
                                        ((JPopupMenu) i.getParent()).pack();
                                    }
                                });
                            }
                        }
                    });
                }
            }
            return i;
        }
    }

    @NbBundle.Messages("LBL_SelectServer.title=Select Team Server")
    private static Action getShareAction () {
        Action action = null;
        Set<TeamServerProvider> offeredProviders = new HashSet<TeamServerProvider>(5);
        for (TeamServerProvider p : TeamServerManager.getDefault().getProviders()) {
            if (p.getShareAction() != null) {
                offeredProviders.add(p);
            }
        }
        if (offeredProviders.size() == 1) {
            action = offeredProviders.iterator().next().getShareAction();
        } else if (offeredProviders.size() > 1) {
            SelectServerPanel panel = new SelectServerPanel(offeredProviders.toArray(new TeamServerProvider[offeredProviders.size()]));
            DialogDescriptor dd = new DialogDescriptor(panel, Bundle.LBL_SelectServer_title(),
                    true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, 
                    new HelpCtx("org.netbeans.modules.team.ui.share.ShareAction"), null); //NOI18N
            if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
                TeamServer server = panel.getSelectedServer();
                if (server != null) {
                    TeamUIUtils.setSelectedServer(server);
                    action = server.getProvider().getShareAction();
                }
            }
        }
        return action;
    }
}

