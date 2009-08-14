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
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.ui.dashboard.DashboardImpl;
import org.netbeans.modules.kenai.ui.spi.NbProjectHandle;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.QueryAccessor;
import org.netbeans.modules.kenai.ui.spi.SourceHandle;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.Presenter;

/**
 *
 * @author tester
 */
public class KenaiPopupMenu extends CookieAction {

    private static Pattern repositoryPattern = Pattern.compile("(https|http)://(testkenai|kenai)\\.com/(svn|hg)/(\\S*)~(.*)");

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new KenaiPopupMenuPresenter(actionContext);
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[]{Project.class, DataFolder.class};
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(KenaiPopupMenu.class, "KENAI_POPUP");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private final class KenaiPopupMenuPresenter extends AbstractAction implements Presenter.Popup {

        private final Project proj;
        private ProjectHandle kenaiProj = null;

        private KenaiPopupMenuPresenter(Lookup actionContext) {
            proj = actionContext.lookup(Project.class);
        }

        public JMenuItem getPopupPresenter() {
            JMenu kenaiPopup = new JMenu(NbBundle.getMessage(KenaiPopupMenu.class, "KENAI_POPUP")); //NOI18N
            if (proj == null || !isKenaiProject(proj) || getActivatedNodes().length > 1) {
                kenaiPopup.setVisible(false);
            } else {
                kenaiPopup.setVisible(true);
                KenaiFeature[] issueTrackers = null;
                try {
                    if (kenaiProj != null) {
                        issueTrackers = Kenai.getDefault().getProject(kenaiProj.getId()).getFeatures(Type.ISSUES);
                        if (issueTrackers != null && issueTrackers.length > 0) {
                            kenaiPopup.add(new LazyFindIssuesAction(kenaiProj));
                            kenaiPopup.add(new LazyNewIssuesAction(kenaiProj));
                            kenaiPopup.add(new JSeparator());
                        }
                    }
                    String projRepo = (String) proj.getProjectDirectory().getAttribute("ProvidedExtensions.RemoteLocation"); //NOI18N
                    final Matcher m = repositoryPattern.matcher(projRepo);
                    if (m.matches()) {
                        kenaiPopup.add(new LazyOpenKenaiProjectAction(m.group(4)));
                    } else {
                        kenaiPopup.setVisible(false);
                    }
                } catch (KenaiException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return kenaiPopup;
        }

        private synchronized boolean isKenaiProject(Project proj) {
            assert proj != null;

            ProjectHandle[] openProjects = DashboardImpl.getInstance().getOpenProjects();
            for (int i = 0; i < openProjects.length; i++) {
                ProjectHandle projectHandle = openProjects[i];
                if (projectHandle == null) {
                    continue;
                }
                List<SourceHandle> sources = SourceAccessorImpl.getDefault().getSources(projectHandle);
                for (SourceHandle sourceHandle : sources) {
                    if (sourceHandle == null) {
                        continue;
                    }
                    for (NbProjectHandle nbProjectHandle : sourceHandle.getRecentProjects()) {
                        if (((NbProjectHandleImpl)nbProjectHandle).getProject().equals(proj)) {
                            kenaiProj = projectHandle;
                            return true;
                        }
                    }
                }
            }

            String projRepo = (String) proj.getProjectDirectory().getAttribute("ProvidedExtensions.RemoteLocation"); //NOI18N
            if (projRepo != null) {
                final Matcher m = repositoryPattern.matcher(projRepo);
                if (m.matches()) {
                    return true;
                }
            }
            return false;
        }

        public void actionPerformed(ActionEvent e) {
        }
    }
}

class LazyFindIssuesAction extends JMenuItem {

    final ProjectHandle kph;

    public LazyFindIssuesAction(ProjectHandle kenaiProj) {
        super(NbBundle.getMessage(KenaiPopupMenu.class, "FIND_ISSUE")); //NOI18N
        kph = kenaiProj;
        this.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                new RequestProcessor("__ISSUETRACKER", 1).post(new Runnable() { //NOI18N

                    public void run() {
                        ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupMenu.class, "CONTACTING_ISSUE_TRACKER"));  //NOI18N
                        handle.start();
                        QueryAccessor.getDefault().getFindIssueAction(kph).actionPerformed(e);
                        handle.finish();
                    }
                });
            }
        });
    }
}

class LazyNewIssuesAction extends JMenuItem {

    final ProjectHandle kph;

    public LazyNewIssuesAction(ProjectHandle kenaiProj) {
        super(NbBundle.getMessage(KenaiPopupMenu.class, "NEW_ISSUE")); //NOI18N
        kph = kenaiProj;
        this.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                new RequestProcessor("__ISSUETRACKER", 1).post(new Runnable() {  //NOI18N

                    public void run() {
                        ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupMenu.class, "CONTACTING_ISSUE_TRACKER")); //NOI18N
                        handle.start();
                        QueryAccessor.getDefault().getCreateIssueAction(kph).actionPerformed(e);
                        handle.finish();
                    }
                });
            }
        });
    }
}

class LazyOpenKenaiProjectAction extends JMenuItem {

    public LazyOpenKenaiProjectAction(final String kenaiProjectUniqueName) {
        super(NbBundle.getMessage(KenaiPopupMenu.class, "OPEN_CORRESPONDING_KENAI_PROJ")); //NOI18N
        this.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        KenaiTopComponent.findInstance().open();
                        KenaiTopComponent.findInstance().requestActive();
                        RequestProcessor.getDefault().post(new Runnable() {

                            public void run() {
                                ProgressHandle handle = null;
                                try {
                                    handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupMenu.class, "CTL_OpenKenaiProjectAction")); //NOI18N
                                    handle.start();
                                    KenaiProject kp = null;
                                    kp = Kenai.getDefault().getProject(kenaiProjectUniqueName);
                                    if (kp != null) {
                                        final ProjectHandleImpl pHandle = new ProjectHandleImpl(kp);
                                        DashboardImpl.getInstance().addProject(pHandle, false);
                                        DashboardImpl.getInstance().selectAndExpand(kp);
                                    }
                                } catch (KenaiException ex) {
                                    Exceptions.printStackTrace(ex);
                                } finally {
                                    if (handle != null) {
                                        handle.finish();
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
    }
}
