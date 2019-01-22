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

package org.netbeans.modules.odcs.ui.dashboard;

import java.awt.EventQueue;
import org.netbeans.modules.odcs.ui.project.DetailsAction;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.odcs.ui.Utilities;
import org.netbeans.modules.team.server.ui.spi.LoginHandle;
import org.netbeans.modules.team.server.ui.spi.ProjectAccessor;
import org.netbeans.modules.odcs.ui.api.ODCSUiServer;
import org.netbeans.modules.odcs.ui.api.OdcsUIUtil;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;
import static org.netbeans.modules.odcs.ui.dashboard.Bundle.*;
import org.netbeans.modules.odcs.ui.utils.Utils;
import org.netbeans.modules.team.server.ui.common.DashboardSupport;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 *
 * 
 */
// XXX not properly implemented yet
public class ProjectAccessorImpl extends ProjectAccessor<ODCSProject> {

    private static final Logger LOG = Logger.getLogger(ProjectAccessorImpl.class.getName());
    private final ODCSUiServer uiServer;

    ProjectAccessorImpl(ODCSUiServer uiServer) {
        this.uiServer = uiServer;
    }
    
    @Override
    public List<ProjectHandle<ODCSProject>> getMemberProjects(TeamServer uiServer, LoginHandle login, boolean force) {
        assert uiServer instanceof TeamServer;
        try {
             return Utilities.getMyProjects((ODCSUiServer) uiServer, force);
        } catch (ODCSException ex) {
            Utils.logException(ex, false);
            return null;
        }
    }

    @Override
    public ProjectHandleImpl getNonMemberProject(TeamServer server, String projectId, boolean force) {
        assert uiServer instanceof TeamServer;
        try {
            ODCSServer odcsServer = ((ODCSUiServer)server).getServer();
            if (odcsServer != null) {
                ODCSProject proj = odcsServer.getProject(projectId, force);
                if (proj != null) {
                    return new ProjectHandleImpl((ODCSUiServer)server, proj);
                }
            }
        } catch (ODCSException ex) {
            Level lvl = ex instanceof ODCSException.ODCSCanceledException ? Level.FINE : Level.INFO;
            LOG.log(lvl, null, ex);
            LOG.log(lvl, "getting a project {0} from {1}", new Object[] { projectId, uiServer.getUrl().toString() } ); // NOI18N
        }
        return null;
    }

    @Override
    public Action getDetailsAction(final ProjectHandle<ODCSProject> project) {
        return DetailsAction.forProject(project);    
//      XXX what is this ?  return new URLDisplayerAction(NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_EditProject"), ((ProjectHandleImpl) project).getProject().getWebLocation());
    }
    
    private Action getWebAction(final ProjectHandle<ODCSProject> project) {
        return new AbstractAction(NbBundle.getMessage(ProjectAccessorImpl.class, "LBL_GotoProjectWeb")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Utils.openBrowser(project.getTeamProject().getWebUrl());
            }
        };
    }

    private Action getOpenAction(final ProjectHandle<ODCSProject> project) {
        // this action is supposed to be used for openenig a project from My Projects
        return new AbstractAction(NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_OpenProject")) { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                uiServer.getDashboard().addProjects(new ProjectHandle[] {project}, false, true);
            }
        };
    }

    @Override
    public Action getDefaultAction(ProjectHandle<ODCSProject> project, boolean opened) {
        return opened ? getDetailsAction(project) : getOpenAction(project);
    }

    @Override
    public Action[] getPopupActions(final ProjectHandle<ODCSProject> project, boolean opened) {
        if (!opened) {
            return new Action[]{getOpenAction(project), new RefreshAction(project), getDetailsAction(project), getWebAction(project)};
        } else {
            return new Action[]{new RefreshAction(project), getDetailsAction(project), getWebAction(project)};
        }
    }

    @Override
    public Action getOpenWikiAction(ProjectHandle<ODCSProject> project) {
        return null; // XXX does anybody call this?
    }

    @Override
    public Action getOpenDownloadsAction(ProjectHandle<ODCSProject> project) {
        return null; // XXX does anybody call this?
    }

    @Override
    public boolean canBookmark() {
        return false;
    }
    
    @Override
    public void bookmark(final ProjectHandle<ODCSProject> project) { }

    private class RefreshAction extends AbstractAction {

        private final ProjectHandle<ODCSProject> projectHandle;

        public RefreshAction(ProjectHandle<ODCSProject> project) {
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
                    } catch (ODCSException ex) {
                        Utils.logException(ex, false);
                    }
                }
            });
        }
    }

}
