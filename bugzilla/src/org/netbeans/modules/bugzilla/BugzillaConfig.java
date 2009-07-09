/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla;

import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.openide.util.ImageUtilities;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaConfig {

    private static BugzillaConfig instance = null;
    private static final String LAST_CHANGE_FROM    = "bugzilla.last_change_from";      // NOI18N // XXX
    private static final String REPO_NAME           = "bugzilla.repository_";           // NOI18N
    private static final String QUERY_NAME          = "bugzilla.query_";                // NOI18N
    private static final String QUERY_REFRESH_INT   = "bugzilla.query_refresh";         // NOI18N
    private static final String QUERY_AUTO_REFRESH  = "bugzilla.query_auto_refresh_";   // NOI18N
    private static final String ISSUE_REFRESH_INT   = "bugzilla.issue_refresh";         // NOI18N
    private static final String DELIMITER           = "<=>";                            // NOI18N
    private static final String CHECK_UPDATES       = "jira.check_updates";         // NOI18N

    public static final int DEFAULT_QUERY_REFRESH = 30;
    public static final int DEFAULT_ISSUE_REFRESH = 15;
    private Map<String, Icon> priorityIcons;

    private BugzillaConfig() { }

    public static BugzillaConfig getInstance() {
        if(instance == null) {
            instance = new BugzillaConfig();
        }
        return instance;
    }

    public Preferences getPreferences() {
        return NbPreferences.forModule(BugzillaConfig.class);
    }

    public void setQueryRefreshInterval(int i) {
        getPreferences().putInt(QUERY_REFRESH_INT, i);
    }

    public void setIssueRefreshInterval(int i) {
        getPreferences().putInt(ISSUE_REFRESH_INT, i);
    }

    public void setQueryAutoRefresh(String queryName, boolean refresh) {
        getPreferences().putBoolean(QUERY_AUTO_REFRESH + queryName, refresh);
    }

    public void setCheckUpdates(boolean bl) {
        getPreferences().putBoolean(CHECK_UPDATES, bl);
    }

    public int getQueryRefreshInterval() {
        return getPreferences().getInt(QUERY_REFRESH_INT, DEFAULT_QUERY_REFRESH);
    }

    public int getIssueRefreshInterval() {
        return getPreferences().getInt(ISSUE_REFRESH_INT, DEFAULT_ISSUE_REFRESH);
    }

    public boolean getQueryAutoRefresh(String queryName) {
        return getPreferences().getBoolean(QUERY_AUTO_REFRESH + queryName, false);
    }

    public boolean getCheckUpdates() {
        return getPreferences().getBoolean(CHECK_UPDATES, true);
    }

    public void putQuery(BugzillaRepository repository, BugzillaQuery query) {
        getPreferences().put(
                getQueryKey(repository.getDisplayName(), query.getDisplayName()),
                query.getUrlParameters() + DELIMITER + query.getLastRefresh() + DELIMITER + query.isUrlDefined());
    }

    public void removeQuery(BugzillaRepository repository, BugzillaQuery query) {
        getPreferences().remove(getQueryKey(repository.getDisplayName(), query.getDisplayName()));
    }

    public BugzillaQuery getQuery(BugzillaRepository repository, String queryName) {
        String value = getStoredQuery(repository, queryName);
        if(value == null) {
            return null;
        }
        String[] values = value.split(DELIMITER);
        assert values.length >= 2;
        String urlParams = values[0];
        long lastRefresh = Long.parseLong(values[1]);
        boolean urlDef = values.length > 2 ? Boolean.parseBoolean(values[2]) : false;
        return new BugzillaQuery(queryName, repository, urlParams, lastRefresh, urlDef);
    }

    public String getUrlParams(BugzillaRepository repository, String queryName) {
        String value = getStoredQuery(repository, queryName);
        if(value == null) {
            return null;
        }
        String[] values = value.split(DELIMITER);
        assert values.length >= 2;
        return values[0];
    }

    public String[] getQueries(String repoName) {        
        return getKeysWithPrefix(QUERY_NAME + repoName + DELIMITER);
    }

    public void putRepository(String repoName, BugzillaRepository repository) {
        String user = repository.getUsername();
        String password = BugtrackingUtil.scramble(repository.getPassword());

        String httpUser = repository.getHttpUsername();
        String httpPassword = BugtrackingUtil.scramble(repository.getHttpPassword());
        String url = repository.getUrl();
        String shortNameEnabled = Boolean.toString(repository.isShortUsernamesEnabled());
        getPreferences().put(
                REPO_NAME + repoName,
                url + DELIMITER +
                user + DELIMITER +
                password + DELIMITER +
                httpUser + DELIMITER +
                httpPassword + DELIMITER +
                shortNameEnabled);
    }

    public BugzillaRepository getRepository(String repoName) {
        String repoString = getPreferences().get(REPO_NAME + repoName, "");     // NOI18N
        if(repoString.equals("")) {                                             // NOI18N
            return null;
        }
        String[] values = repoString.split(DELIMITER);
        assert values.length == 3 || values.length == 5 || values.length == 6;
        String url = values[0];
        String user = values[1];
        String password = BugtrackingUtil.descramble(values[2]);
        String httpUser = values.length > 3 ? values[3] : null;
        String httpPassword = values.length > 3 ? BugtrackingUtil.descramble(values[4]) : null;
        boolean shortNameEnabled = false;
        if (values.length > 5) {
            shortNameEnabled = Boolean.parseBoolean(values[5]);
        }

        return new BugzillaRepository(repoName, url, user, password, httpUser, httpPassword, shortNameEnabled);
    }

    public String[] getRepositories() {
        return getKeysWithPrefix(REPO_NAME);
    }

    public void removeRepository(String name) {
        getPreferences().remove(REPO_NAME + name);
    }

    private String[] getKeysWithPrefix(String prefix) {
        String[] keys = null;
        try {
            keys = getPreferences().keys();
        } catch (BackingStoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex); // XXX
        }
        if (keys == null || keys.length == 0) {
            return new String[0];
        }
        List<String> ret = new ArrayList<String>();
        for (String key : keys) {
            if (key.startsWith(prefix)) {
                ret.add(key.substring(prefix.length()));
            }
        }
        return ret.toArray(new String[ret.size()]);
    }

    private String getQueryKey(String repositoryName, String queryName) {
        return QUERY_NAME + repositoryName + DELIMITER + queryName;
    }

    private String getStoredQuery(BugzillaRepository repository, String queryName) {
        String value = getPreferences().get(getQueryKey(repository.getDisplayName(), queryName), null);
        return value;
    }

    public void setLastChangeFrom(String value) {
        getPreferences().put(LAST_CHANGE_FROM, value);
    }

    public String getLastChangeFrom() {
        return getPreferences().get(LAST_CHANGE_FROM, "");                      // NOI18N
    }

    public Icon getPriorityIcon(String priority) {
        if(priorityIcons == null) {
            priorityIcons = new HashMap<String, Icon>();
            priorityIcons.put("P1", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p1.png", true)); // NOI18N
            priorityIcons.put("P2", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p2.png", true)); // NOI18N
            priorityIcons.put("P3", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p3.png", true)); // NOI18N
            priorityIcons.put("P4", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p4.png", true)); // NOI18N
            priorityIcons.put("P5", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p5.png", true)); // NOI18N
        }
        return priorityIcons.get(priority);
    }
}
