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

package org.netbeans.modules.team.ui.common;

import java.beans.PropertyVetoException;
import java.net.MalformedURLException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.modules.team.ui.LoginAction;
import org.netbeans.modules.team.ui.TeamServerManager;
import org.netbeans.modules.team.ui.TeamView;
import org.netbeans.modules.team.ui.nodes.TeamRootNode;
import org.netbeans.modules.team.ui.nodes.TeamServerInstanceCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import static org.netbeans.modules.team.ui.common.Bundle.*;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamServerProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jan Becicka
 */
@Messages("CTL_AddInstance=Add Team Server...")
public class AddInstanceAction extends AbstractAction {

    @Messages("CTL_ADD=Add")
    public static final String ADD_BUTTON = CTL_ADD();
    @Messages("CTL_Cancel=Cancel")
    public static final String CANCEL_BUTTON = CTL_Cancel();
    private static AddInstanceAction instance;
    public static final String ID = "org.netbeans.modules.team.ui.common.AddInstanceAction";

    private TeamServer teamServer;
    private JDialog dialog;
    private boolean expandNewNode = false;
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

    public AddInstanceAction(boolean expandNewNode) {
        this();
        this.expandNewNode = expandNewNode;
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
        final JButton addButton = new JButton(ADD_BUTTON);
        addButton.getAccessibleContext().setAccessibleDescription(ADD_BUTTON);
        final TeamServerInstanceCustomizer tsInstanceCustomizer = new TeamServerInstanceCustomizer(addButton, getProviders());
        ActionListener bl = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource().equals(addButton)) {
                    tsInstanceCustomizer.startProgress();
                    RequestProcessor.getDefault().post(new Runnable() {

                        @Override
                        public void run() {
                            TeamServer teamServer;
                            try {
                                teamServer = tsInstanceCustomizer.getProvider().createTeamServer(tsInstanceCustomizer.getDisplayName(), tsInstanceCustomizer.getUrl());
                                if (teamServer == null) {

                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            tsInstanceCustomizer.showError(ERR_TeamServerNotValid());
                                        }
                                    });
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
                                            if (expandNewNode) {
                                                selectNode(AddInstanceAction.this.teamServer.getUrl().toString());
                                            }
                                        }
                                    });
                                }
                            } catch (MalformedURLException ex) {
                                SwingUtilities.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        tsInstanceCustomizer.showError(ERR_TeamServerNotValid());
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
                new Object[] {addButton, CANCEL_BUTTON}, addButton,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                bl
                );
        tsInstanceCustomizer.setNotificationsSupport(dd.createNotificationLineSupport());
        tsInstanceCustomizer.setDialogDescriptor(dd);

        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dd);
        dialog.validate();
        dialog.pack();
        dialog.setVisible(true);
    }

    private static final Logger LOG = Logger.getLogger(AddInstanceAction.class.getName());

    private static void selectNode(final String... path) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                TopComponent tab = WindowManager.getDefault().findTopComponent("services"); // NOI18N
                if (tab == null) {
                    // XXX have no way to open it, other than by calling ServicesTabAction
                    LOG.fine("No ServicesTab found");
                    return;
                }
                tab.open();
                tab.requestActive();
                if (!(tab instanceof ExplorerManager.Provider)) {
                    LOG.fine("ServicesTab not an ExplorerManager.Provider");
                    return;
                }
                final ExplorerManager mgr = ((ExplorerManager.Provider) tab).getExplorerManager();
                final Node root = mgr.getRootContext();
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        Node hudson = NodeOp.findChild(root, TeamRootNode.TEAM_NODE_NAME);
                        if (hudson == null) {
                            LOG.fine("ServicesTab does not contain " + TeamRootNode.TEAM_NODE_NAME);
                            return;
                        }
                        Node _selected;
                        try {
                            _selected = NodeOp.findPath(hudson, path);
                        } catch (NodeNotFoundException x) {
                            LOG.log(Level.FINE, "Could not find subnode", x);
                            _selected = x.getClosestNode();
                        }
                        final Node selected = _selected;
                        Mutex.EVENT.readAccess(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mgr.setSelectedNodes(new Node[] {selected});
                                } catch (PropertyVetoException x) {
                                    LOG.log(Level.FINE, "Could not select path", x);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public TeamServer getTeamServer() {
        return teamServer;
    }

    private Collection<TeamServerProvider> getProviders () {
        return provider == null ? TeamServerManager.getDefault().getProviders() : Collections.singleton(provider);
    }
}
