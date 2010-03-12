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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.ui.api.NbModuleOwnerSupport;
import org.netbeans.modules.kenai.ui.dashboard.DashboardImpl;
import org.netbeans.modules.kenai.ui.spi.QueryAccessor;
import org.netbeans.modules.kenai.ui.api.NbModuleOwnerSupport.OwnerInfo;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;


public class KenaiPopupMenu extends AbstractAction implements ContextAwareAction {

    private static Map<Project, String> repoForProjCache = new WeakHashMap<Project, String>();

    private static KenaiPopupMenu inst = null;

    private KenaiPopupMenu() {
        putValue(NAME, NbBundle.getMessage(KenaiPopupMenu.class, "KENAI_POPUP"));
    }

    public static synchronized KenaiPopupMenu getDefault() {
        if (inst == null) {
            inst = new KenaiPopupMenu();
        }
        return inst;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new KenaiPopupMenuPresenter(actionContext);
    }

    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    private KenaiProject getActualKenaiProject (Project p, String kenaiProjectName) throws KenaiException {
        KenaiProject defaultKenaiProject = KenaiProject.forRepository(repoForProjCache.get(p));
        Kenai kenai = defaultKenaiProject != null ? defaultKenaiProject.getKenai(): Utilities.getPreferredKenai(); //NOI18N
        KenaiProject kp = kenai == null ? null : kenai.getProject(kenaiProjectName);
        return kp;
    }

