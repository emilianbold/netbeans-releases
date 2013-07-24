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

package org.netbeans.modules.team.server.api;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.modules.team.server.LoginTask;
import org.netbeans.modules.team.server.TeamServerCombo;
import org.netbeans.modules.team.server.TeamServerTopComponent;
import org.netbeans.modules.team.server.LoginPanel;
import org.netbeans.modules.team.server.TeamView;
import org.netbeans.modules.team.server.api.TeamServerManager;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.netbeans.modules.team.server.ui.spi.TeamServerProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * This class is not yet final. We be changed
 * @author Jan Becicka
 */
public final class TeamUIUtils {

    // Usage logging
    private static Logger metricsLogger;
    private static final String USG_TEAM = "USG_TEAM"; // NOI18N
    /** To avoid logging same params more than once in a session. Expecting
     * less than 20 possible combinations at max. */
    private static Set<String> loggedParams = Collections.synchronizedSet(new HashSet<String>());

    public static void waitStartupFinished() {
        LoginTask.waitStartupFinished();
    }
    
    private TeamUIUtils() {
    }

    public static TeamServer showLogin() {
        for (TeamServer k: TeamServerManager.getDefault().getTeamServers()) {
            if (k.getStatus()==TeamServer.Status.OFFLINE) {
                return showLogin(k, true);
            }
        }
        return showLogin(null, true);
    }

    /**
     * Invokes login dialog
     * @param server
     * @return true, if user was succesfully logged in
     */
    public static boolean showLogin(final TeamServer server) {
        return showLogin(server, true) != null;
    }

    /**
     * Invokes login dialog
     * @param preselectedServer
     * @param listAllProviders
     * @return team instance, where user requested login, or null if login was cancelled
     */
    public static TeamServer showLogin (final TeamServer preselectedServer, boolean listAllProviders) {
        return LoginPanel.login(preselectedServer, listAllProviders);
    }

    public static void logTeamUsage(Object... parameters) {
        String paramStr = getParamString(parameters);
        if (loggedParams.add(paramStr)) {
            // not logged in this session yet
            if (metricsLogger == null) {
                metricsLogger = Logger.getLogger("org.netbeans.ui.metrics.team"); // NOI18N
            }
            LogRecord rec = new LogRecord(Level.INFO, USG_TEAM);
            rec.setParameters(parameters);
            rec.setLoggerName(metricsLogger.getName());
            metricsLogger.log(rec);
        }
    }
    
    public static void activateTeamDashboard () {
        TeamServerTopComponent serverTc = TeamServerTopComponent.findInstance();
        serverTc.open();
        serverTc.requestActive();
    }
    
    public static JComboBox createTeamCombo (TeamServerProvider forProvider, boolean alwaysVisible) {
        return new TeamServerCombo(forProvider, alwaysVisible);
    }
    
    public static void setSelectedServer (TeamServer teamServer) {
        if (TeamView.getInstance().getTeamServer() != teamServer) {
            TeamView.getInstance().setSelectedServer(teamServer);
        }
    }
    
    public static TeamServer getSelectedServer () {
        return TeamView.getInstance().getTeamServer();
    }

    public static File getDefaultRepoFolder() {
        File defaultDir = FileSystemView.getFileSystemView().getDefaultDirectory();
        if (defaultDir != null && defaultDir.exists() && defaultDir.isDirectory()) {
            String nbPrjDirName = NbBundle.getMessage(TeamUIUtils.class, "DIR_NetBeansProjects");
            File nbPrjDir = new File(defaultDir, nbPrjDirName);
            if (nbPrjDir.exists() && nbPrjDir.canWrite()) {
                return nbPrjDir;
            }
        }
        return FileUtil.normalizeFile(new File(System.getProperty("user.home")));
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

