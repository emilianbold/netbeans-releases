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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiUser;
import org.netbeans.modules.kenai.collab.chat.KenaiConnection;
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
    private static Set<String> loggedParams; // to avoid logging same params more than once in a session

    public static String getPrefName(String name)  {
        return Kenai.getDefault().getUrl().getHost() + name;
    }

    public static void waitStartupFinished() {
        KenaiLoginTask.waitStartupFinished();
    }
    
    private UIUtils() {
    }

    /**
     * do we need this method at all
     * TODO: remove me
     * @return
     * @deprecated
     */
    @Deprecated
    public static final JTextPane createHTMLPane() {
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html"); // NOI18N
        Font font = UIManager.getFont("Label.font"); // NOI18N
        String bodyRule = "body { font-family: " + font.getFamily() + "; " + // NOI18N
                "font-size: " + font.getSize() + "pt; }"; // NOI18N

        final StyleSheet styleSheet = ((HTMLDocument) textPane.getDocument()).getStyleSheet();

        styleSheet.addRule(bodyRule);
        styleSheet.addRule(".green {color: green;}"); // NOI18N
        styleSheet.addRule(".red {color: red;"); // NOI18N
        textPane.setEditable(false);
        textPane.setBackground(UIManager.getColor("TextPane.background")); // NOI18N
        return textPane;
    }

    /**
     * do we need this method at all
     * TODO: remove me
     * @param text
     * @return
     * @deprecated
     */
    @Deprecated
    public static final JButton createFocusableHyperlink(String text) {
        final JButton hyperlink=new JButton("<html><body><a href=\"foo\">"+text+"</a>"); // NOI18N
        hyperlink.setBorderPainted(false);
        hyperlink.setContentAreaFilled(false);
        hyperlink.setOpaque(false);
        hyperlink.addMouseListener(new MouseAdapter() {
            private Cursor oldCursor;

            @Override
            public void mouseEntered(MouseEvent e) {
                oldCursor = hyperlink.getCursor();
                hyperlink.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hyperlink.setCursor(oldCursor);
            }
        });
        return hyperlink;
    }


    /**
     * this method will be removed
     * will try to login using stored uname and password if not already logged in
     * @param force
     * @return true if logged in, false otherwise
     * @deprecated 
     */
    @Deprecated
    public static synchronized boolean tryLogin(boolean force) {
        Utilities.isChatSupported();
        if (Kenai.getDefault().getPasswordAuthentication()!=null) {
            return true;
        }
        final Preferences preferences = NbPreferences.forModule(LoginPanel.class);

        if (!force) {
            String online = preferences.get(getPrefName(LOGIN_STATUS_PREF), "false"); // NOI18N
            if (!Boolean.parseBoolean(online)) {
                return false;
            }
        }

        String uname=preferences.get(getPrefName(KENAI_USERNAME_PREF), null); // NOI18N
        if (uname==null) {
            return false;
        }
        String password=preferences.get(getPrefName(KENAI_PASSWORD_PREF), null); // NOI18N
        try {
            KenaiConnection.getDefault();
            Kenai.getDefault().login(uname, Scrambler.getInstance().descramble(password).toCharArray(), force?true:Boolean.parseBoolean(preferences.get(getPrefName(ONLINE_STATUS_PREF), String.valueOf(Utilities.isChatSupported()))));
        } catch (KenaiException ex) {
            return false;
        }
        return true;
    }

    /**
     * Invokes login dialog
     * @return true, if user was succesfully logged in
     */
    public static boolean showLogin() {
        final LoginPanel loginPanel = new LoginPanel(Utilities.isChatSupported());
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
                        if (event.getSource().equals(ctlLogin)) {
                            UIUtils.logKenaiUsage("LOGIN"); // NOI18N
                        loginPanel.showProgress();
                        RequestProcessor.getDefault().post(new Runnable() {

                            public void run() {
                                try {
                                    KenaiConnection.getDefault();
                                    Kenai.getDefault().login(loginPanel.getUsername(), loginPanel.getPassword(), loginPanel.isOnline());
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
                        if (loginPanel.isStorePassword()) {
                            preferences.put(getPrefName(KENAI_USERNAME_PREF), loginPanel.getUsername()); // NOI18N
                            preferences.put(getPrefName(KENAI_PASSWORD_PREF), Scrambler.getInstance().scramble(new String(loginPanel.getPassword()))); // NOI18N
                        } else {
                            preferences.remove(getPrefName(KENAI_USERNAME_PREF)); // NOI18N
                            preferences.remove(getPrefName(KENAI_PASSWORD_PREF)); // NOI18N
                        }
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

        String uname=preferences.get(getPrefName(KENAI_USERNAME_PREF), null); // NOI18N
        String password=preferences.get(getPrefName(KENAI_PASSWORD_PREF), null); // NOI18N
        if (uname!=null && password!=null) {
            loginPanel.setUsername(uname);
            loginPanel.setPassword(Scrambler.getInstance().descramble(password).toCharArray());
        }
        d.pack();
        d.setResizable(false);
        loginPanel.clearStatus();
        d.setVisible(true);

        return loginPanel.getClientProperty("cancel")==null; // NOI18N
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

    public static synchronized void logKenaiUsage(Object... parameters) {
        String paramStr = getParamString(parameters);
        if (loggedParams == null || !loggedParams.contains(paramStr)) {
            // not logged in this session yet
            if (metricsLogger == null) {
                metricsLogger = Logger.getLogger("org.netbeans.ui.metrics.kenai"); // NOI18N
            }
            LogRecord rec = new LogRecord(Level.INFO, USG_KENAI);
            rec.setParameters(parameters);
            rec.setLoggerName(metricsLogger.getName());
            metricsLogger.log(rec);

            if (loggedParams == null) {
                loggedParams = new HashSet<String>();
            }
            loggedParams.add(paramStr);
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

