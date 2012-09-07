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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.kenai.ui.NewKenaiProjectWizardIterator.CreatedProjectInfo;
import org.netbeans.modules.kenai.ui.api.KenaiServer;
import org.netbeans.modules.subversion.api.Subversion;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamUIUtils;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

public final class ShareAction extends AbstractAction {

    private static ShareAction inst = null;

    private ShareAction() {
    }

    public static synchronized ShareAction getDefault() {
        if (inst == null) {
            inst = new ShareAction();
        }
        return inst;
    }

    private static void showDashboard(Set<CreatedProjectInfo> projects) {
        TeamUIUtils.activateTeamDashboard();
        ProjectHandleImpl project = new ProjectHandleImpl(projects.iterator().next().project);
        KenaiServer.getDashboard(project).selectAndExpand(project);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        Node[] n = WindowManager.getDefault().getRegistry().getActivatedNodes();
        if (n.length > 0) {
            actionPerformed(n);
        } else {
            actionPerformed((Node[]) null);
        }
    }
    
    @NbBundle.Messages({
        "# {0} - project name",
        "NameAndLicenseWizardPanelGUI.versioningNotSupported=Local project \"{0}\" is already shared via versioning system."
    })
    private void actionPerformed (final Node[] nodes) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                if (nodes != null) {
                    for (Node node : nodes) {
                        final Project prj = node.getLookup().lookup(Project.class);
                        if (prj != null) {
                            if (Boolean.TRUE.equals(prj.getProjectDirectory().getAttribute("ProvidedExtensions.VCSManaged"))) { //NOI18N
                                EventQueue.invokeLater(new Runnable() {
                                    @Override
                                    public void run () {
                                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                                                Bundle.NameAndLicenseWizardPanelGUI_versioningNotSupported(ProjectUtils.getInformation(prj).getDisplayName()));
                                    }
                                });
                                return;
                            }
                        }
                    }
                    shareProject(nodes);
                }
            }
        });
    }

    private void shareProject (Node[] e) {
        if (Subversion.isClientAvailable(true)) {
            TeamServer teamServer = TeamUIUtils.getSelectedServer();
            WizardDescriptor wizardDescriptor = new WizardDescriptor(new NewKenaiProjectWizardIterator(e,
                    teamServer instanceof KenaiServer ? ((KenaiServer) teamServer).getKenai() : Utilities.getPreferredKenai()));
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

}

