/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.api.Issue;

/**
 *
 * @author Tomas Stupka
 */
public class IssueCache<I> {

    public enum Status {
        /**
         * No information available
         */
        ISSUE_STATUS_UNKNOWN(0),
        /**
         * Issue was seen
         */
        ISSUE_STATUS_SEEN(2),
        /**
         * Issue wasn't seen yet
         */
        ISSUE_STATUS_NEW(4),
        /**
         * Issue was remotely modified since the last time it was seen
         */
        ISSUE_STATUS_MODIFIED(8);
        
        /* used by IssueStorage */
        private final int val;

        private Status(int i) {
            this.val = i;
        }
        int getVal() {
            return val;
        }
    }
    
    /**
     * Seen, New or Modified
     */
    public static final EnumSet<Status> ISSUE_STATUS_ALL = EnumSet.of(
            Status.ISSUE_STATUS_NEW,
            Status.ISSUE_STATUS_MODIFIED,
            Status.ISSUE_STATUS_SEEN);
    /**
     * New or modified
     */
    public static final EnumSet<Status> ISSUE_STATUS_NOT_SEEN = EnumSet.of(
            Status.ISSUE_STATUS_NEW,
            Status.ISSUE_STATUS_MODIFIED);

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache");
    private Map<String, IssueEntry> cache;
    private Map<String, Map<String, String>> lastSeenAttributes;

    private final String nameSpace;

    private final Object CACHE_LOCK = new Object();
    private final long referenceTime;
    private final IssueAccessor<I> issueAccessor;

    /**
     *
     * Provides access to the particular {@link Issue} implementations
     * kept in {@link IssueCache}
     *
     * @param <I>
     */
    public interface IssueAccessor<I> {

        /**
         * Returns attributes to be stored for the given Issue once it was seen
         * @return
         */
        public Map<String, String> getAttributes(I issue);

        /**
         * Returns the last modification time for the given issue
         *
         * @issue issue
         * @return the last modification time
         */
        public long getLastModified(I issue);

        /**
         * Returns the time the issue was created
         *
         * @issue issue
         * @return the last modification time
         */
        public long getCreated(I issue);

    }

