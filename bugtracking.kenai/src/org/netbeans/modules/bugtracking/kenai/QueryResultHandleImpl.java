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
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedList;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.team.spi.TeamUtil;
import org.netbeans.modules.team.server.ui.spi.QueryResultHandle;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
class QueryResultHandleImpl extends QueryResultHandle implements ActionListener {

    private final Query query;
    private final String label;
    private final String tooltip;
    private final Query.QueryMode queryMode;
    private final ResultType type;

    private static MessageFormat totalFormat = new MessageFormat(NbBundle.getMessage(QueryResultHandleImpl.class, "LBL_QueryResultTotal"));       // NOI18N
    private static MessageFormat unseenFormat = new MessageFormat(NbBundle.getMessage(QueryResultHandleImpl.class, "LBL_QueryResultUnseen"));     // NOI18N
    private static MessageFormat newFormat = new MessageFormat(NbBundle.getMessage(QueryResultHandleImpl.class, "LBL_QueryResultNew"));           // NOI18N
    private static MessageFormat totalTooltipFormat = new MessageFormat(NbBundle.getMessage(QueryResultHandleImpl.class, "LBL_QueryResultTotalTooltip"));       // NOI18N
    private static MessageFormat unseenTooltipFormat = new MessageFormat(NbBundle.getMessage(QueryResultHandleImpl.class, "LBL_QueryResultUnseenTooltip"));     // NOI18N
    private static MessageFormat newTooltipFormat = new MessageFormat(NbBundle.getMessage(QueryResultHandleImpl.class, "LBL_QueryResultNewTooltip"));           // NOI18N

    QueryResultHandleImpl(Query query, String label, String tooltip, Query.QueryMode mode, ResultType type) {
        this.query = query;
        this.label = label;
        this.tooltip = tooltip;
        this.queryMode = mode;
        this.type = type;
    }

    @Override
    public String getText() {
        return label;
    }

    @Override
    public String getToolTipText() {
        return tooltip;
    }

    @Override
    public ResultType getResultType() {
        return type;
    }

    public void actionPerformed(ActionEvent e) {
        TeamUtil.openQuery(query, queryMode, true);
    }

    static QueryResultHandleImpl forAllStatus(Query query) {
        Collection<Issue> issues = query.getIssues();
        int issueCount = issues != null ? issues.size() : 0;
        return new QueryResultHandleImpl(
                query,
                totalFormat.format(new Object[] {issueCount}, new StringBuffer(), null).toString(),
                getTotalTooltip(issueCount),
                Query.QueryMode.SHOW_ALL,
                ResultType.NAMED_RESULT);
    }
    
    static QueryResultHandleImpl forNotSeenStatus(Query query) {
        Collection<Issue> c = query.getIssues();
        if(c == null || c.isEmpty()) {
            return null;
        }
        Collection<Issue> issues = new LinkedList<Issue>();
        for (Issue issue : c) {
            if(issue.getStatus() == Issue.Status.MODIFIED ||
               issue.getStatus() == Issue.Status.NEW) 
            {
                issues.add(issue);
            }
        }
        int unseenIssues = issues.size();

        String label = unseenFormat.format(new Object[] {unseenIssues}, new StringBuffer(), null).toString();
        String tooltip = getUnseenTooltip(unseenIssues);

        return new QueryResultHandleImpl(
                query,
                label,
                tooltip,
                Query.QueryMode.SHOW_NEW_OR_CHANGED,
                ResultType.NAMED_RESULT);
    }

    static QueryResultHandle getAllChangedResult(Query query) {
        int notIssues = 0;
        Collection<Issue> c = query.getIssues();
        Collection<Issue> issues = null;
        if(c != null || !c.isEmpty()) {
            issues = new LinkedList<Issue>();
            for (Issue issue : c) {
                if(issue.getStatus() == Issue.Status.MODIFIED ||
                    issue.getStatus() == Issue.Status.NEW) 
                {
                    issues.add(issue);
                }
            }
        }
        notIssues = issues != null ? issues.size() : 0;
        
        return new QueryResultHandleImpl(
                query,
                Integer.toString(notIssues),
                getUnseenTooltip(notIssues),
                Query.QueryMode.SHOW_NEW_OR_CHANGED,
                ResultType.ALL_CHANGES_RESULT);
    }

    private static String getTotalTooltip(int issueCount) {
        if(issueCount == 1) {
            return NbBundle.getMessage(QueryResultHandleImpl.class, "LBL_QueryResultTotal1Tooltip");
        } else {
            return totalTooltipFormat.format(new Object[]{issueCount}, new StringBuffer(), null).toString();
        }
    }

    private static String getNewTooltip(int newIssues) {
        if(newIssues == 1) {
            return NbBundle.getMessage(QueryResultHandleImpl.class, "LBL_QueryResultNew1Tooltip");
        } else {
            return newTooltipFormat.format(new Object[] {newIssues}, new StringBuffer(), null).toString();
        }
    }

    private static String getUnseenTooltip(int unseenIssues) {
        if(unseenIssues == 1) {
            return NbBundle.getMessage(QueryResultHandleImpl.class, "LBL_QueryResultUnseen1Tooltip");
        } else {
            return unseenTooltipFormat.format(new Object[]{unseenIssues}, new StringBuffer(), null).toString();
        }
    }


}
