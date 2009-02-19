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

package org.netbeans.modules.bugzilla.query;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.bugzilla.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaQuery extends Query {

    private String name;
    private final BugzillaRepository repository;
    private QueryController controller;
//    private final Map<String, IssueData> issues = new HashMap<String, IssueData>();
    private final List<String> issues = new ArrayList<String>();
    private final Set<String> obsoleteIssues = new HashSet<String>();

    private String urlParameters;
    private boolean firstRun = true;

    public BugzillaQuery(BugzillaRepository repository) {
        super();
        this.repository = repository;
    }

    public BugzillaQuery(String name, BugzillaRepository repository, String urlParameters, long lastRefresh) {
        this(repository);
        this.name = name;
        this.urlParameters = urlParameters;
        this.setLastRefresh(lastRefresh);
        setSaved(true);
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getTooltip() {
        return name + " - " + repository.getDisplayName();
    }

    @Override
    public QueryController getController() {
        if (controller == null) {
            controller = new QueryController(repository, this, urlParameters);
        }
        return controller;
    }


    @Override
    public ColumnDescriptor[] getColumnDescriptors() {
        return BugzillaIssue.getColumnDescriptors();
    }

    @Override
    public void refresh() {
        // XXX do not refresh until populate!

        assert urlParameters != null;
        assert !SwingUtilities.isEventDispatchThread();

        executeQuery(new Runnable() {
            public void run() {

//                preQuery();
                if(isSaved()) {
                    List<String> ids;
                    if(firstRun) {
                        firstRun = false;
                        ids = repository.getCache().readQuery(BugzillaQuery.this);
                    } else {
                        repository.getCache().storeQuery(BugzillaQuery.this, issues.toArray(new String[issues.size()]));
                        ids = issues;
                    }

                    obsoleteIssues.clear();
                    obsoleteIssues.addAll(ids);
                    ids.clear();
                }

                StringBuffer url = new StringBuffer();
                url.append(BugzillaConstants.URL_ADVANCED_BUG_LIST);
                url.append(urlParameters);
                final IssuesCache cache = repository.getCache();
                TaskDataCollector collector = new TaskDataCollector() {
                    public void accept(TaskData taskData) {

                        // get id
                        String id = BugzillaIssue.getID(taskData);

                        BugzillaIssue issue = cache.setIssueData(taskData);
                        issues.add(id);
                        obsoleteIssues.remove(id);
                        fireNotifyData(issue); // XXX - !!! triggers getIssues()
                    }
                };

                final TaskRepository taskRepository = repository.getTaskRepository();
                BugzillaUtil.performQuery(taskRepository, url.toString(), collector);

                if(isSaved()) {
                    for (String id : obsoleteIssues) {
                        Issue issue = repository.getIssue(id);
                        fireNotifyData(issue); // XXX - !!! triggers getIssues()
                    }
                }

            }
        });


    }

    @Override
    public int getIssueStatus(Issue issue) {
        String id = issue.getID();
        if(obsoleteIssues.contains(id)) {
            return Query.ISSUE_STATUS_OBSOLETE;
        } else {
            return repository.getCache().getStatus(id);
        }
//        IssueData data = issues.get(issue.getID());
//        if(data != null) {
//            return data.status;
//        } else {
//            throw new IllegalStateException("No IssueData for issue: " + issue.getID());
//            //return 0; // XXX WARNING
//        }
    }

    int getSize() {
        return issues.size();
    }

    void refresh(String urlParameters) {
        assert urlParameters != null;
        this.urlParameters = urlParameters;
        refresh();
    }

    public String getUrlParameters() {
        return getController().getUrlParameters();
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setSaved(boolean saved) {
        super.setSaved(saved);
    }

    @Override
    public void fireQuerySaved() {
        super.fireQuerySaved();
    }

    @Override
    public void fireQueryRemoved() {
        super.fireQueryRemoved();
    }

    @Override
    public Issue[] getIssues(int includeStatus) {
        if (issues == null) {
            return new Issue[0];
        }
        List<String> ids = new ArrayList<String>();
        synchronized (issues) {
//            for (Entry<String, IssueData> e : issues.entrySet()) {
//                if((e.getValue().status & includeStatus) != 0) {
//                    ids.add(e.getKey());
//                }
//            }
            ids.addAll(issues);
        }

        // XXX move to cache and sync
        IssuesCache cache = repository.getCache();
        List<Issue> l = new ArrayList<Issue>();
        for (String id : ids) {
            int status = cache.getStatus(id);
            if((status & includeStatus) != 0) {
                l.add(cache.getIssue(id));
            }
        }
        return l.toArray(new Issue[l.size()]);
    }

    // XXX add to Query
    private synchronized void preQuery() {
        if(!isSaved()) {
            return;
        }

        repository.getCache().storeQuery(this, issues.toArray(new String[issues.size()]));
        obsoleteIssues.clear();
        obsoleteIssues.addAll(issues);
        issues.clear();

//        Map<String, Map<String, String>> m;
//        if(firstRun) {
//            firstRun = false;
//            try {
//                // XXX read and lock synchronized on repo
//                m = IssueStorage.getInstance().readQuery(repository.getUrl(), getDisplayName());
//            } catch (FileNotFoundException ex) {
//                // isn't in store yet?
//                Bugzilla.LOG.log(Level.FINE, null, ex);
//                m = new HashMap<String, Map<String, String>>(); // try to make the best of it
//            } catch (IOException ex) {
//                Bugzilla.LOG.log(Level.SEVERE, null, ex);
//                m = new HashMap<String, Map<String, String>>(); // try to make the best of it
//            }
//        } else {
//            m = new HashMap<String, Map<String, String>>();
//            Iterator<Entry<String, IssueData>> i = issues.entrySet().iterator();
//            while(i.hasNext()) {
//                Entry<String, IssueData> e = i.next();
//                IssueData data = e.getValue();
//                if(data.status == Query.ISSUE_STATUS_OBSOLETE) {
//                    i.remove();
//                } else {
//                    String id = e.getKey();
//                    data.status = Query.ISSUE_STATUS_OBSOLETE;
//                    Issue issue = repository.getCachedIssue(id);
//                    if(issue != null) {
//                        Map<String, String> attr = issue.getAttributes();
//                        if(attr != null) {
//                            m.put(id, attr);
//                        }
//                    } else {
//                        // XXX warning
//                    }
//                }
//            }
//        }
//        try {
//            // XXX read and lock synchronized on repo
//            IssueStorage.getInstance().storeQuery(repository.getUrl(), getDisplayName(), m);
//        } catch (IOException ex) {
//            Bugzilla.LOG.log(Level.SEVERE, null, ex);
//        }
    }
}
