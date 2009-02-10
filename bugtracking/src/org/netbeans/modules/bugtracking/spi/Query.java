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
 *
 * @author Tomas Stupka
 */
public abstract class Query implements Comparable<Query> {

    private final PropertyChangeSupport support;
    
    private static Filter[] TABLE_FILTERS = new Filter[] {
        new AllFilter(),
        new UnseenFilter(),
        new RemovedFilter()
    };

    public Query() {
        this.support = new PropertyChangeSupport(this);
    }

    private List<QueryNotifyListener> notifyListeners;
    private IssueTable issueTable;
    private boolean saved;

    /**
     *
     * @return
     */
    public abstract String getDisplayName();

    /**
     *
     * @return
     */
    public abstract String getTooltip();

    /**
     *
     * @return
     */
    public abstract BugtrackingController getController();


    /*********
     * DATA
     *********/

    protected void setSaved(boolean saved) {
        this.saved = saved;
        getIssueTable().initColumns();
    }

    public boolean isSaved() {
        return saved;
    }
    
    /**
     *
     * @param refresh
     * @return
     */
    public abstract Issue[] getIssues();

    /**
     *
     * @param criteria
     * @return
     */
    // XXX Shouldn't be called while running
    public Issue[] getIssues(String criteria) {
        return BugtrackingUtil.getByIdOrSummary(getIssues(), criteria);
    }

    /**
     *
     * @param criteria
     * // XXX return issues?
     */
    public abstract void simpleSearch(String criteria);

    public int compareTo(Query q) {
        if(q == null) {
            return 1;
        }
        return getDisplayName().compareTo(q.getDisplayName());
    }


    protected void dataChanged() {
        getIssueTable().queryDataChanged();
    }

    /*********
     * TABLE
     *********/

    public JComponent getTableComponent() {
        return getIssueTable().getComponent();
    }

    public static class ColumnDescriptor extends ReadOnly {
        public ColumnDescriptor(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }

    public abstract ColumnDescriptor[] getColumnDescriptors(); 

    public Filter[] getFilters() {
        return TABLE_FILTERS;
    }

    public void setFilter(Filter filter) {
        issueTable.setFilter(filter);
    }

    /*********
     * EVENTS
     *********/

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
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

    protected void fireFinnished() {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.finnished();
        }
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
    private static class UnseenFilter extends Filter {
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Query.class, "LBL_UnseenIssuesFilter");
        }
        @Override
        public boolean accept(Issue issue) {
            return !issue.wasSeen();
        }
    }
    private static class RemovedFilter extends Filter {
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Query.class, "LBL_RemovedIssuesFilter");
        }
        @Override
        public boolean accept(Issue issue) {
            return true; // XXX TBD
        }
    }

}
