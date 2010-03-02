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

package org.netbeans.modules.jira;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.openide.util.ImageUtilities;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 */
public class JiraConfig {

    private static JiraConfig instance = null;
    private static final String LAST_CHANGE_FROM    = "jira.last_change_from";      // NOI18N // XXX
    private static final String REPO_NAME           = "jira.repository_";           // NOI18N
    private static final String QUERY_NAME          = "jira.query_";                // NOI18N
    private static final String QUERY_REFRESH_INT   = "jira.query_refresh";         // NOI18N
    private static final String QUERY_AUTO_REFRESH  = "jira.query_auto_refresh_";   // NOI18N
    private static final String ISSUE_REFRESH_INT   = "jira.issue_refresh";         // NOI18N
    private static final String CHECK_UPDATES       = "jira.check_updates";         // NOI18N

    private static final String DELIMITER           = "<=>";                        // NOI18N

    private HashMap<String, Icon> priorityIcons;
    private JiraConfig() { }

    public static JiraConfig getInstance() {
        if(instance == null) {
            instance = new JiraConfig();
        }
        return instance;
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(JiraConfig.class);
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
        return getPreferences().getInt(QUERY_REFRESH_INT, 30);
    }

    public int getIssueRefreshInterval() {
        return getPreferences().getInt(ISSUE_REFRESH_INT, 15);
    }

    public boolean getQueryAutoRefresh(String queryName) {
        return getPreferences().getBoolean(QUERY_AUTO_REFRESH + queryName, false);
    }

    public boolean getCheckUpdates() {
        return getPreferences().getBoolean(CHECK_UPDATES, true);
    }

    public String getUrlParams(JiraRepository repository, String queryName) {
//        String value = getStoredQuery(repository, queryName);
//        if(value == null) {
//            return null;
//        }
//        String[] values = value.split(DELIMITER);
//        assert values.length == 2;
//        return values[0];
        throw new UnsupportedOperationException();
    }

    public void putRepository(String repoID, JiraRepository repository) {
        String repoName = repository.getDisplayName();

        String user = repository.getUsername();        

        String httpUser = repository.getHttpUsername();        
        String url = repository.getUrl();
        getPreferences().put(
                REPO_NAME + repoID,
                url + DELIMITER +
                user + DELIMITER +
                "" + DELIMITER +          // NOI18N - skip password, will be saved via keyring
                httpUser + DELIMITER +
                "" + DELIMITER +          // NOI18N - skip http password, will be saved via keyring
                repoName);

        String password = repository.getPassword();
        String httpPassword = repository.getHttpPassword();
        BugtrackingUtil.savePassword(password, null, user, url);
        BugtrackingUtil.savePassword(httpPassword, "http", httpUser, url); // NOI18N

    }
    public JiraRepository getRepository(String repoID) {
        String repoString = getPreferences().get(REPO_NAME + repoID, "");     // NOI18N
        if(repoString.equals("")) {                                             // NOI18N
            return null;
        }
        String[] values = repoString.split(DELIMITER);
        String url = values[0];
        String user = values[1];
        String password = new String(BugtrackingUtil.readPassword(values[2], null, user, url));
        String httpUser = values.length > 3 ? values[3] : null;
        String httpPassword = new String(values.length > 3 ? BugtrackingUtil.readPassword(values[4], "http", httpUser, url) : null); // NOI18N
        String repoName;
        if(values.length > 5) {
            repoName = values[5];
        } else {
            repoName = repoID;
        }

        JiraRepository repo = new JiraRepository(repoID, repoName, url, user, password, httpUser, httpPassword);

        // make sure tha scrambled password is removed
        if(!values[2].trim().equals("") || (values.length > 3 && !values[3].trim().equals(""))) {
            putRepository(repoID, repo);
        }

        return repo;
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
            Jira.LOG.log(Level.SEVERE, null, ex); // XXX
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
//
//    private String getStoredQuery(JiraRepository repository, String queryName) {
//        String value = getPreferences().get(getQueryKey(repository.getDisplayName(), queryName), null);
//        return value;
//    }

    public void setLastChangeFrom(String value) {
        getPreferences().put(LAST_CHANGE_FROM, value);
    }

    public String getLastChangeFrom() {
        return getPreferences().get(LAST_CHANGE_FROM, "");                      // NOI18N
    }

    public Icon getPriorityIcon(String priorityId) {
        if(priorityIcons == null) {
            priorityIcons = new HashMap<String, Icon>();
            priorityIcons.put(Priority.BLOCKER_ID, ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/blocker.png", true));   // NOI18N
            priorityIcons.put(Priority.CRITICAL_ID, ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/critical.png", true)); // NOI18N
            priorityIcons.put(Priority.MAJOR_ID, ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/major.png", true));       // NOI18N
            priorityIcons.put(Priority.MINOR_ID, ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/minor.png", true));       // NOI18N
            priorityIcons.put(Priority.TRIVIAL_ID, ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/trivial.png", true));   // NOI18N
        }
        return priorityIcons.get(priorityId);
    }
}
