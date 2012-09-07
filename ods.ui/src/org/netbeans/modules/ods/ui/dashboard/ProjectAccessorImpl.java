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

package org.netbeans.modules.ods.ui.dashboard;

import java.awt.EventQueue;
import org.netbeans.modules.ods.ui.project.DetailsAction;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.netbeans.modules.ods.api.CloudServer;
import org.netbeans.modules.ods.api.ODSProject;
import org.netbeans.modules.ods.client.api.ODSException;
import org.netbeans.modules.ods.ui.OpenProjectAction;
import org.netbeans.modules.ods.ui.Utilities;
import org.netbeans.modules.team.ui.spi.LoginHandle;
import org.netbeans.modules.team.ui.spi.ProjectAccessor;
import org.netbeans.modules.ods.ui.api.CloudUiServer;
import org.netbeans.modules.ods.ui.api.OdsUIUtil;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;
import static org.netbeans.modules.ods.ui.dashboard.Bundle.*;
import org.netbeans.modules.ods.ui.utils.Utils;
import org.netbeans.modules.team.ui.common.DefaultDashboard;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jan Becicka
 */
// XXX not properly implemented yet
public class ProjectAccessorImpl extends ProjectAccessor<CloudUiServer, ODSProject> {
    
    private final CloudUiServer uiServer;

    ProjectAccessorImpl(CloudUiServer uiServer) {
        this.uiServer = uiServer;
    }
    
    @Override
    public List<ProjectHandle<ODSProject>> getMemberProjects(CloudUiServer uiServer, LoginHandle login, boolean force) {
        try {
             return Utilities.getMyProjects(uiServer, force);
        } catch (ODSException ex) {
            Utils.logException(ex, false);
            return null;
        }
    }

    @Override
    public ProjectHandleImpl getNonMemberProject(CloudUiServer server, String projectId, boolean force) {
        try {
            CloudServer odsServer = server.getServer();
            if (odsServer != null) {
                ODSProject proj = odsServer.getProject(projectId, false);
                if (proj != null) {
                    return new ProjectHandleImpl(server, proj);
                }
            }
        } catch (ODSException ex) {
            Level lvl = ex instanceof ODSException.ODSCanceledException ? Level.FINE : Level.INFO;
            Logger.getLogger(ProjectAccessorImpl.class.getName()).log(lvl, null, ex);
            Logger.getLogger(ProjectAccessorImpl.class.getName()).log(lvl, "getting a project {0} from {1}", //NOI18N
                    new Object[] { projectId, uiServer.getUrl().toString() } );
        }
        return null;
    }

    @Override
    public Action getOpenNonMemberProjectAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TeamServer ts = org.netbeans.modules.team.ui.spi.TeamUIUtils.getSelectedServer();
                assert ts instanceof CloudUiServer;
                new OpenProjectAction((CloudUiServer) ts).actionPerformed(new ActionEvent(ts, ActionEvent.ACTION_PERFORMED, null));
            }
        };
    }

    @Override
    public Action getDetailsAction(final ProjectHandle<ODSProject> project) {
        return DetailsAction.forProject(project);    
//      XXX what is this ?  return new URLDisplayerAction(NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_EditProject"), ((ProjectHandleImpl) project).getProject().getWebLocation());
    }

    private Action getOpenAction(final ProjectHandle<ODSProject> project) {
        // this action is supposed to be used for openenig a project from My Projects
        return new AbstractAction(NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_OpenProject")) { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                uiServer.getDashboard().addProject(project, false, true);
            }
        };
    }

    @Override
    public Action getDefaultAction(ProjectHandle<ODSProject> project, boolean opened) {
        return opened ? getDetailsAction(project) : getOpenAction(project);
    }

    @Override
    public Action[] getPopupActions(final ProjectHandle<ODSProject> project, boolean opened) {
        if (!opened) {
            if (project.getTeamProject().getServer().isLoggedIn()) {
                return new Action[]{getOpenAction(project), new RefreshAction(project), getDetailsAction(project)};
            } else {
                return new Action[]{getOpenAction(project), new RefreshAction(project), getDetailsAction(project)};
            }
        } else {
            if (project.getTeamProject().getServer().isLoggedIn()) {
                return new Action[]{new RefreshAction(project), getDetailsAction(project)};
            } else {
                return new Action[]{new RefreshAction(project), getDetailsAction(project)};
            }
        }
    }

    @Override
    public Action getOpenWikiAction(ProjectHandle<ODSProject> project) {
        return null; // XXX does anybody call this?
    }

    @Override
    public Action getOpenDownloadsAction(ProjectHandle<ODSProject> project) {
        return null; // XXX does anybody call this?
    }

    @Override
    @Messages({"LBL_ReallyLeave=Really leave this project?", "LBL_ReallyLeaveTitle=Leave Project"})
    public Action getBookmarkAction(final ProjectHandle<ODSProject> project) {
        return new AbstractAction() {
            @Override
            public void actionPerformed (ActionEvent e) {
                final CloudServer server = project.getTeamProject().getServer();
                try {
                    if (!server.isLoggedIn()) {
                        OdsUIUtil.showLogin(server);
                        return;
                    }
                    if (server.getWatchedProjects(enabled).contains(project.getTeamProject())) {
                        if (JOptionPane.YES_OPTION
                                != JOptionPane.showConfirmDialog(
                                WindowManager.getDefault().getMainWindow(),
                                LBL_ReallyLeave(),
                                LBL_ReallyLeaveTitle(),
                                JOptionPane.YES_NO_OPTION)) {
                            return;
                        }
                    }
                } catch (ODSException ex) {
                    Exceptions.printStackTrace(ex);
                }
                final DefaultDashboard<CloudUiServer, ODSProject> dashboard = CloudUiServer.forServer(server).getDashboard();
                dashboard.bookmarkingStarted();
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run () {
                        try {
                            ODSProject prj = project.getTeamProject();
                            if (server.getMyProjects().contains(prj)) {
                                server.unwatch(prj);
                            } else {
                                server.watch(prj);
                            }
                        } catch (ODSException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run () {
                                    dashboard.bookmarkingFinished();
                                    dashboard.refreshMemberProjects(false);
                                    dashboard.refreshMemberProjects(true);
                                }
                            });
                        }
                    }
                });
            }
        };
    }

    @Override
    public Action getNewTeamProjectAction() {
        return NotYetAction.instance;
//        return new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                throw new UnsupportedOperationException("no yet!");
////                new NewProjectAction(DashboardImpl.getInstance().getServer().getKenai()).actionPerformed(null);
//            }
//        };
    }

    private class RefreshAction extends AbstractAction {

        private final ProjectHandle<ODSProject> projectHandle;

        public RefreshAction(ProjectHandle<ODSProject> project) {
            super( NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_RefreshProject"));
            this.projectHandle = project;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        uiServer.getServer().refresh(projectHandle.getTeamProject());
                    } catch (ODSException ex) {
                        Utils.logException(ex, false);
                    }
                }
            });
        }
    }

}
