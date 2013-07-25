/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.odcs.ui.api.ODCSUiServer;
import org.netbeans.modules.odcs.ui.dashboard.ProjectHandleImpl;
import org.netbeans.modules.odcs.ui.utils.Utils;
import org.netbeans.modules.team.server.ui.common.DashboardSupport;
import org.netbeans.modules.team.server.ui.spi.PopupMenuProvider;
import org.netbeans.modules.team.server.ui.spi.TeamUIUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import static org.netbeans.modules.odcs.ui.Bundle.*;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Vrabec
 */
@Messages({"CONTACTING_ISSUE_TRACKER=Contacting Issue Tracker ...",
            "ERROR_ISSUETRACKER=<html>This project does not have an issue tracker.</html>",
            "# {0} - error description", "ERROR_CONNECTION=<html>There was an error while trying to connect to the Team Server - it is not possible to get the Kenai project info.<br>Check the Internet connection and that the Team project still exists, please.<br><br>Reported error was: <b>{0}</b></html>"})
class PopupActionsProvider implements PopupMenuProvider {
    private static PopupActionsProvider inst = null;

    public static synchronized PopupActionsProvider getDefault() {
        if (inst == null) {
            inst = new PopupActionsProvider();
        }
        return inst;
    }

    @Override
    public Action[] getPopupMenuActions (Project proj, String repositoryUrl) {
        return new Action[] {
            new LazyOpenTeamProjectAction(proj, repositoryUrl),
            null,
            new LazyFindIssuesAction(proj, repositoryUrl),
            new LazyNewIssuesAction(proj, repositoryUrl)
        };
    }
    
    abstract class LazyProjectAction extends AbstractAction {
        protected final String repositoryUrl;
        private final String progressMessage;

        public LazyProjectAction (String actionName, String repositoryUrl, String progressMessage) {
            super(actionName);
            this.repositoryUrl = repositoryUrl;
            this.progressMessage = progressMessage;
        }
        
        @Override
        public final void actionPerformed (final ActionEvent e) {
            final ProgressHandle handle = ProgressHandleFactory.createHandle(progressMessage);
            handle.start();
            Utils.getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        ODCSServer server = ODCSServer.findServerForRepository(repositoryUrl);
                        if (server != null) {
                            ODCSUiServer uiServer = ODCSUiServer.forServer(server);
                            if (!server.isLoggedIn()) {
                                if (uiServer != TeamUIUtils.showLogin(uiServer, false)) {
                                    return;
                                }
                            }
                            final ODCSProject project = ODCSProject.findProjectForRepository(repositoryUrl);
                            if (project != null) {
                                if (project.hasTasks()) {
                                    final ProjectHandleImpl pHandle = new ProjectHandleImpl(uiServer, project);
                                    final DashboardSupport<ODCSProject> dashboard = uiServer.getDashboard();
                                    EventQueue.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            dashboard.addProjects(new ProjectHandleImpl[] {pHandle}, false, true);
                                        }
                                    });
                                    performAction(dashboard, pHandle, e);
                                } else {
                                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ERROR_ISSUETRACKER()));
                                }
                            }
                        }
                    } catch (ODCSException e) {
                        String err = e.getLocalizedMessage();
                        if (err == null) {
                            err = e.getMessage();
                        }
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ERROR_CONNECTION(err)));
                    } finally {
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                handle.finish();
                            }
                        });
                    }
                }
            });
        }
        
        protected abstract void performAction (DashboardSupport<ODCSProject> dashboard, ProjectHandle<ODCSProject> pHandle, ActionEvent e);
    }
    
    class LazyFindIssuesAction extends LazyProjectAction {

        @NbBundle.Messages("LBL_FindIssuesActionName=Find Issues...")
        public LazyFindIssuesAction (Project proj, String repositoryUrl) {
            super(Bundle.LBL_FindIssuesActionName(), repositoryUrl, Bundle.CONTACTING_ISSUE_TRACKER());
        }

        @Override
        protected void performAction (DashboardSupport<ODCSProject> dashboard, ProjectHandle<ODCSProject> pHandle, ActionEvent e) {
            dashboard.getDashboardProvider().getQueryAccessor(ODCSProject.class).getFindIssueAction(pHandle).actionPerformed(e);
        }
    }

    class LazyNewIssuesAction extends LazyProjectAction {

        @NbBundle.Messages("LBL_NewIssuesActionName=Report Issue...")
        public LazyNewIssuesAction(final Project proj, final String repositoryUrl) {
            super(Bundle.LBL_NewIssuesActionName(), repositoryUrl, CONTACTING_ISSUE_TRACKER());
        }

        @Override
        protected void performAction (DashboardSupport<ODCSProject> dashboard, ProjectHandle<ODCSProject> pHandle, ActionEvent e) {
            dashboard.getDashboardProvider().getQueryAccessor(ODCSProject.class).getCreateIssueAction(pHandle).actionPerformed(e);
        }

    }

    class LazyOpenTeamProjectAction extends LazyProjectAction {

        @NbBundle.Messages({ "LBL_OpenTeamProjectName=Open Corresponding Team Server Project ...",
            "MSG_OpeningProject=Open Team Project..."
        })
        public LazyOpenTeamProjectAction (final Project proj, final String repositoryUrl) {
            super(Bundle.LBL_OpenTeamProjectName(), repositoryUrl, Bundle.MSG_OpeningProject());
        }

        @Override
        protected void performAction (DashboardSupport<ODCSProject> dashboard, ProjectHandle<ODCSProject> pHandle, ActionEvent e) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    TeamUIUtils.activateTeamDashboard();
                }
            });
        }
    }
    }
