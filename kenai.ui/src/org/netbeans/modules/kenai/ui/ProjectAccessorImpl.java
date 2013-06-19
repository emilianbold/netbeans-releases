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

package org.netbeans.modules.kenai.ui;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProjectMember.Role;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.api.KenaiUser;
import org.netbeans.modules.kenai.ui.api.KenaiServer;
import org.netbeans.modules.kenai.ui.project.DetailsAction;
import org.netbeans.modules.team.ui.spi.LoginHandle;
import org.netbeans.modules.team.ui.spi.ProjectAccessor;
import org.netbeans.modules.kenai.ui.api.KenaiUIUtils;
import org.netbeans.modules.mercurial.api.Mercurial;
import org.netbeans.modules.subversion.api.Subversion;
import org.netbeans.modules.team.ui.common.DashboardSupport;
import org.netbeans.modules.team.ui.common.URLDisplayerAction;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service=ProjectAccessor.class)
public class ProjectAccessorImpl extends ProjectAccessor<KenaiProject> {
    
    private static ProjectAccessorImpl instance;

    public static ProjectAccessorImpl getDefault() {
        if(instance == null) {
            instance = new ProjectAccessorImpl();
        }
        return instance;
    }

    @Override
    public List<ProjectHandle<KenaiProject>> getMemberProjects(TeamServer server, LoginHandle login, boolean force) {
        assert server instanceof KenaiServer;
        try {
            LinkedList<ProjectHandle<KenaiProject>> l = new LinkedList<ProjectHandle<KenaiProject>>();
            for (KenaiProject prj : ((KenaiServer)server).getKenai().getMyProjects(force)) {
                l.add(new ProjectHandleImpl(prj));
                for (KenaiFeature feature : prj.getFeatures(KenaiService.Type.SOURCE)) {
                    if (KenaiService.Names.SUBVERSION.equals(feature.getService())) {
                        try {
                            Subversion.addRecentUrl(feature.getLocation());
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } else if (KenaiService.Names.MERCURIAL.equals(feature.getService())) {
                        try {
                            Mercurial.addRecentUrl(feature.getLocation());
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }

                }
            }
            return l;
        } catch (KenaiException ex) {
            Logger.getLogger(ProjectAccessorImpl.class.getName()).log(Level.INFO, "getMyProject() failed", ex);
            return null;
        }
    }

    @Override
    public ProjectHandle<KenaiProject> getNonMemberProject(TeamServer server, String projectId, boolean force) {
        assert server instanceof KenaiServer;
        try {
            return new ProjectHandleImpl(((KenaiServer)server).getKenai().getProject(projectId,force));
        } catch (KenaiException ex) {
            Logger.getLogger(ProjectAccessorImpl.class.getName()).log(Level.INFO, "getProject() " + projectId + " failed", ex);
            return null;
        }
    }

    @Override
    public Action getDetailsAction(final ProjectHandle project) {
        return DetailsAction.forProject(project);    
//        return new URLDisplayerAction(NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_EditProject"), ((ProjectHandleImpl) project).getKenaiProject().getWebLocation());
    }

    private Action getOpenAction(final ProjectHandle project) {
        // this action is supposed to be used for openenig a project from My Projects
        return new AbstractAction(NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_OpenProject")) { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                Utilities.addProject(project, false, true);
            }
        };
    }

    @Override
    public Action getDefaultAction(ProjectHandle project, boolean opened) {
        return opened ? getDetailsAction(project) : getOpenAction(project);
    }

    @Override
    public Action[] getPopupActions(final ProjectHandle<KenaiProject> project, boolean opened) {
        PasswordAuthentication pa = project.getTeamProject().getKenai().getPasswordAuthentication();
        if (!opened) {
            try {
                if (pa != null && pa.getUserName().equals(project.getTeamProject().getOwner().getUserName())) {
                    return new Action[]{getOpenAction(project), new RefreshAction(project), getDetailsAction(project), new DeleteProjectAction(project)};
                } else {
                    return new Action[]{getOpenAction(project), new RefreshAction(project), getDetailsAction(project)};
                }
            } catch (KenaiException ex) {
                Exceptions.printStackTrace(ex);
                return new Action[]{getOpenAction(project), new RefreshAction(project), getDetailsAction(project)};
            }
        } else {
            try {
                if (pa != null && pa.getUserName().equals(project.getTeamProject().getOwner().getUserName())) {
                    return new Action[]{new RemoveProjectAction(project), new RefreshAction(project), getDetailsAction(project), new DeleteProjectAction(project)};
                } else {
                    return new Action[]{new RemoveProjectAction(project), new RefreshAction(project), getDetailsAction(project)};
                }
            } catch (KenaiException ex) {
                Exceptions.printStackTrace(ex);
                return new Action[]{new RemoveProjectAction(project), new RefreshAction(project), getDetailsAction(project)};
            }
        }
    }

    @Override
    public Action getOpenWikiAction(ProjectHandle<KenaiProject> project) {
        try {
            KenaiFeature[] wiki = ((ProjectHandleImpl) project).getTeamProject().getFeatures(Type.WIKI);
            if (wiki.length == 1) {
                return new URLDisplayerAction(wiki[0].getDisplayName(), wiki[0].getWebLocation());
            }
        } catch (KenaiException kenaiException) {
            Exceptions.printStackTrace(kenaiException);
        }
        return null;
    }

    @Override
    public Action getOpenDownloadsAction(ProjectHandle<KenaiProject> project) {
        try {
            KenaiFeature[] wiki = ((ProjectHandleImpl) project).getTeamProject().getFeatures(Type.DOWNLOADS);
            if (wiki.length == 1) {
                return new URLDisplayerAction(wiki[0].getDisplayName(), wiki[0].getWebLocation());
            }
        } catch (KenaiException kenaiException) {
            Exceptions.printStackTrace(kenaiException);
        }
        return null;
    }

    @Override
    public void bookmark(final ProjectHandle<KenaiProject> project) {
        Kenai kenai = project.getTeamProject().getKenai();
        try {
            if (kenai.getStatus()==Kenai.Status.OFFLINE) {
                KenaiUIUtils.showLogin(kenai);
                return;
            }
            if (kenai.getMyProjects().contains(project.getTeamProject())) {
                if (JOptionPane.YES_OPTION != 
                        JOptionPane.showConfirmDialog(
                        WindowManager.getDefault().getMainWindow(),
                        NbBundle.getMessage(ProjectAccessorImpl.class,"LBL_ReallyLeave"),
                        NbBundle.getMessage(ProjectAccessorImpl.class,"LBL_ReallyLeaveTitle"),
                        JOptionPane.YES_NO_OPTION)) {
                    return;
                }
            }
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }
        final DashboardSupport<KenaiProject> dashboard = KenaiServer.getDashboard(project);
        dashboard.bookmarkingStarted(project);
        Utilities.getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                try {
                    KenaiProject prj = project.getTeamProject();
                    if (prj.getKenai().getMyProjects().contains(prj)) {
                        unbookmark(prj);
                    } else {
                        bookmark(prj);
                    }
                } catch (KenaiException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dashboard.bookmarkingFinished(project);
                        }
                    });
                }
            }
        });
    }

    private void unbookmark(KenaiProject prj) throws KenaiException {
        String fullName = prj.getKenai().getPasswordAuthentication().getUserName()
         + "@" + prj.getKenai().getUrl().getHost(); // NOI18N
        KenaiUser user = KenaiUser.forName(fullName);
        prj.deleteMember(user);
    }

    private void bookmark(KenaiProject prj) throws KenaiException {
        String fullName = prj.getKenai().getPasswordAuthentication().getUserName()
         + "@" + prj.getKenai().getUrl().getHost(); // NOI18N
        KenaiUser user = KenaiUser.forName(fullName);
        prj.addMember(user, Role.OBSERVER);
    }

    private static class RefreshAction extends AbstractAction {

        private final ProjectHandle<KenaiProject> project;

        public RefreshAction(ProjectHandle<KenaiProject> project) {
            super( NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_RefreshProject"));
            this.project = project;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Utilities.getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        project.getTeamProject().getKenai().getProject(project.getId(), true);
                    } catch (KenaiException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    }
}
