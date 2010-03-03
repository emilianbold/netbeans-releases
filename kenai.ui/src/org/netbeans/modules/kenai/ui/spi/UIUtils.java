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

package org.netbeans.modules.kenai.ui.spi;

import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiUser;
import org.netbeans.modules.kenai.collab.chat.KenaiConnection;
import org.netbeans.modules.kenai.collab.chat.PresenceIndicator;
import org.netbeans.modules.kenai.ui.KenaiLoginTask;
import org.netbeans.modules.kenai.ui.LoginPanel;
import org.netbeans.modules.kenai.ui.Utilities;
import org.netbeans.modules.kenai.ui.dashboard.UserNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * This class is not yet final. We be changed
 * @author Jan Becicka
 */
public final class UIUtils {

    public final static String ONLINE_ON_CHAT_PREF = ".online_chat";// NOI18N
    private final static String KENAI_PASSWORD_PREF = ".password"; //NOI18N
    private final static String KENAI_USERNAME_PREF = ".username"; //NOI18N
    public final static String ONLINE_STATUS_PREF = ".online"; // NOI18N
    public final static String LOGIN_STATUS_PREF = ".login";// NOI18N

    // Usage logging
    private static Logger metricsLogger;
    private static final String USG_KENAI = "USG_KENAI"; // NOI18N
    /** To avoid logging same params more than once in a session. Expecting
     * less than 20 possible combinations at max. */
    private static Set<String> loggedParams = Collections.synchronizedSet(new HashSet<String>());

    public static String getPrefName(Kenai kenai, String name)  {
        return kenai.getUrl().getHost() + name;
    }

    public static void waitStartupFinished() {
        KenaiLoginTask.waitStartupFinished();
    }
    
    private UIUtils() {
    }

    /**
     * this method will be removed
     * will try to login using stored uname and password if not already logged in
     * @param force
     * @return true if logged in, false otherwise
     * @deprecated 
     */
    @Deprecated
    public static synchronized boolean tryLogin(final Kenai kenai, boolean force) {
        if (kenai.getStatus()!=Kenai.Status.OFFLINE) {
            return true;
        }
        boolean chatSupported = Utilities.isChatSupported(kenai);
        final Preferences preferences = NbPreferences.forModule(LoginPanel.class);

        if (!force) {
            String online = preferences.get(getPrefName(kenai, LOGIN_STATUS_PREF), "false"); // NOI18N
            if (!Boolean.parseBoolean(online)) {
                return false;
            }
        }

        String uname=preferences.get(getPrefName(kenai, KENAI_USERNAME_PREF), null); // NOI18N
        if (uname==null) {
            return false;
        }
        PresenceIndicator.getDefault().init();
        try {
            KenaiConnection.getDefault(kenai);
            char[] password = loadPassword(kenai, preferences);
            if (password == null) {
                return false;
            }
            kenai.login(uname, password,
                    force ? true : Boolean.parseBoolean(preferences.get(getPrefName(kenai, ONLINE_STATUS_PREF), String.valueOf(chatSupported))));
        } catch (KenaiException ex) {
            return false;
        }
        return true;
    }

    /**
     * @return
     */
    public static boolean showLogin() {
        return showKenaiLogin()!=null;
    }

    /**
     *
     * @return
     */
    public static Kenai showKenaiLogin() {
        for (Kenai k: KenaiManager.getDefault().getKenais()) {
            if (k.getStatus()==Kenai.Status.OFFLINE) {
                return showKenaiLogin(k);
            }
        }
        return showKenaiLogin(KenaiManager.getDefault().getKenai("https://kenai.com"));
    }
    /**
     * Loads password from the keyring. For settings compatibility,
     * can also interpret and upgrade old insecure storage.
     */
    @SuppressWarnings("deprecation")
    private static char[] loadPassword(Kenai kenai,Preferences preferences) {
        String passwordPref = getPrefName(kenai, KENAI_PASSWORD_PREF);
        String scrambledPassword = preferences.get(passwordPref, null); // NOI18N
        char[] newPassword = Keyring.read(passwordPref);
        if (scrambledPassword != null) {
            preferences.remove(passwordPref);
            if (newPassword == null) {
                return Scrambler.getInstance().descramble(scrambledPassword).toCharArray();
            }
        }
        return newPassword;
    }

    /**
     * Invokes login dialog
     * @param kenai
     * @return true, if user was succesfully logged in
     */
    public static boolean showLogin(final Kenai kenai) {
        return showKenaiLogin(kenai) != null;
    }