    /**
     * Creates a new IssueCache
     *
     * @param nameSpace
     * @param issueAccessor
     */
    public IssueCache(String nameSpace, IssueAccessor<I> issueAccessor) {
        assert issueAccessor != null;
        this.nameSpace = nameSpace;
        this.issueAccessor = issueAccessor;
        
        long t = System.currentTimeMillis(); // fallback
        try {
            t = IssueStorage.getInstance().getReferenceTime(nameSpace);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        this.referenceTime = t;

        BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
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

    /**
     * Sets new data into the given issue
     *
     * @param issue issue
     * @param issueData data representing an issue
     * @throws IOException
     */
    public I setIssueData(String id, I issue) throws IOException {
        assert id != null && !id.equals("");

        synchronized(CACHE_LOCK) {
            IssueEntry entry = getCache().get(id);

            if(entry == null) {
                entry = createNewEntry(id);
            }

            entry.issue = issue;
            LOG.log(Level.FINE, "setting issue {0} ", new Object[] {id}); // NOI18N
            if(!entry.wasRead()) {
                readIssue(entry);
                Map<String, String> attr = entry.getSeenAttributes();
                if(attr == null || attr.isEmpty()) {
                    // first timer -> this means the issue was loaded for the first time in the IDE 
                    if(referenceTime >= issueAccessor.getLastModified(entry.issue)) {
                        setSeen(id, true);
                    } else if(referenceTime >= issueAccessor.getCreated(entry.issue)) {
                        entry.seenAttributes = issueAccessor.getAttributes(entry.issue);
                        storeIssue(entry);
                    }
                }
            }

            if(entry.seenAttributes != null) {
                if(entry.wasSeen()) {
                    LOG.log(Level.FINE, " issue {0} was seen", new Object[] {id}); // NOI18N
                    long lastModified = issueAccessor.getLastModified(entry.issue);
                    if(entry.lastSeenModified < lastModified) {
                        LOG.log(Level.FINE, " issue {0} is changed", new Object[] {id}); // NOI18N
                        if(entry.lastSeenModified >= lastModified) {
                            LOG.log(Level.WARNING, " issue '{'0'}' changed, yet last known modify > last modify. [{0},{1}]", new Object[]{entry.lastSeenModified, lastModified}); // NOI18N
                        }
                        storeIssue(entry);
                        entry.seen = false;
                        entry.status= Status.ISSUE_STATUS_MODIFIED;
                    } else {
                        LOG.log(Level.FINE, " issue {0} isn't changed", new Object[] {id}); // NOI18N
                        // keep old values
                    }
                } else {
                    LOG.log(Level.FINE, " issue {0} wasn't seen yet", new Object[] {id}); // NOI18N
                    if(referenceTime < issueAccessor.getLastModified(entry.issue))
                    {
                        LOG.log(Level.FINE, " issue {0} is changed", new Object[] {id}); // NOI18N
                        entry.seen = false;
                        entry.status= Status.ISSUE_STATUS_MODIFIED;
                    } else {
                        LOG.log(Level.FINE, " issue {0} isn't changed", new Object[] {id}); // NOI18N
                        entry.seenAttributes = null;
                        entry.seen = false;
                        entry.status= Status.ISSUE_STATUS_NEW;
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
        LOG.log(Level.FINE, "setting seen {0} for issue {1}", new Object[] {seen, id}); // NOI18N
        assert !SwingUtilities.isEventDispatchThread();
        IssueEntry entry;
        synchronized(CACHE_LOCK) {
            entry = getCache().get(id);
            assert entry != null && entry.issue != null;
            if(seen) {
                getLastSeenAttributes().put(id, entry.seenAttributes);
                entry.seenAttributes = issueAccessor.getAttributes(entry.issue);
                entry.lastSeenModified = issueAccessor.getLastModified(entry.issue);
                entry.lastUnseenStatus = entry.status;
            } else {
                entry.seenAttributes = getLastSeenAttributes().get(id);
                if(entry.lastUnseenStatus != Status.ISSUE_STATUS_UNKNOWN) {
                    entry.status = entry.lastUnseenStatus;
                    if(entry.seenAttributes == null) {
                        // no need to set the attributes once they have been set already
                        entry.seenAttributes = issueAccessor.getAttributes(entry.issue);
                    }
                }
            }
            entry.seen = seen;
            storeIssue(entry);
        }
    }

    /**
     * Determines whether the {@link Issue} with the given id was seen or unseen.
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
        LOG.log(Level.FINE, "returning seen {0} for issue {1}", new Object[] {seen, id}); // NOI18N
        return seen;
    }

    /**
     * Returns the last seen attributes for the issue with the given id.
     *
     * @param id issue id
     * @return last seen attributes
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
     * Returns a the issue instance with the given id
     *
     * @param id issue id
     * @return the {@link Issue} with the given id or null if not known yet
     */
    public I getIssue(String id) {
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
    public Status getStatus(String id) {
        synchronized(CACHE_LOCK) {
            IssueEntry entry = getCache().get(id);
            if(entry == null ) {
                LOG.log(Level.FINE, "returning UKNOWN status for issue {0}", new Object[] {id}); // NOI18N
                return Status.ISSUE_STATUS_UNKNOWN;
            }
            if(entry.seen) {
                LOG.log(Level.FINE, "returning SEEN status for issue {0}", new Object[] {id}); // NOI18N
                return Status.ISSUE_STATUS_SEEN;
            }
            LOG.log(Level.FINE, "returning status {0} for issue {1}", new Object[] {entry.status, id}); // NOI18N
            return entry.status;
        }
    }

    /**
     * Stores the given id-s for a query
     * @param name query name
     * @param ids id-s
     */
    public void storeQueryIssues(String name, String[] ids) {
        synchronized(CACHE_LOCK) {
            try {
                IssueStorage.getInstance().storeQuery(nameSpace, name, ids);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
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
     * @return list of id-s
     */
    public List<String> readQueryIssues(String name) {
        synchronized(CACHE_LOCK) {
            try {
                return IssueStorage.getInstance().readQuery(nameSpace, name);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            return new ArrayList<String>(0);
        }
    }

    /**
     * Stores the given id-s as archived
     *
     * @param name query name
     * @param ids issues id-s
     */
    public void storeArchivedQueryIssues(String name, String[] ids) {
        synchronized(CACHE_LOCK) {
            try {
                IssueStorage.getInstance().storeArchivedQueryIssues(nameSpace, name, ids);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
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
                LOG.log(Level.SEVERE, null, ex);
            }
            return new ArrayList<String>(0);
        }
    }

    /**
     *
     * Removes all data associated with a query from the storage.
     *
     * @param name query name
     */
    public void removeQuery(String name) {
        synchronized(CACHE_LOCK) {
            try {
                IssueStorage.getInstance().removeQuery(nameSpace, name);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * for testing purposes
     */
    void setEntryValues(String id, Status status, boolean seen) {
        synchronized(CACHE_LOCK) {
            IssueEntry entry = getCache().get(id);
            assert entry != null;
            entry.status = status;
            entry.seen = seen;
        }
    }
    
    private IssueEntry createNewEntry(String id) {
        IssueEntry entry = new IssueEntry();
        entry.id = id;
        entry.status = Status.ISSUE_STATUS_NEW;
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
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void storeIssue(IssueEntry entry) throws IOException {
        IssueStorage.getInstance().storeIssue(nameSpace, entry);
    }

    class IssueEntry {
        private I issue;
        private Map<String, String> seenAttributes;
        private Status status;
        private boolean seen = false;
        private String id;
        private long lastSeenModified = -1;
        private Status lastUnseenStatus = Status.ISSUE_STATUS_UNKNOWN;

        IssueEntry() { }

        IssueEntry(I issue, String id, Map<String, String> seenAttributes, Status status, Status lastUnseenStatus, boolean seen, long lastKnownModified) {
            this.issue = issue;
            this.id = id;
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
        public Status getStatus() {
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
        public Status getLastUnseenStatus() {
            return lastUnseenStatus;
        }
        public void setLastUnseenStatus(Status lastUnseenStatus) {
            this.lastUnseenStatus = lastUnseenStatus;
        }
        private boolean wasRead() {
            return seenAttributes != null;
        }
    }
}
