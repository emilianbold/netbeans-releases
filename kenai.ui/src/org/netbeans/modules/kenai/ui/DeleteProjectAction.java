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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.kenai.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.api.KenaiServer;
import org.netbeans.modules.team.server.ui.common.DashboardSupport;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jan Becicka
 */
public class DeleteProjectAction extends AbstractAction {

    private ProjectHandle<KenaiProject> project;
    private final DashboardSupport<KenaiProject> dashboard;

    public DeleteProjectAction(ProjectHandle<KenaiProject> project) {
        super(org.openide.util.NbBundle.getMessage(DeleteProjectAction.class, "CTL_DeleteProject"));
        this.project = project;
        dashboard = KenaiServer.getDashboard(project);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (JOptionPane.YES_OPTION
                != JOptionPane.showConfirmDialog(
                WindowManager.getDefault().getMainWindow(),
                NbBundle.getMessage(ProjectAccessorImpl.class, "LBL_ReallyDelete"),
                NbBundle.getMessage(ProjectAccessorImpl.class, "LBL_ReallyDeleteTitle", project.getDisplayName()),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE)
        ) {
            return;
        }
        dashboard.deletingStarted();
        Utilities.getRequestProcessor().post(new Runnable() {

            public void run() {
                try {
                    KenaiProject prj = project.getTeamProject();
                    prj.delete();
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            dashboard.removeProject(project);
                            dashboard.refreshMemberProjects(false);
                        }
                    });
                } catch (KenaiException ex) {
                    final String message = ex.getAsMap().get("message"); // NOI18N
                    if (message != null) {
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                JOptionPane.showMessageDialog(
                                        WindowManager.getDefault().getMainWindow(),
                                        message,
                                        NbBundle.getMessage(ProjectAccessorImpl.class, "LBL_DeleteFailed", project.getDisplayName()),
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    } else {
                        Exceptions.printStackTrace(ex);
                    }
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            dashboard.deletingFinished();
                        }
                    });
                }
            }
        });
    }
}
