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
package org.netbeans.modules.team.server.ui.share;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.netbeans.api.queries.VersioningQuery;
import org.netbeans.modules.team.ide.spi.IDEProject;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.netbeans.modules.team.server.Utilities;
import org.netbeans.modules.team.server.api.TeamServerManager;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.netbeans.modules.team.server.ui.spi.TeamServerProvider;
import org.netbeans.modules.team.server.api.TeamUIUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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

    /** Caching versioning status of projects for quick check in EDT. */
    private static Map<FileObject, Boolean> versionedProjects = Collections.synchronizedMap(new WeakHashMap<FileObject, Boolean>());

    private ShareAction() {
        putValue(NAME, Bundle.CTL_ShareAction());
    }

    @ActionID(id = "org.netbeans.modules.team.server.ui.ShareAction", category = "Team")
    @ActionRegistration(lazy = false, displayName = "#CTL_ShareAction")
    @ActionReference(path = "Projects/Actions", position = 150)
    public static synchronized ShareAction getDefault() {
        if (inst == null) {
            inst = new ShareAction();
        }
        return inst;
    }

    public static void actionPerformed() {
        ContextShareAction.actionPerformed();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextShareAction();
    }

    private static FileObject[] getSelectedProjects() {
        ProjectServices projects = Lookup.getDefault().lookup(ProjectServices.class);
        FileObject[] selectedFiles = projects.getCurrentSelection();
        for (int i=0; i < selectedFiles.length; i++) {
            FileObject fo = selectedFiles[i];
            FileObject projRoot = projects.getFileOwnerDirectory(fo);
            if (projRoot != null && projRoot != fo) {
                selectedFiles[i] = projRoot;
            }
        }
        return selectedFiles;
    }

    static class ContextShareAction extends AbstractAction implements Presenter.Popup {

        public ContextShareAction() {
            putValue(NAME, Bundle.CTL_ShareAction());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ContextShareAction.actionPerformed();
        }

        @Messages({
            "# {0} - project name",
            "ShareAction.versioningNotSupported=Local project \"{0}\" is already shared via versioning system.",
            "ShareAction.versioningNotSupported2=Selected folder is already shared via versioning system."
        })
        public static void actionPerformed() {
            FileObject[] projectDirs = getSelectedProjects();
            File[] toShare = new File[projectDirs.length];
            int i = 0;
            for (FileObject prjDir : projectDirs) {
                boolean alreadyVersioned;
                if (Boolean.TRUE.equals(versionedProjects.get(prjDir))) {
                    alreadyVersioned = true;
                } else if (!versionedProjects.containsKey(prjDir)) {
                    alreadyVersioned = VersioningQuery.isManaged(prjDir.toURI());
                    versionedProjects.put(prjDir, alreadyVersioned);
                } else {
                    alreadyVersioned = false;
                }
                if (alreadyVersioned) {
                    String prjDisplayName = null;
                    IDEProject ideProject = Lookup.getDefault().lookup(ProjectServices.class).getIDEProject(prjDir.toURL());
                    if (ideProject != null) {
                        prjDisplayName = ideProject.getDisplayName();
                    }
                    String message = prjDisplayName != null ? Bundle.ShareAction_versioningNotSupported(prjDisplayName)
                                                            : Bundle.ShareAction_versioningNotSupported2();
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), message);
                    return;
                }
                toShare[i++] = FileUtil.toFile(prjDir);
            }
            TeamServerProvider tsp = getTeamServerProvider();
            if (tsp != null) {
                tsp.createNewTeamProject(toShare);
            }
        }

        @Override
        public JMenuItem getPopupPresenter() {
            final JMenuItem item = new JMenuItem(this);
            item.setVisible(false);

            final FileObject[] selected = getSelectedProjects();
            if (selected.length > 1) {
                boolean anyVersioned = false;
                for (FileObject prjDir : selected) {
                    if (Boolean.TRUE.equals(versionedProjects.get(prjDir))) {
                        anyVersioned = true;
                        break;
                    }
                }
                if (!anyVersioned) {
                    item.setVisible(true);
                }
                Utilities.getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        boolean anyVersioned = false;
                        for (FileObject prjDir : selected) {
                            if (VersioningQuery.isManaged(prjDir.toURI())) { 
                                versionedProjects.put(prjDir, Boolean.TRUE);
                                anyVersioned = true;
                            } else {
                                versionedProjects.put(prjDir, Boolean.FALSE);
                            }
                        }
                        final boolean showItem = !anyVersioned;
                        if (anyVersioned) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    item.setVisible(showItem);
                                    ((JPopupMenu)item.getParent()).pack();
                                }
                            });
                        }
                    }
                });
            }
            return item;
        }
    }

    @NbBundle.Messages("LBL_SelectServer.title=Select Team Server")
    private static TeamServerProvider getTeamServerProvider() {
        TeamServerProvider tsp = null;
        Set<TeamServerProvider> offeredProviders = new HashSet<TeamServerProvider>(5);
        for (TeamServerProvider p : TeamServerManager.getDefault().getProviders()) {
            if (p.supportNewTeamProjectCreation()) {
                offeredProviders.add(p);
            }
        }
        if (offeredProviders.size() == 1) {
            tsp = offeredProviders.iterator().next();
        } else if (offeredProviders.size() > 1) {
            SelectServerPanel panel = new SelectServerPanel(offeredProviders.toArray(new TeamServerProvider[offeredProviders.size()]));
            DialogDescriptor dd = new DialogDescriptor(panel, Bundle.LBL_SelectServer_title(),
                    true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, 
                    new HelpCtx("org.netbeans.modules.team.server.ui.share.ShareAction"), null); //NOI18N
            if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
                TeamServer server = panel.getSelectedServer();
                if (server != null) {
                    TeamUIUtils.setSelectedServer(server);
                    tsp = server.getProvider();
                }
            }
        }
        return tsp;
    }
}
