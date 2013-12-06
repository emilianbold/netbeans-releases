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
package org.netbeans.modules.nativeexecution.support;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import java.awt.Component;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import javax.swing.JOptionPane;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.support.ui.CertPassphraseDlg;
import org.netbeans.modules.nativeexecution.support.ui.PasswordDlg;
import org.netbeans.modules.nativeexecution.support.ui.PromptPasswordDialog;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

final public class RemoteUserInfo implements UserInfo, UIKeyboardInteractive {

    private final static Object lock = RemoteUserInfo.class.getName() + "Lock"; // NOI18N
    private final static PasswordManager pm = PasswordManager.getInstance();
    private final Component parent;
    private final ExecutionEnvironment env;
    private volatile Component parentWindow = null;
    private final boolean allowInterraction;
    private char[] secret = null;

    public RemoteUserInfo(ExecutionEnvironment env, boolean allowToAskForPassword) {
        this.env = env;
        this.allowInterraction = allowToAskForPassword;
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                parentWindow = WindowManager.getDefault().getMainWindow();
            }
        });
        parent = parentWindow;
    }

    @Override
    public String getPassphrase() {
        return getSecret();
    }

    @Override
    public String getPassword() {
        return getSecret();
    }

    public String getSecret() {
        String result = null;
        synchronized (lock) {
            char[] saved = pm.getPassword(env);
            if (saved != null) {
                result = new String(saved);
            } else if (secret != null) {
                result = new String(secret);
                Arrays.fill(secret, 'x');
                secret = null;
            }
        }
        return result;
    }

    @Override
    public boolean promptPassword(String message) {
        return promptSecret(SecretType.PASSWORD, message);
    }

    @Override
    public boolean promptPassphrase(String message) {
        return promptSecret(SecretType.PASSPHRASE, message);
    }

    private boolean promptSecret(SecretType secretType, String message) {
        synchronized (lock) {
            if (pm.getPassword(env) != null) {
                return true;
            }

            if (!allowInterraction) {
                return false;
            }

            PromptPasswordDialog dlg;
            switch (secretType) {
                case PASSWORD:
                    dlg = new PasswordDlg();
                    break;
                case PASSPHRASE:
                    dlg = new CertPassphraseDlg();
                    break;
                default:
                    throw new InternalError("Wrong secret type"); // NOI18N
            }

            if (!dlg.askPassword(env, message)) {
                throw new CancellationException(loc("USER_AUTH_CANCELED")); // NOI18N
            }

            secret = dlg.getPassword();
            pm.storePassword(env, secret, dlg.isRememberPassword());
            dlg.clearPassword();
        }

        return true;
    }

    @Override
    public boolean promptYesNo(String str) {
        Object[] options = {"yes", "no"}; // NOI18N
        int foo;

        synchronized (lock) {
            if (RemoteUserInfo.isUnitTestMode() || RemoteUserInfo.isStandalone()) {
                System.err.println(str+" yes"); // NOI18N
                foo = 0;
            } else {
                foo = JOptionPane.showOptionDialog(parent, str,
                        loc("TITLE_YN_Warning"), // NOI18N
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            }
        }

        return foo == 0;
    }

    // copy-paste from CndUtils
    private static boolean isStandalone() {
        if ("true".equals(System.getProperty ("cnd.command.line.utility"))) { // NOI18N
            return true;
        }
        return !RemoteUserInfo.class.getClassLoader().getClass().getName().startsWith("org.netbeans."); // NOI18N
    }
    
    // copy-paste from CndUtils
    private static boolean isUnitTestMode() {
        return Boolean.getBoolean("cnd.mode.unittest"); // NOI18N
    }

    @Override
    public void showMessage(String message) {
        synchronized (lock) {
            JOptionPane.showMessageDialog(parent, message);
        }
    }

    @Override
    public String[] promptKeyboardInteractive(String destination,
            String name,
            String instruction,
            String[] prompt,
            boolean[] echo) {

        if (prompt.length == 1 && !echo[0]) {
            // this is a password request
            if (!promptPassword(loc("MSG_PasswordInteractive", // NOI18N
                    destination, prompt[0]))) {
                return null;
            } else {
                return new String[]{getPassword()};
            }
        } else {
            // AK:
            // What else it could ask about?
            // There was a code here that constructed dialog with all prompts
            // based on promt / echo arrays.
            // As I don't know usecases for it, I removed it ;)

            return null;
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(RemoteUserInfo.class, key, params);
    }

    private static enum SecretType {

        PASSWORD,
        PASSPHRASE
    }
}