    private String getKenaiProjectName(Project proj) {
        /* Add action to navigate to Kenai project - based on repository URL (not on Kenai dashboard at the moment) */
        // if isKenaiProject==true, there must be cached result + it is different from ""
        String kpName = null;
        OwnerInfo ownerInfo = NbModuleOwnerSupport.getInstance().getOwnerInfo(proj);
        if (ownerInfo != null) {
            try {
                // ensure project. If none with the given owner name available,
                // fallback on repoForProjCache
                KenaiProject kp = getActualKenaiProject(proj, ownerInfo.getOwner());
                if (kp != null) {
                    kpName = kp.getName();
                }
            } catch (KenaiException ex) {
                String err = ex.getLocalizedMessage();
                if (err == null) {
                    err = ex.getCause().getLocalizedMessage();
                }
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(KenaiPopupMenu.class, "ERROR_CONNECTION", err))); //NOI18N
            }
        }
        if (kpName == null) {
            String projRepo = repoForProjCache.get(proj);
            kpName = KenaiProject.getNameForRepository(projRepo);
        }
        return kpName;
    }



    private final class KenaiPopupMenuPresenter extends AbstractAction implements Presenter.Popup {

        private final Project proj;

        private KenaiPopupMenuPresenter(Lookup actionContext) {
            proj = actionContext.lookup(Project.class);
        }

        public JMenuItem getPopupPresenter() {
            JMenu kenaiPopup = new JMenu(); //NOI18N
            final Node[] nodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
            kenaiPopup.setVisible(false);
            if (proj == null || !isKenaiProject(proj) || nodes.length > 1) { // hide for non-Kenai projects
                if (repoForProjCache.get(proj) == null && nodes.length == 1) { // start caching request, show dummy item...
                    final JMenu dummy = new JMenu(NbBundle.getMessage(KenaiPopupMenu.class, "LBL_CHECKING")); //NOI18N
                    dummy.setVisible(true);
                    dummy.setEnabled(false);
                    RequestProcessor.getDefault().post(new Runnable() { // cache the results, update the popup menu

                        public void run() {
                            String s = (String) proj.getProjectDirectory().getAttribute("ProvidedExtensions.RemoteLocation"); //NOI18N
                            if (s == null || KenaiProject.getNameForRepository(s) == null) {
                                repoForProjCache.put(proj, ""); //NOI18N null cannot be used - project with no repo is null, "" is to indicate I already checked this one...
                                dummy.setVisible(false);
                            } else {
                                repoForProjCache.put(proj, s);
                                final JMenu tmp = constructKenaiMenu(nodes);
                                final Component[] c = tmp.getMenuComponents();
                                SwingUtilities.invokeLater(new Runnable() {

                                    public void run() {
                                        tmp.revalidate();
                                        dummy.setText(NbBundle.getMessage(KenaiPopupMenu.class, "KENAI_POPUP")); //NOI18N
                                        dummy.setEnabled(true);
                                        for (int i = 0; i < c.length; i++) {
                                            Component item = c[i];
                                            dummy.add(item);
                                        }
                                        dummy.getParent().validate();
                                    }
                                });
                            }
                        }
                    });
                    return dummy;
                }
            } else { // show for Kenai projects
                kenaiPopup = constructKenaiMenu(nodes);
            }
            return kenaiPopup;
        }

        private JMenu constructKenaiMenu(Node[] nodes) {
            // show for Kenai projects
            final JMenu kenaiPopup = new JMenu(NbBundle.getMessage(KenaiPopupMenu.class, "KENAI_POPUP")); //NOI18N
            kenaiPopup.setVisible(true);
            
            kenaiPopup.add(new LazyOpenKenaiProjectAction(proj));
            kenaiPopup.addSeparator();
            String projRepo = repoForProjCache.get(proj);
            String kpName = KenaiProject.getNameForRepository(projRepo);
            if (kpName != null) {
                kenaiPopup.add(new LazyFindIssuesAction(proj));
                kenaiPopup.add(new LazyNewIssuesAction(proj));
                kenaiPopup.addSeparator();
                /* Show actions related to versioning - commit/update */
                VersioningSystem owner = VersioningSupport.getOwner(FileUtil.toFile(proj.getProjectDirectory()));
                JMenu versioning = new JMenu(NbBundle.getMessage(KenaiPopupMenu.class, "MSG_VERSIONING")); //NOI18N
                JComponent[] items = createVersioningSystemItems(owner, nodes);
                for (int i = 0; i < items.length; i++) {
                    JComponent item = items[i];
                    if (item != null) {
                        versioning.add(item);
                    }
                }
                kenaiPopup.add(versioning);
            } else {
                kenaiPopup.setVisible(false);
            }
            return kenaiPopup;
        }

        private JComponent[] createVersioningSystemItems(VersioningSystem owner, Node[] nodes) {
            VCSAnnotator an = owner.getVCSAnnotator();
            if (an == null) return null;
            VCSContext ctx = VCSContext.forNodes(nodes);
            Action [] actions = an.getActions(ctx, VCSAnnotator.ActionDestination.PopupMenu);
            JComponent [] items = new JComponent[actions.length];
            int i = 0;
            for (Action action : actions) {
                if (action != null) {
                    JMenuItem item = createmenuItem(action);
                    items[i++] = item;
                } else {
                    items[i++] = createJSeparator();
                }
            }
            return items;
        }

        public JSeparator createJSeparator() {
            JMenu menu = new JMenu();
            menu.addSeparator();
            return (JSeparator)menu.getPopupMenu().getComponent(0);
        }

        private JMenuItem createmenuItem(Action action) {
            JMenuItem item;
            if (action instanceof SystemAction) {
                final SystemAction sa = (SystemAction) action;
                item = new JMenuItem(new AbstractAction(sa.getName()) {
                    public void actionPerformed(ActionEvent e) {
                        sa.actionPerformed(e);
                    }
                });
            } else {
                item = new JMenuItem(action);
            }
            Mnemonics.setLocalizedText(item, (String) action.getValue(Action.NAME));
            return item;
        }

        boolean isKenaiProject(final Project proj) {
            assert proj != null;
            String projRepo = repoForProjCache.get(proj);
            if (projRepo == null) { // repo is not cached - has to be cached on the background before
                return false;
            }
            if (!projRepo.equals("")) { //NOI18N
                return KenaiProject.getNameForRepository(projRepo) !=null;
            }
            return false;
        }

        public void actionPerformed(ActionEvent e) {
        }

    }

    class LazyFindIssuesAction extends JMenuItem {

        public LazyFindIssuesAction(final Project proj) {
            super(NbBundle.getMessage(KenaiPopupMenu.class, "FIND_ISSUE")); //NOI18N
            this.addActionListener(new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    new RequestProcessor("__ISSUETRACKER", 1).post(new Runnable() { //NOI18N

                        public void run() {
                            ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupMenu.class, "CONTACTING_ISSUE_TRACKER"));  //NOI18N
                            handle.start();
                            try {
                                final KenaiProject kp = getActualKenaiProject(proj, getKenaiProjectName(proj));
                                if (kp != null) {
                                    if (kp.getFeatures(Type.ISSUES).length > 0) {
                                        final ProjectHandleImpl pHandle = new ProjectHandleImpl(kp);
                                        SwingUtilities.invokeLater( new Runnable() {
                                            @Override
                                            public void run() {
                                                DashboardImpl.getInstance().addProject(pHandle, false, true);
                                            }
                                        });
                                        QueryAccessor.getDefault().getFindIssueAction(pHandle).actionPerformed(e);
                                        return;
                                    } else {
                                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(KenaiPopupMenu.class, "ERROR_ISSUETRACKER"))); //NOI18N
                                        return;
                                    }
                                }
                            } catch (KenaiException e) {
                                String err = e.getLocalizedMessage();
                                if (err == null) {
                                    err = e.getCause().getLocalizedMessage();
                                }
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(KenaiPopupMenu.class, "ERROR_CONNECTION", err))); //NOI18N
                            } finally {
                                handle.finish();
                            }
                        }
                    });
                }
            });
        }
    }

    class LazyNewIssuesAction extends JMenuItem {

        public LazyNewIssuesAction(final Project proj) {
            super(NbBundle.getMessage(KenaiPopupMenu.class, "NEW_ISSUE")); //NOI18N
            this.addActionListener(new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    new RequestProcessor("__ISSUETRACKER", 1).post(new Runnable() {  //NOI18N

                        public void run() {
                            ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupMenu.class, "CONTACTING_ISSUE_TRACKER")); //NOI18N
                            handle.start();
                            try {
                                final KenaiProject kp = getActualKenaiProject(proj, getKenaiProjectName(proj));
                                if (kp != null) {
                                    if (kp.getFeatures(Type.ISSUES).length > 0) {
                                        final ProjectHandleImpl pHandle = new ProjectHandleImpl(kp);
                                        SwingUtilities.invokeLater( new Runnable() {
                                            @Override
                                            public void run() {
                                                DashboardImpl.getInstance().addProject(pHandle, false, true);
                                            }
                                        });
                                        QueryAccessor.getDefault().getCreateIssueAction(pHandle).actionPerformed(e);
                                        return;
                                    } else {
                                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(KenaiPopupMenu.class, "ERROR_ISSUETRACKER"))); //NOI18N
                                        return;
                                    }
                                }
                            } catch (KenaiException e) {
                                String err = e.getLocalizedMessage();
                                if (err == null) {
                                    err = e.getCause().getLocalizedMessage();
                                }
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(KenaiPopupMenu.class, "ERROR_CONNECTION", err))); //NOI18N
                            } finally {
                                handle.finish();
                            }
                        }
                    });
                }
            });
        }
    }

    class LazyOpenKenaiProjectAction extends JMenuItem {

        public LazyOpenKenaiProjectAction(final Project proj) {
            super(NbBundle.getMessage(KenaiPopupMenu.class, "OPEN_CORRESPONDING_KENAI_PROJ")); //NOI18N
            this.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            KenaiTopComponent.findInstance().open();
                            KenaiTopComponent.findInstance().requestActive();
                        }
                    });
                    RequestProcessor.getDefault().post(new Runnable() {

                        public void run() {
                            ProgressHandle handle = null;
                            try {
                                handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupMenu.class, "CTL_OpenKenaiProjectAction")); //NOI18N
                                handle.start();
                                final KenaiProject kp = getActualKenaiProject(proj, getKenaiProjectName(proj));
                                if (kp != null) {
                                    final ProjectHandleImpl pHandle = new ProjectHandleImpl(kp);
                                        SwingUtilities.invokeLater( new Runnable() {
                                            @Override
                                            public void run() {
                                                DashboardImpl.getInstance().addProject(pHandle, false, true);
                                            }
                                        });
                                }
                            } catch (KenaiException e) {
                                String err = e.getLocalizedMessage();
                                if (err == null) {
                                    err = e.getCause().getLocalizedMessage();
                                }
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(KenaiPopupMenu.class, "ERROR_CONNECTION", err))); //NOI18N
                            } finally {
                                if (handle != null) {
                                    handle.finish();
                                    return;
                                }
                            }
                        }
                    });
                }
            });
        }
    }

}