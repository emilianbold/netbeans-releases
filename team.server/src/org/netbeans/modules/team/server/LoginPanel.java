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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.team.server.ui.common.AddInstanceAction;
import org.netbeans.modules.team.server.ui.spi.LoginPanelSupport;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.netbeans.modules.team.server.ui.spi.TeamServerProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * @author Jan Becicka
 * @author maros
 */
public class LoginPanel extends javax.swing.JPanel implements org.netbeans.modules.team.server.ui.spi.LoginPanelSupport.LoginPanelCallback {

    private final TeamServerProvider listedProvider;
    private TeamServer server;
    private LoginPanelSupport loginSupport;
    private Map<TeamServer, LoginPanelSupport> cached = new HashMap<TeamServer, LoginPanelSupport>();

    private static final Map<String, LoginCallable> map = new HashMap<>();
        
    /** Creates new form LoginPanel */
    private LoginPanel(TeamServer preselectedServer, TeamServerProvider listedProvider) {
        this.server = preselectedServer;
        this.listedProvider = listedProvider;
        initComponents();
        teamCombo.setSelectedItem(preselectedServer);
        if (preselectedServer != null) {
            updateDetails(preselectedServer);
        }
    }

    @Override
    public void showError (String errorMessage, String tooltipText) {
        teamCombo.setEnabled(true);
        progressBar.setVisible(false);
        error.setToolTipText(tooltipText);
        error.setText(errorMessage);
        error.setVisible(true);
        setLoginButtonEnabled(true);
    }

