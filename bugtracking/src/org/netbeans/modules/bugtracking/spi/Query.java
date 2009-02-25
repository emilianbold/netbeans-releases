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

package org.netbeans.modules.bugtracking.spi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.bugtracking.ui.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.util.NbBundle;

/**
 * Represents an query on a bugtracing repository.
 *
 *
 * @author Tomas Stupka
 */
public abstract class Query implements Comparable<Query> {

    private final PropertyChangeSupport support;

    public static final int ISSUE_STATUS_UNKNOWN        = 0;
    public static final int ISSUE_STATUS_SEEN           = 2;
    public static final int ISSUE_STATUS_NEW            = 4; 
    public static final int ISSUE_STATUS_MODIFIED       = 8;
    public static final int ISSUE_STATUS_OBSOLETE       = 16;
    public static final int ISSUE_STATUS_NOT_OBSOLETE   =
                                ISSUE_STATUS_SEEN |
                                ISSUE_STATUS_NEW |
                                ISSUE_STATUS_MODIFIED;
    public static final int ISSUE_STATUS_NOT_SEEN   =
                                ISSUE_STATUS_NEW |
                                ISSUE_STATUS_MODIFIED;

    public static Filter FILTER_ALL = new AllFilter();
    public static Filter FILTER_NOT_SEEN = new NotSeenFilter();
    public Filter FILTER_OUTOFDATE = new OutOfDateFilter(this);

    /**
     * querie issues list was changed
     */
    public static String EVENT_QUERY_ISSUES_CHANGED = "bugtracking.query.issues_changed";

    /**
     * query was saved
     */
    public static String EVENT_QUERY_SAVED   = "bugtracking.query.saved";

    /**
     * qeury was removed
     */
    public static String EVENT_QUERY_REMOVED = "bugtracking.query.removed";


    private List<QueryNotifyListener> notifyListeners;
    private IssueTable issueTable;
    private boolean saved;
    private long lastRefresh = -1;

    /**
     * Creates a query
     */
    public Query() {
        this.support = new PropertyChangeSupport(this);
    }

    /**
     * Returns the queries display name
     * @return
     */
    public abstract String getDisplayName();

    /**
     * Returns the queries toltip
     * @return
     */
    public abstract String getTooltip();

    /**
     * Returns the {@link BugtrackignController} for this query
     * @return
     */
    public abstract BugtrackingController getController();

    /**
     * Returns the issue table filters for this query
     * @return
     */
    public Filter[] getFilters() {
        return new Filter[] {
            FILTER_ALL,
            FILTER_NOT_SEEN,
            new OutOfDateFilter(this)
        };    
    }

    /*********
     * DATA
     *********/

    /**
     * Runs the query against the remote repository
     */
    public abstract void refresh(); // XXX throw? // XXX do we need this in api
    
    /**
     * Sets te queries status as saved. The {@link IssueTable} assotiated with
     * this query will change its column layout
     *
     * @param saved
     */
    protected void setSaved(boolean saved) {
        this.saved = saved;
        fireQuerySaved();
        getIssueTable().initColumns();
    }

    /**
     * Returns true if query is saved
     * @return
     */
    public boolean isSaved() {
        return saved;
    }
    
    /**
     * Returns issue given by the last refresh
     * @return 
     */
    public abstract Issue[] getIssues(int includeStatus);

    public Issue[] getIssues() {
        return getIssues(~0);
    }

    /**
     * Returns all issues given by the last refresh for
     * which applies that their ID or summary contains the
     * given criteria string
     *
     * @param criteria
     * @return
     */
    // XXX Shouldn't be called while running
    // XXX on repository?
    public Issue[] getIssues(String criteria) {
        return BugtrackingUtil.getByIdOrSummary(getIssues(), criteria);
    }

    public int compareTo(Query q) {
        if(q == null) {
            return 1;
        }
        return getDisplayName().compareTo(q.getDisplayName());
    }

    /*********
     * TABLE
     *********/

    /**
     * Returns a visual component containig a table with this queries issues 
     * @return
     */
    public JComponent getTableComponent() {
        return getIssueTable().getComponent();
    }

    /**
     * Describes a particular column in the queries table
     */
    public static class ColumnDescriptor extends ReadOnly {
        public ColumnDescriptor(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }

    /**
     * Returns the columns descriptors for this queries table
     * @return
     */
    public abstract ColumnDescriptor[] getColumnDescriptors(); 

    public void setFilter(Filter filter) {
        getIssueTable().setFilter(filter);
    }

    public long getLastRefresh() {
        return lastRefresh;
    }

    public abstract int getIssueStatus(Issue issue);

    /*********
     * EVENTS
     *********/

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    protected void fireQuerySaved() {
        support.firePropertyChange(EVENT_QUERY_SAVED, null, null);
    }

    protected void fireQueryRemoved() {
        support.firePropertyChange(EVENT_QUERY_REMOVED, null, null);
    }

    protected void fireQueryIssuesChanged() {
        support.firePropertyChange(EVENT_QUERY_ISSUES_CHANGED, null, null);
    }

    public void addNotifyListener(QueryNotifyListener l) {
        List<QueryNotifyListener> list = getNotifyListeners();
        synchronized(list) {
            list.add(l);
        }
    }

    public void removeNotifyListener(QueryNotifyListener l) {
        List<QueryNotifyListener> list = getNotifyListeners();
        synchronized(list) {
            list.remove(l);
        }
    }

    protected void fireNotifyData(Issue issue) {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.notifyData(issue);
        }
    }

    protected void fireStarted() {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.started();
        }
    }

    protected void fireFinished() {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.finished();
        }
    }

    protected void executeQuery (Runnable r) {
        fireStarted();
        try {
            r.run();
        } finally {
            fireFinished();
            fireQueryIssuesChanged();
            setLastRefresh(System.currentTimeMillis());
        }
    }
    
    protected void setLastRefresh(long lastRefresh) {
        this.lastRefresh = lastRefresh;
    }

    private IssueTable getIssueTable() {
        if(issueTable == null) {
            issueTable = new IssueTable(this);
        }
        return issueTable;
    }

    private QueryNotifyListener[] getListeners() {
        List<QueryNotifyListener> list = getNotifyListeners();
        QueryNotifyListener[] listeners;
        synchronized (list) {
            listeners = list.toArray(new QueryNotifyListener[list.size()]);
        }
        return listeners;
    }

    private List<QueryNotifyListener> getNotifyListeners() {
        if(notifyListeners == null) {
            notifyListeners = new ArrayList<QueryNotifyListener>();
        }
        return notifyListeners;
    }

    public abstract static class Filter {
        public abstract String getDisplayName();
        public abstract boolean accept(Issue issue);
    }

    private static class AllFilter extends Filter {
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Query.class, "LBL_AllIssuesFilter");
        }
        @Override
        public boolean accept(Issue issue) {
            return true;
        }
    }
    private static class NotSeenFilter extends Filter {
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Query.class, "LBL_UnseenIssuesFilter");
        }
        @Override
        public boolean accept(Issue issue) {
            return !issue.wasSeen();
        }
    }
    private static class OutOfDateFilter extends Filter {
        private final Query query;

        public OutOfDateFilter(Query query) {
            this.query = query;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Query.class, "LBL_RemovedIssuesFilter");
        }
        @Override
        public boolean accept(Issue issue) {
            return query.getIssueStatus(issue) == Query.ISSUE_STATUS_OBSOLETE;
        }
    }

}
