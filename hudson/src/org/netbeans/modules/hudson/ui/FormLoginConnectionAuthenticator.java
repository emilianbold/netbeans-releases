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

package org.netbeans.modules.hudson.ui;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.netbeans.modules.hudson.spi.ConnectionAuthenticator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implements authentication based on Hudson's standard login form.
 * {@code main/core/src/main/resources/hudson/model/Hudson/login.jelly} shows the style.
 * Assumes the Hudson instance is set up to use ACEGI-based security, not "legacy" container auth.
 * Keeps username in preferences and authenticates only when requested.
 */
public class FormLoginConnectionAuthenticator extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(FormLoginConnectionAuthenticator.class.getName());

    private static Preferences loginPrefs() {
        return NbPreferences.forModule(FormLoginConnectionAuthenticator.class).node("authentication"); // NOI18N
    }

    /**
     * Session cookies set by home.
     * {@link java.net.CookieManager} in JDK 6 would be a bit easier.
     */
    private static final Map<URL,String[]> COOKIES = new HashMap<URL,String[]>();

    private FormLoginConnectionAuthenticator() {
        initComponents();
    }

    @ServiceProvider(service=ConnectionAuthenticator.class, position=100)
    public static class AuthImpl implements ConnectionAuthenticator {

        public void prepareRequest(URLConnection conn, URL home) {
            if (COOKIES.containsKey(home)) {
                for (String cookie : COOKIES.get(home)) {
                    String cookieBare = cookie.replaceFirst(";.*", ""); // NOI18N
                    LOGGER.log(Level.FINER, "Setting cookie {0} for {1}", new Object[] {cookieBare, conn.getURL()});
                    conn.setRequestProperty("Cookie", cookieBare); // NOI18N
                }
            }
        }

        public URLConnection forbidden(URLConnection conn, URL home) {
            FormLoginConnectionAuthenticator panel = new FormLoginConnectionAuthenticator();
            String server = HudsonManagerImpl.simplifyServerLocation(home.toString(), true);
            String username = loginPrefs().get(server, null);
            if (username != null) {
                panel.userField.setText(username);
            }
            panel.locationField.setText(home.toString());
            DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(FormLoginConnectionAuthenticator.class, "FormLoginConnectionAuthenticator.log_in"));
            if (DialogDisplayer.getDefault().notify(dd) != NotifyDescriptor.OK_OPTION) {
                return null;
            }
            username = panel.userField.getText();
            loginPrefs().put(server, username);
            String password = new String(panel.passField.getPassword());
            panel.passField.setText("");
            try {
                Map<String,List<String>> responseHeaders = new HashMap<String,List<String>>();
                new ConnectionBuilder().url(new URL(home, "j_acegi_security_check")). // NOI18N
                        postData(("j_username=" + URLEncoder.encode(username, "UTF-8") + "&j_password=" + // NOI18N
                                  URLEncoder.encode(password, "UTF-8")).getBytes("UTF-8")). // NOI18N
                        collectResponseHeaders(responseHeaders).connection();
                List<String> cookies = responseHeaders.get("Set-Cookie"); // NOI18N
                if (cookies == null) {
                    LOGGER.log(Level.FINE, "No cookies set from authentication to {0}", home);
                    return null;
                }
                LOGGER.log(Level.FINE, "Authenticated to {0}: {1}", new Object[] {home, cookies});
                COOKIES.put(home, cookies.toArray(new String[0]));
                return conn.getURL().openConnection();
            } catch (IOException x) {
                LOGGER.log(Level.FINE, null, x);
            }
            return null;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        locationLabel = new javax.swing.JLabel();
        locationField = new javax.swing.JTextField();
        userLabel = new javax.swing.JLabel();
        userField = new javax.swing.JTextField();
        passLabel = new javax.swing.JLabel();
        passField = new javax.swing.JPasswordField();

        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(FormLoginConnectionAuthenticator.class, "FormLoginConnectionAuthenticator.locationLabel.text")); // NOI18N

        locationField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(FormLoginConnectionAuthenticator.class, "FormLoginConnectionAuthenticator.userLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(passLabel, org.openide.util.NbBundle.getMessage(FormLoginConnectionAuthenticator.class, "FormLoginConnectionAuthenticator.passLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(locationLabel)
                        .add(36, 36, 36)
                        .add(locationField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(passLabel)
                            .add(userLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(passField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                            .add(userField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(locationLabel)
                    .add(locationField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(userLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passLabel)
                    .add(passField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField locationField;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JPasswordField passField;
    private javax.swing.JLabel passLabel;
    private javax.swing.JTextField userField;
    private javax.swing.JLabel userLabel;
    // End of variables declaration//GEN-END:variables

    public @Override void addNotify() {
        super.addNotify();
        ((userField.getText().length() > 0) ? passField : userField).requestFocus();
    }

}
