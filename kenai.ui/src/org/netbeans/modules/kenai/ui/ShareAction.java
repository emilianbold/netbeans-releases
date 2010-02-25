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
import java.io.File;
import java.text.MessageFormat;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.NewKenaiProjectWizardIterator.CreatedProjectInfo;
import org.netbeans.modules.kenai.ui.dashboard.DashboardImpl;
import org.netbeans.modules.subversion.api.Subversion;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.windows.WindowManager;

public final class ShareAction extends AbstractAction implements ContextAwareAction {

    private static ShareAction inst = null;

    private ShareAction() {
    }

    public static synchronized ShareAction getDefault() {
        if (inst == null) {
            inst = new ShareAction();
        }
        return inst;
    }

    public static void actionPerformed(Node[] e) {
        ContextShareAction.actionPerformed(e);
    }

    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextShareAction();
    }

    static class ContextShareAction extends AbstractAction implements Presenter.Popup {

        public ContextShareAction() {
            putValue(NAME, NbBundle.getMessage(ShareAction.class, "CTL_ShareAction"));
        }

        public void actionPerformed(ActionEvent e) {
            Node[] n = WindowManager.getDefault().getRegistry().getActivatedNodes();
            if (n.length > 0) {
                ContextShareAction.actionPerformed(n);
            } else {
                ContextShareAction.actionPerformed((Node[]) null);
            }
        }

        public static void actionPerformed(Node[] e) {
            if (e != null) {
                for (Node node : e) {
                    Project prj = node.getLookup().lookup(Project.class);
                    if (prj != null) {
                        File file = FileUtil.toFile(prj.getProjectDirectory());
                        if (VersioningSupport.getOwner(file) != null) {
                            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                                    NbBundle.getMessage(ShareAction.class, "NameAndLicenseWizardPanelGUI.versioningNotSupported", ProjectUtils.getInformation(prj).getDisplayName()));
                            return;
                        }
                    }
                }
            }
            if (Subversion.isClientAvailable(true)) {

                WizardDescriptor wizardDescriptor = new WizardDescriptor(new NewKenaiProjectWizardIterator(e,KenaiManager.getDefault().getKenai("https://kenai.com")));
                // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
                wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
                wizardDescriptor.setTitle(NbBundle.getMessage(NewKenaiProjectAction.class,
                        "NewKenaiProjectAction.dialogTitle")); // NOI18N

                DialogDisplayer.getDefault().notify(wizardDescriptor);

                boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
                if (!cancelled) {
                    Set<CreatedProjectInfo> createdProjects = wizardDescriptor.getInstantiatedObjects();
                    showDashboard(createdProjects);
                }
            }
        }

        public static void showDashboard(Set<CreatedProjectInfo> projects) {
            final KenaiTopComponent kenaiTc = KenaiTopComponent.findInstance();
            kenaiTc.open();
            kenaiTc.requestActive();
            DashboardImpl.getInstance().selectAndExpand(projects.iterator().next().project);

        }

        @Override
        public JMenuItem getPopupPresenter() {
            final JMenuItem i = new JMenuItem(this);
            i.setVisible(true);

            Node[] n = WindowManager.getDefault().getRegistry().getActivatedNodes();
            if (n.length > 0) {
                if (n.length == 1) {
                    final Project prj = n[0].getLookup().lookup(Project.class);
                    if (prj == null) {
                        Logger.getLogger(ShareAction.class.getName()).fine("ShareAction: cannot find project for node " + n[0].getDisplayName());
                    } else {
                        RequestProcessor.getDefault().post(new Runnable() {

                            @Override
                            public void run() {
                                String remoteLocation = (String) prj.getProjectDirectory().getAttribute("ProvidedExtensions.RemoteLocation"); // NOI18N)
                                if (KenaiProject.getNameForRepository(remoteLocation) != null) {
                                    SwingUtilities.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            i.setVisible(false);
                                            ((JPopupMenu) i.getParent()).pack();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
            return i;
        }
    }
}