    public void showProgress() {
        error.setVisible(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        teamCombo.setEnabled(false);
        setLoginButtonEnabled(false);
    }

    public void clearStatus() {
        error.setVisible(false);
        progressBar.setVisible(false);
        setLoginButtonEnabled(true);
        teamCombo.setEnabled(true);
    }

    @Override
    public void successful () {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JRootPane rootPane = getRootPane();
                if (rootPane != null) {
                    JDialog parent = (JDialog) rootPane.getParent();
                    if (parent != null) {
                        parent.setVisible(false);
                        parent.dispose();
                    }
                }
            }
        });
    }

    public LoginPanelSupport getLoginSupport () {
        return loginSupport;
    }

    private void setLoginButtonEnabled(boolean enabled) {
        try {
            ((Container) getParent().getComponents()[1]).getComponents()[0].setEnabled(enabled);
        } catch (Exception e) {
            //ignore
        }
    } 

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        error = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        teamServerLabel = new javax.swing.JLabel();
        teamCombo = listedProvider == null ? new org.netbeans.modules.team.server.TeamServerCombo(true) : new org.netbeans.modules.team.server.TeamServerCombo(listedProvider, true);
        loginDetails = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        error.setForeground(java.awt.Color.red);
        error.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/team/server/resources/error.png"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(teamServerLabel, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.teamServerLabel.text")); // NOI18N

        teamCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                teamComboActionPerformed(evt);
            }
        });

        loginDetails.setLayout(new javax.swing.BoxLayout(loginDetails, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(error)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(teamServerLabel)
                        .addGap(86, 86, 86)
                        .addComponent(teamCombo, 0, 297, Short.MAX_VALUE))
                    .addComponent(loginDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(teamServerLabel)
                    .addComponent(teamCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loginDetails, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(error)
                .addGap(0, 0, 0)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    private void teamComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_teamComboActionPerformed
        if (teamCombo.getSelectedItem() instanceof TeamServer) {
            this.server = ((TeamServer) teamCombo.getSelectedItem());
            updateDetails(server);
        } else if (teamCombo.getSelectedItem() instanceof String) {
            final ActionEvent e = evt;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    AddInstanceAction a = new AddInstanceAction(listedProvider);
                    a.actionPerformed(e);
                    server = a.getTeamServer();
                    if (server != null) {
                        teamCombo.setSelectedItem(server);
                        updateDetails(server);
                    }
                }
            });
        } 
    }//GEN-LAST:event_teamComboActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JLabel error;
    javax.swing.JPanel loginDetails;
    javax.swing.JProgressBar progressBar;
    javax.swing.JComboBox teamCombo;
    javax.swing.JLabel teamServerLabel;
    // End of variables declaration//GEN-END:variables

    public TeamServer getTeamServer() {
        Object item = teamCombo.getSelectedItem();
        return item instanceof TeamServer? (TeamServer) item : null;
    }

    private void updateDetails (TeamServer server) {
        clearStatus();
        loginSupport = getLoginSupport(server);
        loginDetails.removeAll();
        loginDetails.setLayout(new BorderLayout(0, 0));
        loginDetails.add(loginSupport.getLoginPanelComponent(), BorderLayout.CENTER);
        invalidate();
        revalidate();
        repaint();
    }

    private LoginPanelSupport getLoginSupport (TeamServer server) {
        LoginPanelSupport supp = cached.get(server);
        if (supp == null) {
            supp = server.createLoginSupport();
            cached.put(server, supp);
        }
        return supp;
    }
    
    public static TeamServer login(TeamServer preselectedServer, boolean listAllProviders) {
        LoginCallable login;
        synchronized ( map ) {
            String key = loginKey(preselectedServer, listAllProviders);
            login = map.get(key);
            if(login == null) {
                login = new LoginCallable(preselectedServer, listAllProviders);
                map.put(key, login);
            } 
        }
        return login.call();
    }

    private static String loginKey(TeamServer server, boolean listAllProviders) {
        return (server != null ? server.getUrl().toString() : "") + "#" + listAllProviders; // NOI18N
    }
    
    private static class LoginCallable implements Callable<TeamServer> {
        private final TeamServer preselectedServer;
        private final boolean listAllProviders;
        private TeamServer res;

        public LoginCallable(TeamServer preselectedServer, boolean listAllProviders) {
            this.preselectedServer = preselectedServer;
            this.listAllProviders = listAllProviders;
        }
        
        private TeamServer showLogin (final TeamServer preselectedServer, boolean listAllProviders) {
            final LoginPanel loginPanel = new LoginPanel(preselectedServer, listAllProviders || preselectedServer == null
                    ? null 
                    : preselectedServer.getProvider());
            final String ctlLogin = NbBundle.getMessage(Utilities.class, "CTL_Login");
            final String ctlCancel = NbBundle.getMessage(Utilities.class, "CTL_Cancel");
            DialogDescriptor login = new DialogDescriptor(
                    loginPanel,
                    NbBundle.getMessage(Utilities.class, "CTL_LoginToTeam"),
                    true,
                    new Object[]{ctlLogin,ctlCancel},ctlLogin,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            if (event.getSource().equals(ctlLogin)) {
                                loginPanel.showProgress();
                                loginPanel.getLoginSupport().startLogin(loginPanel);
                            } else {
                                loginPanel.putClientProperty("cancel", "true"); // NOI18N
                                JDialog parent = (JDialog) loginPanel.getRootPane().getParent();
                                parent.setVisible(false);
                                parent.dispose();
                            }
                        }
            });
            login.setClosingOptions(new Object[]{ctlCancel});
            Dialog d = DialogDisplayer.getDefault().createDialog(login);

            d.pack();
            d.setResizable(true);
            loginPanel.clearStatus();
            d.setVisible(true);

            if (loginPanel.getClientProperty("cancel")==null) {  // NOI18N
                return loginPanel.getTeamServer();
            }
            return null;
        }

        private boolean alreadyCalled = false;
        
        @Override
        public synchronized TeamServer call() {
            if(!alreadyCalled) {
                alreadyCalled = true;
                res = showLogin(preselectedServer, listAllProviders);
                synchronized ( map ) {
                    map.remove(loginKey(preselectedServer, listAllProviders));
                }                
            } 
            return res;
        }
    }    
}
