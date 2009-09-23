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

package org.netbeans.modules.bugtracking.kenai;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiSupport;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.kenai.ui.spi.QueryResultHandle;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class QueryResultHandleImpl extends QueryResultHandle implements ActionListener {

    private final Query query;
    private final String label;
    private Filter filter;

    private static MessageFormat totalFormat = new MessageFormat(NbBundle.getMessage(QueryResultHandleImpl.class, "LBL_QueryResultTotal"));       // NOI18N
    private static MessageFormat unseenFormat = new MessageFormat(NbBundle.getMessage(QueryResultHandleImpl.class, "LBL_QueryResultUnseen"));     // NOI18N
    private static MessageFormat newFormat = new MessageFormat(NbBundle.getMessage(QueryResultHandleImpl.class, "LBL_QueryResultNew"));           // NOI18N

    QueryResultHandleImpl(Query query, String label, Filter filter) {
        this.query = query;
        this.label = label;
        this.filter = filter;
    }

    @Override
    public String getText() {
        return label;
    }

    public void actionPerformed(ActionEvent e) {

        // XXX this is a hack for now - filter should be set only for the one relevant support
        BugtrackingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        for (BugtrackingConnector c : connectors) {
            KenaiSupport support = c.getLookup().lookup(KenaiSupport.class);
            support.setFilter(query, filter);
        }
        QueryAction.openQuery(query, null, true);
    }

    static QueryResultHandleImpl forStatus(Query query, int status) {
        Issue[] issues;
        switch(status) {

            case IssueCache.ISSUE_STATUS_ALL:

                issues = query.getIssues(status);
                return new QueryResultHandleImpl(
                        query,
                        totalFormat.format(new Object[] {issues != null ? issues.length : 0}, new StringBuffer(), null).toString(),
                        Filter.getAllFilter(query));

            case IssueCache.ISSUE_STATUS_NOT_SEEN:

                int notIssues = 0;
                issues = query.getIssues(IssueCache.ISSUE_STATUS_NOT_SEEN);
                if(issues == null || issues.length == 0) {
                    return null;
                }
                notIssues = issues.length;

                StringBuffer label = new StringBuffer();
                unseenFormat.format(new Object[] {notIssues}, label, null);
                
                return new QueryResultHandleImpl(query, label.toString(), Filter.getNotSeenFilter());

            case IssueCache.ISSUE_STATUS_NEW:

                int newIssues = 0;
                issues = query.getIssues(IssueCache.ISSUE_STATUS_NEW);
                if(issues == null || issues.length == 0) {
                    return null;
                }
                newIssues = issues.length;

                label = new StringBuffer();
                newFormat.format(new Object[] {newIssues}, label, null);

                return new QueryResultHandleImpl(query, label.toString(), Filter.getNewFilter(query));

            default:
                throw new IllegalStateException("wrong status value [" + status + "]"); // NOI18N
        }
    }

}
