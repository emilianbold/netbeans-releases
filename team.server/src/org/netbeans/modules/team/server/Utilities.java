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

package org.netbeans.modules.team.server;

import java.util.Collection;
import java.util.prefs.Preferences;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Milan Kubec
 */
public class Utilities {
    private static RequestProcessor RP;

    public static void assertJid(String name) {
        assert name!=null;
        assert name.contains("@"): "name must be FQN";
        assert !name.contains("/"): "name cannot contain '/'";
    }

    public static TeamServer getPreferredServer() {
        Collection<TeamServer> servers = TeamServerManager.getDefault().getTeamServers();
        TeamServer server = null;
        for (TeamServer k:servers) {
            if (k.getUrl().getHost().endsWith("java.net")) { //NOI18N
                return k;
            }
            if (k.getUrl().getHost().equals("kenai.com")) { //NOI18N
                server = k;
            }
        }
        if (server!=null) {
            return server;
        }
        if (!servers.isEmpty()) {
            return servers.iterator().next();
        }
        return null;
    }

    public static TeamServer getLastTeamServer() {
        Preferences prefs = NbPreferences.forModule(Utilities.class);
        String url = prefs.get("dashboard.last.selected.teamserver", null); //NOI18N
        if (url!=null) {
            return TeamServerManager.getDefault().getTeamServer(url);
        }
        return null;
    }

    public static void setLastTeamServer(TeamServer k) {
        if (k==null)
            return;
        Preferences prefs = NbPreferences.forModule(Utilities.class);
        prefs.put("dashboard.last.selected.teamserver", k.getUrl().toString()); //NOI18N
    }
    
    public static RequestProcessor getRequestProcessor () {
        if (RP == null) {
            RP = new RequestProcessor("Team tasks", 1); //NOI18N
        }
        return RP;
    }

    public static boolean isMoreProjectsDashboard() {
        return Boolean.getBoolean("team.dashboard.showAllProjects"); // NOI18N
    }
    
}
