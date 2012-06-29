/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.kenai.ui.impl;

import java.util.prefs.Preferences;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.collab.chat.KenaiConnection;
import org.netbeans.modules.kenai.collab.chat.PresenceIndicator;
import org.netbeans.modules.kenai.ui.Utilities;
import org.netbeans.modules.kenai.ui.dashboard.DashboardImpl;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Becicka, Ondrej Vrabec
 */
public class LoginUtils {
    public static final String ONLINE_ON_CHAT_PREF = ".online_chat"; // NOI18N
    public static final String ONLINE_STATUS_PREF = ".online"; // NOI18N
    public static final String LOGIN_STATUS_PREF = ".login"; // NOI18N
    final static String KENAI_USERNAME_PREF = ".username"; //NOI18N
    private final static String KENAI_PASSWORD_PREF = ".password"; //NOI18N
    
    private LoginUtils () {
        
    }

    public static boolean tryLogin (Kenai kenai, boolean force) {
        if (kenai.getStatus()!=Kenai.Status.OFFLINE) {
            return true;
        }
        final Preferences preferences = NbPreferences.forModule(DashboardImpl.class);

        if (!force) {
            String online = preferences.get(UIUtils.getPrefName(kenai, LOGIN_STATUS_PREF), "false"); // NOI18N
            if (!Boolean.parseBoolean(online)) {
                return false;
            }
        }

        String uname=preferences.get(UIUtils.getPrefName(kenai, KENAI_USERNAME_PREF), null); // NOI18N
        if (uname==null) {
            return false;
        }
        boolean goOnline = Boolean.parseBoolean(preferences.get(UIUtils.getPrefName(kenai, ONLINE_STATUS_PREF), "false")) && Utilities.isChatSupported(kenai);
        PresenceIndicator.getDefault().init();
        try {
            KenaiConnection.getDefault(kenai);
            char[] password = loadPassword(kenai, preferences);
            if (password == null) {
                return false;
            }
            kenai.login(uname, password,
                    force ? true : goOnline);
        } catch (KenaiException ex) {
            return false;
        }
        return true;
    }
    
    /**
     * Loads password from the keyring. For settings compatibility,
     * can also interpret and upgrade old insecure storage.
     */
    @SuppressWarnings("deprecation")
    private static char[] loadPassword(Kenai kenai,Preferences preferences) {
        String passwordPref = UIUtils.getPrefName(kenai, KENAI_PASSWORD_PREF);
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

    static void savePassword (Kenai kenai, String username, char[] password) {
        String passwordPref = UIUtils.getPrefName(kenai, KENAI_PASSWORD_PREF);
        Preferences preferences = NbPreferences.forModule(LoginUtils.class);
        if (password != null) {
            preferences.put(UIUtils.getPrefName(kenai, KENAI_USERNAME_PREF), username); //NOI18N
            Keyring.save(passwordPref, password,
                    NbBundle.getMessage(UIUtils.class, "UIUtils.password_keyring_description", kenai.getUrl().getHost()));
        } else {
            preferences.remove(UIUtils.getPrefName(kenai, KENAI_USERNAME_PREF)); //NOI18N
            Keyring.delete(passwordPref);
        }
        preferences.remove(passwordPref);
    }
    
    static class CredentialsImpl implements LoginPanelDetails.Credentials {

        @Override
        public String getUsername(Kenai kenai) {
            final Preferences preferences = NbPreferences.forModule(LoginUtils.class);
            String uname = preferences.get(UIUtils.getPrefName(kenai, KENAI_USERNAME_PREF), ""); // NOI18N
            if (uname==null) {
                return "";
            }
            return uname;
        }

        @Override
        public char[] getPassword(Kenai kenai) {
            final Preferences preferences = NbPreferences.forModule(LoginUtils.class);
            char[] password = loadPassword(kenai, preferences);
            if (password==null) {
                return new char[0];
            }
            return password;
        }
    }
}
