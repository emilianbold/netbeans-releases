/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.support;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author gordonp
 */
public class RemoteUserInfo implements UserInfo, UIKeyboardInteractive {
    
    private static Map<String, RemoteUserInfo> map;
    private static final String REMOTE_USER_INFO = "remote.user.info"; // NOI18N
    private String passwd;
    private JTextField passwordField = (JTextField) new JPasswordField(20);
    private final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1,
                         GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0,0,0,0),0,0);
    private Container panel;
    private boolean cancelled = false;
    private final static Object DLGLOCK = new Object();
    private Component parent;
    private final String host;
    private final Encrypter crypter;
    
    private RemoteUserInfo(String host) {
        this.host = host;
        this.crypter = new Encrypter(host);
        String hostKey = encrypt(REMOTE_USER_INFO + host);
        this.passwd = decrypt(NbPreferences.forModule(RemoteUserInfo.class).get(hostKey, null));
        setParentComponent(this);
    }
    /**
     * Get the UserInfo for the remote host.
     * 
     * @param key The host key to look for
     * @param reset Reset password information if true
     * @return The RemoteHostInfo instance for this key
     */
    public static synchronized RemoteUserInfo getUserInfo(String key, boolean retry) {
        if (map == null) {
            map = new HashMap<String, RemoteUserInfo>();
        }
        
        RemoteUserInfo ui = map.get(key);
        if (ui == null) {
            ui = new RemoteUserInfo(key);
            map.put(key, ui);
            retry = false;
        }
        if (retry) {
            ui.cancelled = false;
            ui.reset();
        }
        return ui;
    }

    private String encrypt(String pwd) {
        return crypter.encrypt(pwd);
    }

    private String decrypt(String pwd) {
        return crypter.decrypt(pwd);
    }
    
    private void reset() {
        setPassword(null, false);
        passwordField.setText(""); // clear textfield
        cancelled = false;
    }

    public String getPassword() {
        return passwd;
    }

    /**
     *
     * @param pwd password or null to reset password
     * @param rememberPassword
     */
    public void setPassword(String pwd, boolean rememberPassword) {
        this.passwd = pwd;
        String hostKey = encrypt(REMOTE_USER_INFO + host);
        if (rememberPassword && pwd != null) {
            NbPreferences.forModule(RemoteUserInfo.class).put(hostKey, encrypt(pwd));
        } else {
            NbPreferences.forModule(RemoteUserInfo.class).remove(hostKey);
        }
    }
    
    public synchronized boolean promptYesNo(String str) {
        Object[] options = { "yes", "no" }; // NOI18N
        int foo;
        
        synchronized (DLGLOCK) {
            foo = JOptionPane.showOptionDialog(parent, str,
                NbBundle.getMessage(RemoteUserInfo.class, "TITLE_YN_Warning"), JOptionPane.DEFAULT_OPTION, 
             JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        }
       return foo == 0;
    }

    public String getPassphrase() {
        return null; 
    }

    public boolean promptPassphrase(String message) {
        return true; 
    }

    public synchronized boolean promptPassword(String message) {
        if (!isCancelled()) {
            if (passwd != null) {
                return true;
            } else {
                boolean result;
                PasswordDlg pwdDlg = new PasswordDlg();
                synchronized (DLGLOCK) {
                    result = pwdDlg.askPassword(host);
                }

                if (result) {
                    setPassword(pwdDlg.getPassword(), pwdDlg.isRememberPassword());
                    return true;
                } else {
                    setPassword(null, false);
                    cancelled = true;
                    return false; 
                }
            }
        } else {
            return false;
        }
    }
    
    public boolean isCancelled() {
        return cancelled;
    }

    public void showMessage(String message){
        synchronized (DLGLOCK) {
            JOptionPane.showMessageDialog(parent, message);
        }
    }

    public synchronized String[] promptKeyboardInteractive(String destination, String name,
                          String instruction, String[] prompt, boolean[] echo) {
        if (prompt.length == 1 && !echo[0]) {
            // this is password request
            if (!promptPassword(NbBundle.getMessage(RemoteUserInfo.class, "MSG_PasswordInteractive", destination, prompt[0]))) {
                return null;
            } else {
                return new String[] { getPassword() };
            }
        } else {        
            panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridx = 0;
            panel.add(new JLabel(instruction), gbc);
            gbc.gridy++;

            gbc.gridwidth = GridBagConstraints.RELATIVE;

            JTextField[] texts = new JTextField[prompt.length];
            for (int i = 0; i < prompt.length; i++) {
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridx = 0;
                gbc.weightx = 1;
                panel.add(new JLabel(prompt[i]), gbc);

                gbc.gridx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weighty = 1;
                if(echo[i]){
                    texts[i]=new JTextField(20);
                } else{
                    texts[i]=new JPasswordField(20);
                }
                panel.add(texts[i], gbc);
                gbc.gridy++;
            }

            synchronized (DLGLOCK) {
                if (!isCancelled() && JOptionPane.showConfirmDialog(parent, panel,
                            NbBundle.getMessage(RemoteUserInfo.class, "TITLE_KeyboardInteractive", destination, name),
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
                    String[] response = new String[prompt.length];
                    for (int i = 0; i < prompt.length; i++) {
                        response[i] = texts[i].getText();
                    }
                    return response;
                } else {
                    cancelled = true;
                    return null;  // cancel
                }
            }
        }
    }
    
    private static void setParentComponent(final RemoteUserInfo info) {
        if (SwingUtilities.isEventDispatchThread()) {
            info.parent = WindowManager.getDefault().getMainWindow();
        } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        info.parent = WindowManager.getDefault().getMainWindow();
                    }
                });
        }
    }
}
