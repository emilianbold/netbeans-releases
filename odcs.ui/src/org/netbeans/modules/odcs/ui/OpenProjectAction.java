/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.ui.api.ODCSUiServer;
import org.netbeans.modules.odcs.ui.dashboard.ProjectHandleImpl;
import org.netbeans.modules.odcs.ui.project.ODCSSearchPanel;
import org.netbeans.modules.team.ui.spi.TeamUIUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Team", id = "org.netbeans.modules.odcs.ui.OpenProjectAction")
@ActionRegistration(displayName = "#CTL_OpenProjectAction")
@Messages("CTL_OpenProjectAction=&Open Project...")
public final class OpenProjectAction extends AbstractAction {

    private ODCSServer server;

    public OpenProjectAction(ODCSUiServer server) {
        super(Bundle.CTL_OpenProjectAction());
        this.server = server.getServer();
    }

    public OpenProjectAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        org.netbeans.modules.team.ui.spi.TeamUIUtils.activateTeamDashboard();
        if (server == null) {
            server = Utilities.getActiveServer(true);
        }
        if (server == null) {
            return;
        }

        final JButton open = new JButton(NbBundle.getMessage(OpenProjectAction.class, "OpenODCSProjectAction.OpenFromODCS"));
        open.setDefaultCapable(true);
        open.setEnabled(false);
        open.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OpenProjectAction.class, "OpenODCSProjectAction.OpenFromODCS"));

        JButton cancel = new JButton(NbBundle.getMessage(OpenProjectAction.class, "OpenODCSProjectAction.Cancel"));
        cancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OpenProjectAction.class, "OpenODCSProjectAction.Cancel"));

        ODCSSearchPanel openPanel = new ODCSSearchPanel(true, server);
        openPanel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (ODCSDialogDescriptor.PROP_SELECTION_VALID.equals(evt.getPropertyName())) {
                    open.setEnabled((Boolean) evt.getNewValue());
                }
            }
        });

        String dialogTitle = NbBundle.getMessage(OpenProjectAction.class, "OpenODCSProjectWindowTitle");
        DialogDescriptor dialogDesc = new ODCSDialogDescriptor(openPanel, dialogTitle, true, null);
        dialogDesc.setOptions(new Object[]{open, cancel});
        dialogDesc.setOptionType(DialogDescriptor.OK_CANCEL_OPTION);

        Object option = DialogDisplayer.getDefault().notify(dialogDesc);

        if (open.equals(option)) {
            ODCSProject selProjects[] = openPanel.getSelectedProjects();
            if (null != selProjects && selProjects.length > 0) {
                ODCSUiServer uiServer = ODCSUiServer.forServer(server);
                ProjectHandleImpl[] impls = new ProjectHandleImpl[selProjects.length];
                for (int i = 0; i < selProjects.length; i++) {
                    ODCSProject prj = selProjects[i];
                    impls[i] = new ProjectHandleImpl(uiServer, prj);
                }
                uiServer.getDashboard().addProjects(impls, false, true);
                TeamUIUtils.activateTeamDashboard();
            }
        }
    }
}
