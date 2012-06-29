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

package org.netbeans.modules.team.ui;

import org.netbeans.modules.team.ui.common.ColorManager;
import org.netbeans.modules.team.ui.common.LinkButton;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.accessibility.AccessibleContext;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.*;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.treelist.TreeLabel;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import static org.netbeans.modules.team.ui.Bundle.*;
import org.openide.util.NbBundle.Messages;

public final class Dashboard {

    private final JScrollPane dashboardComponent;
    private JComponent emptyComponent;
    private DummyUIComponent dummyUIComponent;
    private TeamServer teamServer;

    @Messages("A11Y_TeamProjects=Team Projects")
    private Dashboard() {
        dummyUIComponent = new DummyUIComponent();
        dashboardComponent = new JScrollPane() {
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
        dashboardComponent.setBorder(BorderFactory.createEmptyBorder());
        dashboardComponent.setBackground(ColorManager.getDefault().getDefaultBackground());
        dashboardComponent.getViewport().setBackground(ColorManager.getDefault().getDefaultBackground());

        AccessibleContext accessibleContext = dashboardComponent.getAccessibleContext();
        String a11y = A11Y_TeamProjects();
        accessibleContext.setAccessibleName(a11y);
        accessibleContext.setAccessibleDescription(a11y);
        teamServer = Utilities.getLastTeamServer();
        setTeamServer(teamServer == null ? Utilities.getPreferredServer() : teamServer);
    }

    /**
     * currently visible kenai instance
     * @return
     */
    public TeamServer getTeamServer() {
        return teamServer;
    }

    public static Dashboard getInstance() {
        return Holder.theInstance;
    }

    void setTeamServer (TeamServer server) {
        Utilities.setLastTeamServer(server);
        teamServer = server;
        switchContent();
    }

    private static class Holder {
        private static final Dashboard theInstance = new Dashboard();
    }

    public void close() {
        //TODO OV probably delegate to DashboardComponent
    }

    public JComponent getComponent() {
        switchContent();
        return dashboardComponent;
    }

    private void switchContent() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                JComponent comp = teamServer == null ? emptyComponent = dummyUIComponent.getJComponent() : teamServer.getDashboardComponent();
                boolean isEmpty = teamServer == null || comp == null;
                if( isEmpty ) {
                    if( dashboardComponent.getViewport().getView() == null 
                            || dashboardComponent.getViewport().getView() != emptyComponent ) {
                        dashboardComponent.setViewportView(emptyComponent = dummyUIComponent.getJComponent());
                        dashboardComponent.invalidate();
                        dashboardComponent.revalidate();
                        dashboardComponent.repaint();
                    }
                } else {
                    if( comp != dashboardComponent.getViewport().getView() ) {
                        dashboardComponent.setViewportView(comp);
                        dashboardComponent.invalidate();
                        dashboardComponent.revalidate();
                        dashboardComponent.repaint();
                        // hack: ensure the dashboard component has focus (when
                        // added to already visible and activated TopComponent)
                        TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, dashboardComponent);
                        if (tc != null && TopComponent.getRegistry().getActivated() == tc) {
                            comp.requestFocus();
                        }
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
    
    private static class DummyUIComponent {
        private JPanel res;

        @Messages({"LBL_No_Team_Project_Open=No Team Project Open", "LBL_WhatIsTeam=What is Team Server?"})
        public JComponent getJComponent () {
            if (res == null) {
                res = new JPanel( new GridBagLayout() );
                res.setOpaque(false);

                JLabel lbl = new TreeLabel(LBL_No_Team_Project_Open()); //NOI18N
                lbl.setForeground(ColorManager.getDefault().getDisabledColor());
                lbl.setHorizontalAlignment(JLabel.CENTER);
                LinkButton btnWhatIs = new LinkButton(LBL_WhatIsTeam(), createWhatIsTeamServerAction() ); //NOI18N

                res.add( new JLabel(), new GridBagConstraints(0, 1, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
                res.add( lbl, new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0) );
                res.add( btnWhatIs, new GridBagConstraints(0, 3, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 0, 0, 0), 0, 0) );
                res.add( new JLabel(), new GridBagConstraints(0, 4, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
                return res;
            }
            return res;
        }

        private Action createWhatIsTeamServerAction() {
            return new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        URLDisplayer.getDefault().showURL(
                                new URL("http://netbeans.org/kb/docs/ide/team-servers.html")); //NOI18N
                    } catch( MalformedURLException ex ) {
                        //shouldn't happen
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
        }

    }
}
