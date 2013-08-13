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

package org.netbeans.modules.team.server;

import org.netbeans.modules.team.server.ui.common.OneProjectDashboardPicker;
import org.netbeans.modules.team.server.ui.common.ColorManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import javax.accessibility.AccessibleContext;
import javax.swing.*;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.netbeans.modules.team.ide.spi.TeamDashboardComponentProvider;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.openide.windows.TopComponent;
import static org.netbeans.modules.team.server.Bundle.*;
import org.netbeans.modules.team.server.api.TeamServerManager;
import org.netbeans.modules.team.server.ui.common.AddInstanceAction;
import org.netbeans.modules.team.server.ui.common.DashboardSupport;
import org.netbeans.modules.team.server.ui.common.LinkButton;
import org.netbeans.modules.team.server.ui.common.OneProjectDashboard;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;

public final class TeamView {

    private JPanel dashboardPanel;
    private JScrollPane dashboardScrollPane;
    private JComboBox combo;    
    private JComponent noProjectComponent;
    private TeamServer teamServer;
    private OneProjectDashboardPicker projectPicker;

    private final PropertyChangeListener serverManagerListener;
    
    @Messages("A11Y_TeamProjects=Team Projects")
    private TeamView() {
        serverManagerListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(TeamServerManager.PROP_INSTANCES.equals(evt.getPropertyName())) {
                    switchContent();
                }
            }
        };
        TeamServerManager.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(serverManagerListener, TeamServerManager.getDefault()));                    
        
        teamServer = Utilities.getLastTeamServer();
        if(teamServer == null) {
            teamServer = Utilities.getPreferredServer();
            Utilities.setLastTeamServer(teamServer);
        }
    }

    /**
     * currently visible team server instance
     * @return
     */
    public synchronized TeamServer getTeamServer() {
        return teamServer;
    }

    public synchronized void setSelectedServer(TeamServer server) {
        if (combo!=null) {
            combo.setSelectedItem(server);
        } else {
            setTeamServer(server);
        }
    }
    
    private synchronized void setTeamServer (TeamServer server) {
        Utilities.setLastTeamServer(server);
        teamServer = server;
        switchContent();
    }

    @Messages("LBL_Server=Team Server:")
    private Component getServerSwitcher() {
        if(Utilities.isMoreProjectsDashboard()) {
            return createServerComboPanel();
        } else {
            return getProjectPicker();
        }
    }    

    public static synchronized TeamView getInstance() {
        return Holder.theInstance;
    }

    public synchronized OneProjectDashboardPicker getProjectPicker() {
        if(projectPicker == null) {
            projectPicker = new OneProjectDashboardPicker();
        }
        return projectPicker;
    }

    private Component createServerComboPanel() {
        combo = new TeamServerCombo(true);
        Object k = Utilities.getLastTeamServer();
        if (k!=null) {
            combo.setSelectedItem(k);
        }
        combo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                if (combo.getSelectedItem() instanceof TeamServer) {
                    setTeamServer((TeamServer) combo.getSelectedItem());
                } else if (combo.getSelectedItem() instanceof String) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            new AddInstanceAction().actionPerformed(e);
                            setTeamServer((TeamServer) combo.getSelectedItem());
                        }
                    });
                } else {
                    setTeamServer(null);
                }
            }
        });

        final JPanel panel = new JPanel();
        java.awt.GridBagConstraints gridBagConstraints;

        JLabel serverLabel = new javax.swing.JLabel();
        JSeparator jSeparator = new javax.swing.JSeparator();

        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 0, 3));
        panel.setLayout(new java.awt.GridBagLayout());

        Mnemonics.setLocalizedText(serverLabel, LBL_Server());
        serverLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 3, 3));
        panel.add(serverLabel, new java.awt.GridBagConstraints());

        combo.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 3, 3));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        panel.add(combo, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        panel.add(jSeparator, gridBagConstraints);

        panel.setBackground(dashboardPanel.getBackground());
        combo.setOpaque(false);

        combo.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                panel.setVisible(true);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                panel.setVisible(false);
            }
        });

        return panel;
    }
    
    private static class Holder {
        private static final TeamView theInstance = new TeamView();
    }

    public void close() {
        //TODO OV probably delegate to DashboardComponent
    }

    public synchronized JComponent getComponent() {
        JComponent c = getComponentIntern();
        switchContent();
        return c;
    }
    
    private synchronized JComponent getComponentIntern() {
        if(dashboardPanel == null) {
            dashboardPanel = new JPanel();
            dashboardPanel.setBackground(ColorManager.getDefault().getDefaultBackground());
            dashboardPanel.setLayout(new java.awt.BorderLayout());

            Component serverSwitcher = getServerSwitcher();
            if(serverSwitcher != null) {
                dashboardPanel.add(serverSwitcher, BorderLayout.NORTH);
            }

            dashboardScrollPane = new JScrollPane() {
                @Override
                public void requestFocus() {
                    Component view = getViewport().getView();
                    if (view != null) {
                        view.requestFocus();
                    } else {
                        super.requestFocus();
                    }
                }
                @Override
                public boolean requestFocusInWindow() {
                    Component view = getViewport().getView();
                    return view != null ? view.requestFocusInWindow() : super.requestFocusInWindow();
                }
            };
            dashboardScrollPane.setBorder(BorderFactory.createEmptyBorder());
            dashboardScrollPane.setBackground(ColorManager.getDefault().getDefaultBackground());
            dashboardScrollPane.getViewport().setBackground(ColorManager.getDefault().getDefaultBackground());
            dashboardPanel.add(dashboardScrollPane, BorderLayout.CENTER);

            AccessibleContext accessibleContext = dashboardScrollPane.getAccessibleContext();
            String a11y = A11Y_TeamProjects();
            accessibleContext.setAccessibleName(a11y);
            accessibleContext.setAccessibleDescription(a11y);
        }      
        return dashboardPanel;
    }
    
    private void switchContent() {
        if(getComponentIntern()== null) {
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                JComponent comp;
                boolean noServers = TeamServerManager.getDefault().getTeamServers().isEmpty();
                if(noServers) {
                    comp = createNoProjectComponent(new AddInstanceAction());
                } else if(getProjectPicker().isNoProject()) {
                    comp = createNoProjectComponent(null);
                } else {
                    comp = teamServer.getDashboardComponent();
                }
                if( comp != dashboardScrollPane.getViewport().getView() ) {
                    dashboardScrollPane.setViewportView(comp);
                    if(projectPicker != null) {
                        projectPicker.setVisible(!noServers);
                    }                        
                    dashboardScrollPane.invalidate();
                    dashboardScrollPane.revalidate();
                    dashboardScrollPane.repaint();
                    // hack: ensure the dashboard component has focus (when
                    // added to already visible and activated TopComponent)
                    TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, dashboardScrollPane);
                    if (tc != null && TopComponent.getRegistry().getActivated() == tc) {
                        comp.requestFocus();
                    }
                }
            }
        };
        if( SwingUtilities.isEventDispatchThread() ) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    public JComponent createNoProjectComponent(Action addInstanceAction) {
        if(noProjectComponent == null) {
            Collection<? extends TeamDashboardComponentProvider> c = Lookup.getDefault().lookupAll(TeamDashboardComponentProvider.class);
            TeamDashboardComponentProvider providerImpl = c != null && c.size() > 0 ? c.iterator().next() : null;
            if(providerImpl != null) {
                return providerImpl.createNoProjectComponent(addInstanceAction);
            } else if(Boolean.getBoolean("team.dashboard.useDummyComponentProvider")) { // NOI18N
                // dummy impl
                JPanel res = new JPanel();

                res.setLayout(new GridBagLayout());
                res.setOpaque(false);

                JLabel lbl = new TreeLabel("No Team Project Open"); //NOI18N
                lbl.setForeground(ColorManager.getDefault().getDisabledColor());
                lbl.setHorizontalAlignment(JLabel.CENTER);

                if(addInstanceAction != null) {
                    JButton connect = new JButton(addInstanceAction);
                    connect.setText("Connect"); //NOI18N
                    res.add( connect, new GridBagConstraints(0, 0, 3, 4, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0) );
                }

                res.add( new JLabel(), new GridBagConstraints(0, 5, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
                res.add( lbl, new GridBagConstraints(0, 6, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0) );
                res.add( new JLabel(), new GridBagConstraints(0, 7, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );

                return res;
            } else {    
                return new NoProjectComponent(addInstanceAction);
            }
        }
        return noProjectComponent;
    }
    
    @NbBundle.Messages({"LBL_No_Team_Project_Open=No Team Project Open", 
                    "LBL_WhatIsTeam=What is Team Server?", 
                    "LBL_Connect=Connect"})
    private class NoProjectComponent extends JPanel {

        public NoProjectComponent (Action addInstanceAction) {
            setLayout(new GridBagLayout());
            setOpaque(false);

            JLabel lbl = new TreeLabel(Bundle.LBL_No_Team_Project_Open()); //NOI18N
            lbl.setForeground(ColorManager.getDefault().getDisabledColor());
            lbl.setHorizontalAlignment(JLabel.CENTER);
            LinkButton btnWhatIs = new LinkButton(Bundle.LBL_WhatIsTeam(), createWhatIsTeamAction() ); //NOI18N

            if(addInstanceAction != null && !Utilities.isMoreProjectsDashboard()) {
                JButton connect = new JButton(addInstanceAction);
                connect.setText(Bundle.LBL_Connect());
                add( connect, new GridBagConstraints(0, 0, 3, 4, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0) );
            }

            add( new JLabel(), new GridBagConstraints(0, 5, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
            add( lbl, new GridBagConstraints(0, 6, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0) );
            add( btnWhatIs, new GridBagConstraints(0, 7, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 0, 0, 0), 0, 0) );
            add( new JLabel(), new GridBagConstraints(0, 8, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
        }

        private Action createWhatIsTeamAction() {
            return new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        HtmlBrowser.URLDisplayer.getDefault().showURL(
                                new URL(NbBundle.getMessage(DashboardSupport.class, "URL_TeamOverview"))); //NOI18N
                    } catch( MalformedURLException ex ) {
                        //shouldn't happen
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
        }
    }
    
}
