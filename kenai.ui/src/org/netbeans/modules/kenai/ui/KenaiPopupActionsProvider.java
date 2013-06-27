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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.team.ui.common.NbModuleOwnerSupport;
import org.netbeans.modules.team.ui.common.NbModuleOwnerSupport.OwnerInfo;
import org.netbeans.modules.kenai.ui.api.KenaiServer;
import org.netbeans.modules.team.ui.common.DashboardSupport;
import org.netbeans.modules.team.ui.spi.PopupMenuProvider;
import org.netbeans.modules.team.ui.spi.TeamUIUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class KenaiPopupActionsProvider implements PopupMenuProvider {

    private static KenaiPopupActionsProvider inst = null;

    public static synchronized KenaiPopupActionsProvider getDefault() {
        if (inst == null) {
            inst = new KenaiPopupActionsProvider();
        }
        return inst;
    }

    private KenaiProject getActualKenaiProject (String kenaiProjectName, String repositoryUrl) throws KenaiException {
        KenaiProject defaultKenaiProject = KenaiProject.forRepository(repositoryUrl);
        Kenai kenai = defaultKenaiProject != null ? defaultKenaiProject.getKenai(): Utilities.getPreferredKenai(); //NOI18N
        KenaiProject kp = kenai == null ? null : kenai.getProject(kenaiProjectName);
        return kp;
    }

    private String getKenaiProjectName (Project proj, String repositoryUrl) {
        /* Add action to navigate to Kenai project - based on repository URL (not on Kenai dashboard at the moment) */
        // if isKenaiProject==true, there must be cached result + it is different from ""
        String kpName = null;
        OwnerInfo ownerInfo = NbModuleOwnerSupport.getInstance().getOwnerInfo(proj);
        if (ownerInfo != null) {
            try {
                // ensure project. If none with the given owner name available,
                // fallback on repoForProjCache
                KenaiProject kp = getActualKenaiProject(ownerInfo.getOwner(), repositoryUrl);
                if (kp != null) {
                    kpName = kp.getName();
                }
            } catch (KenaiException ex) {
                String err = ex.getLocalizedMessage();
                if (err == null) {
                    err = ex.getCause().getLocalizedMessage();
                }
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(KenaiPopupActionsProvider.class, "ERROR_CONNECTION", err))); //NOI18N
            }
        }
        if (kpName == null) {
            kpName = KenaiProject.getNameForRepository(repositoryUrl);
        }
        return kpName;
    }

    @Override
    public Action[] getPopupMenuActions (Project proj, String repositoryUrl) {
        String kpName = KenaiProject.getNameForRepository(repositoryUrl);
        if (kpName == null) {
            return new Action[0];
        } else {
            return new Action[] {
                new LazyOpenKenaiProjectAction(proj, repositoryUrl),
                null,
                new LazyFindIssuesAction(proj, repositoryUrl),
                new LazyNewIssuesAction(proj, repositoryUrl)
            };
        }
    }
    
    class LazyFindIssuesAction extends AbstractAction {
        private final String repositoryUrl;
        private final Project proj;

        public LazyFindIssuesAction (Project proj, String repositoryUrl) {
            super(NbBundle.getMessage(KenaiPopupActionsProvider.class, "FIND_ISSUE")); //NOI18N
            this.proj = proj;
            this.repositoryUrl = repositoryUrl;
        }

        @Override
        public void actionPerformed (final ActionEvent e) {
            new RequestProcessor("__ISSUETRACKER", 1).post(new Runnable() { //NOI18N

                @Override
                public void run() {
                    ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupActionsProvider.class, "CONTACTING_ISSUE_TRACKER"));  //NOI18N
                    handle.start();
                    try {
                        final KenaiProject kp = getActualKenaiProject(getKenaiProjectName(proj, repositoryUrl), repositoryUrl);
                        if (kp != null) {
                            if (kp.getFeatures(Type.ISSUES).length > 0) {
                                final ProjectHandleImpl pHandle = new ProjectHandleImpl(kp);
                                final DashboardSupport<KenaiProject> dashboard = KenaiServer.getDashboard(pHandle);
                                SwingUtilities.invokeLater( new Runnable() {
                                    @Override
                                    public void run() {
                                        Utilities.addProject(pHandle, false, true);
                                    }
                                });
                                dashboard.getDashboardProvider().getQueryAccessor(KenaiProject.class).getFindIssueAction(pHandle).actionPerformed(e);
                            } else {
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(KenaiPopupActionsProvider.class, "ERROR_ISSUETRACKER"))); //NOI18N
                            }
                        }
                    } catch (KenaiException e) {
                        String err = e.getLocalizedMessage();
                        if (err == null) {
                            err = e.getCause().getLocalizedMessage();
                        }
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(KenaiPopupActionsProvider.class, "ERROR_CONNECTION", err))); //NOI18N
                    } finally {
                        handle.finish();
                    }
                }
            });
        }
    }

    class LazyNewIssuesAction extends AbstractAction {
        private final Project proj;
        private final String repositoryUrl;

        public LazyNewIssuesAction(final Project proj, final String repositoryUrl) {
            super(NbBundle.getMessage(KenaiPopupActionsProvider.class, "NEW_ISSUE")); //NOI18N
            this.proj = proj;
            this.repositoryUrl = repositoryUrl;
        }

        @Override
        public void actionPerformed (final ActionEvent e) {
            new RequestProcessor("__ISSUETRACKER", 1).post(new Runnable() {  //NOI18N
                @Override
                public void run() {
                    ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupActionsProvider.class, "CONTACTING_ISSUE_TRACKER")); //NOI18N
                    handle.start();
                    try {
                        final KenaiProject kp = getActualKenaiProject(getKenaiProjectName(proj, repositoryUrl), repositoryUrl);
                        if (kp != null) {
                            if (kp.getFeatures(Type.ISSUES).length > 0) {
                                final ProjectHandleImpl pHandle = new ProjectHandleImpl(kp);
                                final DashboardSupport<KenaiProject> dashboard = KenaiServer.getDashboard(pHandle);
                                SwingUtilities.invokeLater( new Runnable() {
                                    @Override
                                    public void run() {
                                        Utilities.addProject(pHandle, false, true);
                                    }
                                });
                                dashboard.getDashboardProvider().getQueryAccessor(KenaiProject.class).getCreateIssueAction(pHandle).actionPerformed(e);
                            } else {
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(KenaiPopupActionsProvider.class, "ERROR_ISSUETRACKER"))); //NOI18N
                            }
                        }
                    } catch (KenaiException e) {
                        String err = e.getLocalizedMessage();
                        if (err == null) {
                            err = e.getCause().getLocalizedMessage();
                        }
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(KenaiPopupActionsProvider.class, "ERROR_CONNECTION", err))); //NOI18N
                    } finally {
                        handle.finish();
                    }
                }
            });
        }
    }

    class LazyOpenKenaiProjectAction extends AbstractAction {
        private Project proj;
        private String repositoryUrl;

        public LazyOpenKenaiProjectAction (final Project proj, final String repositoryUrl) {
            super(NbBundle.getMessage(KenaiPopupActionsProvider.class, "OPEN_CORRESPONDING_KENAI_PROJ")); //NOI18N
            this.proj = proj;
            this.repositoryUrl = repositoryUrl;
        }

        @Override
        public void actionPerformed (final ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    TeamUIUtils.activateTeamDashboard();
                }
            });
            Utilities.getRequestProcessor().post(new Runnable() {

                @Override
                public void run() {
                    ProgressHandle handle = null;
                    try {
                        handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupActionsProvider.class, "CTL_OpenKenaiProjectAction")); //NOI18N
                        handle.start();
                        final KenaiProject kp = getActualKenaiProject(getKenaiProjectName(proj, repositoryUrl), repositoryUrl);
                        if (kp != null) {
                            final ProjectHandleImpl pHandle = new ProjectHandleImpl(kp);
                            SwingUtilities.invokeLater( new Runnable() {
                                @Override
                                public void run() {
                                    Utilities.addProject(pHandle, false, true);
                                }
                            });
                        }
                    } catch (KenaiException e) {
                        String err = e.getLocalizedMessage();
                        if (err == null) {
                            err = e.getCause().getLocalizedMessage();
                        }
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(KenaiPopupActionsProvider.class, "ERROR_CONNECTION", err))); //NOI18N
                    } finally {
                        handle.finish();
                    }
                }
            });
        }
    }

}