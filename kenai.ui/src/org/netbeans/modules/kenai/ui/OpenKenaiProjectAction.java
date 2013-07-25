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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.api.KenaiServer;
import org.netbeans.modules.team.server.ui.common.DashboardSupport;
import org.netbeans.modules.team.server.ui.spi.TeamUIUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(id = OpenKenaiProjectAction.ID, category = "Team")
@ActionRegistration(displayName = "#CTL_OpenKenaiProjectAction")
@NbBundle.Messages("CTL_OpenKenaiProjectAction=Open Project...")
public final class OpenKenaiProjectAction extends AbstractAction {

    static final String ID = "org.netbeans.modules.kenai.ui.OpenKenaiProjectAction"; //NOI18N
    private final String dialogTitle = NbBundle.getMessage(OpenKenaiProjectAction.class, "OpenKenaiProjectWindowTitle");

    private Kenai kenai;

    public OpenKenaiProjectAction(Kenai kenai) {
        super(NbBundle.getMessage(OpenKenaiProjectAction.class, "CTL_OpenKenaiProjectAction"));
        this.kenai = kenai;
    }

    public OpenKenaiProjectAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        final JButton open = new JButton(NbBundle.getMessage(OpenKenaiProjectAction.class, "OpenKenaiProjectAction.OpenFromKenai"));
        open.setDefaultCapable(true);
        open.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OpenKenaiProjectAction.class, "OpenKenaiProjectAction.OpenFromKenai"));

        JButton cancel = new JButton(NbBundle.getMessage(OpenKenaiProjectAction.class, "OpenKenaiProjectAction.Cancel"));
        cancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OpenKenaiProjectAction.class, "OpenKenaiProjectAction.Cancel"));

        KenaiSearchPanel openPanel = new KenaiSearchPanel(KenaiSearchPanel.PanelType.OPEN, true, kenai!=null?kenai:Utilities.getPreferredKenai());
        openPanel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (KenaiDialogDescriptor.PROP_SELECTION_VALID.equals(evt.getPropertyName())) {
                    open.setEnabled((Boolean) evt.getNewValue());
                }
            }
        });

        DialogDescriptor dialogDesc = new KenaiDialogDescriptor(openPanel, dialogTitle, true, null);
        dialogDesc.setOptions(new Object[] { open, cancel });
        dialogDesc.setOptionType(DialogDescriptor.OK_CANCEL_OPTION);

        Object option = DialogDisplayer.getDefault().notify(dialogDesc);

        if (open.equals(option)) {
            KenaiProject selProjects[] = openPanel.getSelectedProjects();
            if (null != selProjects && selProjects.length > 0) {
                ProjectHandleImpl[] impls = new ProjectHandleImpl[selProjects.length];
                for (int i = 0; i < selProjects.length; i++) {
                    KenaiProject prj = selProjects[i];
                    impls[i] = new ProjectHandleImpl(prj);
                }
                DashboardSupport<KenaiProject> dashboard = KenaiServer.getDashboard(impls[0]);
                dashboard.addProjects(impls, false, true);
                TeamUIUtils.activateTeamDashboard();
            }
        }

    }
}
