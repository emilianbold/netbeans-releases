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
package org.netbeans.modules.nativeexecution.support;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import java.awt.Component;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import javax.swing.JOptionPane;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.support.ui.PasswordDlg;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

final class RemoteUserInfo implements UserInfo, UIKeyboardInteractive {

    private final static PasswordManager pm = PasswordManager.getInstance();
    private final ExecutionEnvironment env;

    public RemoteUserInfo(ExecutionEnvironment env) {
        this.env = env;
    }

    @Override
    public String getPassphrase() {
        return null;
    }

    @Override
    public String getPassword() {
        char[] clearPassword = pm.get(env);
        return clearPassword == null ? "" : String.valueOf(clearPassword); // NOI18N
    }

    @Override
    public boolean promptPassword(String arg0) {
        return true;
    }

    @Override
    public boolean promptPassphrase(String arg0) {
        return true;
    }

    @Override
    public boolean promptYesNo(String str) {
        return true;
    }

    @Override
    public void showMessage(String message) {
    }

    @Override
    public String[] promptKeyboardInteractive(String destination,
            String name,
            String instruction,
            String[] prompt,
            boolean[] echo) {
        if (prompt.length == 1 && !echo[0]) {
            // this is a password request
            char[] chars = pm.get(env);
            return chars == null ? null : new String[]{new String(pm.get(env))};
        } else {
            return null;
        }
    }

    static final class Interractive implements UserInfo, UIKeyboardInteractive {

        private final static Object lock = RemoteUserInfo.Interractive.class.getName() + "Lock"; // NOI18N
        private final static PasswordManager pm = PasswordManager.getInstance();
        private final Component parent;
        private final ExecutionEnvironment env;
        private volatile Component parentWindow = null;

        public Interractive(ExecutionEnvironment env) {
            this.env = env;
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
            return null;
        }

        @Override
        public String getPassword() {
            char[] clearPassword = pm.get(env);
            return clearPassword == null ? "" : String.valueOf(clearPassword); // NOI18N
        }

        @Override
        public boolean promptPassword(String message) {
            synchronized (lock) {
                boolean result;
                PasswordDlg pwdDlg = new PasswordDlg();

                synchronized (lock) {
                    result = pwdDlg.askPassword(env);
                }

                if (result) {
                    char[] clearPassword = pwdDlg.getPassword();
                    pm.storePassword(env, clearPassword, pwdDlg.isRememberPassword());
                    Arrays.fill(clearPassword, (char) 0);
                    pwdDlg.clearPassword();
                    return true;
                } else {
                    throw new CancellationException(loc("USER_AUTH_CANCELED")); // NOI18N
                }
            }
        }

        @Override
        public boolean promptPassphrase(String arg0) {
            return true;
        }

        @Override
        public boolean promptYesNo(String str) {
            Object[] options = {"yes", "no"}; // NOI18N
            int foo;

            synchronized (lock) {
                foo = JOptionPane.showOptionDialog(parent, str,
                        loc("TITLE_YN_Warning"), // NOI18N
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            }

            return foo == 0;
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
    }
}
