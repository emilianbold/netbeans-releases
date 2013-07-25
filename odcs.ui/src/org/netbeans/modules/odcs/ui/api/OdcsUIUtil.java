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
package org.netbeans.modules.odcs.ui.api;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComboBox;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.api.ODCSManager;
import org.netbeans.modules.odcs.ui.ODCSServerProviderImpl;
import org.netbeans.modules.team.server.ui.common.AddInstanceAction;
import static org.netbeans.modules.odcs.ui.api.Bundle.*;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.netbeans.modules.team.server.api.TeamUIUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public final class OdcsUIUtil {
    
    private static final String USG_ODCS = "USG_ODCS"; // NOI18N
    /** To avoid logging same params more than once in a session. Expecting
     * less than 20 possible combinations at max. */
    private static Set<String> loggedParams = Collections.synchronizedSet(new HashSet<String>());
    private static Logger metricsLogger;
    private OdcsUIUtil() { }
    
    public static void addDashboardListener(ODCSServer server, PropertyChangeListener propertyChangeListener) {
        ODCSUiServer.forServer(server).getDashboard().addPropertyChangeListener(propertyChangeListener);
    }
    
    public static void removeDashboardListener(ODCSServer server, PropertyChangeListener propertyChangeListener) {
        ODCSUiServer.forServer(server).getDashboard().removePropertyChangeListener(propertyChangeListener);
    }
    
    @NbBundle.Messages("CTL_AddInstance=Add ODCS Server")
    public static Action createAddInstanceAction() {
        return new AddInstanceAction(ODCSServerProviderImpl.getDefault(), CTL_AddInstance());
    }
    
    public static JComboBox createTeamCombo() {
        return TeamUIUtils.createTeamCombo(ODCSServerProviderImpl.getDefault(), true);
    }

    public static void logODCSUsage (Object... parameters) {
        String paramStr = getParamString(parameters);
        if (loggedParams.add(paramStr)) {
            // not logged in this session yet
            if (metricsLogger == null) {
                metricsLogger = Logger.getLogger("org.netbeans.ui.metrics.odcs"); //NOI18N
            }
            LogRecord rec = new LogRecord(Level.INFO, USG_ODCS);
            rec.setParameters(parameters);
            rec.setLoggerName(metricsLogger.getName());
            metricsLogger.log(rec);
        }
    }

    /**
     * @return
     */
    public static boolean showLogin () {
        return showKenaiLogin() != null;
    }

    /**
     *
     * @return
     */
    public static ODCSServer showKenaiLogin () {
        for (ODCSServer s: ODCSManager.getDefault().getServers()) {
            if (!s.isLoggedIn()) {
                return showServerLogin(s);
            }
        }
        return showServerLogin(null);
    }
    
    /**
     * Invokes login dialog
     * @param server
     * @return true, if user was successfully logged in
     */
    public static boolean showLogin (final ODCSServer server) {
        return showServerLogin(server) != null;
    }

    /**
     * Invokes login dialog
     * @param odcsServer
     * @return server instance, where user requested login, or null if login was
     * canceled
     */
    public static ODCSServer showServerLogin(final ODCSServer odcsServer) {
        TeamServer server = ODCSUiServer.forServer(odcsServer);
        server = org.netbeans.modules.team.server.api.TeamUIUtils.showLogin(server, false);
        return (server instanceof ODCSUiServer) ? ((ODCSUiServer) server).getServer() : null;
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
