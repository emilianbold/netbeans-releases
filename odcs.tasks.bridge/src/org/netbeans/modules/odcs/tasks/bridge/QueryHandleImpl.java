/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.tasks.bridge;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Util;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.team.server.ui.spi.QueryHandle;
import static org.netbeans.modules.team.server.ui.spi.QueryHandle.PROP_QUERY_RESULT;
import org.netbeans.modules.team.server.ui.spi.QueryResultHandle;
import org.netbeans.modules.team.spi.TeamAccessorUtils;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 */
public class QueryHandleImpl extends QueryHandle implements QueryDescriptor, ActionListener, PropertyChangeListener {
    private final Query query;
    private final PropertyChangeSupport changeSupport;
    protected final boolean predefined;
    private String stringValue;
    protected boolean needsRefresh;

    private StatusChangedListener statusChangedListener;
    
    QueryHandleImpl(Query query, boolean needsRefresh) {
        this.query = query;
        this.needsRefresh = needsRefresh;
        this.predefined = isPredefined(query);
        changeSupport = new PropertyChangeSupport(query);
        query.addPropertyChangeListener(WeakListeners.propertyChange(this, query));
    }

    @Override
    public Query getQuery() {
        return query;
    }

    @Override
    public boolean isPredefined() {
        return predefined;
    }

    @Override
    public String getDisplayName() {
        return query.getDisplayName();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Util.selectQuery(query);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Query.EVENT_QUERY_REFRESHED)) {
            registerStatusCL();
            changeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_QUERY_RESULT, null, getQueryResults())); // XXX add result handles
        } else if(evt.getPropertyName().equals(IssueStatusProvider.EVENT_STATUS_CHANGED)) {
            changeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_QUERY_RESULT, null, getQueryResults())); // XXX add result handles
        } 
    }

    List<QueryResultHandle> getQueryResults() {
        List<QueryResultHandle> ret = new ArrayList<>();
        QueryResultHandle qh = QueryResultHandleImpl.forAllStatus(query);
        if(qh != null) {
            ret.add(qh);
        }
        qh = QueryResultHandleImpl.forNotSeenStatus(query);
        if(qh != null) {
            ret.add(qh);
        }
        qh = QueryResultHandleImpl.getAllChangedResult(query);
        if(qh != null) {
            ret.add(qh);
        }
        return ret;
    }

    synchronized void refreshIfNeeded() {
        if(needsRefresh) {
            needsRefresh = false;
            query.refresh();
        }
    }

    @Override
    public String toString() {
        if(stringValue == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");                                                     // NOI18N
            sb.append(query.getRepository().getDisplayName());
            sb.append(",");                                                     // NOI18N
            sb.append(query.getDisplayName());
            sb.append("]");                                                     // NOI18N
            stringValue = sb.toString();
        }
        return stringValue;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueryHandleImpl other = (QueryHandleImpl) obj;
        if ((this.toString() == null) ? (other.toString() != null) : !this.toString().equals(other.toString())) {
            return false;
        }
        return true;
    }

    void fireQueryActivated() {
        changeSupport.firePropertyChange(QueryHandle.PROP_QUERY_ACTIVATED, null, null);
    }

    // XXX HACK!
    private static boolean isPredefined(Query q) {
        String displayName = q.getDisplayName();
        if(displayName == null) {
            return false;
        }
        return displayName.equals(TeamAccessorUtils.ALL_ISSUES_QUERY_DISPLAY_NAME) ||
               displayName.equals(TeamAccessorUtils.MINE_ISSUES_QUERY_DISPLAY_NAME) || 
               displayName.equals(TeamAccessorUtils.OPEN_ISSUES_QUERY_DISPLAY_NAME) || 
               displayName.equals(TeamAccessorUtils.RECENT_ISSUES_QUERY_DISPLAY_NAME) || 
               displayName.equals(TeamAccessorUtils.RELATED_ISSUES_QUERY_DISPLAY_NAME); 
    }
    
    static boolean isAllIssues(Query q) {
        String displayName = q.getDisplayName();
        if(displayName == null) {
            return false;
        }
        return displayName.equals(TeamAccessorUtils.ALL_ISSUES_QUERY_DISPLAY_NAME); 
    }

    private Set<String> statusListeners;
    private void registerStatusCL() {
        synchronized(query) {
            if(statusListeners == null) {
                statusListeners = new ConcurrentSkipListSet<>();
            }
            if(statusChangedListener == null) {
                statusChangedListener = new StatusChangedListener();
            }
        }
        Collection<Issue> issues = query.getIssues();
        
        Set<String> ids = new HashSet<>();
        for (Issue issue : issues) {
            if(!statusListeners.contains(issue.getID())) {
                statusListeners.add(issue.getID());
                ids.add(issue.getID());
                issue.addPropertyChangeListener(WeakListeners.propertyChange(statusChangedListener, issue));
            }
        }
        statusListeners.retainAll(ids);
    }

    private class StatusChangedListener implements PropertyChangeListener {
        private RequestProcessor.Task statusChangeTask;
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            statusChangeTask = new RequestProcessor("ODCSQueryHandle - issue status change" + (query != null ? query.getDisplayName() : ""), 1).create(new Runnable() {
               @Override
               public void run() {
                   changeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_QUERY_RESULT, null, getQueryResults())); // XXX add result handles
               }
            });
            statusChangeTask.schedule(1000);
        }
    }
}