    /**
     * Invokes login dialog
     * @param kenai
     * @return kenai instance, where user requested login, or null if login was
     * cancelled
     */
    public static Kenai showKenaiLogin(final Kenai kenai) {
        PresenceIndicator.getDefault().init();
        final LoginPanel loginPanel = new LoginPanel(kenai, new CredentialsImpl());
        final Preferences preferences = NbPreferences.forModule(LoginPanel.class);
        final String ctlLogin = NbBundle.getMessage(Utilities.class, "CTL_Login");
        final String ctlCancel = NbBundle.getMessage(Utilities.class, "CTL_Cancel");
        DialogDescriptor login = new DialogDescriptor(
                loginPanel,
                NbBundle.getMessage(Utilities.class, "CTL_LoginToKenai"),
                true,
                new Object[]{ctlLogin,ctlCancel},ctlLogin,
                DialogDescriptor.DEFAULT_ALIGN,
                null, new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        final Kenai k = loginPanel.getKenai();
                        if (event.getSource().equals(ctlLogin)) {
                            UIUtils.logKenaiUsage("LOGIN"); // NOI18N
                        loginPanel.showProgress();
                        RequestProcessor.getDefault().post(new Runnable() {

                            public void run() {
                                try {
                                    KenaiConnection.getDefault(k);
                                    k.login(loginPanel.getUsername(), loginPanel.getPassword(), loginPanel.isOnline());
                                    SwingUtilities.invokeLater(new Runnable() {

                                        public void run() {
                                            JRootPane rootPane = loginPanel.getRootPane();
                                            if (rootPane != null) {
                                                JDialog parent = (JDialog) rootPane.getParent();
                                                if (parent != null) {
                                                    parent.setVisible(false);
                                                    parent.dispose();
                                                }
                                            }
                                        }
                                    });
                                } catch (final KenaiException ex) {
                                    SwingUtilities.invokeLater(new Runnable() {

                                        public void run() {
                                            loginPanel.showError(ex);
                                        }
                                    });
                                }
                            }
                        });
                        String passwordPref = getPrefName(k, KENAI_PASSWORD_PREF);
                        if (loginPanel.isStorePassword()) {
                            preferences.put(getPrefName(k, KENAI_USERNAME_PREF), loginPanel.getUsername()); // NOI18N
                            Keyring.save(passwordPref, loginPanel.getPassword(),
                                    NbBundle.getMessage(UIUtils.class, "UIUtils.password_keyring_description", k.getUrl().getHost()));
                        } else {
                            preferences.remove(getPrefName(k, KENAI_USERNAME_PREF)); // NOI18N
                            Keyring.delete(passwordPref);
                        }
                        preferences.remove(passwordPref);
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
        d.setResizable(false);
        loginPanel.clearStatus();
        d.setVisible(true);

        if (loginPanel.getClientProperty("cancel")==null) {  // NOI18N
            return loginPanel.getKenai();
        }
        return null;
    }

    private static class CredentialsImpl implements LoginPanel.Credentials {

        public String getUsername(Kenai kenai) {
            final Preferences preferences = NbPreferences.forModule(LoginPanel.class);
            String uname = preferences.get(getPrefName(kenai, KENAI_USERNAME_PREF), ""); // NOI18N
            if (uname==null) {
                return "";
            }
            return uname;
        }

        public char[] getPassword(Kenai kenai) {
            final Preferences preferences = NbPreferences.forModule(LoginPanel.class);
            char[] password = loadPassword(kenai, preferences);
            if (password==null) {
                return new char[0];
            }
            return password;
        }
    }

    public static JLabel createUserWidget(String user) {
        return createUserWidget(new KenaiUserUI(user));
    }

    static JLabel createUserWidget(final KenaiUserUI u) {
        final JLabel result = new JLabel(u.getUserName());
        result.setIcon(u.getIcon());
        final String name = u.getKenaiUser().getFirstName() + " " + u.getKenaiUser().getLastName(); // NOI18N
        result.setToolTipText(NbBundle.getMessage(UserNode.class, u.getKenaiUser().isOnline()?"LBL_ONLINE_MEMBER_TOOLTIP": "LBL_OFFLINE_MEMBER_TOOLTIP", u.getUserName(), name));
        u.user.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (KenaiUser.PROP_PRESENCE.equals(evt.getPropertyName())) {
                    result.firePropertyChange(KenaiUser.PROP_PRESENCE, (Boolean) evt.getOldValue(), (Boolean) evt.getNewValue());
                    result.setToolTipText(NbBundle.getMessage(UserNode.class, u.getKenaiUser().isOnline()?"LBL_ONLINE_MEMBER_TOOLTIP": "LBL_OFFLINE_MEMBER_TOOLTIP", u.getUserName(), name));
                    result.repaint();
                }
            }
        });
        result.addMouseListener(new MouseAdapter() {
            private Cursor oldCursor;

            @Override
            public void mouseEntered(MouseEvent e) {
                oldCursor = result.getCursor();
                result.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                result.setCursor(oldCursor);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton()==1)
                    u.startChat();
            }

        });

        return result;
    }

    public static void logKenaiUsage(Object... parameters) {
        String paramStr = getParamString(parameters);
        if (loggedParams.add(paramStr)) {
            // not logged in this session yet
            if (metricsLogger == null) {
                metricsLogger = Logger.getLogger("org.netbeans.ui.metrics.kenai"); // NOI18N
            }
            LogRecord rec = new LogRecord(Level.INFO, USG_KENAI);
            rec.setParameters(parameters);
            rec.setLoggerName(metricsLogger.getName());
            metricsLogger.log(rec);
        }
    }

    private static String getParamString(Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            return ""; // NOI18N
        }
        if (parameters.length == 1) {
            return parameters[0].toString();
        }
        StringBuilder buf = new StringBuilder();
        for (Object p : parameters) {
            buf.append(p.toString());
        }
        return buf.toString();
    }

}

