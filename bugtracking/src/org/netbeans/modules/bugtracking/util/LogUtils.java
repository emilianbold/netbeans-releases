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
package org.netbeans.modules.bugtracking.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;

/**
 *
 * @author Tomas Stupka
 */
public final class LogUtils {
    /**
     * Metrics logger
     */
    private static Logger METRICS_LOG = Logger.getLogger("org.netbeans.ui.metrics.bugtracking"); // NOI18N

    /**
     * The automatic refresh was set on or off.<br>
     * Parameters:
     * <ol>
     *  <li>connector name : String
     *  <li>is on : Boolean
     * </ol>
     */
    public static final String USG_BUGTRACKING_AUTOMATIC_REFRESH = "USG_BUGTRACKING_AUTOMATIC_REFRESH"; // NOI18N

    /**
     * A query was refreshed.<br>
     * Parameters:
     * <ol>
     *  <li>connector name : String
     *  <li>query name : String
     *  <li>issues count : Integer
     *  <li>is a kenai query : Boolean
     *  <li>is a automatic refresh : Boolean
     * </ol>
     */
    public static final String USG_BUGTRACKING_QUERY             = "USG_BUGTRACKING_QUERY"; // NOI18N

    private static final String USG_ISSUE_TRACKING = "USG_ISSUE_TRACKING"; // NOI18N

    private static Set<String> loggedParams; // to avoid logging same params more than once in a session

    public static void logQueryEvent(String connector, String name, int count, boolean isKenai, boolean isAutoRefresh) {
        name = obfuscateQueryName(name);
        logBugtrackingEvents(USG_BUGTRACKING_QUERY, new Object[] {connector, name, count, isKenai, isAutoRefresh} );
    }

    public static void logAutoRefreshEvent(String connector, String queryName, boolean isKenai, boolean on) {
        queryName = obfuscateQueryName(queryName);
        logBugtrackingEvents(USG_BUGTRACKING_AUTOMATIC_REFRESH, new Object[] {connector, queryName, isKenai, on} );
    }
    
    public static synchronized void logBugtrackingUsage(Repository repository, String operation) {
        if (repository == null) {
            return;
        }
        String btType = getBugtrackingType(APIAccessor.IMPL.getImpl(repository).getProvider());
        if (btType == null) {
            return;
        }
        // log Kenai usage
        if (KenaiUtil.isKenai(repository)) {
            KenaiUtil.logKenaiUsage("ISSUE_TRACKING", btType); // NOI18N
        }
        if (operation == null) {
            return;
        }
        // log general bugtracking usage
        String paramStr = getParamString(btType, operation);
        if (loggedParams == null || !loggedParams.contains(paramStr)) {
            // not logged in this session yet
            LogRecord rec = new LogRecord(Level.INFO, USG_ISSUE_TRACKING);
            rec.setParameters(new Object[] { btType, operation });
            rec.setLoggerName(METRICS_LOG.getName());
            METRICS_LOG.log(rec);

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

    private static String getBugtrackingType(RepositoryProvider repository) {
        // XXX hack: there's no clean way to determine the type of bugtracking
        // from RepositoryProvider (need BugtrackingConnector.getDisplayName)
        String clsName = repository.getClass().getName();
        if (clsName.contains(".bugzilla.")) { // NOI18N
            return "Bugzilla"; // NOI18N
        }
        if (clsName.contains(".jira.")) { // NOI18N
            return "Jira"; // NOI18N
        }
        return null;
    }

    /**
     * Logs bugtracking events
     *
     * @param key - the events key
     * @param parameters - the parameters for the given event
     */
    private static void logBugtrackingEvents(String key, Object[] parameters) {
        LogRecord rec = new LogRecord(Level.INFO, key);
        rec.setParameters(parameters);
        rec.setLoggerName(METRICS_LOG.getName());
        METRICS_LOG.log(rec);
    }    
    
    private static String obfuscateQueryName(String name) {
        if (name == null) {
            name = "Find Issues"; // NOI18N
        } else {
            name = getMD5(name);
        }
        return name;
    }
    
    private static String getMD5(String name) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");                          // NOI18N
        } catch (NoSuchAlgorithmException e) {
            // should not happen
            return null;
        }
        digest.update(name.getBytes());
        byte[] hash = digest.digest();
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(hash[i] & 0x000000FF);
            if(hex.length()==1) {
                hex = "0" + hex;                                                // NOI18N
            }
            ret.append(hex);
        }
        return ret.toString();
    }    
    
}
