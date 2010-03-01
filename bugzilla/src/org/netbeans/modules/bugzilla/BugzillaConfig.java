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

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.api.NBBugzillaUtils;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.bugzilla.util.FileUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaConfig {

    private static BugzillaConfig instance = null;
    private static final String LAST_CHANGE_FROM    = "bugzilla.last_change_from";      // NOI18N // XXX
    private static final String REPO_ID           = "bugzilla.repository_";           // NOI18N
    private static final String QUERY_NAME          = "bugzilla.query_";                // NOI18N
    private static final String QUERY_REFRESH_INT   = "bugzilla.query_refresh";         // NOI18N
    private static final String QUERY_AUTO_REFRESH  = "bugzilla.query_auto_refresh_";   // NOI18N
    private static final String ISSUE_REFRESH_INT   = "bugzilla.issue_refresh";         // NOI18N
    private static final String DELIMITER           = "<=>";                            // NOI18N
    private static final String CHECK_UPDATES       = "jira.check_updates";         // NOI18N
    private static final String TASKLISTISSUES_STORAGE_FILE = "tasklistissues.data"; //NOI18N
    private static final Level LOG_LEVEL = BugzillaUtil.isAssertEnabled() ? Level.SEVERE : Level.INFO;

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

    private Preferences getPreferences() {
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
                getQueryKey(repository.getID(), query.getDisplayName()),
                query.getUrlParameters() + DELIMITER + /* skip query.getLastRefresh() + */ DELIMITER + query.isUrlDefined());
    }

    public void removeQuery(BugzillaRepository repository, BugzillaQuery query) {
        getPreferences().remove(getQueryKey(repository.getID(), query.getDisplayName()));
    }

    public BugzillaQuery getQuery(BugzillaRepository repository, String queryName) {
        String value = getStoredQuery(repository, queryName);
        if(value == null) {
            return null;
        }
        String[] values = value.split(DELIMITER);
        assert values.length >= 2;
        String urlParams = values[0];
//      skip  long lastRefresh = Long.parseLong(values[1]); // skip
        boolean urlDef = values.length > 2 ? Boolean.parseBoolean(values[2]) : false;
        return new BugzillaQuery(queryName, repository, urlParams, true, urlDef, true);
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

    public String[] getQueries(String repoID) {
        return getKeysWithPrefix(QUERY_NAME + repoID + DELIMITER);
    }

    public void putRepository(String repoID, BugzillaRepository repository) {
        String repoName = repository.getDisplayName();

        String user = repository.getUsername();

        String httpUser = repository.getHttpUsername();        
        String url = repository.getUrl();
        String shortNameEnabled = Boolean.toString(repository.isShortUsernamesEnabled());
        getPreferences().put(
                REPO_ID + repoID,
                url + DELIMITER +
                user + DELIMITER +
                "" + DELIMITER +                // NOI18N - skip password, will be saved via keyring
                httpUser + DELIMITER +
                "" + DELIMITER +                // NOI18N - skip password, will be saved via keyring
                shortNameEnabled + DELIMITER +
                repoName);
        
        
        String password = repository.getPassword();
        String httpPassword = repository.getHttpPassword();
        if(BugtrackingUtil.isNbRepository(repository)) {
            NBBugzillaUtils.saveNBUsername(user);
            String psswd = repository.getPassword();
            NBBugzillaUtils.saveNBPassword(psswd != null ? psswd.toCharArray() : null);
        } else {
            BugtrackingUtil.savePassword(password, null, user, url);
            BugtrackingUtil.savePassword(httpPassword, "http", httpUser, url); // NOI18N
        }
    }

    public BugzillaRepository getRepository(String repoID) {
        String repoString = getPreferences().get(REPO_ID + repoID, "");         // NOI18N
        if(repoString.equals("")) {                                             // NOI18N
            return null;
        }
        String[] values = repoString.split(DELIMITER);
        assert values.length == 3 || values.length == 6 || values.length == 7;
        String url = values[0];
        String user;
        String password;
        if(BugtrackingUtil.isNbRepository(url)) {
            user = NBBugzillaUtils.getNBUsername();
            char[] psswdArray = NBBugzillaUtils.getNBPassword();
            password = psswdArray != null ? new String(psswdArray) : null;
        } else {
            user = values[1];
            password = new String(BugtrackingUtil.readPassword(values[2], null, user, url));
        }
        String httpUser = values.length > 3 ? values[3] : null;
        String httpPassword = new String(values.length > 3 ? BugtrackingUtil.readPassword(values[4], "http", httpUser, url) : null); // NOI18N
        boolean shortNameEnabled = false;
        if (values.length > 5) {
            shortNameEnabled = Boolean.parseBoolean(values[5]);
        }
        String name;
        if (values.length > 6) {
            name = values[6];
        } else {
            name = repoID;
        }
        BugzillaRepository repo = new BugzillaRepository(repoID, name, url, user, password, httpUser, httpPassword, shortNameEnabled);

        // make sure tha scrambled password is removed
        if(!values[2].trim().equals("") || (values.length > 3 && !values[3].trim().equals(""))) {
            putRepository(repoID, repo); 
        }

        return repo;
    }

    public String[] getRepositories() {
        return getKeysWithPrefix(REPO_ID);
    }

    public void removeRepository(String id) {
        getPreferences().remove(REPO_ID + id);
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

    private String getQueryKey(String repositoryID, String queryName) {
        return QUERY_NAME + repositoryID + DELIMITER + queryName;
    }

    private String getStoredQuery(BugzillaRepository repository, String queryName) {
        String value = getPreferences().get(getQueryKey(repository.getID(), queryName), null);
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

    /**
     * Saves issue attributes permanently
     * @param issues
     */
    public void setTaskListIssues(HashMap<String, List<String>> issues) {
        Bugzilla.LOG.fine("setTaskListIssues: saving issues");              //NOI18N
        File f = new File(getNBConfigPath());
        f.mkdirs();
        if (!f.canWrite()) {
            Bugzilla.LOG.warning("setTaskListIssues: Cannot create perm storage"); //NOI18N
            return;
        }
        java.io.ObjectOutputStream out = null;
        File file = new File(f, TASKLISTISSUES_STORAGE_FILE + ".tmp");
        boolean success = false;
        try {
            // saving to a temp file
            out = new java.io.ObjectOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(file)));
            out.writeInt(issues.size());
            for (Map.Entry<String, List<String>> entry : issues.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeInt(entry.getValue().size());
                for (String issueAttributes : entry.getValue()) {
                    out.writeUTF(issueAttributes);
                }
            }
            success = true;
        } catch (IOException ex) {
            Bugzilla.LOG.log(LOG_LEVEL, null, ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        if (success) {
            success = false;
            // rename the temp file to the permanent one
            File newFile = new File(f, TASKLISTISSUES_STORAGE_FILE);
            try {
                FileUtils.renameFile(file, newFile);
            } catch (IOException ex) {
                Bugzilla.LOG.log(LOG_LEVEL, null, ex);
                success = false;
            }
        }
        if (!success) {
            Bugzilla.LOG.warning("setTaskListIssues: could not save issues"); //NOI18N
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
    }

    /**
     * Loads issues from a permanent storage
     * @return
     */
    public Map<String, List<String>> getTaskListIssues () {
        Bugzilla.LOG.fine("loadTaskListIssues: loading issues");            //NOI18N
        File f = new File(getNBConfigPath());
        java.io.ObjectInputStream ois = null;
        File file = new File(f, TASKLISTISSUES_STORAGE_FILE);
        if (!file.canRead()) {
            Bugzilla.LOG.fine("loadTaskListIssues: no saved data");         //NOI18N
            return Collections.emptyMap();
        }
        try {
            ois = new java.io.ObjectInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(file)));
            int size = ois.readInt();
            Bugzilla.LOG.fine("loadTaskListIssues: loading " + size + " records"); //NOI18N
            HashMap<String, List<String>> issuesPerRepo = new HashMap<String, List<String>>(size);
            while (size-- > 0) {
                String repoUrl = ois.readUTF();
                Bugzilla.LOG.fine("loadTaskListIssues: loading issues for " + repoUrl); //NOI18N
                int issueCount = ois.readInt();
                Bugzilla.LOG.fine("loadTaskListIssues: loading " + issueCount + " issues"); //NOI18N
                LinkedList<String> issues = new LinkedList<String>();
                while (issueCount-- > 0) {
                    issues.add(ois.readUTF());
                }
                issuesPerRepo.put(repoUrl, issues);
            }
            return issuesPerRepo;
        } catch (IOException ex) {
            Bugzilla.LOG.log(LOG_LEVEL, null, ex);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                }
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Returns the path for the Bugzilla configuration directory.
     *
     * @return the path
     *
     */
    private static String getNBConfigPath() {
        //T9Y - nb bugzilla confing should be changable
        String t9yNbConfigPath = System.getProperty("netbeans.t9y.bugzilla.nb.config.path"); //NOI18N
        if (t9yNbConfigPath != null && t9yNbConfigPath.length() > 0) {
            return t9yNbConfigPath;
        }
        String nbHome = System.getProperty("netbeans.user");            //NOI18N
        return nbHome + "/config/issue-tracking/org-netbeans-modules-bugzilla"; //NOI18N
    }
}
