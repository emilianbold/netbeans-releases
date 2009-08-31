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

package org.netbeans.modules.jira.repository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.query.JiraQuery;
import org.netbeans.modules.jira.util.FileUtils;
import org.netbeans.modules.jira.util.JiraUtils;

/**
 *
 * @author Ondra Vrabec
 */
public class JiraStorageManager {
    private static JiraStorageManager instance;

    private final Object QUERY_LOCK = new Object();
    private HashMap<String, JiraQueryData> queriesData;
    private static final String QUERY_DELIMITER           = "<=>";      //NOI18N
    private static final String QUERIES_STORAGE_FILE = "queries.data";  //NOI18N
    private static final String TASKLISTISSUES_STORAGE_FILE = "tasklistissues.data"; //NOI18N
    private static final Level LOG_LEVEL = JiraUtils.isAssertEnabled() ? Level.SEVERE : Level.INFO;

    private JiraStorageManager () {

    }
    
    public static JiraStorageManager getInstance() {
        if (instance == null) {
            instance = new JiraStorageManager();
        }
        return instance;
    }

    /**
     * MAY access IO (on the first access)
     * @param repository
     * @param query
     */
    void putQuery(JiraRepository repository, JiraQuery query) {
        getCachedQueries().put(getQueryKey(repository.getID(), query.getDisplayName()), new JiraQueryData(query));
    }

    private JiraQuery createQuery(JiraRepository repository, JiraQueryData data) {
        assert data != null;
        return new JiraQuery(data.getQueryName(), repository, data.getFilterDefinition());
    }

    private HashMap<String, JiraQueryData> getCachedQueries () {
        synchronized (QUERY_LOCK) {
            if (queriesData == null) {
                loadQueries();
            }
        }
        return queriesData;
    }

     /**
     * Removes a query with the given queryName.
     * MAY access IO (on the first access)
     * @param repository
     * @param queryName
     */
    void removeQuery(JiraRepository repository, JiraQuery query) {
        getCachedQueries().remove(getQueryKey(repository.getID(), query.getDisplayName()));
    }

     /**
     * Returns a set of queries registered with the given repository name
     * MAY access IO (on the first access)
     * @param repository a repository which's queries will be returned
     */
    HashSet<Query> getQueries (JiraRepository repository) {
        HashSet<Query> queries = new HashSet<Query>(10);
        for (Entry<String, JiraQueryData> e : getCachedQueries().entrySet()) {
            if (e.getKey().startsWith(repository.getID() + QUERY_DELIMITER)) {
                queries.add(createQuery(repository, e.getValue()));
            }
        }
        return queries;
    }

