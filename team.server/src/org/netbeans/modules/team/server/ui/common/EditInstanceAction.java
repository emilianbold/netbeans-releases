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

package org.netbeans.modules.team.server.ui.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.modules.team.server.ui.nodes.TeamServerInstanceCustomizer;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Becicka
 */
public class EditInstanceAction extends AbstractAction {

    @NbBundle.Messages("CTL_Change=Change")
    public static final String CHANGE_BUTTON = Bundle.CTL_Change();
    public static final String CANCEL_BUTTON = Bundle.CTL_Cancel();
    
    private JDialog dialog;
    
    public TeamServer server;
    @NbBundle.Messages("CTL_ChangeInstance=Change Team Server...")
    public EditInstanceAction(TeamServer server) {
        super(Bundle.CTL_ChangeInstance());
        this.server = server;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final JButton changeButton = new JButton(CHANGE_BUTTON);
        changeButton.getAccessibleContext().setAccessibleDescription(CHANGE_BUTTON);
        final TeamServerInstanceCustomizer tsInstanceCustomizer = new TeamServerInstanceCustomizer(changeButton, server.getProvider());
        ActionListener bl = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource().equals(changeButton)) {
                    tsInstanceCustomizer.startProgress();
                    RequestProcessor.getDefault().post(new Runnable() {
                        @Override
                        public void run() {
                            String name = tsInstanceCustomizer.getDisplayName();
                            String url = tsInstanceCustomizer.getUrl();
                            server.setDisplayName(name);
                            try {
                                server.setUrl(url);
                            } catch (MalformedURLException ex) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        tsInstanceCustomizer.showError(Bundle.ERR_TeamServerNotValid());
                                    }
                                });
                                return;
                            }
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    tsInstanceCustomizer.stopProgress();
                                    dialog.setVisible(false);
                                    dialog.dispose();
                                }
                            });                            
                        }   
                    });
                } else {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            }
        };

        DialogDescriptor dd = new DialogDescriptor(
                tsInstanceCustomizer,
                Bundle.CTL_NewTeamServerInstance(),
                true,
                new Object[] {changeButton, CANCEL_BUTTON}, changeButton,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                bl
                );
        tsInstanceCustomizer.setNotificationsSupport(dd.createNotificationLineSupport());
        tsInstanceCustomizer.setDialogDescriptor(dd);
        tsInstanceCustomizer.setDisplayName(server.getDisplayName());
        tsInstanceCustomizer.setUrl(server.getUrl().toString());
                
        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dd);
        dialog.validate();
        dialog.pack();
        dialog.setVisible(true);
    }

}
