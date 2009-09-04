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
package org.netbeans.modules.wag.manager.util;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author peterliu
 */
public class WagPreferences {

    private static final String ZEMBLY_USERNAME_PREF = "zemblyUsernamePref";    //NOI18N
    private static final String ZEMBLY_PASSWORD_PREF = "zemblyPasswordPref";    //NOI18N
    private static final String ZEMBLY_ONLINE_STATUS_PREF = "zemblyOnlineStatusPref";    //NOI18N
    private static WagPreferences instance;
    private static Preferences preferences;

    private WagPreferences() {
        preferences = NbPreferences.forModule(WagPreferences.class);
    }

    public static final WagPreferences getInstance() {
        if (instance == null) {
            instance = new WagPreferences();
        }

        return instance;
    }

    public void setUsername(String username) {
        if (username != null) {
            preferences.put(ZEMBLY_USERNAME_PREF, username);
        } else {
            preferences.remove(ZEMBLY_USERNAME_PREF);
        }
    }

    public String getUsername() {
        return preferences.get(ZEMBLY_USERNAME_PREF, null);
    }

    public void setPassword(char[] password) {
        if (password != null) {
            preferences.put(ZEMBLY_PASSWORD_PREF,
                    Scrambler.getInstance().scramble(new String(password)));
        } else {
            preferences.remove(ZEMBLY_PASSWORD_PREF);
        }

    }

    public char[] getPassword() {
        String password = preferences.get(ZEMBLY_PASSWORD_PREF, null);

        if (password != null) {
            return Scrambler.getInstance().descramble(password).toCharArray();
        } else {
            return null;
        }
    }

    public void setOnlineStatus(boolean flag) {
        preferences.put(ZEMBLY_ONLINE_STATUS_PREF, Boolean.toString(flag));
    }

    public boolean getOnlineStatus() {
        String status = preferences.get(ZEMBLY_ONLINE_STATUS_PREF, null);

        if (status == null) {
            return false;
        } else {
            return Boolean.valueOf(status);
        }
    }
}
