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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.nodes.Node;

/**
 * Represents an query on a bugtracing repository.
 *
 *
 * @author Tomas Stupka
 */
public abstract class Query implements Comparable<Query> {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * queries issue list was changed
     */
    public final static String EVENT_QUERY_ISSUES_CHANGED = "bugtracking.query.issues_changed";   // NOI18N

    /**
     * query was saved
     */
    public final static String EVENT_QUERY_SAVED   = "bugtracking.query.saved";       // NOI18N

    /**
     * query was removed
     */
    public final static String EVENT_QUERY_REMOVED = "bugtracking.query.removed";     // NOI18N


    private List<QueryNotifyListener> notifyListeners;
    protected boolean saved;
    private long lastRefresh = -1;

    static {
        QueryAccessorImpl.create();
    }
    private Node[] selection;

    /**
     * Creates a query
     */
    public Query() {
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
     * XXX we don't need this. use get component instead and get rid of the BugtrackingController
     * @return
     */
    public abstract BugtrackingController getController();

    /**
     *
     * Returns this queries {@link Repository}
     *
     * @return {@link Repository}
     */
    public abstract Repository getRepository();

    /**
     * XXX should this realy be in the spi?
     * @param query
     */
    public static void openNew(Repository repository) {
        QueryAction.openQuery(null, repository);
    }

    /*********
     * DATA
     *********/

    /**
     * Refreshes this Query
     * @return true if the query was refreshed, otherwise false
     */
    public abstract boolean refresh();

    /**
     * Sets te queries status as saved. The {@link IssueTable} assotiated with
     * this query will change its column layout
     *
     * @param saved
     */
    protected void setSaved(boolean saved) {
        this.saved = saved;
        if(saved) {
            selection = null;
        }
        fireQuerySaved();
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
    // XXX used only by kenai - move out from spi
    public abstract Issue[] getIssues(int includeStatus);

    public Issue[] getIssues() {
        return getIssues(~0);
    }

    /**
     * Returns true if the issue does belong to the query
     * @param issue
     * @return
     */
    public abstract boolean contains(Issue issue);

    /**
     * Returns all issues given by the last refresh for
     * which applies that their ID or summary contains the
     * given criteria string
     *
     * @param criteria
     * @return
     */
    // XXX Shouldn't be called while running
    // XXX move to simple search

    public Issue[] getIssues(String criteria) {
        return BugtrackingUtil.getByIdOrSummary(getIssues(), criteria);
    }

    public int compareTo(Query q) {
        if(q == null) {
            return 1;
        }
        return getDisplayName().compareTo(q.getDisplayName());
    }    

    // XXX get rid of this
    public long getLastRefresh() {
        return lastRefresh;
    }

    // XXX used only by issue table - move out from spi
    public abstract int getIssueStatus(Issue issue);

    /*********
     * EVENTS
     *********/

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
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

    void setSelection(Node[] nodes) {
        this.selection = nodes;
}

    protected Node[] getSelection() {
        return selection;
    }
}