    private void loadQueries () {
        Jira.LOG.fine("loadQueries: loading queries");                   //NOI18N

        File f = new File(getNBConfigPath());
        try {
            ObjectInputStream ois = null;
            File file = new File(f, QUERIES_STORAGE_FILE);
            if (!file.canRead()) {
                Jira.LOG.info("loadQueries: no saved data");             //NOI18N
                return;
            }
            try {
                ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
                String version = ois.readUTF();
                if (!JiraQueryData.VERSION.equals(version)) {
                    Jira.LOG.info("loadQueries: old data format: " + version); //NOI18N
                    return;
                }
                int size = ois.readInt();
                Jira.LOG.fine("loadQueries: loading " + size + " queries"); //NOI18N
                queriesData = new HashMap<String, JiraQueryData>(size + 5);
                while (size-- > 0) {
                    String queryIdent = ois.readUTF();
                    Jira.LOG.fine("loadQueries: loading data for " + queryIdent); //NOI18N
                    JiraQueryData data = (JiraQueryData) ois.readObject();
                    queriesData.put(queryIdent, data);
                }
            } catch (IOException ex) {
                Jira.LOG.log(LOG_LEVEL, null, ex);
            } catch (ClassNotFoundException ex) {
                Jira.LOG.log(LOG_LEVEL, null, ex);
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                    }
                }
            }
        } finally {
            if (queriesData == null) {
                queriesData = new HashMap<String, JiraQueryData>(5);
            }
        }
    }

    private String getQueryKey(String repositoryName, String queryName) {
        return repositoryName + QUERY_DELIMITER + queryName;
    }

    private void storeQueries () {
        Jira.LOG.fine("storeQueries: saving queries");                  //NOI18N
        if (queriesData == null) {
            Jira.LOG.fine("storeQueries: no data loaded, no data saved"); //NOI18N
            return;
        }
        File f = new File(getNBConfigPath());
        f.mkdirs();
        if (!f.canWrite()) {
            Jira.LOG.warning("storeQueries: Cannot create perm storage"); //NOI18N
            return;
        }
        ObjectOutputStream out = null;
        File file = new File(f, QUERIES_STORAGE_FILE + ".tmp");
        boolean success = false;
        try {
            // saving to a temp file
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            out.writeUTF(JiraQueryData.VERSION);
            out.writeInt(queriesData.size());
            for (Entry<String, JiraQueryData> entry : queriesData.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeObject(entry.getValue());
            }
            success = true;
        } catch (IOException ex) {
            Jira.LOG.log(LOG_LEVEL, null, ex);
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
            File newFile = new File(f, QUERIES_STORAGE_FILE);
            try {
                FileUtils.renameFile(file, newFile);
            } catch (IOException ex) {
                Jira.LOG.log(LOG_LEVEL, null, ex);
                success = false;
            }
        }
        if (!success) {
            file.deleteOnExit();
        }
    }

    /**
     * Returns the path for the Jira configuration directory.
     *
     * @return the path
     *
     */
    private static String getNBConfigPath() {
        //T9Y - nb jira confing should be changable
        String t9yNbConfigPath = System.getProperty("netbeans.t9y.jira.nb.config.path"); //NOI18N
        if (t9yNbConfigPath != null && t9yNbConfigPath.length() > 0) {
            return t9yNbConfigPath;
        }
        String nbHome = System.getProperty("netbeans.user");            //NOI18N
        return nbHome + "/config/jira/";                                //NOI18N
    }

    public void shutdown() {
        storeQueries();
    }

    /**
     * Saves issue attributes permanently
     * @param issues
     */
    public void setTaskListIssues(HashMap<String, List<String>> issues) {
        Jira.LOG.fine("setTaskListIssues: saving issues");              //NOI18N
        File f = new File(getNBConfigPath());
        f.mkdirs();
        if (!f.canWrite()) {
            Jira.LOG.warning("setTaskListIssues: Cannot create perm storage"); //NOI18N
            return;
        }
        ObjectOutputStream out = null;
        File file = new File(f, TASKLISTISSUES_STORAGE_FILE + ".tmp");
        boolean success = false;
        try {
            // saving to a temp file
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            out.writeInt(issues.size());
            for (Entry<String, List<String>> entry : issues.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeInt(entry.getValue().size());
                for (String issueAttributes : entry.getValue()) {
                    out.writeUTF(issueAttributes);
                }
            }
            success = true;
        } catch (IOException ex) {
            Jira.LOG.log(LOG_LEVEL, null, ex);
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
                Jira.LOG.log(LOG_LEVEL, null, ex);
                success = false;
            }
        }
        if (!success) {
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
        Jira.LOG.fine("loadTaskListIssues: loading issues");            //NOI18N
        File f = new File(getNBConfigPath());
        ObjectInputStream ois = null;
        File file = new File(f, TASKLISTISSUES_STORAGE_FILE);
        if (!file.canRead()) {
            Jira.LOG.fine("loadTaskListIssues: no saved data");         //NOI18N
            return Collections.emptyMap();
        }
        try {
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            int size = ois.readInt();
            Jira.LOG.fine("loadTaskListIssues: loading " + size + " records"); //NOI18N
            HashMap<String, List<String>> issuesPerRepo = new HashMap<String, List<String>>(size);
            while (size-- > 0) {
                String repoUrl = ois.readUTF();
                Jira.LOG.fine("loadTaskListIssues: loading issues for " + repoUrl); //NOI18N
                int issueCount = ois.readInt();
                Jira.LOG.fine("loadTaskListIssues: loading " + issueCount + " issues"); //NOI18N
                LinkedList<String> issues = new LinkedList<String>();
                while (issueCount-- > 0) {
                    issues.add(ois.readUTF());
                }
                issuesPerRepo.put(repoUrl, issues);
            }
            return issuesPerRepo;
        } catch (IOException ex) {
            Jira.LOG.log(LOG_LEVEL, null, ex);
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

    private static class JiraQueryData implements Serializable {
        private static final String VERSION = "0.1";                    //NOI18N
        private String queryName;
        private final long lastRefresh;
        private final FilterDefinition filterDefinition;

        private JiraQueryData(JiraQuery query) {
            queryName = query.getDisplayName();
            lastRefresh = query.getLastRefresh();
            filterDefinition = query.getFilterDefinition();
        }

        public FilterDefinition getFilterDefinition() {
            return filterDefinition;
        }

        public long getLastRefresh() {
            return lastRefresh;
        }

        public String getQueryName () {
            return queryName;
        }
    }
}
