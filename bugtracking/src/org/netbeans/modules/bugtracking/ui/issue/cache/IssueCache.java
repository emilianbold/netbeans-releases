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

package org.netbeans.modules.bugtracking.ui.issue.cache;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.bugtracking.spi.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.BugtrackingManager;

/**
 *
 * @author Tomas Stupka
 */
public class IssueCache<T> {

    /**
     * No information available
     */
    public static final int ISSUE_STATUS_UNKNOWN = 0;
    /**
     * Issue was seen
     */
    public static final int ISSUE_STATUS_SEEN = 2;
    /**
     * Issue wasn't seen yet
     */
    public static final int ISSUE_STATUS_NEW = 4;
    /**
     * Issue was remotely modified since the last time it was seen
     */
    public static final int ISSUE_STATUS_MODIFIED = 8;
    /**
     * Seen, New or Modified
     */
    public static final int ISSUE_STATUS_ALL =
            ISSUE_STATUS_NEW |
            ISSUE_STATUS_MODIFIED |
            ISSUE_STATUS_SEEN;
    /**
     * New or modified
     */
    public static final int ISSUE_STATUS_NOT_SEEN =
            ISSUE_STATUS_NEW |
            ISSUE_STATUS_MODIFIED;

    /**
     * issues seen state changed
     */
    public static final String EVENT_ISSUE_SEEN_CHANGED = "issue.seen_changed"; // NOI18N
    
    private Map<String, IssueEntry> cache;
    private final Map<String, PropertyChangeSupport> supports = new HashMap<String, PropertyChangeSupport>();
    private Map<String, Map<String, String>> lastSeenAttributes;

    private String nameSpace;

    private final Object CACHE_LOCK = new Object();
    private long referenceTime;
    private final IssueAccessor<T> issueAccessor;

    /**
     * 
     * Provides access to the particular {@link Issue} implementations
     * kept in {@link IssueCache}
     *
     * @param <T>
     */
    public interface IssueAccessor<T> {
        /**
         * Creates a new Issue for the given taskdata
         * @param taskData
         * @return
         */
        public Issue createIssue(T issueData);

        /**
         * Sets new task data in the issue. Mind synchrone reentrant calls
         * on the cache for the given issue.
         *
         * @param issue
         * @param taskData
         */
        public void setIssueData(Issue issue, T issueData);

        /**
         * Returns a description summarizing the changes made
         * in the given issue since the last time it was as seen.
         *
         * @return
         */
        public String getRecentChanges(Issue issue);

        /**
         * Returns the last modification time for the given issue
         *
         * @issue issue
         * @return the last modification time
         */
        public long getLastModified(Issue issue);

        /**
         * Returns the time the issue was created
         *
         * @issue issue
         * @return the last modification time
         */
        public long getCreated(Issue issue);

    }

