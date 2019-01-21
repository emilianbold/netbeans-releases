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

import java.awt.EventQueue;
import java.net.MalformedURLException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.team.server.TeamServerInstanceCustomizer;
import org.netbeans.modules.team.server.TeamView;
import org.netbeans.modules.team.server.api.TeamServerManager;
import org.netbeans.modules.team.server.api.TeamUIUtils;
import static org.netbeans.modules.team.server.ui.common.Bundle.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.netbeans.modules.team.server.ui.spi.TeamServerProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

/**
 *
 * 
 */
@Messages("CTL_AddInstance=Add Team Server...")
public class AddInstanceAction extends AbstractAction {

    private static AddInstanceAction instance;
    public static final String ID = "org.netbeans.modules.team.server.ui.common.AddInstanceAction";

    private TeamServer teamServer;
    private JDialog dialog;
    private TeamServerProvider provider;

    public AddInstanceAction() {
        super(CTL_AddInstance());
    }

    public AddInstanceAction (TeamServerProvider teamProvider) {
        this(teamProvider, CTL_AddInstance());
    }

    public AddInstanceAction(TeamServerProvider teamProvider, String actionName) {
        super(actionName);
        this.provider = teamProvider;
    }

    @ActionID(id = ID, category = "Team")
    @ActionRegistration(lazy = false, displayName = "#CTL_AddInstance")
    public static synchronized AddInstanceAction getDefault() {
        if (instance==null) {
            instance=new AddInstanceAction();
            TeamServerManager.getDefault();
        }
        return instance;
    }
    
    @Override
    @Messages({"ERR_TeamServerNotValid=Provide a valid Team Server URL.", "CTL_NewTeamServerInstance=New Team Server"})
    public void actionPerformed(final ActionEvent ae) {
        final TeamServerInstanceCustomizer tsInstanceCustomizer = new TeamServerInstanceCustomizer(getProviders());
        ActionListener bl = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource().equals(DialogDescriptor.OK_OPTION)) {
                    try {
                        // check url
                        new URL(tsInstanceCustomizer.getUrl());
                    } catch (MalformedURLException ex) {
                        teamServerNotValid(tsInstanceCustomizer);
                        return;
                    }

                    tsInstanceCustomizer.startProgress();
                    RequestProcessor.getDefault().post(new Runnable() {
                        @Override
                        public void run() {
                            TeamServer teamServer = null;
                            try {
                                teamServer = tsInstanceCustomizer.getProvider().createTeamServer(tsInstanceCustomizer.getDisplayName(), tsInstanceCustomizer.getUrl());
                            } catch (MalformedURLException ex) {
                                // should not happen
                                Exceptions.printStackTrace(ex);
                            }
                            if (teamServer == null) {
                                teamServerNotValid(tsInstanceCustomizer);
                            } else {
                                AddInstanceAction.this.teamServer = teamServer;
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        tsInstanceCustomizer.stopProgress();
                                        dialog.setVisible(false);
                                        dialog.dispose();
                                        if (ae != null && ae.getSource() instanceof JComboBox) {
                                            ((JComboBox) ae.getSource()).setSelectedItem(AddInstanceAction.this.teamServer);
                                        } else {
                                            TeamView.getInstance().setSelectedServer(AddInstanceAction.this.teamServer);
                                        }
                                        TeamUIUtils.activateTeamDashboard();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    dialog.setVisible(false);
                    dialog.dispose();
                    if (ae != null && ae.getSource() instanceof JComboBox) {
                        JComboBox combo = (JComboBox) ae.getSource();
                        if (combo.getModel().getElementAt(0) instanceof TeamServer) {
                            combo.setSelectedIndex(0);
                        } else {
                            combo.setSelectedItem(null);
                        }
                    } 
                }
            }
        };

        DialogDescriptor dd = new DialogDescriptor(
                tsInstanceCustomizer,
                CTL_NewTeamServerInstance(),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(ID),
                bl
                );
        tsInstanceCustomizer.setNotificationsSupport(dd.createNotificationLineSupport());
        tsInstanceCustomizer.setDialogDescriptor(dd);
        dd.setValid(false); // initially disabled OK button

        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dd);
        dialog.validate();
        dialog.pack();
        dialog.setVisible(true);
    }

    private void teamServerNotValid(final TeamServerInstanceCustomizer tsInstanceCustomizer) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                tsInstanceCustomizer.showError(ERR_TeamServerNotValid());
            }
        };
        if(EventQueue.isDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);            
        }
    }
                            
    public TeamServer getTeamServer() {
        return teamServer;
    }

    private Collection<TeamServerProvider> getProviders () {
        return provider == null ? TeamServerManager.getDefault().getProviders() : Collections.singleton(provider);
    }
}
