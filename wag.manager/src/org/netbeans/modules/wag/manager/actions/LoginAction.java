/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.wag.manager.actions;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.wag.manager.wizards.LoginPanel;
import org.netbeans.modules.wag.manager.zembly.ZemblySession;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.openide.util.*;

/**
 * 
 * @author  peterliu
 */
public class LoginAction extends NodeAction {

    protected boolean enable(org.openide.nodes.Node[] nodes) {
        return true;
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return new HelpCtx(LoginAction.class);
    }

    public String getName() {
        if (ZemblySession.getInstance().isLoggedIn()) {
            return NbBundle.getMessage(LoginAction.class, "LogoutAction");
        } else {
            return NbBundle.getMessage(LoginAction.class, "LoginAction");
        }
    }

    protected void performAction(final Node[] nodes) {
        if (nodes == null) {
            return;
        }

        if (!ZemblySession.getInstance().isLoggedIn()) {
            showLogin();
        }
    }

    protected boolean asynchronous() {
        return false;
    }

    protected String iconResource() {
        return "org/netbeans/modules/wag/manager/resources/restservice.png"; // NOI18N
    }

    private void showLogin() {
        final LoginPanel loginPanel = new LoginPanel();
        final Preferences preferences = NbPreferences.forModule(LoginPanel.class);
        final String ctlLogin = NbBundle.getMessage(LoginAction.class, "CTL_Login");
        final String ctlCancel = NbBundle.getMessage(LoginAction.class, "CTL_Cancel");
        DialogDescriptor login = new DialogDescriptor(
                loginPanel,
                NbBundle.getMessage(LoginAction.class, "CTL_LoginToZembly"),
                true,
                new Object[]{ctlLogin, ctlCancel}, ctlLogin,
                DialogDescriptor.DEFAULT_ALIGN,
                null, new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (event.getSource().equals(ctlLogin)) {
                    loginPanel.showProgress();
                    RequestProcessor.getDefault().post(new Runnable() {

                        public void run() {
                            try {
                                //KenaiConnection.getDefault();
                                //Kenai.getDefault().login(loginPanel.getUsername(), loginPanel.getPassword());
                                ZemblySession.getInstance().login(loginPanel.getUsername(), loginPanel.getPassword());
                                SwingUtilities.invokeLater(new Runnable() {

                                    public void run() {
                                        JRootPane rootPane = loginPanel.getRootPane();
                                        if (rootPane != null) {
                                            Container parent = rootPane.getParent();
                                            if (parent != null) {
                                                parent.setVisible(false);
                                            }
                                        }
                                    }
                                });
                            } catch (final Exception ex) {
                                SwingUtilities.invokeLater(new Runnable() {

                                    public void run() {
                                        //loginPanel.showError(ex);
                                    }
                                });
                            }
                        }
                    });
                /*
                if (loginPanel.isStorePassword()) {
                preferences.put(KENAI_USERNAME_PREF, loginPanel.getUsername()); // NOI18N
                preferences.put(KENAI_PASSWORD_PREF, Scrambler.getInstance().scramble(new String(loginPanel.getPassword()))); // NOI18N
                } else {
                preferences.remove(KENAI_USERNAME_PREF); // NOI18N
                preferences.remove(KENAI_PASSWORD_PREF); // NOI18N
                }
                 */
                } else {
                    loginPanel.putClientProperty("cancel", "true"); // NOI18N
                    loginPanel.getRootPane().getParent().setVisible(false);
                }
            }
        });
        login.setClosingOptions(new Object[]{ctlCancel});
        Dialog d = DialogDisplayer.getDefault().createDialog(login);

        /*
        String uname=preferences.get(KENAI_USERNAME_PREF, null); // NOI18N
        String password=preferences.get(KENAI_PASSWORD_PREF, null); // NOI18N
        if (uname!=null && password!=null) {
        loginPanel.setUsername(uname);
        loginPanel.setPassword(Scrambler.getInstance().descramble(password).toCharArray());
        }
         */
        d.pack();
        d.setResizable(false);
        loginPanel.clearStatus();
        d.setVisible(true);

    //return loginPanel.getClientProperty("cancel")==null; // NOI18N
    }
}
