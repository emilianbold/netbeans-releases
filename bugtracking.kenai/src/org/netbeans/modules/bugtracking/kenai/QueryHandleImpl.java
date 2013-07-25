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

package org.netbeans.modules.bugtracking.kenai;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.team.spi.TeamUtil;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.team.server.ui.spi.QueryHandle;
import org.netbeans.modules.team.server.ui.spi.QueryResultHandle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Stupka
 */
class QueryHandleImpl extends QueryHandle implements QueryDescriptor, ActionListener, PropertyChangeListener {
    private final Query query;
    private final PropertyChangeSupport changeSupport;
    protected final boolean predefined;
    private Collection<Issue> issues = Collections.emptyList();
    private String stringValue;
    protected boolean needsRefresh;

    QueryHandleImpl(Query query, boolean needsRefresh, boolean predefined) {
        this.query = query;
        this.needsRefresh = needsRefresh;
        this.predefined = predefined;
        changeSupport = new PropertyChangeSupport(query);
        query.addPropertyChangeListener(WeakListeners.propertyChange(this, query));
        registerIssues();
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
        TeamUtil.openQuery(query, Query.QueryMode.SHOW_ALL, true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Query.EVENT_QUERY_ISSUES_CHANGED)) {
            registerIssues();
            changeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_QUERY_RESULT, null, getQueryResults())); // XXX add result handles
        } else if(evt.getPropertyName().equals(IssueStatusProvider.EVENT_SEEN_CHANGED)) {
            changeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_QUERY_RESULT, null, getQueryResults())); // XXX add result handles
        } 
    }

    List<QueryResultHandle> getQueryResults() {
        List<QueryResultHandle> ret = new ArrayList<QueryResultHandle>();
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

    private void registerIssues() {
        issues = query.getIssues();
        for (Issue issue : issues) {
            issue.addPropertyChangeListener(WeakListeners.propertyChange(this, issue));
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

}