    /**
     * Creates a new IssueCache
     * 
     * @param nameSpace
     * @param issueAccessor
     */
    public IssueCache(String nameSpace, IssueAccessor<T> issueAccessor) {
        assert issueAccessor != null;
        this.nameSpace = nameSpace;
        this.issueAccessor = issueAccessor;
        
        try {
            this.referenceTime = IssueStorage.getInstance().getReferenceTime(nameSpace);
        } catch (IOException ex) {
            referenceTime = System.currentTimeMillis(); // fallback
            BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
        }
        
        BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                synchronized(CACHE_LOCK) {
                    cleanup();
                }
            }
        });
    }

    /**
     * override in unit tests
     */
    protected void cleanup() {
        IssueStorage.getInstance().cleanup(IssueCache.this.nameSpace);
    }

    IssueAccessor<T> getIssueAccessor() {
        return issueAccessor;
    }

    /**
     * Sets new data into {@link Issue} with the given id. A new issue will be created
     * in case it doesn't exist yet.
     *
     * @param issue id
     * @param issueData data representing an issue
     * @return the {@link Issue} with the given id
     * @throws IOException
     */
    public Issue setIssueData(String id, T issueData) throws IOException {
        assert issueData != null;
        assert id != null && !id.equals("");
        return setIssueData(id, null, issueData);
    }

    /**
     * Sets new data into the given issue
     *
     * @param issue issue
     * @param issueData data representing an issue
     * @throws IOException
     */
    public void setIssueData(Issue issue, T issueData) throws IOException {
        assert issueData != null;
        assert issue != null;

        setIssueData(issue.getID(), issue, issueData);
    }

    private Issue setIssueData(String id, Issue issue, T issueData) throws IOException {
        assert issueData != null;
        assert id != null && !id.equals("");
        assert issue == null || issue.getID().equals(id);
        
        synchronized(CACHE_LOCK) {
            IssueEntry entry = getCache().get(id);

            if(entry == null) {
                entry = createNewEntry(id);
            }

            if(entry.issue == null) {
                if(issue != null) {
                    entry.issue = issue;
                    BugtrackingManager.LOG.log(Level.FINE, "setting task data for issue {0} ", new Object[] {id}); // NOI18N
                    issueAccessor.setIssueData(entry.issue, issueData);
                } else {
                    entry.issue = issueAccessor.createIssue(issueData);
                    BugtrackingManager.LOG.log(Level.FINE, "created issue {0} ", new Object[] {id}); // NOI18N
                    readIssue(entry);                    
                    Map<String, String> attr = entry.getSeenAttributes();
                    if(attr == null || attr.size() == 0) {
                        // firsttimer
                        if(referenceTime >= issueAccessor.getLastModified(entry.issue)) {
                            setSeen(id, true);
                        } else if(referenceTime >= issueAccessor.getCreated(entry.issue)) {
                            entry.seenAttributes = entry.issue.getAttributes();
                            storeIssue(entry);
                        }
                    }
                }
            } else {
                BugtrackingManager.LOG.log(Level.FINE, "setting task data for issue {0} ", new Object[] {id}); // NOI18N
                issueAccessor.setIssueData(entry.issue, issueData);
            }

            if(entry.seenAttributes != null) {
                if(entry.wasSeen()) {
                    BugtrackingManager.LOG.log(Level.FINE, " issue {0} was seen", new Object[] {id}); // NOI18N
                    long lastModified = issueAccessor.getLastModified(entry.issue);
                    if(isChanged(entry.seenAttributes, entry.issue.getAttributes()) || entry.lastSeenModified < lastModified) {
                        BugtrackingManager.LOG.log(Level.FINE, " issue {0} is changed", new Object[] {id}); // NOI18N
                        if(entry.lastSeenModified >= lastModified) {
                            BugtrackingManager.LOG.warning(" issue {0} changed, yet last known modify > last modify. [" + entry.lastSeenModified + "," + lastModified +"]"); // NOI18N
                        }
                        storeIssue(entry);
                        entry.seen = false;
                        entry.status= ISSUE_STATUS_MODIFIED;
                    } else {
                        BugtrackingManager.LOG.log(Level.FINE, " issue {0} isn't changed", new Object[] {id}); // NOI18N
                        // keep old values
                    }
                } else {
                    BugtrackingManager.LOG.log(Level.FINE, " issue {0} wasn't seen yet", new Object[] {id}); // NOI18N
                    if(isChanged(entry.seenAttributes, entry.issue.getAttributes()) ||
                       referenceTime < issueAccessor.getLastModified(entry.issue))
                    {
                        BugtrackingManager.LOG.log(Level.FINE, " issue {0} is changed", new Object[] {id}); // NOI18N
                        entry.seen = false;
                        entry.status= ISSUE_STATUS_MODIFIED;
                    } else {
                        BugtrackingManager.LOG.log(Level.FINE, " issue {0} isn't changed", new Object[] {id}); // NOI18N
                        entry.seenAttributes = null;
                        entry.seen = false;
                        entry.status= ISSUE_STATUS_NEW;
                    }
                }
            }
            return entry.issue;
        }
    }

    /**
     * Sets the {@link Issue} with the given id as seen, or unseen.
     *
     * @param id issue id
     * @param seen seen flag
     * @throws IOException
     */
    public void setSeen(String id, boolean seen) throws IOException {
        if (id == null) {
            return;
        }
        BugtrackingManager.LOG.log(Level.FINE, "setting seen {0} for issue {1}", new Object[] {seen, id}); // NOI18N
        assert !SwingUtilities.isEventDispatchThread();
        boolean oldValue;
        IssueEntry entry;
        synchronized(CACHE_LOCK) {
            oldValue = wasSeen(id);
            entry = getCache().get(id);
            assert entry != null && entry.issue != null;
            if(seen) {
                getLastSeenAttributes().put(id, entry.seenAttributes);
                entry.seenAttributes = entry.issue.getAttributes();
                entry.lastSeenModified = issueAccessor.getLastModified(entry.issue);
                entry.lastUnseenStatus = entry.status;
            } else {
                entry.seenAttributes = getLastSeenAttributes().get(id);
                if(entry.lastUnseenStatus != ISSUE_STATUS_UNKNOWN) {
                    entry.status = entry.lastUnseenStatus;
                    if(entry.seenAttributes == null) {
                        entry.seenAttributes = entry.issue.getAttributes();
                    }
                }
            }
            entry.seen = seen;
            storeIssue(entry);
        }
        fireSeenChanged(entry.issue, oldValue, seen);
    }

    /**
     * Determines wheter the {@link Issue} with the given id was seen or unseen.
     *
     * @param id issue id
     * @return true if issue was seen, otherwise false
     */
    public boolean wasSeen(String id) {
        IssueEntry entry;
        synchronized(CACHE_LOCK) {
            entry = getCache().get(id);
            if(entry == null) {
                entry = createNewEntry(id);
                readIssue(entry);
            }
        }
        boolean seen = entry != null ? entry.seen : false;
        BugtrackingManager.LOG.log(Level.FINE, "returning seen {0} for issue {1}", new Object[] {seen, id}); // NOI18N
        return seen;
    }

    /**
     * Returns the last seen attributes for the issue with the given id.
     * 
     * @param id issue id
     * @return last seen sttributes
     */
    public Map<String, String> getSeenAttributes(String id) {
        IssueEntry entry;
        synchronized(CACHE_LOCK) {
            entry = getCache().get(id);
            if(entry == null) {
                assert !SwingUtilities.isEventDispatchThread();
                entry = createNewEntry(id);
                readIssue(entry);
            }
            return entry.seenAttributes != null ? entry.seenAttributes : null;
        }
    }

    /**
     * Returns a {@link Issue} with the given id
     * 
     * @param id issue id
     * @return the {@link Issue} with the given id or null if not known yet
     */
    public Issue getIssue(String id) {
        synchronized(CACHE_LOCK) {
            IssueEntry entry = getCache().get(id);
            return (entry == null) ? null : entry.issue;
        }
    }

    /**
     * Returns status value for the {@link Issue} with the given id
     *
     * @param id issue id
     * @return issue status
     * @see #ISSUE_STATUS_UNKNOWN
     * @see #ISSUE_STATUS_NEW
     * @see #ISSUE_STATUS_MODIFIED
     * @see #ISSUE_STATUS_ALL
     * @see #ISSUE_STATUS_SEEN
     * @see #ISSUE_STATUS_NOT_SEEN
     */
    public int getStatus(String id) {
        synchronized(CACHE_LOCK) {
            IssueEntry entry = getCache().get(id);
            if(entry == null ) {
                BugtrackingManager.LOG.log(Level.FINE, "returning UKNOWN status for issue {0}", new Object[] {id}); // NOI18N
                return ISSUE_STATUS_UNKNOWN;
            }
            if(entry.seen) {
                BugtrackingManager.LOG.log(Level.FINE, "returning SEEN status for issue {0}", new Object[] {id}); // NOI18N
                return ISSUE_STATUS_SEEN;
            }
            BugtrackingManager.LOG.log(Level.FINE, "returning status {0} for issue {1}", new Object[] {entry.status, id}); // NOI18N
            return entry.status;
        }
    }

    /**
     * Stres the given id-s for a query
     * @param name query name
     * @param ids id-s
     */
    public void storeQueryIssues(String name, String[] ids) {
        synchronized(CACHE_LOCK) {
            try {
                IssueStorage.getInstance().storeQuery(nameSpace, name, ids);
            } catch (IOException ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Returns the timestamp when a queries issues were written the last time
     *
     * @param name query name
     * @return timestamp
     */
    public long getQueryTimestamp(String name) {
        return IssueStorage.getInstance().getQueryTimestamp(nameSpace, name);
    }

    /**
     * Returns the id-s stored for a query
     * @param name query name
     * @return list od id-s
     */
    public List<String> readQueryIssues(String name) {
        synchronized(CACHE_LOCK) {
            try {
                return IssueStorage.getInstance().readQuery(nameSpace, name);
            } catch (IOException ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            }
            return new ArrayList<String>(0);
        }
    }

    /**
     * Stores the given id-s as archived
     *
     * @param name query name
     * @param ids isues id-s
     */
    public void storeArchivedQueryIssues(String name, String[] ids) {
        synchronized(CACHE_LOCK) {
            try {
                IssueStorage.getInstance().storeArchivedQueryIssues(nameSpace, name, ids);
            } catch (IOException ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Returns the id-s for all issues stored as archived for the query with the given name
     *
     * @param name query name
     * @return list of id-s
     */
    public List<String> readArchivedQueryIssues(String name) {
        synchronized(CACHE_LOCK) {
            try {
                Map<String, Long> m = IssueStorage.getInstance().readArchivedQueryIssues(nameSpace, name);
                return new ArrayList<String>(m.keySet());
            } catch (IOException ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            }
            return new ArrayList<String>(0);
        }
    }

    /**
     * 
     * Removes all data assotiated with a query from the storage.
     *
     * @param name query name
     */
    public void removeQuery(String name) {
        synchronized(CACHE_LOCK) {
            try {
                IssueStorage.getInstance().removeQuery(nameSpace, name);
            } catch (IOException ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private IssueEntry createNewEntry(String id) {
        IssueEntry entry = new IssueEntry();
        entry.id = id;
        entry.status = ISSUE_STATUS_NEW;
        getCache().put(id, entry);
        return entry;
    }

    private Map<String, IssueEntry> getCache() {
        if(cache == null) {
            cache = new HashMap<String, IssueEntry>();
        }
        return cache;
    }

    private Map<String, Map<String, String>> getLastSeenAttributes() {
        if(lastSeenAttributes == null) {
            lastSeenAttributes = new HashMap<String, Map<String, String>>();
        }
        return lastSeenAttributes;
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

    void addPropertyChangeListener(Issue issue, PropertyChangeListener propertyChangeListener) {
        PropertyChangeSupport support = getChangeSupport(issue, true);
        support.addPropertyChangeListener(propertyChangeListener);
    }

    private PropertyChangeSupport getChangeSupport(Issue issue, boolean forceCreate) {
        PropertyChangeSupport support = supports.get(issue.getID());
        if (support == null && forceCreate) {
            support = new PropertyChangeSupport(issue);
            supports.put(issue.getID(), support);
        }
        return support;
    }

    void removePropertyChangeListener(Issue issue, PropertyChangeListener propertyChangeListener) {
        PropertyChangeSupport support = getChangeSupport(issue, true);
        support.removePropertyChangeListener(propertyChangeListener);
    }

    /**
     * Notify listeners on this issue that the seen state has chaged
     *
     * @param oldSeen the old seen state
     * @param newSeen the new seen state
     * @see #EVENT_ISSUE_SEEN_CHANGED
     */
    private void fireSeenChanged(Issue issue, boolean oldSeen, boolean newSeen) {
        PropertyChangeSupport support = getChangeSupport(issue, false);
        if(support != null) {
            support.firePropertyChange(EVENT_ISSUE_SEEN_CHANGED, oldSeen, newSeen);
        }
    }

    private boolean isChanged(Map<String, String> oldAttr, Map<String, String> newAttr) {
        if(oldAttr == null) {
            return false; // can't be changed if it wasn't seen yet
        }
        for (Entry<String, String> e : oldAttr.entrySet()) {
            String newValue = newAttr.get(e.getKey());
            String oldValue = e.getValue();
            if(newValue == null && oldValue == null) {
                continue;
            }
        }
        return false;
    }

    static class IssueEntry {
        private Issue issue;
        private Map<String, String> seenAttributes;
        private int status;
        private boolean seen = false;
        private String id;
        private long lastSeenModified = -1;
        private int lastUnseenStatus = ISSUE_STATUS_UNKNOWN;

        IssueEntry() { }

        IssueEntry(Issue issue, Map<String, String> seenAttributes, int status, int lastUnseenStatus, boolean seen, long lastKnownModified) {
            this.issue = issue;
            this.id = issue.getID();
            this.seenAttributes = seenAttributes;
            this.status = status;
            this.seen = seen;
            this.lastSeenModified = lastKnownModified;
            this.lastUnseenStatus = lastUnseenStatus;
        }
        
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
            return id;
        }
        public long getLastSeenModified() {
            return lastSeenModified;
        }
        public void setLastSeenModified(long lastKnownModified) {
            this.lastSeenModified = lastKnownModified;
        }
        public int getLastUnseenStatus() {
            return lastUnseenStatus;
        }
        public void setLastUnseenStatus(int lastUnseenStatus) {
            this.lastUnseenStatus = lastUnseenStatus;
        }
    }
}
