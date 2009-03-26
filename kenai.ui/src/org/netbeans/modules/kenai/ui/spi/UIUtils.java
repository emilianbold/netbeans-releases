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
import java.security.NoSuchAlgorithmException;
import java.util.prefs.Preferences;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.ui.LoginPanel;
import org.netbeans.modules.kenai.ui.Utilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * This class is not yet final. We be changed
 * @author Jan Becicka
 */
public final class UIUtils {
    private static final String encMethod = "AES"; // NOI18N
    private static SecretKeySpec key = new SecretKeySpec("netbeansnetbeans".getBytes(), encMethod); // NOI18N
    private static Cipher cipher;
    static {
        try {
            cipher = Cipher.getInstance(encMethod);
        } catch (NoSuchAlgorithmException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchPaddingException ex) {
            Exceptions.printStackTrace(ex);
        }
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
     * @return true if logged in, false otherwise
     */
    @Deprecated
    public static boolean tryLogin() {
        if (Kenai.getDefault().getPasswordAuthentication()!=null) {
            return true;
        }
        final Preferences preferences = NbPreferences.forModule(LoginPanel.class);
        String uname=preferences.get("kenai.username", null); // NOI18N
        if (uname==null) {
            return false;
        }
        byte[] password=preferences.getByteArray("kenai.password", null); // NOI18N
        try {
            Kenai.getDefault().login(uname, new String(decode(password)).toCharArray());
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
        final LoginPanel loginPanel = new LoginPanel();
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
                        loginPanel.showProgress();
                        RequestProcessor.getDefault().post(new Runnable() {

                            public void run() {
                                try {
                                    if (loginPanel.getUsername().contains("@")) //NOI18N
                                        throw new KenaiException(NbBundle.getMessage(LoginPanel.class, "ERR_InvalidUsername"));
                                    Kenai.getDefault().login(loginPanel.getUsername(), loginPanel.getPassword());
                                    loginPanel.getRootPane().getParent().setVisible(false);
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
                            preferences.put("kenai.username", loginPanel.getUsername()); // NOI18N
                            preferences.putByteArray("kenai.password", encode(new String(loginPanel.getPassword()).getBytes())); // NOI18N
                        } else {
                            preferences.remove("kenai.username"); // NOI18N
                            preferences.remove("kenai.password"); // NOI18N
                        }
                    } else {
                        loginPanel.putClientProperty("cancel", "true"); // NOI18N
                        loginPanel.getRootPane().getParent().setVisible(false);
                    }
                }
        });
        login.setClosingOptions(new Object[]{ctlCancel});
        Dialog d = DialogDisplayer.getDefault().createDialog(login);

        String uname=preferences.get("kenai.username", null); // NOI18N
        byte[] password=preferences.getByteArray("kenai.password", null); // NOI18N
        if (uname!=null && password!=null) {
            loginPanel.setUsername(uname);
            loginPanel.setPassword(new String(decode(password)).toCharArray());
        }
        d.pack();
        loginPanel.clearStatus();
        d.setVisible(true);

        return loginPanel.getClientProperty("cancel")==null; // NOI18N
    }


        /** Creates new form LoginPanel */


    static byte[] encode(byte[] password) {

        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(password);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    static byte[] decode(byte[] pass) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(pass);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
