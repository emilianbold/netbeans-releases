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

package org.netbeans.modules.kenai.ui;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
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
import org.netbeans.modules.kenai.ui.dashboard.DashboardImpl;
import org.netbeans.modules.kenai.ui.project.DetailsAction;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.netbeans.modules.kenai.ui.spi.LoginHandle;
import org.netbeans.modules.kenai.ui.spi.ProjectAccessor;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.netbeans.modules.mercurial.api.Mercurial;
import org.netbeans.modules.subversion.api.Subversion;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service=ProjectAccessor.class)
public class ProjectAccessorImpl extends ProjectAccessor {

    @Override
    public List<ProjectHandle> getMemberProjects(Kenai kenai, LoginHandle login, boolean force) {
        try {
            LinkedList<ProjectHandle> l = new LinkedList<ProjectHandle>();
            for (KenaiProject prj : kenai.getMyProjects(force)) {
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
    public ProjectHandle getNonMemberProject(Kenai kenai, String projectId, boolean force) {
        try {
            return new ProjectHandleImpl(kenai.getProject(projectId,force));
        } catch (KenaiException ex) {
            Logger.getLogger(ProjectAccessorImpl.class.getName()).log(Level.INFO, "getProject() " + projectId + " failed", ex);
            return null;
        }
    }

    @Override
    public Action getOpenNonMemberProjectAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new OpenKenaiProjectAction(DashboardImpl.getInstance().getKenai()).actionPerformed(null);
            }
        };
    }

    @Override
    public Action getDetailsAction(final ProjectHandle project) {
        return DetailsAction.forProject(project);    
//        return new URLDisplayerAction(NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_EditProject"), ((ProjectHandleImpl) project).getKenaiProject().getWebLocation());
    }

    private Action getOpenAction(final ProjectHandle project) {
        // this action is supposed to be used for openenig a project from My Projects
        return new AbstractAction(NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_OpenProject")) { // NOI18N
            public void actionPerformed(ActionEvent e) {
                Dashboard.getDefault().addProject(project, false, true);
            }
        };
    }

    @Override
    public Action getDefaultAction(ProjectHandle project, boolean opened) {
        return opened ? getDetailsAction(project) : getOpenAction(project);
    }

    @Override
    public Action[] getPopupActions(final ProjectHandle project, boolean opened) {
        if (!opened) {
            return new Action[]{
                        getOpenAction(project),
                        new RefreshAction(project),
                        getDetailsAction(project),
            };
        } else {
            return new Action[]{
                        new RemoveProjectAction(project),
                        new RefreshAction(project),
                        getDetailsAction(project)
            };
        }
    }

    @Override
    public Action getOpenWikiAction(ProjectHandle project) {
        try {
            KenaiFeature[] wiki = ((ProjectHandleImpl) project).getKenaiProject().getFeatures(Type.WIKI);
            if (wiki.length == 1) {
                return new URLDisplayerAction(wiki[0].getDisplayName(), wiki[0].getWebLocation());
            }
        } catch (KenaiException kenaiException) {
            Exceptions.printStackTrace(kenaiException);
        }
        return null;
    }

    @Override
    public Action getOpenDownloadsAction(ProjectHandle project) {
        try {
            KenaiFeature[] wiki = ((ProjectHandleImpl) project).getKenaiProject().getFeatures(Type.DOWNLOADS);
            if (wiki.length == 1) {
                return new URLDisplayerAction(wiki[0].getDisplayName(), wiki[0].getWebLocation());
            }
        } catch (KenaiException kenaiException) {
            Exceptions.printStackTrace(kenaiException);
        }
        return null;
    }

    @Override
    public Action getBookmarkAction(final ProjectHandle project) {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Kenai kenai = project.getKenaiProject().getKenai();
                try {
                    if (kenai.getStatus()==Kenai.Status.OFFLINE) {
                        UIUtils.showLogin(kenai);
                        return;
                    }
                    if (kenai.getMyProjects().contains(project.getKenaiProject())) {
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
                DashboardImpl.getInstance().bookmarkingStarted();
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            KenaiProject prj = project.getKenaiProject();
                            if (prj.getKenai().getMyProjects().contains(prj)) {
                                unbookmark(prj);
                            } else {
                                bookmark(prj);
                            }
                        } catch (KenaiException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    DashboardImpl.getInstance().bookmarkingFinished();
                                }
                            });
                        }
                    }
                });
            }
        };
    }

    private void unbookmark(KenaiProject prj) throws KenaiException {
        String fullName = prj.getKenai().getPasswordAuthentication().getUserName()
         + "@" + prj.getKenai().getUrl().getHost();
        KenaiUser user = KenaiUser.forName(fullName);
        prj.deleteMember(user);
    }

    private void bookmark(KenaiProject prj) throws KenaiException {
        String fullName = prj.getKenai().getPasswordAuthentication().getUserName()
         + "@" + prj.getKenai().getUrl().getHost();
        KenaiUser user = KenaiUser.forName(fullName);
        prj.addMember(user, Role.OBSERVER);
    }

    @Override
    public Action getNewKenaiProjectAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new NewKenaiProjectAction(DashboardImpl.getInstance().getKenai()).actionPerformed(null);
            }
        };
    }

    private static class RefreshAction extends AbstractAction {

        private final ProjectHandle project;

        public RefreshAction(ProjectHandle project) {
            super( NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_RefreshProject"));
            this.project = project;
        }

        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    try {
                        project.getKenaiProject().getKenai().getProject(project.getId(), true);
                    } catch (KenaiException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    }
}
