/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.team.spi;

import java.util.regex.Pattern;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.bugtracking.BugtrackingConfig;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public final class NBBugzillaUtils {
    private static final String NB_BUGZILLA_PASSWORD = "nbbugzilla.password";                // NOI18N
    private static final String NB_BUGZILLA_USERNAME = "nbbugzilla.username";                // NOI18N
    
    private static final Pattern netbeansUrlPattern = Pattern.compile("(https|http)://(([a-z]|\\d)+\\.)*([a-z]|\\d)*netbeans([a-z]|\\d)*(([a-z]|\\d)*\\.)+org(.*)"); // NOI18N
    /**
     * Determines wheter the given {@link RepositoryProvider} is the
     * repository hosting netbeans or not
     *
     * @param repo
     * @return true if the given repository is the netbenas bugzilla, otherwise false
     */
    public static boolean isNbRepository(String url) {
        boolean ret = netbeansUrlPattern.matcher(url.toString()).matches();
        if(ret) {
            return true;
        }
        String nbUrl = System.getProperty("netbeans.bugzilla.url");  // NOI18N
        if(nbUrl == null || nbUrl.equals("")) {                      // NOI18N
            return false;
        }
        return url.toString().startsWith(nbUrl);
    }
    
    /**
     * Returns the netbeans.org username
     * Shouldn't be called in awt
     *
     * @return username
     */
    public static String getNBUsername() {
        String user = BugtrackingConfig.getInstance().getPreferences().get(NB_BUGZILLA_USERNAME, ""); // NOI18N
        if("".equals(user)) {                                                   // NOI18N    
            user = RepositoryRegistry.getBugzillaNBUsername();
        }
        return user.equals("") ? null : user;                                   // NOI18N
    }

    /**
     * Returns the netbeans.org password
     * Shouldn't be called in awt
     *
     * @return password
     */
    public static char[] getNBPassword() {
        return Keyring.read(NB_BUGZILLA_PASSWORD);
    }

    /**
     * Save the given username as a netbeans.org username.
     * Shouldn't be called in awt
     */
    public static void saveNBUsername(String username) {
        if(username == null) {
            return;
        }
        BugtrackingConfig.getInstance().getPreferences().put(NB_BUGZILLA_USERNAME, username);
    }

    /**
     * Saves the given value as a netbeans.org password
     * Shouldn't be called in awt
     */
    public static void saveNBPassword(char[] password) {
        if(password == null) {
            Keyring.delete(NB_BUGZILLA_PASSWORD);
        } else {
            Keyring.save(
                NB_BUGZILLA_PASSWORD,
                password,
                NbBundle.getMessage(
                    BugtrackingUtil.class,
                    "NBRepositorySupport.password_keyring_description"));       // NOI18N

        }
    }
}
