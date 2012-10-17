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
package org.netbeans.modules.ods.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.ods.api.CloudServer;
import org.netbeans.modules.ods.api.CloudServerManager;
import org.netbeans.modules.ods.api.ODSProject;
import org.netbeans.modules.ods.client.api.ODSException;
import org.openide.util.NbPreferences;
import static org.netbeans.modules.ods.ui.Bundle.*;
import org.netbeans.modules.ods.ui.api.CloudUiServer;
import org.netbeans.modules.ods.ui.dashboard.ProjectHandleImpl;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamUIUtils;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Vrabec
 */
public class Utilities {
    
    public final static String LOGIN_STATUS_PREF = ".login"; //NOI18N
    final static String CLOUD_USERNAME_PREF = ".username"; //NOI18N
    private final static String CLOUD_PASSWORD_PREF = ".password"; //NOI18N
    
    private Utilities () {
        
    }
    
    public static String getPrefName (CloudServer server, String name)  {
        return server.getUrl().getHost() + name;
    }
    
    /**
     * Loads password from the keyring. For settings compatibility,
     * can also interpret and upgrade old insecure storage.
     */
    @SuppressWarnings("deprecation")
    private static char[] loadPassword (CloudServer server, Preferences preferences) {
        String passwordPref = Utilities.getPrefName(server, CLOUD_PASSWORD_PREF);
        char[] newPassword = Keyring.read(passwordPref);
        return newPassword;
    }

    @Messages({"# {0} - cloud server name", "Utilities.password_keyring_description=Password for {0}"})
    static void savePassword (CloudServer server, String username, char[] password) {
        String passwordPref = Utilities.getPrefName(server, CLOUD_PASSWORD_PREF);
        Preferences preferences = NbPreferences.forModule(Utilities.class);
        if (password != null) {
            preferences.put(Utilities.getPrefName(server, CLOUD_USERNAME_PREF), username); //NOI18N
            Keyring.save(passwordPref, password,
                    Utilities_password_keyring_description(server.getUrl().getHost()));
        } else {
            preferences.remove(Utilities.getPrefName(server, CLOUD_USERNAME_PREF)); //NOI18N
            Keyring.delete(passwordPref);
        }
        preferences.remove(passwordPref);
    }
    
    static class CredentialsImpl implements LoginPanelDetails.Credentials {

        @Override
        public String getUsername (CloudServer server) {
            final Preferences preferences = NbPreferences.forModule(Utilities.class);
            String uname = preferences.get(Utilities.getPrefName(server, CLOUD_USERNAME_PREF), ""); // NOI18N
            if (uname==null) {
                return "";
            }
            return uname;
        }

        @Override
        public char[] getPassword (CloudServer server) {
            final Preferences preferences = NbPreferences.forModule(Utilities.class);
            char[] password = loadPassword(server, preferences);
            if (password==null) {
                return new char[0];
            }
            return password;
        }
    }

    static boolean login (CloudServer server) {
        if (!server.isLoggedIn()) {
            final Preferences preferences = NbPreferences.forModule(Utilities.class);
            if (!preferences.getBoolean(getPrefName(server, LOGIN_STATUS_PREF), false)) {
                return false;
            }
            String uname = preferences.get(Utilities.getPrefName(server, CLOUD_USERNAME_PREF), ""); //NOI18N
            if (uname.isEmpty()) {
                return false;
            }
            char[] pwd = loadPassword(server, preferences);
            if (pwd == null || pwd.length == 0) {
                return false;
            }
            try {
                server.login(uname, pwd);
                return true;
            } catch (ODSException ex) {
                Logger.getLogger(Utilities.class.getName()).log(Level.FINE, null, ex);
            }
        }
        return false;
    }
    
    public static List<ProjectHandle<ODSProject>> getMyProjects(CloudUiServer uiServer, boolean force) throws ODSException {
        return toODSProjects(uiServer, uiServer.getServer().getMyProjects(force));
    }

    public static List<ProjectHandle<ODSProject>> getMyProjects(CloudUiServer uiServer) throws ODSException {
        return toODSProjects(uiServer, uiServer.getServer().getMyProjects());
    }

    public static CloudServer getActiveServer (boolean loggedInExpected) {
        CloudServer server = null;
        TeamServer selectedServer = TeamUIUtils.getSelectedServer();
        if (selectedServer instanceof CloudUiServer) {
            server = ((CloudUiServer) selectedServer).getServer();
        } else {
            Collection<CloudServer> servers = CloudServerManager.getDefault().getServers();
            for (CloudServer s : servers) {
                if (server == null) {
                    server = s;
                }
                if (s.isLoggedIn()) {
                    server = s;
                    break;
                }
            }
        }
        if (server == null) {
            return null;
        }
        if (loggedInExpected && !server.isLoggedIn()) {
            TeamServer uiServer = TeamUIUtils.showLogin(CloudUiServer.forServer(server), false);
            if (uiServer instanceof CloudUiServer) {
                server = ((CloudUiServer) uiServer).getServer();
            }
        }
        return server;
    }


    private static List<ProjectHandle<ODSProject>> toODSProjects(CloudUiServer uiServer, Collection<ODSProject> projects) {
        if(projects == null) {
            return Collections.emptyList();
        }
        List<ProjectHandle<ODSProject>> ret = new ArrayList<ProjectHandle<ODSProject>>(projects.size());
        for (ODSProject project : projects) {
            ret.add(new ProjectHandleImpl(uiServer, project));
        }
        return ret;
    }        
}
