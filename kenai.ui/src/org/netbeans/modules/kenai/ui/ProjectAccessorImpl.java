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
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.project.DetailsAction;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.netbeans.modules.kenai.ui.spi.LoginHandle;
import org.netbeans.modules.kenai.ui.spi.ProjectAccessor;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.mercurial.api.Mercurial;
import org.netbeans.modules.subversion.api.Subversion;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service=ProjectAccessor.class)
public class ProjectAccessorImpl extends ProjectAccessor {

    private Kenai kenai = Kenai.getDefault();

    @Override
    public List<ProjectHandle> getMemberProjects(LoginHandle login, boolean force) {
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
    public ProjectHandle getNonMemberProject(String projectId, boolean force) {
        try {
            return new ProjectHandleImpl(kenai.getProject(projectId,force));
        } catch (KenaiException ex) {
            Logger.getLogger(ProjectAccessorImpl.class.getName()).log(Level.INFO, "getProject() " + projectId + " failed", ex);
            return null;
        }
    }

    @Override
    public Action getOpenNonMemberProjectAction() {
        return new OpenKenaiProjectAction();
    }

    @Override
    public Action getDetailsAction(final ProjectHandle project) {
        return DetailsAction.forProject(project.getId());    
//        return new URLDisplayerAction(NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_EditProject"), ((ProjectHandleImpl) project).getKenaiProject().getWebLocation());
    }

    @Override
    public Action getDefaultAction(ProjectHandle project) {
        return getDetailsAction(project);
    }

    @Override
    public Action[] getPopupActions(final ProjectHandle project) {
        if (Dashboard.getDefault().isMemberProject(project)) {
            return new Action[]{
                        new RefreshAction(project),
                        (Action) getDetailsAction(project),
            };
        } else {
            return new Action[]{
                        new RefreshAction(project),
                        new RemoveProjectAction(project),
                        (Action) getDetailsAction(project)
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
    public Action getBookmarkAction(ProjectHandle project) {
        return new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet. Please vote for http://kenai.com/jira/browse/KENAI-735");
            }
        };
    }

    @Override
    public Action getNewKenaiProjectAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ShareAction.actionPerformed((Node) null);
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
                        Kenai.getDefault().getProject(project.getId(), true);
                        project.firePropertyChange(ProjectHandle.PROP_CONTENT, null, project);
                    } catch (KenaiException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    }
}
