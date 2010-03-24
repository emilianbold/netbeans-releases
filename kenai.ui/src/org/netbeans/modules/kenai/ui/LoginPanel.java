/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.ui.dashboard.LinkButton;
import org.netbeans.modules.kenai.ui.nodes.AddInstanceAction;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * @author Jan Becicka
 * @author maros
 */
public class LoginPanel extends javax.swing.JPanel {

    private Credentials credentials;

    private URL getForgetPasswordUrl() {
        try {
            if (kenai!=null) {
            return new URL(kenai.getUrl().toString() + "/people/forgot_password"); // NOI18N
            } else {
                return new URL("https://netbeans.org/people/forgot_password"); // NOI18N
            }
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private URL getRegisterUrl() {
        try {
            if (kenai!=null) {
            return new URL(kenai.getUrl().toString() + "/people/signup"); // NOI18N
            } else {
                return new URL("https://netbeans.org/people/signup"); // NOI18N
            }
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private Kenai kenai;

    /** Creates new form LoginPanel */
    public LoginPanel(Kenai kenai, Credentials credentials) {
        this.kenai = kenai;
        this.credentials = credentials;
        initComponents();
//        lblKenaiLogoCenter.setBorder(null);
//        lblKenaiLogoLeft.setBorder(null);
//        lblKenaiLogoRight.setBorder(null);
//        String hostName = kenai.getUrl().getHost();
//        if (!hostName.equals("kenai.com")) {//NOI18N
//            lblKenaiLogoCenter.setVisible(false);
//            lblKenaiLogoRight.setVisible(false);
//            lblKenaiLogoLeft.setText(NbBundle.getMessage(LoginPanel.class, "LBL_LoginTo", kenai.getName(), kenai.getUrl().toString()));
//            lblKenaiLogoLeft.setBorder(new EmptyBorder(10, 12, 0, 10));
//            lblKenaiLogoLeft.setIcon(null);
//        }
        kenaiCombo.setSelectedItem(kenai);
        if (kenai!=null) {
        setUsername(credentials.getUsername(kenai));
        setPassword(credentials.getPassword(kenai));
        setChkOnline();
        } else {
            setChildrenEnabled(false);
        }
    }

    public boolean isStorePassword() {
        return chkRememberMe.isSelected();
    }

    public void showError(KenaiException ex) {
        kenaiCombo.setEnabled(true);
        progressBar.setVisible(false);
        String errorMessage = ex.getMessage();
        if (errorMessage==null || "".equals(errorMessage.trim())) {
            errorMessage = NbBundle.getMessage(LoginPanel.class, "LBL_AuthenticationFailed");
            Logger.getLogger(LoginPanel.class.getName()).log(Level.INFO, errorMessage, ex);
        }
        error.setText(errorMessage);
        Map<String, String> errors = ex.getErrors();
        if (errors!=null) {
            String msg = errors.get("message");//NOI18N
            if (msg!=null) {
                error.setToolTipText(msg);
            }
        }
        error.setVisible(true);
        password.requestFocus();
        setLoginButtonEnabled(true);
    }

    public void showProgress() {
        error.setVisible(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        kenaiCombo.setEnabled(false);
        setLoginButtonEnabled(false);
    }

    private void setChildrenEnabled(boolean enabled) {
        for (Component c:getComponents()) {
            if (c!=kenaiCombo && c!=kenaiLabel) {
                c.setEnabled(enabled);
            }
        }
    }

    public void clearStatus() {
        error.setVisible(false);
        progressBar.setVisible(false);
        setLoginButtonEnabled(true);
        kenaiCombo.setEnabled(true);
    }

    private void setChkOnline() {
        chkIsOnline.setSelected(false);
        chkIsOnline.setEnabled(false);
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                if (kenai==null)
                    return;
                boolean is = Utilities.isChatSupported(kenai);
                if (is) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            final Preferences preferences = NbPreferences.forModule(LoginPanel.class);
                            chkIsOnline.setEnabled(true);
                            chkIsOnline.setSelected(Boolean.parseBoolean(preferences.get(UIUtils.getPrefName(kenai, UIUtils.ONLINE_ON_CHAT_PREF), "true"))); // NOI18N
                        }
                    });
                }
            }
        });
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

        lblUserName = new javax.swing.JLabel();
        username = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        chkRememberMe = new javax.swing.JCheckBox();
        lblNoAccount = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        forgotPassword = new LinkButton(NbBundle.getMessage(LoginPanel.class, "LoginPanel.forgotPassword.text"), new URLDisplayerAction("",getForgetPasswordUrl()));
        signUp = new LinkButton(NbBundle.getMessage(LoginPanel.class, "LoginPanel.register.text"), new URLDisplayerAction("",getRegisterUrl()));
        error = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        chkIsOnline = new javax.swing.JCheckBox();
        kenaiLabel = new javax.swing.JLabel();
        kenaiCombo = new KenaiCombo(Kenai.Status.OFFLINE, true);

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        lblUserName.setLabelFor(username);
        org.openide.awt.Mnemonics.setLocalizedText(lblUserName, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.lblUserName.text")); // NOI18N

        lblPassword.setLabelFor(password);
        org.openide.awt.Mnemonics.setLocalizedText(lblPassword, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.lblPassword.text")); // NOI18N

        chkRememberMe.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(chkRememberMe, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.chkRememberMe.text")); // NOI18N
        chkRememberMe.setToolTipText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.chkRememberMe.toolTipText")); // NOI18N
        chkRememberMe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkRememberMeActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblNoAccount, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.lblNoAccount.text")); // NOI18N

        password.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                passwordFocusGained(evt);
            }
        });

        error.setForeground(java.awt.Color.red);
        error.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/kenai/ui/resources/error.png"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chkIsOnline, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.chkIsOnline.text")); // NOI18N
        chkIsOnline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkIsOnlineActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(kenaiLabel, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.kenaiLabel.text")); // NOI18N

        kenaiCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kenaiComboActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(error)
                    .add(progressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblUserName)
                            .add(lblPassword)
                            .add(kenaiLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(lblNoAccount)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(signUp))
                            .add(chkRememberMe)
                            .add(password, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                            .add(username, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                            .add(forgotPassword)
                            .add(chkIsOnline)
                            .add(kenaiCombo, 0, 292, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(kenaiLabel)
                    .add(kenaiCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblUserName)
                    .add(username, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPassword)
                    .add(password, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chkRememberMe)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chkIsOnline)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(forgotPassword)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblNoAccount)
                    .add(signUp))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(error)
                .add(0, 0, 0)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        lblUserName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.lblUserName.AccessibleContext.accessibleDescription")); // NOI18N
        lblPassword.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.lblPassword.AccessibleContext.accessibleDescription")); // NOI18N
        chkRememberMe.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.chkRememberMe.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    private void passwordFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_passwordFocusGained
        password.setSelectionStart(0);
        password.setSelectionEnd(password.getPassword().length);
    }//GEN-LAST:event_passwordFocusGained

    private void chkRememberMeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkRememberMeActionPerformed
        if (chkRememberMe.isSelected()) {
            ToolTipManager tooltipManager = ToolTipManager.sharedInstance();
            int initialDelay = tooltipManager.getInitialDelay();
            tooltipManager.setInitialDelay(0);
            tooltipManager.mouseMoved(new MouseEvent(chkRememberMe, 0, 0, 0, 0, 0, 0, false));
            tooltipManager.setInitialDelay(initialDelay);
        }
    }//GEN-LAST:event_chkRememberMeActionPerformed

    private void chkIsOnlineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkIsOnlineActionPerformed
        NbPreferences.forModule(LoginPanel.class).put(UIUtils.getPrefName(kenai, UIUtils.ONLINE_ON_CHAT_PREF), Boolean.toString(isOnline()));
        System.out.println(chkIsOnline.isEnabled());
    }//GEN-LAST:event_chkIsOnlineActionPerformed

    private void kenaiComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kenaiComboActionPerformed
        if (kenaiCombo.getSelectedItem() instanceof Kenai) {
            this.kenai = ((Kenai) kenaiCombo.getSelectedItem());
            forgotPassword.setAction(new URLDisplayerAction("", getForgetPasswordUrl()));
            signUp.setAction(new URLDisplayerAction("", getRegisterUrl()));

            forgotPassword.setText(NbBundle.getMessage(LoginPanel.class, "LoginPanel.forgotPassword.text"));
            signUp.setText(NbBundle.getMessage(LoginPanel.class, "LoginPanel.register.text"));
            
            setUsername(credentials.getUsername(kenai));
            setPassword(credentials.getPassword(kenai));
            setChildrenEnabled(true);
            setChkOnline();
        } else if (kenaiCombo.getSelectedItem() instanceof String) {
            final ActionEvent e = evt;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    new AddInstanceAction().actionPerformed(e);
                    LoginPanel.this.kenai = ((Kenai) kenaiCombo.getSelectedItem());
                    if (LoginPanel.this.kenai==null)
                        return;
                    forgotPassword.setAction(new URLDisplayerAction("", getForgetPasswordUrl()));
                    signUp.setAction(new URLDisplayerAction("", getRegisterUrl()));

                    forgotPassword.setText(NbBundle.getMessage(LoginPanel.class, "LoginPanel.forgotPassword.text"));
                    signUp.setText(NbBundle.getMessage(LoginPanel.class, "LoginPanel.register.text"));

                    setChildrenEnabled(true);
                    setChkOnline();
                }
            });
        } 
    }//GEN-LAST:event_kenaiComboActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JCheckBox chkIsOnline;
    javax.swing.JCheckBox chkRememberMe;
    javax.swing.JLabel error;
    javax.swing.JButton forgotPassword;
    javax.swing.JComboBox kenaiCombo;
    javax.swing.JLabel kenaiLabel;
    javax.swing.JLabel lblNoAccount;
    javax.swing.JLabel lblPassword;
    javax.swing.JLabel lblUserName;
    javax.swing.JPasswordField password;
    javax.swing.JProgressBar progressBar;
    javax.swing.JButton signUp;
    javax.swing.JTextField username;
    // End of variables declaration//GEN-END:variables

    public char[] getPassword() {
        return password.getPassword();
    }

    public String getUsername() {
        return username.getText();
    }

    private void setUsername(String uname) {
        username.setText(uname);
        chkRememberMe.setSelected(true);
    }

    private void setPassword(char[] pwd) {
        password.setText(new String(pwd));
    }

    public boolean isOnline() {
        return chkIsOnline.isSelected();
    }

    public Kenai getKenai() {
        return (Kenai) kenaiCombo.getSelectedItem();
    }

    public interface Credentials {
        String getUsername(Kenai kenai);
        char[] getPassword(Kenai kenai);
    }
}
