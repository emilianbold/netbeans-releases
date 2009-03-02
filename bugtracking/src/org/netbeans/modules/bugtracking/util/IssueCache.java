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

package org.netbeans.modules.bugtracking.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.BugtrackingManager;

/**
 *
 * @author Tomas Stupka
 */
public abstract class IssueCache {

    private Map<String, IssueEntry> cache;

    private String nameSpace;

    public IssueCache(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    protected abstract Issue createIssue(TaskData taskData);

    protected abstract void setTaskData(Issue issue, TaskData taskData);

    public synchronized Issue setIssueData(String id, TaskData taskData) throws IOException {
        IssueEntry entry = getCache().get(id);
        if(entry == null) {
            entry = new IssueEntry();
            getCache().put(id, entry);

            entry.issue = createIssue(taskData);
            BugtrackingManager.LOG.log(Level.FINE, "created issue {0} ", new Object[] {id}); // NOI18N
            readIssue(entry);
            entry.status = Issue.ISSUE_STATUS_NEW;
        } else {
            setTaskData(entry.issue, taskData);
            BugtrackingManager.LOG.log(Level.FINE, "set task data for issue {0} ", new Object[] {id}); // NOI18N
        }
        if(entry.seenAttributes != null) {
            if(entry.wasSeen()) {
                BugtrackingManager.LOG.log(Level.FINE, " issue {0} was seen", new Object[] {id}); // NOI18N
                if(isChanged(entry.seenAttributes, entry.issue.getAttributes())) {
                    BugtrackingManager.LOG.log(Level.FINE, " issue {0} is changed", new Object[] {id}); // NOI18N
                    storeIssue(entry);
                    entry.seen = false;
                    entry.status= Issue.ISSUE_STATUS_MODIFIED;
                } else {
                    BugtrackingManager.LOG.log(Level.FINE, " issue {0} isn't changed", new Object[] {id}); // NOI18N
                    // keep old values
                }
            } else {
                if(isChanged(entry.seenAttributes, entry.issue.getAttributes())) {
                    BugtrackingManager.LOG.log(Level.FINE, " issue {0} is changed", new Object[] {id}); // NOI18N
                    entry.seen = false; 
                    entry.status= Issue.ISSUE_STATUS_MODIFIED;
                } else {
                    BugtrackingManager.LOG.log(Level.FINE, " issue {0} isn't changed", new Object[] {id}); // NOI18N
                    entry.seenAttributes = null;
                    entry.seen = false;
                    entry.status= Issue.ISSUE_STATUS_NEW;
                }
            }
        } 
        return entry.issue;
    }

    public synchronized void setSeen(String id, boolean seen) throws IOException {
        BugtrackingManager.LOG.log(Level.FINE, "setting seen {0} for issue {1}", new Object[] {seen, id}); // NOI18N
        assert !SwingUtilities.isEventDispatchThread();
        final IssueEntry entry = getCache().get(id);
        assert entry != null;
        if(seen) {
            entry.seenAttributes = entry.issue.getAttributes();
        } 
        entry.seen = seen;
        storeIssue(entry);
    }

    public synchronized boolean wasSeen(String id) {
        IssueEntry entry = getCache().get(id);
        boolean seen = entry != null ? entry.seen : false;
        BugtrackingManager.LOG.log(Level.FINE, "returning seen {0} for issue {1}", new Object[] {seen, id}); // NOI18N
        return seen;
    }

    public synchronized Issue getIssue(String id) {
        return getCache().get(id).issue;
    }

    public synchronized int getStatus(String id) {
        IssueEntry entry = getCache().get(id);
        if(entry == null) {
            BugtrackingManager.LOG.log(Level.FINE, "returning UKNOWN status for issue {0}", new Object[] {id}); // NOI18N
            return Issue.ISSUE_STATUS_UNKNOWN;
        }
        if(entry.seen) {
            BugtrackingManager.LOG.log(Level.FINE, "returning SEEN status for issue {0}", new Object[] {id}); // NOI18N
            return Issue.ISSUE_STATUS_SEEN;
        }
        BugtrackingManager.LOG.log(Level.FINE, "returning status {0} for issue {1}", new Object[] {entry.status, id}); // NOI18N
        return entry.status;
    }

    public synchronized void storeQuery(String name, String[] ids) {
        try {
            IssueStorage.getInstance().storeQuery(nameSpace, name, ids);
        } catch (IOException ex) {
            BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
        }
    }

    public synchronized List<String> readQuery(String name) {
        try {
            return IssueStorage.getInstance().readQuery(nameSpace, name);
        } catch (IOException ex) {
            BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
        }
        return new ArrayList<String>(0);
    }

    private Map<String, IssueEntry> getCache() {
        if(cache == null) {
            cache = new HashMap<String, IssueEntry>();
        }
        return cache;
    }

    public synchronized Map<String, String> getSeenAttributes(String id) {
        IssueEntry entry = getCache().get(id);
        return entry.seenAttributes != null ? entry.seenAttributes : null;
    }

    private synchronized void readIssue(IssueEntry entry) {
        try {
            IssueStorage.getInstance().readIssue(nameSpace, entry);
        } catch (IOException ex) {
            BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void storeIssue(IssueEntry entry) throws IOException {
        IssueStorage.getInstance().storeIssue(nameSpace, entry);
    }

    private boolean isChanged(Map<String, String> oldAttr, Map<String, String> newAttr) {
        if(oldAttr == null) {
            return false; // can't be changed if it wasn't seen yet
        }
        for (Entry<String, String> e : oldAttr.entrySet()) {
            String newValue = newAttr.get(e.getKey());
            String oldValue = e.getValue();
            if(newValue == null && oldValue == null) continue;
            if(newValue == null || !newValue.trim().equals(oldValue.trim())) return true;
        }
        return false;
    }

    static class IssueEntry {
        private Issue issue;
        private Map<String, String> seenAttributes;
        private int status;
        private boolean seen;
        public IssueEntry() { }
        public boolean wasSeen() {
            return seen;
        }
        public Map<String, String> getSeenAttributes() {
            return seenAttributes;
        }
        public int getStatus() {
            return status;
        }
        public void setSeen(boolean seen) {
            this.seen = seen;
        }
        public void setSeenAttributes(Map<String, String> seenAttributes) {
            this.seenAttributes = seenAttributes;
        }
        public String getId() {
            return issue.getID();
        }
    }
}
