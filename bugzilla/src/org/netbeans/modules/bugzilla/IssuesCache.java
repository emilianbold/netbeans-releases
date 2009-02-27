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

package org.netbeans.modules.bugzilla;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;

/**
 *
 * @author Tomas Stupka
 */
public class IssuesCache {

    private Map<String, BugzillaIssue> issueMap;
    private Map<String, IssueData> seenIssueData; // XXX merge with issue XXX rename - isn't related only to seen 

    private Map<Query, String> queryIssues;
    private BugzillaRepository repository;

    IssuesCache(BugzillaRepository repo) {
        repository = repo;
    }

    public synchronized BugzillaIssue setIssueData(TaskData taskData) {
        String id = BugzillaIssue.getID(taskData);
        BugzillaIssue issue = getCache().get(id);
        Map<String, String> oldAttr = null;
        int oldStatus;
        if(issue == null) {
            issue = new BugzillaIssue(taskData, repository);
            oldAttr = getSeenAttributes(id);
            oldStatus = Query.ISSUE_STATUS_NEW;
        } else {

            issue.setTaskData(taskData);
            IssueData data = getSeenIssueData().get(id);
            assert data != null;
            oldAttr = data.attributes;
            oldStatus = data.status;
        }
        getCache().put(id, issue);

        if(oldAttr != null) {
            boolean seen = "1".equals(oldAttr.get("seen"));
            if(seen) {
                int status;
                if(isChanged(oldAttr, issue.getAttributes())) {
                    status = Query.ISSUE_STATUS_MODIFIED;
                    seen = false; // write
                    try {
                        IssueStorage.getInstance().storeIssue(repository.getUrl(), id, seen, oldAttr);
                    } catch (IOException ex) {
                        Bugzilla.LOG.log(Level.SEVERE, null, ex);
                    }
                    getSeenIssueData().put(id, new IssueData(oldAttr, status));
                } else {
                    getSeenIssueData().put(id, new IssueData(oldAttr, oldStatus));
                }
            } else {
                int status;
                if(isChanged(oldAttr, issue.getAttributes())) {
                    status = Query.ISSUE_STATUS_MODIFIED;
                } else {
                    status = Query.ISSUE_STATUS_NEW;
                }
                getSeenIssueData().put(id, new IssueData(null, status));
            }
            issue.setSeen(seen, false); // XXX true triggers already written attr
        } else {
            getSeenIssueData().put(id, new IssueData(null, oldStatus));
        }
        
        return issue;
    }

    public synchronized void setSeen(final String id, final boolean seen, Map<String, String> attr) {

        BugzillaIssue issue = getCache().get(id);
        assert issue != null;

        final Map<String, String> newAttr;
        if(seen) {
            newAttr = attr;
        } else { 
            newAttr = null;
        }

        final IssueData oldData = getSeenIssueData().get(id);
        assert oldData != null;
        getSeenIssueData().put(id, new IssueData(newAttr, oldData.status));

        try {
            IssueStorage.getInstance().storeIssue(repository.getUrl(), id, seen, newAttr);
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }

    }

    public synchronized BugzillaIssue getIssue(String id) {
        return getCache().get(id);
    }

    public int getStatus(String id) {
        BugzillaIssue issue = getIssue(id);
        if(issue == null) {
            return Query.ISSUE_STATUS_UNKNOWN;
        }
        if(issue.wasSeen()) {
            return Query.ISSUE_STATUS_SEEN;
        }
        IssueData data = getSeenIssueData().get(id);
        assert data != null;
        return data.status;
    }

    public void storeQuery(BugzillaQuery query, String[] ids) {
        try {
            IssueStorage.getInstance().storeQuery(repository.getUrl(), query.getDisplayName(), ids);
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
    }

    public List<String> readQuery(BugzillaQuery query) {
        try {
            return IssueStorage.getInstance().readQuery(repository.getUrl(), query.getDisplayName());
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
        return new ArrayList<String>(0);
    }

//    private int computeStatus(boolean seen, IssueData oldData, Map<String, String> attrNew) {
//        if(oldData == null || oldData.attributes == null) {
//            return Query.ISSUE_STATUS_NEW;
//        }
//        if(isChanged(oldData.attributes, attrNew)) {
//            return Query.ISSUE_STATUS_MODIFIED;
//        }
//        if(seen) {
//            return Query.ISSUE_STATUS_UPTODATE;
//        } else {
//            return Query.ISSUE_STATUS_NEW;
//        }
//    }

    private Map<String, BugzillaIssue> getCache() {
        if(issueMap == null) {
            issueMap = new HashMap<String, BugzillaIssue>();
        }
        return issueMap;
    }

    public Map<String, String> getSeenAttributes(String id) {
        Map<String, String> oldAttr = null;
        // XXX init storage for repo
        try {
            oldAttr = IssueStorage.getInstance().readIssue(repository.getUrl(), id);
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
        return oldAttr;
    }

    private Map<String, IssueData> getSeenIssueData() {
        if(seenIssueData == null) {
            seenIssueData = new HashMap<String, IssueData>();
        }
        return seenIssueData;
    }

    private Map<Query, String> getQueryIssues() {
        if(queryIssues == null) {
            queryIssues = new HashMap<Query, String>();
        }
        return queryIssues;
    }

    private class IssueData {
        private Map<String, String> attributes;
        private int status;
        public IssueData(Map<String, String> attributes, int status) {
            this.attributes = attributes;
            this.status = status;
        }
    }

    private boolean isChanged(Map<String, String> attrOld, Map<String, String> attrNew) {
        for (Entry<String, String> e : attrOld.entrySet()) {
            String v = attrNew.get(e.getKey());
            if(v == null && e.getValue() == null) continue;
            if(v == null) return true;
            if(!e.getKey().equals("seen") && !v.equals(e.getValue())) return true; // XXX get rid of this                 if(isChanged(oldAttr, issue.getAttributes())) {

        }
        return false;
    }

}
